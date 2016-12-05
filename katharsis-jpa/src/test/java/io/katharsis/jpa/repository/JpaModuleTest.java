package io.katharsis.jpa.repository;

import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.JpaModule;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;

@Transactional
public class JpaModuleTest extends AbstractJpaTest {

	@Override
	protected void setupModule(JpaModule module) {
		Set<Class<?>> resourceClasses = module.getResourceClasses();
		int n = resourceClasses.size();
		Assert.assertNotEquals(0, n);
		module.removeRepository(TestEntity.class);
		Assert.assertEquals(n - 1, module.getResourceClasses().size());
		module.removeRepositories();
	}

	@Test
	public void test() throws InstantiationException, IllegalAccessException {
		Assert.assertEquals(0, module.getResourceClasses().size());

		Assert.assertEquals("jpa", module.getModuleName());

		Assert.assertNotNull(module.getEntityManagerFactory());
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
