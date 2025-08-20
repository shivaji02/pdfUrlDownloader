package org.pdf.downloader.core;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pdf.downloader.resolver.FileNameResolver;
import org.pdf.downloader.resolver.AttemptContextResolver;
import org.pdf.downloader.observer.DownloadObserver;
import org.pdf.downloader.model.DownloadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    private final PDFDownloader downloader;
    private final FileNameResolver nameResolver;
    private final AttemptContextResolver contextResolver;
    private final DownloadObserver observer;

    public DownloadManager(PDFDownloader downloader, FileNameResolver nameResolver,
                          AttemptContextResolver contextResolver, DownloadObserver observer) {
        this.downloader = downloader;
        this.nameResolver = nameResolver;
        this.contextResolver = contextResolver;
        this.observer = observer;
    }

    public void downloadFromUrl(String url, String downloadDir) {
        try {
            // Ensure download directory path ends with separator
            if (!downloadDir.endsWith(File.separator)) {
                downloadDir = downloadDir + File.separator;
            }

            // Create download directory if it doesn't exist
            File dir = new File(downloadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    observer.onStart("Created directory: " + downloadDir);
                } else {
                    observer.onError("Failed to create download directory: " + downloadDir);
                    return;
                }
            } else {
                observer.onStart("Using existing directory: " + downloadDir);
            }

            observer.onStart("Connecting to: " + url);

            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();

            Elements links = doc.select("a[href]");

            List<DownloadTask> tasks = new ArrayList<>();
            String context = contextResolver.resolveContext(doc);

            for (Element link : links) {
                String href = link.absUrl("href");
                if (isPdfLink(href)) {
                    String fileName = nameResolver.resolveFileName(link, context);
                    tasks.add(new DownloadTask(href, fileName, downloadDir));
                }
            }

            observer.onTasksIdentified(tasks.size());

            int successCount = 0;
            for (DownloadTask task : tasks) {
                try {
                    observer.onTaskStart(task);
                    downloader.download(task);
                    observer.onTaskComplete(task);
                    successCount++;
                } catch (Exception e) {
                    observer.onTaskError(task, e);
                }
            }

            observer.onComplete(successCount);

        } catch (IOException e) {
            observer.onError("Failed to connect to URL: " + e.getMessage());
        }
    }

    private boolean isPdfLink(String url) {
        return url.toLowerCase().endsWith(".pdf");
    }
}