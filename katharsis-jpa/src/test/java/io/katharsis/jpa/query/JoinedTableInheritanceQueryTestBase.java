package io.katharsis.jpa.query;

import org.junit.Ignore;
import org.junit.Test;

import io.katharsis.jpa.model.JoinedTableBaseEntity;
import io.katharsis.jpa.model.JoinedTableChildEntity;

public abstract class JoinedTableInheritanceQueryTestBase
		extends AbstractInheritanceTest<JoinedTableBaseEntity, JoinedTableChildEntity> {

	public JoinedTableInheritanceQueryTestBase() {
		super(JoinedTableBaseEntity.class, JoinedTableChildEntity.class);
	}

	@Override
	@Test
	@Ignore
	public void testOrderBySubtypeAttribute() {
		// NOTE those not work with JPA/Hibernate
	}
}
