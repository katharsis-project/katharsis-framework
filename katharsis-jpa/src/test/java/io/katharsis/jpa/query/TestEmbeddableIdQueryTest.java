package io.katharsis.jpa.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.internal.query.QueryBuilder;
import io.katharsis.jpa.model.TestEmbeddedIdEntity;

@Transactional
public class TestEmbeddableIdQueryTest extends AbstractJpaTest {

	private QueryBuilder<TestEmbeddedIdEntity> builder() {
		return factory.newBuilder(TestEmbeddedIdEntity.class);
	}

	@Test
	public void testAll() {
		assertEquals(5, builder().buildExecutor().getResultList().size());
	}

}
