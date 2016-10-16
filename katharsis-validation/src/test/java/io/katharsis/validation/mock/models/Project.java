package io.katharsis.validation.mock.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.validation.mock.ComplexValid;

@JsonApiResource(type = "projects")
@ComplexValid
public class Project {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	private String description;

	@Valid
	private ProjectData data;

	@JsonApiToMany
	@Valid
	private List<Task> tasks = new ArrayList<>();

	@JsonApiToMany
	@Valid
	private Task task;

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
}
