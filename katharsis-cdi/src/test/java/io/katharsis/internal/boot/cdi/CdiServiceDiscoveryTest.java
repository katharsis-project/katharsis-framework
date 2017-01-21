package io.katharsis.internal.boot.cdi;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.katharsis.cdi.internal.CdiServiceDiscovery;
import io.katharsis.core.internal.boot.DefaultServiceDiscoveryFactory;
import io.katharsis.internal.boot.cdi.model.ProjectRepository;
import io.katharsis.internal.boot.cdi.model.TaskRepository;
import io.katharsis.legacy.repository.annotations.JsonApiResourceRepository;
import io.katharsis.module.ServiceDiscovery;
import io.katharsis.repository.Repository;

@RunWith(CdiTestRunner.class)
@ApplicationScoped
public class CdiServiceDiscoveryTest {

	@Test
	public void testFactory() {
		DefaultServiceDiscoveryFactory factory = new DefaultServiceDiscoveryFactory();
		ServiceDiscovery instance = factory.getInstance();
		Assert.assertNotNull(instance);
		Assert.assertEquals(CdiServiceDiscovery.class, instance.getClass());

		List<?> repositories = instance.getInstancesByType(Repository.class);
		Assert.assertEquals(1, repositories.size());
		Assert.assertTrue(repositories.get(0) instanceof ProjectRepository);

		repositories = instance.getInstancesByAnnotation(JsonApiResourceRepository.class);
		Assert.assertEquals(1, repositories.size());
		Assert.assertTrue(repositories.get(0) instanceof TaskRepository);
	}
}
