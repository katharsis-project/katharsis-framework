package io.katharsis.dispatcher.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.katharsis.dispatcher.registry.annotated.ParametersFactory;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.NewInstanceRepositoryFactory;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.repository.mock.NewInstanceRepositoryParameterProvider;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.utils.parser.TypeParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public abstract class BaseControllerTest {
    protected static final QueryParams REQUEST_PARAMS = new QueryParams();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    protected ObjectMapper objectMapper;
    protected ResourceRegistry resourceRegistry;
    protected TypeParser typeParser;
    protected IncludeLookupSetter includeFieldSetter;
    protected RepositoryParameterProvider parameterProvider;
    protected QueryParamsBuilder queryParamsBuilder;
    ParametersFactory parametersFactory = new ParametersFactory();

    @Before
    public void prepare() {
        ResourceInformationBuilder resourceInformationBuilder = new ResourceInformationBuilder(
                new ResourceFieldNameTransformer());
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(
                new NewInstanceRepositoryFactory(parametersFactory), resourceInformationBuilder);
        resourceRegistry = registryBuilder
                .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);
        typeParser = new TypeParser();
        includeFieldSetter = new IncludeLookupSetter(resourceRegistry);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JsonApiModuleBuilder().build(resourceRegistry));

        parameterProvider = new NewInstanceRepositoryParameterProvider();
        queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
    }


    protected InputStream serialize(Object object) {
        try {
            return new ByteArrayInputStream(objectMapper.writeValueAsString(object).getBytes());
        } catch (JsonProcessingException e) {
            throw Throwables.propagate(e);
        }
    }

}
