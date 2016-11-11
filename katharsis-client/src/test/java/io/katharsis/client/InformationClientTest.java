package io.katharsis.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.mock.models.Project;
import io.katharsis.client.mock.models.Task;
import io.katharsis.client.mock.repository.ProjectRepository.ProjectsLinksInformation;
import io.katharsis.client.mock.repository.ProjectRepository.ProjectsMetaInformation;
import io.katharsis.client.response.ResourceList;
import io.katharsis.queryspec.QuerySpec;

public class InformationClientTest extends AbstractClientTest {

	protected QuerySpecResourceRepositoryStub<Task, Long> taskRepo;

	protected QuerySpecResourceRepositoryStub<Project, Long> projectRepo;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getQuerySpecRepository(Task.class);
		projectRepo = client.getQuerySpecRepository(Project.class);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(false);
	}

	@Test
	public void testMeta() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		ProjectsMetaInformation metaInformation = list.getMetaInformation(ProjectsMetaInformation.class);
		Assert.assertEquals("testMeta", metaInformation.getMetaValue());
	}

	@Test
	public void testLinks() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		ProjectsLinksInformation lnksInformation = list.getLinksInformation(ProjectsLinksInformation.class);
		Assert.assertEquals("testLink", lnksInformation.getLinkValue());
	}
}