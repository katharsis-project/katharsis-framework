package io.katharsis.queryspec.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.AbstractQuerySpecTest;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.RegistryEntry;

public class QuerySpecResourceRepositoryTest extends AbstractQuerySpecTest {

	@Test
	public void test() {

		RegistryEntry<?> registryEntry = resourceRegistry.getEntry(Task.class);
		TestQuerySpecResourceRepository repo = (TestQuerySpecResourceRepository) registryEntry
				.getResourceRepository(null).getResourceRepository();

		repo = Mockito.spy(repo);

		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "sort[tasks][name]", "asc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		repo.findOne(null, queryParams);
		Mockito.verify(repo, Mockito.times(1)).findOne(Mockito.anyLong(), Mockito.any(QuerySpec.class));

		repo.findAll(queryParams);
		Mockito.verify(repo, Mockito.times(1)).findAll(Mockito.any(QuerySpec.class));

		repo.findAll(null, queryParams);
		Mockito.verify(repo, Mockito.times(1)).findAll(Mockito.anyListOf(Long.class), Mockito.any(QuerySpec.class));
	}

}
