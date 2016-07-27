package io.katharsis.resource.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiToMany;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceFieldTest {

    @Test
    public void onWithLazyFieldClassShouldReturnTrue() throws Exception {
        // GIVEN
        List<Annotation> annotations = Arrays.asList(WithLazyFieldClass.class.getDeclaredField("value").getAnnotations());
        ResourceField sut = new ResourceField("", "", String.class, String.class, annotations);

        // WHEN
        boolean result = sut.isLazy();

        // THEN

        assertThat(result).isTrue();
    }

    @Test
    public void onWithToManyEagerFieldClassShouldReturnFalse() throws Exception {
        // GIVEN
        List<Annotation> annotations = Arrays.asList(WithToManyEagerFieldClass.class.getDeclaredField("value").getAnnotations());
        ResourceField sut = new ResourceField("", "", String.class, String.class, annotations);

        // WHEN
        boolean result = sut.isLazy();

        // THEN

        assertThat(result).isFalse();
    }

    @Test
    public void onWithoutToManyFieldClassShouldReturnFalse() throws Exception {
        // GIVEN
        List<Annotation> annotations = Arrays.asList(WithoutToManyFieldClass.class.getDeclaredField("value").getAnnotations());
        ResourceField sut = new ResourceField("", "", String.class, String.class, annotations);

        // WHEN
        boolean result = sut.isLazy();

        // THEN

        assertThat(result).isFalse();
    }

    @Test
    public void onLazyRelationshipToManyAndInclusionByDefaultShouldReturnEagerFlag() throws Exception {
        // GIVEN
        List<Annotation> annotations = Arrays.asList(WithLazyFieldAndInclusionByDefaultClass.class.getDeclaredField("value").getAnnotations());
        ResourceField sut = new ResourceField("", "", String.class, String.class, annotations);

        // WHEN
        boolean result = sut.isLazy();

        // THEN

        assertThat(result).isFalse();
    }

    private static class WithLazyFieldClass {

        @JsonProperty("sth")
        @JsonApiToMany
        private String value;
    }

    private static class WithLazyFieldAndInclusionByDefaultClass {

        @JsonApiIncludeByDefault
        @JsonApiToMany
        private String value;
    }

    private static class WithToManyEagerFieldClass {

        @JsonApiToMany(lazy = false)
        private String value;
    }

    private static class WithoutToManyFieldClass {
        private String value;

    }
}
