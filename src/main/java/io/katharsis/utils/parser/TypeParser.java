package io.katharsis.utils.parser;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public class TypeParser {

    public <T extends Serializable> T parse(String input, Class<T> clazz) {
        try {
            return parseInput(input, clazz);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                NumberFormatException | ParserException
                e) {
            throw new ParserException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T parseInput(String input, Class<T> clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        T t;
        if (String.class.equals(clazz)) {
            t = (T) input;
        } else if (StandardTypeParsers.parsers.containsKey(clazz)) {
            StandardTypeParser standardTypeParser = StandardTypeParsers.parsers.get(clazz);

            t = (T) standardTypeParser.parse(input);
        } else if (isEnum(clazz)) {
            t = (T) Enum.valueOf((Class<Enum>)clazz.asSubclass(Enum.class), input.trim());
        } else if (containsStringConstructor(clazz)) {
            t = clazz.getDeclaredConstructor(String.class).newInstance(input);
        } else {
            throw new ParserException(String.format("Cannot parse to %s : %s", clazz.getName(), input));
        }
        return t;
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
