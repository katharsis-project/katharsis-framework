package io.katharsis.client;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapterListener;
import io.katharsis.client.mock.models.Task;
import io.katharsis.client.module.HttpAdapterAware;
import io.katharsis.module.Module;
import io.katharsis.module.Module.ModuleContext;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import okhttp3.OkHttpClient.Builder;

public class ModuleTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	private TestOkHttpAdapterListener adapterListener = Mockito.spy(new TestOkHttpAdapterListener());

	private OkHttpTestModule testModule = Mockito.spy(new OkHttpTestModule());

	@Before
	public void setup() {
		super.setup();
		client.addModule(testModule);
		taskRepo = client.getQuerySpecRepository(Task.class);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(true);
	}

	@Test
	public void test() {
		Task task = new Task();
		task.setId(1L);
		task.setName("task");
		taskRepo.create(task);

		List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertEquals(1, tasks.size());

		Mockito.verify(testModule, Mockito.times(1)).setupModule(Mockito.any(ModuleContext.class));
		Mockito.verify(testModule, Mockito.times(1)).setHttpAdapter(Mockito.eq(client.getHttpAdapter()));
		Mockito.verify(adapterListener, Mockito.times(1)).onBuild(Mockito.any(Builder.class));
	}

	class TestOkHttpAdapterListener implements OkHttpAdapterListener {

		@Override
		public void onBuild(Builder builder) {
			builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
		}
	}

	private class OkHttpTestModule implements Module, HttpAdapterAware {

		@Override
		public String getModuleName() {
			return "okhttp-test";
		}

		@Override
		public void setupModule(ModuleContext context) {
			// nothing to do
		}

		@Override
		public void setHttpAdapter(HttpAdapter adapter) {
			((OkHttpAdapter) adapter).addListener(adapterListener);
		}

	}

}