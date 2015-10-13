package io.katharsis.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.annotations.Delete;
import io.katharsis.repository.annotations.FindAll;
import io.katharsis.repository.annotations.FindOne;
import io.katharsis.repository.annotations.Save;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.repository.exception.RepositoryMethodException;
import io.katharsis.resource.mock.models.Project;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Parameter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ResourceRepositoryFacadeTest {
    private RequestParams requestParams;
    private NewInstanceResourceMethodParameterProvider parameterProvider;

    @Before
    public void setUp() throws Exception {
        requestParams = new RequestParams(null);
        parameterProvider = new NewInstanceResourceMethodParameterProvider();
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutFindOneShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repo = new ResourceRepositoryWithoutAnyMethods();
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        sut.findOne(1L, requestParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidFindOneShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptyFindOne repo = new ResourceRepositoryWithEmptyFindOne();
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        sut.findOne(1L, requestParams);
    }

    @Test
    public void onClassWithFindOneShouldReturnValue() throws Exception {
        // GIVEN
        ResourceRepositoryWithFindOne repo = spy(ResourceRepositoryWithFindOne.class);
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        Project result = sut.findOne(1L, requestParams);

        // THEN
        verify(repo).findOne(eq(1L), eq(requestParams), eq(""));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutFindAllShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repo = new ResourceRepositoryWithoutAnyMethods();
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        sut.findAll(requestParams);
    }

    @Test
    public void onClassWithFindAllShouldReturnValue() throws Exception {
        // GIVEN
        ResourceRepositoryWithFindAll repo = spy(ResourceRepositoryWithFindAll.class);
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        Iterable<Project> result = sut.findAll(requestParams);

        // THEN
        verify(repo).findAll(eq(requestParams), eq(""));
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next()).isNotNull();
        assertThat(result.iterator().next().getId()).isEqualTo(1L);
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithoutSaveShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repo = new ResourceRepositoryWithoutAnyMethods();
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        sut.save(new Project());
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidSaveShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptySave repo = new ResourceRepositoryWithEmptySave();
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        sut.save(new Project());
    }

    @Test
    public void onClassWithSaveShouldReturnValue() throws Exception {
        // GIVEN
        ResourceRepositoryWithSave repo = spy(ResourceRepositoryWithSave.class);
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

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
        
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        sut.delete(1L);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidDeleteShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptyDelete repo = new ResourceRepositoryWithEmptyDelete();
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        sut.delete(1L);
    }

    @Test
    public void onClassWithDeleteShouldInvokeMethod() throws Exception {
        // GIVEN
        ResourceRepositoryWithDelete repo = spy(ResourceRepositoryWithDelete.class);
        ResourceRepositoryFacade<Project, Long> sut = new ResourceRepositoryFacade<>(repo, parameterProvider);

        // WHEN
        sut.delete(1L);

        // THEN
        verify(repo).delete(eq(1L), eq(""));
    }

    @io.katharsis.repository.annotations.ResourceRepository(Project.class)
    public static class ResourceRepositoryWithoutAnyMethods {
    }

    @io.katharsis.repository.annotations.ResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptyFindOne {

        @FindOne
        public Project findOne() {
            return new Project();
        }
    }

    @io.katharsis.repository.annotations.ResourceRepository(Project.class)
    public static class ResourceRepositoryWithFindOne {

        @FindOne
        public Project findOne(Long id, RequestParams requestParams, String someString) {
            return new Project()
                .setId(id);
        }
    }

    @io.katharsis.repository.annotations.ResourceRepository(Project.class)
    public static class ResourceRepositoryWithFindAll {

        @FindAll
        public Iterable<Project> findAll(RequestParams requestParams, String s) {
            return Collections.singletonList(new Project().setId(1L));
        }
    }

    @io.katharsis.repository.annotations.ResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptySave {

        @Save
        public Project save() {
            return new Project();
        }
    }

    @io.katharsis.repository.annotations.ResourceRepository(Project.class)
    public static class ResourceRepositoryWithSave {

        @Save
        public Project save(Project project, String s) {
            return project
                .setId(1L);
        }
    }

    @io.katharsis.repository.annotations.ResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptyDelete {

        @Delete
        public void delete() {
        }
    }

    @io.katharsis.repository.annotations.ResourceRepository(Project.class)
    public static class ResourceRepositoryWithDelete {

        @Delete
        public void delete(Long id, String s) {
        }
    }

    public static class NewInstanceResourceMethodParameterProvider implements ResourceMethodParameterProvider {

        @Override
        public <T> T provide(Parameter parameter) {
            try {
                return (T) parameter.getType().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
