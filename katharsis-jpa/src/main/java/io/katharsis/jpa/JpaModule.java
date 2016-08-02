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
import io.katharsis.jpa.internal.DefaultQueryParamsProcessor;
import io.katharsis.jpa.internal.JpaResourceInformationBuilder;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.QueryBuilderFactory;
import io.katharsis.jpa.internal.query.impl.QueryBuilderFactoryImpl;
import io.katharsis.module.Module;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.response.BaseResponseContext;

/**
 * Katharsis module that adds support to expose JPA entities as repositories. It supports:
 * 
 * <ul>
 * 	<li>
 * 		Sorting
 * 		<code>
 * 			?sort[task][name]=asc
 * 		</code>
 * </li>
 * 	<li>
 * 		Filtering
 * 		<code>
 * 			?filter[task][name]=MyTask
 * 		</code>
 * </li>
 * <li>
 * 		Access to relationships for any operation (sorting, filtering, etc.)
 * 		<code>
 * 			?filter[task][project][name]=MyProject
 * 		</code>
 * </li>
 * <li>
 * 		Includes for relationships
 * 		<code>
 * 			?include[task]=project
 * 		</code>
 * </li>
 * <li>
 * 		Paging
 * 		<code>
 * 			?page[offset]=20&page[limit]=10
 * 		</code>
 * </li>
 * </ul>
 * 
 * 
 * Not supported so far:
 * 
 * <ul>
 * <li>
 * 		Selection of fields, always all fields are returned.
 * 		<code>
 * 			?fields[task]=id,name
 * 		</code>
 * </li>
 * <li>
 * 		Sorting and filtering on related resources. Consider doing separate requests on the relations
 *      where necessary. 
 * 		<code>
 * 			/tasks/?sort[project][name]=asc
 * 		</code>
 * </li>
 * </ul>
 * 
 */
public class JpaModule implements Module {

	private Logger logger = LoggerFactory.getLogger(JpaModule.class);

	private static final String MODULE_NAME = "jpa";

	private String resourceSearchPackage;

	private EntityManagerFactory emFactory;
	private EntityManager em;

	private QueryBuilderFactory queryBuilderFactory;
	private QueryParamsProcessor processor;
	private ResourceInformationBuilder resourceInformationBuilder;
	private TransactionRunner transactionRunner;

	private ModuleContext context;

	private MetaLookup metaLookup = MetaLookup.INSTANCE;

	/**
	 * Constructor used on client side.
	 */
	public JpaModule(String resourceSearchPackage) {
		this.resourceSearchPackage = resourceSearchPackage;
	}

	/**
	 * Constructor used on server side.
	 */
	public JpaModule(EntityManagerFactory emFactory, EntityManager em, TransactionRunner transactionRunner) {
		this.emFactory = emFactory;
		this.em = em;
		this.transactionRunner = transactionRunner;
		this.queryBuilderFactory = new QueryBuilderFactoryImpl(em);
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;
		// context.addResourceLookup(getResourceLookup());
		context.addResourceInformationBuilder(getResourceInformationBuilder());

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

	public static class JpaEntityResourceLookup implements ResourceLookup {

		private Reflections reflections;

		public JpaEntityResourceLookup(String packageName) {
			if (packageName != null) {
				String[] packageNames = packageName.split(",");
				reflections = new Reflections(packageNames);
			} else {
				reflections = new Reflections(packageName);
			}
		}

		@Override
		public Set<Class<?>> getResourceClasses() {
			return reflections.getTypesAnnotatedWith(Entity.class);
		}

		@Override
		public Set<Class<?>> getResourceRepositoryClasses() {
			return Collections.emptySet();
		}
	}

	private void setupServerRepositories() {
		Set<ManagedType<?>> managedTypes = emFactory.getMetamodel().getManagedTypes();
		for (ManagedType<?> managedType : managedTypes) {
			MetaElement meta = metaLookup.getMeta(managedType.getJavaType());
			setupRepository(meta);

		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		JpaEntityRepository repository = new JpaEntityRepository(this, resourceClass);
		context.addRepository(resourceClass, repository);

		Set<Class<?>> relatedResourceClasses = new HashSet<Class<?>>();
		for (MetaAttribute attr : metaEntity.getAttributes()) {
			if (attr.isAssociation()) {
				relatedResourceClasses.add(attr.getType().getImplementationClass());
			}
		}
		for (Class<?> relatedResourceClass : relatedResourceClasses) {
			JpaRelationshipRepository relationshipRepository = new JpaRelationshipRepository(this, resourceClass);
			context.addRepository(resourceClass, relatedResourceClass, relationshipRepository);
		}
	}

	// /**
	// * ResourceLookup used to expose JPA entity repositories. By default all
	// * entities will be exposed using {@link JpaResourceLookup}}.
	// */
	// public ResourceLookup getResourceLookup() {
	// if (resourceLookup == null)
	// resourceLookup = new JpaResourceLookup(this);
	// return resourceLookup;
	// }

	/**
	 * ResourceInformationBuilder used to describe JPA entity classes.
	 */
	public ResourceInformationBuilder getResourceInformationBuilder() {
		if (resourceInformationBuilder == null)
			resourceInformationBuilder = new JpaResourceInformationBuilder(em);
		return resourceInformationBuilder;
	}

	/**
	 * {@link QueryParamsProcessor}} used to process request parameters and
	 * translate to queries..
	 */
	public QueryParamsProcessor getProcessor() {
		if (processor == null)
			processor = new DefaultQueryParamsProcessor(context.getResourceRegistry());
		return processor;
	}

	// public void setResourceLookup(JpaResourceLookup resourceLookup) {
	// this.resourceLookup = resourceLookup;
	// }

	/**
	 * {@link QueryBuilderFactory}} implementation used to create JPA queries.
	 */
	public QueryBuilderFactory getQueryBuilderFactory() {
		return queryBuilderFactory;
	}

	public void setQueryBuilderFactory(QueryBuilderFactory queryBuilderFactory) {
		this.queryBuilderFactory = queryBuilderFactory;
	}

	public void setProcessor(QueryParamsProcessor processor) {
		this.processor = processor;
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
