package io.katharsis.client.action;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;

import io.katharsis.resource.registry.ServiceUrlProvider;

public class JerseyActionStubFactory implements ActionStubFactory {

	private Client client;

	private ActionStubFactoryContext context;

	private JerseyActionStubFactory() {
	}

	public static JerseyActionStubFactory newInstance() {
		return newInstance(ClientBuilder.newClient());
	}

	public static JerseyActionStubFactory newInstance(Client client) {
		JerseyActionStubFactory factory = new JerseyActionStubFactory();
		factory.client = client;
		return factory;
	}

	@Override
	public void init(ActionStubFactoryContext context) {
		this.context = context;
	}

	@Override
	public <T> T createStub(Class<T> interfaceClass) {
		ServiceUrlProvider serviceUrlProvider = context.getServiceUrlProvider();
		String serviceUrl = serviceUrlProvider.getUrl();

		WebTarget target = client.target(serviceUrl);
		return WebResourceFactory.newResource(interfaceClass, target);
	}

}
