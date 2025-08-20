package org.pdf.downloader.observer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.pdf.downloader.model.DownloadTask;

public class ConsoleLogger implements DownloadObserver {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int currentTask = 0;
    
    @Override
    public void onStart(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println("[" + timestamp + "] " + "🚀 " + message);
    }
    
    @Override
    public void onTasksIdentified(int taskCount) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println("[" + timestamp + "] " + "📋 Found " + taskCount + " PDF files to download");
        System.out.println();
    }
    
    @Override
    public void onTaskStart(DownloadTask task) {
        currentTask++;
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.printf("[%s] ⬇️  [%d] Downloading: %s%n", timestamp, currentTask, task.getFileName());
    }
    
    @Override
    public void onTaskComplete(DownloadTask task) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.printf("[%s] ✅ [%d] Completed: %s%n", timestamp, currentTask, task.getFileName());
    }
    
    @Override
    public void onTaskError(DownloadTask task, Exception error) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.err.printf("[%s] ❌ [%d] Failed: %s - %s%n", timestamp, currentTask, task.getFileName(), error.getMessage());
    }
    
    @Override
    public void onComplete(int totalTasks) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println();
        System.out.println("[" + timestamp + "] " + "🎉 Download completed!");
        System.out.println("[" + timestamp + "] " + "📊 Successfully downloaded: " + totalTasks + " files");
    }
    
    @Override
    public void onError(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.err.println("[" + timestamp + "] " + "💥 Error: " + message);
    }
}