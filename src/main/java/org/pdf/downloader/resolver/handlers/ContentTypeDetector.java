package org.pdf.downloader.resolver.handlers;

import java.util.regex.Pattern;

public class ContentTypeDetector {
    
    private static final Pattern MTP_PATTERN = Pattern.compile("\\b(?:mtp|mock\\s*test)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RTP_PATTERN = Pattern.compile("\\b(?:rtp|revision\\s*test)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUESTION_PATTERN = Pattern.compile("\\b(?:question\\s*paper|exam)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SYLLABUS_PATTERN = Pattern.compile("\\b(?:syllabus|study\\s*material|chapter)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUGGESTED_PATTERN = Pattern.compile("\\b(?:suggested\\s*answer|solution)\\b", Pattern.CASE_INSENSITIVE);
    
    public ContentType detectType(String text) {
        if (MTP_PATTERN.matcher(text).find()) return ContentType.MTP;
        if (RTP_PATTERN.matcher(text).find()) return ContentType.RTP;
        if (SUGGESTED_PATTERN.matcher(text).find()) return ContentType.SUGGESTED_ANSWER;
        if (QUESTION_PATTERN.matcher(text).find()) return ContentType.QUESTION_PAPER;
        if (SYLLABUS_PATTERN.matcher(text).find()) return ContentType.SYLLABUS;
        return ContentType.UNKNOWN;
    }
    
    public enum ContentType {
        MTP, RTP, QUESTION_PAPER, SUGGESTED_ANSWER, SYLLABUS, UNKNOWN
    }
}