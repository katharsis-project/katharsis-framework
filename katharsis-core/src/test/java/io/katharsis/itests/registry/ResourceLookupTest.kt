package io.katharsis.itests.registry

import io.katharsis.dispatcher.registry.DefaultResourceLookup
import io.katharsis.errorhandling.exception.KatharsisInitializationException
import io.katharsis.itests.registry.fixtures.simple.Task
import io.katharsis.itests.registry.fixtures.simple.TaskRestRepo
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ResourceLookupTest {

    val goodFixtures = arrayOf("io.katharsis.itests.registry.fixtures.simple");
    val repoWithoutResourceFixtures = arrayOf("io.katharsis.itests.registry.fixtures.noresource");
    val repoWithRelationships = arrayOf("io.katharsis.itests.registry.fixtures.relationships");

    @Test
    fun testResourceDiscovery() {
        val registry = DefaultResourceLookup();
        val resources: Map<String, Any> = registry.scan(goodFixtures).getResources();

        assertFalse(resources.isEmpty())

        assertTrue(resources.containsKey("tasks"))
        assertEquals(Task::class.toString(), resources.get("tasks").toString(), "Classes do not match")
    }

    @Test
    fun testRepositoryDiscovery() {
        val registry = DefaultResourceLookup();
        val repos: Map<String, Any> = registry.scan(goodFixtures).getRepositories();

        assertFalse(repos.isEmpty())

        assertTrue(repos.containsKey("tasks"))
        assertEquals(TaskRestRepo::class.toString(), repos.get("tasks").toString(), "Classes do not match")
    }

    @Test
    fun testResourceDiscoveryForRepositoryWithoutResource() {
        try {
            DefaultResourceLookup().scan(repoWithoutResourceFixtures);
        } catch (e: KatharsisInitializationException) {
            val msg = e.message
            if (msg != null) {
                assertTrue(msg.contains("Required annotation interface io.katharsis.resource.annotations.JsonApiResource is missing from class java.lang.Object", true))
            }

        }
    }

    @Test
    fun testRelationshipRepostiroiesAreFound() {
        val registry = DefaultResourceLookup();
        val repos: Map<String, Map<String, Any>> = registry.scan(repoWithRelationships).getRelationships()

        assertFalse(repos.isEmpty())

        assertTrue(repos.containsKey("projects"))
        assertEquals(io.katharsis.itests.registry.fixtures.relationships.Task::class.toString(), repos.get("projects")?.get("tasks").toString(), "Classes do not match")
    }
}
