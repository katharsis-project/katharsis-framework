package io.katharsis.jpa.query;

import io.katharsis.jpa.model.TablePerClassBaseEntity;
import io.katharsis.jpa.model.TablePerClassChildEntity;

public class TablePerClassInhertitance extends AbstractInheritanceTest<TablePerClassBaseEntity, TablePerClassChildEntity> {

	public TablePerClassInhertitance() {
		super(TablePerClassBaseEntity.class, TablePerClassChildEntity.class);
	}

}
