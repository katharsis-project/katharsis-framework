package io.katharsis.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.internal.JpaResourceInformationBuilder;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEmbeddable;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.util.ResourceFieldComparator;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;

public class JpaResourceInformationBuilderTest {

	@Test
	public void test()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Set<Class<? extends Object>> entityClasses = new HashSet<Class<? extends Object>>(
				Arrays.asList(TestEntity.class, RelatedEntity.class));

		JpaResourceInformationBuilder builder = new JpaResourceInformationBuilder(new MetaLookup(), null,
				entityClasses);

		ResourceInformation info = builder.build(TestEntity.class);
		ResourceField idField = info.getIdField();
		assertNotNull(idField);
		assertEquals("id", idField.getJsonName());
		assertEquals("id", idField.getUnderlyingName());
		assertEquals(Long.class, idField.getType());
		assertEquals(Long.class, idField.getGenericType());

		List<ResourceField> attrFields = new ArrayList<>(info.getAttributeFields().getFields());
		Collections.sort(attrFields, ResourceFieldComparator.INSTANCE);
		assertEquals(5, attrFields.size());
		ResourceField embField = attrFields.get(1);
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
