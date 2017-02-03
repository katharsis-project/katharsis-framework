package io.katharsis.jpa;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.jpa.internal.JpaResourceInformationBuilder;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;

@Ignore
public class JpaPartialEntityExposureTest extends AbstractJpaJerseyTest {

	private JpaModule module;

	protected ResourceRepositoryStub<TestEntity, Long> testRepo;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getQueryParamsRepository(TestEntity.class);
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		super.setupModule(module, server);
		this.module = module;
		this.module.removeRepository(RelatedEntity.class);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testCrud() {
		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.save(test);

		List<TestEntity> tests = testRepo.findAll(new QueryParams());
		Assert.assertEquals(1, tests.size());
		test = tests.get(0);
		Assert.assertEquals(2L, test.getId().longValue());
		Assert.assertNull(test.getOneRelatedValue());
		Assert.assertNull(test.getEagerRelatedValue());
		Assert.assertTrue(test.getManyRelatedValues().isEmpty());

		testRepo.delete(test.getId());
		tests = testRepo.findAll(new QueryParams());
		Assert.assertEquals(0, tests.size());
	}

	@Test
	public void testInformationBuilder() {
		EntityManager em = null;
		JpaResourceInformationBuilder builder = new JpaResourceInformationBuilder(module.getJpaMetaLookup());
		ResourceInformation info = builder.build(TestEntity.class);
		List<ResourceField> relationshipFields = info.getRelationshipFields();
		Assert.assertEquals(0, relationshipFields.size());
	}

}
