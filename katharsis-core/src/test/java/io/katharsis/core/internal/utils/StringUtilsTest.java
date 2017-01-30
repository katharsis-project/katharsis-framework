package io.katharsis.core.internal.utils;

import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.katharsis.core.internal.utils.StringUtils;

public class StringUtilsTest {

    @Test
    public void onSingleElementShouldReturnTheSameValue() throws Exception {
        // GIVEN
        String string = "hello world";
        List<String> values = Collections.singletonList(string);

        // WHEN
        String result = StringUtils.join(",", values);

        // THEN
        assertThat(result).isEqualTo(string);
    }

    @Test
    public void onTwoElementsShouldReturnJoinedValues() throws Exception {
        // GIVEN
        List<String> values = Arrays.asList("hello", "world");

        // WHEN
        String result = StringUtils.join(" ", values);

        // THEN
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    public void onIsBlankValues() throws Exception {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertFalse(StringUtils.isBlank("katharsis"));
        assertFalse(StringUtils.isBlank("  katharsis  "));
    }


}
