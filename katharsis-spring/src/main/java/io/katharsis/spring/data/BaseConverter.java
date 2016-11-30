package io.katharsis.spring.data;

import io.katharsis.utils.StringUtils;

/**
 * Base functionality for converting Katharsis objects
 */
abstract class BaseConverter {
    /**
     * Converts an attributePath String Iterable, which contains each property in the path, to a period delimited path string
     *
     * @param attributePath The Iterable of path tokens to convert
     * @return A period delimited string containing the joined attributePath tokens.
     */
    String convertAttributePathToString(Iterable<String> attributePath) {
        String result = null;

        if (attributePath != null) {
            result = StringUtils.join(".", attributePath);
        }

        return result;
    }
}
