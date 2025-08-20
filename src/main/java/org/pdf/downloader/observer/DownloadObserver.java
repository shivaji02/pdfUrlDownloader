package org.pdf.downloader.observer;

import org.pdf.downloader.model.DownloadTask;

public interface DownloadObserver {
    void onStart(String message);
    void onTasksIdentified(int taskCount);
    void onTaskStart(DownloadTask task);
    void onTaskComplete(DownloadTask task);
    void onTaskError(DownloadTask task, Exception error);
    void onComplete(int totalTasks);
    void onError(String message);
}