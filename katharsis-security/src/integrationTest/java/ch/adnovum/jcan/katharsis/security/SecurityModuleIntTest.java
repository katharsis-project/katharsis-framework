package ch.adnovum.jcan.katharsis.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.adnovum.jcan.ee7.deltaspike.resteasy.InMemoryIdentityManager;
import ch.adnovum.jcan.ee7.deltaspike.resteasy.ResteasyDeltaspikeRule;
import ch.adnovum.jcan.katharsis.mock.models.Project;
import ch.adnovum.jcan.katharsis.mock.models.Task;
import ch.adnovum.jcan.katharsis.mock.repository.ProjectRepository;
import ch.adnovum.jcan.katharsis.mock.repository.TaskRepository;
import ch.adnovum.jcan.katharsis.util.KatharsisNetworkUtil;
import io.katharsis.client.KatharsisClient;
import io.katharsis.client.QuerySpecRelationshipRepositoryStub;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.security.ForbiddenException;
import io.katharsis.security.UnauthorizedException;

@RunWith(CdiTestRunner.class)
@Singleton
public class SecurityModuleIntTest {

	private final InMemoryIdentityManager identityManager = new InMemoryIdentityManager();

	@Rule
	public ResteasyDeltaspikeRule resteasy = ResteasyDeltaspikeRule.builder().applicationClass(TestApplication.class)
			.useBasicAuthentication(identityManager).build();

	protected KatharsisClient client;

	protected QuerySpecResourceRepositoryStub<Task, Long> taskRepo;

	protected QuerySpecResourceRepositoryStub<Project, Long> projectRepo;

	protected QuerySpecRelationshipRepositoryStub<Task, Long, Project, Long> relRepo;

	@Inject
	private SecurityModule module;

	@After
	@Before
	public void cleanup() {
		module.setEnabled(true);
	}

	@Before
	public void setup() {
		identityManager.clear();

		client = new KatharsisClient(resteasy.getBaseUri().toString());
		client.addModule(SecurityModule.newClientModule());
		client.setPushAlways(false);
		client.getHttpAdapter().setReceiveTimeout(1000000, TimeUnit.MILLISECONDS);

		taskRepo = client.getQuerySpecRepository(Task.class);
		projectRepo = client.getQuerySpecRepository(Project.class);
		relRepo = client.getQuerySpecRepository(Task.class, Project.class);

		KatharsisNetworkUtil.setBasicAuthentication(client, "doe", "doePass");

		TaskRepository.clear();
		ProjectRepository.clear();
	}

	@Test
	public void metaAllPermissions() {
		identityManager.addUser("doe", "doePass".toCharArray(), "allRole");

		ResourceList<Project> list = projectRepo.findAll(new QuerySpec(Project.class));
		ResourcePermissionInformation metaInformation = list.getMeta(ResourcePermissionInformationImpl.class);
		ResourcePermission resourcePermission = metaInformation.getResourcePermission();
		Assert.assertEquals(ResourcePermission.ALL, resourcePermission);
	}

	@Test
	public void metaGetPatchPermissions() {
		identityManager.addUser("doe", "doePass".toCharArray());

		ResourceList<Project> list = projectRepo.findAll(new QuerySpec(Project.class));
		ResourcePermissionInformation metaInformation = list.getMeta(ResourcePermissionInformationImpl.class);
		ResourcePermission resourcePermission = metaInformation.getResourcePermission();
		Assert.assertEquals(ResourcePermission.GET.or(ResourcePermission.POST), resourcePermission);
	}

	@Test
	public void rootAll() {
		identityManager.addUser("doe", "doePass".toCharArray(), "allRole");

		Project project = new Project();
		project.setId(1L);
		project.setName("test");
		projectRepo.create(project);

		project.setName("updated");
		projectRepo.save(project);

		project = projectRepo.findOne(project.getId(), new QuerySpec(Project.class));
		Assert.assertNotNull(project);

		projectRepo.delete(project.getId());
	}

	@Test(expected = ForbiddenException.class)
	public void forbiddenPost() {
		identityManager.addUser("doe", "doePass".toCharArray(), "getRole");

		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);
	}

	@Test
	public void disableSecurityModule() {
		module.setEnabled(false);

		Assert.assertTrue(module.isAllowed(Project.class, ResourcePermission.ALL));
		Assert.assertTrue(module.isAllowed(Task.class, ResourcePermission.ALL));
		Assert.assertEquals(ResourcePermission.ALL, module.getResourcePermission(Task.class));
	}

	@Test(expected = IllegalStateException.class)
	public void noIsRolesAllowedWhenDisabled() {
		module.setEnabled(false);

		module.isUserInRole("whatever");
	}

	@Test
	public void getPostOnly() {
		identityManager.addUser("doe", "doePass".toCharArray(), "getRole", "postRole");

		Project project = new Project();
		project.setId(1L);
		project.setName("test");
		projectRepo.create(project);

		project = projectRepo.findOne(project.getId(), new QuerySpec(Project.class));
		Assert.assertNotNull(project);
	}

	@Test(expected = UnauthorizedException.class)
	public void unauthorizedException() {
		identityManager.addUser("otherUser", "doePass".toCharArray(), "allRole");

		Project project = new Project();
		project.setId(1L);
		project.setName("test");
		projectRepo.create(project);
	}

	@Test
	public void permitAllMatchAnyType() {
		identityManager.addUser("doe", "doePass".toCharArray());
		projectRepo.findAll(new QuerySpec(Project.class));
	}

	@Test
	public void permitAllMatchProjectType() {
		identityManager.addUser("doe", "doePass".toCharArray());
		Project project = new Project();
		project.setId(1L);
		project.setName("test");
		projectRepo.create(project);
	}

	@Test(expected = ForbiddenException.class)
	public void permitAllNoMatch() {
		identityManager.addUser("doe", "doePass".toCharArray());
		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);
	}

	@ApplicationPath("/")
	public static class TestApplication extends Application {

		@Override
		public Set<Class<?>> getClasses() {
			return new HashSet<>(Arrays.asList(KatharsisFeature.class));
		}
	}
}
