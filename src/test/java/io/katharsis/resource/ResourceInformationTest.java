package io.katharsis.resource;

import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Task;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceInformationTest {

    @Test
    public void onRelationshipFieldSearchShouldReturnExistingField() throws NoSuchFieldException {
        // GIVEN
        Field field = String.class.getDeclaredField("value");
        ResourceField resourceField = new ResourceField("value", "value", field.getType(), field.getGenericType());
        ResourceInformation sut = new ResourceInformation(Task.class, null, null, Collections.singleton(resourceField));

        // WHEN
        ResourceField result = sut.findRelationshipFieldByName("value");

        // THEN
        assertThat(result.getUnderlyingName()).isEqualTo(field.getName());
    }
}
