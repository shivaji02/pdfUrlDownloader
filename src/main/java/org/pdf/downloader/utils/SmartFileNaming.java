package org.pdf.downloader.utils;

public class SmartFileNaming {
    
    /**
     * Generate smart, readable file names using first 3-4 letters algorithm
     */
    public static String generateSmartName(String originalTitle, String context) {
        if (originalTitle == null || originalTitle.trim().isEmpty()) {
            return "document.pdf";
        }
        
        String cleaned = originalTitle.trim();
        
        // Remove common prefixes and noise
        cleaned = removeNoiseWords(cleaned);
        
        // Extract meaningful words
        String[] words = cleaned.split("\\s+");
        
        if (words.length == 1) {
            // Single word: use first 4-6 characters
            return getSingleWordAbbrev(words[0]) + ".pdf";
        } else if (words.length <= 3) {
            // 2-3 words: first 3 letters of each
            return getMultiWordAbbrev(words) + ".pdf";
        } else {
            // Many words: smart selection
            return getSmartAbbrev(words) + ".pdf";
        }
    }
    
    private static String removeNoiseWords(String text) {
        // Remove dates, numbers, common words
        text = text.replaceAll("\\b(20\\d{2})\\b", ""); // Remove years
        text = text.replaceAll("\\b(chapter|section|unit|part)\\s*\\d+\\b", ""); // Remove chapter numbers
        text = text.replaceAll("\\b(the|and|of|in|on|for|to|with|by)\\b", " "); // Remove articles
        text = text.replaceAll("\\s+", " ").trim(); // Clean extra spaces
        return text;
    }
    
    private static String getSingleWordAbbrev(String word) {
        word = word.replaceAll("[^a-zA-Z]", ""); // Remove non-letters
        if (word.length() <= 4) return word.toLowerCase();
        if (word.length() <= 8) return word.substring(0, 4).toLowerCase();
        return word.substring(0, 6).toLowerCase();
    }
    
    private static String getMultiWordAbbrev(String[] words) {
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            String clean = word.replaceAll("[^a-zA-Z]", "");
            if (!clean.isEmpty() && clean.length() >= 2) {
                result.append(clean.substring(0, Math.min(3, clean.length())).toLowerCase());
            }
        }
        return result.length() > 0 ? result.toString() : "doc";
    }
    
    private static String getSmartAbbrev(String[] words) {
        StringBuilder result = new StringBuilder();
        int count = 0;
        
        for (String word : words) {
            String clean = word.replaceAll("[^a-zA-Z]", "");
            if (!clean.isEmpty() && clean.length() >= 3 && count < 4) {
                // Skip very common words
                if (!isCommonWord(clean.toLowerCase())) {
                    result.append(clean.substring(0, 3).toLowerCase());
                    count++;
                }
            }
        }
        
        return result.length() > 0 ? result.toString() : "document";
    }
    
    private static boolean isCommonWord(String word) {
        String[] common = {"the", "and", "for", "are", "but", "not", "you", "all", "can", "had", "her", "was", "one", "our", "out", "day", "get", "has", "him", "his", "how", "its", "may", "new", "now", "old", "see", "two", "who", "boy", "did", "way", "when", "what", "with", "have", "from", "they", "know", "want", "been", "good", "much", "some", "time", "very", "when", "come", "here", "just", "like", "long", "make", "many", "over", "such", "take", "than", "them", "well", "were"};
        
        for (String c : common) {
            if (word.equals(c)) return true;
        }
        return false;
    }
}