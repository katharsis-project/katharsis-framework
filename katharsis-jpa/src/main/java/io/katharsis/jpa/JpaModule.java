package io.katharsis.jpa;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.core.internal.boot.TransactionRunner;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.jpa.internal.JpaRequestContext;
import io.katharsis.jpa.internal.JpaResourceInformationBuilder;
import io.katharsis.jpa.internal.OptimisticLockExceptionMapper;
import io.katharsis.jpa.internal.query.backend.querydsl.QuerydslQueryImpl;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.meta.JpaMetaProvider;
import io.katharsis.jpa.meta.MetaEntity;
import io.katharsis.jpa.meta.MetaJpaDataObject;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.JpaQueryFactoryContext;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslRepositoryFilter;
import io.katharsis.jpa.query.querydsl.QuerydslTranslationContext;
import io.katharsis.jpa.query.querydsl.QuerydslTranslationInterceptor;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaType;
import io.katharsis.meta.model.resource.MetaJsonObject;
import io.katharsis.meta.model.resource.MetaResource;
import io.katharsis.meta.provider.resource.ResourceMetaProvider;
import io.katharsis.module.Module;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.repository.decorate.RelationshipRepositoryDecorator;
import io.katharsis.repository.decorate.RepositoryDecoratorFactory;
import io.katharsis.repository.decorate.ResourceRepositoryDecorator;
import io.katharsis.repository.filter.AbstractDocumentFilter;
import io.katharsis.repository.filter.DocumentFilterChain;
import io.katharsis.repository.filter.DocumentFilterContext;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.information.ResourceInformationBuilder;

/**
 * Katharsis module that adds support to expose JPA entities as repositories. It
 * supports:
 * 
 * <ul>
 * <li>Sorting</li>
 * <li>Filtering</li>
 * <li>Access to relationships for any operation (sorting, filtering, etc.)</li>
 * <li>Includes for relationships</li>
 * <li>Paging</li>
 * <li>Mapping to DTOs</li>
 * <li>Criteria API and QueryDSL support</li>
 * <li>Computed attributes that map JPA Criteria/QueryDSL expressions to DTO
 * attributes</li>
 * <li>JpaRepositoryFilter to customize the repositories</li>
 * <li>Client and server support</li>
 * <li>No need for katharsis annotations by default. Reads the entity
 * annotations.</li>
 * </ul>
 * 
 * 
 * Not supported so far:
 * 
 * <ul>
 * <li>Selection of fields, always all fields are returned.</li>
 * <li>Sorting and filtering on related resources. Consider doing separate
 * requests on the relations where necessary.</li>
 * </ul>
 * 
 */
public class JpaModule implements Module {

	private Logger logger = LoggerFactory.getLogger(JpaModule.class);

	private static final String MODULE_NAME = "jpa";

	private EntityManagerFactory emFactory;

	private EntityManager em;

	private JpaQueryFactory queryFactory;

	private ResourceInformationBuilder resourceInformationBuilder;

	private TransactionRunner transactionRunner;

	private ModuleContext context;

	private MetaLookup jpaMetaLookup = new MetaLookup();

	private MetaLookup resourceMetaLookup = new MetaLookup();

	/**
	 * Maps resource class to its configuration
	 */
	private Map<Class<?>, JpaRepositoryConfig<?>> repositoryConfigurationMap = new HashMap<>();

	private JpaRepositoryFactory repositoryFactory;

	private List<JpaRepositoryFilter> filters = new CopyOnWriteArrayList<>();

	private ResourceMetaProvider resourceMetaProvider;

	/**
	 * Constructor used on client side.
	 */
	private JpaModule() {
		this.jpaMetaLookup.addProvider(new JpaMetaProvider());
		this.resourceMetaProvider = new ResourceMetaProvider();
		this.resourceMetaLookup.addProvider(resourceMetaProvider);
	}

	/**
	 * Constructor used on server side.
	 */
	private JpaModule(EntityManagerFactory emFactory, EntityManager em, TransactionRunner transactionRunner) {
		this();

		this.emFactory = emFactory;
		this.em = em;
		this.transactionRunner = transactionRunner;
		setQueryFactory(JpaCriteriaQueryFactory.newInstance());

		if (emFactory != null) {
			Set<ManagedType<?>> managedTypes = emFactory.getMetamodel().getManagedTypes();
			for (ManagedType<?> managedType : managedTypes) {
				Class<?> managedJavaType = managedType.getJavaType();
				MetaElement meta = jpaMetaLookup.getMeta(managedJavaType, MetaJpaDataObject.class);
				if (meta instanceof MetaEntity) {
					addRepository(JpaRepositoryConfig.builder(managedJavaType).build());
				}
			}
		}
		this.setRepositoryFactory(new DefaultJpaRepositoryFactory());
	}

	/**
	 * Creates a new JpaModule for a Katharsis client.
	 * 
	 * @return module
	 */
	public static JpaModule newClientModule() {
		return new JpaModule();
	}

	/**
	 * Creates a new JpaModule for a Katharsis client.
	 * 
	 * @param resourceSearchPackage
	 *            where to find the entity classes. Has no impact anymore.
	 * @return module
	 */
	@Deprecated
	public static JpaModule newClientModule(String resourceSearchPackage) {
		return newClientModule();
	}

	/**
	 * Creates a new JpaModule for a Katharsis server. No entities are by
	 * default exposed as JSON API resources. Make use of
	 * {@link #addEntityClass(Class)} andd
	 * {@link #addMappedEntityClass(Class, Class, JpaMapper)} to add resources.
	 * 
	 * @param em
	 *            to use
	 * @param transactionRunner
	 *            to use
	 * @return created module
	 */
	public static JpaModule newServerModule(EntityManager em, TransactionRunner transactionRunner) {
		return new JpaModule(null, em, transactionRunner);
	}

	/**
	 * Creates a new JpaModule for a Katharsis server. All entities managed by
	 * the provided EntityManagerFactory are registered to the module and
	 * exposed as JSON API resources if not later configured otherwise.
	 * 
	 * @param emFactory
	 *            to retrieve the managed entities.
	 * @param em
	 *            to use
	 * @param transactionRunner
	 *            to use
	 * @return created module
	 */
	public static JpaModule newServerModule(EntityManagerFactory emFactory, EntityManager em, TransactionRunner transactionRunner) {
		return new JpaModule(emFactory, em, transactionRunner);
	}

	/**
	 * Adds the given filter to this module. Filter will be used by all
	 * repositories managed by this module.
	 * 
	 * @param filter
	 *            to add
	 */
	public void addFilter(JpaRepositoryFilter filter) {
		filters.add(filter);
	}

	/**
	 * Removes the given filter to this module.
	 * 
	 * @param filter
	 *            to remove
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

	public void setRepositoryFactory(JpaRepositoryFactory repositoryFactory) {
		checkNotInitialized();
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * @return set of resource classes made available as repository (entity or
	 *         dto).
	 * @Deprecated use getResourceClasses
	 */
	public Set<Class<?>> getResourceClasses() {
		return Collections.unmodifiableSet(repositoryConfigurationMap.keySet());
	}

	/**
	 * Adds the repository to this module.
	 * 
	 * @param configuration
	 *            to use
	 */
	public <T> void addRepository(JpaRepositoryConfig<T> config) {
		checkNotInitialized();
		Class<?> resourceClass = config.getResourceClass();
		if (repositoryConfigurationMap.containsKey(resourceClass)) {
			throw new IllegalArgumentException(resourceClass.getName() + " is already registered");
		}
		repositoryConfigurationMap.put(resourceClass, config);
	}

	/**
	 * Removes the repository with the given type from this module.
	 * 
	 * @param <D>
	 *            resourse class (entity or mapped dto)
	 * @param resourceClass
	 *            to remove
	 */
	public <T> void removeRepository(Class<T> resourceClass) {
		checkNotInitialized();
		repositoryConfigurationMap.remove(resourceClass);
	}

	private final class JpaQuerydslTranslationInterceptor implements QuerydslTranslationInterceptor {

		@Override
		public <T> void intercept(QuerydslQueryImpl<T> query, QuerydslTranslationContext<T> translationContext) {

			JpaRequestContext requestContext = (JpaRequestContext) query.getPrivateData();
			if (requestContext != null) {
				for (JpaRepositoryFilter filter : filters) {
					invokeFilter(filter, requestContext, translationContext);
				}
			}
		}

		private <T> void invokeFilter(JpaRepositoryFilter filter, JpaRequestContext requestContext, QuerydslTranslationContext<T> translationContext) {
			if (filter instanceof QuerydslRepositoryFilter) {
				Object repository = requestContext.getRepository();
				QuerySpec querySpec = requestContext.getQuerySpec();
				((QuerydslRepositoryFilter) filter).filterQueryTranslation(repository, querySpec, translationContext);
			}
		}
	}

	/**
	 * Removes all entity classes registered by default. Use
	 * {@link #addEntityClass(Class)} or
	 * {@link #addMappedEntityClass(Class, Class, JpaMapper)} to register
	 * classes manually.
	 */
	public void removeRepositories() {
		checkNotInitialized();
		repositoryConfigurationMap.clear();
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}

	private void checkNotInitialized() {
		PreconditionUtil.assertNull("module is already initialized, no further changes can be performed", context);
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;

		this.jpaMetaLookup.initialize();
		this.resourceMetaLookup.initialize();

		context.addResourceInformationBuilder(getResourceInformationBuilder());
		context.addExceptionMapper(new OptimisticLockExceptionMapper());
		context.addRepositoryDecoratorFactory(new JpaRepositoryDecoratorFactory());

		if (em != null) {
			setupServerRepositories();
			setupTransactionMgmt();
		}
	}

	class JpaRepositoryDecoratorFactory implements RepositoryDecoratorFactory {

		@Override
		public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(ResourceRepositoryV2<T, I> repository) {
			JpaRepositoryConfig<T> config = getRepositoryConfig(repository.getResourceClass());
			if (config != null) {
				return config.getRepositoryDecorator();
			}
			return null;
		}

		@Override
		public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryDecorator<T, I, D, J> decorateRepository(RelationshipRepositoryV2<T, I, D, J> repository) {
			JpaRepositoryConfig<T> config = getRepositoryConfig(repository.getSourceResourceClass());
			if (config != null) {
				return config.getRepositoryDecorator(repository.getTargetResourceClass());
			}
			return null;
		}
	}

	protected void setupTransactionMgmt() {
		context.addFilter(new AbstractDocumentFilter() {

			@Override
			public Response filter(final DocumentFilterContext context, final DocumentFilterChain chain) {
				return transactionRunner.doInTransaction(new Callable<Response>() {

					@Override
					public Response call() throws Exception {
						return chain.doFilter(context);
					}
				});
			}
		});
	}

	private void setupServerRepositories() {
		for (JpaRepositoryConfig<?> config : repositoryConfigurationMap.values()) {
			setupRepository(config);
		}
	}

	private void setupRepository(JpaRepositoryConfig<?> config) {
		Class<?> resourceClass = config.getResourceClass();
		MetaEntity metaEntity = jpaMetaLookup.getMeta(config.getEntityClass(), MetaEntity.class);
		if (isValidEntity(metaEntity)) {
			JpaEntityRepository<?, Serializable> jpaRepository = repositoryFactory.createEntityRepository(this, config);

			ResourceRepositoryV2<?, ?> repository = filterResourceCreation(resourceClass, jpaRepository);

			context.addRepository(repository);
			setupRelationshipRepositories(resourceClass, config.getResourceClass() != config.getEntityClass());
		}
	}

	private ResourceRepositoryV2<?, ?> filterResourceCreation(Class<?> resourceClass, JpaEntityRepository<?, ?> repository) {
		JpaEntityRepository<?, ?> filteredRepository = repository;
		for (JpaRepositoryFilter filter : filters) {
			if (filter.accept(resourceClass)) {
				filteredRepository = filter.filterCreation(filteredRepository);
			}
		}
		return filteredRepository;
	}

	private RelationshipRepositoryV2<?, ?, ?, ?> filterRelationshipCreation(Class<?> resourceClass, JpaRelationshipRepository<?, ?, ?, ?> repository) {
		JpaRelationshipRepository<?, ?, ?, ?> filteredRepository = repository;
		for (JpaRepositoryFilter filter : filters) {
			if (filter.accept(resourceClass)) {
				filteredRepository = filter.filterCreation(filteredRepository);
			}
		}
		return filteredRepository;
	}

	/**
	 * Sets up relationship repositories for the given resource class. In case
	 * of a mapper the resource class might not correspond to the entity class.
	 */
	private void setupRelationshipRepositories(Class<?> resourceClass, boolean mapped) {
		MetaLookup metaLookup = mapped ? resourceMetaLookup : jpaMetaLookup;

		Class<? extends MetaDataObject> metaClass = mapped ? MetaJsonObject.class : MetaJpaDataObject.class;
		MetaDataObject meta = metaLookup.getMeta(resourceClass, metaClass);

		for (MetaAttribute attr : meta.getAttributes()) {
			if (!attr.isAssociation()) {
				continue;
			}
			MetaType attrType = attr.getType().getElementType();

			if (attrType instanceof MetaEntity) {
				// normal entity association
				Class<?> attrImplClass = attrType.getImplementationClass();
				JpaRepositoryConfig<?> attrConfig = getRepositoryConfig(attrImplClass);

				// only include relations that are exposed as repositories
				if (attrConfig != null) {
					RelationshipRepositoryV2<?, ?, ?, ?> relationshipRepository = filterRelationshipCreation(attrImplClass, repositoryFactory.createRelationshipRepository(this, resourceClass, attrConfig));
					context.addRepository(relationshipRepository);
				}
			} else if (attrType instanceof MetaResource) {
				Class<?> attrImplClass = attrType.getImplementationClass();
				JpaRepositoryConfig<?> attrConfig = getRepositoryConfig(attrImplClass);
				if (attrConfig == null || attrConfig.getMapper() == null) {
					throw new IllegalStateException("no mapped entity for " + attrType.getName() + " reference by " + attr.getId() + " registered");
				}
				JpaRepositoryConfig<?> targetConfig = getRepositoryConfig(attrImplClass);
				Class<?> targetResourceClass = targetConfig.getResourceClass();

				RelationshipRepositoryV2<?, ?, ?, ?> relationshipRepository = filterRelationshipCreation(targetResourceClass, repositoryFactory.createRelationshipRepository(this, resourceClass, attrConfig));
				context.addRepository(relationshipRepository);
			} else {
				throw new IllegalStateException("unable to process relation: " + attr.getId() + ", neither a entity nor a mapped entity is referenced");
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
	 * @return ResourceInformationBuilder used to describe JPA entity classes.
	 */
	public ResourceInformationBuilder getResourceInformationBuilder() {
		if (resourceInformationBuilder == null) {
			resourceInformationBuilder = new JpaResourceInformationBuilder(jpaMetaLookup);
		}
		return resourceInformationBuilder;
	}

	/**
	 * @return {@link JpaQueryFactory}} implementation used to create JPA
	 *         queries.
	 */
	public JpaQueryFactory getQueryFactory() {
		return queryFactory;
	}

	public void setQueryFactory(JpaQueryFactory queryFactory) {
		this.queryFactory = queryFactory;

		queryFactory.initalize(new JpaQueryFactoryContext() {

			@Override
			public EntityManager getEntityManager() {
				return em;
			}

			@Override
			public MetaLookup getMetaLookup() {
				return jpaMetaLookup;
			}
		});

		if (queryFactory instanceof QuerydslQueryFactory) {
			QuerydslQueryFactory querydslFactory = (QuerydslQueryFactory) queryFactory;
			querydslFactory.addInterceptor(new JpaQuerydslTranslationInterceptor());
		}
	}

	/**
	 * @return {@link EntityManager}} in use.
	 */
	public EntityManager getEntityManager() {
		return em;
	}

	/**
	 * @return {@link EntityManagerFactory}} in use.
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		return emFactory;
	}

	/**
	 * @param resourceClass
	 * @return config
	 */
	@SuppressWarnings("unchecked")
	public <T> JpaRepositoryConfig<T> getRepositoryConfig(Class<T> resourceClass) {
		return (JpaRepositoryConfig<T>) repositoryConfigurationMap.get(resourceClass);
	}

	public MetaLookup getJpaMetaLookup() {
		return jpaMetaLookup;
	}

	public MetaLookup getResourceMetaLookup() {
		return resourceMetaLookup;
	}
}
