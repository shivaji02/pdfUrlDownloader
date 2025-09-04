package org.pdf.downloader.resolver.handlers;

import org.pdf.downloader.resolver.handlers.ContentTypeDetector.ContentType;
import org.pdf.downloader.resolver.handlers.DateExtractor.DateInfo;
import org.pdf.downloader.resolver.handlers.GroupExtractor.GroupInfo;
import org.pdf.downloader.utils.StringUtils;

public class FileNameGenerator {
    
    public String generateFileName(ContentType contentType, DateInfo dateInfo, 
                                 GroupInfo groupInfo, String linkText, String href) {
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

                // 3) Otherwise, derive concise name from URL if meaningful; else "document"
                String fromHref = extractBaseNameFromHref(href);
                if (isMeaningfulToken(fromHref)) {
                    fileName.append(fromHref);
                } else {
                    fileName.append("document");
                }
        }
        
        return fileName.length() == 0 ? "document.pdf" : fileName.toString() + ".pdf";
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
}
