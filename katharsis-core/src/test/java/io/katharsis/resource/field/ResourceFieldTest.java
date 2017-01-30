package io.katharsis.resource.field;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldType;

public class ResourceFieldTest {

	@Test
	public void getResourceFieldType() {
		assertThat(ResourceFieldType.get(true, false, false, false)).isEqualByComparingTo(ResourceFieldType.ID);
		assertThat(ResourceFieldType.get(false, true, false, false)).isEqualByComparingTo(ResourceFieldType.LINKS_INFORMATION);
		assertThat(ResourceFieldType.get(false, false, true, false)).isEqualByComparingTo(ResourceFieldType.META_INFORMATION);
		assertThat(ResourceFieldType.get(false, false, false, true)).isEqualByComparingTo(ResourceFieldType.RELATIONSHIP);
		assertThat(ResourceFieldType.get(false, false, false, false)).isEqualByComparingTo(ResourceFieldType.ATTRIBUTE);
	}

	@Test
	public void onWithLazyFieldClassShouldReturnTrue() throws Exception {
		// GIVEN
		List<Annotation> annotations = Arrays.asList(WithLazyFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut = new AnnotationResourceInformationBuilder.AnnotatedResourceField("", "", String.class, String.class, null, annotations);

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isTrue();
	}

	@Test
	public void onWithToManyEagerFieldClassShouldReturnFalse() throws Exception {
		// GIVEN
		List<Annotation> annotations = Arrays.asList(WithToManyEagerFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut = new AnnotationResourceInformationBuilder.AnnotatedResourceField("", "", String.class, String.class, null, annotations);

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isFalse();
	}

	@Test
	public void onWithoutToManyFieldClassShouldReturnFalse() throws Exception {
		// GIVEN
		List<Annotation> annotations = Arrays.asList(WithoutToManyFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut = new AnnotationResourceInformationBuilder.AnnotatedResourceField("", "", String.class, String.class, null, annotations);

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isFalse();
	}

	@Test
	public void onLazyRelationshipToManyAndInclusionByDefaultShouldReturnEagerFlag() throws Exception {
		// GIVEN
		List<Annotation> annotations = Arrays.asList(WithLazyFieldAndInclusionByDefaultClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut = new AnnotationResourceInformationBuilder.AnnotatedResourceField("", "", String.class, String.class, null, annotations);

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
