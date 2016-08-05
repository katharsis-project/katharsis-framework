package io.katharsis.errorhandling.mapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class ExceptionMapperLookupFactory {

	public static ExceptionMapperLookup toExceptionMapperLookup(JsonApiExceptionMapper... mappers) {
		return new CollectionExceptionMapperLookup(new HashSet<JsonApiExceptionMapper>(Arrays.asList(mappers)));
	}

	private static class CollectionExceptionMapperLookup implements ExceptionMapperLookup {

		private Set<JsonApiExceptionMapper> set;

		private CollectionExceptionMapperLookup(Set<JsonApiExceptionMapper> set) {
			this.set = set;
		}

		@Override
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			return set;
		}
	}
}
