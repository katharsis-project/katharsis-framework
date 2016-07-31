package io.katharsis.itests.tck

import org.springframework.data.annotation.Id
import org.springframework.data.keyvalue.annotation.KeySpace
import org.springframework.data.repository.CrudRepository

/**
 * We keep our domain here. No JSON API or web related stuff. Just data.
 */
@KeySpace("task")
data class Task(
        @Id
        var uuid: String,
        var task: String,
        var project: Project?
)

@KeySpace("project")
data class Project(
        @Id
        var uuid: String,
        var name: String,
        var tasks: List<Task>
)

interface TaskRepository : CrudRepository<Task, String>
interface ProjectRepository : CrudRepository<Project, String>