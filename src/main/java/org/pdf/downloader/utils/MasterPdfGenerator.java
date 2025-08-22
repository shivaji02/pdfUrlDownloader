package org.pdf.downloader.utils;

import org.pdf.downloader.core.EnhancedDownloadManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
// import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MasterPdfGenerator {
    
    public String generateMasterPdf(String downloadDir, 
            Map<String, String> fileMappings, 
                        String sourceUrl,
                        EnhancedDownloadManager.DownloadResult result) {
        
        // Path masterPath = Paths.get(downloadDir, "Master.pdf");
        
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get(downloadDir));
            
            // Generate content as text first (we'll convert to PDF later)
            String masterContent = generateMasterContent(fileMappings, sourceUrl, result);
            
            // For now, create as .txt (you can enhance to actual PDF later)
            String masterTxtPath = downloadDir + "Master.txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(masterTxtPath))) {
                writer.print(masterContent);
            }
            
            System.out.println("📄 Master file created as: " + masterTxtPath);
            return masterTxtPath;
            
        } catch (IOException e) {
            System.err.println("❌ Failed to create Master.pdf: " + e.getMessage());
            return "";
        }
    }
    
    private String generateMasterContent(Map<String, String> fileMappings, 
                                       String sourceUrl,
                                       EnhancedDownloadManager.DownloadResult result) {
        
        StringBuilder content = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        // Header
        content.append("=" .repeat(80)).append("\n");
        content.append("                    CA FINAL DOWNLOADS - MASTER INDEX\n");
        content.append("=" .repeat(80)).append("\n\n");
        
        content.append("📅 Generated on: ").append(timestamp).append("\n");
        content.append("🌐 Source URL: ").append(sourceUrl).append("\n");
        content.append("📊 Total Files: ").append(result.getSuccessCount()).append("\n");
        content.append("❌ Failed: ").append(result.getFailureCount()).append("\n\n");
        
        // File mappings
        content.append("📚 FILE NAME MAPPINGS:\n");
        content.append("-" .repeat(80)).append("\n");
        content.append(String.format("%-30s | %s\n", "SHORTENED NAME", "ORIGINAL TITLE"));
        content.append("-" .repeat(80)).append("\n");
        
        fileMappings.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String shortName = entry.getKey();
                String originalTitle = entry.getValue();
                
                // Truncate long titles for better formatting
                if (originalTitle.length() > 45) {
                    originalTitle = originalTitle.substring(0, 42) + "...";
                }
                
                content.append(String.format("%-30s | %s\n", shortName, originalTitle));
            });
        
        // Legend
        content.append("\n").append("=" .repeat(80)).append("\n");
        content.append("📖 NAMING RULES EXPLANATION:\n");
        content.append("=" .repeat(80)).append("\n");
        content.append("• 2 Words: First 3 letters of each word (e.g., Quality Control → Quacon)\n");
        content.append("• >2 Words: First 3 letters of each word (e.g., Due Diligence Investigation → Duedilinv)\n");
        content.append("• Initial Pages: InitialPages.pdf\n");
        content.append("• Units: Unit1.pdf, Unit2.pdf, etc.\n");
        content.append("• Duplicates: _2, _3, _4 suffixes added\n");
        content.append("• MTP: M25G1MTP.pdf (Month+Year+Group+MTP)\n");
        content.append("• RTP: N24G2RTP.pdf (Month+Year+Group+RTP)\n\n");
        
        // Paper structure
        content.append("📋 CA FINAL PAPER STRUCTURE:\n");
        content.append("-" .repeat(40)).append("\n");
        content.append("GROUP 1:\n");
        content.append("  P1 - FR   (Financial Reporting)\n");
        content.append("  P2 - AFM  (Advanced Financial Management)\n");
        content.append("  P3 - AUD  (Advanced Auditing & Assurance)\n\n");
        content.append("GROUP 2:\n");
        content.append("  P4 - LAW  (Corporate & Other Laws)\n");
        content.append("  P5 - COST (Strategic Cost Management)\n");
        content.append("  P6 - SBM  (Strategic Business Management)\n\n");
        
        content.append("💡 TIP: Use this master file to quickly find what each shortened filename contains!\n");
        content.append("=" .repeat(80)).append("\n");
        
        return content.toString();
    }
}