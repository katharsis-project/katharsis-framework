package io.katharsis.brave.mock.models;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;

@JsonApiResource(type = "projects")
public class Project {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	@JsonApiToMany(opposite = "project")
	private List<Task> tasks;

	public Project() {

	}

	public Project(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
