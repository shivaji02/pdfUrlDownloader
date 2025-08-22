package org.pdf.downloader.utils;

import java.util.HashMap;
import java.util.Map;

public class ChapterAbbreviations {
    
    private static final Map<String, String> AFM_CHAPTERS = new HashMap<>();
    private static final Map<String, String> FR_CHAPTERS = new HashMap<>();
    private static final Map<String, String> COMMON_TERMS = new HashMap<>();
    
    static {
        // AFM (Advanced Financial Management) specific chapters
        AFM_CHAPTERS.put("securitization", "SecurZ");
        AFM_CHAPTERS.put("security analysis", "SecAnalys");
        AFM_CHAPTERS.put("securities analysis", "SecAnalys");
        AFM_CHAPTERS.put("sec valuation", "SecVal");
        AFM_CHAPTERS.put("securities valuation", "SecVal");
        AFM_CHAPTERS.put("security valuation", "SecVal");
        AFM_CHAPTERS.put("portfolio management", "Portfolio");
        AFM_CHAPTERS.put("portfolio theory", "PortTheory");
        AFM_CHAPTERS.put("derivatives", "Derivat");
        AFM_CHAPTERS.put("derivative instruments", "DerivInst");
        AFM_CHAPTERS.put("risk management", "RiskMgmt");
        AFM_CHAPTERS.put("valuation", "Valuation");
        AFM_CHAPTERS.put("business valuation", "BusVal");
        AFM_CHAPTERS.put("company valuation", "CompVal");
        AFM_CHAPTERS.put("merger", "Merger");
        AFM_CHAPTERS.put("mergers", "Mergers");
        AFM_CHAPTERS.put("mergers and acquisitions", "MA");
        AFM_CHAPTERS.put("capital structure", "CapStruct");
        AFM_CHAPTERS.put("capital budgeting", "CapBudget");
        AFM_CHAPTERS.put("working capital", "WorkCap");
        AFM_CHAPTERS.put("dividend policy", "DivPolicy");
        AFM_CHAPTERS.put("cost of capital", "CostCap");
        AFM_CHAPTERS.put("leverage", "Leverage");
        AFM_CHAPTERS.put("mutual funds", "MutualF");
        AFM_CHAPTERS.put("hedge funds", "HedgeF");
        AFM_CHAPTERS.put("private equity", "PrivEq");
        AFM_CHAPTERS.put("venture capital", "VentCap");
        
        // FR (Financial Reporting) specific chapters
        FR_CHAPTERS.put("accounting standards", "AS");
        FR_CHAPTERS.put("indian accounting standards", "IndAS");
        FR_CHAPTERS.put("consolidated financial statements", "CFS");
        FR_CHAPTERS.put("financial statements", "FS");
        FR_CHAPTERS.put("financial reporting", "FR");
        FR_CHAPTERS.put("revenue recognition", "RevRec");
        FR_CHAPTERS.put("lease accounting", "LeaseAcc");
        
        // Common terms across all subjects
        COMMON_TERMS.put("introduction", "Intro");
        COMMON_TERMS.put("overview", "Overview");
        COMMON_TERMS.put("analysis", "Analys");
        COMMON_TERMS.put("valuation", "Valuat");
        COMMON_TERMS.put("management", "Mgmt");
    }
    
    public static String getAbbreviation(String text, String subject) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.toLowerCase().trim();
        
        // Check subject-specific abbreviations first
        if ("AFM".equalsIgnoreCase(subject)) {
            String afmAbbrev = AFM_CHAPTERS.get(text);
            if (afmAbbrev != null) {
                return afmAbbrev;
            }
        }
        
        if ("FR".equalsIgnoreCase(subject)) {
            String frAbbrev = FR_CHAPTERS.get(text);
            if (frAbbrev != null) {
                return frAbbrev;
            }
        }
        
        // Check common terms
        return COMMON_TERMS.get(text);
    }
    
    public static String smartAbbreviation(String text, String subject) {
        // Try static map first
        String staticAbbrev = getAbbreviation(text, subject);
        if (staticAbbrev != null) return staticAbbrev;

        // Remove stopwords
        String[] stopwords = {"the", "and", "of", "in", "on", "for", "to", "with", "by"};
        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            boolean isStopword = false;
            for (String stop : stopwords) {
                if (word.equals(stop)) {
                    isStopword = true;
                    break;
                }
            }
            if (!isStopword && !word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        String initials = sb.toString();
        if (initials.length() >= 2) return initials;

        // Fallback: first 4 letters
        return text.length() > 4 ? text.substring(0, 4) : text;
    }
}