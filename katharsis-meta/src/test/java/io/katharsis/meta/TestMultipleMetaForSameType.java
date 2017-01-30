package io.katharsis.meta;

import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.meta.mock.model.Schedule;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.resource.MetaResource;
import io.katharsis.meta.provider.MetaProviderBase;
import io.katharsis.meta.provider.MetaProviderContext;
import io.katharsis.meta.provider.resource.ResourceMetaProvider;

/**
 * e.g. an entity can be both a resource and a entity.
 */
public class TestMultipleMetaForSameType extends AbstractMetaTest {

	private MetaLookup lookup;

	@Before
	public void setup() {
		super.setup();

		ResourceMetaProvider provider = new ResourceMetaProvider();
		//provider.setResourceRegistry(boot.getResourceRegistry());

		lookup = new MetaLookup();
		lookup.addProvider(provider);
		lookup.addProvider(new DummyMetaProvider());

		// this will triger to avoid reading the JSON API annotations and treat it as a dummy POJO
		lookup.putIdMapping("io.katharsis.meta.mock.model", MetaDummyDataObject.class, "app.dummy");

		lookup.putIdMapping("io.katharsis.meta.mock.model", MetaResource.class, "app.resource");
		lookup.initialize();
	}

	@Test
	public void testAsResource() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		Assert.assertNotNull(meta);
		Assert.assertEquals(MetaResource.class, meta.getClass());
		Assert.assertNotNull(meta.getPrimaryKey());
		Assert.assertEquals("app.resource.Schedule", meta.getId());
	}

	@Test
	public void testAsDummyDataObject() {
		MetaDummyDataObject meta = lookup.getMeta(Schedule.class, MetaDummyDataObject.class);
		Assert.assertNotNull(meta);
		Assert.assertEquals(MetaDummyDataObject.class, meta.getClass());
		Assert.assertNull(meta.getPrimaryKey());
		Assert.assertEquals("app.dummy.Schedule", meta.getId());
	}

	@Test
	public void testBothConcurrently() {
		MetaDummyDataObject metaDataObject = lookup.getMeta(Schedule.class, MetaDummyDataObject.class);
		MetaResource metaResource = lookup.getMeta(Schedule.class, MetaResource.class);

		Assert.assertEquals(MetaResource.class, metaResource.getClass());
		Assert.assertEquals(MetaDummyDataObject.class, metaDataObject.getClass());
	}

	public static class MetaDummyDataObject extends MetaDataObject {

	}

	public class DummyMetaProvider extends MetaProviderBase {

		@Override
		public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
			return metaClass == MetaDummyDataObject.class;
		}

		@Override
		public MetaElement createElement(Type type, MetaProviderContext context) {
			Class<?> rawType = ClassUtils.getRawType(type);
			MetaDummyDataObject dummy = new MetaDummyDataObject();
			dummy.setName(rawType.getSimpleName());
			dummy.setImplementationType(type);
			return dummy;
		}

	}

}
