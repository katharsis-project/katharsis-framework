package io.katharsis.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceLookup;

/**
 * Vanilla {@link Module} implementation that allows registration of extensions.
 */
public class SimpleModule implements Module {

	private List<ResourceInformationBuilder> resourceInformationBuilders = new ArrayList<ResourceInformationBuilder>();
	private List<Filter> filters = new ArrayList<Filter>();
	private List<ResourceLookup> resourceLookups = new ArrayList<ResourceLookup>();
	private List<com.fasterxml.jackson.databind.Module> jacksonModules = new ArrayList<com.fasterxml.jackson.databind.Module>();
	private List<RelationshipRepositoryRegistration> relationshipRepositoryRegistrations = new ArrayList<RelationshipRepositoryRegistration>();
	private List<ResourceRepositoryRegistration> resourceRepositoryRegistrations = new ArrayList<ResourceRepositoryRegistration>();
	private List<ExceptionMapperLookup> exceptionMapperLookups = new ArrayList<ExceptionMapperLookup>();

	private String moduleName;
	
	private ModuleContext context;

	public SimpleModule(String moduleName) {
		this.moduleName = moduleName;
	}

	@Override
	public String getModuleName() {
		return moduleName;
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;
		for (ResourceInformationBuilder resourceInformationBuilder : resourceInformationBuilders) {
			context.addResourceInformationBuilder(resourceInformationBuilder);
		}
		for (ResourceLookup resourceLookup : resourceLookups) {
			context.addResourceLookup(resourceLookup);
		}
		for (Filter filter : filters) {
			context.addFilter(filter);
		}
		for (com.fasterxml.jackson.databind.Module jacksonModule : jacksonModules) {
			context.addJacksonModule(jacksonModule);
		}
		for (ResourceRepositoryRegistration reg : resourceRepositoryRegistrations) {
			context.addRepository(reg.resourceClass, reg.repository);
		}
		for (RelationshipRepositoryRegistration reg : relationshipRepositoryRegistrations) {
			context.addRepository(reg.sourceType, reg.targetType, reg.repository);
		}
		for (ExceptionMapperLookup exceptionMapperLookup : exceptionMapperLookups) {
			context.addExceptionMapperLookup(exceptionMapperLookup);
		}
	}
	
	private void checkInitialized(){
		if(context != null){
			throw new IllegalStateException("module cannot be changed addModule was called");
		}
	}

	/**
	 * Registers a new {@link ResourceInformationBuilder} with this module.
	 * 
	 * @param resourceInformationBuilder resource information builder
	 */
	public void addResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder) {
		checkInitialized();
		resourceInformationBuilders.add(resourceInformationBuilder);
	}


	public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
		checkInitialized();
		exceptionMapperLookups.add(exceptionMapperLookup);
	}
	
	public void addExceptionMapper(@SuppressWarnings("rawtypes") JsonApiExceptionMapper exceptionMapper) {
		checkInitialized();
		ExceptionMapperLookup exceptionMapperLookup = new CollectionExceptionMapperLookup(exceptionMapper);
		exceptionMapperLookups.add(exceptionMapperLookup);
	}
	
	protected List<ResourceInformationBuilder> getResourceInformationBuilders() {
		checkInitialized();
		return Collections.unmodifiableList(resourceInformationBuilders);
	}

	public void addFilter(Filter filter) {
		checkInitialized();
		filters.add(filter);
	}

	protected List<Filter> getFilters() {
		checkInitialized();
		return Collections.unmodifiableList(filters);
	}

	public void addJacksonModule(com.fasterxml.jackson.databind.Module module) {
		checkInitialized();
		jacksonModules.add(module);
	}

	protected List<com.fasterxml.jackson.databind.Module> getJacksonModules() {
		checkInitialized();
		return Collections.unmodifiableList(jacksonModules);
	}

	/**
	 * Registers a new {@link ResourceLookup} with this module.
	 * 
	 * @param resourceLookup resource lookup
	 */
	public void addResourceLookup(ResourceLookup resourceLookup) {
		checkInitialized();
		resourceLookups.add(resourceLookup);
	}

	protected List<ResourceLookup> getResourceLookups() {
		checkInitialized();
		return Collections.unmodifiableList(resourceLookups);
	}

	public void addRepository(Class<?> type, ResourceRepository<?, ?> repository) {
		checkInitialized();
		resourceRepositoryRegistrations.add(new ResourceRepositoryRegistration(type, repository));
	}

	public void addRepository(Class<?> sourceType, Class<?> targetType, RelationshipRepository<?, ?, ?, ?> repository) {
		checkInitialized();
		relationshipRepositoryRegistrations
				.add(new RelationshipRepositoryRegistration(sourceType, targetType, repository));
	}

	public List<RelationshipRepositoryRegistration> getRelationshipRepositoryRegistrations() {
		return Collections.unmodifiableList(relationshipRepositoryRegistrations);
	}

	public List<ResourceRepositoryRegistration> getResourceRepositoryRegistrations() {
		return Collections.unmodifiableList(resourceRepositoryRegistrations);
	}

	public static class RelationshipRepositoryRegistration {

		private Class<?> sourceType;
		private Class<?> targetType;
		private RelationshipRepository<?, ?, ?, ?> repository;

		public RelationshipRepositoryRegistration(Class<?> sourceType, Class<?> targetType,
				RelationshipRepository<?, ?, ?, ?> repository) {
			this.sourceType = sourceType;
			this.targetType = targetType;
			this.repository = repository;
		}

		public Class<?> getSourceType() {
			return sourceType;
		}

		public Class<?> getTargetType() {
			return targetType;
		}

		public RelationshipRepository<?, ?, ?, ?> getRepository() {
			return repository;
		}

	}

	public static class ResourceRepositoryRegistration {

		private Class<?> resourceClass;
		private ResourceRepository<?, ?> repository;

		public ResourceRepositoryRegistration(Class<?> resourceClass, ResourceRepository<?, ?> repository) {
			this.resourceClass = resourceClass;
			this.repository = repository;
		}

		public Class<?> getResourceClass() {
			return resourceClass;
		}

		public ResourceRepository<?, ?> getRepository() {
			return repository;
		}
	}

	public List<ExceptionMapperLookup> getExceptionMapperLookups() {
		return Collections.unmodifiableList(exceptionMapperLookups);
	}
	

	@SuppressWarnings("rawtypes")
	private static class CollectionExceptionMapperLookup implements ExceptionMapperLookup {

		private Set<JsonApiExceptionMapper> set;

		private CollectionExceptionMapperLookup(Set<JsonApiExceptionMapper> set) {
			this.set = set;
		}

		public CollectionExceptionMapperLookup(JsonApiExceptionMapper exceptionMapper) {
			this(new HashSet<JsonApiExceptionMapper>(Arrays.asList(exceptionMapper)));
		}

		@Override
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			return set;
		}
	}
}
