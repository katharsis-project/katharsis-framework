package io.katharsis.queryspec.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.AbstractQuerySpecTest;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.RegistryEntry;

public class QuerySpecRelationshipRepositoryTest extends AbstractQuerySpecTest {

	@Test
	public void test() {
		RegistryEntry<?> registryEntry = resourceRegistry.getEntry(Task.class);
		TestQuerySpecRelationshipRepository repo = (TestQuerySpecRelationshipRepository) registryEntry
				.getRelationshipRepositoryForClass(Project.class, null).getRelationshipRepository();

		repo = Mockito.spy(repo);

		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "sort[projects][name]", "asc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		repo.findManyTargets(null, null, queryParams);
		Mockito.verify(repo, Mockito.times(1)).findManyTargets(Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(QuerySpec.class));

		repo.findOneTarget(null, null, queryParams);
		Mockito.verify(repo, Mockito.times(1)).findOneTarget(Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(QuerySpec.class));
	}
}
