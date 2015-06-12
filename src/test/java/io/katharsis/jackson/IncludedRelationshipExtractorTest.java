package io.katharsis.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.serializer.IncludedRelationshipExtractor;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.queryParams.RequestParamsBuilder;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class IncludedRelationshipExtractorTest {

    private IncludedRelationshipExtractor sut;

    @Before
    public void setUp() throws Exception {
        sut = new IncludedRelationshipExtractor();

    }

    @Test
    public void onEmptyInclusionShouldReturnEmptySet() throws Exception {
        // GIVEN
        ResourceResponse response = new ResourceResponse(null, null, new RequestParams(null));

        // WHEN
        Set result = sut.extractIncludedResources(new Project(), Collections.<Field>emptySet(), response);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onDefaultNullInclusionShouldReturnEmptySet() throws Exception {
        // GIVEN
        ResourceResponse response = new ResourceResponse(null, null, new RequestParams(null));

        // WHEN
        Set result = sut.extractIncludedResources(new Task(), Collections.singleton(Task.class.getDeclaredField("project")), response);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onDefaultInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        ResourceResponse response = new ResourceResponse(null, null, new RequestParams(null));
        Task resource = new Task();
        Project project = new Project();
        resource.setProject(project);

        // WHEN
        Set result = sut.extractIncludedResources(resource, Collections.singleton(Task.class.getDeclaredField("project")), response);

        // THEN
        assertThat(result).containsExactly(new Container(project));
    }

    @Test
    public void onInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        RequestParams requestParams = getRequestParamsWithInclusion("[\"project\"]");

        ResourceResponse response = new ResourceResponse(null, new ResourcePath("tasks"), requestParams);
        Task resource = new Task();
        Project project = new Project();
        resource.setProject(project);

        // WHEN
        Set result = sut.extractIncludedResources(resource, Collections.emptySet(), response);

        // THEN
        assertThat(result).containsExactly(new Container(project));
    }

    @Test
    public void onNullInclusionShouldReturnEmptySet() throws Exception {
        // GIVEN
        RequestParams requestParams = getRequestParamsWithInclusion("[]");
        ResourceResponse response = new ResourceResponse(null, new ResourcePath("tasks"), requestParams);

        // WHEN
        Set result = sut.extractIncludedResources(null, Collections.emptySet(), response);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    public void onFieldInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        RequestParams requestParams = getRequestParamsWithInclusion("[\"task.project\"]");

        ResourceResponse response = new ResourceResponse(null, new FieldPath("project"), requestParams);
        Task resource = new Task();
        Project project = new Project();
        resource.setProject(project);

        // WHEN
        Set result = sut.extractIncludedResources(resource, Collections.emptySet(), response);

        // THEN
        assertThat(result).containsExactly(new Container(project));
    }

    @Test
    public void onMultipleFieldsInclusionShouldReturnOneElement() throws Exception {
        // GIVEN
        RequestParams requestParams = getRequestParamsWithInclusion("[\"classBs.classCs\"]");

        ResourceResponse response = new ResourceResponse(null, new ResourcePath("classAs"), requestParams);
        ClassC classC = new ClassC();
        ClassA classA = new ClassA(new ClassB(classC));

        // WHEN
        Set result = sut.extractIncludedResources(classA, Collections.emptySet(), response);

        // THEN
        assertThat(result).containsExactly(new Container(classC));
    }

    @Test
    public void onNullFieldInclusionShouldReturnEmptySet() throws Exception {
        // GIVEN
        RequestParams requestParams = getRequestParamsWithInclusion("[\"task.project\"]");
        ResourceResponse response = new ResourceResponse(null, new FieldPath("tasks"), requestParams);
        Task resource = new Task();

        // WHEN
        Set result = sut.extractIncludedResources(resource, Collections.emptySet(), response);

        // THEN
        assertThat(result).isEmpty();
    }

    private RequestParams getRequestParamsWithInclusion(String project1) {
        RequestParamsBuilder requestParamsBuilder = new RequestParamsBuilder(new ObjectMapper());
        return requestParamsBuilder.buildRequestParams(Collections.singletonMap(RestrictedQueryParamsMembers.include.name(), project1));
    }

    private static class ClassA {
        private List<ClassB> classBs;

        public ClassA(ClassB classBs) {
            this.classBs = Collections.singletonList(classBs);
        }

        public List<ClassB> getClassBs() {
            return classBs;
        }
    }

    private static class ClassB {
        private List<ClassC> classCs;

        public ClassB(ClassC classCs) {
            this.classCs = Collections.singletonList(classCs);
        }

        public List<ClassC> getClassCs() {
            return classCs;
        }
    }

    private static class ClassC {
    }
}
