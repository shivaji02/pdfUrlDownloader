package org.pdf.downloader.server.dto;

import java.util.List;

public class DownloadResponse {
    private int success;
    private int failed;
    private int total;
    private List<String> errors;
    private String downloadDir;

    public DownloadResponse(int success, int failed, List<String> errors, String downloadDir) {
        this.success = success;
        this.failed = failed;
        this.total = success + failed;
        this.errors = errors;
        this.downloadDir = downloadDir;
    }

    public int getSuccess() { return success; }
    public int getFailed() { return failed; }
    public int getTotal() { return total; }
    public List<String> getErrors() { return errors; }
    public String getDownloadDir() { return downloadDir; }
}

