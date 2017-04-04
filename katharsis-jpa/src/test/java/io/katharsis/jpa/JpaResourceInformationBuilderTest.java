package io.katharsis.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.katharsis.jpa.internal.JpaResourceInformationBuilder;
import io.katharsis.jpa.merge.MergedResource;
import io.katharsis.jpa.meta.JpaMetaProvider;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEmbeddable;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.util.ResourceFieldComparator;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.provider.resource.ResourceMetaProvider;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.utils.parser.TypeParser;

public class JpaResourceInformationBuilderTest {

	private JpaResourceInformationBuilder builder;

	@Before
	public void setup() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.addProvider(new ResourceMetaProvider());
		builder = new JpaResourceInformationBuilder(lookup);
		builder.init(new DefaultResourceInformationBuilderContext(builder, new TypeParser()));
	}

	@Test
	public void test()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		ResourceInformation info = builder.build(TestEntity.class);
		ResourceField idField = info.getIdField();
		assertNotNull(idField);
		assertEquals("id", idField.getJsonName());
		assertEquals("id", idField.getUnderlyingName());
		assertEquals(Long.class, idField.getType());
		assertEquals(Long.class, idField.getGenericType());

		List<ResourceField> attrFields = new ArrayList<ResourceField>(info.getAttributeFields().getFields());
		Collections.sort(attrFields, ResourceFieldComparator.INSTANCE);
		assertEquals(5, attrFields.size());
		ResourceField embField = attrFields.get(1);
		assertEquals(TestEntity.ATTR_embValue, embField.getJsonName());
		assertEquals(TestEntity.ATTR_embValue, embField.getUnderlyingName());
		assertEquals(TestEmbeddable.class, embField.getType());
		assertEquals(TestEmbeddable.class, embField.getGenericType());

		ArrayList<ResourceField> relFields = new ArrayList<ResourceField>(info.getRelationshipFields());
		Collections.sort(relFields, ResourceFieldComparator.INSTANCE);
		assertEquals(4, relFields.size());
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

	@Test
	@Ignore
	public void mergeRelationsAnnotation() {
		Assert.assertTrue(builder.accept(MergedResource.class));

		ResourceInformation info = builder.build(MergedResource.class);
		Assert.assertEquals("merged", info.getResourceType());
		Assert.assertEquals(MergedResource.class, info.getResourceClass());
		Assert.assertNull(info.findRelationshipFieldByName("oneRelatedValue"));
		Assert.assertNull(info.findRelationshipFieldByName("manyRelatedValues"));
		Assert.assertNotNull(info.findAttributeFieldByName("oneRelatedValue"));
		Assert.assertNotNull(info.findAttributeFieldByName("manyRelatedValues"));
	}
}
