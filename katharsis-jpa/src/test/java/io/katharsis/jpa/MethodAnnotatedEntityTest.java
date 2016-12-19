package io.katharsis.jpa;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.model.MethodAnnotatedEntity;
import io.katharsis.queryspec.QuerySpec;

public class MethodAnnotatedEntityTest extends AbstractJpaJerseyTest {

	@Test
	public void testMeta() {
		MethodAnnotatedEntity entity = new MethodAnnotatedEntity();
		entity.setId(13L);
		entity.setStringValue("test");

		MetaLookup lookup = new MetaLookup();
		MetaEntity meta = lookup.getMeta(MethodAnnotatedEntity.class).asEntity();
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());

		MetaAttribute stringValueAttr = meta.getAttribute("stringValue");
		Assert.assertNotNull(stringValueAttr);
		Assert.assertEquals("stringValue", stringValueAttr.getName());
		Assert.assertEquals("test", stringValueAttr.getValue(entity));

		MetaAttribute idAttr = meta.getAttribute("id");
		Assert.assertNotNull(idAttr);
		Assert.assertEquals("id", idAttr.getName());
		Assert.assertEquals(13L, idAttr.getValue(entity));
		Assert.assertTrue(idAttr.isId());

	}

	@Test
	public void testMethodAnnotatedFields() {
		// tests whether JPA annotations on methods are supported as well
		QuerySpecResourceRepositoryStub<MethodAnnotatedEntity, Long> methodRepo = client
				.getQuerySpecRepository(MethodAnnotatedEntity.class);

		MethodAnnotatedEntity task = new MethodAnnotatedEntity();
		task.setId(1L);
		task.setStringValue("test");
		methodRepo.save(task);

		// check retrievable with findAll
		List<MethodAnnotatedEntity> list = methodRepo.findAll(new QuerySpec(MethodAnnotatedEntity.class));
		Assert.assertEquals(1, list.size());
		MethodAnnotatedEntity savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findAll(ids)
		list = methodRepo.findAll(Arrays.asList(1L), new QuerySpec(MethodAnnotatedEntity.class));
		Assert.assertEquals(1, list.size());
		savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findOne
		savedTask = methodRepo.findOne(1L, new QuerySpec(MethodAnnotatedEntity.class));
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());
	}
}
