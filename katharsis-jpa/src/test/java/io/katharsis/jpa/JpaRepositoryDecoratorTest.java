package io.katharsis.jpa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.client.QuerySpecRelationshipRepositoryStub;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.jpa.JpaRepositoryConfig.Builder;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.repository.decorate.RelationshipRepositoryDecoratorBase;
import io.katharsis.repository.decorate.ResourceRepositoryDecoratorBase;

public class JpaRepositoryDecoratorTest extends AbstractJpaJerseyTest {

	private QuerySpecResourceRepositoryStub<TestEntity, Long> testRepo;

	private ResourceRepositoryDecoratorBase<TestEntity, Long> resourceDecorator;

	private RelationshipRepositoryDecoratorBase<TestEntity, Long, RelatedEntity, Long> relationshipDecorator;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getQuerySpecRepository(TestEntity.class);
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		super.setupModule(module, server);

		if (server) {
			module.removeRepository(TestEntity.class);

			resourceDecorator = Mockito.spy(new ResourceRepositoryDecoratorBase<TestEntity, Long>() {
			});
			relationshipDecorator = Mockito.spy(new RelationshipRepositoryDecoratorBase<TestEntity, Long, RelatedEntity, Long>() {
			});
			Builder<TestEntity> configBuilder = JpaRepositoryConfig.builder(TestEntity.class);
			configBuilder.setRepositoryDecorator(resourceDecorator);
			configBuilder.putRepositoryDecorator(RelatedEntity.class, relationshipDecorator);
			module.addRepository(configBuilder.build());
		}
	}

	@Test
	public void test() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();

		Mockito.verify(resourceDecorator, Mockito.timeout(1)).create(Mockito.any(TestEntity.class));
		Mockito.verify(relationshipDecorator, Mockito.timeout(1)).setRelation(Mockito.any(TestEntity.class), Mockito.anyLong(),
				Mockito.eq(TestEntity.ATTR_oneRelatedValue));
	}

	private TestEntity addTestWithOneRelation() {
		QuerySpecResourceRepositoryStub<RelatedEntity, Long> relatedRepo = client.getQuerySpecRepository(RelatedEntity.class);
		RelatedEntity related = new RelatedEntity();
		related.setId(1L);
		related.setStringValue("project");
		relatedRepo.save(related);

		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.save(test);

		QuerySpecRelationshipRepositoryStub<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getQuerySpecRepository(TestEntity.class, RelatedEntity.class);
		relRepo.setRelation(test, related.getId(), TestEntity.ATTR_oneRelatedValue);

		return test;
	}
}