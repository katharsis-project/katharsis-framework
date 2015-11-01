package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.repository.annotations.*;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.repository.exception.RepositoryMethodException;
import io.katharsis.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class RelationshipRepositoryAdapterTest {
    private QueryParams queryParams;
    private ParametersFactory parameterProvider;

    @Before
    public void setUp() throws Exception {
        queryParams = new QueryParams();
        parameterProvider = new ParametersFactory(new NewInstanceRepositoryMethodParameterProvider());
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutSetRelationShouldThrowException() throws Exception {
        // GIVEN
        RelationshipRepositoryWithoutAnyMethods repo = new RelationshipRepositoryWithoutAnyMethods();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.setRelation(new Task(), 1L, "project");
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithEmptySetRelationShouldThrowException() throws Exception {
        // GIVEN

        RelationshipRepositoryWithEmptySetRelation repo = new RelationshipRepositoryWithEmptySetRelation();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.setRelation(new Task(), 1L, "project");
    }

    @Test
    public void onClassWithSetRelationShouldSetValue() throws Exception {
        // GIVEN
        RelationshipRepositoryWithSetRelation repo = spy(RelationshipRepositoryWithSetRelation.class);
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);
        Task task = new Task();

        // WHEN
        sut.setRelation(task, 1L, "project");

        // THEN
        verify(repo).setRelation(eq(task), eq(1L), eq("project"));
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutSetRelationsShouldThrowException() throws Exception {
        // GIVEN
        RelationshipRepositoryWithoutAnyMethods repo = new RelationshipRepositoryWithoutAnyMethods();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.setRelations(new Task(), Collections.singleton(1L), "project");
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithEmptySetRelationsShouldThrowException() throws Exception {
        // GIVEN

        RelationshipRepositoryWithEmptySetRelations repo = new RelationshipRepositoryWithEmptySetRelations();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.setRelations(new Task(), Collections.singleton(1L), "project");
    }

    @Test
    public void onClassWithSetRelationsShouldSetValues() throws Exception {
        // GIVEN
        RelationshipRepositoryWithSetRelations repo = spy(RelationshipRepositoryWithSetRelations.class);
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);
        Task task = new Task();

        // WHEN
        sut.setRelations(task, Collections.singleton(1L), "project");

        // THEN
        verify(repo).setRelations(eq(task), eq(Collections.singleton(1L)), eq("project"));
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutAddRelationsShouldThrowException() throws Exception {
        // GIVEN
        RelationshipRepositoryWithoutAnyMethods repo = new RelationshipRepositoryWithoutAnyMethods();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.addRelations(new Task(), Collections.singleton(1L), "project");
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithEmptyAddRelationsShouldThrowException() throws Exception {
        // GIVEN

        RelationshipRepositoryWithEmptyAddRelations repo = new RelationshipRepositoryWithEmptyAddRelations();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.addRelations(new Task(), Collections.singleton(1L), "project");
    }

    @Test
    public void onClassWithAddRelationsShouldAddValue() throws Exception {
        // GIVEN
        RelationshipRepositoryWithAddRelations repo = spy(RelationshipRepositoryWithAddRelations.class);
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);
        Task task = new Task();

        // WHEN
        sut.addRelations(task, Collections.singleton(1L), "project");

        // THEN
        verify(repo).addRelations(eq(task), eq(Collections.singleton(1L)), eq("project"));
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutRemoveRelationsShouldThrowException() throws Exception {
        // GIVEN
        RelationshipRepositoryWithoutAnyMethods repo = new RelationshipRepositoryWithoutAnyMethods();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.removeRelations(new Task(), Collections.singleton(1L), "project");
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithEmptyRemoveRelationsShouldThrowException() throws Exception {
        // GIVEN

        RelationshipRepositoryWithEmptyRemoveRelations repo = new RelationshipRepositoryWithEmptyRemoveRelations();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.removeRelations(new Task(), Collections.singleton(1L), "project");
    }

    @Test
    public void onClassWithRemoveRelationsShouldAddValue() throws Exception {
        // GIVEN
        RelationshipRepositoryWithRemoveRelations repo = spy(RelationshipRepositoryWithRemoveRelations.class);
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);
        Task task = new Task();

        // WHEN
        sut.removeRelations(task, Collections.singleton(1L), "project");

        // THEN
        verify(repo).removeRelations(eq(task), eq(Collections.singleton(1L)), eq("project"));
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutFindOneTargetShouldThrowException() throws Exception {
        // GIVEN
        RelationshipRepositoryWithoutAnyMethods repo = new RelationshipRepositoryWithoutAnyMethods();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findOneTarget(1L, "project", queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithEmptyFindOneTargetShouldThrowException() throws Exception {
        // GIVEN

        RelationshipRepositoryWithEmptyFindOneTargetRelations repo = new RelationshipRepositoryWithEmptyFindOneTargetRelations();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findOneTarget(1L, "project", queryParams);
    }

    @Test
    public void onClassWithFindOneTargetShouldAddValue() throws Exception {
        // GIVEN
        RelationshipRepositoryWithFindOneTargetRelations repo = spy(RelationshipRepositoryWithFindOneTargetRelations.class);
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        Project project = sut.findOneTarget(1L, "project", queryParams);

        // THEN
        verify(repo).findOneTarget(1L, "project", queryParams, "");
        assertThat(project).isNotNull();
        assertThat(project.getId()).isEqualTo(42L);
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutFindManyTargetShouldThrowException() throws Exception {
        // GIVEN
        RelationshipRepositoryWithoutAnyMethods repo = new RelationshipRepositoryWithoutAnyMethods();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findManyTargets(1L, "project", queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithEmptyFindManyTargetShouldThrowException() throws Exception {
        // GIVEN

        RelationshipRepositoryWithEmptyFindManyTargetsRelations repo = new RelationshipRepositoryWithEmptyFindManyTargetsRelations();
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findManyTargets(1L, "project", queryParams);
    }

    @Test
    public void onClassWithFindManyTargetShouldAddValue() throws Exception {
        // GIVEN
        RelationshipRepositoryWithFindManyTargetsRelations repo = spy(RelationshipRepositoryWithFindManyTargetsRelations.class);
        RelationshipRepositoryAdapter<Task, Long, Project, Long> sut = new RelationshipRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        Iterable<Project> projects = sut.findManyTargets(1L, "project", queryParams);

        // THEN
        verify(repo).findManyTargets(1L, "project", queryParams, "");
        assertThat(projects).isNotNull();
        assertThat(projects).hasSize(1);
        assertThat(projects.iterator().next().getId()).isEqualTo(42L);
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithoutAnyMethods {
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithEmptySetRelation {

        @JsonApiSetRelation
        public void setRelation() {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithSetRelation {

        @JsonApiSetRelation
        public void setRelation(Task task, Long id, String fieldName) {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithEmptySetRelations {

        @JsonApiSetRelations
        public void setRelations() {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithSetRelations {

        @JsonApiSetRelations
        public void setRelations(Task task, Iterable<Long> ids, String fieldName) {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithEmptyAddRelations {

        @JsonApiAddRelations
        public void addRelations() {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithAddRelations {

        @JsonApiAddRelations
        public void addRelations(Task task, Iterable<Long> ids, String fieldName) {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithEmptyRemoveRelations {

        @JsonApiRemoveRelations
        public void removeRelations() {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithRemoveRelations {

        @JsonApiRemoveRelations
        public void removeRelations(Task task, Iterable<Long> ids, String fieldName) {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithEmptyFindOneTargetRelations {

        @JsonApiFindOneTarget
        public void findOneTarget() {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithFindOneTargetRelations {

        @JsonApiFindOneTarget
        public Project findOneTarget(Long id, String fieldName, QueryParams queryParams, String sth) {
            return new Project()
                .setId(42L);
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithEmptyFindManyTargetsRelations {

        @JsonApiFindManyTargets
        public void findManyTargets() {
        }
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class RelationshipRepositoryWithFindManyTargetsRelations {

        @JsonApiFindManyTargets
        public Iterable<Project> findManyTargets(Long id, String fieldName, QueryParams queryParams, String sth) {
            return Collections.singleton(new Project()
                .setId(42L));
        }
    }
}
