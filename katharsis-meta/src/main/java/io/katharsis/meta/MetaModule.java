package io.katharsis.meta;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.meta.internal.MetaRelationshipRepository;
import io.katharsis.meta.internal.MetaResourceRepositoryImpl;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaCollectionType;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaInterface;
import io.katharsis.meta.model.MetaKey;
import io.katharsis.meta.model.MetaListType;
import io.katharsis.meta.model.MetaMapType;
import io.katharsis.meta.model.MetaPrimitiveType;
import io.katharsis.meta.model.MetaSetType;
import io.katharsis.meta.model.MetaType;
import io.katharsis.meta.provider.MetaProvider;
import io.katharsis.module.InitializingModule;
import io.katharsis.module.Module;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistryAware;

public class MetaModule implements Module, InitializingModule {

	private MetaLookup lookup = new MetaLookup();

	private ModuleContext context;

	@Override
	public String getModuleName() {
		return "meta";
	}

	public void putIdMapping(String packageName, String idPrefix) {
		lookup.putIdMapping(packageName, idPrefix);
	}

	public void putIdMapping(String packageName, Class<? extends MetaElement> type, String idPrefix) {
		lookup.putIdMapping(packageName, type, idPrefix);
	}

	public void addMetaProvider(MetaProvider provider) {
		PreconditionUtil.assertNull("module is already initialized and cannot be changed anymore", context);
		lookup.addProvider(provider);
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;

		final Set<Class<? extends MetaElement>> metaClasses = new HashSet<>();
		metaClasses.add(MetaElement.class);
		metaClasses.add(MetaAttribute.class);
		metaClasses.add(MetaCollectionType.class);
		metaClasses.add(MetaDataObject.class);
		metaClasses.add(MetaKey.class);
		metaClasses.add(MetaListType.class);
		metaClasses.add(MetaMapType.class);
		metaClasses.add(MetaPrimitiveType.class);
		metaClasses.add(MetaSetType.class);
		metaClasses.add(MetaType.class);
		metaClasses.add(MetaInterface.class);
		for (MetaProvider provider : lookup.getProviders()) {
			metaClasses.addAll(provider.getMetaTypes());
		}

		AnnotationResourceInformationBuilder informationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		informationBuilder.init(new DefaultResourceInformationBuilderContext(informationBuilder, context.getTypeParser()));

		for (Class<? extends MetaElement> metaClass : metaClasses) {
			if (context.isServer()) {
				context.addRepository(new MetaResourceRepositoryImpl<>(lookup, metaClass));

				HashSet<Class<? extends MetaElement>> targetResourceClasses = new HashSet<>();
				ResourceInformation information = informationBuilder.build(metaClass);
				for (ResourceField relationshipField : information.getRelationshipFields()) {
					if (!MetaElement.class.isAssignableFrom(relationshipField.getElementType())) {
						throw new IllegalStateException("only MetaElement relations supported, got " + relationshipField);
					}
					targetResourceClasses.add((Class<? extends MetaElement>) relationshipField.getElementType());
				}
				for (Class<? extends MetaElement> targetResourceClass : targetResourceClasses) {
					context.addRepository(new MetaRelationshipRepository(lookup, metaClass, targetResourceClass));
				}
			}
		}

		context.addResourceLookup(new ResourceLookup() {

			@SuppressWarnings("unchecked")
			@Override
			public Set<Class<?>> getResourceClasses() {
				return (Set) metaClasses;
			}

			@Override
			public Set<Class<?>> getResourceRepositoryClasses() {
				return Collections.emptySet();
			}
		});
	}

	@Override
	public void init() {
		for (MetaProvider provider : lookup.getProviders()) {
			if (provider instanceof ResourceRegistryAware) {
				((ResourceRegistryAware) provider).setResourceRegistry(context.getResourceRegistry());
			}
		}
		lookup.initialize();
	}

	public static MetaModule create() {
		return new MetaModule();
	}

	public MetaLookup getLookup() {
		return lookup;
	}
}
