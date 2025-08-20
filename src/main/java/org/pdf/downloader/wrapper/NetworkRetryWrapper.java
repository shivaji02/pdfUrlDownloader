package org.pdf.downloader.wrapper;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.IOException;

public class NetworkRetryWrapper {
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_SECONDS = 5;
    private static final int DOWNLOAD_TIMEOUT_MINUTES = 10;
    
    private final ThreadManagerWrapper threadWrapper;
    
    public NetworkRetryWrapper(ThreadManagerWrapper threadWrapper) {
        this.threadWrapper = threadWrapper;
    }
    
    public boolean downloadWithRetry(String baseUrl, String downloadDir) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                System.out.println("🔄 Attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS);
                
                boolean success = threadWrapper.executeDownloadWithTimeout(
                    baseUrl, downloadDir, DOWNLOAD_TIMEOUT_MINUTES);
                
                if (success) {
                    return true;
                }
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    System.out.println("❌ Attempt failed, retrying...");
                    waitBeforeRetry(attempt);
                }
                
            } catch (ConnectException e) {
                System.err.println("🌐 Connection failed: " + e.getMessage());
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    waitBeforeRetry(attempt);
                } else {
                    return false;
                }
                
            } catch (UnknownHostException e) {
                System.err.println("🌐 DNS resolution failed: " + e.getMessage());
                return false; // Don't retry DNS failures
                
            } catch (SocketTimeoutException | java.util.concurrent.TimeoutException e) {
                System.err.println("⏰ Timeout: " + e.getMessage());
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    waitBeforeRetry(attempt);
                } else {
                    return false;
                }
                
            } catch (IOException e) {
                System.err.println("📡 I/O error: " + e.getMessage());
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    waitBeforeRetry(attempt);
                } else {
                    return false;
                }
                
            } catch (InterruptedException e) {
                System.err.println("🛑 Download interrupted");
                Thread.currentThread().interrupt();
                return false;
                
            } catch (Exception e) {
                System.err.println("💥 Unexpected error: " + e.getMessage());
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    waitBeforeRetry(attempt);
                } else {
                    throw new RuntimeException("All retry attempts failed", e);
                }
            }
        }
        
        return false;
    }
    
    private void waitBeforeRetry(int attempt) {
        System.out.println("⏳ Waiting " + RETRY_DELAY_SECONDS + " seconds before retry...");
        try {
            Thread.sleep(RETRY_DELAY_SECONDS * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("🛑 Retry wait interrupted");
        }
    }
}