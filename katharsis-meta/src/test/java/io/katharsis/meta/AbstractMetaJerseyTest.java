package io.katharsis.meta;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.http.okhttp.OkHttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapterListenerBase;
import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.meta.mock.model.Schedule;
import io.katharsis.meta.provider.resource.ResourceMetaProvider;
import io.katharsis.rs.KatharsisFeature;
import okhttp3.OkHttpClient.Builder;

public abstract class AbstractMetaJerseyTest extends JerseyTest {

	protected KatharsisClient client;

	@Before
	public void setup() {
		client = new KatharsisClient(getBaseUri().toString());
		client.setPushAlways(false);
		client.addModule(createModule());
		setNetworkTimeout(client, 10000, TimeUnit.SECONDS);
	}

	public static void setNetworkTimeout(KatharsisClient client, final int timeout, final TimeUnit timeUnit) {
		OkHttpAdapter httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
		httpAdapter.addListener(new OkHttpAdapterListenerBase() {

			@Override
			public void onBuild(Builder builder) {
				builder.readTimeout(timeout, timeUnit);
			}
		});
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private class TestApplication extends ResourceConfig {

		public TestApplication() {
			property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.meta.mock.model");
			KatharsisFeature feature = new KatharsisFeature();
			feature.addModule(createModule());
			register(feature);
		}
	}

	public MetaModule createModule() {
		MetaModule module = MetaModule.create();
		module.addMetaProvider(new ResourceMetaProvider());
		module.putIdMapping(Schedule.class.getPackage().getName(), "app.resources");
		return module;
	}

}
