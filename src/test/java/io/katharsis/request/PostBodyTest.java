package io.katharsis.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class PostBodyTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void onSimplePostDataShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"name\": \"asdasd\", \"links\": {\"project\": {\"type\": " +
                "\"projects\", \"id\": \"123\"}}}}";

        // WHEN
        PostBody result = objectMapper.readValue(body, PostBody.class);

        // THEN
        result.getData();
    }
}
