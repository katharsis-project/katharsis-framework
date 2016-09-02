package io.katharsis.jpa.query;

import io.katharsis.jpa.model.SingleTableBaseEntity;
import io.katharsis.jpa.model.SingleTableChildEntity;

public abstract class SingleTableInheritanceQueryTestBase
		extends AbstractInheritanceTest<SingleTableBaseEntity, SingleTableChildEntity> {

	public SingleTableInheritanceQueryTestBase() {
		super(SingleTableBaseEntity.class, SingleTableChildEntity.class);
	}

}
