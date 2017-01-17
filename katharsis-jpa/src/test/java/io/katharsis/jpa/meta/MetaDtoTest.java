package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.model.dto.TestDTO;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaKey;
import io.katharsis.meta.model.resource.MetaResource;
import io.katharsis.meta.provider.resource.ResourceMetaProvider;

public class MetaDtoTest {

	@Test
	public void testDtoMeta() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.addProvider(new ResourceMetaProvider());
		MetaResource meta = lookup.getMeta(TestDTO.class, MetaResource.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());

		MetaAttribute oneRelatedAttr = meta.getAttribute("oneRelatedValue");
		Assert.assertTrue(oneRelatedAttr.isAssociation());
	}

}
