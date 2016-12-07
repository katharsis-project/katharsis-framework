package io.katharsis.response.paging;

public class DefaultPagedMetaInformation implements PagedMetaInformation {

	private Long totalResourceCount;

	@Override
	public Long getTotalResourceCount() {
		return totalResourceCount;
	}

	@Override
	public void setTotalResourceCount(Long totalResourceCount) {
		this.totalResourceCount = totalResourceCount;
	}
}
