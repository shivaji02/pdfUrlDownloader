package org.pdf.downloader.observer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.pdf.downloader.model.DownloadTask;

public class ConsoleLogger implements DownloadObserver {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    private final ConcurrentMap<DownloadTask, Integer> taskIndex = new ConcurrentHashMap<>();
    
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
        int idx = taskCounter.incrementAndGet();
        taskIndex.put(task, idx);
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.printf("[%s] ⬇️  [%d] Downloading: %s%n", timestamp, idx, task.getFileName());
    }
    
    @Override
    public void onTaskComplete(DownloadTask task) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        Integer idx = taskIndex.getOrDefault(task, -1);
        System.out.printf("[%s] ✅ [%d] Completed: %s%n", timestamp, idx, task.getFileName());
        taskIndex.remove(task);
    }
    
    @Override
    public void onTaskError(DownloadTask task, Exception error) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        Integer idx = taskIndex.getOrDefault(task, -1);
        System.err.printf("[%s] ❌ [%d] Failed: %s - %s%n", timestamp, idx, task.getFileName(), error.getMessage());
        taskIndex.remove(task);
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
