package org.pdf.downloader.server;

import org.springframework.stereotype.Service;
import org.pdf.downloader.core.EnhancedDownloadManager;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class NotificationService {

    public void sendCompletion(String title, String message) {
        // Try SystemTray when not headless
        try {
            if (!GraphicsEnvironment.isHeadless() && SystemTray.isSupported()) {
                displayTray(title, message);
                return;
            }
        } catch (Throwable ignored) { /* fallthrough */ }

        // Fallback to OS-specific notifications
        tryOsNotification(title, message);
    }

    /**
     * Enhanced notification with download completion data
     */
    public void sendDownloadCompletionNotification(EnhancedDownloadManager.DownloadResult result, String downloadLocation) {
        if (result == null) {
            sendCompletion("Download Failed", "Download completed with errors");
            return;
        }

        String title = "📱 PDF Download Complete!";
        
        // Create detailed message with download statistics
        StringBuilder message = new StringBuilder();
        message.append("✅ Success: ").append(result.getSuccessCount());
        
        if (result.getFailureCount() > 0) {
            message.append(" ❌ Failed: ").append(result.getFailureCount());
        }
        
        message.append(" (Total: ").append(result.getTotalAttempted()).append(")");
        
        // Add folder location
        if (downloadLocation != null) {
            message.append("\n📁 Location: ").append(downloadLocation);
        }
        
        // Add error summary if any
        if (!result.getErrors().isEmpty()) {
            message.append("\n⚠️ Issues: ").append(result.getErrors().size()).append(" errors logged");
        }

        // Send the notification
        sendCompletion(title, message.toString());
        
        // Also log to console for debugging
        System.out.println("🔔 Frontend Notification Sent:");
        System.out.println("   Title: " + title);
        System.out.println("   Message: " + message.toString());
        System.out.println("   Success: " + result.getSuccessCount() + ", Failed: " + result.getFailureCount());
    }
    
    /**
     * Send notification for large download with dynamic thread info
     */
    public void sendLargeDownloadStarted(int fileCount, int threadCount, String location) {
        String title = "🚀 Large Download Started";
        String message = String.format("📊 Processing %d files with %d threads\n📁 Location: %s", 
                                      fileCount, threadCount, location);
        sendCompletion(title, message);
        System.out.println("🔔 Large download notification sent: " + fileCount + " files, " + threadCount + " threads");
    }

    private void displayTray(String title, String message) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            TrayIcon trayIcon = new TrayIcon(image, "PDF Downloader");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

            // Remove after a short delay to avoid tray clutter
            new Thread(() -> {
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                tray.remove(trayIcon);
            }).start();
        } catch (Throwable ignored) {
            // Ignore and fallback
        }
    }

    private void tryOsNotification(String title, String message) {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("mac")) {
                // macOS notification via AppleScript - enhanced for longer messages
                String script = String.format("display notification \"%s\" with title \"%s\" sound name \"Glass\"", 
                                             escape(message), escape(title));
                new ProcessBuilder("osascript", "-e", script).start();
            } else if (os.contains("nux") || os.contains("nix")) {
                // Linux notify-send if available - with longer timeout for detailed info
                new ProcessBuilder("notify-send", "-t", "8000", title, message).start();
            } else {
                // Windows: enhanced PowerShell notification
                String ps = String.format(
                    "Add-Type -AssemblyName System.Windows.Forms; " +
                    "[System.Windows.Forms.MessageBox]::Show('%s', '%s', [System.Windows.Forms.MessageBoxButtons]::OK, [System.Windows.Forms.MessageBoxIcon]::Information)",
                    escape(message), escape(title)
                );
                new ProcessBuilder("powershell", "-NoProfile", "-Command", ps).start();
            }
        } catch (Throwable ignored) {
            // As a last resort, just log
            System.out.println("[Notification] " + title + ": " + message);
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("'", "\\'");
    }
}

