package io.katharsis.jpa.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class TypeUtils {

	private TypeUtils() {
	}

	public static final ParameterizedType parameterize(final Class<?> raw, final Type... typeArguments) {
		final Type useOwner;
		if (raw.getEnclosingClass() == null) {
			useOwner = null;
		} else {
			useOwner = raw.getEnclosingClass();
		}
		return new ParameterizedTypeImpl(raw, useOwner, typeArguments);
	}

	private static final class ParameterizedTypeImpl implements ParameterizedType {
		private final Class<?> raw;
		private final Type useOwner;
		private final Type[] typeArguments;

		private ParameterizedTypeImpl(final Class<?> raw, final Type useOwner, final Type[] typeArguments) {
			this.raw = raw;
			this.useOwner = useOwner;
			this.typeArguments = typeArguments;
		}

		@Override
		public Type getRawType() {
			return raw;
		}

		@Override
		public Type getOwnerType() {
			return useOwner;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return typeArguments.clone();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((raw == null) ? 0 : raw.hashCode());
			result = prime * result + Arrays.hashCode(typeArguments);
			result = prime * result + ((useOwner == null) ? 0 : useOwner.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ParameterizedTypeImpl other = (ParameterizedTypeImpl) obj;
			if (raw == null) {
				if (other.raw != null)
					return false;
			} else if (!raw.equals(other.raw))
				return false;
			if (!Arrays.equals(typeArguments, other.typeArguments))
				return false;
			if (useOwner == null) {
				if (other.useOwner != null)
					return false;
			} else if (!useOwner.equals(other.useOwner))
				return false;
			return true;
		}
	}

}
