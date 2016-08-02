package io.katharsis.jpa.query;

import io.katharsis.jpa.model.SingleTableBaseEntity;
import io.katharsis.jpa.model.SingleTableChildEntity;

public class SingleTableInheritanceTest extends AbstractInheritanceTest<SingleTableBaseEntity, SingleTableChildEntity> {

	public SingleTableInheritanceTest() {
		super(SingleTableBaseEntity.class, SingleTableChildEntity.class);
	}

}
