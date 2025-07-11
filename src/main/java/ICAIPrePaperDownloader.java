import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.URL;
import java.util.*;

public class ICAIPrePaperDownloader {

    private static final String BASE_URL = "https://www.icai.org/post/question-papers-final-course";
    private static final String BASE_DOWNLOAD_DIR = "/Users/Neosoft/Downloads/CA-F/PrevPapers/";

    private static final Map<String, String> paperShortNames = new HashMap<>();
    static {
        paperShortNames.put("Financial Reporting", "FR");
        paperShortNames.put("Advanced Financial Management", "AFM");
        paperShortNames.put("Advanced Auditing", "AUD");
    }

    static class DownloadEntry {
        String paperKey, attemptContext, url;
        int paperIndex;
        DownloadEntry(String paperKey, String attemptContext, String url, int paperIndex) {
            this.paperKey = paperKey;
            this.attemptContext = attemptContext;
            this.url = url;
            this.paperIndex = paperIndex;
        }
    }

    private static String indexToCode(int idx) {
        switch (idx) {
            case 1: return "m25";
            case 2: return "n24";
            case 3: return "m24";
            default: return "unk";
        }
    }

    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect(BASE_URL).get();
            Elements links = doc.select("a[href]");

            List<DownloadEntry> downloadList = new ArrayList<>();
            Map<String, Integer> attemptCounter = new HashMap<>();

            for (Element link : links) {
                String fileUrl = link.absUrl("href");
                String anchorText = link.text().trim();

                if (!fileUrl.endsWith(".pdf") || anchorText.isEmpty()) continue;

                // Match paper
                String paperKey = paperShortNames.entrySet().stream()
                        .filter(e -> anchorText.contains(e.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);
                if (paperKey == null) continue;

                // Find context
                Element parent = link.parent();
                while (parent != null && !parent.tagName().equals("ul")) {
                    parent = parent.parent();
                }
                String context = "UnknownAttempt";
                if (parent != null && parent.previousElementSibling() != null) {
                    context = parent.previousElementSibling().text().replaceAll("\\s+", "");
                }

                // Collect with sequential index per attempt+paper
                String key = context + "." + paperKey;
                int count = attemptCounter.getOrDefault(key, 0) + 1;
                attemptCounter.put(key, count);

                downloadList.add(new DownloadEntry(paperKey, context, fileUrl, count));
            }

            // Download with mapped names
            for (DownloadEntry entry : downloadList) {
                String attemptCode = indexToCode(entry.paperIndex);
                String attemptDirPath = BASE_DOWNLOAD_DIR + "/" + entry.attemptContext;
                File attemptDir = new File(attemptDirPath);
                if (!attemptDir.exists()) attemptDir.mkdirs();

                String fileName = entry.paperKey + "~" + attemptCode + ".pdf";
                String fullPath = attemptDirPath + "/" + fileName;

                System.out.println("⬇️  Downloading: " + fileName + " from " + entry.attemptContext);
                downloadFile(entry.url, fullPath);
            }

            System.out.println("✅ All selected attempt PDFs downloaded.");
        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void downloadFile(String urlStr, String dest) throws IOException {
        try (InputStream in = new URL(urlStr).openStream();
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }
    }
}
