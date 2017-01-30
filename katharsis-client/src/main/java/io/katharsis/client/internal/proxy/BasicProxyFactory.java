package io.katharsis.client.internal.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.core.internal.utils.WrappedList;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * Basic implementation of {@link ClientProxyFactory}.
 * 
 * Note that resources are not really proxied with this implementation. No lazy-loading is happending. Instead, just the ID is set on an empty resource.
 * Collections are fully proxied and get loaded when accessed.
 *
 */
public class BasicProxyFactory implements ClientProxyFactory {

	private ClientProxyFactoryContext context;

	private Constructor<?> listConstructor;

	private Constructor<?> setConstructor;

	@Override
	public void init(ClientProxyFactoryContext context) {
		this.context = context;

		ClassLoader loader = getClass().getClassLoader();

		listConstructor = Proxy.getProxyClass(loader, ObjectProxy.class, ResourceList.class).getConstructors()[0];
		setConstructor = Proxy.getProxyClass(loader, ObjectProxy.class, Set.class).getConstructors()[0];
	}

	@Override
	public <T> T createResourceProxy(Class<T> clazz, Object id) {
		T instance = ClassUtils.newInstance(clazz);

		ResourceRegistry resourceRegistry = context.getModuleRegistry().getResourceRegistry();
		RegistryEntry entry = resourceRegistry.findEntry(clazz);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		resourceInformation.setId(instance, id);

		return instance;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <C extends Collection<T>, T> C createCollectionProxy(Class<T> resourceClass, Class<C> collectionClass, String url) {

		boolean useSet = Set.class.isAssignableFrom(collectionClass);
		InvocationHandler handler = new CollectionInvocationHandler(resourceClass, url, context, useSet);

		final Constructor<?> constructor = useSet ? setConstructor : listConstructor;

		try {
			Collection lazyCollection = (Collection) constructor.newInstance(handler);
			boolean isCustomClass = WrappedList.class.isAssignableFrom(collectionClass);
			if (isCustomClass) {
				WrappedList collectionImpl = (WrappedList) collectionClass.newInstance();
				collectionImpl.setWrappedList((List) lazyCollection);
				return (C) collectionImpl;
			}
			else {
				return (C) lazyCollection;
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
