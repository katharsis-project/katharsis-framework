package io.katharsis.meta;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.core.internal.boot.KatharsisBoot;
import io.katharsis.core.internal.boot.ReflectionsServiceDiscovery;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.resource.MetaResource;
import io.katharsis.meta.provider.resource.ResourceMetaProvider;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;

public class MetaMetaTest {

	private MetaLookup lookup;

	@Before
	public void setup() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.katharsis.meta.mock.model", new SampleJsonServiceLocator()));
		MetaModule module = MetaModule.create();
		module.addMetaProvider(new ResourceMetaProvider());
		boot.addModule(module);
		boot.boot();

		lookup = module.getLookup();
	}

	@Test
	public void testAttributesProperlyDeclaredAndNotInherited() {
		MetaResource elementMeta = lookup.getMeta(MetaElement.class, MetaResource.class);
		MetaResource dataMeta = lookup.getMeta(MetaDataObject.class, MetaResource.class);

		Assert.assertSame(elementMeta.getAttribute("id"), dataMeta.getAttribute("id"));
		Assert.assertSame(elementMeta.getPrimaryKey(), dataMeta.getPrimaryKey());
	}

	@Test
	public void testMetaDataObjectMeta() {
		MetaResource meta = lookup.getMeta(MetaDataObject.class, MetaResource.class);

		MetaAttribute elementTypeAttr = meta.getAttribute("elementType");
		Assert.assertNotNull(elementTypeAttr);
		Assert.assertNotNull(elementTypeAttr.getType());
		Assert.assertEquals("io.katharsis.meta.MetaType.elementType", elementTypeAttr.getId());

		MetaAttribute attrsAttr = meta.getAttribute("attributes");
		Assert.assertNotNull(attrsAttr.getType());

		MetaAttribute childrenAttr = meta.getAttribute("children");
		Assert.assertEquals("io.katharsis.meta.MetaElement.children", childrenAttr.getId());
		Assert.assertEquals("io.katharsis.meta.MetaElement$List", childrenAttr.getType().getId());
	}
}
