package io.katharsis.jpa.meta;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;

import io.katharsis.jpa.meta.internal.EmbeddableMetaProvider;
import io.katharsis.jpa.meta.internal.EntityMetaProvider;
import io.katharsis.jpa.meta.internal.MappedSuperclassMetaProvider;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.provider.MetaProvider;
import io.katharsis.meta.provider.MetaProviderBase;
import io.katharsis.meta.provider.MetaProviderContext;

public class JpaMetaProvider extends MetaProviderBase {

	private EntityManagerFactory entityManagerFactory;

	public JpaMetaProvider() {
	}

	public JpaMetaProvider(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public Collection<MetaProvider> getDependencies() {
		return Arrays.asList((MetaProvider) new EntityMetaProvider(), new EmbeddableMetaProvider(), new MappedSuperclassMetaProvider());
	}

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		Set<Class<? extends MetaElement>> set = new HashSet<>();
		set.add(MetaEntity.class);
		set.add(MetaEmbeddable.class);
		set.add(MetaMappedSuperclass.class);
		set.add(MetaEntityAttribute.class);
		set.add(MetaEmbeddableAttribute.class);
		return set;
	}

	@Override
	public void discoverElements(MetaProviderContext context) {
		if (entityManagerFactory != null) {
			Set<EmbeddableType<?>> embeddables = entityManagerFactory.getMetamodel().getEmbeddables();
			for (EmbeddableType<?> embeddable : embeddables) {
				context.getLookup().getMeta(embeddable.getJavaType(), MetaJpaDataObject.class);
			}

			Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();
			for (EntityType<?> entity : entities) {
				context.getLookup().getMeta(entity.getJavaType(), MetaJpaDataObject.class);
			}
		}
	}

}
