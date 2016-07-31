package io.katharsis.itests.registry;

import io.katharsis.dispatcher.registry.RepositoryRegistryImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.assertNotNull

class RepositoryRegistryImplTest {

    val tck = "io.katharsis.itests.tck";

    @Test
    fun testTwoRepositoriesAreFoundAndCreated() {
        val registry = RepositoryRegistryImpl.build(tck, "/api")

        assertThat(registry.adapters.size).isEqualTo(2);

        val tasks = registry.get("tasks");
        val projects = registry.get("project")

        assertNotNull(tasks)
        assertNotNull(projects)
    }

    @Test
    fun testTasksRepositoryHasFindOne() {
        val registry = RepositoryRegistryImpl.build(tck, "/api")

        val tasks = registry.get("tasks");
        assertNotNull(tasks)

    }
}



