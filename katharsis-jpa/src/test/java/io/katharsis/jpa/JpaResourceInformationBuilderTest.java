package io.katharsis.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.internal.JpaResourceInformationBuilder;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEmbeddable;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.util.ResourceFieldComparator;
import io.katharsis.resource.field.ResourceAttributesBridge;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;

public class JpaResourceInformationBuilderTest {

	@Test
	public void test()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		JpaResourceInformationBuilder builder = new JpaResourceInformationBuilder(null);

		ResourceInformation info = builder.build(TestEntity.class);
		ResourceField idField = info.getIdField();
		assertNotNull(idField);
		assertEquals("id", idField.getJsonName());
		assertEquals("id", idField.getUnderlyingName());
		assertEquals(Long.class, idField.getType());
		assertEquals(Long.class, idField.getGenericType());

		// TODO fix this in kartharsis
		ResourceAttributesBridge attributeFieldsBridge = info.getAttributeFields();
		Field field = attributeFieldsBridge.getClass().getDeclaredField("staticFields");
		field.setAccessible(true);

		ArrayList<ResourceField> attrFields = new ArrayList<ResourceField>(
				(Collection) field.get(attributeFieldsBridge));
		Collections.sort(attrFields, ResourceFieldComparator.INSTANCE);
		assertEquals(4, attrFields.size());
		ResourceField embField = attrFields.get(0);
		assertEquals(TestEntity.ATTR_embValue, embField.getJsonName());
		assertEquals(TestEntity.ATTR_embValue, embField.getUnderlyingName());
		assertEquals(TestEmbeddable.class, embField.getType());
		assertEquals(TestEmbeddable.class, embField.getGenericType());

		ArrayList<ResourceField> relFields = new ArrayList<ResourceField>(info.getRelationshipFields());
		Collections.sort(relFields, ResourceFieldComparator.INSTANCE);
		assertEquals(3, relFields.size());
		boolean found = false;
		for (ResourceField relField : relFields) {
			if (relField.getUnderlyingName().equals(TestEntity.ATTR_oneRelatedValue)) {
				assertEquals(TestEntity.ATTR_oneRelatedValue, relField.getJsonName());
				assertEquals(RelatedEntity.class, relField.getType());
				assertEquals(RelatedEntity.class, relField.getGenericType());
				found = true;
			}
		}
		Assert.assertTrue(found);
	}

}
