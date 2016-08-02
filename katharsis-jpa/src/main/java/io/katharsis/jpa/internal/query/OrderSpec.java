package io.katharsis.jpa.internal.query;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

public class OrderSpec implements Serializable {


	private static final long serialVersionUID = -3547744992729509448L;

	public enum Direction {
		ASC, DESC
	}

	private String path;

	private Direction direction;

	protected OrderSpec() {
	}

	public OrderSpec(String path, Direction direction) {
		if (path == null || path.length() == 0 || direction == null)
			throw new IllegalArgumentException("Parameters may not be empty");
		this.path = path;
		this.direction = direction;
	}

	public static OrderSpec asc(String expression) {
		return new OrderSpec(expression, Direction.ASC);
	}

	public static OrderSpec desc(String attributeName) {
		return new OrderSpec(attributeName, Direction.DESC);
	}

	public String getPath() {
		return path;
	}

	public Direction getDirection() {
		return direction;
	}

	public OrderSpec reverse() {
		return new OrderSpec(path, direction == Direction.ASC ? Direction.DESC : Direction.ASC);
	}

	@Override
	public int hashCode() {
		return 231 * (path.hashCode() + 37 * direction.hashCode());
	}

	@Override
	public boolean equals(Object o) {
		OrderSpec other;
		return this == o || (o instanceof OrderSpec && direction == ((other = (OrderSpec) o)).direction && path.equals(other.path));
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("[");
		b.append(path);
		b.append(' ');
		b.append(direction);
		b.append(']');
		return b.toString();
	}
}
