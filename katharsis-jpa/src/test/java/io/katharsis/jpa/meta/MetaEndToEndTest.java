package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.jpa.AbstractJpaJerseyTest;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.resource.MetaJsonObject;
import io.katharsis.meta.model.resource.MetaResource;
import io.katharsis.meta.model.resource.MetaResourceBase;

public class MetaEndToEndTest extends AbstractJpaJerseyTest {

	@Override
	@Before
	public void setup() {
		super.setup();
	}

	@Test
	public void test() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource testMeta = lookup.getMeta(TestEntity.class, MetaResource.class);
		Assert.assertNotNull(testMeta);
		MetaDataObject superMeta = testMeta.getSuperType();
		Assert.assertEquals(MetaResourceBase.class, superMeta.getClass());
		
		MetaAttribute embAttrMeta = testMeta.getAttribute(TestEntity.ATTR_embValue);
		Assert.assertEquals(MetaJsonObject.class, embAttrMeta.getType().getClass());
	}
}
