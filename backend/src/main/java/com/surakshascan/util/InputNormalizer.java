package com.surakshascan.util;
import java.net.URI;
public class InputNormalizer {
    public static String normalize(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase().replaceAll("\\s+", " ");
    }
    public static String extractDomain(String urlStr) {
        if (urlStr == null) return "";
        urlStr = urlStr.trim().toLowerCase();
        try {
            if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
                urlStr = "http://" + urlStr;
            }
            URI uri = new URI(urlStr);
            String host = uri.getHost();
            return host != null ? host.toLowerCase() : urlStr;
        } catch(Exception e) {
            return urlStr;
        }
    }
    public static String normalizeUpi(String upi) {
        if (upi == null) return "";
        return upi.trim().toLowerCase();
    }
    public static String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.trim().replaceAll("[\\s\\-\\(\\)]", "");
    }
}
