package com.mb.modulith.shared.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailUtils {

    private static final Pattern LOCAL_SEGMENT = Pattern.compile("[a-zA-Z0-9_+&*-]+");
    private static final Pattern DOMAIN_LABEL = Pattern.compile("[a-zA-Z0-9-]+");
    private static final Pattern DOMAIN_TLD = Pattern.compile("[a-zA-Z]{2,7}");

    public static String getEmailDomain(String emailAddress) {
        return emailAddress.substring(emailAddress.indexOf('@') + 1);
    }

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        int atIndex = value.indexOf('@');
        if (atIndex < 0) {
            return false;
        }
        if (value.indexOf('@', atIndex + 1) >= 0) {
            return false;
        }
        String localPart = value.substring(0, atIndex);
        String domainPart = value.substring(atIndex + 1);
        return isValidLocalPart(localPart) && isValidDomainPart(domainPart);
    }

    private static boolean isValidLocalPart(String localPart) {
        if (localPart.isEmpty()) {
            return false;
        }
        String[] segments = localPart.split("\\.", -1);
        for (String segment : segments) {
            if (segment.isEmpty() || !LOCAL_SEGMENT.matcher(segment).matches()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidDomainPart(String domainPart) {
        if (domainPart.isEmpty()) {
            return false;
        }
        String[] labels = domainPart.split("\\.", -1);
        if (labels.length < 2) {
            return false;
        }
        for (int i = 0; i < labels.length - 1; i++) {
            if (labels[i].isEmpty() || !DOMAIN_LABEL.matcher(labels[i]).matches()) {
                return false;
            }
        }
        String tld = labels[labels.length - 1];
        return !tld.isEmpty() && DOMAIN_TLD.matcher(tld).matches();
    }
}
