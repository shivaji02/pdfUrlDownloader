package org.pdf.downloader.resolver;

import org.jsoup.nodes.Element;
import org.pdf.downloader.resolver.handlers.*;
import org.pdf.downloader.resolver.tracking.FileTracker;
import org.pdf.downloader.utils.MemoryMonitor;

public class CleanAnchorResolver implements FileNameResolver {
    
    private final FileTracker tracker;
    private final ChapterContentHandler chapterHandler;
    private final DateExtractor dateExtractor;
    private final GroupExtractor groupExtractor;
    private final ContentTypeDetector contentDetector;
    private final FileNameGenerator nameGenerator;
    private boolean isFirstCall = true;
    private int processedCount = 0;
    private static final int MEMORY_CHECK_INTERVAL = 50; // Check memory every 50 files
    
    public CleanAnchorResolver() {
        this.tracker = new FileTracker();
        this.chapterHandler = new ChapterContentHandler();
        this.dateExtractor = new DateExtractor();
        this.groupExtractor = new GroupExtractor();
        this.contentDetector = new ContentTypeDetector();
        this.nameGenerator = new FileNameGenerator();
        
        // Log initial memory state
        MemoryMonitor.resetTimer();
        MemoryMonitor.logMemoryUsage("CleanAnchorResolver initialization");
    }
    
    @Override
    public String resolveFileName(Element linkElement, String context) {
        if (isFirstCall) {
            System.out.println("🚀 Starting Enhanced ICAI PDF Downloader...");
            isFirstCall = false;
        }
        
        processedCount++;
        
        String linkText = linkElement.text().trim();
        String href = linkElement.attr("href");
        
        System.out.println("🔍 Processing: " + linkText);
        
        String finalFileName = "";
        
        try {
            // Build combined text once
            String combinedText = (linkText + " " + context + " " + href).toLowerCase();

            // 1. Try chapter-specific handling first (prefix with subject if detected)
            String chapterFileName = chapterHandler.handleChapterContent(linkText, href);
            if (!chapterFileName.isEmpty()) {
                GroupExtractor.GroupInfo chGroup = groupExtractor.extractGroup(combinedText, href);
                if (chGroup != null && chGroup.getSubject() != null && !chGroup.getSubject().isEmpty()) {
                    String subjectPrefix = chGroup.getSubject().toUpperCase();
                    // keep it compact: FRUnit1.pdf, AFMModule2.pdf, etc.
                    chapterFileName = subjectPrefix + chapterFileName;
                }
                finalFileName = tracker.handleDuplicates(chapterFileName);
                tracker.trackMapping(finalFileName, linkText);
                System.out.println("✅ Generated: " + finalFileName);
                return finalFileName;
            }
            
            // 2. Handle other content types
            
            // Extract information
            DateExtractor.DateInfo dateInfo = dateExtractor.extractDate(combinedText);
            GroupExtractor.GroupInfo groupInfo = groupExtractor.extractGroup(combinedText, href);
            ContentTypeDetector.ContentType contentType = contentDetector.detectType(combinedText);
            
            // Generate filename
            String fileName = nameGenerator.generateFileName(contentType, dateInfo, groupInfo, linkText, href);
            finalFileName = tracker.handleDuplicates(fileName);
            
            // Track mapping
            tracker.trackMapping(finalFileName, linkText);
            
            System.out.println("✅ Generated: " + finalFileName);
            return finalFileName;
            
        } finally {
            // Periodic memory monitoring and cleanup
            if (processedCount % MEMORY_CHECK_INTERVAL == 0) {
                MemoryMonitor.logMemoryUsage("After " + processedCount + " files processed");
                tracker.reportMemoryUsage();
                
                // Force cleanup if memory usage is high
                Runtime runtime = Runtime.getRuntime();
                long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                double usedPercentage = (usedMemory * 100.0) / runtime.maxMemory();
                
                if (usedPercentage > 75) {
                    System.out.println("🧹 High memory usage detected, performing cleanup...");
                    MemoryMonitor.forceCleanup();
                }
            }
        }
    }
    
    public void onDownloadComplete(String downloadDir, int successCount) {
        try {
            if (successCount > 0) {
                tracker.generateMasterFile(downloadDir, successCount);
            }
        } finally {
            // Final memory report
            MemoryMonitor.logMemoryUsage("Download completion");
            System.out.println("📊 Total files processed: " + processedCount);
            
            // Comprehensive cleanup after download
            performFullCleanup();
        }
    }
    
    // **NEW: Comprehensive cleanup method**
    public void performFullCleanup() {
        System.out.println("🧹 Performing full CleanAnchorResolver cleanup...");
        
        // Clean up all components
        if (tracker != null) {
            tracker.cleanup();
        }
        
        // Reset counters
        processedCount = 0;
        isFirstCall = true;
        
        // Force garbage collection
        MemoryMonitor.forceCleanup();
        
        MemoryMonitor.logMemoryUsage("Full cleanup complete");
    }
    
    public void resetFileNameCounter() {
        tracker.reset();
        processedCount = 0;
        MemoryMonitor.logMemoryUsage("Counter reset");
    }
    
    // **NEW: Resource disposal**
    @Override
    protected void finalize() throws Throwable {
        try {
            performFullCleanup();
        } finally {
            super.finalize();
        }
    }
}
