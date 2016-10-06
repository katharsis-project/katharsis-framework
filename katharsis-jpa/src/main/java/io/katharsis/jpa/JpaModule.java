package io.katharsis.jpa;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

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
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.meta.impl.MetaResourceImpl;
import io.katharsis.jpa.internal.util.KatharsisAssert;
import io.katharsis.jpa.mapping.IdentityMapper;
import io.katharsis.jpa.mapping.JpaMapper;
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

	private HashSet<Class<?>> entityClasses = new HashSet<>();;

	private Map<Class<?>, MappedRegistration<?, ?>> mappings = new HashMap<>();

	private JpaRepositoryFactory repositoryFactory;

	private List<JpaRepositoryFilter> filters = new CopyOnWriteArrayList<>();

	/**
	 * Constructor used on client side.
	 */
	private JpaModule(String resourceSearchPackage) {
		this.resourceSearchPackage = resourceSearchPackage;

		Reflections reflections;
		if (resourceSearchPackage != null) {
			String[] packageNames = resourceSearchPackage.split(",");
			Object[] objPackageNames = new Object[packageNames.length];
			System.arraycopy(packageNames, 0, objPackageNames, 0, packageNames.length);
			reflections = new Reflections(objPackageNames);
		}
		else {
			reflections = new Reflections(resourceSearchPackage);
		}
		this.entityClasses.addAll(reflections.getTypesAnnotatedWith(Entity.class));
	}

	/**
	 * Constructor used on server side.
	 */
	private JpaModule(EntityManagerFactory emFactory, EntityManager em, TransactionRunner transactionRunner) {
		this.emFactory = emFactory;
		this.em = em;
		this.transactionRunner = transactionRunner;
		this.queryFactory = JpaCriteriaQueryFactory.newInstance(metaLookup, em);

		if (emFactory != null) {
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
	}

	/**
	 * Creates a new JpaModule for a Katharsis client. 
	 * 
	 * @param resourceSearchPackage where to find the entity classes.
	 * @return module
	 */
	public static JpaModule newClientModule(String resourceSearchPackage) {
		return new JpaModule(resourceSearchPackage);
	}

	/**
	 * Creates a new JpaModule for a Katharsis server. No entities are 
	 * by default exposed as JSON API resources. Make use of
	 * {@link #addEntityClass(Class)} andd {@link #addMappedEntityClass(Class, Class, JpaMapper)}
	 * to add resources.
	 * 
	 * @param emFactory
	 * @param entityManager
	 * @param transactionRunner
	 * @return module
	 */
	public static JpaModule newServerModule(EntityManager em, TransactionRunner transactionRunner) {
		return new JpaModule(null, em, transactionRunner);
	}

	/**
	 * Creates a new JpaModule for a Katharsis server. All entities managed by
	 * the provided EntityManagerFactory are registered to the module 
	 * and exposed as JSON API resources if not later configured otherwise.
	 * 
	 * @param emFactory
	 * @param entityManager
	 * @param transactionRunner
	 * @return module
	 */
	public static JpaModule newServerModule(EntityManagerFactory emFactory, EntityManager em,
			TransactionRunner transactionRunner) {
		return new JpaModule(emFactory, em, transactionRunner);
	}

	/**
	 * Adds the given filter to this module. Filter will be used by all repositories managed by this module.
	 * 
	 * @param filter
	 */
	public void addFilter(JpaRepositoryFilter filter) {
		filters.add(filter);
	}

	/**
	 * Removes the given filter to this module. 
	 * 
	 * @param filter
	 */
	public void removeFilter(JpaRepositoryFilter filter) {
		filters.remove(filter);
	}

	/**
	 * @return all filters
	 */
	public List<JpaRepositoryFilter> getFilters() {
		return filters;
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
	 * Adds the given entity class which is mapped to a DTO with the provided mapper.
	 * 
	 * @param entityClass
	 * @param dtoClass
	 * @param mapper
	 */
	public <E, D> void addMappedEntityClass(Class<E> entityClass, Class<D> dtoClass, JpaMapper<E, D> mapper) {
		checkNotInitialized();
		if (mappings.containsKey(dtoClass)) {
			throw new IllegalArgumentException(dtoClass.getName() + " is already registered");
		}
		mappings.put(dtoClass, new MappedRegistration<>(entityClass, dtoClass, mapper));
	}

	/**
	 * Adds the given entity class which is mapped to a DTO with the provided mapper.
	 * 
	 * @param entityClass
	 * @param dtoClass
	 * @param mapper
	 */
	public <D> void removeMappedEntityClass(Class<D> dtoClass) {
		checkNotInitialized();
		mappings.remove(dtoClass);
	}

	private static class MappedRegistration<E, D> {

		Class<E> entityClass;

		Class<D> dtoClass;

		JpaMapper<E, D> mapper;

		MappedRegistration(Class<E> entityClass, Class<D> dtoClass, JpaMapper<E, D> mapper) {
			this.entityClass = entityClass;
			this.dtoClass = dtoClass;
			this.mapper = mapper;
		}

		public Class<E> getEntityClass() {
			return entityClass;
		}

		public Class<D> getDtoClass() {
			return dtoClass;
		}

		public JpaMapper<E, D> getMapper() {
			return mapper;
		}
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

	/**
	 * Removes all entity classes registered by default. Use {@link #addEntityClass(Class)} or
	 * {@link #addMappedEntityClass(Class, Class, JpaMapper)} to register classes manually.
	 */
	public void removeAllEntityClasses() {
		checkNotInitialized();
		entityClasses.clear();
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
		}
		else {
			context.addResourceLookup(new MappingsResourceLookup());
			setupServerRepositories();
			setupTransactionMgmt();
		}
	}

	/**
	 * Makes all the mapped DTO classes available to katharsis.
	 */
	private class MappingsResourceLookup implements ResourceLookup {

		@Override
		public Set<Class<?>> getResourceClasses() {
			return Collections.unmodifiableSet(mappings.keySet());
		}

		@Override
		public Set<Class<?>> getResourceRepositoryClasses() {
			return Collections.emptySet();
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

		for (MappedRegistration<?, ?> mapping : mappings.values()) {
			setupMappedRepository(mapping);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupMappedRepository(MappedRegistration<?, ?> mapping) {
		MetaEntity metaEntity = metaLookup.getMeta(mapping.getEntityClass()).asEntity();
		if (isValidEntity(metaEntity)) {
			JpaEntityRepository<?, ?> repository = repositoryFactory.createMappedEntityRepository(this, mapping.getEntityClass(),
					mapping.getDtoClass(), (JpaMapper) mapping.getMapper());
			context.addRepository(mapping.getDtoClass(), repository);

			setupRelationshipRepositories(mapping.getDtoClass());
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private void setupRepository(MetaElement meta) {
		if (!(meta instanceof MetaEntity))
			return;
		MetaEntity metaEntity = meta.asEntity();
		if (isValidEntity(metaEntity)) {
			Class<?> resourceClass = metaEntity.getImplementationClass();
			JpaEntityRepository repository = repositoryFactory.createEntityRepository(this, resourceClass);
			context.addRepository(resourceClass, repository);

			setupRelationshipRepositories(resourceClass);

		}
	}

	/**
	 * Sets up  relationship repositories for the given resource class. In case of a mapper
	 * the resource class might not correspond to the entity class.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupRelationshipRepositories(Class<?> resourceClass) {
		MetaDataObject meta = metaLookup.getMeta(resourceClass).asDataObject();

		for (MetaAttribute attr : meta.getAttributes()) {
			if (!attr.isAssociation()) {
				continue;
			}
			MetaType attrType = attr.getType().getElementType();
			if (attrType instanceof MetaEntity) {
				// normal entity association
				Class<?> attrImplClass = attr.getType().getElementType().getImplementationClass();

				// only include relations that are exposed as repositories
				if (entityClasses.contains(attrImplClass)) {
					JpaRelationshipRepository<?, ?, ?, ?> relationshipRepository = repositoryFactory
							.createRelationshipRepository(this, resourceClass, attrImplClass);
					context.addRepository(resourceClass, attrImplClass, relationshipRepository);
				}
			}
			else if (attrType instanceof MetaResourceImpl) {
				Class<?> attrImplClass = attrType.getImplementationClass();
				if (!mappings.containsKey(attrImplClass)) {
					throw new IllegalStateException(
							"no mapped entity for " + attrType.getName() + " reference by " + attr.getId() + " registered");
				}
				MappedRegistration<?, ?> targetMapping = mappings.get(attrImplClass);
				Class<?> targetEntityClass = targetMapping.getEntityClass();
				Class<?> targetDtoClass = targetMapping.getDtoClass();
				JpaMapper targetMapper = targetMapping.getMapper();

				Class sourceEntityClass;
				JpaMapper sourceMapper;
				if (meta instanceof MetaEntity) {
					sourceEntityClass = resourceClass;
					sourceMapper = IdentityMapper.newInstance();
				}
				else {
					MappedRegistration<?, ?> sourceMapping = mappings.get(resourceClass);
					sourceEntityClass = sourceMapping.getEntityClass();
					sourceMapper = sourceMapping.getMapper();
				}

				JpaRelationshipRepository<?, ?, ?, ?> relationshipRepository = repositoryFactory
						.createMappedRelationshipRepository(this, sourceEntityClass, resourceClass, targetEntityClass, targetDtoClass,
								sourceMapper, targetMapper);
				context.addRepository(resourceClass, targetDtoClass, relationshipRepository);
			}
			else {
				throw new IllegalStateException(
						"unable to process relation: " + attr.getId() + ", neither a entity nor a mapped entity is referenced");
			}
		}
	}

	private boolean isValidEntity(MetaEntity metaEntity) {
		if (metaEntity.getPrimaryKey() == null) {
			logger.warn("{} has no primary key and will be ignored", metaEntity.getName());
			return false;
		}
		if (metaEntity.getPrimaryKey().getElements().size() > 1) {
			logger.warn("{} has a compound primary key and will be ignored", metaEntity.getName());
			return false;
		}
		return true;
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
