package org.pdf.downloader.resolver.tracking;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.pdf.downloader.utils.MemoryMonitor; // ← ADD THIS IMPORT

public class FileTracker {
    
    private static final int MAX_MAPPINGS = 10000; // Limit memory usage
    
    private final Map<String, Integer> fileNameCounter = new HashMap<>();
    
    // Use LinkedHashMap with size limit to prevent memory leaks
    private final Map<String, String> fileMappings = new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > MAX_MAPPINGS;
        }
    };
    
    public String handleDuplicates(String originalFileName) {
        String baseName = originalFileName.endsWith(".pdf") ? 
            originalFileName.substring(0, originalFileName.length() - 4) : originalFileName;
            
        Integer count = fileNameCounter.get(baseName);
        
        if (count == null) {
            fileNameCounter.put(baseName, 1);
            return baseName + ".pdf";
        } else {
            count++;
            fileNameCounter.put(baseName, count);
            return baseName + "_" + count + ".pdf";
        }
    }
    
    public void trackMapping(String shortName, String originalTitle) {
        fileMappings.put(shortName, originalTitle);
    }
    
    public void generateMasterFile(String downloadDir, int successCount) {
        // Use try-with-resources for automatic cleanup
        String masterPath = downloadDir + "Master.txt";
        
        try (FileWriter writer = new FileWriter(masterPath)) {
            String content = createMasterContent(successCount);
            writer.write(content);
            
            System.out.println("✅ Master.txt created with " + fileMappings.size() + " mappings!");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to create Master.txt: " + e.getMessage());
        } finally {
            // Log memory usage after file creation
            MemoryMonitor.logMemoryUsage("Master file creation");
        }
    }
    
    private String createMasterContent(int successCount) {
        StringBuilder content = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        content.append("CA FINAL DOWNLOADS - MASTER INDEX\n");
        content.append("=".repeat(50)).append("\n");
        content.append("Generated: ").append(timestamp).append("\n");
        content.append("Total Files: ").append(successCount).append("\n");
        content.append("Memory Mappings: ").append(fileMappings.size()).append("\n\n");
        
        content.append("FILE MAPPINGS:\n");
        content.append("-".repeat(50)).append("\n");
        
        fileMappings.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> content.append(String.format("%-25s → %s\n", 
                entry.getKey(), entry.getValue())));
        
        return content.toString();
    }
    
    // **NEW: Comprehensive cleanup method**
    public void cleanup() {
        System.out.println("🧹 Cleaning up FileTracker memory...");
        
        int counterSize = fileNameCounter.size();
        int mappingsSize = fileMappings.size();
        
        fileNameCounter.clear();
        fileMappings.clear();
        
        System.out.println("✅ Cleared " + counterSize + " counters and " + mappingsSize + " mappings");
        
        // Suggest garbage collection
        System.gc();
        
        MemoryMonitor.logMemoryUsage("FileTracker cleanup");
    }
    
    public void reset() {
        cleanup(); // Use comprehensive cleanup
    }
    
    // **NEW: Memory usage reporting**
    public void reportMemoryUsage() {
        System.out.println("📊 FileTracker Memory Usage:");
        System.out.println("   File Counter: " + fileNameCounter.size() + " entries");
        System.out.println("   File Mappings: " + fileMappings.size() + " entries");
        System.out.println("   Max Mappings Limit: " + MAX_MAPPINGS);
    }
    
    // **NEW: Finalize for emergency cleanup**
    @Override
    protected void finalize() throws Throwable {
        try {
            cleanup();
        } finally {
            super.finalize();
        }
    }
}