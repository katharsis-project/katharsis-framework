package io.katharsis.jpa.internal.meta;

import java.util.List;

public interface MetaKey extends MetaTypedElement {

	List<MetaAttribute> getElements();

	boolean isUnique();

	boolean isPrimaryKey();

	MetaAttribute getUniqueElement();

}
