package org.pdf.downloader.resolver.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupExtractor {
    
    private static final Pattern GROUP_PATTERN = Pattern.compile("\\b(?:group|grp)\\s*[:-]?\\s*([12])\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAPER_PATTERN = Pattern.compile("\\b(?:paper|p)\\s*[:-]?\\s*([1-6])\\b", Pattern.CASE_INSENSITIVE);
    
    public GroupInfo extractGroup(String text, String href) {
        String group = "";
        String paper = "";
        
        // Extract paper from URL
        Matcher urlPaperMatcher = Pattern.compile("\\b[ps]([1-6])\\b").matcher(href.toLowerCase());
        if (urlPaperMatcher.find()) {
            paper = "P" + urlPaperMatcher.group(1);
        }
        
        // Extract group from URL/text
        if (href.contains("group-1") || href.contains("g1") || text.contains("group 1")) {
            group = "G1";
        } else if (href.contains("group-2") || href.contains("g2") || text.contains("group 2")) {
            group = "G2";
        } else {
            Matcher groupMatcher = GROUP_PATTERN.matcher(text);
            if (groupMatcher.find()) {
                group = "G" + groupMatcher.group(1);
            }
        }
        
        // Determine group from paper if missing
        if (group.isEmpty() && !paper.isEmpty()) {
            int paperNum = Integer.parseInt(paper.substring(1));
            group = (paperNum >= 1 && paperNum <= 3) ? "G1" : "G2";
        }
        
        // Extract subject
        String subject = extractSubject(paper, text, href);
        
        return new GroupInfo(group, paper, subject);
    }
    
    private String extractSubject(String paper, String text, String href) {
        // CA Final Papers mapping (New Scheme):
        // P1: FR, P2: AFM, P3: AUD, P4: DT, P5: IDT, P6: MCD
        if (!paper.isEmpty()) {
            switch (paper) {
                case "P1": return "FR";
                case "P2": return "AFM";
                case "P3": return "AUD";
                case "P4": return "DT";
                case "P5": return "IDT";
                case "P6": return "MCD";
            }
        }

        String url = href.toLowerCase();
        String lowerText = text.toLowerCase();

        // Keyword-based subject detection for CA Final
        if (url.contains("financial reporting") || lowerText.contains("financial reporting") || url.matches(".*\\bfr\\b.*") || lowerText.matches(".*\\bfr\\b.*")) return "FR";
        if (url.contains("afm") || lowerText.contains("advanced financial") || lowerText.contains("advanced financial management")) return "AFM";
        // Audit synonyms (legacy/new names): AAAPE/AAPE/Advanced Auditing and Professional Ethics
        if (url.contains("audit") || lowerText.contains("audit") ||
            url.contains("aaape") || lowerText.contains("aaape") ||
            url.contains("aape") || lowerText.contains("aape") ||
            lowerText.contains("advanced auditing") || lowerText.contains("professional ethics")) {
            return "AUD";
        }
        if (url.contains("direct tax") || lowerText.contains("direct tax") || url.matches(".*\\bdt\\b.*") || lowerText.matches(".*\\bdt\\b.*") || lowerText.contains("income tax")) return "DT";
        if (url.contains("gst") || lowerText.contains("gst") || url.contains("indirect tax") || lowerText.contains("indirect tax") || url.matches(".*\\bidt\\b.*") || lowerText.matches(".*\\bidt\\b.*")) return "IDT";
        if (lowerText.contains("multi") && lowerText.contains("disciplinary") && lowerText.contains("case") || url.contains("mcs") || url.contains("mcd") || lowerText.contains("mcs") || lowerText.contains("mcd")) return "MCD";

        // Remove inter-only subjects: LAW/COST/SBM
        return "";
    }
    
    public static class GroupInfo {
        private final String group;
        private final String paper;
        private final String subject;
        
        public GroupInfo(String group, String paper, String subject) {
            this.group = group;
            this.paper = paper;
            this.subject = subject;
        }
        
        public String getGroup() { return group; }
        public String getPaper() { return paper; }
        public String getSubject() { return subject; }
    }
}
