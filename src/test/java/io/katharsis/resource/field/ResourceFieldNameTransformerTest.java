package io.katharsis.resource.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    @Test
    public void onWrappedBooleanFieldShouldReturnFieldNameBasedOnGetter() throws Exception {
        // GIVEN
        Method method = TestClass.class.getDeclaredMethod("getAccessorField");

        // WHEN
        String name = sut.getName(method);

        // THEN
        assertThat(name).isEqualTo("accessorField");
    }

    @Test
    public void onWrappedFieldShouldReturnFieldNameBasedOnGetter() throws Exception {
        // GIVEN
        Method method = TestClass.class.getDeclaredMethod("isBooleanProperty");

        // WHEN
        String name = sut.getName(method);

        // THEN
        assertThat(name).isEqualTo("booleanProperty");
    }

    @Test
    public void onAnnotatedWrappedFieldShouldReturnFieldNameBasedOnAnnotation() throws Exception {
        // GIVEN
        Method method = TestClass.class.getDeclaredMethod("getAccessorFieldWithAnnotation");

        // WHEN
        String name = sut.getName(method);

        // THEN
        assertThat(name).isEqualTo("wrappedCustomName");
    }

    private static class TestClass {

        private String field;

        public String getAccessorField() {
            return null;
        }

        @JsonProperty("wrappedCustomName")
        public String getAccessorFieldWithAnnotation() {
            return null;
        }

        @JsonProperty("customName")
        private String fieldWithJsonProperty;

        @JsonProperty
        private String fieldWithDefaultJsonProperty;

        private boolean isBooleanProperty() {
            return false;
        }
    }
}
