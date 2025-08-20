package org.pdf.downloader.resolver.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateExtractor {
    
    private static final Pattern DATE_PATTERN = Pattern.compile("(May|Nov)\\s*(\\d{4})", Pattern.CASE_INSENSITIVE);
    
    public DateInfo extractDate(String text) {
        Matcher dateMatcher = DATE_PATTERN.matcher(text);
        if (dateMatcher.find()) {
            String month = dateMatcher.group(1).toLowerCase();
            String year = dateMatcher.group(2);
            String monthCode = "may".equals(month) ? "M" : "N";
            String yearCode = year.length() >= 4 ? year.substring(2) : year;
            return new DateInfo(monthCode, yearCode, monthCode + yearCode);
        }
        return new DateInfo("", "", "");
    }
    
    public static class DateInfo {
        private final String month;
        private final String year;
        private final String code;
        
        public DateInfo(String month, String year, String code) {
            this.month = month;
            this.year = year;
            this.code = code;
        }
        
        public String getCode() { return code; }
        public String getMonth() { return month; }
        public String getYear() { return year; }
    }
}