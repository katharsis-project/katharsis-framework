//package io.katharsis.jpa.internal;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.ParameterizedType;
//import java.util.HashSet;
//import java.util.Set;
//
//import javax.persistence.metamodel.ManagedType;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.katharsis.jpa.JpaEntityRepository;
//import io.katharsis.jpa.JpaModule;
//import io.katharsis.jpa.internal.meta.MetaElement;
//import io.katharsis.jpa.internal.meta.MetaEntity;
//import io.katharsis.jpa.internal.meta.MetaLookup;
//import io.katharsis.resource.registry.ResourceLookup;
//import net.bytebuddy.ByteBuddy;
//import net.bytebuddy.description.modifier.Visibility;
//import net.bytebuddy.dynamic.DynamicType.Builder;
//import net.bytebuddy.implementation.MethodCall;
//
//public class JpaResourceLookup implements ResourceLookup {
//
//	private static final Logger logger = LoggerFactory.getLogger(JpaResourceLookup.class);
//
//	private Set<Class<?>> entityClasses = new HashSet<Class<?>>();
//	private Set<Class<?>> repositoryClasses = new HashSet<Class<?>>();
//
//	public JpaResourceLookup(JpaModule module) {
//		MetaLookup metaLookup = MetaLookup.INSTANCE;
//
//		Set<ManagedType<?>> managedTypes = module.getEntityManagerFactory().getMetamodel().getManagedTypes();
//		for (ManagedType<?> managedType : managedTypes) {
//			MetaElement meta = metaLookup.getMeta(managedType.getJavaType());
//			if (!(meta instanceof MetaEntity))
//				continue;
//			MetaEntity metaEntity = meta.asEntity();
//			if (metaEntity.getPrimaryKey() == null) {
//				logger.warn("{} has no primary key and will be ignored", metaEntity.getName());
//				continue;
//			}
//			if (metaEntity.getPrimaryKey().getElements().size() > 1) {
//				logger.warn("{} has a compound primary key and will be ignored", metaEntity.getName());
//				continue;
//			}
//
//			Class<?> pkType = metaEntity.getPrimaryKey().getType().getImplementationClass();
//			ParameterizedType superType = TypeUtils.parameterize(JpaEntityRepository.class,
//					metaEntity.getImplementationClass(), pkType);
//
//			
//			
//			
//			Builder<Object> builder = new ByteBuddy().subclass(superType);
//
//			try {
//				@SuppressWarnings("rawtypes")
//				Constructor<JpaEntityRepository> superconstructor = JpaEntityRepository.class
//						.getDeclaredConstructor(JpaModule.class, Class.class);
//
//				builder = builder.defineConstructor(Visibility.PUBLIC).intercept(MethodCall.invoke(superconstructor)
//						.onSuper().with(module, metaEntity.getImplementationClass()));
//				Class<? extends Object> repositoryType = builder.make().load(getClass().getClassLoader()).getLoaded();
//
//				entityClasses.add(metaEntity.getImplementationClass());
//				repositoryClasses.add(repositoryType);
//			} catch (Exception e) {
//				throw new IllegalStateException("failed to create repository for " + metaEntity.getName(), e);
//			}
//		}
//	}
//
//	@Override
//	public Set<Class<?>> getResourceClasses() {
//		return entityClasses;
//	}
//
//	@Override
//	public Set<Class<?>> getResourceRepositoryClasses() {
//		return repositoryClasses;
//	}
//
//}
