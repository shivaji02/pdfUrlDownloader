package org.pdf.downloader.server;

import org.pdf.downloader.core.EnhancedDownloadManager;
import org.pdf.downloader.observer.ConsoleLogger;
import org.pdf.downloader.resolver.AttemptContextResolver;
import org.pdf.downloader.resolver.CleanAnchorResolver;
import org.pdf.downloader.utils.DirectoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Service
public class DownloadService {

    @Autowired
    private NotificationService notificationService;

    public EnhancedDownloadManager.DownloadResult runDownload(String url, String downloadDir,
                                                              int concurrency, int timeoutMinutes,
                                                              boolean followNestedPages) throws Exception {
        // Use DirectoryUtils for secure directory resolution (Downloads folder only)
        String targetDir = DirectoryUtils.resolveDownloadDirectory(downloadDir);
        
        // Use dynamic thread count based on expected file count
        int estimatedFiles = estimateFileCount(url); // Simple estimation
        int dynamicThreads = DirectoryUtils.getDynamicThreadCount(estimatedFiles);
        
        // Send notification for large downloads
        if (estimatedFiles > 10) {
            notificationService.sendLargeDownloadStarted(estimatedFiles, dynamicThreads, targetDir);
        }

        CleanAnchorResolver resolver = new CleanAnchorResolver();
        EnhancedDownloadManager manager = new EnhancedDownloadManager(
                resolver,
                new AttemptContextResolver(),
                new ConsoleLogger(),
                dynamicThreads,
                followNestedPages
        );

        try {
            // Start and wait synchronously with timeout
            java.util.concurrent.CompletableFuture<EnhancedDownloadManager.DownloadResult> future =
                    manager.downloadFromUrl(url, targetDir);
            EnhancedDownloadManager.DownloadResult result = future.get(timeoutMinutes, TimeUnit.MINUTES);
            
            // Send completion notification with download data
            notificationService.sendDownloadCompletionNotification(result, targetDir);
            
            return result;
        } finally {
            // Ensure resolver cleanup
            resolver.performFullCleanup();
            System.out.println("💨 Download service cleanup completed - resources freed!");
        }
    }
    
    /**
     * Simple estimation of file count from URL (placeholder implementation)
     */
    private int estimateFileCount(String url) {
        // This is a simple estimation - in practice, you might analyze the URL pattern
        // or do a preliminary scan
        if (url.contains("chapter") || url.contains("book") || url.contains("manual")) {
            return 15; // Assume multi-chapter document
        }
        return 5; // Default small download
    }

    /**
     * Compute a reasonable concurrency level dynamically based on system resources.
     * Network-bound downloads benefit from higher parallelism than CPU cores.
     */
    private int computeRecommendedConcurrency() {
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());

        // Base: 3x cores for I/O bound tasks, within sane limits
        int base = cores * 3;

        // Memory pressure adjustment
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        long used = rt.totalMemory() - rt.freeMemory();
        double usedPct = max > 0 ? (used * 1.0 / max) : 0.0;
        if (usedPct > 0.75) {
            base = Math.max(2, base / 2); // cut in half if high memory usage
        }

        // Env overrides
        int upperCap = 32;
        String envCap = System.getenv("DOWNLOAD_MAX_CONCURRENCY");
        if (envCap != null) {
            try { upperCap = Math.max(2, Integer.parseInt(envCap.trim())); } catch (NumberFormatException ignored) {}
        }

        int lowerCap = 4; // don't go too low unless constrained

        int recommended = Math.min(upperCap, Math.max(lowerCap, base));
        System.out.println("⚙️ Using dynamic concurrency: " + recommended + " (cores=" + cores + ")");
        return recommended;
    }

    public String resolveTargetDir(String dir) {
        String home = System.getProperty("user.home");
        // Base application folder under home
        String appBase = home + File.separator + "PDFAPP";

        String target = dir;
        if (target == null || target.trim().isEmpty()) {
            // Default to ~/PDFAPP
            target = appBase;
        } else {
            target = target.trim();
            // Expand leading ~ to user.home
            if (target.startsWith("~" + File.separator) || target.equals("~")) {
                target = home + target.substring(1);
            }
            // If not absolute, place inside ~/PDFAPP/<name>
            java.nio.file.Path p = java.nio.file.Paths.get(target);
            if (!p.isAbsolute()) {
                target = appBase + File.separator + target;
            }
        }

        if (!target.endsWith(File.separator)) {
            target = target + File.separator;
        }
        return target;
    }
}
