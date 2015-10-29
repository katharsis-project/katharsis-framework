package io.katharsis.jackson;

import io.katharsis.jackson.mock.models.*;
import io.katharsis.jackson.serializer.IncludedRelationshipExtractor;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class IncludedRelationshipExtractorTest {

    private IncludedRelationshipExtractor sut;
    private ResourceField resourceField;
    private ResourceResponse testResponse;

    @Before
    public void setUp() throws Exception {
        ResourceInformationBuilder resourceInformationBuilder = new ResourceInformationBuilder(
            new ResourceFieldNameTransformer());

        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
            resourceInformationBuilder);

        String resourceSearchPackage = String.format("%s,%s", ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE,
            "io.katharsis.jackson.mock");
        ResourceRegistry resourceRegistry = registryBuilder
            .build(resourceSearchPackage, ResourceRegistryTest.TEST_MODELS_URL);

        sut = new IncludedRelationshipExtractor(resourceRegistry);
        Field someField = Task.class.getDeclaredField("project");
        List<Annotation> declaredAnnotations = Arrays.asList(someField.getDeclaredAnnotations());
        resourceField = new ResourceField(someField.getName(), someField.getType(), someField.getGenericType(),
            declaredAnnotations);

        testResponse = new ResourceResponse(null, null, new QueryParams(), null, null);
    }

    @Test
    public void onEmptyInclusionShouldReturnEmptySet() throws Exception {
        // GIVEN
        ResourceResponse response = new ResourceResponse(null, null, new QueryParams(), null, null);

        // WHEN
        Set result = sut.extractIncludedResources(new Project(), response);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onDefaultNullInclusionShouldReturnEmptySet() throws Exception {
        // GIVEN
        ResourceResponse response = new ResourceResponse(null, null, new QueryParams(), null, null);

        // WHEN
        Set result = sut.extractIncludedResources(new ClassAWithInclusion(), response);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onDefaultInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        ResourceResponse response = new ResourceResponse(null, null, new QueryParams(), null, null);
        ClassBWithInclusion classBsWithInclusion = new ClassBWithInclusion();
        ClassAWithInclusion classAWithInclusion = new ClassAWithInclusion(classBsWithInclusion);

        // WHEN
        Set<?> result = sut.extractIncludedResources(classAWithInclusion, response);

        // THEN
        assertThat(result).containsExactly(new Container(classBsWithInclusion, testResponse));
    }

    @Test
    public void onDefaultInclusionShouldReturnTwoElements() throws Exception {
        // GIVEN
        ResourceResponse response = new ResourceResponse(null, null, new QueryParams(), null, null);
        ClassCWithInclusion classCWithInclusion = new ClassCWithInclusion();
        ClassBWithInclusion classBWithInclusion = new ClassBWithInclusion(classCWithInclusion);
        ClassAWithInclusion classAWithInclusion = new ClassAWithInclusion(classBWithInclusion);

        // WHEN
        Set<?> result = sut.extractIncludedResources(classAWithInclusion, response);

        // THEN
        assertThat(result).containsOnly(new Container(classBWithInclusion, testResponse),
            new Container(classCWithInclusion, testResponse));
    }

    @Test
    public void onDefaultInclusionWithLoopShouldReturnOneElement() throws Exception {
        // GIVEN
        ResourceResponse response = new ResourceResponse(null, null, new QueryParams(), null, null);
        ClassCWithInclusion classCWithInclusion = new ClassCWithInclusion();
        classCWithInclusion.setClassCsWithInclusion(Collections.singletonList(classCWithInclusion));

        // WHEN
        Set<?> result = sut.extractIncludedResources(classCWithInclusion, response);

        // THEN
        assertThat(result).containsExactly(new Container(classCWithInclusion, testResponse));
    }

    @Test
    public void onInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[classBsWithInclusion]");

        ResourceResponse response = new ResourceResponse(null, new ResourcePath("classAsWithInclusion"), queryParams,
            null, null);
        ClassBWithInclusion classBsWithInclusion = new ClassBWithInclusion();
        ClassAWithInclusion classAWithInclusion = new ClassAWithInclusion(classBsWithInclusion);

        // WHEN
        Set<?> result = sut.extractIncludedResources(classAWithInclusion, response);

        // THEN
        assertThat(result).containsExactly(new Container(classBsWithInclusion, testResponse));
    }

    @Test
    public void onNullInclusionShouldReturnEmptySet() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("");
        ResourceResponse response = new ResourceResponse(null, new ResourcePath("tasks"), queryParams, null, null);

        // WHEN
        Set<?> result = sut.extractIncludedResources(null, response);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onFieldInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[task][project]");

        ResourceResponse response = new ResourceResponse(null, new FieldPath("project"), queryParams, null, null);
        Task resource = new Task();
        Project project = new Project();
        resource.setProject(project);

        // WHEN
        Set<?> result = sut.extractIncludedResources(resource, response);

        // THEN
        assertThat(result).containsExactly(new Container(project, testResponse));
    }

    @Test
    public void onMultipleFieldsInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[classBs][classCs]");

        ResourceResponse response = new ResourceResponse(null, new ResourcePath("classAs"), queryParams, null, null);
        ClassC classC = new ClassC();
        ClassA classA = new ClassA(new ClassB(classC));

        // WHEN
        Set<?> result = sut.extractIncludedResources(classA, response);

        // THEN
        assertThat(result).containsExactly(new Container(classC, testResponse));
    }

    @Test
    public void onNullFieldInclusionShouldReturnEmptySet() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[task][project]");
        ResourceResponse response = new ResourceResponse(null, new FieldPath("tasks"), queryParams, null, null);
        Task resource = new Task();

        // WHEN
        Set<?> result = sut.extractIncludedResources(resource, response);

        // THEN
        assertThat(result).isEmpty();
    }

    private QueryParams getRequestParamsWithInclusion(String project1) {
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder();
        return queryParamsBuilder.buildQueryParams(Collections.singletonMap(project1, Collections.singleton("")));
    }
}
