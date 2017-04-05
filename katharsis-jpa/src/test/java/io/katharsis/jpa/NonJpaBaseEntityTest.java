package io.katharsis.jpa;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.model.NonJpaChildEntity;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.ResourceList;

public class NonJpaBaseEntityTest extends AbstractJpaJerseyTest {

	@Test
	public void testEntityDerivedFromNonJpaEntityProperlyWorking()
			throws InstantiationException, IllegalAccessException {
		ResourceRepositoryV2<NonJpaChildEntity, Serializable> repo = client
				.getRepositoryForType(NonJpaChildEntity.class);
		NonJpaChildEntity test = new NonJpaChildEntity();
		test.setId(2);
		test.setIntValue(13);
		test.setNonJpaValue("nonJpa");
		repo.create(test);

		ResourceList<NonJpaChildEntity> list = repo.findAll(new QuerySpec(NonJpaChildEntity.class));
		Assert.assertEquals(1, list.size());
		NonJpaChildEntity persistedEntity = list.get(0);
		Assert.assertEquals(2, persistedEntity.getId());
		Assert.assertEquals(13, persistedEntity.getIntValue());
		Assert.assertNull(persistedEntity.getNonJpaValue());
	}
}
