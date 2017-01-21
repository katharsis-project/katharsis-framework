package io.katharsis.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Test;

import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceField.ResourceFieldType;
import io.katharsis.resource.mock.models.Task;

public class ResourceInformationTest {

    @Test
    public void onRelationshipFieldSearchShouldReturnExistingField() throws NoSuchFieldException {
        // GIVEN
        Field field = String.class.getDeclaredField("value");
        ResourceField idField = new ResourceField("id", "id", ResourceFieldType.ID, field.getType(), field.getGenericType());
        ResourceField resourceField = new ResourceField("value", "value", ResourceFieldType.RELATIONSHIP, field.getType(), field.getGenericType());
		ResourceInformation sut = new ResourceInformation(Task.class, "tasks", Arrays.asList(idField, resourceField));

        // WHEN
        ResourceField result = sut.findRelationshipFieldByName("value");

        // THEN
        assertThat(result.getUnderlyingName()).isEqualTo(field.getName());
    }
}
