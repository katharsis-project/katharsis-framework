package io.katharsis.utils.parser;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * Parses {@link String} into an instance of provided {@link Class}. It support the following classes:
 * <ol>
 *     <li>{@link String}</li>
 *     <li>{@link Byte} and {@link byte}</li>
 *     <li>{@link Short} and {@link short}</li>
 *     <li>{@link Integer} and {@link int}</li>
 *     <li>{@link Long} and {@link long}</li>
 *     <li>{@link Float} and {@link float}</li>
 *     <li>{@link Double} and {@link double}</li>
 *     <li>{@link BigInteger}</li>
 *     <li>{@link BigDecimal}</li>
 *     <li>{@link Character} and {@link char}</li>
 *     <li>{@link Boolean} and {@link boolean}</li>
 *     <li>An {@link Enum}</li>
 *     <li>A class with a {@link String} only constructor</li>
 * </ol>
 */
public class TypeParser {

    /**
     * Parses an {@link Iterable} of String instances to {@link Iterable} of parsed values.
     * @param inputs list of Strings
     * @param clazz type to be parsed to
     * @param <T> type of class
     * @return {@link Iterable} of parsed values
     */
    public <T extends Serializable> Iterable<T> parse(Iterable<String> inputs, Class<T> clazz) {
        List<T> parsedValues = new LinkedList<>();
        for (String input : inputs) {
            parsedValues.add(parse(input, clazz));
        }

        return parsedValues;
    }

    /**
     *Parses a {@link String} to an instance of passed {@link Class}
     * @param input String value
     * @param clazz type to be parsed to
     * @param <T> type of class
     * @return instance of parsed value
     */
    public <T extends Serializable> T parse(String input, Class<T> clazz) {
        try {
            return parseInput(input, clazz);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                NumberFormatException | ParserException
                e) {
            throw new ParserException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T parseInput(String input, Class<T> clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        T parsedValue;
        if (String.class.equals(clazz)) {
            parsedValue = (T) input;
        } else if (StandardTypeParsers.parsers.containsKey(clazz)) {
            StandardTypeParser standardTypeParser = StandardTypeParsers.parsers.get(clazz);

            parsedValue = (T) standardTypeParser.parse(input);
        } else if (isEnum(clazz)) {
            parsedValue = (T) Enum.valueOf((Class<Enum>)clazz.asSubclass(Enum.class), input.trim());
        } else if (containsStringConstructor(clazz)) {
            parsedValue = clazz.getDeclaredConstructor(String.class).newInstance(input);
        } else {
            throw new ParserException(String.format("Cannot parse to %s : %s", clazz.getName(), input));
        }
        return parsedValue;
    }

    private <T extends Serializable> boolean isEnum(Class<T> clazz) {
        return clazz.isEnum();
    }

    private boolean containsStringConstructor(Class<?> clazz) throws NoSuchMethodException {
        boolean result = false;
        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 1 && constructor.getParameterTypes()[0] == String.class) {
                result = true;
            }
        }
        return result;
    }
}
