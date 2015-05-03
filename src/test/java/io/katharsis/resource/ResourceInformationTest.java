package io.katharsis.resource;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceInformationTest {

    @Test
    public void onRelationshipFieldSearchShouldReturnExistingField() throws NoSuchFieldException {
        // GIVEN
        ResourceInformation sut = new ResourceInformation();
        Field field = String.class.getDeclaredField("value");
        sut.setRelationshipFields(Collections.singleton(field));

        // WHEN
        Field result = sut.findRelationshipFieldByName("value");

        // THEN
        assertThat(result).isEqualTo(field);
    }
}
