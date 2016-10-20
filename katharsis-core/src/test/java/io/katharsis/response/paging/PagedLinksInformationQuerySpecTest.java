package io.katharsis.response.paging;

import io.katharsis.queryspec.AbstractQuerySpecTest;
import io.katharsis.queryspec.OffsetBasedPagingSpec;
import io.katharsis.queryspec.PageBasedPagingSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PagedLinksInformationQuerySpecTest extends AbstractQuerySpecTest {

	private ResourceRepositoryAdapter<Task, Long> adapter;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		TestPagedResourceRepository.clear();

		super.setup();
		RegistryEntry<?> registryEntry = resourceRegistry.getEntry(Task.class);
		TestPagedResourceRepository repo = (TestPagedResourceRepository) registryEntry.getResourceRepository(null)
				.getResourceRepository();

		repo = Mockito.spy(repo);

		adapter = registryEntry.getResourceRepository(null);

		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		for (long i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(i);
			task.setName("myTask");
			adapter.save(task, queryAdapter);
		}

	}

	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class, new OffsetBasedPagingSpec(2, 2));
		QuerySpecAdapter specAdapter = new QuerySpecAdapter(querySpec, resourceRegistry);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(specAdapter).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=0", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=4", linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=0", linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=4", linksInformation.getNext());

		querySpec = new QuerySpec(Task.class, new PageBasedPagingSpec(1, 2));
		specAdapter = new QuerySpecAdapter(querySpec, resourceRegistry);

		linksInformation = (PagedLinksInformation) adapter.findAll(specAdapter).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks/?page[number]=0&page[size]=2", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[number]=2&page[size]=2", linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[number]=0&page[size]=2", linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[number]=2&page[size]=2", linksInformation.getNext());
	}

	@Test
	public void testPagingFirst() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class, new PageBasedPagingSpec(0, 3)), resourceRegistry);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks/?page[number]=0&page[size]=3", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[number]=1&page[size]=3", linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[number]=1&page[size]=3", linksInformation.getNext());
	}

	@Test
	public void testPagingLast() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class, new OffsetBasedPagingSpec(4, 4)), resourceRegistry);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=4&page[offset]=0", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=4&page[offset]=4", linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=4&page[offset]=0", linksInformation.getPrev());
		Assert.assertNull(linksInformation.getNext());
	}
}
