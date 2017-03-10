package io.katharsis.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Test;

import io.katharsis.core.internal.resource.ResourceFieldImpl;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.utils.parser.TypeParser;

public class ResourceInformationTest {

	@Test
	public void onRelationshipFieldSearchShouldReturnExistingField() throws NoSuchFieldException {
		// GIVEN
		Field field = String.class.getDeclaredField("value");
		ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, field.getType(), field.getGenericType(), null);
		ResourceField resourceField = new ResourceFieldImpl("value", "value", ResourceFieldType.RELATIONSHIP, field.getType(), field.getGenericType(), "projects");
		TypeParser typeParser = new TypeParser();
		ResourceInformation sut = new ResourceInformation(typeParser, Task.class, "tasks", Arrays.asList(idField, resourceField));

		// WHEN
		ResourceField result = sut.findRelationshipFieldByName("value");

		// THEN
		assertThat(result.getUnderlyingName()).isEqualTo(field.getName());
	}
}
