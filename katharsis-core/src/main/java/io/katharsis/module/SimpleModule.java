package io.katharsis.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.katharsis.dispatcher.filter.Filter;
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

	private String moduleName;

	public SimpleModule(String moduleName) {
		this.moduleName = moduleName;
	}

	@Override
	public String getModuleName() {
		return moduleName;
	}

	@Override
	public void setupModule(ModuleContext context) {
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
	}

	/**
	 * Registers a new {@link ResourceInformationBuilder} with this module.
	 * 
	 * @param resourceInformationBuilder
	 */
	public void addResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder) {
		resourceInformationBuilders.add(resourceInformationBuilder);
	}

	protected List<ResourceInformationBuilder> getResourceInformationBuilders() {
		return Collections.unmodifiableList(resourceInformationBuilders);
	}

	public void addFilter(Filter filter) {
		filters.add(filter);
	}

	protected List<Filter> getFilters() {
		return Collections.unmodifiableList(filters);
	}

	public void addJacksonModule(com.fasterxml.jackson.databind.Module module) {
		jacksonModules.add(module);
	}

	protected List<com.fasterxml.jackson.databind.Module> getJacksonModules() {
		return Collections.unmodifiableList(jacksonModules);
	}

	/**
	 * Registers a new {@link ResourceLookup} with this module.
	 * 
	 * @param resourceInformationBuilder
	 */
	public void addResourceLookup(ResourceLookup resourceLookup) {
		resourceLookups.add(resourceLookup);
	}

	protected List<ResourceLookup> getResourceLookups() {
		return Collections.unmodifiableList(resourceLookups);
	}

	public void addRepository(Class<?> type, ResourceRepository<?, ?> repository) {
		resourceRepositoryRegistrations.add(new ResourceRepositoryRegistration(type, repository));
	}

	public void addRepository(Class<?> sourceType, Class<?> targetType, RelationshipRepository<?, ?, ?, ?> repository) {
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
}
