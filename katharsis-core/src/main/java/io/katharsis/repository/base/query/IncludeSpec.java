package io.katharsis.repository.base.query;

import java.io.Serializable;
import java.util.List;

public abstract class IncludeSpec extends AbstractPathSpec implements Serializable {

	private static final long serialVersionUID = -2629584104921925080L;

	protected IncludeSpec() {
	}

	public IncludeSpec(List<String> path) {
		super(path);
		if (path == null || path.size() == 0)
			throw new IllegalArgumentException("Parameters may not be empty");
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("[");
		b.append(attributePath);
		b.append(']');
		return b.toString();
	}
}
