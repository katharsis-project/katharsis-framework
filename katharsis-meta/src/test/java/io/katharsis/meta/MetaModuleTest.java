package io.katharsis.meta;

import java.io.Serializable;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaPrimitiveType;
import io.katharsis.meta.model.resource.MetaResource;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.ResourceList;

public class MetaModuleTest extends AbstractMetaJerseyTest {

	private ResourceRepositoryV2<MetaElement, Serializable> repository;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		repository = client.getQuerySpecRepository(MetaElement.class);
	}

	@Test
	public void testFindAll() {
		ResourceList<MetaElement> list = repository.findAll(new QuerySpec(MetaElement.class));
		Assert.assertFalse(list.isEmpty());
	}

	@Test
	public void testIdPrefix() {
		ResourceList<MetaElement> list = repository.findAll(new QuerySpec(MetaElement.class));
		Assert.assertFalse(list.isEmpty());
		for (MetaElement elem : list) {
			if (elem instanceof MetaPrimitiveType) {
				Assert.assertTrue(elem.getId(), elem.getId().startsWith("base."));
			}
			else {
				Assert.assertTrue(elem.getId(), elem.getId().startsWith("app.resources.")
						|| elem.getId().startsWith("io.katharsis.meta.")
						|| elem.getId().startsWith("io.katharsis.resource.")
						|| elem.getId().startsWith("io.katharsis.jpa."));
			}
		}
	}

	@Test
	public void testAttributesHaveParent() {
		QuerySpec querySpec = new QuerySpec(MetaAttribute.class);
		querySpec.includeRelation(Arrays.asList("parent"));
		ResourceList<MetaAttribute> list = client.getQuerySpecRepository(MetaAttribute.class).findAll(querySpec);
		Assert.assertFalse(list.isEmpty());
		for (MetaAttribute elem : list) {
			Assert.assertNotNull(elem.getParent());
		}
	}

	@Test
	public void testAttributesHaveType() {
		QuerySpec querySpec = new QuerySpec(MetaAttribute.class);
		querySpec.includeRelation(Arrays.asList("type", "elementType"));
		ResourceList<MetaAttribute> list = client.getQuerySpecRepository(MetaAttribute.class).findAll(querySpec);
		Assert.assertFalse(list.isEmpty());
		for (MetaAttribute elem : list) {
			Assert.assertNotNull(elem.getType());
			Assert.assertNotNull(elem.getType().getElementType());
		}
	}

	@Test
	public void testGetResource() {
		testResource(false);
	}

	@Test
	public void testGetResourceAsMetaElement() {
		testResource(true);
	}

	public void testResource(boolean accessAsMetaElement) {
		Class<? extends MetaElement> elementClass = accessAsMetaElement ? MetaElement.class : MetaResource.class;
		QuerySpec querySpec = new QuerySpec(elementClass);
		querySpec.includeRelation(Arrays.asList("attributes", "type"));
		querySpec.includeRelation(Arrays.asList("declaredAttributes"));
		querySpec.includeRelation(Arrays.asList("primaryKey", "elements"));
		querySpec.includeRelation(Arrays.asList("superType"));
		String id = "app.resources.Schedule";
		MetaResource resource = (MetaResource) client.getQuerySpecRepository(elementClass).findOne(id, querySpec);
		Assert.assertNotNull(resource);
		Assert.assertNotNull(resource.getAttributes());
		Assert.assertNotNull(resource.getDeclaredAttributes());
		Assert.assertNotNull(resource.getPrimaryKey());
		Assert.assertNull(resource.getSuperType());
		Assert.assertEquals(1, resource.getPrimaryKey().getElements().size());

		MetaAttribute idAttr = resource.getAttribute("id");
		Assert.assertEquals("id", idAttr.getName());
		Assert.assertNotNull(idAttr.getType());
		Assert.assertTrue(idAttr.getType() instanceof MetaPrimitiveType);
		Assert.assertFalse(idAttr.isAssociation());
	}

	public void testIdNaming() {
		QuerySpec querySpec = new QuerySpec(MetaElement.class);
		Assert.assertNotNull(repository.findOne("io.katharsis.meta.metaElement", querySpec));
		Assert.assertNotNull(repository.findOne("io.katharsis.meta.metaElement$List", querySpec));
		Assert.assertNotNull(repository.findOne("io.katharsis.meta.metaAttribute", querySpec));
		Assert.assertNotNull(repository.findOne("io.katharsis.meta.metaType", querySpec));
		Assert.assertNotNull(repository.findOne("io.katharsis.jpa.metaEmbeddableAttribute.laz", querySpec));
		Assert.assertNotNull(repository.findOne("io.katharsis.meta.metaType$primaryKey", querySpec));
	}
}
