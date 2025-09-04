package org.pdf.downloader;

import org.pdf.downloader.core.EnhancedDownloadManager;
import org.pdf.downloader.resolver.CleanAnchorResolver;
import org.pdf.downloader.resolver.AttemptContextResolver;
import org.pdf.downloader.observer.ConsoleLogger;
import org.pdf.downloader.wrapper.ErrorHandlerWrapper;
import org.pdf.downloader.wrapper.NetworkRetryWrapper;
import org.pdf.downloader.wrapper.ThreadManagerWrapper;
import org.pdf.downloader.utils.MemoryMonitor;

public class Main {
    public static void main(String[] args) {
        // Initialize memory monitoring
        MemoryMonitor.resetTimer();
        MemoryMonitor.logMemoryUsage("Application startup");
        
        System.out.println("🚀 Starting Enhanced ICAI PDF Downloader...");
        
        // Configuration
        String baseUrl = "https://boslive.icai.org/sm_chapter_details.php?p_id=100&m_id=136";
        String downloadDir = System.getProperty("user.home") + "/Documents/CA-F/AFM/";
        
        // Create and execute
        CleanAnchorResolver resolver = new CleanAnchorResolver();
        EnhancedDownloadManager manager = new EnhancedDownloadManager(
            resolver,
            new AttemptContextResolver(), 
            new ConsoleLogger(),
            4
        );
        
        ThreadManagerWrapper threadWrapper = new ThreadManagerWrapper(manager);
        NetworkRetryWrapper networkWrapper = new NetworkRetryWrapper(threadWrapper);
        ErrorHandlerWrapper errorWrapper = new ErrorHandlerWrapper(networkWrapper);
        
        try {
            errorWrapper.executeDownload(baseUrl, downloadDir);
        } finally {
            // Cleanup
            if (resolver != null) {
                resolver.performFullCleanup();
            }
            MemoryMonitor.logMemoryUsage("Application complete");
        }
    }
}