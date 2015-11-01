package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.adapter.RepositoryAdapter;
import io.katharsis.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.repository.annotations.*;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.repository.exception.RepositoryMethodException;
import io.katharsis.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class RepositoryAdapterTest {
    private QueryParams queryParams;
    private ParametersFactory parameterFactory;

    @Before
    public void setUp() throws Exception {
        queryParams = new QueryParams();
        parameterFactory = new ParametersFactory(new NewInstanceRepositoryMethodParameterProvider());
    }

    @Test
    public void onClassWithNoGetLinksInformationShouldReturnFalse() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repository = new ResourceRepositoryWithoutAnyMethods();
        SimpleRepositoryAdapter sut = new SimpleRepositoryAdapter(repository, parameterFactory);

        // WHEN
        boolean result = sut.linksRepositoryAvailable();

        // THEN
        assertThat(result).isFalse();
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithNoGetLinksInformationShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repository = new ResourceRepositoryWithoutAnyMethods();
        SimpleRepositoryAdapter sut = new SimpleRepositoryAdapter(repository, parameterFactory);

        // WHEN
        sut.getLinksInformation(Collections.singletonList(new Project()), queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidGetLinksInformationShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptyGetLinksInformation repo = new ResourceRepositoryWithEmptyGetLinksInformation();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterFactory);

        // WHEN
        sut.getLinksInformation(Collections.singletonList(new Project()), queryParams);
    }

    @Test
    public void onClassWithGetLinksInformationShouldReturnTrue() throws Exception {
        // GIVEN
        ResourceRepositoryWithGetLinksInformation repo = spy(ResourceRepositoryWithGetLinksInformation.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterFactory);

        // WHEN
        boolean result = sut.linksRepositoryAvailable();

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onClassWithGetLinksInformationShouldInvokeMethod() throws Exception {
        // GIVEN
        ResourceRepositoryWithGetLinksInformation repo = spy(ResourceRepositoryWithGetLinksInformation.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterFactory);
        List<Project> resources = Collections.singletonList(new Project());

        // WHEN
        sut.getLinksInformation(resources, queryParams);

        // THEN
        verify(repo).getLinksInformation(resources, queryParams, "");
    }

    @Test
    public void onClassWithNoGetMetaInformationShouldReturnFalse() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repository = new ResourceRepositoryWithoutAnyMethods();
        SimpleRepositoryAdapter sut = new SimpleRepositoryAdapter(repository, parameterFactory);

        // WHEN
        boolean result = sut.metaRepositoryAvailable();

        // THEN
        assertThat(result).isFalse();
    }

    @Test(expected = RepositoryAnnotationNotFoundException.class)
    public void onClassWithNoGetMetaInformationShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithoutAnyMethods repository = new ResourceRepositoryWithoutAnyMethods();
        SimpleRepositoryAdapter sut = new SimpleRepositoryAdapter(repository, parameterFactory);

        // WHEN
        sut.getMetaInformation(Collections.singletonList(new Project()), queryParams);
    }

    @Test(expected = RepositoryMethodException.class)
    public void onClassWithInvalidGetMetaInformationShouldThrowException() throws Exception {
        // GIVEN
        ResourceRepositoryWithEmptyGetMetaInformation repo = new ResourceRepositoryWithEmptyGetMetaInformation();
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterFactory);

        // WHEN
        sut.getMetaInformation(Collections.singletonList(new Project()), queryParams);
    }

    @Test
    public void onClassWithGetMetaInformationShouldReturnTrue() throws Exception {
        // GIVEN
        ResourceRepositoryWithGetMetaInformation repo = spy(ResourceRepositoryWithGetMetaInformation.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterFactory);

        // WHEN
        boolean result = sut.metaRepositoryAvailable();

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onClassWithGetMetaInformationShouldInvokeMethod() throws Exception {
        // GIVEN
        ResourceRepositoryWithGetMetaInformation repo = spy(ResourceRepositoryWithGetMetaInformation.class);
        ResourceRepositoryAdapter<Project, Long> sut = new ResourceRepositoryAdapter<>(repo, parameterFactory);
        List<Project> resources = Collections.singletonList(new Project());

        // WHEN
        sut.getMetaInformation(resources, queryParams);

        // THEN
        verify(repo).getMetaInformation(resources, queryParams, "");
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithoutAnyMethods {
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptyGetLinksInformation {

        @JsonApiLinks
        public LinksData getLinksInformation() {
            return new LinksData();
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithGetLinksInformation {

        @JsonApiLinks
        public LinksData getLinksInformation(Iterable<Project> entities, QueryParams queryParams, String someString) {
            return new LinksData();
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithEmptyGetMetaInformation {

        @JsonApiMeta
        public MetaData getMetaInformation() {
            return new MetaData();
        }
    }

    @JsonApiResourceRepository(Project.class)
    public static class ResourceRepositoryWithGetMetaInformation {

        @JsonApiMeta
        public MetaData getMetaInformation(Iterable<Project> entities, QueryParams queryParams, String someString) {
            return new MetaData();
        }
    }

    public static class SimpleRepositoryAdapter extends RepositoryAdapter {
        public SimpleRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
            super(implementationObject, parametersFactory);
        }
    }

    public static class LinksData implements LinksInformation {
    }

    public static class MetaData implements MetaInformation {
    }
}
