package io.katharsis.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.concurrent.Future;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.errorhandling.exception.MultipleJsonApiLinksInformationException;
import io.katharsis.errorhandling.exception.MultipleJsonApiMetaInformationException;
import io.katharsis.errorhandling.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.errorhandling.exception.ResourceDuplicateIdException;
import io.katharsis.errorhandling.exception.ResourceIdNotFoundException;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiRelation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.annotations.LookupIncludeBehavior;
import io.katharsis.resource.annotations.SerializeType;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilderContext;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.UnAnnotatedTask;
import io.katharsis.utils.parser.TypeParser;

public class ResourceInformationBuilderTest {

	private static final String NAME_PROPERTY = "underlyingName";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private final ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());

	private final ResourceInformationBuilderContext context = new DefaultResourceInformationBuilderContext(resourceInformationBuilder, new TypeParser());

	@Before
	public void setup() {
		resourceInformationBuilder.init(context);
	}

	@Test
	public void shouldHaveResourceClassInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getResourceClass()).isNotNull().isEqualTo(Task.class);
	}

	@Test
	public void shouldHaveIdFieldInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getIdField().getUnderlyingName()).isNotNull().isEqualTo("id");
	}

	@Test
	public void shouldThrowExceptionWhenResourceWithNoAnnotation() {
		expectedException.expect(RepositoryAnnotationNotFoundException.class);

		resourceInformationBuilder.build(UnAnnotatedTask.class);
	}

	@Test
	public void shouldThrowExceptionWhenMoreThan1IdAnnotationFound() throws Exception {
		expectedException.expect(ResourceDuplicateIdException.class);
		expectedException.expectMessage("Duplicated Id field found in class");

		resourceInformationBuilder.build(DuplicatedIdResource.class);
	}

	@Test
	public void shouldHaveProperRelationshipFieldInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(5).extracting(NAME_PROPERTY).contains("project", "projects");
	}

	@Test
	public void shouldThrowExceptionWhenResourceWithIgnoredIdAnnotation() {
		expectedException.expect(ResourceIdNotFoundException.class);

		resourceInformationBuilder.build(IgnoredIdResource.class);
	}

	@Test
	public void shouldReturnIdFieldBasedOnFieldGetter() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(IdFieldWithAccessorGetterResource.class);

		assertThat(resourceInformation.getIdField()).isNotNull();
	}

	@Test
	public void shouldReturnMergedAnnotationsOnAnnotationsOnFieldAndMethod() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(AnnotationOnFieldAndMethodResource.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(0);
	}

	@Test
	public void shouldContainMetaInformationField() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getMetaField().getUnderlyingName()).isEqualTo("metaInformation");
	}

	@Test
	public void shouldThrowExceptionOnMultipleMetaInformationFields() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getMetaField().getUnderlyingName()).isEqualTo("metaInformation");
	}

	@Test
	public void shouldContainLinksInformationField() throws Exception {
		expectedException.expect(MultipleJsonApiMetaInformationException.class);

		resourceInformationBuilder.build(MultipleMetaInformationResource.class);
	}

	@Test
	public void shouldThrowExceptionOnMultipleLinksInformationFields() throws Exception {
		expectedException.expect(MultipleJsonApiLinksInformationException.class);

		resourceInformationBuilder.build(MultipleLinksInformationResource.class);
	}

	@Test
	public void shouldHaveProperTypeWhenFieldAndGetterTypesDiffer() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(DifferentTypes.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(1).extracting("type").contains(String.class);
	}

	@Test
	public void shouldHaveProperTypeWhenFieldAndGetterTypesDifferV2() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(DifferentTypes.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(1).extracting("type").contains(String.class);
	}

	@Test
	public void shouldRecognizeJsonAPIRelationTypeWithDefaults() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(JsonApiRelationType.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotEmpty().hasSize(2).extracting("type").contains(Future.class).contains(Collection.class);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lazy").contains(true, true);
		assertThat(resourceInformation.getRelationshipFields()).extracting("includeByDefault").contains(false, false);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior").contains(LookupIncludeBehavior.NONE, LookupIncludeBehavior.NONE);
		assertThat(resourceInformation.getRelationshipFields()).extracting("resourceFieldType").contains(ResourceFieldType.RELATIONSHIP, ResourceFieldType.RELATIONSHIP);
	}

	@Test
	public void shouldRecognizeJsonAPIRelationTypeWithNonDefaults() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(JsonApiRelationTypeNonDefaults.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotEmpty().hasSize(2).extracting("type").contains(Future.class).contains(Collection.class);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lazy").contains(false, false);
		assertThat(resourceInformation.getRelationshipFields()).extracting("includeByDefault").contains(false, true);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior").contains(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
		assertThat(resourceInformation.getRelationshipFields()).extracting("resourceFieldType").contains(ResourceFieldType.RELATIONSHIP, ResourceFieldType.RELATIONSHIP);
	}

	@JsonApiResource(type = "duplicatedIdAnnotationResources")
	private static class DuplicatedIdResource {
		@JsonApiId
		private Long id;

		@JsonApiId
		private Long id2;
	}

	@JsonApiResource(type = "ignoredId")
	private static class IgnoredIdResource {
		@JsonApiId
		@JsonIgnore
		private Long id;
	}

	@JsonApiResource(type = "ignoredAttribute")
	private static class IgnoredAttributeResource {
		@JsonApiId
		private Long id;

		@JsonIgnore
		private String attribute;
	}

	@JsonApiResource(type = "accessorGetter")
	private static class AccessorGetterResource {
		@JsonApiId
		private Long id;

		private String getAccessorField() {
			return null;
		}
	}

	@JsonApiResource(type = "ignoredAccessorGetter")
	private static class IgnoredAccessorGetterResource {
		@JsonApiId
		private Long id;

		@JsonIgnore
		private String getAccessorField() {
			return null;
		}
	}

	@JsonApiResource(type = "fieldWithAccessorGetterResource")
	private static class FieldWithAccessorGetterResource {
		@JsonApiId
		private Long id;

		public String getAccessorField() {
			return accessorField;
		}

		private String accessorField;
	}

	@JsonApiResource(type = "idFieldWithAccessorGetterResource")
	private static class IdFieldWithAccessorGetterResource {

		@JsonApiId
		public Long getId() {
			return null;
		}
	}

	@JsonApiResource(type = "annotationOnFieldAndMethod")
	private static class AnnotationOnFieldAndMethodResource {
		@JsonApiId
		private Long id;

		@JsonIgnore
		private String field;

		@JsonApiToOne
		private String getField() {
			return null;
		}
	}

	@JsonApiResource(type = "ignoredAttribute")
	private static class IgnoredStaticAttributeResource {
		@JsonApiId
		private Long id;

		public static String attribute;
	}

	@JsonApiResource(type = "ignoredAttribute")
	private static class IgnoredTransientAttributeResource {

		@JsonApiId
		private Long id;

		public transient int attribute;

		public int getAttribute() {
			return attribute;
		}

	}

	@JsonApiResource(type = "ignoredAttribute")
	private static class IgnoredStaticGetterResource {
		@JsonApiId
		private Long id;

		public static int getAttribute() {
			return 0;
		}
	}

	@JsonPropertyOrder({"b", "a", "c"})
	@JsonApiResource(type = "orderedResource")
	private static class OrderedResource {
		@JsonApiId
		private Long id;

		public String c;
		public String b;
		public String a;
	}

	@JsonPropertyOrder(alphabetic = true)
	@JsonApiResource(type = "AlphabeticResource")
	private static class AlphabeticResource {
		@JsonApiId
		private Long id;

		public String c;
		public String b;
		public String a;
	}

	@JsonApiResource(type = "multipleMetaInformationResource")
	private static class MultipleMetaInformationResource {
		@JsonApiId
		private Long id;

		@JsonApiMetaInformation
		public String c;

		@JsonApiMetaInformation
		public String b;
	}

	@JsonApiResource(type = "multipleLinksInformationResource")
	private static class MultipleLinksInformationResource {
		@JsonApiId
		private Long id;

		@JsonApiLinksInformation
		public String c;

		@JsonApiLinksInformation
		public String b;
	}

	@JsonApiResource(type = "differentTypes")
	private static class DifferentTypes {
		@JsonApiId
		private Long id;

		public Future<String> field;

		@JsonApiToOne
		public String getField() {
			return null;
		}
	}

	@JsonApiResource(type = "differentTypesv2")
	private static class DifferentTypesv2 {
		@JsonApiId
		private Long id;

		@JsonApiToOne
		public Future<String> field;

		public String getField() {
			return null;
		}
	}


	@JsonApiResource(type = "jsonAPIRelationType")
	private static class JsonApiRelationType {
		@JsonApiId
		private Long id;

		@JsonApiRelation
		public Future<String> field;

		@JsonApiRelation
		public Collection<Future<String>> fields;

		public String getField() {
			return null;
		}
	}

	@JsonApiResource(type = "jsonAPIRelationType")
	private static class JsonApiRelationTypeNonDefaults {
		@JsonApiId
		private Long id;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, serialize = SerializeType.EAGER)
		public Future<String> field;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.ONLY_ID)
		public Collection<Future<String>> fields;

		public String getField() {
			return null;
		}
	}
}
