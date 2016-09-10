package io.katharsis.queryspec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.parser.ParserException;
import io.katharsis.utils.parser.TypeParser;

public class DefaultQuerySpecDeserializer implements QuerySpecDeserializer {

	private static final String OFFSET_PARAMETER = "offset";

	private static final String LIMIT_PARAMETER = "limit";

	private static final Pattern PARAMETER_PATTERN = Pattern.compile("(\\w+)(\\[(\\w+)\\])?([\\w\\[\\]]*)");

	private ResourceRegistry resourceRegistry;

	private TypeParser typeParser = new TypeParser();

	public DefaultQuerySpecDeserializer(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	@Override
	public QuerySpec deserialize(Class<?> rootResourceClass, Map<String, Set<String>> parameterMap) {

		QuerySpec rootQuerySpec = new QuerySpec(rootResourceClass);

		List<Parameter> parameters = parseParameters(parameterMap, rootResourceClass);
		for (Parameter parameter : parameters) {
			QuerySpec querySpec = rootQuerySpec.getOrCreateQuerySpec(parameter.resourceClass);
			switch (parameter.paramType) {
				case sort:
					deserializeSort(querySpec, parameter);
					break;
				case filter:
					deserializeFilter(querySpec, parameter);
					break;
				case include:
					deserializeIncludes(querySpec, parameter);
					break;
				case fields:
					deserializeFields(querySpec, parameter);
					break;
				case page:
					deserializePage(querySpec, parameter);
					break;
				default:
					throw new IllegalStateException(parameter.paramType.toString());
			}

		}

		return rootQuerySpec;
	}

	private void deserializeIncludes(QuerySpec querySpec, Parameter parameter) {
		if (parameter.name != null) {
			throw new ParserException("invalid parameter " + parameter);
		}

		for (String value : parameter.values) {
			List<String> attributePath = splitAttributePath(value, parameter);
			querySpec.includeRelation(attributePath);
		}
	}

	private void deserializeFields(QuerySpec querySpec, Parameter parameter) {
		if (parameter.name != null) {
			throw new ParserException("invalid parameter " + parameter);
		}

		for (String value : parameter.values) {
			List<String> attributePath = splitAttributePath(value, parameter);
			querySpec.includeField(attributePath);
		}
	}

	private void deserializePage(QuerySpec querySpec, Parameter parameter) {
		if (OFFSET_PARAMETER.equalsIgnoreCase(parameter.name)) {
			querySpec.setOffset(parameter.getLongValue());
		}
		else if (LIMIT_PARAMETER.equalsIgnoreCase(parameter.name)) {
			querySpec.setLimit(parameter.getLongValue());
		}
		else {
			throw new ParserException(parameter.toString());
		}
	}

	private void deserializeFilter(QuerySpec querySpec, Parameter parameter) {
		Set<FilterOperator> operators = parameter.repositoryAdapter.getSupportedOperators();

		List<String> attributePath = splitKeyPath(parameter.name, parameter);

		String lastPathElement = attributePath.get(attributePath.size() - 1);

		// find operation
		FilterOperator filterOp = null;
		for (FilterOperator op : operators) {
			if (op.getName().equalsIgnoreCase(lastPathElement)) {
				filterOp = op;
				attributePath = attributePath.subList(0, attributePath.size() - 1);
				break;
			}
		}
		if (filterOp == null) {
			filterOp = parameter.repositoryAdapter.getDefaultOperator();
		}

		Class<?> attributeType = PropertyUtils.getPropertyClass(querySpec.getResourceClass(), attributePath);
		Set<Object> typedValues = new HashSet<>();
		for (String stringValue : parameter.values) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Object value = typeParser.parse(stringValue, (Class) attributeType);
			typedValues.add(value);
		}
		Object value = typedValues.size() == 1 ? typedValues.iterator().next() : typedValues;

		querySpec.addFilter(new FilterSpec(attributePath, filterOp, value));
	}

	private void deserializeSort(QuerySpec querySpec, Parameter parameter) {
		if (parameter.name != null) {
			throw new ParserException("invalid parameter " + parameter);
		}

		for (String value : parameter.values) {
			boolean desc = value.startsWith("-");
			if (desc) {
				value = value.substring(1);
			}
			List<String> attributePath = splitAttributePath(value, parameter);
			Direction dir = desc ? Direction.DESC : Direction.ASC;
			querySpec.addSort(new SortSpec(attributePath, dir));
		}
	}

	private List<Parameter> parseParameters(Map<String, Set<String>> params, Class<?> rootResourceClass) {
		List<Parameter> list = new ArrayList<>();
		Set<Entry<String, Set<String>>> entrySet = params.entrySet();
		for (Entry<String, Set<String>> entry : entrySet) {

			Matcher m = PARAMETER_PATTERN.matcher(entry.getKey());
			boolean accepted = m.matches();
			if (!accepted) {
				throw new ParserException("failed to parse parameter " + entry.getKey());
			}

			String strParamType = m.group(1);
			String resourceType = m.group(3);
			String path = m.group(4);
			RegistryEntry<?> registryEntry = resourceType != null ? resourceRegistry.getEntry(resourceType) : null;

			Parameter param = new Parameter();
			param.fullKey = entry.getKey();
			param.paramType = RestrictedQueryParamsMembers.valueOf(strParamType.toLowerCase());
			param.values = entry.getValue();
			if (registryEntry == null) {
				registryEntry = resourceRegistry.getEntry(rootResourceClass);
				param.repositoryAdapter = registryEntry.getResourceRepository(null);
				param.resourceClass = rootResourceClass;
				param.name = emptyToNull(nullToEmpty(resourceType) + nullToEmpty(path));
			}
			else {
				param.repositoryAdapter = registryEntry.getResourceRepository(null);
				param.resourceClass = registryEntry.getResourceInformation().getResourceClass();
				param.name = emptyToNull(path);
			}
			list.add(param);
		}
		return list;
	}

	private static String emptyToNull(String value) {
		return value.length() != 0 ? value : null;
	}

	private static String nullToEmpty(String value) {
		return value.length() > 0 ? value : "";
	}

	class Parameter {

		public ResourceRepositoryAdapter<?, ?> repositoryAdapter;

		String fullKey;

		RestrictedQueryParamsMembers paramType;

		Class<?> resourceClass;

		String name;

		Set<String> values;

		public Long getLongValue() {
			if (values.size() != 1) {
				throw new ParserException("expected a Long for " + toString());
			}
			try {
				return Long.parseLong(values.iterator().next());
			}
			catch (NumberFormatException e) {
				throw new ParserException("expected a Long for " + toString());
			}
		}

		@Override
		public String toString() {
			return fullKey + "=" + values;
		}
	}

	private List<String> splitKeyPath(String pathString, Parameter param) {
		if (!pathString.startsWith("[") || !pathString.endsWith("]")) {
			throw new ParserException("invalid attribute path in " + param.toString());
		}
		String temp = pathString.substring(1, pathString.length() - 1);

		return Arrays.asList(temp.split("\\]\\["));
	}

	private List<String> splitAttributePath(String pathString, Parameter param) {
		return Arrays.asList(pathString.split("\\."));
	}
}
