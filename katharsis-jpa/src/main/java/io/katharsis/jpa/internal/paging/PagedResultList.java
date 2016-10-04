package io.katharsis.jpa.internal.paging;

import java.util.List;

import io.katharsis.utils.WrappedList;

/**
 * Use this class as return type and provide the total number of (potentially filtered) 
 */
public class PagedResultList<T> extends WrappedList<T> {

	private Long totalCount;

	private String relationshipField;

	private Object relationshipSourceId;

	private Class<?> relationshipSourceClass;

	public PagedResultList(List<T> list, Long totalCount) {
		this(list, totalCount, null, null, null);
	}

	public PagedResultList(List<T> list, Long totalCount, Class<?> relationshipSourceClass, Object relationshipSourceId,
			String relationshipField) {
		super(list);
		this.totalCount = totalCount;
		this.relationshipSourceClass = relationshipSourceClass;
		this.relationshipField = relationshipField;
		this.relationshipSourceId = relationshipSourceId;
	}

	public String getRelationshipField() {
		return relationshipField;
	}

	public Class<?> getRelationshipSourceClass() {
		return relationshipSourceClass;
	}

	public Object getRelationshipSourceId() {
		return relationshipSourceId;
	}

	public Long getTotalCount() {
		return totalCount;
	}
}
