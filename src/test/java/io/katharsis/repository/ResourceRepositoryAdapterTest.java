package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.repository.annotations.*;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.repository.exception.RepositoryMethodException;
import io.katharsis.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.katharsis.resource.mock.models.Project;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ResourceRepositoryAdapterTest {
    private QueryParams queryParams;
    private ParametersFactory parameterProvider;

    @Before
    public void setUp() throws Exception {
        queryParams = new QueryParams();
        parameterProvider = new ParametersFactory(new NewInstanceRepositoryMethodParameterProvider());
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutFindOneShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repo = new ResourceRepositoryWithoutAnyMethods();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findOne(1L, queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidFindOneShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptyFindOne repo = new ResourceRepositoryWithEmptyFindOne();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findOne(1L, queryParams);
    }

    @Test
    public void onClassWithFindOneShouldReturnValue() throws Exception {
        // GIVEN
        ResourceRepositoryWithFindOne repo = spy(ResourceRepositoryWithFindOne.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        Project result = sut.findOne(1L, queryParams);

        // THEN
        verify(repo).findOne(eq(1L), eq(queryParams), eq(""));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutFindAllShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repo = new ResourceRepositoryWithoutAnyMethods();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findAll(queryParams);
    }

    @Test
    public void onClassWithFindAllShouldReturnValue() throws Exception {
        // GIVEN
        ResourceRepositoryWithFindAll repo = spy(ResourceRepositoryWithFindAll.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        Iterable<Project> result = sut.findAll(queryParams);

        // THEN
        verify(repo).findAll(eq(queryParams), eq(""));
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next()).isNotNull();
        assertThat(result.iterator().next().getId()).isEqualTo(1L);
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutFindAllWithIdsShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repo = new ResourceRepositoryWithoutAnyMethods();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findAll(Collections.singletonList(1L), queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidFindAllWithIdsShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptyFindAllWithIds repo = new ResourceRepositoryWithEmptyFindAllWithIds();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.findAll(Collections.singletonList(1L), queryParams);
    }

    @Test
    public void onClassWithFindAllWithIdsShouldReturnValue() throws Exception {
        // GIVEN
        ResourceRepositoryWithFindAllWithIds repo = spy(ResourceRepositoryWithFindAllWithIds.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);
        List<Long> ids = Collections.singletonList(1L);

        // WHEN
        Iterable<Project> result = sut.findAll(ids, queryParams);

        // THEN
        verify(repo).findAll(eq(ids), eq(queryParams), eq(""));
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo(1L);
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutSaveShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repo = new ResourceRepositoryWithoutAnyMethods();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.save(new Project());
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidSaveShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptySave repo = new ResourceRepositoryWithEmptySave();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.save(new Project());
    }

    @Test
    public void onClassWithSaveShouldReturnValue() throws Exception {
        // GIVEN
        ResourceRepositoryWithSave repo = spy(ResourceRepositoryWithSave.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        Project entity = new Project();
        Project result = sut.save(entity);

        // THEN
        verify(repo).save(eq(entity), eq(""));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutDeleteShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repo = new ResourceRepositoryWithoutAnyMethods();
        
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.delete(1L);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidDeleteShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptyDelete repo = new ResourceRepositoryWithEmptyDelete();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.delete(1L);
    }

    @Test
    public void onClassWithDeleteShouldInvokeMethod() throws Exception {
        // GIVEN
        ResourceRepositoryWithDelete repo = spy(ResourceRepositoryWithDelete.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterProvider);

        // WHEN
        sut.delete(1L);

        // THEN
        verify(repo).delete(eq(1L), eq(""));
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithoutAnyMethods {
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptyFindOne {

        @JsonApiFindOne
        public Project findOne() {
            return new Project();
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithFindOne {

        @JsonApiFindOne
        public Project findOne(Long id, QueryParams queryParams, String someString) {
            return new Project()
                .setId(id);
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithFindAll {

        @JsonApiFindAll
        public Iterable<Project> findAll(QueryParams queryParams, String s) {
            return Collections.singletonList(new Project().setId(1L));
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptyFindAllWithIds {

        @JsonApiFindAllWithIds
        public Iterable<Project> findAll() {
            return Collections.singletonList(new Project().setId(1L));
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithFindAllWithIds {

        @JsonApiFindAllWithIds
        public Iterable<Project> findAll(Iterable<Long> id, QueryParams queryParams, String someString) {
            return Collections.singletonList(new Project().setId(1L));
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptySave {

        @JsonApiSave
        public Project save() {
            return new Project();
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithSave {

        @JsonApiSave
        public Project save(Project project, String s) {
            return project
                .setId(1L);
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptyDelete {

        @JsonApiDelete
        public void delete() {
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithDelete {

        @JsonApiDelete
        public void delete(Long id, String s) {
        }
    }
}
