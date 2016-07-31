package io.katharsis.itests

import io.katharsis.domain.SingleResponse
import io.katharsis.itests.tck.Project
import io.katharsis.itests.tck.Task
import io.katharsis.itests.tck.TaskResource
import io.katharsis.itests.tck.from
import io.katharsis.request.Request
import io.katharsis.request.dto.DataBody
import io.katharsis.request.dto.RequestBody
import io.katharsis.request.dto.ResourceRelationships
import io.katharsis.request.path.JsonApiPath
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@RunWith(value = SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(IntegrationConfig::class))
class RoutingIntegrationTest() : KatharsisIntegrationSupport() {

    @Before
    fun setUp() {
        taskRepository.deleteAll()
        projectRepository.deleteAll()
    }

    @Test
    fun testGetCollectionReturnsEmptyResponse() {
        val path = JsonApiPath.parsePathFromStringUrl("http://domain/tasks")

        val req = Request(path, "GET", null, paramProvider)
        var res = requestDispatcher.handle(req)

        assertNotNull(res)
        assertEquals(200, res.httpStatus)
    }

    @Test
    fun testGetSingleResourceReturnsTheResource() {
        val task = Task(UUID.randomUUID().toString(), "single-task", null)
        taskRepository.save(task);

        val path = JsonApiPath.parsePathFromStringUrl("http://domain/tasks/${task.uuid}")

        val req = Request(path, "GET", null, paramProvider)
        var res = requestDispatcher.handle(req)

        assertNotNull(res)
        assertEquals(200, res.httpStatus)

        val taskRes = from(task)
        assertEquals(taskRes, (res.getDocument() as SingleResponse).data)
    }

    // http://jsonapi.org/format/#fetching-relationships
    @Test
    fun testGetResourceRelationshipShouldReturnResourceLinkage() {
        val project = Project(UUID.randomUUID().toString(), "sample-project", ArrayList())
        val task = Task(UUID.randomUUID().toString(), "another-task", project)
        projectRepository.save(project)
        taskRepository.save(task)

        val path = JsonApiPath.parsePathFromStringUrl("http://domain/tasks/${task.uuid}/relationships/project")

        val req = Request(path, "GET", null, paramProvider)
        var res = requestDispatcher.handle(req)

        assertNotNull(res)
        assertEquals(200, res.httpStatus)

//        val taskRes = from(task)
//        assertEquals(taskRes, res.response.entity)
    }

    @Test
    @Ignore
    fun testCreateNewResourceCreatesAResource() {
        val taskRes = TaskResource(null, "created-task", null)
        val attributes = objectMapper.createObjectNode().put("task", "created-task");
        val body = RequestBody(DataBody(null, "task", ResourceRelationships(), attributes))

        val path = JsonApiPath.parsePathFromStringUrl("http://domain/tasks")

        val req = Request(path, "POST", serialize(body), paramProvider)
        var res = requestDispatcher.handle(req)

        assertNotNull(res)
        assertEquals(201, res.httpStatus)

//        assertEquals(taskRes, res.response.entity)

    }
}