package io.katharsis.jpa.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.model.TestEmbeddedIdEntity;

@Transactional
public abstract class EmbeddableIdQueryTestBase extends AbstractJpaTest {

	private JpaQuery<TestEmbeddedIdEntity> builder() {
		return queryFactory.query(TestEmbeddedIdEntity.class);
	}

	@Test
	public void testAll() {
		assertEquals(5, builder().buildExecutor().getResultList().size());
	}

}
