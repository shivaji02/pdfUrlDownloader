package org.pdf.downloader.utils;

public class StringUtils {
    
    // Simple sanitizer with default fallback
    public static String sanitizeFileName(String fileName) {
        return sanitizeFileName(fileName, "AFM"); // Default to AFM
    }
    
    // Advanced sanitizer with subject-aware processing
    public static String sanitizeFileName(String fileName, String subject) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "unnamedFile";
        }
        
        String sanitized = fileName.trim();
        
        // Remove common prefixes that add noise
        sanitized = removeCommonPrefixes(sanitized);
        
        // Convert date patterns to short format
        sanitized = convertDatePatterns(sanitized);
        
        // Convert to more readable format with abbreviations
        sanitized = convertToReadableFormat(sanitized);
        
        // Remove unnecessary words
        sanitized = removeUnnecessaryWords(sanitized);
        
        // Replace invalid characters with spaces first
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._\\-\\s()]", " ");
        
        // Smart abbreviation logic
        sanitized = createSmartAbbreviation(sanitized, subject);
        
        return sanitized.isEmpty() ? "unnamedFile" : sanitized;
    }
    
    // **NEW: Simple sanitizer for basic filename cleanup**
    public static String sanitizeFileNameSimple(String input, String fallback) {
        if (input == null || input.trim().isEmpty()) {
            return fallback.isEmpty() ? "document" : fallback;
        }
        
        return input
            .replaceAll("[^a-zA-Z0-9._-]", "")
            .replaceAll("\\s+", "_")
            .trim();
    }
    
    // Helper methods (keep all your existing helper methods)
    private static String createSmartAbbreviation(String text, String subject) {
        if (text == null || text.trim().isEmpty()) {
            return "unnamedFile";
        }
        
        text = text.trim().toLowerCase();
        
        // Check if we have a predefined abbreviation for this text
        String predefined = ChapterAbbreviations.getAbbreviation(text, subject);
        if (predefined != null) {
            return toCamelCase(predefined);
        }
        
        String[] words = text.split("\\s+");
        
        if (words.length == 1) {
            String word = words[0];
            if (word.length() <= 20) {
                return toCamelCase(word);
            } else {
                return toCamelCase(word.substring(0, 15));
            }
        }
        
        // Multiple words - use intelligent abbreviation
        StringBuilder abbreviated = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i].trim();
            if (!word.isEmpty()) {
                String wordAbbrev = ChapterAbbreviations.getAbbreviation(word, subject);
                String abbrev;
                
                if (wordAbbrev != null) {
                    abbrev = wordAbbrev;
                } else {
                    abbrev = getWordAbbreviation(word, i == 0);
                }
                
                abbreviated.append(abbrev);
            }
        }
        
        return abbreviated.toString();
    }
    
    private static String getWordAbbreviation(String word, boolean isFirst) {
        int length = Math.min(word.length(), word.length() > 6 ? 5 : 4);
        String abbrev = word.substring(0, length);
        
        if (isFirst) {
            return abbrev.toLowerCase();
        } else {
            return Character.toUpperCase(abbrev.charAt(0)) + abbrev.substring(1).toLowerCase();
        }
    }
    
    private static String removeCommonPrefixes(String text) {
        text = text.replaceAll("^ICAI[\\s_]+", "");
        text = text.replaceAll("^(Download|PDF|Document)[\\s_]+", "");
        text = text.replaceAll("^Chapter\\s+\\d+[\\s_]*", "");
        text = text.replaceAll("^Section\\s+\\d+[\\s_]*", "");
        text = text.replaceAll("^Unit\\s+\\d+[\\s_]*", "");
        
        return text;
    }
    
    private static String convertDatePatterns(String text) {
        text = text.replaceAll("\\bMay\\s*2025\\b", "M25");
        text = text.replaceAll("\\bNov\\s*2025\\b", "N25");
        text = text.replaceAll("\\bMay\\s*2024\\b", "M24");
        text = text.replaceAll("\\bNov\\s*2024\\b", "N24");
        text = text.replaceAll("\\bMay\\s*2026\\b", "M26");
        text = text.replaceAll("\\bNov\\s*2026\\b", "N26");
        
        return text;
    }
    
    private static String convertToReadableFormat(String text) {
        text = text.replaceAll("Ind\\s*AS\\s*(\\d+)", "IndAS$1");
        return text;
    }
    
    private static String removeUnnecessaryWords(String text) {
        text = text.replaceAll("\\b(a|an|the|and|or|but|in|on|at|to|for|of|with|by)\\b", " ");
        text = text.replaceAll("\\b(may|will|shall|should|would|could)\\b", " ");
        text = text.replaceAll("\\b(very|more|most|much|many)\\b", " ");
        text = text.replaceAll("\\b(some|any|all|each|every)\\b", " ");
        text = text.replaceAll("\\b(this|that|these|those)\\b", " ");
        text = text.replaceAll("\\b(is|are|was|were|be|been|being)\\b", " ");
        text = text.replaceAll("\\b(have|has|had)\\b", " ");
        text = text.replaceAll("\\b(do|does|did|done)\\b", " ");
        text = text.replaceAll("\\b(can|cannot|may|might)\\b", " ");
        text = text.replaceAll("\\b(detailed|comprehensive|complete|full|entire)\\b", " ");
        text = text.replaceAll("\\b(overview|summary|guide|manual|handbook)\\b", " ");
        text = text.replaceAll("\\b(chapter|section|unit|part|lesson)\\b", " ");
        text = text.replaceAll("\\b(study|material|notes|book|text)\\b", " ");
        
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }
    
    private static String toCamelCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        if (!text.contains(" ")) {
            return Character.toLowerCase(text.charAt(0)) + text.substring(1);
        }
        
        return text;
    }
    
    // **UTILITY METHODS**
    public static String getFirstNChars(String word, int n) {
        if (word == null || word.isEmpty()) return "";
        String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
        return cleanWord.length() > n ? cleanWord.substring(0, n) : cleanWord;
    }
    
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}