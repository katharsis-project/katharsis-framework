package io.katharsis.example.springboot.simple.domain.repository;

import io.katharsis.example.springboot.simple.domain.model.Project;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.links.PagedLinksInformation;
import io.katharsis.resource.list.ResourceListBase;
import io.katharsis.resource.meta.PagedMetaInformation;

public interface ProjectRepository extends ResourceRepositoryV2<Project, Long> {

	public class ProjectListMeta implements PagedMetaInformation {

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

	public class ProjectListLinks implements PagedLinksInformation {

		private String first;

		private String last;

		private String next;

		private String prev;

		@Override
		public String getFirst() {
			return first;
		}

		@Override
		public void setFirst(String first) {
			this.first = first;
		}

		@Override
		public String getLast() {
			return last;
		}

		@Override
		public void setLast(String last) {
			this.last = last;
		}

		@Override
		public String getNext() {
			return next;
		}

		@Override
		public void setNext(String next) {
			this.next = next;
		}

		@Override
		public String getPrev() {
			return prev;
		}

		@Override
		public void setPrev(String prev) {
			this.prev = prev;
		}

	}

	public class ProjectList extends ResourceListBase<Project, ProjectListMeta, ProjectListLinks> {

	}

	@Override
	public ProjectList findAll(QuerySpec querySpec);
}
