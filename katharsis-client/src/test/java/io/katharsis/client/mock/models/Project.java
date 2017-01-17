package io.katharsis.client.mock.models;

import java.util.ArrayList;
import java.util.List;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

@JsonApiResource(type = "projects")
public class Project {

	@JsonApiId
	private Long id;

	private String name;

	private String description;

	private ProjectData data;

	@JsonApiToMany
	private List<Task> tasks = new ArrayList<>();

	@JsonApiToMany
	private Task task;

	@JsonApiLinksInformation
	private ProjectLinks links;

	@JsonApiMetaInformation
	private ProjectMeta meta;

	public static class ProjectLinks implements LinksInformation {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class ProjectMeta implements MetaInformation {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public Long getId() {
		return id;
	}

	public Project setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(@SuppressWarnings("SameParameterValue") String description) {
		this.description = description;
	}

	public ProjectData getData() {
		return data;
	}

	public void setData(ProjectData data) {
		this.data = data;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public ProjectLinks getLinks() {
		return links;
	}

	public void setLinks(ProjectLinks links) {
		this.links = links;
	}

	public ProjectMeta getMeta() {
		return meta;
	}

	public void setMeta(ProjectMeta meta) {
		this.meta = meta;
	}
}
