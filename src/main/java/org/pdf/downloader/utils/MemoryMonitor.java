package org.pdf.downloader.utils;

public class MemoryMonitor {
    
    private static long startTime = System.currentTimeMillis();
    
    public static void logMemoryUsage(String phase) {
        Runtime runtime = Runtime.getRuntime();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double usedMB = usedMemory / (1024.0 * 1024.0);
        double totalMB = totalMemory / (1024.0 * 1024.0);
        double maxMB = maxMemory / (1024.0 * 1024.0);
        double usedPercentage = (usedMemory * 100.0) / maxMemory;
        
        long elapsed = System.currentTimeMillis() - startTime;
        
        System.out.println("💾 [" + phase + "] Memory Status:");
        System.out.printf("   Used: %.1f MB / %.1f MB (%.1f%%)\n", usedMB, maxMB, usedPercentage);
        System.out.printf("   Total Allocated: %.1f MB\n", totalMB);
        System.out.printf("   Time Elapsed: %.1f seconds\n", elapsed / 1000.0);
        
        // Warning for high memory usage
        if (usedPercentage > 80) {
            System.err.println("⚠️  WARNING: High memory usage (" + String.format("%.1f", usedPercentage) + "%)");
            System.err.println("   Consider calling cleanup() or reducing concurrent downloads");
        }
    }
    
    public static void forceCleanup() {
        System.out.println("🧹 Forcing garbage collection...");
        long beforeGC = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        System.gc();
        System.runFinalization();
        
        // Give GC time to work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long afterGC = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freed = beforeGC - afterGC;
        
        if (freed > 0) {
            System.out.printf("✅ Garbage collection freed %.1f MB\n", freed / (1024.0 * 1024.0));
        }
    }
    
    public static void resetTimer() {
        startTime = System.currentTimeMillis();
    }
}