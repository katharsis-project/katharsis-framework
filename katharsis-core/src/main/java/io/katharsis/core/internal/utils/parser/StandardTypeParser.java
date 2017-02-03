package io.katharsis.core.internal.utils.parser;

interface StandardTypeParser<T> {
    T parse(String input);
}
