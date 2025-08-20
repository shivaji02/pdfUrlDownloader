package org.pdf.downloader.model;

public class DownloadTask {
    private final String url;
    private final String fileName;
    private final String downloadDir;
    
    public DownloadTask(String url, String fileName, String downloadDir) {
        this.url = url;
        this.fileName = fileName;
        this.downloadDir = downloadDir;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getDownloadDir() {
        return downloadDir;
    }
    
    @Override
    public String toString() {
        return "DownloadTask{" +
                "url='" + url + '\'' +
                ", fileName='" + fileName + '\'' +
                ", downloadDir='" + downloadDir + '\'' +
                '}';
    }
}