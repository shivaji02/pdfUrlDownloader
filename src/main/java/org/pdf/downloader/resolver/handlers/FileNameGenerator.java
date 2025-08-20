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
                String cleanName = StringUtils.sanitizeFileName(linkText, "");
                if (cleanName.isEmpty()) {
                    cleanName = "document";
                }
                fileName.append(cleanName);
        }
        
        return fileName.length() == 0 ? "document.pdf" : fileName.toString() + ".pdf";
    }
}