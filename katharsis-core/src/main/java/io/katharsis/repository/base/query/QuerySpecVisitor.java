package io.katharsis.repository.base.query;

public interface QuerySpecVisitor {

	public boolean visit(QuerySpec querySpec);

	public void visit(FilterSpec filterSpec);

	public void visit(SortSpec sortSpec);

	public void visit(IncludeFieldSpec includeSpec);

	public void visit(IncludeRelationSpec querySpec);

	public void visit(Class<?> relatedType, QuerySpec querySpec);

	public void visitOffset(long longValue);

	public void visitLimit(long longValue);

}
