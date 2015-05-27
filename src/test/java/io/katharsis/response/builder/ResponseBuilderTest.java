package io.katharsis.response.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.resource.mock.models.Task;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class ResponseBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ResponseBuilder sut;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        sut = new ResponseBuilder();
    }

    @Test
    public void onNullSingleResourceShouldReturnNullData() throws Exception {
        // WHEN
        Object result = sut.buildResponse(null, Task.class, false);
        String resultJson = objectMapper.writeValueAsString(result);

        // THEN
        assertThatJson(resultJson).node("data").isEqualTo(null);
    }

    @Test
    public void onNullCollectionResourceShouldReturnEmptyData() throws Exception {
        // WHEN
        Object result = sut.buildResponse(null, Task.class, true);
        String resultJson = objectMapper.writeValueAsString(result);

        // THEN
        assertThatJson(resultJson).node("data").isArray().ofLength(0);
    }

    @Test
    public void onEmptyCollectionResourceShouldReturnEmptyData() throws Exception {
        // WHEN
        Object result = sut.buildResponse(Collections.emptyList(), Task.class, true);
        String resultJson = objectMapper.writeValueAsString(result);

        // THEN
        assertThatJson(resultJson).node("data").isArray().ofLength(0);
    }

    @Test
    public void onNonCollectionResourceWronglyMarkedShouldThrowException() throws Exception {
        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.buildResponse("invalid resource", Task.class, true);
    }
}
