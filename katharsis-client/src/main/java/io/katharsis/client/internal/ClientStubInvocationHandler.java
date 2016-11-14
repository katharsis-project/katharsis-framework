package io.katharsis.client.internal;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.client.action.ActionStubFactory;

public class ClientStubInvocationHandler implements InvocationHandler {

	private static final Set<String> REPOSITORY_METHODS = getMethodNames(QuerySpecResourceRepositoryStub.class);

	private QuerySpecResourceRepositoryStub<?, Serializable> repositoryStub;

	private Object actionStub;

	public ClientStubInvocationHandler(QuerySpecResourceRepositoryStub<?, Serializable> repositoryStub, Object actionStub) {
		this.repositoryStub = repositoryStub;
		this.actionStub = actionStub;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Object.class || REPOSITORY_METHODS.contains(method.getName())) {
			// execute repository method
			return method.invoke(repositoryStub, args);
		}
		else if (actionStub != null) {
			// execute action
			return method.invoke(actionStub, args);
		}
		else {
			throw new IllegalStateException("cannot execute actions, no " + ActionStubFactory.class.getSimpleName() + " set with "
					+ KatharsisClient.class.getName());
		}
	}

	private static Set<String> getMethodNames(Class<?> clazz) {
		Set<String> repositoryMethods = new HashSet<>();
		Method[] repositoryMethodObjects = clazz.getMethods();
		for (Method repositoryMethodObject : repositoryMethodObjects) {
			repositoryMethods.add(repositoryMethodObject.getName());
		}
		return repositoryMethods;
	}
}
