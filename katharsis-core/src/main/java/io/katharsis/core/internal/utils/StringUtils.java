package io.katharsis.core.internal.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StringUtils {

    public static final String EMPTY = "";

    public static String join(String delimiter, Iterable<String> stringsIterable) {
        List<String> strings = new LinkedList<>();
        Iterator<String> iterator = stringsIterable.iterator();
        while (iterator.hasNext()) {
            strings.add(iterator.next());
        }

        StringBuilder ab = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            ab.append(strings.get(i));
            if (i != strings.size() - 1) {
                ab.append(delimiter);
            }
        }
        return ab.toString();
    }

    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("katharsis")     = false
     * StringUtils.isBlank("  katharsis  ") = false
     * </pre>
     *
     * @param value the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     */
    public static boolean isBlank(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(value.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static String emptyToNull(String value) {
        if (value.length() == 0) {
            return null;
        }
        return value;
    }
}
