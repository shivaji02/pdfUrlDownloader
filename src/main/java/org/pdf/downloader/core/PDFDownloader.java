package org.pdf.downloader.core;

import org.pdf.downloader.model.DownloadTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PDFDownloader {
    
    private static final int BUFFER_SIZE = 8192;
    private static final int TIMEOUT = 30000; // 30 seconds
    
    public void download(DownloadTask task) throws IOException {
        URL url = new URL(task.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Set connection properties
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setRequestProperty("User-Agent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        try {
            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
            }
            
            // Create target file path
            Path targetPath = Paths.get(task.getDownloadDir(), task.getFileName());
            
            // Download file
            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                 OutputStream outputStream = Files.newOutputStream(targetPath)) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            // Verify file was created and has content
            if (!Files.exists(targetPath) || Files.size(targetPath) == 0) {
                throw new IOException("Downloaded file is empty or was not created");
            }
            
        } finally {
            connection.disconnect();
        }
    }
}