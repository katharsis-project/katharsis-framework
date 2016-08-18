package io.katharsis.jackson;

import io.katharsis.jackson.mock.models.*;
import io.katharsis.jackson.serializer.include.IncludedRelationshipExtractor;
import io.katharsis.jackson.serializer.include.ResourceDigest;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.response.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IncludedRelationshipExtractorTest {

    private IncludedRelationshipExtractor sut;
    private ResourceResponseContext testResponse;

    @Before
    public void setUp() throws Exception {
        ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
                new ResourceFieldNameTransformer());

        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
                resourceInformationBuilder);

        String resourceSearchPackage = String.format("%s,%s", ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE,
                "io.katharsis.jackson.mock");
        ResourceRegistry resourceRegistry = registryBuilder
                .build(resourceSearchPackage, ResourceRegistryTest.TEST_MODELS_URL);

        sut = new IncludedRelationshipExtractor(resourceRegistry);

        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");
        testResponse = new ResourceResponseContext(new JsonApiResponse(), jsonPath, new QueryParams());
    }

    @Test
    public void onEmptyInclusionShouldReturnEmptySet() throws Exception {
        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(new Project(), testResponse);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onDefaultNullInclusionShouldReturnEmptySet() throws Exception {
        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(new ClassAWithInclusion(), testResponse);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onDefaultInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        ClassBWithInclusion classBsWithInclusion = new ClassBWithInclusion()
                .setId(42L);
        ClassAWithInclusion classAWithInclusion = new ClassAWithInclusion(classBsWithInclusion);

        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classAWithInclusion, testResponse);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result).containsKey(new ResourceDigest(42L, "classBsWithInclusion"));
        assertThat(result).containsValue(new Container(classBsWithInclusion, testResponse));
    }

    @Test
    public void onDefaultInclusionShouldReturnTwoElements() throws Exception {
        // GIVEN
        ClassCWithInclusion classCWithInclusion = new ClassCWithInclusion()
                .setId(43L);
        ClassBWithInclusion classBWithInclusion = new ClassBWithInclusion(classCWithInclusion)
                .setId(42L);
        ClassAWithInclusion classAWithInclusion = new ClassAWithInclusion(classBWithInclusion);

        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classAWithInclusion, testResponse);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys(new ResourceDigest(42L, "classBsWithInclusion"),
                new ResourceDigest(43L, "classCsWithInclusion"));
        assertThat(result).containsValues(new Container(classBWithInclusion, testResponse),
                new Container(classCWithInclusion, testResponse));
    }

    @Test
    public void onDefaultInclusionWithLoopShouldReturnOneElement() throws Exception {
        // GIVEN
        ClassCWithInclusion classCWithInclusion = new ClassCWithInclusion()
                .setId(42L);
        classCWithInclusion.setClassCsWithInclusion(Collections.singletonList(classCWithInclusion));

        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classCWithInclusion, testResponse);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result).containsKey(new ResourceDigest(42L, "classCsWithInclusion"));
        assertThat(result).containsValues(new Container(classCWithInclusion, testResponse));
    }

    @Test
    public void onInclusionWithDefaultInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[classAsWithInclusion]",
                "classBsWithInclusion");

        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse(),
                new ResourcePath("classAsWithInclusion"), queryParams);
        ClassBWithInclusion classBsWithInclusion = new ClassBWithInclusion()
                .setId(42L);
        ClassAWithInclusion classAWithInclusion = new ClassAWithInclusion(classBsWithInclusion);

        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classAWithInclusion, response);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result).containsKey(new ResourceDigest(42L, "classBsWithInclusion"));
        assertThat(result).containsValue(new Container(classBsWithInclusion, testResponse));
    }

    @Test
    public void onInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[classAs]",
                "classBs");

        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse(),
                new ResourcePath("classAs"), queryParams);
        ClassB classBs = new ClassB()
                .setId(42L);
        ClassA classA = new ClassA(classBs);

        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classA, response);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result).containsKey(new ResourceDigest(42L, "classBs"));
        assertThat(result).containsValue(new Container(classBs, testResponse));
    }

    @Test(expected = ResourceFieldNotFoundException.class)
    public void onNonExistingInclusionShouldReturnMatchingError() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[classAs]",
                "asdasd");

        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse(),
                new ResourcePath("classAs"), queryParams);
        ClassB classBs = new ClassB();
        ClassA classA = new ClassA(classBs);

        // WHEN
        sut.extractIncludedResources(classA, response);
    }

    @Test
    public void onNullJsonPathShouldReturnNoElements() throws Exception {
        // GIVEN
        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse(), HttpStatus.NO_CONTENT_204);
        ClassA classA = new ClassA(null);
        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classA, response);

        assertThat(result).isEmpty();
    }

    @Test
    public void onDifferentTypeInclusionShouldReturnNoElements() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[classBsWith]",
                "classCsWith");

        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse(),
                new ResourcePath("classAsWith"), queryParams);
        ClassA classAWith = new ClassA(new ClassB());

        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classAWith, response);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onNestedInclusionShouldReturnMultipleElements() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[classAs]",
                "classBs.classC");

        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse(),
                new ResourcePath("classAs"), queryParams);
        ClassC classC = new ClassC();
        classC.setId(1L);
        ClassB classB = new ClassB(null, classC);
        classB.setId(2L);
        ClassA classAs = new ClassA(classB);
        classAs.setId(3L);

        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classAs, response);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).containsKey(new ResourceDigest(2L, "classBs"));
        assertThat(result).containsValue(new Container(classB, testResponse, ContainerType.INCLUDED, "classBs"));
        assertThat(result).containsKey(new ResourceDigest(1L, "classCs"));
        assertThat(result).containsValue(new Container(classC, testResponse, ContainerType.INCLUDED_NESTED, "classC"));
    }

    @Test
    public void onNestedDefaultInclusionShouldReturnMultipleElements() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[classAs]",
                "classBs");

        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse(),
                new ResourcePath("classAs"), queryParams);
        ClassA nestedClassA = new ClassA(null);
        nestedClassA.setId(1L);
        ClassB classB = new ClassB(nestedClassA);
        classB.setId(2L);
        ClassA classAs = new ClassA(classB);
        classAs.setId(3L);

        // WHEN
        Map<ResourceDigest, Container> result = sut.extractIncludedResources(classAs, response);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).containsKey(new ResourceDigest(2L, "classBs"));
        assertThat(result).containsValue(new Container(classB, testResponse, ContainerType.INCLUDED, "classBs"));
        assertThat(result).containsKey(new ResourceDigest(1L, "classAs"));
        assertThat(result).containsValue(new Container(nestedClassA, testResponse, ContainerType.INCLUDED_NESTED, "classC"));
    }

    private QueryParams getRequestParamsWithInclusion(String resourceType, String relationshipField) {
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        return queryParamsBuilder.buildQueryParams(Collections.singletonMap(resourceType, Collections.singleton(relationshipField)));
    }
}
