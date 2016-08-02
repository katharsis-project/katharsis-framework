package io.katharsis.jpa.query;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.FilterOperator;
import io.katharsis.jpa.internal.query.QueryBuilder;
import io.katharsis.jpa.internal.query.OrderSpec.Direction;

@Transactional
public abstract class AbstractInheritanceTest<B, C> extends AbstractJpaTest {

	private Class<B> baseClass;
	private Class<C> childClass;

	protected AbstractInheritanceTest(Class<B> baseClass, Class<C> childClass) {
		this.baseClass = baseClass;
		this.childClass = childClass;
	}

	private QueryBuilder<B> baseBuilder() {
		return factory.newBuilder(baseClass);
	}

	@Test
	public void testMeta() {
		MetaEntity baseMeta = MetaLookup.INSTANCE.getMeta(baseClass).asEntity();
		MetaEntity childMeta = MetaLookup.INSTANCE.getMeta(childClass).asEntity();
		Assert.assertSame(baseMeta, childMeta.getSuperType());

		Assert.assertEquals(1, childMeta.getDeclaredAttributes().size());
		Assert.assertEquals(2, baseMeta.getAttributes().size());
		Assert.assertEquals(3, childMeta.getAttributes().size());

		Assert.assertNotNull(baseMeta.getAttribute("id"));
		Assert.assertNotNull(baseMeta.getAttribute("stringValue"));
		Assert.assertNull(baseMeta.getAttribute("intValue"));

		Assert.assertNotNull(childMeta.getAttribute("id"));
		Assert.assertNotNull(childMeta.getAttribute("stringValue"));
		Assert.assertNotNull(childMeta.getAttribute("intValue"));
	}

	@Test
	public void testAll() {
		assertEquals(10, baseBuilder().buildExecutor().getResultList().size());
	}

	@Test
	public void testFilterBySubtypeAttribute() {
		// FIXME subtype lookup
		MetaLookup.INSTANCE.getMeta(childClass).asEntity();

		assertEquals(1, baseBuilder().addFilter("intValue", FilterOperator.EQUAL, 2).buildExecutor().getResultList().size());
		assertEquals(3, baseBuilder().addFilter("intValue", FilterOperator.GREATER, 1).buildExecutor().getResultList().size());
	}

	@Test
	public void testOrderBySubtypeAttribute() {
		// FIXME subtype lookup
		MetaLookup.INSTANCE.getMeta(childClass).asEntity();

		List<B> list = baseBuilder().addOrderBy(Direction.DESC, "intValue").buildExecutor().getResultList();
		Assert.assertEquals(10, list.size());
		for (int i = 0; i < 10; i++) {
			B entity = list.get(i);
			MetaEntity meta = MetaLookup.INSTANCE.getMeta(entity.getClass()).asEntity();

			if (i < 5) {
				Assert.assertTrue(childClass.isInstance(entity));
				Assert.assertEquals(4 - i, meta.getAttribute("intValue").getValue(entity));
			} else {
				Assert.assertFalse(childClass.isInstance(entity));

				// order by primary key by default second order criteria
				Assert.assertEquals(Long.valueOf(i - 5), meta.getAttribute("id").getValue(entity));
			}
		}
	}

}
