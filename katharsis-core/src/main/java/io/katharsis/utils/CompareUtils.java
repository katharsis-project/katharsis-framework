package io.katharsis.utils;

public class CompareUtils {

	private CompareUtils() {
	}

	public static boolean isEquals(Object a, Object b) {
		return (a == b) || (a != null && a.equals(b));
	}
}
