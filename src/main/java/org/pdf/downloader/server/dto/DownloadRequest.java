package org.pdf.downloader.server.dto;

public class DownloadRequest {
    private String url;
    private String downloadDir; // optional
    private Integer concurrency; // optional
    private Integer timeoutMinutes; // optional
    /** When true, follow other same-site pages linked from the URL and download their PDFs too. Default false. */
    private Boolean followNestedPages;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDownloadDir() { return downloadDir; }
    public void setDownloadDir(String downloadDir) { this.downloadDir = downloadDir; }

    public Integer getConcurrency() { return concurrency; }
    public void setConcurrency(Integer concurrency) { this.concurrency = concurrency; }

    public Integer getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(Integer timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }

    public Boolean getFollowNestedPages() { return followNestedPages; }
    public void setFollowNestedPages(Boolean followNestedPages) { this.followNestedPages = followNestedPages; }
}

