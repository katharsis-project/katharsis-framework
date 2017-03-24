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
import io.katharsis.jpa.model.AnnotationMappedSuperclassEntity;
import io.katharsis.jpa.model.AnnotationTestEntity;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEmbeddable;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.util.ResourceFieldComparator;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.provider.resource.ResourceMetaProvider;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.utils.parser.TypeParser;

public class JpaResourceInformationBuilderTest {

	private JpaResourceInformationBuilder builder;

	private MetaLookup lookup;

	@Before
	public void setup() {
		lookup = new MetaLookup();
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
		Assert.assertTrue(idField.isPostable());
		Assert.assertFalse(idField.isPatchable());
		Assert.assertTrue(idField.isSortable());
		Assert.assertTrue(idField.isFilterable());

		List<ResourceField> attrFields = new ArrayList<ResourceField>(info.getAttributeFields().getFields());
		Collections.sort(attrFields, ResourceFieldComparator.INSTANCE);
		assertEquals(5, attrFields.size());
		ResourceField embField = attrFields.get(1);
		assertEquals(TestEntity.ATTR_embValue, embField.getJsonName());
		assertEquals(TestEntity.ATTR_embValue, embField.getUnderlyingName());
		assertEquals(TestEmbeddable.class, embField.getType());
		assertEquals(TestEmbeddable.class, embField.getGenericType());
		Assert.assertTrue(embField.isPostable());
		Assert.assertTrue(embField.isPatchable());
		Assert.assertTrue(embField.isSortable());
		Assert.assertTrue(embField.isFilterable());

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
	public void testAttributeAnnotations()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ResourceInformation info = builder.build(AnnotationTestEntity.class);

		ResourceField lobField = info.findAttributeFieldByName("lobValue");
		ResourceField fieldAnnotatedField = info.findAttributeFieldByName("fieldAnnotatedValue");
		ResourceField columnAnnotatedField = info.findAttributeFieldByName("columnAnnotatedValue");

		Assert.assertFalse(lobField.isSortable());
		Assert.assertFalse(lobField.isFilterable());
		Assert.assertTrue(lobField.isPostable());
		Assert.assertTrue(lobField.isPatchable());

		Assert.assertFalse(fieldAnnotatedField.isSortable());
		Assert.assertFalse(fieldAnnotatedField.isFilterable());
		Assert.assertTrue(fieldAnnotatedField.isPostable());
		Assert.assertFalse(fieldAnnotatedField.isPatchable());

		Assert.assertTrue(columnAnnotatedField.isSortable());
		Assert.assertTrue(columnAnnotatedField.isFilterable());
		Assert.assertFalse(columnAnnotatedField.isPostable());
		Assert.assertTrue(columnAnnotatedField.isPatchable());

		MetaDataObject meta = lookup.getMeta(AnnotationTestEntity.class).asDataObject();
		Assert.assertTrue(meta.getAttribute("lobValue").isLob());
		Assert.assertFalse(meta.getAttribute("fieldAnnotatedValue").isLob());
	}
	
	@Test
	public void testReadOnlyField()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ResourceInformation info = builder.build(AnnotationTestEntity.class);

		ResourceField field = info.findAttributeFieldByName("readOnlyValue");

		Assert.assertFalse(field.isPostable());
		Assert.assertFalse(field.isPatchable());


		MetaDataObject meta = lookup.getMeta(AnnotationTestEntity.class).asDataObject();
		MetaAttribute attribute = meta.getAttribute("readOnlyValue");

		Assert.assertFalse(attribute.isInsertable());
		Assert.assertFalse(attribute.isUpdatable());
	}

	@Test
	public void testMappedSuperclass()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ResourceInformation info = builder.build(AnnotationMappedSuperclassEntity.class);
		
		Assert.assertNull(info.getResourceType());

		ResourceField lobField = info.findAttributeFieldByName("lobValue");
		ResourceField fieldAnnotatedField = info.findAttributeFieldByName("fieldAnnotatedValue");
		ResourceField columnAnnotatedField = info.findAttributeFieldByName("columnAnnotatedValue");

		Assert.assertFalse(lobField.isSortable());
		Assert.assertFalse(lobField.isFilterable());
		Assert.assertTrue(lobField.isPostable());
		Assert.assertTrue(lobField.isPatchable());

		Assert.assertFalse(fieldAnnotatedField.isSortable());
		Assert.assertFalse(fieldAnnotatedField.isFilterable());
		Assert.assertTrue(fieldAnnotatedField.isPostable());
		Assert.assertFalse(fieldAnnotatedField.isPatchable());

		Assert.assertTrue(columnAnnotatedField.isSortable());
		Assert.assertTrue(columnAnnotatedField.isFilterable());
		Assert.assertFalse(columnAnnotatedField.isPostable());
		Assert.assertTrue(columnAnnotatedField.isPatchable());

		MetaDataObject meta = lookup.getMeta(AnnotationMappedSuperclassEntity.class).asDataObject();
		Assert.assertTrue(meta.getAttribute("lobValue").isLob());
		Assert.assertFalse(meta.getAttribute("fieldAnnotatedValue").isLob());
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
