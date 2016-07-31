package io.katharsis.itests

import io.katharsis.itests.tck.Task
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(value = SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(IntegrationConfig::class))
class SpringDataRepositoriesTest() : KatharsisIntegrationSupport() {

    @Test
    fun makeSureRepositoriesWork() {
        var originalTask = Task(UUID.randomUUID().toString(), "aaaa", null)
        taskRepository.save(originalTask);

        val result = taskRepository.findOne(originalTask.uuid)
        assertNotNull(result);

        assertEquals(originalTask, result)
        assertEquals("aaaa", result.task)
    }

}
