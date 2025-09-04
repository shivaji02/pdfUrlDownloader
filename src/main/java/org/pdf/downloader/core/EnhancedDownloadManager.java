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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.MalformedURLException;
import java.net.URL;

public class EnhancedDownloadManager {
    private final MultiThreadedPDFDownloader downloader;
    private final FileNameResolver nameResolver;
    private final AttemptContextResolver contextResolver;
    private final DownloadObserver observer;
    private final ExecutorService executorService;
    private final int maxConcurrentDownloads;

    public EnhancedDownloadManager(FileNameResolver nameResolver, 
                                 AttemptContextResolver contextResolver, 
                                 DownloadObserver observer,
                                 int maxConcurrentDownloads) {
        this.nameResolver = nameResolver;
        this.contextResolver = contextResolver;
        this.observer = observer;
        this.maxConcurrentDownloads = maxConcurrentDownloads;
        this.executorService = Executors.newFixedThreadPool(maxConcurrentDownloads);
        this.downloader = new MultiThreadedPDFDownloader();
    }

    public CompletableFuture<DownloadResult> downloadFromUrl(String url, String downloadDir) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return performDownload(url, downloadDir);
            } catch (Exception e) {
                observer.onError("Download failed: " + e.getMessage());
                return new DownloadResult(0, 0, Collections.emptyList());
            }
        }, executorService);
    }

    private DownloadResult performDownload(String url, String downloadDir) throws IOException {
        // Setup directory
        setupDownloadDirectory(downloadDir);
        
        observer.onStart("Connecting to: " + url);
        
        // Parse webpage
        Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(15000)
            .get();
            
        Elements links = doc.select("a[href]");
        
        // Prepare download tasks (includes one-level crawl for nested PDF lists)
        List<DownloadTask> tasks = prepareTasks(url, links, doc, downloadDir);
        observer.onTasksIdentified(tasks.size());
        
        // Execute downloads concurrently
        return executeDownloads(tasks);
    }

    private List<DownloadTask> prepareTasks(String baseUrl, Elements links, Document doc, String downloadDir) {
        String context = contextResolver.resolveContext(doc);
        Set<String> seenUrls = new HashSet<>();
        List<DownloadTask> tasks = new ArrayList<>();

        // 1) Collect PDFs from the base page
        collectPdfTasksFromLinks(links, context, downloadDir, seenUrls, tasks);

        // 2) One-step crawl: follow non-PDF links within same host, collect PDFs from those pages
        String baseHost = getHostSafe(baseUrl);
        Set<String> visitedPages = new HashSet<>();
        for (Element link : links) {
            String href = link.absUrl("href");
            if (href == null || href.isEmpty()) continue;
            if (isPdfLink(href)) continue; // PDFs already handled
            if (!isHttp(href)) continue;
            String host = getHostSafe(href);
            if (!Objects.equals(baseHost, host)) continue; // stay on same site
            if (!visitedPages.add(normalizeUrl(href))) continue; // already visited

            try {
                Document child = Jsoup.connect(href)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
                Elements childLinks = child.select("a[href]");
                String childContext = contextResolver.resolveContext(child);
                collectPdfTasksFromLinks(childLinks, childContext, downloadDir, seenUrls, tasks);
            } catch (IOException e) {
                // Log but continue; child pages are optional
                observer.onError("Failed to crawl: " + href + " - " + e.getMessage());
            }
        }

        return tasks;
    }

    private void collectPdfTasksFromLinks(Elements links, String context, String downloadDir,
                                          Set<String> seenUrls, List<DownloadTask> tasks) {
        for (Element link : links) {
            String href = link.absUrl("href");
            if (isPdfLink(href) && seenUrls.add(normalizeUrl(href))) {
                String fileName = nameResolver.resolveFileName(link, context);
                tasks.add(new DownloadTask(href, fileName, downloadDir));
            }
        }
    }

    private DownloadResult executeDownloads(List<DownloadTask> tasks) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        
        // Use semaphore to control concurrent downloads
        Semaphore semaphore = new Semaphore(maxConcurrentDownloads);
        CountDownLatch latch = new CountDownLatch(tasks.size());
        
        for (DownloadTask task : tasks) {
            CompletableFuture.runAsync(() -> {
                try {
                    semaphore.acquire();
                    observer.onTaskStart(task);
                    
                    downloader.download(task);
                    
                    observer.onTaskComplete(task);
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    observer.onTaskError(task, e);
                    failureCount.incrementAndGet();
                    errors.add("Failed to download " + task.getFileName() + ": " + e.getMessage());
                } finally {
                    semaphore.release();
                    latch.countDown();
                }
            }, executorService);
        }
        
        try {
            // Wait for all downloads to complete
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            observer.onError("Downloads interrupted: " + e.getMessage());
        }
        
        observer.onComplete(successCount.get());
        return new DownloadResult(successCount.get(), failureCount.get(), errors);
    }

    private void setupDownloadDirectory(String downloadDir) throws IOException {
        if (!downloadDir.endsWith(File.separator)) {
            downloadDir = downloadDir + File.separator;
        }
        
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create download directory: " + downloadDir);
            }
            observer.onStart("Created directory: " + downloadDir);
        }
    }

    private boolean isPdfLink(String url) {
        return url.toLowerCase().endsWith(".pdf");
    }

    private boolean isHttp(String url) {
        String u = url.toLowerCase();
        return u.startsWith("http://") || u.startsWith("https://");
    }

    private String getHostSafe(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    private String normalizeUrl(String url) {
        // Basic normalization to avoid trivial duplicates
        if (url == null) return "";
        String u = url.trim();
        if (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        return u;
    }

    /**
     * Graceful shutdown of executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            System.out.println("🔄 Shutting down executor service...");
            executorService.shutdown();
        }
    }
    
    /**
     * Wait for executor service termination
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (executorService != null) {
            return executorService.awaitTermination(timeout, unit);
        }
        return true;
    }
    
    /**
     * Force immediate shutdown of executor service
     */
    public List<Runnable> shutdownNow() {
        if (executorService != null) {
            return executorService.shutdownNow();
        }
        return new ArrayList<>();
    }
    
    /**
     * Check if executor service is terminated
     */
    public boolean isTerminated() {
        if (executorService != null) {
            return executorService.isTerminated();
        }
        return true;
    }
    
    /**
     * Check if executor service is shutdown
     */
    public boolean isShutdown() {
        if (executorService != null) {
            return executorService.isShutdown();
        }
        return true;
    }

    // Result class to track download statistics
    public static class DownloadResult {
        private final int successCount;
        private final int failureCount;
        private final List<String> errors;

        public DownloadResult(int successCount, int failureCount, List<String> errors) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errors = new ArrayList<>(errors);
        }

        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public int getTotalAttempted() { return successCount + failureCount; }
    }
}
