//package io.katharsis.jpa;
//
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import io.katharsis.jpa.internal.JpaResourceLookup;
//import io.katharsis.jpa.model.JoinedTableBaseEntity;
//import io.katharsis.jpa.model.JoinedTableChildEntity;
//import io.katharsis.jpa.model.RelatedEntity;
//import io.katharsis.jpa.model.SingleTableBaseEntity;
//import io.katharsis.jpa.model.SingleTableChildEntity;
//import io.katharsis.jpa.model.TablePerClassBaseEntity;
//import io.katharsis.jpa.model.TablePerClassChildEntity;
//import io.katharsis.jpa.model.TestEntity;
//import io.katharsis.jpa.query.AbstractJpaTest;
//import io.katharsis.queryParams.QueryParams;
//
//public class JpaResourceLookupTest extends AbstractJpaTest {
//
//	@Test
//	public void test() throws InstantiationException, IllegalAccessException {
//		JpaResourceLookup lookup = new JpaResourceLookup(module);
//		Set<Class<?>> resourceClasses = lookup.getResourceClasses();
//		assertNotEquals(0, resourceClasses.size());
//
//		Set<Class<?>> resourceRepositoryClasses = lookup.getResourceRepositoryClasses();
//		assertNotEquals(0, resourceRepositoryClasses.size());
//
//		Set<Class<?>> entityClasses = new HashSet<Class<?>>();
//		for (Class<?> resourceRepositoryClass : resourceRepositoryClasses) {
//			assertTrue(JpaEntityRepository.class.isAssignableFrom(resourceRepositoryClass));
//			JpaEntityRepository<?, ?> repo = (JpaEntityRepository<?, ?>) resourceRepositoryClass.newInstance();
//			Assert.assertNotNull(repo.getEntityType());
//			entityClasses.add(repo.getEntityType());
//
//			QueryParams queryParams = new QueryParams();
//			List<?> entities = repo.findAll(queryParams);
//			assertNotNull(entities);
//			assertNotEquals(0, entities.size());
//		}
//
//		assertTrue(entityClasses.contains(TestEntity.class));
//		assertTrue(entityClasses.contains(RelatedEntity.class));
//		assertTrue(entityClasses.contains(TablePerClassBaseEntity.class));
//		assertTrue(entityClasses.contains(TablePerClassChildEntity.class));
//		assertTrue(entityClasses.contains(JoinedTableBaseEntity.class));
//		assertTrue(entityClasses.contains(JoinedTableChildEntity.class));
//		assertTrue(entityClasses.contains(SingleTableBaseEntity.class));
//		assertTrue(entityClasses.contains(SingleTableChildEntity.class));
//	}
//}
