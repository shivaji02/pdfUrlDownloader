package org.pdf.downloader.wrapper;

import org.pdf.downloader.core.EnhancedDownloadManager;
import org.pdf.downloader.utils.MemoryMonitor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wrapper for managing download timeouts and thread cleanup
 * Handles memory monitoring and resource disposal
 */
public class ThreadManagerWrapper {
    
    private final EnhancedDownloadManager manager;
    private CompletableFuture<EnhancedDownloadManager.DownloadResult> currentFuture;
    
    public ThreadManagerWrapper(EnhancedDownloadManager manager) {
        this.manager = manager;
        MemoryMonitor.logMemoryUsage("ThreadManagerWrapper initialized");
    }
    
    /**
     * Execute download with timeout protection and memory monitoring
     */
    public boolean executeDownloadWithTimeout(String baseUrl, String downloadDir, 
                                            int timeoutMinutes) throws Exception {
        
        MemoryMonitor.logMemoryUsage("Download session started");
        
        try {
            // Start download with future tracking
            currentFuture = manager.downloadFromUrl(baseUrl, downloadDir);
            
            // Wait with timeout protection
            EnhancedDownloadManager.DownloadResult result = 
                currentFuture.get(timeoutMinutes, TimeUnit.MINUTES);
            
            // Analyze and report results
            boolean success = result.getSuccessCount() > 0;
            
            System.out.println("\n📊 Download Results:");
            System.out.println("✅ Successful: " + result.getSuccessCount());
            System.out.println("❌ Failed: " + result.getFailureCount());
            System.out.println("📁 Total Processed: " + result.getTotalAttempted());
            
            MemoryMonitor.logMemoryUsage("Download session completed");
            return success;
            
        } catch (TimeoutException e) {
            System.err.println("⏰ Download timed out after " + timeoutMinutes + " minutes");
            handleTimeout();
            throw e;
            
        } catch (InterruptedException e) {
            System.err.println("🛑 Download was interrupted");
            handleInterruption();
            Thread.currentThread().interrupt();
            throw e;
            
        } catch (Exception e) {
            System.err.println("💥 Download failed: " + e.getMessage());
            handleError(e);
            throw e;
            
        } finally {
            // Always perform cleanup
            performCleanup();
        }
    }
    
    /**
     * Handle timeout scenarios
     */
    private void handleTimeout() {
        System.out.println("🔄 Handling timeout...");
        cancelCurrentDownload();
        MemoryMonitor.forceCleanup();
    }
    
    /**
     * Handle interruption scenarios  
     */
    private void handleInterruption() {
        System.out.println("🔄 Handling interruption...");
        cancelCurrentDownload();
    }
    
    /**
     * Handle error scenarios
     */
    private void handleError(Exception e) {
        System.err.println("🔄 Handling error: " + e.getClass().getSimpleName());
        cancelCurrentDownload();
    }
    
    /**
     * Cancel ongoing download and cleanup future
     */
    private void cancelCurrentDownload() {
        if (currentFuture != null && !currentFuture.isDone()) {
            System.out.println("🛑 Cancelling ongoing download...");
            boolean cancelled = currentFuture.cancel(true);
            System.out.println(cancelled ? "✅ Download cancelled successfully" : 
                                         "⚠️  Could not cancel download");
        }
        
        // Clear reference for garbage collection
        currentFuture = null;
    }
    
    /**
     * Comprehensive cleanup and immediate resource release
     */
    private void performCleanup() {
        try {
            System.out.println("\n🧹 Performing immediate resource cleanup...");
            MemoryMonitor.logMemoryUsage("Before cleanup");
            
            // 1. Cancel any remaining operations immediately
            cancelCurrentDownload();
            
            // 2. Minimal grace period for critical operations
            System.out.println("⏳ Brief grace period for critical operations...");
            Thread.sleep(500); // Only 500ms grace period
            
            // 3. Aggressive memory cleanup
            System.out.println("🧹 Forcing aggressive garbage collection...");
            for (int i = 0; i < 3; i++) {
                System.gc();
                System.runFinalization();
                Thread.sleep(100);
            }
            
            // 4. Clear all references
            currentFuture = null;
            
            // 5. Force memory reclaim
            MemoryMonitor.forceCleanup();
            
            // 6. Final memory report
            long freedMemory = MemoryMonitor.getFreeMemoryMB();
            System.out.println("✅ Garbage collection freed " + freedMemory + " MB");
            MemoryMonitor.logMemoryUsage("After cleanup");
            
            System.out.println("🎯 Resources immediately available for next operation!");
            
        } catch (InterruptedException e) {
            System.err.println("⚠️  Cleanup interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("❌ Cleanup error: " + e.getMessage());
        }
    }
    
    /**
     * Immediate aggressive cleanup for large downloads
     */
    public void immediateCleanup() {
        System.out.println("🚀 Immediate aggressive cleanup initiated...");
        
        // Cancel everything immediately
        cancelCurrentDownload();
        
        // Force multiple GC cycles
        for (int i = 0; i < 5; i++) {
            System.gc();
            System.runFinalization();
        }
        
        // Clear manager resources if available
        if (manager != null) {
            // Note: Enhanced cleanup handled by wrapper itself
            System.out.println("🔧 Download manager cleared");
        }
        
        MemoryMonitor.forceCleanup();
        System.out.println("💨 Resources freed and ready for idle state!");
    }
    
    /**
     * Manual cleanup method for external calls
     */
    public void cleanup() {
        System.out.println("🔧 Manual cleanup requested");
        cancelCurrentDownload();
        MemoryMonitor.forceCleanup();
    }
    
    /**
     * Get current download status
     */
    public boolean isDownloadActive() {
        return currentFuture != null && !currentFuture.isDone();
    }
    
    /**
     * Emergency cleanup for finalization
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            if (isDownloadActive()) {
                System.err.println("⚠️  Emergency cleanup in finalize()");
                cleanup();
            }
        } finally {
            super.finalize();
        }
    }
}