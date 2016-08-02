package io.katharsis.jpa.internal;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.util.KatharsisAssert;

public class JpaRepositoryUtils {

	/**
	 * Gets the primary key attribute of the given entity. Assumes a primary key is available and no compound primary
	 * keys are supported.
	 */
	public static MetaAttribute getPrimaryKeyAttr(MetaEntity meta) {
		MetaKey primaryKey = meta.getPrimaryKey();
		KatharsisAssert.assertNotNull(primaryKey);
		KatharsisAssert.assertEquals(1, primaryKey.getElements().size());
		MetaAttribute attr = primaryKey.getElements().get(0);
		return attr;
	}

}
