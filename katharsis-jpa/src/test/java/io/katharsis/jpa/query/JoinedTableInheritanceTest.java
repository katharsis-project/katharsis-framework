package io.katharsis.jpa.query;

import org.junit.Ignore;
import org.junit.Test;

import io.katharsis.jpa.model.JoinedTableBaseEntity;
import io.katharsis.jpa.model.JoinedTableChildEntity;

public class JoinedTableInheritanceTest extends AbstractInheritanceTest<JoinedTableBaseEntity, JoinedTableChildEntity> {

	public JoinedTableInheritanceTest() {
		super(JoinedTableBaseEntity.class, JoinedTableChildEntity.class);
	}

	@Test
	@Ignore
	public void testOrderBySubtypeAttribute() {
		// NOTE those not work with JPA/Hibernate
	}
}
