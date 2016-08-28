package io.katharsis.repository.base.query;

public class DefaultQueryVisitor implements QuerySpecVisitor {

	@Override
	public boolean visit(QuerySpec querySpec) {
		return true;
	}

	@Override
	public void visit(FilterSpec filterSpec) {
	}

	@Override
	public void visit(SortSpec sortSpec) {
	}

	@Override
	public void visit(IncludeFieldSpec includeSpec) {
	}

	@Override
	public void visit(IncludeRelationSpec querySpec) {
	}

	@Override
	public void visit(Class<?> relatedType, QuerySpec querySpec) {
	}

	@Override
	public void visitOffset(long longValue) {
	}

	@Override
	public void visitLimit(long longValue) {
	}
}
