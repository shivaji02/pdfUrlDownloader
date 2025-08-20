package org.pdf.downloader.resolver.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChapterContentHandler {
    
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("chapter\\s+(\\d+)[:;]?\\s*(.*?)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MODULE_PATTERN = Pattern.compile("module\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNIT_PATTERN = Pattern.compile("unit[-\\s]+(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern INITIAL_PAGES_PATTERN = Pattern.compile("\\b(?:initial\\s*pages?|ip)\\b", Pattern.CASE_INSENSITIVE);
    
    public String handleChapterContent(String linkText, String href) {
        // Handle Initial Pages
        if (INITIAL_PAGES_PATTERN.matcher(linkText).find() || href.contains("-ip.pdf")) {
            return "InitialPages.pdf";
        }
        
        // Handle Chapters
        Matcher chapterMatcher = CHAPTER_PATTERN.matcher(linkText);
        if (chapterMatcher.find()) {
            String chapterTitle = chapterMatcher.group(2).trim();
            String cleanTitle = cleanChapterTitle(chapterTitle);
            return cleanTitle + ".pdf";
        }
        
        // Handle Units
        Matcher unitMatcher = UNIT_PATTERN.matcher(linkText);
        if (unitMatcher.find()) {
            String unitNum = unitMatcher.group(1);
            return "Unit" + unitNum + ".pdf";
        }
        
        // Handle Modules
        Matcher moduleMatcher = MODULE_PATTERN.matcher(linkText);
        if (moduleMatcher.find()) {
            String moduleNum = moduleMatcher.group(1);
            return "Module" + moduleNum + ".pdf";
        }
        
        return ""; // Not chapter content
    }
    
    private String cleanChapterTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Chapter";
        }
        
        // Remove common words
        String cleaned = title
            .replaceAll("(?i)\\b(and|&|the|of|in|for|to|with|on|a|an)\\b", " ")
            .replaceAll("\\s+", " ")
            .trim();
        
        String[] words = cleaned.split("\\s+");
        if (words.length == 0) return "Chapter";
        
        StringBuilder result = new StringBuilder();
        
        if (words.length == 2) {
            // 2 words = first 3 letters each
            result.append(getFirstNChars(words[0], 3));
            result.append(getFirstNChars(words[1], 3));
        } else {
            // >2 words = first 3 letters each
            for (String word : words) {
                result.append(getFirstNChars(word, 3));
                if (result.length() >= 15) break;
            }
        }
        
        String finalResult = result.toString();
        if (finalResult.length() > 0) {
            finalResult = Character.toUpperCase(finalResult.charAt(0)) + finalResult.substring(1).toLowerCase();
        }
        
        return finalResult.isEmpty() ? "Chapter" : finalResult;
    }
    
    private String getFirstNChars(String word, int n) {
        if (word == null || word.isEmpty()) return "";
        String cleanWord = word.replaceAll("[^a-zA-Z]", "");
        return cleanWord.length() > n ? cleanWord.substring(0, n) : cleanWord;
    }
}




