package io.katharsis.queryspec;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.params.IncludedFieldsParams;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.repository.information.internal.ResourceRepositoryInformationImpl;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

public class QuerySpecAdapterTest {

	@Test
	public void test() {
		ResourceRegistry resourceRegistry = new ResourceRegistry(new ModuleRegistry(), new ConstantServiceUrlProvider("http://localhost"));
		resourceRegistry.addEntry(Task.class,
				new RegistryEntry(new ResourceRepositoryInformationImpl(Task.class, "tasks", new ResourceInformation(Task.class, "tasks", null)), null, null));

		QuerySpec spec = new QuerySpec(Task.class);
		spec.includeField(Arrays.asList("test"));
		spec.includeRelation(Arrays.asList("relation"));
		QuerySpecAdapter adapter = new QuerySpecAdapter(spec, resourceRegistry);
		Assert.assertEquals(Task.class, adapter.getResourceClass());
		Assert.assertEquals(spec, adapter.getQuerySpec());

		TypedParams<IncludedFieldsParams> includedFields = adapter.getIncludedFields();
		IncludedFieldsParams includedFieldsParams = includedFields.getParams().get("tasks");
		Assert.assertEquals(1, includedFieldsParams.getParams().size());
		Assert.assertEquals("test", includedFieldsParams.getParams().iterator().next());
		TypedParams<IncludedRelationsParams> includedRelations = adapter.getIncludedRelations();
		IncludedRelationsParams includedRelationsParams = includedRelations.getParams().get("tasks");
		Assert.assertEquals(1, includedRelationsParams.getParams().size());
		Assert.assertEquals("relation", includedRelationsParams.getParams().iterator().next().getPath());
	}
}
