package org.pdf.downloader.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryUtils {
    
    private static final String SAFE_BASE_DIR = "Downloads";
    private static final String DEFAULT_APP_DIR = "PDFAPP";
    
    /**
     * Resolves download directory - RESTRICTED to Downloads folder only for security
     */
    public static String resolveDownloadDirectory(String requestedDir) {
        String homeDir = System.getProperty("user.home");
        String downloadsDir = Paths.get(homeDir, SAFE_BASE_DIR).toString();
        
        System.out.println("🏠 Home directory: " + homeDir);
        System.out.println("📥 Downloads directory: " + downloadsDir);
        
        // Handle null or empty - default to ~/Downloads/PDFAPP/
        if (requestedDir == null || requestedDir.trim().isEmpty()) {
            String defaultPath = Paths.get(downloadsDir, DEFAULT_APP_DIR).toString() + File.separator;
            System.out.println("📁 Using safe default path: " + defaultPath);
            return defaultPath;
        }
        
        requestedDir = requestedDir.trim();
        
        // Handle common patterns and clean up the path
        String cleanPath = requestedDir;
        
        // If it starts with /Users/username/PDFAPP, extract just the PDFAPP part and beyond
        if (cleanPath.startsWith("/Users/")) {
            int pdfappIndex = cleanPath.indexOf("PDFAPP");
            if (pdfappIndex > 0) {
                cleanPath = cleanPath.substring(pdfappIndex);
            }
        }
        
        // Remove leading slashes and tildes
        cleanPath = cleanPath.replaceAll("^[/\\\\~]+", "");
        
        // Security: Remove any ".." path traversal attempts
        cleanPath = cleanPath.replaceAll("\\.\\./", "").replaceAll("\\.\\.", "");
        
        // If it's empty after cleaning, use default
        if (cleanPath.isEmpty()) {
            cleanPath = DEFAULT_APP_DIR;
        }
        
        // Always create under Downloads folder, preserving subdirectory structure
        String safePath = Paths.get(downloadsDir, cleanPath).toString() + File.separator;
        System.out.println("🔒 Secured path within Downloads: " + safePath);
        return safePath;
    }
    
    /**
     * Ensure directory exists, create if necessary
     */
    public static void ensureDirectoryExists(String directoryPath) {
        File dir = new File(directoryPath);
        System.out.println("🔍 Checking directory: " + dir.getAbsolutePath());
        
        if (!dir.exists()) {
            System.out.println("📂 Directory doesn't exist, creating...");
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("✅ Created directory: " + dir.getAbsolutePath());
            } else {
                System.err.println("❌ Failed to create directory: " + dir.getAbsolutePath());
            }
        } else {
            System.out.println("✅ Directory already exists: " + dir.getAbsolutePath());
        }
    }
    
    /**
     * Dynamic thread count based on content size and system capabilities
     */
    public static int getOptimalThreadCount() {
        int cores = Runtime.getRuntime().availableProcessors();
        long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        
        // Base thread count on cores and memory
        int threadsByCore = Math.max(2, cores);
        int threadsByMemory = (int) Math.max(3, maxMemoryMB / 256); // 256MB per thread
        
        int optimalThreads = Math.min(threadsByCore, threadsByMemory);
        
        // Cap at reasonable limits based on system
        if (maxMemoryMB < 2048) { // Less than 2GB
            optimalThreads = Math.min(4, optimalThreads);
        } else if (maxMemoryMB < 4096) { // Less than 4GB
            optimalThreads = Math.min(8, optimalThreads);
        } else { // 4GB+
            optimalThreads = Math.min(12, optimalThreads);
        }
        
        System.out.println("🔧 Auto-tuned threads: " + optimalThreads + 
                          " (cores: " + cores + ", memory: " + maxMemoryMB + "MB)");
        
        return optimalThreads;
    }
    
    /**
     * Dynamic thread count based on file count - for large downloads
     */
    public static int getDynamicThreadCount(int fileCount) {
        int baseThreads = getOptimalThreadCount();
        
        if (fileCount <= 10) {
            return Math.max(2, baseThreads / 2); // Few files, use fewer threads
        } else if (fileCount <= 50) {
            return baseThreads; // Normal amount
        } else if (fileCount <= 200) {
            return Math.min(baseThreads + 2, 10); // More files, add threads
        } else {
            // Huge download - scale up significantly
            return Math.min(baseThreads + 4, 16);
        }
    }
    
    /**
     * Get the actual directory where files will be saved
     * (for API response)
     */
    public static String getNormalizedPath(String resolvedPath) {
        try {
            return Paths.get(resolvedPath).toAbsolutePath().normalize().toString();
        } catch (Exception e) {
            return resolvedPath;
        }
    }
}
