package io.katharsis.legacy.queryParams;

import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.legacy.queryParams.include.Inclusion;
import io.katharsis.legacy.queryParams.params.IncludedFieldsParams;
import io.katharsis.legacy.queryParams.params.IncludedRelationsParams;
import io.katharsis.legacy.queryParams.params.SortingParams;
import io.katharsis.queryspec.*;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

@SuppressWarnings("deprecation")
public class DefaultQueryParamsConverterTest extends AbstractQueryParamsTest {

    private QueryParams defaultQueryParamsWithOffset0() {
        QueryParams expected = queryParamsBuilder.buildQueryParams(new HashMap<String, Set<String>>());
        HashMap<RestrictedPaginationKeys, Integer> map = new HashMap<>();
        map.put(RestrictedPaginationKeys.offset, 0);
        expected.setPagination(map);
        return expected;
    }

    private QueryParams defaultQueryParamsWithOffset0(Map<String, Set<String>> params) {
        QueryParams expected = queryParamsBuilder.buildQueryParams(params);
        HashMap<RestrictedPaginationKeys, Integer> map = new HashMap<>();
        map.put(RestrictedPaginationKeys.offset, 0);
        expected.setPagination(map);
        return expected;
    }

    @Test
    public void testFindAll() throws InstantiationException, IllegalAccessException {
        QueryParams queryParams = converter.fromParams(Task.class, new QuerySpec(Task.class));
        Assert.assertEquals(defaultQueryParamsWithOffset0(), queryParams);
    }

    @Test
    public void testFindAllOrderByAsc() throws InstantiationException, IllegalAccessException {
        testFindAllOrder(true);
    }

    @Test
    public void testFindAllOrderByDesc() throws InstantiationException, IllegalAccessException {
        testFindAllOrder(false);
    }

    private void testFindAllOrder(boolean asc) throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "sort[tasks][name]", asc ? "asc" : "desc");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        Direction dir = asc ? Direction.ASC : Direction.DESC;
        inputSpec.addSort(new SortSpec(Collections.singletonList("name"), dir));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertEquals(expectedParams.getSorting(), actualParams.getSorting());

        Map<String, SortingParams> sortingParams = actualParams.getSorting().getParams();
        RestrictedSortingValues expectedDir = asc ? RestrictedSortingValues.asc : RestrictedSortingValues.desc;

        Assert.assertEquals(1, sortingParams.size());
        Assert.assertTrue(sortingParams.containsKey("tasks"));
        Assert.assertEquals(1, sortingParams.get("tasks").getParams().size());
        Assert.assertTrue("sorting by name", sortingParams.get("tasks").getParams().containsKey("name"));
        Assert.assertTrue("sorting name is " + dir.name(), sortingParams.get("tasks").getParams().containsValue
                (expectedDir));

        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testFilterString() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][name]", "test1");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Collections.singletonList("name"), FilterOperator.EQ, "test1"));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);
        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testFilterLong() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][id]", "12");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Collections.singletonList("id"), FilterOperator.EQ, 12L));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("id").contains("12"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);
        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testFilterMultipleSame() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][id]", "12", "20");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Collections.singletonList("id"), FilterOperator.EQ, setParam(12L, 20L)));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("id").contains("12"));
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("id").contains("20"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);

        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testFilterMultipleDifferent() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][id]", "12", "20");
        addParams(params, "filter[tasks][name]", "foo", "bar");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Collections.singletonList("id"), FilterOperator.EQ, setParam(12L, 20L)));
        inputSpec.addFilter(new FilterSpec(Collections.singletonList("name"), FilterOperator.EQ, setParam("foo", "bar")));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("id").contains("12"));
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("id").contains("20"));
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("name").contains("foo"));
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("name").contains("bar"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);

        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilterUnknownResource() throws InstantiationException, IllegalAccessException {
        QuerySpec inputSpec = new QuerySpec(String.class);
        inputSpec.addFilter(new FilterSpec(Collections.singletonList("id"), FilterOperator.EQ, setParam(12L, 20L)));
        converter.fromParams(Task.class, inputSpec);
    }

    @Test
    public void testNestedRelationFilter() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][project][name]", "myProject");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Arrays.asList("project", "name"), FilterOperator.EQ, "myProject"));

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("project.name").contains("myProject"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);
        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testNestedRelationFilterMultipleSame() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][project][name]", "myProject", "otherProject");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Arrays.asList("project", "name"), FilterOperator.EQ, setParam("myProject",
                "otherProject")));

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("project.name").contains("myProject"));
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("project.name").contains("otherProject"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);
        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testNestedRelationFilterMultipleDifferent() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][project][name]", "myProject", "otherProject");
        addParams(params, "filter[tasks][includedProject][name]", "includedName");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Arrays.asList("project", "name"), FilterOperator.EQ, setParam("myProject",
                "otherProject")));
        inputSpec.addFilter(new FilterSpec(Arrays.asList("includedProject", "name"), FilterOperator.EQ, "includedName"));

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("project.name").contains("myProject"));
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("project.name").contains("otherProject"));
        Assert.assertTrue("expected filter value exists", actualParams.getFilters().getParams().get("tasks").getParams()
                .get("includedProject.name").contains("includedName"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);

        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testNestedObjectFilter() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[projects][data][data]", "myData");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Project.class);
        inputSpec.addFilter(new FilterSpec(Arrays.asList("data", "data"), FilterOperator.EQ, "myData"));

        transitivityCheckProject(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Project.class, inputSpec);

        Assert.assertTrue("params has the nested data keys", actualParams.getFilters().getParams().get("projects")
                .getParams().containsKey("data.data"));
        Assert.assertTrue("params has the nested data value", actualParams.getFilters().getParams().get("projects")
                .getParams().get("data.data").contains("myData"));

        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);
        transitivityCheckProject(inputSpec, actualParams);
    }

    @Test
    public void testFilterNEQ() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][id][NEQ]", "12");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Collections.singletonList("id"), FilterOperator.NEQ, 12L));

        transitivityCheckTask(inputSpec, expectedParams);
        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);

        Assert.assertTrue("sub path with dotted operator exists", actualParams.getFilters().getParams().get("tasks")
                .getParams().containsKey("id.NEQ"));
        Assert.assertTrue("sub path with dotted operator value exists", actualParams.getFilters().getParams().get
                ("tasks").getParams().get("id.NEQ").contains("12"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);
        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testFilterLike() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "filter[tasks][name][GE]", "myTask");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addFilter(new FilterSpec(Collections.singletonList("name"), FilterOperator.GE, "myTask"));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Assert.assertTrue(actualParams.getFilters().getParams().get("tasks").getParams().containsKey("name.GE"));
        Assert.assertTrue(actualParams.getFilters().getParams().get("tasks").getParams().get("name.GE").contains
                ("myTask"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);
        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testIncludeField() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "fields[tasks]", "name");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.includeField(Collections.singletonList("name"));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Map<String, IncludedFieldsParams> includedFields = actualParams.getIncludedFields().getParams();

        Assert.assertEquals(1, includedFields.size());
        Assert.assertTrue(includedFields.containsKey("tasks"));
        Assert.assertEquals(1, includedFields.get("tasks").getParams().size());
        Assert.assertTrue(includedFields.get("tasks").getParams().contains("name"));

        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);

        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testIncludeFieldMultipleSame() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "fields[tasks]", "name", "category");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.includeField(Collections.singletonList("category"));
        inputSpec.includeField(Collections.singletonList("name"));

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Map<String, IncludedFieldsParams> includedFields = actualParams.getIncludedFields().getParams();

        Assert.assertEquals(1, includedFields.size());
        Assert.assertTrue(includedFields.containsKey("tasks"));
        Assert.assertEquals(2, includedFields.get("tasks").getParams().size());
        Assert.assertTrue(includedFields.get("tasks").getParams().contains("name"));
        Assert.assertTrue(includedFields.get("tasks").getParams().contains("category"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);

        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "include[tasks]", "project");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.includeRelation(Collections.singletonList("project"));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Map<String, IncludedRelationsParams> includedRelations = actualParams.getIncludedRelations().getParams();

        Assert.assertEquals(1, includedRelations.size());
        Assert.assertTrue(includedRelations.containsKey("tasks"));
        Assert.assertEquals(1, includedRelations.get("tasks").getParams().size());
        Assert.assertTrue(includedRelations.get("tasks").getParams().contains(new Inclusion("project")));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);

        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testIncludeRelationsMultipleSame() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "include[tasks]", "project", "projects");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.includeRelation(Collections.singletonList("project"));
        inputSpec.includeRelation(Collections.singletonList("projects"));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Map<String, IncludedRelationsParams> includedRelations = actualParams.getIncludedRelations().getParams();

        Assert.assertEquals(1, includedRelations.size());
        Assert.assertTrue(includedRelations.containsKey("tasks"));
        Assert.assertEquals(2, includedRelations.get("tasks").getParams().size());
        Assert.assertTrue(includedRelations.get("tasks").getParams().contains(new Inclusion("project")));
        Assert.assertTrue(includedRelations.get("tasks").getParams().contains(new Inclusion("projects")));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);

        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testPaging() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "page[offset]", "1");
        addParams(params, "page[limit]", "2");
        addParams(params, "sort[tasks][id]", "asc");
        QueryParams expectedParams = queryParamsBuilder.buildQueryParams(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.setLimit(2L);
        inputSpec.setOffset(1L);
        inputSpec.setSort(Collections.singletonList(new SortSpec(Collections.singletonList("id"), Direction.ASC)));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);
        Map<String, SortingParams> sortingParams = actualParams.getSorting().getParams();

        Assert.assertEquals(1, sortingParams.size());
        Assert.assertTrue(sortingParams.containsKey("tasks"));
        Assert.assertEquals(1, sortingParams.get("tasks").getParams().size());
        Assert.assertTrue("sorting by id", sortingParams.get("tasks").getParams().containsKey("id"));
        Assert.assertTrue("sorting id is ascending", sortingParams.get("tasks").getParams().containsValue
                (RestrictedSortingValues.asc));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);
        transitivityCheckTask(inputSpec, actualParams);
    }

    @Test
    public void testNestedSort() throws InstantiationException, IllegalAccessException {
        Map<String, Set<String>> params = new HashMap<>();
        addParams(params, "sort[tasks][project][name]", "asc");
        QueryParams expectedParams = defaultQueryParamsWithOffset0(params);

        QuerySpec inputSpec = new QuerySpec(Task.class);
        inputSpec.addSort(new SortSpec(Arrays.asList("project", "name"), Direction.ASC));

        transitivityCheckTask(inputSpec, expectedParams);

        QueryParams actualParams = converter.fromParams(Task.class, inputSpec);

        Map<String, SortingParams> sortingParamsMap = actualParams.getSorting().getParams();

        Assert.assertTrue("sort contains tasks at top level", sortingParamsMap.containsKey("tasks"));
        Map<String, RestrictedSortingValues> nestedPaths = sortingParamsMap.get("tasks").getParams();
        Assert.assertTrue("tasks sort contains sub project.name sort", nestedPaths.containsKey("project.name"));
        Assert.assertEquals("sub project.name sort is ascending", RestrictedSortingValues.asc, nestedPaths.get
                ("project.name"));
        Assert.assertEquals("expected params must be the same as the result", expectedParams, actualParams);

        transitivityCheckTask(inputSpec, actualParams);
    }

    private void transitivityCheckProject(QuerySpec expected, QueryParams base) {
        QuerySpec actual = paramsToSpecConverter.fromParams(Project.class, base);
        Assert.assertEquals("transitivity check", actual, expected);
    }

    private void transitivityCheckTask(QuerySpec expected, QueryParams base) {
        QuerySpec actual = paramsToSpecConverter.fromParams(Task.class, base);
        Assert.assertEquals("transitivity check", actual, expected);
    }

    private <T> Set<T> setParam(T... vars) {
        Set<T> set = new LinkedHashSet<>();
        Collections.addAll(set, vars);
        return set;
    }
}
