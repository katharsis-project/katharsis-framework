package io.katharsis.repository.base.query;

import java.util.List;

/**
 * Applies the given QuerySpec to the provided list in memory. Result available
 * with getResult();
 */
public class InMemoryEvaluator<T> implements QuerySpecVisitor {

	public InMemoryEvaluator(List<T> resources, QuerySpec spec) {

	}

	public List<T> getResult() {
		return null;
	}

	@Override
	public boolean visit(QuerySpec querySpec) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void visit(FilterSpec filterSpec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SortSpec sortSpec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IncludeFieldSpec includeSpec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IncludeRelationSpec querySpec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Class<?> relatedType, QuerySpec querySpec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitOffset(long longValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLimit(long longValue) {
		// TODO Auto-generated method stub

	}

}
