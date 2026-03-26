package org.pdf.downloader.server;

import org.pdf.downloader.core.EnhancedDownloadManager;
import org.pdf.downloader.server.dto.DownloadRequest;
import org.pdf.downloader.server.dto.DownloadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DownloadController {

    private final DownloadService service;
    private final NotificationService notifier;

    public DownloadController(DownloadService service, NotificationService notifier) {
        this.service = service;
        this.notifier = notifier;
    }

    @PostMapping("/download")
    public ResponseEntity<?> download(@RequestBody DownloadRequest req) {
        if (req.getUrl() == null || req.getUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("url is required");
        }
        int concurrency = req.getConcurrency() != null ? Math.max(1, req.getConcurrency()) : 8;
        int timeout = req.getTimeoutMinutes() != null ? Math.max(1, req.getTimeoutMinutes()) : 10;
        boolean followNested = Boolean.TRUE.equals(req.getFollowNestedPages());

        try {
            String targetDir = service.resolveTargetDir(req.getDownloadDir());
            EnhancedDownloadManager.DownloadResult result = service.runDownload(
                    req.getUrl(),
                    targetDir,
                    concurrency,
                    timeout,
                    followNested
            );

            DownloadResponse body = new DownloadResponse(
                    result.getSuccessCount(),
                    result.getFailureCount(),
                    result.getErrors() != null ? result.getErrors() : new ArrayList<>(),
                    targetDir
            );

            // Fire-and-forget notification on server host
            try {
                String title = "PDF Download Complete";
                String msg = "Saved " + body.getSuccess() + "/" + body.getTotal() + " to " + body.getDownloadDir();
                notifier.sendCompletion(title, msg);
            } catch (Exception ignored) {}

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Download failed: " + e.getMessage());
        }
    }
}
