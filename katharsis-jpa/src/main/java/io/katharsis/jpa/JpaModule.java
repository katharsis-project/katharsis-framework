package io.katharsis.jpa;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.dispatcher.filter.AbstractFilter;
import io.katharsis.dispatcher.filter.FilterChain;
import io.katharsis.dispatcher.filter.FilterRequestContext;
import io.katharsis.jpa.internal.JpaResourceInformationBuilder;
import io.katharsis.jpa.internal.OptimisticLockExceptionMapper;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.util.KatharsisAssert;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.katharsis.module.Module;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.response.BaseResponseContext;

/**
 * Katharsis module that adds support to expose JPA entities as repositories. It
 * supports:
 * 
 * <ul>
 * <li>Sorting <code>
 * 			?sort[task][name]=asc
 * 		</code></li>
 * <li>Filtering <code>
 * 			?filter[task][name]=MyTask
 * 		</code></li>
 * <li>Access to relationships for any operation (sorting, filtering, etc.)
 * <code>
 * 			?filter[task][project][name]=MyProject
 * 		</code></li>
 * <li>Includes for relationships <code>
 * 			?include[task]=project
 * 		</code></li>
 * <li>Paging <code>
 * 			?page[offset]=20&page[limit]=10
 * 		</code></li>
 * </ul>
 * 
 * 
 * Not supported so far:
 * 
 * <ul>
 * <li>Selection of fields, always all fields are returned. <code>
 * 			?fields[task]=id,name
 * 		</code></li>
 * <li>Sorting and filtering on related resources. Consider doing separate
 * requests on the relations where necessary. <code>
 * 			/tasks/?sort[project][name]=asc
 * 		</code></li>
 * </ul>
 * 
 */
public class JpaModule implements Module {

	private Logger logger = LoggerFactory.getLogger(JpaModule.class);

	private static final String MODULE_NAME = "jpa";

	private String resourceSearchPackage;

	private EntityManagerFactory emFactory;
	private EntityManager em;

	private JpaQueryFactory queryFactory;
	private ResourceInformationBuilder resourceInformationBuilder;
	private TransactionRunner transactionRunner;

	private ModuleContext context;

	private MetaLookup metaLookup = new MetaLookup();
	private HashSet<Class<?>> entityClasses;

	private JpaRepositoryFactory repositoryFactory;

	/**
	 * Constructor used on client side.
	 */
	public JpaModule(String resourceSearchPackage) {
		this.resourceSearchPackage = resourceSearchPackage;

		Reflections reflections;
		if (resourceSearchPackage != null) {
			String[] packageNames = resourceSearchPackage.split(",");
			Object[] objPackageNames = new Object[packageNames.length];
			System.arraycopy(packageNames, 0, objPackageNames, 0, packageNames.length);
			reflections = new Reflections(objPackageNames);
		} else {
			reflections = new Reflections(resourceSearchPackage);
		}
		this.entityClasses = new HashSet<>();
		this.entityClasses.addAll(reflections.getTypesAnnotatedWith(Entity.class));
	}

	/**
	 * Constructor used on server side.
	 */
	public JpaModule(EntityManagerFactory emFactory, EntityManager em, TransactionRunner transactionRunner) {
		this.emFactory = emFactory;
		this.em = em;
		this.transactionRunner = transactionRunner;
		this.queryFactory = JpaCriteriaQueryFactory.newInstance(metaLookup, em);

		this.entityClasses = new HashSet<>();
		Set<ManagedType<?>> managedTypes = emFactory.getMetamodel().getManagedTypes();
		for (ManagedType<?> managedType : managedTypes) {
			Class<?> managedJavaType = managedType.getJavaType();
			MetaElement meta = metaLookup.getMeta(managedJavaType);
			if (meta instanceof MetaEntity) {
				entityClasses.add(managedJavaType);
			}
		}
		this.setRepositoryFactory(new DefaultJpaRepositoryFactory());
	}

	public MetaLookup getMetaLookup() {
		return metaLookup;
	}

	public void setRepositoryFactory(JpaRepositoryFactory repositoryFactory) {
		checkNotInitialized();
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * @return set of entity classes made available as repository.
	 */
	public Set<Class<?>> getEntityClasses() {
		return Collections.unmodifiableSet(entityClasses);
	}

	/**
	 * Adds the given entity class to expose the entity as repository.
	 * 
	 * @param entityClass
	 */
	public void addEntityClass(Class<?> entityClass) {
		checkNotInitialized();
		entityClasses.add(entityClass);
	}

	/**
	 * Removes the given entity class to not expose the entity as repository.
	 * 
	 * @param entityClass
	 */
	public void removeEntityClass(Class<?> entityClass) {
		checkNotInitialized();
		entityClasses.remove(entityClass);
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}

	private void checkNotInitialized() {
		KatharsisAssert.assertNull("module is already initialized, no further changes can be performed", context);
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;

		context.addResourceInformationBuilder(getResourceInformationBuilder());
		context.addExceptionMapper(new OptimisticLockExceptionMapper());

		if (resourceSearchPackage != null) {
			setupClientResourceLookup();
		} else {
			setupServerRepositories();
			setupTransactionMgmt();
		}
	}

	protected void setupTransactionMgmt() {
		context.addFilter(new AbstractFilter() {

			@Override
			public BaseResponseContext filter(final FilterRequestContext context, final FilterChain chain) {
				return transactionRunner.doInTransaction(new Callable<BaseResponseContext>() {

					@Override
					public BaseResponseContext call() throws Exception {
						return chain.doFilter(context);
					}
				});
			}
		});
	}

	private void setupClientResourceLookup() {
		context.addResourceLookup(new JpaEntityResourceLookup(resourceSearchPackage));
	}

	public class JpaEntityResourceLookup implements ResourceLookup {

		public JpaEntityResourceLookup(String packageName) {
		}

		@Override
		public Set<Class<?>> getResourceClasses() {
			return entityClasses;
		}

		@Override
		public Set<Class<?>> getResourceRepositoryClasses() {
			return Collections.emptySet();
		}
	}

	private void setupServerRepositories() {
		for (Class<?> entityClass : entityClasses) {
			MetaElement meta = metaLookup.getMeta(entityClass);
			setupRepository(meta);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private void setupRepository(MetaElement meta) {
		if (!(meta instanceof MetaEntity))
			return;
		MetaEntity metaEntity = meta.asEntity();
		if (metaEntity.getPrimaryKey() == null) {
			logger.warn("{} has no primary key and will be ignored", metaEntity.getName());
			return;
		}
		if (metaEntity.getPrimaryKey().getElements().size() > 1) {
			logger.warn("{} has a compound primary key and will be ignored", metaEntity.getName());
			return;
		}

		Class<?> resourceClass = metaEntity.getImplementationClass();
		JpaEntityRepository repository = repositoryFactory.createEntityRepository(this, resourceClass);
		context.addRepository(resourceClass, repository);

		Set<Class<?>> relatedResourceClasses = getRelatedResource(metaEntity);
		for (Class<?> relatedResourceClass : relatedResourceClasses) {
			JpaRelationshipRepository relationshipRepository = repositoryFactory.createRelationshipRepository(this,
					resourceClass, relatedResourceClass);
			context.addRepository(resourceClass, relatedResourceClass, relationshipRepository);
		}
	}

	private Set<Class<?>> getRelatedResource(MetaEntity metaEntity) {
		Set<Class<?>> relatedResourceClasses = new HashSet<>();
		for (MetaAttribute attr : metaEntity.getAttributes()) {
			if (attr.isAssociation()) {
				Class<?> relType = attr.getType().getImplementationClass();

				// only include relations that are exposed as repositories
				if (entityClasses.contains(relType)) {
					relatedResourceClasses.add(relType);
				}
			}
		}
		return relatedResourceClasses;
	}

	/**
	 * ResourceInformationBuilder used to describe JPA entity classes.
	 */
	public ResourceInformationBuilder getResourceInformationBuilder() {
		if (resourceInformationBuilder == null)
			resourceInformationBuilder = new JpaResourceInformationBuilder(metaLookup, em, entityClasses);
		return resourceInformationBuilder;
	}

	/**
	 * {@link JpaQueryFactory}} implementation used to create JPA queries.
	 */
	public JpaQueryFactory getQueryFactory() {
		return queryFactory;
	}

	public void setQueryFactory(JpaQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	/**
	 * {@link EntityManager}} in use.
	 */
	public EntityManager getEntityManager() {
		return em;
	}

	/**
	 * {@link EntityManagerFactory}} in use.
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		return emFactory;
	}
}
