package io.katharsis.jpa.internal;

import java.lang.reflect.Type;

import io.katharsis.core.internal.resource.ResourceFieldImpl;
import io.katharsis.meta.information.MetaAwareInformation;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.resource.annotations.LookupIncludeBehavior;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.utils.Optional;

public class JpaResourceField extends ResourceFieldImpl implements MetaAwareInformation<MetaAttribute> {

	private MetaAttribute projectedJpaAttribute;

	public JpaResourceField(MetaAttribute projectedJpaAttribute, String jsonName, String underlyingName,
			ResourceFieldType resourceFieldType, Class<?> type, Type genericType, String oppositeResourceType,
			String oppositeName, boolean lazy, boolean includeByDefault, LookupIncludeBehavior lookupIncludeBehavior,
			boolean sortable, boolean filterable, boolean postable, boolean patchable) {
		super(jsonName, underlyingName, resourceFieldType, type, genericType, oppositeResourceType, oppositeName, lazy,
				includeByDefault, lookupIncludeBehavior, sortable, filterable, postable, patchable);
		this.projectedJpaAttribute = projectedJpaAttribute;
	}

	@Override
	public Optional<MetaAttribute> getMetaElement() {
		return Optional.empty();
	}

	@Override
	public Optional<MetaAttribute> getProjectedMetaElement() {
		return Optional.of(projectedJpaAttribute);
	}

}
