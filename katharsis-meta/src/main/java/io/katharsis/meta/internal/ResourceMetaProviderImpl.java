package io.katharsis.meta.internal;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaKey;
import io.katharsis.meta.model.resource.MetaJsonObject;
import io.katharsis.meta.model.resource.MetaResource;
import io.katharsis.meta.model.resource.MetaResourceField;
import io.katharsis.meta.provider.MetaProviderBase;
import io.katharsis.meta.provider.MetaProviderContext;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryAware;

public class ResourceMetaProviderImpl extends MetaProviderBase implements ResourceRegistryAware {

	private ResourceRegistry resourceRegistry;

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		return new HashSet<>(Arrays.asList(MetaResource.class, MetaJsonObject.class, MetaResourceField.class));
	}

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		if (metaClass != MetaResource.class && metaClass != MetaElement.class && metaClass != MetaJsonObject.class)
			return false;

		// note that the resourceRegistry might also contain none JSON API objects with a custom
		// information builder, so only accept if a MetaResource was explicitly requested
		if (resourceRegistry != null && (metaClass == MetaResource.class || metaClass == MetaJsonObject.class)) {
			Class<?> clazz = ClassUtils.getRawType(type);
			if (resourceRegistry.hasEntry(clazz)) {
				return true;
			}
		}

		// always accept if json api annotations are found
		return ClassUtils.getRawType(type).getAnnotation(JsonApiResource.class) != null;
	}

	@Override
	public MetaElement createElement(Type type, MetaProviderContext context) {
		ResourceInformation information = getResourceInformation(ClassUtils.getRawType(type));

		Class<?> resourceClass = information.getResourceClass();

		Class<?> superClass = resourceClass.getSuperclass();
		MetaDataObject superMeta = null;
		if (superClass != Object.class) {
			// super type is either MetaResource or MetaDataObject
			superMeta = context.getLookup().getMeta(superClass, MetaJsonObject.class);
		}

		MetaResource resource = new MetaResource();
		resource.setImplementationType(resourceClass);
		resource.setSuperType(superMeta);
		resource.setName(resourceClass.getSimpleName());
		resource.setResourceType(information.getResourceType());

		ResourceField idField = information.getIdField();
		addAttribute(resource, idField);

		ResourceField metaField = information.getMetaField();
		if (metaField != null) {
			addAttribute(resource, metaField);
		}

		ResourceField linksField = information.getLinksField();
		if (linksField != null) {
			addAttribute(resource, linksField);
		}

		List<ResourceField> attrFields = information.getAttributeFields().getFields();
		for (ResourceField field : attrFields) {
			addAttribute(resource, field);
		}

		for (ResourceField field : information.getRelationshipFields()) {
			addAttribute(resource, field);
		}

		return resource;
	}

	@Override
	public void discoverElements(MetaProviderContext context) {
		if (resourceRegistry != null) {
			// enforce setup of meta data
			for (RegistryEntry entry : resourceRegistry.getResources()) {
				ResourceInformation information = entry.getResourceInformation();
				context.getLookup().getMeta(information.getResourceClass(), MetaResource.class);
			}
		}
	}

	@Override
	public void onInitialized(MetaProviderContext context, MetaElement element) {
		if (element instanceof MetaResource) {
			MetaResource metaResource = (MetaResource) element;

			ResourceInformation information = getResourceInformation(metaResource.getImplementationClass());
			PreconditionUtil.assertNotNull(information.getResourceType(), metaResource);
			for (ResourceField field : information.getRelationshipFields()) {
				if (field.getOppositeName() != null) {
					Class<?> oppositeType = ClassUtils.getRawType(field.getElementType());
					MetaResource oppositeMeta = context.getLookup().getMeta(oppositeType, MetaResource.class);
					MetaAttribute attr = metaResource.getAttribute(field.getUnderlyingName());
					MetaAttribute oppositeAttr = oppositeMeta.getAttribute(field.getOppositeName());
					PreconditionUtil.assertNotNull(attr.getId() + " opposite not found", oppositeAttr);
					attr.setOppositeAttribute(oppositeAttr);
				}
			}

			MetaAttribute idAttr = metaResource.getAttribute(information.getIdField().getUnderlyingName());

			if (metaResource.getSuperType() == null || metaResource.getSuperType().getPrimaryKey() == null) {
				MetaKey primaryKey = new MetaKey();
				primaryKey.setName(metaResource.getName() + "$primaryKey");
				primaryKey.setName(metaResource.getId() + "$primaryKey");
				primaryKey.setElements(Arrays.asList(idAttr));
				primaryKey.setUnique(true);
				primaryKey.setParent(metaResource);
				metaResource.setPrimaryKey(primaryKey);
				context.add(primaryKey);
			}
		}

		if (element instanceof MetaAttribute && element.getParent() instanceof MetaResource) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaDataObject parent = attr.getParent();
			Type implementationType = PropertyUtils.getPropertyType(parent.getImplementationClass(), attr.getName());

			MetaElement metaType = context.getLookup().getMeta(implementationType, MetaJsonObject.class);
			attr.setType(metaType.asType());
		}

	}

	private ResourceInformation getResourceInformation(Class<?> resourceClass) {
		if (resourceRegistry != null) {
			RegistryEntry entry = resourceRegistry.getEntryForClass(resourceClass);
			PreconditionUtil.assertNotNull(resourceClass.getName(), entry);
			return entry.getResourceInformation();
		}
		else {
			AnnotationResourceInformationBuilder infoBuilder = new AnnotationResourceInformationBuilder(
					new ResourceFieldNameTransformer());
			infoBuilder.init(new DefaultResourceInformationBuilderContext(infoBuilder));
			return infoBuilder.build(resourceClass);
		}
	}

	private void addAttribute(MetaResource resource, ResourceField field) {
		if (resource.getSuperType() != null && resource.getSuperType().hasAttribute(field.getUnderlyingName())) {
			return; // nothing to do
		}

		MetaResourceField attr = new MetaResourceField();

		attr.setParent(resource);
		attr.setName(field.getUnderlyingName());
		attr.setAssociation(field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP);
		attr.setMeta(field.getResourceFieldType() == ResourceFieldType.META_INFORMATION);
		attr.setLinks(field.getResourceFieldType() == ResourceFieldType.LINKS_INFORMATION);
		attr.setVersion(false); // TODO
		attr.setDerived(false);
		attr.setLazy(field.isLazy());

		PreconditionUtil.assertFalse(attr.getName(),
				!attr.isAssociation() && MetaElement.class.isAssignableFrom(field.getElementType()));

		// FIXME
		//		field.getJsonName()
		//		field.getLookupIncludeAutomatically()
		//		field.getOppositeName()
		//		field.getIncludeByDefault()
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}
}
