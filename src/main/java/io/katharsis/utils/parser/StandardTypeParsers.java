package io.katharsis.utils.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Parsers for standard Java types.
 */
public final class StandardTypeParsers {
    public static final Map<Class, StandardTypeParser> parsers = new HashMap<>();

    static {
        addType(asList(Byte.class, byte.class), Byte::valueOf);
        addType(asList(Short.class, short.class), Short::valueOf);
        addType(asList(Integer.class, int.class), Integer::valueOf);
        addType(asList(Long.class, long.class), Long::valueOf);
        addType(asList(Float.class, float.class), Float::valueOf);
        addType(asList(Double.class, double.class), Double::valueOf);
        addType(singletonList(BigInteger.class), BigInteger::new);
        addType(singletonList(BigDecimal.class), BigDecimal::new);
        addType(asList(Character.class, char.class), (input) -> {
            if (input.length() != 1) {
                throwException(Character.class, input);
            }
            return input.charAt(0);
        });
        addType(asList(Boolean.class, boolean.class), (input) -> {
            String inputNormalized = input.toLowerCase();
            if ("true".equals(inputNormalized) || "t".equals(inputNormalized)) {
                return true;
            } else if ("false".equals(inputNormalized) || "f".equals(inputNormalized)) {
                return false;
            } else {
                throwException(Boolean.class, input);
            }
            return input.charAt(0);
        });
        addType(asList(UUID.class), UUID::fromString);
    }

    private static void addType(List<Class<?>> classes, StandardTypeParser standardTypeParser) {
        classes.forEach((clazz) -> parsers.put(clazz, standardTypeParser));
    }

    private static void throwException(Class clazz, String input) {
        throw new IllegalArgumentException(String.format("String cannot be casted to %s: %s", clazz, input));
    }
}
