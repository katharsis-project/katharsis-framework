package io.katharsis.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
