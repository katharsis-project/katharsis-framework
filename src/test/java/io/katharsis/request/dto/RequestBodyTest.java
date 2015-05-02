package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class RequestBodyTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void onSimplePostDataShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"name\": \"asdasd\", \"links\": {\"project\": {\"type\": " +
                "\"projects\", \"id\": \"123\"}}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        result.getData();
    }
}
