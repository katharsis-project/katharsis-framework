package io.katharsis.resource.paging;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.core.internal.query.QuerySpecAdapter;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.queryspec.AbstractQuerySpecTest;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.links.PagedLinksInformation;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.RegistryEntry;

public class PagedLinksInformationQuerySpecTest extends AbstractQuerySpecTest {

	private ResourceRepositoryAdapter<Task, Long> adapter;

	@Before
	public void setup() {
		TestPagedResourceRepository.clear();

		super.setup();
		RegistryEntry registryEntry = resourceRegistry.findEntry(Task.class);
		TestPagedResourceRepository repo = (TestPagedResourceRepository) registryEntry.getResourceRepository(null)
				.getResourceRepository();

		repo = Mockito.spy(repo);

		adapter = registryEntry.getResourceRepository(null);

		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		for (long i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(i);
			task.setName("myTask");
			adapter.create(task, queryAdapter);
		}

	}

	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=2", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=4", linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=2", linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=4", linksInformation.getNext());
	}

	@Test
	public void testPagingNoContents() throws InstantiationException, IllegalAccessException {
		TestPagedResourceRepository.clear();

		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		querySpec.setOffset(0L);
		querySpec.setLimit(2L);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertNull(linksInformation.getFirst());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertNull(linksInformation.getNext());
	}

	@Test
	public void testPagingFirst() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		querySpec.setOffset(0L);
		querySpec.setLimit(3L);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=3", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=3&page[offset]=3", linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=3&page[offset]=3", linksInformation.getNext());
	}

	@Test
	public void testPagingLast() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		querySpec.setOffset(4L);
		querySpec.setLimit(4L);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=4", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=4&page[offset]=4", linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks/?page[limit]=4", linksInformation.getFirst());
		Assert.assertNull(linksInformation.getNext());
	}
}
