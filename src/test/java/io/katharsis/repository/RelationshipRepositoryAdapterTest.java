package io.katharsis.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import org.junit.Before;

public class RelationshipRepositoryAdapterTest {
    private RequestParams requestParams;
    private ParametersFactory parameterProvider;

    @Before
    public void setUp() throws Exception {
        requestParams = new RequestParams(null);
        parameterProvider = new ParametersFactory(new NewInstanceRepositoryMethodParameterProvider());
    }

    @JsonApiRelationshipRepository(source = Task.class, target = Project.class)
    public static class ResourceRepositoryWithoutAnyMethods {
    }
}
