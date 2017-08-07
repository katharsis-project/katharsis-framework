package io.katharsis.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.katharsis.client.KatharsisClient;
import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.HttpAdapterRequest;
import io.katharsis.client.http.HttpAdapterResponse;
import io.katharsis.core.internal.exception.ExceptionMapperLookup;
import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.core.internal.exception.ExceptionMapperRegistryBuilder;
import io.katharsis.core.internal.jackson.JsonApiModuleBuilder;
import io.katharsis.core.internal.utils.JsonApiUrlBuilder;
import io.katharsis.errorhandling.exception.InternalServerErrorException;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.repository.request.HttpMethod;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static io.katharsis.client.internal.AbstractStub.ResponseType.RESOURCE;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AbstractStubTest {
	@Mock
	private KatharsisClient mockKatharsisClient;
	@Mock
	private JsonApiUrlBuilder mockJsonApiUrlBuilder;
	@Mock
	private HttpAdapter mockHttpAdapter;
	@Mock
	private HttpAdapterRequest mockHttpAdapterRequest;
	@Mock
	private HttpAdapterResponse mockHttpAdapterResponse;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		initMocks(this);
	}

	@Test
	public void shouldProcessErrorData_EvenIfContentTypeHasCharSet() throws IOException {
		when(mockKatharsisClient.getHttpAdapter()).thenReturn(mockHttpAdapter);

		when(mockKatharsisClient.getObjectMapper()).thenReturn(objectMapper());
		when(mockKatharsisClient.getExceptionMapperRegistry()).thenReturn(exceptionMapperRegistry);

		when(mockHttpAdapter.newRequest("/test", HttpMethod.GET, null)).thenReturn(mockHttpAdapterRequest);
		when(mockHttpAdapterRequest.execute()).thenReturn(mockHttpAdapterResponse);

		when(mockHttpAdapterResponse.isSuccessful()).thenReturn(false);
		when(mockHttpAdapterResponse.code()).thenReturn(500);
		when(mockHttpAdapterResponse.getResponseHeader("content-type")).thenReturn("application/vnd.api+json; charset=utf-8");
		when(mockHttpAdapterResponse.body()).thenReturn(errorJson());

		AbstractStub abstractStub = new AbstractStub(mockKatharsisClient, mockJsonApiUrlBuilder);
		thrown.expect(InternalServerErrorException.class);
		thrown.expectMessage("An error occurred. Please contact support.");

		abstractStub.executeGet("/test", RESOURCE);
	}

	private ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonApiModuleBuilder moduleBuilder = new JsonApiModuleBuilder();
		SimpleModule jsonApiModule = moduleBuilder.build(null, true);
		objectMapper.registerModule(jsonApiModule);
		return objectMapper;
	}

	private String errorJson() {
		return "{\n" +
				"    \"errors\": [\n" +
				"        {\n" +
				"            \"id\": \"7fc18d09-7b65-4047-9fc6-242f2e72ecb0\",\n" +
				"            \"status\": \"500\",\n" +
				"            \"code\": \"Test-100\",\n" +
				"            \"title\": \"Error Occurred\",\n" +
				"            \"detail\": \"An error occurred. Please contact support.\"\n" +
				"        }\n" +
				"    ]\n" +
				"}";
	}

	private ExceptionMapperRegistry exceptionMapperRegistry = new ExceptionMapperRegistryBuilder().build(new ExceptionMapperLookup() {
		@Override
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			return Collections.EMPTY_SET;
		}
	});
}