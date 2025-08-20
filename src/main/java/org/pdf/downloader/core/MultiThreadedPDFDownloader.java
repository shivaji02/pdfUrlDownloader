package org.pdf.downloader.core;

import org.pdf.downloader.model.DownloadTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

public class MultiThreadedPDFDownloader {
    
    private static final int BUFFER_SIZE = 16384; // 16KB buffer
    private static final int TIMEOUT = 30000; // 30 seconds
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 second base delay
    
    public void download(DownloadTask task) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                performDownload(task);
                return; // Success
            } catch (IOException e) {
                lastException = e;
                
                if (attempt < MAX_RETRIES) {
                    // Exponential backoff with jitter
                    long delay = RETRY_DELAY_MS * (1L << (attempt - 1)) + 
                                ThreadLocalRandom.current().nextLong(0, 1000);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Download interrupted", ie);
                    }
                }
            }
        }
        
        throw new IOException("Failed after " + MAX_RETRIES + " attempts", lastException);
    }
    
    private void performDownload(DownloadTask task) throws IOException {
        URL url = new URL(task.getUrl());
        HttpURLConnection connection = createConnection(url);
        
        try {
            validateResponse(connection);
            
            Path targetPath = Paths.get(task.getDownloadDir(), task.getFileName());
            
            // Check if file already exists and has content
            if (Files.exists(targetPath) && Files.size(targetPath) > 0) {
                // File already exists, skip download
                return;
            }
            
            // Stream download to avoid memory issues
            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
                 OutputStream outputStream = Files.newOutputStream(targetPath)) {
                
                streamCopy(inputStream, outputStream);
            }
            
            // Verify download
            verifyDownload(targetPath);
            
        } finally {
            connection.disconnect();
        }
    }
    
    private HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Set connection properties
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setRequestProperty("User-Agent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        connection.setRequestProperty("Accept", 
            "application/pdf,application/octet-stream,*/*");
        connection.setRequestProperty("Connection", "close");
        
        return connection;
    }
    
    private void validateResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IOException("File not found (404): " + connection.getURL());
        } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new IOException("Access forbidden (403): " + connection.getURL());
        } else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
            throw new IOException("Service unavailable (503): " + connection.getURL());
        } else if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
        }
    }
    
    private void streamCopy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        long totalBytesRead = 0;
        
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        
        if (totalBytesRead == 0) {
            throw new IOException("No data received - empty response");
        }
    }
    
    private void verifyDownload(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException("Downloaded file does not exist: " + filePath);
        }
        
        long fileSize = Files.size(filePath);
        if (fileSize == 0) {
            throw new IOException("Downloaded file is empty: " + filePath);
        }
        
        // Basic PDF validation - check if file starts with PDF header
        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] header = new byte[4];
            int read = is.read(header);
            
            if (read < 4 || !new String(header).equals("%PDF")) {
                throw new IOException("Downloaded file is not a valid PDF: " + filePath);
            }
        }
    }
}