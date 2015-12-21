package io.katharsis.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StringUtils {

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
}
