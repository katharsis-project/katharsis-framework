package io.katharsis.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.mock.models.Project;
import io.katharsis.client.mock.models.Project.ProjectLinks;
import io.katharsis.client.mock.models.Project.ProjectMeta;
import io.katharsis.client.mock.models.Task;
import io.katharsis.client.mock.repository.ProjectRepository.ProjectsLinksInformation;
import io.katharsis.client.mock.repository.ProjectRepository.ProjectsMetaInformation;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.ResourceList;

public class InformationClientTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getQuerySpecRepository(Task.class);
		projectRepo = client.getQuerySpecRepository(Project.class);

		Project project = new Project();
		project.setId(14L);
		projectRepo.create(project);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(false);
	}

	@Test
	public void testMeta() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		ProjectsMetaInformation metaInformation = list.getMeta(ProjectsMetaInformation.class);
		Assert.assertEquals("testMeta", metaInformation.getMetaValue());
		Project project = list.get(0);
		ProjectMeta projectMeta = project.getMeta();
		Assert.assertNotNull(projectMeta);
		Assert.assertEquals("someMetaValue", projectMeta.getValue());
	}

	@Test
	public void testLinks() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		ProjectsLinksInformation lnksInformation = list.getLinks(ProjectsLinksInformation.class);
		Assert.assertEquals("testLink", lnksInformation.getLinkValue());
		Project project = list.get(0);
		ProjectLinks projectLinks = project.getLinks();
		Assert.assertNotNull(projectLinks);
		Assert.assertEquals("someLinkValue", projectLinks.getValue());
	}
}