package org.pdf.downloader.wrapper;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.IOException;

public class ErrorHandlerWrapper {
    
    private final NetworkRetryWrapper networkWrapper;
    
    public ErrorHandlerWrapper(NetworkRetryWrapper networkWrapper) {
        this.networkWrapper = networkWrapper;
    }
    
    public void executeDownload(String baseUrl, String downloadDir) {
        try {
            System.out.println("🌐 Target URL: " + baseUrl);
            System.out.println("📁 Download directory: " + downloadDir);
            System.out.println("=" .repeat(60));
            
            boolean success = networkWrapper.downloadWithRetry(baseUrl, downloadDir);
            
            if (success) {
                System.out.println("\n🎉 Download completed successfully!");
            } else {
                System.err.println("\n💥 Download failed after all attempts!");
                System.exit(1);
            }
            
        } catch (Exception e) {
            handleFatalError(e);
            System.exit(1);
        }
    }
    
    private void handleFatalError(Exception e) {
        System.err.println("\n🚨 FATAL ERROR:");
        System.err.println("=" .repeat(40));
        System.err.println("Type: " + e.getClass().getSimpleName());
        System.err.println("Message: " + e.getMessage());
        
        if (e.getCause() != null) {
            System.err.println("Cause: " + e.getCause().getMessage());
        }
        
        // Network diagnostics
        if (isNetworkError(e)) {
            System.err.println("\n💡 NETWORK TROUBLESHOOTING:");
            System.err.println("1. Check your internet connection");
            System.err.println("2. Verify ICAI website is accessible");
            System.err.println("3. Check proxy/firewall settings");
        }
        
        // Thread diagnostics
        if (e instanceof InterruptedException) {
            System.err.println("\n💡 THREAD ISSUE:");
            System.err.println("1. Download was interrupted");
            System.err.println("2. Check system resources");
        }
        
        if (System.getProperty("debug") != null) {
            e.printStackTrace();
        }
    }
    
    private boolean isNetworkError(Exception e) {
        return e instanceof ConnectException || 
               e instanceof UnknownHostException || 
               e instanceof SocketTimeoutException ||
               e instanceof IOException;
    }
}