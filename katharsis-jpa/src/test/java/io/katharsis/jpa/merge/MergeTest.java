package io.katharsis.jpa.merge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.jpa.AbstractJpaJerseyTest;
import io.katharsis.jpa.JpaModule;
import io.katharsis.jpa.JpaRepositoryConfig;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.util.SpringTransactionRunner;

/**
 * Example of how to merge an entity with several related entities into a single resource to achieve atomic updates.
 */
public class MergeTest extends AbstractJpaJerseyTest {

	private QuerySpecResourceRepositoryStub<MergedResource, Long> repo;

	private EntityManager entityManager;

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = client.getQuerySpecRepository(MergedResource.class);
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		super.setupModule(module, server);

		if (server) {
			entityManager = module.getEntityManager();

			MergedResourceMapper mapper = new MergedResourceMapper(entityManager);
			module.addRepository(JpaRepositoryConfig.builder(TestEntity.class, MergedResource.class, mapper).build());
		}
	}

	private MergedResource newMergedResource() {
		RelatedEntity oneRelated = new RelatedEntity();
		oneRelated.setId(3L);
		oneRelated.setStringValue("oneRelated");

		RelatedEntity manyRelated = new RelatedEntity();
		manyRelated.setId(4L);
		manyRelated.setStringValue("manyRelated");

		List<RelatedEntity> manyRelatedValues = new ArrayList<>();
		manyRelatedValues.add(manyRelated);

		MergedResource test = new MergedResource();
		test.setId(2L);
		test.setStringValue("test");
		test.setOneRelatedValue(oneRelated);
		test.setManyRelatedValues(manyRelatedValues);
		return test;
	}

	@Test
	public void testInsert() throws InstantiationException, IllegalAccessException {
		final MergedResource test = newMergedResource();
		repo.save(test);

		SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);
		transactionRunner.doInTransaction(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				TestEntity testEntity = entityManager.find(TestEntity.class, test.getId());
				Assert.assertEquals("test", testEntity.getStringValue());
				RelatedEntity oneRelatedEntity = testEntity.getOneRelatedValue();
				List<RelatedEntity> manyRelatedEntities = testEntity.getManyRelatedValues();
				Assert.assertNotNull(oneRelatedEntity);
				Assert.assertEquals(1, manyRelatedEntities.size());
				return null;
			}
		});
	}

}