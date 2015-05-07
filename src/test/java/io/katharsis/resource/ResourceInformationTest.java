package io.katharsis.resource;

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
        ResourceInformation sut = new ResourceInformation(Task.class, null, null, Collections.singleton(field));

        // WHEN
        Field result = sut.findRelationshipFieldByName("value");

        // THEN
        assertThat(result).isEqualTo(field);
    }
}
