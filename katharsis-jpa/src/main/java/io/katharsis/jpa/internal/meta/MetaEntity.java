package io.katharsis.jpa.internal.meta;

public interface MetaEntity extends MetaDataObject {

	@Override
	MetaAttribute getVersionAttribute();

}
