package io.katharsis.resource.field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.resource.ResourceAttributesBridge;
import io.katharsis.core.internal.resource.ResourceFieldImpl;
import io.katharsis.errorhandling.exception.InvalidResourceException;
import io.katharsis.errorhandling.exception.ResourceException;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.mock.models.Task;

public class ResourceAttributesBridgeTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void onValidClassShouldInitializeResourceAttributesBridge() throws Exception {
        // WHEN
        new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), DynamicResource.class);
    }

    @Test(expected = InvalidResourceException.class)
    public void onClassWithoutAnyGetterShouldThrowException() throws Exception {
        // WHEN
        new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), ClassWithoutAnyGetter.class);
    }

    @Test(expected = InvalidResourceException.class)
    public void onClassWithoutAnySetterShouldThrowException() throws Exception {
        // WHEN
        new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), ClassWithoutAnySetter.class);
    }

    @Test
    public void onSimpleAttributesShouldPutInstanceValues() throws Exception {
        // GIVEN
        ResourceField field = new ResourceFieldImpl("name", "name", ResourceFieldType.ATTRIBUTE, String.class, String.class, null);
        ResourceAttributesBridge<Task> sut =
            new ResourceAttributesBridge<>(Collections.singletonList(field), Task.class);
        HashMap<String, JsonNode> attributes = new HashMap<String, JsonNode>();
        attributes.put("name", objectMapper.readTree("\"value\""));
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
            new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), DynamicResource.class);
        HashMap<String, JsonNode> attributes = new HashMap<String, JsonNode>();
        attributes.put("name", objectMapper.readTree("\"value\""));
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
            new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), DynamicResourceWithSetterException.class);
        HashMap<String, JsonNode> attributes = new HashMap<String, JsonNode>();
        attributes.put("name", objectMapper.readTree("\"value\""));

        // WHEN
        sut.setProperties(objectMapper, new DynamicResourceWithSetterException(), attributes);
    }

    @Test(expected = ResourceException.class)
    public void onDynamicAttributesWritingShouldThrowException() throws Exception {
        // GIVEN
        ResourceAttributesBridge<DynamicResourceWithSetterException> sut =
            new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), DynamicResourceWithSetterException.class);
        
        HashMap<String, JsonNode> attributes = new HashMap<String, JsonNode>();
        attributes.put("name", objectMapper.readTree("\"value\""));

        // WHEN
        sut.setProperties(objectMapper, new DynamicResourceWithSetterException(), attributes);
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
