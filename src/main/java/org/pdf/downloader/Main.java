package org.pdf.downloader;

import org.pdf.downloader.core.EnhancedDownloadManager;
import org.pdf.downloader.resolver.CleanAnchorResolver;
import org.pdf.downloader.resolver.AttemptContextResolver;
import org.pdf.downloader.observer.ConsoleLogger;
import org.pdf.downloader.wrapper.ErrorHandlerWrapper;
import org.pdf.downloader.wrapper.NetworkRetryWrapper;
import org.pdf.downloader.wrapper.ThreadManagerWrapper;
import org.pdf.downloader.utils.MemoryMonitor;
import org.pdf.downloader.utils.DirectoryUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    
    public static void testDirectoryResolution() {
        System.out.println("=== DIRECTORY RESOLUTION TEST ===");
        
        // Test cases
        String[] testDirs = {null, "", "ICAI", "~/Documents/ICAI", "/tmp/test"};
        
        for (String testDir : testDirs) {
            System.out.println("Input: '" + testDir + "'");
            String resolved = DirectoryUtils.resolveDownloadDirectory(testDir);
            System.out.println("Output: '" + resolved + "'");
            System.out.println("---");
        }
        
        System.out.println("=== END TEST ===\n");
    }

    /**
     * Log detailed download location information
     */
    public static void logDownloadLocation(String actualDownloadDir) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📍 DOWNLOAD LOCATION DETAILS");
        System.out.println("=".repeat(60));
        
        try {
            File downloadDir = new File(actualDownloadDir);
            
            // Basic info
            System.out.println("📁 Download Directory: " + downloadDir.getAbsolutePath());
            System.out.println("🏠 User Home: " + System.getProperty("user.home"));
            System.out.println("💻 Current Working Directory: " + System.getProperty("user.dir"));
            
            // Directory status
            System.out.println("\n📊 Directory Status:");
            System.out.println("   ✅ Exists: " + downloadDir.exists());
            System.out.println("   📝 Writable: " + downloadDir.canWrite());
            System.out.println("   📖 Readable: " + downloadDir.canRead());
            System.out.println("   📂 Is Directory: " + downloadDir.isDirectory());
            
            // Space information
            long freeSpace = downloadDir.getFreeSpace();
            long totalSpace = downloadDir.getTotalSpace();
            long usableSpace = downloadDir.getUsableSpace();
            
            System.out.println("\n💾 Storage Information:");
            System.out.println("   Free Space: " + formatBytes(freeSpace));
            System.out.println("   Total Space: " + formatBytes(totalSpace));
            System.out.println("   Usable Space: " + formatBytes(usableSpace));
            
            // Current contents (if exists)
            if (downloadDir.exists() && downloadDir.isDirectory()) {
                File[] files = downloadDir.listFiles();
                if (files != null) {
                    System.out.println("\n📋 Current Contents (" + files.length + " items):");
                    if (files.length > 0) {
                        for (int i = 0; i < Math.min(files.length, 10); i++) {
                            File file = files[i];
                            String type = file.isDirectory() ? "📁" : "📄";
                            long size = file.length();
                            System.out.printf("   %s %s (%s)\n", 
                                type, file.getName(), formatBytes(size));
                        }
                        if (files.length > 10) {
                            System.out.println("   ... and " + (files.length - 10) + " more items");
                        }
                    } else {
                        System.out.println("   📭 Directory is empty");
                    }
                }
            }
            
            // Path breakdown
            System.out.println("\n🧭 Path Breakdown:");
            String[] pathParts = actualDownloadDir.split(File.separator.equals("\\") ? "\\\\" : File.separator);
            StringBuilder currentPath = new StringBuilder();
            for (int i = 0; i < pathParts.length; i++) {
                if (!pathParts[i].isEmpty()) {
                    currentPath.append(File.separator).append(pathParts[i]);
                    String indent = "  ".repeat(i);
                    System.out.println("   " + indent + "└─ " + pathParts[i]);
                }
            }
            
            // Master.txt preview location
            String masterFilePath = actualDownloadDir + "Master.txt";
            System.out.println("\n📝 Master File Location:");
            System.out.println("   " + masterFilePath);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting directory info: " + e.getMessage());
        }
        
        System.out.println("=".repeat(60) + "\n");
    }
    
    /**
     * Log post-download summary
     */
    public static void logPostDownloadSummary(String actualDownloadDir) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📦 DOWNLOAD COMPLETION SUMMARY");
        System.out.println("=".repeat(60));
        
        try {
            File downloadDir = new File(actualDownloadDir);
            
            if (downloadDir.exists() && downloadDir.isDirectory()) {
                File[] files = downloadDir.listFiles();
                if (files != null) {
                    int pdfCount = 0;
                    int otherCount = 0;
                    long totalSize = 0;
                    
                    for (File file : files) {
                        if (file.isFile()) {
                            totalSize += file.length();
                            if (file.getName().toLowerCase().endsWith(".pdf")) {
                                pdfCount++;
                            } else {
                                otherCount++;
                            }
                        }
                    }
                    
                    System.out.println("📊 Download Results:");
                    System.out.println("   📄 PDF Files: " + pdfCount);
                    System.out.println("   📁 Other Files: " + otherCount);
                    System.out.println("   📏 Total Size: " + formatBytes(totalSize));
                    
                    // List recent PDFs
                    if (pdfCount > 0) {
                        System.out.println("\n📋 Downloaded PDFs:");
                        int count = 0;
                        for (File file : files) {
                            if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf") && count < 5) {
                                System.out.printf("   📄 %s (%s)\n", 
                                    file.getName(), formatBytes(file.length()));
                                count++;
                            }
                        }
                        if (pdfCount > 5) {
                            System.out.println("   ... and " + (pdfCount - 5) + " more PDFs");
                        }
                    }
                    
                    // Master.txt info
                    File masterFile = new File(actualDownloadDir + "Master.txt");
                    if (masterFile.exists()) {
                        System.out.println("\n📝 Master File Created:");
                        System.out.println("   📍 Location: " + masterFile.getAbsolutePath());
                        System.out.println("   📏 Size: " + formatBytes(masterFile.length()));
                    }
                }
            }
            
            System.out.println("\n🎯 Quick Access:");
            System.out.println("   🖱️  Open in Finder/Explorer: " + actualDownloadDir);
            System.out.println("   📋 Master mapping file: " + actualDownloadDir + "Master.txt");
            
        } catch (Exception e) {
            System.err.println("❌ Error getting post-download info: " + e.getMessage());
        }
        
        System.out.println("=".repeat(60));
    }
    
    /**
     * Format bytes to human readable format
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    public static void main(String[] args) {
        testDirectoryResolution(); // Directory resolution test

        // Initialize memory monitoring
        MemoryMonitor.resetTimer();
        MemoryMonitor.logMemoryUsage("Application startup");
        
        System.out.println("🚀 Starting Enhanced ICAI PDF Downloader...");
        
        // Configuration
        String baseUrl = "https://www.icai.org/post/19133";
        
        // ✅ FIX: Use DirectoryUtils instead of hardcoded path
        String requestedDir = "ICAI"; // You can change this or make it configurable
        String actualDownloadDir = DirectoryUtils.resolveDownloadDirectory(requestedDir);
        
        // ✅ FIX: Ensure the directory exists
        DirectoryUtils.ensureDirectoryExists(actualDownloadDir);
        
        // ✅ NEW: Log detailed download location info
        logDownloadLocation(actualDownloadDir);
        
        System.out.println("📁 Final Download Directory: " + actualDownloadDir);
        
        // Create and execute with auto-tuned concurrency
        CleanAnchorResolver resolver = new CleanAnchorResolver();
        int autoTunedThreads = DirectoryUtils.getOptimalThreadCount();
        
        EnhancedDownloadManager manager = new EnhancedDownloadManager(
            resolver,
            new AttemptContextResolver(), 
            new ConsoleLogger(),
            autoTunedThreads
        );
        
        ThreadManagerWrapper threadWrapper = new ThreadManagerWrapper(manager);
        NetworkRetryWrapper networkWrapper = new NetworkRetryWrapper(threadWrapper);
        ErrorHandlerWrapper errorWrapper = new ErrorHandlerWrapper(networkWrapper);
        
        try {
            // ✅ FIX: Pass the resolved directory
            errorWrapper.executeDownload(baseUrl, actualDownloadDir);
            
        } finally {
            // ✅ NEW: Log post-download summary
            logPostDownloadSummary(actualDownloadDir);
            
            // Cleanup
            if (resolver != null) {
                resolver.performFullCleanup();
            }
            MemoryMonitor.logMemoryUsage("Application complete");
            
            // ✅ NEW: Final location reminder
            System.out.println("\n🎉 Download Complete!");
            System.out.println("📍 Your files are saved at: " + actualDownloadDir);
            System.out.println("📋 Check Master.txt for file mappings!");
        }
    }
}