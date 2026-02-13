package com.blog.platform.common.security;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class XssSanitizer {

    private static final PolicyFactory RICH_TEXT_POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "b", "i", "u", "strong", "em", "span", "div",
                          "h1", "h2", "h3", "h4", "h5", "h6",
                          "ul", "ol", "li", "blockquote", "pre", "code",
                          "a", "img", "table", "thead", "tbody", "tr", "td", "th",
                          "hr", "sub", "sup", "del", "ins")
            .allowAttributes("href", "title", "target").onElements("a")
            .allowAttributes("src", "alt", "title", "width", "height", "loading").onElements("img")
            .allowAttributes("class", "id").onElements("p", "span", "div", "pre", "code", "table", "td", "th")
            .allowAttributes("colspan", "rowspan").onElements("td", "th")
            .allowStandardUrlProtocols()
            .requireRelNofollowOnLinks()
            .allowStyling()
            .toFactory();

    private static final PolicyFactory PLAIN_TEXT_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    private static final Pattern DANGEROUS_PATTERNS = Pattern.compile(
            "<script|javascript:|on\\w+\\s*=|data:|vbscript:|expression\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return RICH_TEXT_POLICY.sanitize(input);
    }

    public String sanitizeStrict(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return PLAIN_TEXT_POLICY.sanitize(input);
    }

    public String sanitizeForAttribute(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String sanitized = input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
        
        return sanitized;
    }

    public String sanitizeForJavaScript(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("</", "<\\/");
    }

    public String sanitizeForUrl(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        try {
            return java.net.URLEncoder.encode(input, "UTF-8");
        } catch (Exception e) {
            return input;
        }
    }

    public boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return DANGEROUS_PATTERNS.matcher(input).find();
    }

    public ValidationResult validate(String input) {
        if (input == null) {
            return new ValidationResult(true, null);
        }
        
        if (containsXss(input)) {
            return new ValidationResult(false, "Input contains potentially dangerous content");
        }
        
        return new ValidationResult(true, null);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
}
