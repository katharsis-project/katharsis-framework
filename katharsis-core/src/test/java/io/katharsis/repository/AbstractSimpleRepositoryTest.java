package io.katharsis.repository;

import org.junit.Test;

import io.katharsis.legacy.repository.AbstractSimpleRepository;
import io.katharsis.resource.mock.models.Task;

public class AbstractSimpleRepositoryTest {

	private AbstractSimpleRepository<Task, Long> repo = new AbstractSimpleRepository<Task, Long>() {
	};

	@Test(expected = UnsupportedOperationException.class)
	public void findOne() {
		repo.findOne(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findOneWithParams() {
		repo.findOne(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findAll() {
		repo.findAll(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findAllWithIds() {
		repo.findAll(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void save() {
		repo.save(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void delete() {
		repo.delete(null);
	}

}
