package io.katharsis.brave;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.BraveExecutorService;
import com.github.kristofa.brave.okhttp.BraveTracingInterceptor;
import com.github.kristofa.brave.okhttp.BraveTracingInterceptor.Builder;

import io.katharsis.brave.internal.BraveRepositoryFilter;
import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapterListener;
import io.katharsis.client.module.HttpAdapterAware;
import io.katharsis.module.Module;
import okhttp3.Dispatcher;

/**
 * Integrates Brave into katharsis client and server:
 * 
 * <ul>
 *   <li>On the client-side all requests are traced with the brave-okhttp implementation.</li>
 *   <li>
 *   	On the server-side all the repository accesses are traced. Keep in mind that a single HTTP request
 *		can trigger multiple repository accesses if the request contains an inclusion of relations.
 *		Note that no HTTP calls itself are traced by this module. That is the responsibility of the
 *		web container and Brave.
 *   </li>
 * </ul>
 */
public class BraveModule implements Module, HttpAdapterAware, OkHttpAdapterListener {

	private Brave brave;

	private boolean server;

	private BraveModule(Brave brave, boolean server) {
		this.brave = brave;
		this.server = server;
	}

	public static BraveModule newClientModule(Brave brave) {
		return new BraveModule(brave, false);
	}

	public static BraveModule newServerModule(Brave brave) {
		return new BraveModule(brave, true);
	}

	@Override
	public String getModuleName() {
		return "brave";
	}

	@Override
	public void setupModule(ModuleContext context) {
		// nothing to do
		if (server) {
			BraveRepositoryFilter filter = new BraveRepositoryFilter(brave, context);
			context.addRepositoryFilter(filter);
		}
	}

	@Override
	public void setHttpAdapter(HttpAdapter adapter) {
		if (!(adapter instanceof OkHttpAdapter)) {
			throw new IllegalStateException(adapter.getClass() + " not supported yet");
		}
		OkHttpAdapter okHttpAdapter = (OkHttpAdapter) adapter;
		okHttpAdapter.addListener(this);
	}

	@Override
	public void onBuild(okhttp3.OkHttpClient.Builder builder) {
		BraveTracingInterceptor interceptor = buildInterceptor();

		BraveExecutorService tracePropagatingExecutor = buildExecutor(builder);

		builder.addInterceptor(interceptor);
		builder.addNetworkInterceptor(interceptor);
		builder.dispatcher(new Dispatcher(tracePropagatingExecutor));
	}

	protected BraveExecutorService buildExecutor(okhttp3.OkHttpClient.Builder builder) {
		return new BraveExecutorService(new Dispatcher().executorService(), brave.serverSpanThreadBinder());
	}

	protected BraveTracingInterceptor buildInterceptor() {
		Builder tracingBuilder = BraveTracingInterceptor.builder(brave);
		return tracingBuilder.build();
	}

	public Brave getBrave() {
		return brave;
	}
}
