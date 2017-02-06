package io.katharsis.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.meta.mock.model.Schedule;
import io.katharsis.meta.mock.model.ScheduleRepository.ScheduleListLinks;
import io.katharsis.meta.mock.model.ScheduleRepository.ScheduleListMeta;
import io.katharsis.meta.mock.model.Task;
import io.katharsis.meta.mock.model.Task.TaskLinksInformation;
import io.katharsis.meta.mock.model.Task.TaskMetaInformation;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaKey;
import io.katharsis.meta.model.resource.MetaResource;
import io.katharsis.meta.model.resource.MetaResourceAction;
import io.katharsis.meta.model.resource.MetaResourceAction.MetaRepositoryActionType;
import io.katharsis.meta.model.resource.MetaResourceField;
import io.katharsis.meta.model.resource.MetaResourceRepository;
import io.katharsis.meta.provider.MetaProvider;
import io.katharsis.meta.provider.resource.ResourceMetaProvider;
import io.katharsis.resource.registry.ResourceRegistryAware;

public class ResourceMetaProviderTest extends AbstractMetaTest {

	private MetaLookup lookup;

	@Before
	public void setup() {
		super.setup();

		ResourceMetaProvider provider = new ResourceMetaProvider();

		lookup = new MetaLookup();
		lookup.addProvider(provider);
		lookup.putIdMapping("io.katharsis.meta.mock.model", "app");

		for (MetaProvider p : lookup.getProviders()) {
			if (p instanceof ResourceRegistryAware) {
				((ResourceRegistryAware) p).setResourceRegistry(boot.getResourceRegistry());
			}
		}

		lookup.initialize();
	}

	@Test
	public void testPrimaryKey() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);

		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull("id", primaryKey.getName());
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertSame(primaryKey.getElements().get(0), meta.getAttribute("id"));
		Assert.assertTrue(meta.getPrimaryKey().isUnique());
	}

	@Test
	public void testResourceProperties() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);

		Assert.assertEquals("schedules", meta.getResourceType());
		Assert.assertEquals("Schedule", meta.getName());
		Assert.assertEquals("app.Schedule", meta.getId());

		Assert.assertEquals(Schedule.class, meta.getImplementationClass());
		Assert.assertEquals(Schedule.class, meta.getImplementationType());
		Assert.assertNull(meta.getParent());
		Assert.assertTrue(meta.getSubTypes().isEmpty());
	}

	@Test
	public void testLinksAttribute() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("linksInformation");
		Assert.assertEquals("linksInformation", attr.getName());
		Assert.assertEquals("app.Task.linksInformation", attr.getId());
		Assert.assertFalse(attr.isLazy());
		Assert.assertFalse(attr.isMeta());
		Assert.assertTrue(attr.isLinks());
		Assert.assertNull(attr.getOppositeAttribute());
		Assert.assertEquals(TaskLinksInformation.class, attr.getType().getImplementationClass());
	}

	@Test
	public void testMetaAttribute() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("metaInformation");
		Assert.assertEquals("metaInformation", attr.getName());
		Assert.assertEquals("app.Task.metaInformation", attr.getId());
		Assert.assertFalse(attr.isLazy());
		Assert.assertTrue(attr.isMeta());
		Assert.assertFalse(attr.isLinks());
		Assert.assertNull(attr.getOppositeAttribute());
		Assert.assertEquals(TaskMetaInformation.class, attr.getType().getImplementationClass());
	}

	@Test
	public void testSingleValuedRelation() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("schedule");
		Assert.assertEquals("schedule", attr.getName());
		Assert.assertEquals("app.Task.schedule", attr.getId());
		Assert.assertFalse(attr.isLazy());
		Assert.assertFalse(attr.isMeta());
		Assert.assertFalse(attr.isLinks());
		Assert.assertTrue(attr.isAssociation());
		Assert.assertNotNull(attr.getOppositeAttribute());
		Assert.assertNotNull("tasks", attr.getOppositeAttribute().getName());
		Assert.assertEquals(Schedule.class, attr.getType().getImplementationClass());
	}

	@Test
	public void testMultiValuedRelation() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("tasks");
		Assert.assertEquals("tasks", attr.getName());
		Assert.assertEquals("app.Schedule.tasks", attr.getId());
		Assert.assertTrue(attr.isLazy());
		Assert.assertFalse(attr.isMeta());
		Assert.assertFalse(attr.isLinks());
		Assert.assertTrue(attr.isAssociation());
		Assert.assertNotNull(attr.getOppositeAttribute());
		Assert.assertNotNull("tasks", attr.getOppositeAttribute().getName());
		Assert.assertEquals(List.class, attr.getType().getImplementationClass());
		Assert.assertEquals(Task.class, attr.getType().getElementType().getImplementationClass());
	}

	@Test
	public void testRepository() {
		MetaResource resourceMeta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaResourceRepository meta = (MetaResourceRepository) lookup.getMetaById().get(resourceMeta.getId() + "Repository");
		Assert.assertEquals(resourceMeta, meta.getResourceType());
		Assert.assertNotNull(meta.getListLinksType());
		Assert.assertNotNull(meta.getListMetaType());
		Assert.assertEquals(ScheduleListLinks.class, meta.getListLinksType().getImplementationClass());
		Assert.assertEquals(ScheduleListMeta.class, meta.getListMetaType().getImplementationClass());

		List<MetaElement> children = new ArrayList<>(meta.getChildren());
		Collections.sort(children, new Comparator<MetaElement>() {

			@Override
			public int compare(MetaElement o1, MetaElement o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		Assert.assertEquals(2, children.size());

		MetaResourceAction repositoryActionMeta = (MetaResourceAction) children.get(0);
		Assert.assertEquals("repositoryAction", repositoryActionMeta.getName());
		Assert.assertEquals(MetaRepositoryActionType.REPOSITORY, repositoryActionMeta.getActionType());
		MetaResourceAction resourceActionMeta = (MetaResourceAction) children.get(1);
		Assert.assertEquals("resourceAction", resourceActionMeta.getName());
		Assert.assertEquals(MetaRepositoryActionType.RESOURCE, resourceActionMeta.getActionType());

	}

}
