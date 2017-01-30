package io.katharsis.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.mock.models.Project;
import io.katharsis.client.mock.models.Task;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.information.ResourceInformation;

public class QuerySpecUnknownAttributeClientTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	protected RelationshipRepositoryV2<Task, Long, Project, Long> relRepo;

	private DefaultQuerySpecDeserializer deserializer;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getQuerySpecRepository(Task.class);
		projectRepo = client.getQuerySpecRepository(Project.class);
		relRepo = client.getQuerySpecRepository(Task.class, Project.class);
	}

	@Override
	protected TestApplication configure() {
		TestApplication app = new TestApplication(true);
		deserializer = (DefaultQuerySpecDeserializer) app.getFeature().getQuerySpecDeserializer();
		deserializer.setAllowUnknownAttributes(true);
		return app;
	}

	@Test
	public void testCall() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("unknownAttr"), Direction.ASC));
		querySpec.addFilter(new FilterSpec(Arrays.asList("unknownAttr"), FilterOperator.EQ, "test"));
		List<Task> tasks = taskRepo.findAll(querySpec);
		Assert.assertEquals(0, tasks.size()); // no matches naturally...
	}

	@Test
	public void testDeserialize() {
		Map<String, Set<String>> parameterMap = new HashMap<>();
		parameterMap.put("filter[unknownAttr]", Collections.singleton("test"));
		parameterMap.put("sort", Collections.singleton("-unknownAttr"));
		ResourceInformation taskInformation = client.getRegistry().getEntryForClass(Task.class).getResourceInformation();
		QuerySpec querySpec = deserializer.deserialize(taskInformation, parameterMap);
		Assert.assertEquals(1, querySpec.getFilters().size());
		FilterSpec filterSpec = querySpec.getFilters().get(0);
		Assert.assertEquals("unknownAttr", filterSpec.getAttributePath().get(0));
		Assert.assertEquals(FilterOperator.EQ, filterSpec.getOperator());
		Assert.assertEquals("test", filterSpec.getValue());

		Assert.assertEquals(1, querySpec.getSort().size());
		SortSpec sortSpec = querySpec.getSort().get(0);
		Assert.assertEquals("unknownAttr", sortSpec.getAttributePath().get(0));
		Assert.assertEquals(Direction.DESC, sortSpec.getDirection());
	}

}