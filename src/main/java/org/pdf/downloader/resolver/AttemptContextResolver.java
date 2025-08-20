package org.pdf.downloader.resolver;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttemptContextResolver {
    
    private static final Pattern ATTEMPT_PATTERN = Pattern.compile("\\b(May|Nov)\\s+(\\d{4})\\b");
    
    public String resolveContext(Document document) {
        // Try to find context from page title
        String title = document.title();
        if (title != null && !title.isEmpty()) {
            Matcher matcher = ATTEMPT_PATTERN.matcher(title);
            if (matcher.find()) {
                return matcher.group(1) + matcher.group(2); // e.g., "May2025"
            }
        }
        
        // Try to find context from headings
        Elements headings = document.select("h1, h2, h3");
        for (Element heading : headings) {
            String headingText = heading.text();
            Matcher matcher = ATTEMPT_PATTERN.matcher(headingText);
            if (matcher.find()) {
                return matcher.group(1) + matcher.group(2);
            }
        }
        
        // Try to find context from meta tags
        Elements metaTags = document.select("meta[name=description], meta[name=keywords]");
        for (Element meta : metaTags) {
            String content = meta.attr("content");
            Matcher matcher = ATTEMPT_PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group(1) + matcher.group(2);
            }
        }
        
        // Fallback to ICAI
        return "ICAI";
    }
}