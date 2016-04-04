package io.katharsis.resource.field;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.init.InvalidResourceException;
import io.katharsis.resource.mock.models.Task;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class ResourceAttributesBridgeTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void onValidClassShouldInitializeResourceAttributesBridge() throws Exception {
        // WHEN
        new ResourceAttributesBridge<>(Collections.<ResourceField>emptySet(), DynamicResource.class);
    }

    @Test(expected = InvalidResourceException.class)
    public void onClassWithoutAnyGetterShouldThrowException() throws Exception {
        // WHEN
        new ResourceAttributesBridge<>(Collections.<ResourceField>emptySet(), ClassWithoutAnyGetter.class);
    }

    @Test(expected = InvalidResourceException.class)
    public void onClassWithoutAnySetterShouldThrowException() throws Exception {
        // WHEN
        new ResourceAttributesBridge<>(Collections.<ResourceField>emptySet(), ClassWithoutAnySetter.class);
    }

    @Test
    public void onSimpleAttributesShouldPutInstanceValues() throws Exception {
        // GIVEN
        ResourceField field = new ResourceField("name", "name", String.class, String.class);
        ResourceAttributesBridge<Task> sut =
            new ResourceAttributesBridge<>(Collections.singleton(field), Task.class);
        JsonNode attributes = objectMapper.createObjectNode()
            .put("name", "value");
        Task task = new Task();

        // WHEN
        sut.setProperties(objectMapper, task, attributes);

        // THEN
        assertThat(task.getName()).isEqualTo("value");
    }

    @Test
    public void onDynamicAttributesShouldPutInstanceValues() throws Exception {
        // GIVEN
        ResourceAttributesBridge<DynamicResource> sut =
            new ResourceAttributesBridge<>(Collections.<ResourceField>emptySet(), DynamicResource.class);
        JsonNode attributes = objectMapper.createObjectNode()
            .put("name", "value");
        DynamicResource resource = new DynamicResource();

        // WHEN
        sut.setProperties(objectMapper, resource, attributes);

        // THEN
        assertThat(resource.anyGetter())
            .containsOnly(entry("name", "value"));
    }

    @Test(expected = ResourceException.class)
    public void onDynamicAttributesReadingShouldThrowException() throws Exception {
        // GIVEN
        ResourceAttributesBridge<DynamicResourceWithSetterException> sut =
            new ResourceAttributesBridge<>(Collections.<ResourceField>emptySet(), DynamicResourceWithSetterException.class);
        JsonNode attributes = objectMapper.createObjectNode()
            .put("name", "value");

        // WHEN
        sut.setProperties(objectMapper, new DynamicResourceWithSetterException(), attributes);
    }

    @Test(expected = ResourceException.class)
    public void onDynamicAttributesWritingShouldThrowException() throws Exception {
        // GIVEN
        ResourceAttributesBridge<DynamicResourceWithGetterException> sut =
            new ResourceAttributesBridge<>(Collections.<ResourceField>emptySet(), DynamicResourceWithGetterException.class);
        JsonNode attributes = objectMapper.createObjectNode()
            .put("name", "value");

        // WHEN
        sut.setProperties(objectMapper, new DynamicResourceWithGetterException(), attributes);
    }

    public static class DynamicResource {

        private Map<String, Object> values = new HashMap<>(1);

        @JsonAnyGetter
        public Map<String, Object> anyGetter() {
            return values;
        }

        @JsonAnySetter
        public void anySetter(String name, Object value) {
            values.put(name, value);
        }
    }

    public static class DynamicResourceWithSetterException {

        @JsonAnyGetter
        public Map<String, Object> anyGetter() {
            return Collections.emptyMap();
        }

        @JsonAnySetter
        public void anySetter(String name, Object value) {
            throw new IllegalStateException();
        }
    }

    public static class DynamicResourceWithGetterException {

        @JsonAnyGetter
        public Map<String, Object> anyGetter() {
            throw new IllegalStateException();
        }

        @JsonAnySetter
        public void anySetter(String name, Object value) {
        }
    }

    private static class ClassWithoutAnyGetter {
        @JsonAnySetter
        public void anySetter(String name, Object value) {
        }
    }

    private static class ClassWithoutAnySetter {
        @JsonAnyGetter
        public Map<String, Object> anyGetter() {
            return Collections.emptyMap();
        }
    }
}
