package io.katharsis.utils.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
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
        addType(asList(Byte.class, byte.class), new StandardTypeParser<Byte>() {
            @Override
            public Byte parse(String input) {
                return Byte.valueOf(input);
            }
        });
        addType(asList(Short.class, short.class), new StandardTypeParser<Short>() {
            @Override
            public Short parse(String input) {
                return Short.valueOf(input);
            }
        });
        addType(asList(Integer.class, int.class), new StandardTypeParser<Integer>() {
            @Override
            public Integer parse(String input) {
                return Integer.valueOf(input);
            }
        });
        addType(asList(Long.class, long.class), new StandardTypeParser<Long>() {
            @Override
            public Long parse(String input) {
                return Long.valueOf(input);
            }
        });
        addType(asList(Float.class, float.class), new StandardTypeParser<Float>() {
            @Override
            public Float parse(String input) {
                return Float.valueOf(input);
            }
        });
        addType(asList(Double.class, double.class), new StandardTypeParser<Double>() {
            @Override
            public Double parse(String input) {
                return Double.valueOf(input);
            }
        });
        addType(singletonList(BigInteger.class), new StandardTypeParser<BigInteger>() {
            @Override
            public BigInteger parse(String input) {
                return new BigInteger(input);
            }
        });
        addType(singletonList(BigDecimal.class), new StandardTypeParser<BigDecimal>() {
            @Override
            public BigDecimal parse(String input) {
                return new BigDecimal(input);
            }
        });
        addType(asList(Character.class, char.class), new StandardTypeParser<Character>() {
            @Override
            public Character parse(String input) {
                if (input.length() != 1) {
                    throwException(Character.class, input);
                }
                return input.charAt(0);
            }
        });
        addType(asList(Boolean.class, boolean.class), new StandardTypeParser<Boolean>() {
            @Override
            public Boolean parse(String input) {
                String inputNormalized = input.toLowerCase();
                if ("true".equals(inputNormalized) || "t".equals(inputNormalized)) {
                    return true;
                } else if ("false".equals(inputNormalized) || "f".equals(inputNormalized)) {
                    return false;
                } else {
                    throwException(Boolean.class, input);
                }
                return false;
            }
        });
        addType(Collections.singletonList(UUID.class), new StandardTypeParser<UUID>() {
            @Override
            public UUID parse(String input) {
                return UUID.fromString(input);
            }
        });
    }


    private static <T> void addType(List<Class<T>> classes, StandardTypeParser<T> standardTypeParser) {
        for (Class<T> clazz : classes) {
            parsers.put(clazz, standardTypeParser);
        }
    }

    private static void throwException(Class clazz, String input) {
        throw new IllegalArgumentException(String.format("String cannot be casted to %s: %s", clazz, input));
    }
}
