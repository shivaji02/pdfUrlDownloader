package org.pdf.downloader.resolver.handlers;

import org.pdf.downloader.resolver.handlers.ContentTypeDetector.ContentType;
import org.pdf.downloader.resolver.handlers.DateExtractor.DateInfo;
import org.pdf.downloader.resolver.handlers.GroupExtractor.GroupInfo;
import org.pdf.downloader.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameGenerator {

    /** ICAI CDN PDFs embed the standard id in the filename (e.g. ...-indas7.pdf). Prefer this over link text so numbers are never dropped and "Interim Financial Reporting" does not force subject FR and skip Ind AS naming. */
    private static final Pattern INDAS_IN_HREF = Pattern.compile("(?i)indas(\\d+)\\.pdf");

    public String generateFileName(ContentType contentType, DateInfo dateInfo, 
                                 GroupInfo groupInfo, String linkText, String href) {
        String indAsFromHref = buildIndAsNameFromHref(linkText, href);
        if (indAsFromHref != null) {
            return indAsFromHref + ".pdf";
        }

        StringBuilder fileName = new StringBuilder();
        
        switch (contentType) {
            case MTP:
                fileName.append(dateInfo.getCode())
                        .append(groupInfo.getGroup())
                        .append("MTP");
                break;
                
            case RTP:
                fileName.append(dateInfo.getCode())
                        .append(groupInfo.getGroup())
                        .append("RTP");
                break;
                
            case QUESTION_PAPER:
                fileName.append(dateInfo.getCode());
                if (!groupInfo.getSubject().isEmpty()) {
                    fileName.append(groupInfo.getSubject());
                } else if (!groupInfo.getGroup().isEmpty()) {
                    fileName.append(groupInfo.getGroup());
                }
                fileName.append("Ques");
                break;
                
            case SUGGESTED_ANSWER:
                fileName.append(dateInfo.getCode());
                if (!groupInfo.getSubject().isEmpty()) {
                    fileName.append(groupInfo.getSubject());
                } else if (!groupInfo.getGroup().isEmpty()) {
                    fileName.append(groupInfo.getGroup());
                }
                fileName.append("Ans");
                break;
                
            case SYLLABUS:
                if (!groupInfo.getSubject().isEmpty()) {
                    fileName.append(groupInfo.getSubject()).append("Syllabus");
                } else {
                    fileName.append("Syllabus");
                }
                break;
                
            default:
                // Clean, clear naming rules for unknown/other content:
                // 1) If subject known: [code][SUBJECT] or just [SUBJECT]
                String subject = groupInfo != null ? groupInfo.getSubject() : "";
                String code = (dateInfo != null) ? dateInfo.getCode() : "";
                if (subject != null && !subject.isEmpty()) {
                    if (code != null && !code.isEmpty()) {
                        fileName.append(code).append(subject.toUpperCase());
                    } else {
                        fileName.append(subject.toUpperCase());
                    }
                    break;
                }

                // 2) If no subject but we have date code: use the code alone
                if (code != null && !code.isEmpty()) {
                    fileName.append(code);
                    break;
                }

                // 3) Create meaningful name from link text instead of URL
                String cleanedFromText = createCleanNameFromText(linkText);
                if (!cleanedFromText.isEmpty() && !cleanedFromText.equals("document")) {
                    fileName.append(cleanedFromText);
                } else {
                    fileName.append("document");
                }
        }
        
        return fileName.length() == 0 ? "document.pdf" : fileName.toString() + ".pdf";
    }

    private String buildIndAsNameFromHref(String linkText, String href) {
        if (href == null || href.isEmpty()) {
            return null;
        }
        Matcher m = INDAS_IN_HREF.matcher(href);
        if (!m.find()) {
            return null;
        }
        String num = m.group(1);
        String base = "Indas" + num;
        String afterStandard = stripLeadingIndAsTitle(linkText);
        if (afterStandard == null || afterStandard.isEmpty()) {
            return base;
        }
        String suffix = createCleanNameFromText(afterStandard);
        if (suffix.isEmpty() || "document".equalsIgnoreCase(suffix)) {
            return base;
        }
        return base + suffix;
    }

    /** Remove "Ind AS 34:" / "Ind AS 7" prefix so the suffix abbreviator only sees the topic title. */
    private String stripLeadingIndAsTitle(String linkText) {
        if (linkText == null) {
            return "";
        }
        return linkText.replaceFirst("(?i)^\\s*Ind\\s*AS\\s*\\d+\\s*:?\\s*", "").trim();
    }

    private String extractBaseNameFromHref(String href) {
        if (href == null || href.trim().isEmpty()) return "";
        String path = href;
        int q = path.indexOf('?');
        if (q >= 0) path = path.substring(0, q);
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String last = slash >= 0 ? path.substring(slash + 1) : path;
        if (last.toLowerCase().endsWith(".pdf")) {
            last = last.substring(0, last.length() - 4);
        }
        // Keep it short and clean
        String simple = StringUtils.sanitizeFileNameSimple(last, "");
        if (simple.length() > 40) simple = simple.substring(0, 40);
        return simple;
    }

    private String sanitizeNonGeneric(String text) {
        if (text == null) return "";
        String t = text.trim().toLowerCase();
        // Remove generic phrases
        String[] generics = {"click here", "click", "here", "download", "view", "open", "link",
                "read more", "more", "see", "pdf", "saransh", "icai", "bos", "education"};
        for (String g : generics) {
            t = t.replace(g, "").trim();
        }
        t = t.replaceAll("\\s+", " ");
        String cleaned = StringUtils.sanitizeFileNameSimple(t, "");
        return cleaned;
    }

    private boolean isMeaningfulToken(String token) {
        if (token == null) return false;
        String t = token.trim();
        if (t.isEmpty()) return false;
        // Consider meaningful if it has at least 3 letters
        int letters = 0;
        for (int i = 0; i < t.length(); i++) {
            if (Character.isLetter(t.charAt(i))) letters++;
            if (letters >= 3) return true;
        }
        return false;
    }
    
    private String createCleanNameFromText(String linkText) {
        if (linkText == null || linkText.trim().isEmpty()) {
            return "document";
        }
        
        String text = linkText.trim();
        
        // Remove common noise words and phrases
        text = text.replaceAll("(?i)\\b(please\\s+click\\s+here\\s+for\\s*|click\\s+here\\s*|download\\s+the\\s*|please\\s+click\\s+here\\s*)", "");
        text = text.replaceAll("(?i)\\b(document|file|pdf|view|open|read|see|more)\\b", "");
        text = text.trim();
        
        if (text.isEmpty()) {
            return "document";
        }
        
        // Split into words for analysis
        String[] words = text.split("\\s+");
        
        // Special case: If it's only one word and less than 10 characters, use it as-is
        if (words.length == 1) {
            String singleWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
            if (singleWord.length() > 0 && singleWord.length() < 10) {
                // Use the full word, properly capitalized
                return Character.toUpperCase(singleWord.charAt(0)) + singleWord.substring(1).toLowerCase();
            }
        }
        
        // Use first 3 letters algorithm for multiple words or longer single words
        StringBuilder result = new StringBuilder();
        
        int wordCount = 0;
        for (String word : words) {
            if (wordCount >= 6) break; // Limit to avoid very long names
            
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
            if (cleanWord.isEmpty()) {
                continue;
            }

            // Ind AS 1–9: the standard number is a single digit — old logic required len>=2, so "2" was dropped
            if (cleanWord.matches("\\d+")) {
                result.append(cleanWord.length() <= 3 ? cleanWord : cleanWord.substring(0, 3));
                wordCount++;
                continue;
            }

            if (cleanWord.length() >= 2) {
                if (cleanWord.length() >= 3) {
                    result.append(cleanWord.substring(0, 3));
                } else {
                    result.append(cleanWord);
                }
                wordCount++;
            }
        }
        
        String finalResult = result.toString();
        if (finalResult.length() > 0) {
            // Capitalize first letter, rest lowercase
            finalResult = Character.toUpperCase(finalResult.charAt(0)) + finalResult.substring(1).toLowerCase();
        }
        
        return finalResult.isEmpty() ? "document" : finalResult;
    }
}
