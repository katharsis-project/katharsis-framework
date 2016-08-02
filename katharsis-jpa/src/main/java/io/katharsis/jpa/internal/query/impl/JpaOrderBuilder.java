package io.katharsis.jpa.internal.query.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.query.AnyTypeObject;
import io.katharsis.jpa.internal.query.OrderSpec;
import io.katharsis.jpa.internal.query.OrderSpec.Direction;
import io.katharsis.jpa.internal.query.impl.QueryBuilderImpl.QueryBuilderContext;

public abstract class JpaOrderBuilder<T> {

	private QueryBuilderContext<T> ctx;
	private QueryBuilderImpl<T> builder;

	public JpaOrderBuilder(QueryBuilderImpl<T> builder, QueryBuilderContext<T> ctx) {
		this.ctx = ctx;
		this.builder = builder;
	}

	protected void applyOrderSpec() {
		List<OrderSpec> orderSpecs = builder.getOrderSpecs();
		if (!orderSpecs.isEmpty()) {
			ctx.criteriaQuery.orderBy(orderSpecListToOrderArray());
		}

		// ensure a total order, add primary key if necessary necssary
		if (builder.getEnsureTotalOrder() && !QueryUtil.hasTotalOrder(builder.getMeta(), orderSpecs)) {
			List<Order> totalOrderList = new ArrayList<Order>(ctx.criteriaQuery.getOrderList());
			MetaKey primaryKey = builder.getMeta().getPrimaryKey();
			if (primaryKey != null) {
				for (MetaAttribute primaryKeyElem : primaryKey.getElements()) {
					Order primaryKeyElemOrder = ctx.cb.asc(ctx.root.get(primaryKeyElem.getName()));
					totalOrderList.add(primaryKeyElemOrder);
				}
			}
			ctx.criteriaQuery.orderBy(totalOrderList);
		}
	}

	protected Order[] orderSpecListToOrderArray() {
		ArrayList<Order> orderList = new ArrayList<Order>();
		for (OrderSpec orderSpec : builder.getOrderSpecs()) {
			orderList.addAll(orderSpecToOrder(orderSpec));
		}
		Order[] orderArray = orderList.toArray(new Order[orderList.size()]);
		return orderArray;
	}

	private List<Order> orderSpecToOrder(OrderSpec orderSpec) {
		String attrPathString = orderSpec.getPath();
		List<Order> orders = new ArrayList<Order>();

		// check for AnyType
		MetaAttributePath path = builder.getMeta().resolvePath(attrPathString, true);
		MetaAttribute attr = path.getLast();
		MetaType valueType = attr.getType();
		if (valueType instanceof MetaMapType) {
			valueType = ((MetaMapType) valueType).getValueType();
		}
		boolean anyType = AnyTypeObject.class.isAssignableFrom(valueType.getImplementationClass());
		if (anyType) {
			// order by anything, if types are not mixed for a given key, it will be ok
			MetaDataObject anyMeta = valueType.asDataObject();
			for (MetaAttribute anyAttr : anyMeta.getAttributes()) {
				if (!anyAttr.isDerived()){
					orders.add(newOrder(attrPathString + "." + anyAttr.getName(), orderSpec.getDirection()));
				}
			}
		} else {
			orders.add(newOrder(attrPathString, orderSpec.getDirection()));
		}
		return orders;
	}

	private Order newOrder(String attrPath, Direction direction) {
		Expression<?> expr = getAttribute(builder.getMeta().resolvePath(attrPath));
		if (direction == Direction.ASC) {
			return ctx.cb.asc(expr);
		} else if (direction == Direction.DESC) {
			return ctx.cb.desc(expr);
		} else {
			throw new IllegalArgumentException("Invalid order direction <" + direction + ">");
		}
	}

	protected abstract Expression<?> getAttribute(MetaAttributePath attrPath);

}
