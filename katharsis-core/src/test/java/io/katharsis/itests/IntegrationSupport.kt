package io.katharsis.itests

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Throwables
import io.katharsis.dispatcher.JsonApiDispatcherImpl
import io.katharsis.dispatcher.handlers.JsonApiDelete
import io.katharsis.dispatcher.handlers.JsonApiGet
import io.katharsis.dispatcher.handlers.JsonApiPatch
import io.katharsis.dispatcher.handlers.JsonApiPost
import io.katharsis.dispatcher.registry.RepositoryRegistryImpl
import io.katharsis.dispatcher.registry.api.RepositoryRegistry
import io.katharsis.itests.tck.ProjectRepository
import io.katharsis.itests.tck.TaskRepository
import io.katharsis.jackson.JsonApiModuleBuilder
import io.katharsis.repository.RepositoryParameterProvider
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.map.repository.config.EnableMapRepositories
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets

@RunWith(value = SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(IntegrationConfig::class))
open class KatharsisIntegrationSupport {

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var requestDispatcher: JsonApiDispatcherImpl

    @Autowired
    lateinit var paramProvider: ParamProvider


    @Autowired
    lateinit var objectMapper: ObjectMapper

    fun serialize(obj: Any?): InputStream {
        try {
            val body = objectMapper.writeValueAsString(obj)
            return ByteArrayInputStream(body?.toByteArray(StandardCharsets.UTF_8));
        } catch (e: JsonProcessingException) {
            throw Throwables.propagate(e);
        }
    }

}

@Configuration
@EnableMapRepositories("io.katharsis.itests")
open class IntegrationConfig {

    @Autowired
    lateinit var context: ApplicationContext ;

    @Bean
    open fun resourceRegistry(): RepositoryRegistry {
        val resourceRegistry = RepositoryRegistryImpl.build("io.katharsis.itests.tck", "")
        return resourceRegistry
    }

    @Bean
    open fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper();
        mapper.registerModule(JsonApiModuleBuilder.create())
        return mapper;
    }

    @Bean
    open fun paramProvider(): RepositoryParameterProvider {
        return ParamProvider(context)
    }

    @Bean
    @Autowired
    open fun requestDispatcher(objectMapper: ObjectMapper, registry: RepositoryRegistry): JsonApiDispatcherImpl {
//        exceptionMapperRegistry, resourceRegistry,        TypeParser(), objectMapper, QueryParamsBuilder(DefaultQueryParamsParser())
        return JsonApiDispatcherImpl(JsonApiGet(registry), JsonApiPost(registry), JsonApiPatch(registry), JsonApiDelete(registry));
    }
}

class ParamProvider(val context: ApplicationContext) : RepositoryParameterProvider {

    override fun <T> provide(method: Method?, parameterIndex: Int): T {
        val aClass = method!!.getParameterTypes()[parameterIndex]
        val bean = context.getBean(aClass);

        return bean as T;
    }

}
