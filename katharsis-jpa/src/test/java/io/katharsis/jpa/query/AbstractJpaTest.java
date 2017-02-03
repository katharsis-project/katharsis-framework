package io.katharsis.jpa.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.registry.ResourceRegistryImpl;
import io.katharsis.jpa.JpaModule;
import io.katharsis.jpa.meta.JpaMetaProvider;
import io.katharsis.jpa.model.JoinedTableBaseEntity;
import io.katharsis.jpa.model.JoinedTableChildEntity;
import io.katharsis.jpa.model.OtherRelatedEntity;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.SingleTableBaseEntity;
import io.katharsis.jpa.model.SingleTableChildEntity;
import io.katharsis.jpa.model.TablePerClassBaseEntity;
import io.katharsis.jpa.model.TablePerClassChildEntity;
import io.katharsis.jpa.model.TestAnyType;
import io.katharsis.jpa.model.TestEmbeddable;
import io.katharsis.jpa.model.TestEmbeddedIdEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.model.TestIdEmbeddable;
import io.katharsis.jpa.model.TestNestedEmbeddable;
import io.katharsis.jpa.model.TestSubclassWithSuperclassPk;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.katharsis.jpa.util.SpringTransactionRunner;
import io.katharsis.jpa.util.TestConfig;
import io.katharsis.meta.MetaLookup;
import io.katharsis.module.CoreModule;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public abstract class AbstractJpaTest {

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	protected EntityManagerFactory emFactory;

	protected JpaModule module;
	protected JpaQueryFactory queryFactory;

	protected int numTestEntities = 5;

	@Autowired
	protected PlatformTransactionManager txManager;

	@Autowired
	private SpringTransactionRunner transactionRunner;

	protected ResourceRegistry resourceRegistry;

	@Before
	public void setup() {

		ModuleRegistry moduleRegistry = new ModuleRegistry();
		resourceRegistry = new ResourceRegistryImpl(moduleRegistry, new ConstantServiceUrlProvider("http://localhost:1234"));
		module = JpaModule.newServerModule(emFactory, em, transactionRunner);
		setupModule(module);
		moduleRegistry.addModule(new CoreModule(new ResourceFieldNameTransformer()));
		moduleRegistry.addModule(module);
		moduleRegistry.init(new ObjectMapper());

		queryFactory = createQueryFactory(em);
		module.setQueryFactory(queryFactory);

		clear();
		for (int i = 0; i < numTestEntities; i++) {

			TestEmbeddedIdEntity idEntity = new TestEmbeddedIdEntity();
			idEntity.setId(new TestIdEmbeddable(i, "test" + i));
			idEntity.setLongValue(100L + i);
			em.persist(idEntity);

			RelatedEntity related = new RelatedEntity();
			related.setId(100L + i);
			related.setStringValue("related" + i);
			em.persist(related);

			TestAnyType anyValue = new TestAnyType();
			if (i == 0)
				anyValue.setValue("first");
			else
				anyValue.setValue(i);

			TestEmbeddable embValue = new TestEmbeddable();
			embValue.setEmbIntValue(i);
			embValue.setEmbStringValue("emb" + i);
			embValue.setNestedValue(new TestNestedEmbeddable(i == 0));
			// embValue.setRelatedValue(related);
			embValue.setAnyValue(anyValue);

			TestEntity test = new TestEntity();
			test.setStringValue("test" + i);
			test.setId((long) i);
			test.setLongValue(i);
			test.setEmbValue(embValue);
			// test.setLocalTimeValue(LocalTime.now());
			// test.setOffsetDateTimeValue(OffsetDateTime.now());
			// test.setOffsetTimeValue(OffsetTime.now());
			// test.setLocalDateTimeValue(LocalDateTime.now());
			// test.setLocalDateValue(LocalDate.now());

			// do not include relation/map for last value to check for proper
			// left join sorting
			if (i != numTestEntities - 1) {
				test.setOneRelatedValue(related);
				test.getMapValue().put("a", "a" + i);
				test.getMapValue().put("b", "b" + i);
				test.getMapValue().put("c", "c" + i);
			}
			em.persist(test);

			// inheritance
			SingleTableBaseEntity singleTableBase = new SingleTableBaseEntity();
			singleTableBase.setId((long) i);
			singleTableBase.setStringValue("base" + i);
			em.persist(singleTableBase);
			SingleTableChildEntity singleTableChild = new SingleTableChildEntity();
			singleTableChild.setId((long) i + numTestEntities);
			singleTableChild.setStringValue("child" + i);
			singleTableChild.setIntValue(i);
			em.persist(singleTableChild);

			JoinedTableBaseEntity joinedTableBase = new JoinedTableBaseEntity();
			joinedTableBase.setId((long) i);
			joinedTableBase.setStringValue("base" + i);
			em.persist(joinedTableBase);
			JoinedTableChildEntity joinedTableChild = new JoinedTableChildEntity();
			joinedTableChild.setId((long) i + numTestEntities);
			joinedTableChild.setStringValue("child" + i);
			joinedTableChild.setIntValue(i);
			em.persist(joinedTableChild);

			TablePerClassBaseEntity tablePerClassBase = new TablePerClassBaseEntity();
			tablePerClassBase.setId((long) i);
			tablePerClassBase.setStringValue("base" + i);
			em.persist(tablePerClassBase);
			TablePerClassChildEntity tablePerClassChild = new TablePerClassChildEntity();
			tablePerClassChild.setId((long) i + numTestEntities);
			tablePerClassChild.setStringValue("child" + i);
			tablePerClassChild.setIntValue(i);
			em.persist(tablePerClassChild);
		}
		em.flush();
		em.clear();

		queryFactory = createQueryFactory(em);
		module.setQueryFactory(queryFactory);
	}

	/**
	 * Implement this to switch between Criteria und QueryDSL.
	 */
	protected abstract JpaQueryFactory createQueryFactory(EntityManager em);

	protected void setupModule(JpaModule module2) {
	}

	private void clear() {
		clear(em, createQueryFactory(em));
	}

	public static void clear(EntityManager em) {
		clear(em, JpaCriteriaQueryFactory.newInstance());
	}

	public static void clear(final EntityManager em, JpaQueryFactory factory) {
		factory.initalize(new JpaQueryFactoryContext(){
			@Override
			public EntityManager getEntityManager() {
				return em;
			}

			@Override
			public MetaLookup getMetaLookup() {
				MetaLookup metaLookup = new MetaLookup();
				metaLookup.addProvider(new JpaMetaProvider());
				return metaLookup;
			}});
		clear(em, factory.query(TestSubclassWithSuperclassPk.class).buildExecutor().getResultList());
		clear(em, factory.query(RelatedEntity.class).buildExecutor().getResultList());
		clear(em, factory.query(TestEntity.class).buildExecutor().getResultList());
		clear(em, factory.query(OtherRelatedEntity.class).buildExecutor().getResultList());
		em.flush();
		em.clear();
	}

	private static void clear(EntityManager em, List<?> list) {
		for (Object obj : list) {
			em.remove(obj);
		}
	}

}
