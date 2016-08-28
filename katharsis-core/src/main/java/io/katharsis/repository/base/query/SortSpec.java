package io.katharsis.repository.base.query;

import java.io.Serializable;
import java.util.List;

public class SortSpec extends AbstractPathSpec implements Serializable {

	private static final long serialVersionUID = -3547744992729509448L;

	private Direction direction;

	protected SortSpec() {
	}

	public SortSpec(List<String> path, Direction direction) {
		super(path);
		if (path == null || path.size() == 0 || direction == null)
			throw new IllegalArgumentException("Parameters may not be empty");
		this.direction = direction;
	}

	public static SortSpec asc(List<String> expression) {
		return new SortSpec(expression, Direction.ASC);
	}

	public static SortSpec desc(List<String> attributeName) {
		return new SortSpec(attributeName, Direction.DESC);
	}

	public Direction getDirection() {
		return direction;
	}

	public SortSpec reverse() {
		return new SortSpec(attributePath, direction == Direction.ASC ? Direction.DESC : Direction.ASC);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		return super.hashCode() | result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SortSpec other = (SortSpec) obj;
		if (direction != other.direction)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("[");
		b.append(attributePath);
		b.append(' ');
		b.append(direction);
		b.append(']');
		return b.toString();
	}
}
