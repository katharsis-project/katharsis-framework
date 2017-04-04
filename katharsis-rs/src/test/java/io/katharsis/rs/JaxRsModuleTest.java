package io.katharsis.rs;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.SecurityContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.repository.information.RepositoryAction;
import io.katharsis.repository.information.RepositoryAction.RepositoryActionType;
import io.katharsis.repository.information.RepositoryInformationBuilderContext;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.rs.internal.JaxrsModule;
import io.katharsis.rs.internal.JaxrsModule.JaxrsResourceRepositoryInformationBuilder;
import io.katharsis.rs.resource.model.Task;
import io.katharsis.utils.parser.TypeParser;

public class JaxRsModuleTest {

	private JaxrsResourceRepositoryInformationBuilder builder;

	private RepositoryInformationBuilderContext context;

	@Before
	public void setup() {
		final ModuleRegistry moduleRegistry = new ModuleRegistry();
		builder = new JaxrsResourceRepositoryInformationBuilder();
		final ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		resourceInformationBuilder.init(new DefaultResourceInformationBuilderContext(resourceInformationBuilder, moduleRegistry.getTypeParser()));
		context = new RepositoryInformationBuilderContext() {

			@Override
			public ResourceInformationBuilder getResourceInformationBuilder() {
				return resourceInformationBuilder;
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}
		};
	}

	@Test
	public void testGetter() {
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		JaxrsModule module = new JaxrsModule(securityContext);
		Assert.assertEquals("jaxrs", module.getModuleName());
	}

	@Test
	public void testActionDetection() {
		ResourceRepositoryInformation information = (ResourceRepositoryInformation) builder.build(ScheduleRepository.class,
				context);
		Map<String, RepositoryAction> actions = information.getActions();
		Assert.assertEquals(5, actions.size());
		RepositoryAction action = actions.get("repositoryAction");
		Assert.assertNotNull(actions.get("repositoryPostAction"));
		Assert.assertNotNull(actions.get("repositoryDeleteAction"));
		Assert.assertNotNull(actions.get("repositoryPutAction"));
		Assert.assertNull(actions.get("notAnAction"));
		Assert.assertNotNull(action);
		Assert.assertEquals("repositoryAction", action.getName());
		Assert.assertEquals(RepositoryActionType.REPOSITORY, action.getActionType());
		Assert.assertEquals(RepositoryActionType.RESOURCE, actions.get("resourceAction").getActionType());
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidRootPathRepository() {
		builder.build(InvalidRootPathRepository.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidIdPathRepository1() {
		builder.build(InvalidIdPathRepository1.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidIdPathRepository2() {
		builder.build(InvalidIdPathRepository2.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testPathToLongRepository() {
		builder.build(PathToLongRepository.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingPathRepository1() {
		builder.build(MissingPathRepository1.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingPathRepository2() {
		builder.build(MissingPathRepository2.class, context);
	}

	@Path("schedules")
	public interface ScheduleRepository extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("repositoryAction")
		public String repositoryAction(@QueryParam(value = "msg") String msg);

		@POST
		@Path("repositoryPostAction")
		public String repositoryPostAction();

		@DELETE
		@Path("repositoryDeleteAction")
		public String repositoryDeleteAction();

		@PUT
		@Path("/repositoryPutAction/")
		public String repositoryPutAction();

		@GET
		@Path("{id}/resourceAction")
		public String resourceAction(@PathParam("id") long id, @QueryParam(value = "msg") String msg);

	}

	@Path("schedules")
	public interface InvalidRootPathRepository extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("")
		public String resourceAction();

	}

	@Path("schedules")
	public interface MissingPathRepository1 extends ResourceRepositoryV2<Task, Long> {

		@GET
		public String resourceAction();

	}

	@Path("schedules")
	public interface MissingPathRepository2 extends ResourceRepositoryV2<Task, Long> {

		public String resourceAction(@PathParam("id") long id);

	}

	@Path("schedules")
	public interface PathToLongRepository extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("a/b/c")
		public String resourceAction();

	}

	@Path("schedules")
	public interface InvalidIdPathRepository1 extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("{something}/test")
		public String resourceAction();

	}

	@Path("schedules")
	public interface InvalidIdPathRepository2 extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("{id}")
		public String resourceAction();

	}

}
