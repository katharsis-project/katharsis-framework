package io.katharsis.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Path.Node;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.utils.StringUtils;
import io.katharsis.validation.mock.ComplexValidator;
import io.katharsis.validation.mock.models.Project;
import io.katharsis.validation.mock.models.ProjectData;
import io.katharsis.validation.mock.models.Task;

// TODO remo: root/leaf bean not yet available, Katharsis extensions required
public class ValidationTest extends AbstractValidationTest {

	@Test
	public void testPropertyNotNull() {
		Project project = new Project();
		project.setId(1L);
		project.setName(null); // violation
		try {
			projectRepo.save(project);
			Assert.fail();
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			Assert.assertEquals(1, violations.size());
			ConstraintViolation<?> violation = violations.iterator().next();
			Assert.assertEquals("{javax.validation.constraints.NotNull.message}", violation.getMessageTemplate());
			assertPath("name", violation.getPropertyPath());
		}
	}

	@Test
	public void testNestedPropertyNotNull() {
		ProjectData data = new ProjectData();
		data.setValue(null); // violation

		Project project = new Project();
		project.setId(1L);
		project.setName("test");
		project.setData(data);

		try {
			projectRepo.save(project);
			Assert.fail();
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			Assert.assertEquals(1, violations.size());
			ConstraintViolation<?> violation = violations.iterator().next();
			Assert.assertEquals("{javax.validation.constraints.NotNull.message}", violation.getMessageTemplate());
			assertPath("data.value", violation.getPropertyPath());
		}
	}

	@Test
	public void testResource() {
		Project project = new Project();
		project.setId(1L);
		project.setName(ComplexValidator.INVALID_NAME);
		try {
			projectRepo.save(project);
			Assert.fail();
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			Assert.assertEquals(1, violations.size());
			ConstraintViolation<?> violation = violations.iterator().next();
			Assert.assertEquals("{complex.message}", violation.getMessageTemplate());
			assertPath("", violation.getPropertyPath());
		}
	}

	@Test
	public void testPropertyOnRelation() {
		Task task = new Task();
		task.setId(1L);
		task.setName(null);
		taskRepo.save(task);

		Project project = new Project();
		project.setName("test");
		project.getTasks().add(task);

		try {
			projectRepo.save(project, buildIncludes("projects", "tasks"));
			Assert.fail();
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			Assert.assertEquals(1, violations.size());
			ConstraintViolation<?> violation = violations.iterator().next();
			Assert.assertEquals("{javax.validation.constraints.NotNull.message}", violation.getMessageTemplate());
			assertPath("name", violation.getPropertyPath());
		}
	}

	@Test
	public void testRelationProperty() {
		Task task = new Task();
		task.setId(1L);
		task.setName(ComplexValidator.INVALID_NAME);
		taskRepo.save(task);

		Project project = new Project();
		project.setName("test");
		project.setTask(task);

		try {
			projectRepo.save(project, buildIncludes("projects", "task"));
			Assert.fail();
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			Assert.assertEquals(1, violations.size());
			ConstraintViolation<?> violation = violations.iterator().next();
			Assert.assertEquals("{complex.message}", violation.getMessageTemplate());
			assertPath("", violation.getPropertyPath());
		}
	}

	private QueryParams buildIncludes(String root, String... relations) {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[" + root + "]", StringUtils.join(",", Arrays.asList(relations)));
		return queryParamsBuilder.buildQueryParams(params);
	}

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}

	private void assertPath(String expectedPath, Path propertyPath) {
		Iterator<Node> iterator = propertyPath.iterator();
		StringBuilder builder = new StringBuilder();
		while (iterator.hasNext()) {
			if (builder.length() > 0) {
				builder.append(".");
			}
			String name = iterator.next().getName();
			builder.append(name);
		}
		String actualPath = builder.toString();

		Assert.assertEquals(expectedPath, actualPath);
	}

}