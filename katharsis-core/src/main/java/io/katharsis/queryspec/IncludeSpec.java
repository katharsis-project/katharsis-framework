package io.katharsis.queryspec;

import java.io.Serializable;
import java.util.List;

import io.katharsis.core.internal.utils.StringUtils;

public abstract class IncludeSpec extends AbstractPathSpec implements Serializable {

	private static final long serialVersionUID = -2629584104921925080L;

	public IncludeSpec(List<String> path) {
		super(path);
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Parameters may not be empty");
		}
	}

	@Override
	public String toString() {
		return StringUtils.join(".", attributePath);
	}
}
