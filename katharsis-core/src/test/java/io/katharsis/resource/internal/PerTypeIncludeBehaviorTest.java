package io.katharsis.resource.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.katharsis.core.internal.boot.PropertiesProvider;
import io.katharsis.core.internal.resource.IncludeBehavior;
import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.mock.models.HierarchicalTask;

@RunWith(MockitoJUnitRunner.class)
public class PerTypeIncludeBehaviorTest extends AbstractIncludeBehaviorTest {

	@Override
	protected PropertiesProvider getPropertiesProvider() {
		return new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (key.equals(KatharsisProperties.INCLUDE_BEHAVIOR))
					return IncludeBehavior.PER_TYPE.toString();
				return null;
			}
		};
	}

	@Test
	public void includeParent() throws Exception {
		QuerySpec querySpec = new QuerySpec(HierarchicalTask.class);
		querySpec.includeRelation(Arrays.asList("parent"));

		Document document = mapper.toDocument(toResponse(h11), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		Relationship parentRelationship = taskResource.getRelationships().get("parent");
		assertNotNull(parentRelationship);
		assertNotNull(parentRelationship.getSingleData());
		ResourceIdentifier parentResource = parentRelationship.getSingleData().get();
		assertNotNull(h1.getId().toString(), parentResource.getId());

		List<Resource> included = document.getIncluded();
		assertEquals(2, included.size());
		assertNotNull(h1.getId().toString(), included.get(0).getId());
		assertNotNull(h.getId().toString(), included.get(1).getId());
	}

	@Test
	public void includeParentChildren() throws Exception {
		QuerySpec querySpec = new QuerySpec(HierarchicalTask.class);
		querySpec.includeRelation(Arrays.asList("parent", "children"));

		Document document = mapper.toDocument(toResponse(h11), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		Relationship parentRelationship = taskResource.getRelationships().get("parent");
		assertNotNull(parentRelationship);
		assertNotNull(parentRelationship.getSingleData());
		ResourceIdentifier parentResource = parentRelationship.getSingleData().get();
		assertNotNull(h1.getId().toString(), parentResource.getId());

		List<Resource> included = document.getIncluded();
		// both parent and children recursively included
		assertEquals(3, included.size());
	}

	@Test
	public void includeCyclicParent() throws Exception {
		h.setParent(h1);

		QuerySpec querySpec = new QuerySpec(HierarchicalTask.class);
		querySpec.includeRelation(Arrays.asList("parent"));

		Document document = mapper.toDocument(toResponse(h1), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		Relationship parentRelationship = taskResource.getRelationships().get("parent");
		assertNotNull(parentRelationship);
		assertNotNull(parentRelationship.getSingleData());
		ResourceIdentifier parentResource = parentRelationship.getSingleData().get();
		assertNotNull(h.getId().toString(), parentResource.getId());

		List<Resource> included = document.getIncluded();
		assertEquals(1, included.size());
		assertNotNull(h.getId().toString(), included.get(0).getId());
		Relationship rootParent = included.get(0).getRelationships().get("parent");
		assertTrue(rootParent.getSingleData().isPresent());
		assertNotNull(h1.getId().toString(), rootParent.getSingleData().get().getId());
	}
}
