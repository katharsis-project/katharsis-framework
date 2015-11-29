package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.adapter.FieldRepositoryAdapter;
import io.katharsis.repository.annotations.*;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.repository.exception.RepositoryMethodException;
import io.katharsis.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class FieldRepositoryAdapterTest {

    private QueryParams queryParams;
    private ParametersFactory parameterProvider;

    @Before
    public void setUp() throws Exception {
        queryParams = new QueryParams();
        parameterProvider = new ParametersFactory(new NewInstanceRepositoryMethodParameterProvider());
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutAddFieldShouldThrowException() throws Exception {
        // GIVEN
        FieldRepositoryWithoutAnyMethods repo = new FieldRepositoryWithoutAnyMethods();
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.addField(1L, new Project(), "project", queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidAddFieldShouldThrowException() throws Exception {
        // GIVEN
        FieldRepositoryWithEmptyAddField repo = new FieldRepositoryWithEmptyAddField();
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.addField(1L, new Project(), "project", queryParams);
    }

    @Test
    public void onClassWithAddFieldShouldReturnValue() throws Exception {
        // GIVEN
        FieldRepositoryWithAddField repo = spy(FieldRepositoryWithAddField.class);
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        Project entity = new Project();
        Project result = sut.addField(1L, entity, "project", queryParams);

        // THEN
        verify(repo).addField(eq(1L), eq(entity), eq("project"), eq(queryParams), eq(""));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutAddFieldsShouldThrowException() throws Exception {
        // GIVEN
        FieldRepositoryWithoutAnyMethods repo = new FieldRepositoryWithoutAnyMethods();
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.addFields(1L, Collections.singleton(new Project()), "project", queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidAddFieldsShouldThrowException() throws Exception {
        // GIVEN
        FieldRepositoryWithEmptyAddFields repo = new FieldRepositoryWithEmptyAddFields();
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.addFields(1L, Collections.singleton(new Project()), "project", queryParams);
    }

    @Test
    public void onClassWithAddFieldsShouldReturnValue() throws Exception {
        // GIVEN
        FieldRepositoryWithAddFields repo = spy(FieldRepositoryWithAddFields.class);
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        Set<Project> entities = Collections.singleton(new Project());
        Iterable<Project> result = sut.addFields(1L, entities, "project", queryParams);

        // THEN
        verify(repo).addFields(eq(1L), eq(entities), eq("project"), eq(queryParams), eq(""));
        assertThat(result).isNotNull();
        result.forEach(project -> assertThat(project.getId()).isEqualTo(1L));
        
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutDeleteFieldShouldThrowException() throws Exception {
        // GIVEN
        FieldRepositoryWithoutAnyMethods repo = new FieldRepositoryWithoutAnyMethods();
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.deleteField(1L, "project", queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidDeleteFieldShouldThrowException() throws Exception {
        // GIVEN
        FieldRepositoryWithEmptyDeleteField repo = new FieldRepositoryWithEmptyDeleteField();
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.deleteField(1L, "project", queryParams);
    }

    @Test
    public void onClassWithDeleteFieldShouldReturnValue() throws Exception {
        // GIVEN
        FieldRepositoryWithDeleteField repo = spy(FieldRepositoryWithDeleteField.class);
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.deleteField(1L, "project", queryParams);

        // THEN
        verify(repo).deleteField(eq(1L), eq("project"), eq(queryParams), eq(""));
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutDeleteFieldsShouldThrowException() throws Exception {
        // GIVEN
        FieldRepositoryWithoutAnyMethods repo = new FieldRepositoryWithoutAnyMethods();
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.deleteFields(1L, Collections.singleton(1L), "project", queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidDeleteFieldsShouldThrowException() throws Exception {
        // GIVEN
        FieldRepositoryWithEmptyDeleteFields repo = new FieldRepositoryWithEmptyDeleteFields();
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.deleteFields(1L, Collections.singleton(1L), "project", queryParams);
    }

    @Test
    public void onClassWithDeleteFieldsShouldReturnValue() throws Exception {
        // GIVEN
        FieldRepositoryWithDeleteFields repo = spy(FieldRepositoryWithDeleteFields.class);
        FieldRepositoryAdapter<Task, Long, Project, Long> sut = new FieldRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        Set<Long> entityIds = Collections.singleton(1L);
        sut.deleteFields(1L, entityIds, "project", queryParams);

        // THEN
        verify(repo).deleteFields(eq(1L), eq(entityIds), eq("project"), eq(queryParams), eq(""));

    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithoutAnyMethods {
    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithEmptyAddField {

        @JsonApiAddField
        public Project addField() {
            return new Project();
        }
    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithAddField {

        @JsonApiAddField
        public Project addField(Long id, Project project, String fieldName, QueryParams queryParams, String s) {
            return project
                .setId(1L);
        }
    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithEmptyAddFields {

        @JsonApiAddFields
        public Iterable<Project> addFields() {
            return Collections.emptyList();
        }
    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithAddFields {

        @JsonApiAddFields
        public Iterable<Project> addFields(Long id, Iterable<Project> projects, String fieldName, QueryParams queryParams, String s) {
            long i = 1;
            for (Project project : projects) {
                project.setId(i++);
            }
            return projects;
        }
    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithEmptyDeleteField {

        @JsonApiDeleteField
        public void deleteField() {
        }
    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithDeleteField {

        @JsonApiDeleteField
        public void deleteField(Long id, String fieldName, QueryParams queryParams, String s) {
        }
    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithEmptyDeleteFields {

        @JsonApiDeleteFields
        public void deleteFields() {
        }
    }

    @JsonApiFieldRepository(source = Task.class, target = Project.class)
    public static class FieldRepositoryWithDeleteFields {

        @JsonApiDeleteFields
        public void deleteFields(Long id, Iterable<Long> entityIds, String fieldName, QueryParams queryParams, String s) {
        }
    }
}
