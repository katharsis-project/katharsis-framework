package io.katharsis.jpa.repository;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.JpaRepositoryConfig;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.resource.list.ResourceListBase;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.paging.DefaultPagedLinksInformation;

public class JpaRepositoryConfigTest {

	@Test
	public void test() {
		JpaRepositoryConfig<TestEntity> config = JpaRepositoryConfig.builder(TestEntity.class)
				.setInterfaceClass(TestRepository.class).build();
		Assert.assertEquals(TestList.class, config.getListClass());
		TestList list = (TestList) config.newResultList();
		Assert.assertTrue(list.getMeta() instanceof TestListMeta);
		Assert.assertTrue(list.getLinks() instanceof TestListLinks);

	}

	public interface TestRepository extends QuerySpecResourceRepository<TestEntity, Long> {

		@Override
		public TestList findAll(QuerySpec querySpec);

	}

	public static class TestList extends ResourceListBase<TestEntity, TestListMeta, TestListLinks> {

	}

	public static class TestListLinks extends DefaultPagedLinksInformation implements LinksInformation {

		public String name = "value";
	}

	public static class TestListMeta implements MetaInformation {

		public String name = "value";

	}
}
