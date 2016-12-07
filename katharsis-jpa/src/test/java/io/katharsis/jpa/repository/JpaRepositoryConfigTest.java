package io.katharsis.jpa.repository;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.JpaRepositoryConfig;
import io.katharsis.jpa.JpaRepositoryConfig.Builder;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceListBase;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.paging.DefaultPagedLinksInformation;
import io.katharsis.response.paging.DefaultPagedMetaInformation;

public class JpaRepositoryConfigTest {

  @Test
  public void testTypedList() {
    JpaRepositoryConfig<TestEntity> config = JpaRepositoryConfig.builder(TestEntity.class).setInterfaceClass(TestRepository.class)
        .build();
    Assert.assertEquals(TestList.class, config.getListClass());
    TestList list = (TestList) config.newResultList();
    Assert.assertTrue(list.getMeta() instanceof TestListMeta);
    Assert.assertTrue(list.getLinks() instanceof TestListLinks);
  }

  @Test
  public void testDefaultList() {
    JpaRepositoryConfig<TestEntity> config = JpaRepositoryConfig.builder(TestEntity.class).build();
    Assert.assertEquals(DefaultResourceList.class, config.getListClass());
    DefaultResourceList<TestEntity> list = (DefaultResourceList<TestEntity>) config.newResultList();
    Assert.assertTrue(list.getMeta() instanceof DefaultPagedMetaInformation);
    Assert.assertTrue(list.getLinks() instanceof DefaultPagedLinksInformation);
  }

  @Test(expected = IllegalStateException.class)
  public void testFindAllNotOverriden() {
    Builder<TestEntity> builder = JpaRepositoryConfig.builder(TestEntity.class);
    builder.setInterfaceClass(IncompleteTestRepository.class);
  }

  @Test(expected = IllegalStateException.class)
  public void testFindAllInvalidReturnType() {
    Builder<TestEntity> builder = JpaRepositoryConfig.builder(TestEntity.class);
    builder.setInterfaceClass(InvalidReturnTypeTestRepository.class);
  }

  public interface TestRepository extends QuerySpecResourceRepository<TestEntity, Long> {

    @Override
    public TestList findAll(QuerySpec querySpec);

  }

  public interface IncompleteTestRepository extends QuerySpecResourceRepository<TestEntity, Long> {

  }

  public interface InvalidReturnTypeTestRepository extends QuerySpecResourceRepository<TestEntity, Long> {

    @Override
    public Iterable<TestEntity> findAll(QuerySpec querySpec);

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
