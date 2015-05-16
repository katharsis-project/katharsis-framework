package io.katharsis.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceFieldNameTransformerTest {

    private ResourceFieldNameTransformer sut;

    @Before
    public void setUp() throws Exception {
        sut = new ResourceFieldNameTransformer();
    }

    @Test
    public void onFieldWithoutJsonPropertyShouldReturnBaseName() throws Exception {
        // GIVEN
        Field field = TestClass.class.getDeclaredField("field");

        // WHEN
        String name = sut.getName(field);

        // THEN
        assertThat(name).isEqualTo("field");
    }

    @Test
    public void onFieldWithJsonPropertyShouldReturnCustomName() throws Exception {
        // GIVEN
        Field field = TestClass.class.getDeclaredField("fieldWithJsonProperty");

        // WHEN
        String name = sut.getName(field);

        // THEN
        assertThat(name).isEqualTo("customName");
    }

    @Test
    public void onFieldWithDefaultJsonPropertyShouldReturnBaseName() throws Exception {
        // GIVEN
        Field field = TestClass.class.getDeclaredField("fieldWithDefaultJsonProperty");

        // WHEN
        String name = sut.getName(field);

        // THEN
        assertThat(name).isEqualTo("fieldWithDefaultJsonProperty");
    }

    public static class TestClass {

        private String field;

        @JsonProperty("customName")
        private String fieldWithJsonProperty;

        @JsonProperty
        private String fieldWithDefaultJsonProperty;
    }
}
