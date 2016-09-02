package io.katharsis.jpa.query;

import io.katharsis.jpa.model.TablePerClassBaseEntity;
import io.katharsis.jpa.model.TablePerClassChildEntity;

public abstract class TablePerClassInhertitanceQueryTestBase extends AbstractInheritanceTest<TablePerClassBaseEntity, TablePerClassChildEntity> {

	public TablePerClassInhertitanceQueryTestBase() {
		super(TablePerClassBaseEntity.class, TablePerClassChildEntity.class);
	}

}
