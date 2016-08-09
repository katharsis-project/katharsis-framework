package io.katharsis.jpa.internal.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MetaAttributePath implements Iterable<MetaAttribute> {

	public static final char PATH_SEPARATOR_CHAR = '.';
	public static final String PATH_SEPARATOR = ".";
	public static final MetaAttributePath EMPTY_PATH = new MetaAttributePath();

	private MetaAttribute[] pathElements;

	public MetaAttributePath(List<? extends MetaAttribute> pathElements) {
		this(pathElements.toArray(new MetaAttribute[pathElements.size()]));
	}

	public MetaAttributePath(MetaAttribute... pathElements) {
		if (pathElements == null) {
			throw new IllegalArgumentException("pathElements must not be null.");
		}
		// if (pathElements.length == 0) {
		// throw new IllegalArgumentException("pathElements must not be empty.");
		// }
		this.pathElements = pathElements;

		checkArray(pathElements);
	}

	public MetaAttributePath subPath(int startIndex) {
		MetaAttribute[] range = Arrays.copyOfRange(pathElements, startIndex, pathElements.length);
		return new MetaAttributePath(range);
	}
	
	public MetaAttributePath subPath(int startIndex, int endIndex) {
		MetaAttribute[] range = Arrays.copyOfRange(pathElements, startIndex, endIndex);
		return new MetaAttributePath(range);
	}

	protected void checkArray(MetaAttribute[] pathElements) {

	}

	protected MetaAttribute[] newArray(int length) {
		return new MetaAttribute[length];
	}

	protected MetaAttributePath to(MetaAttribute... pathElements) {
		return new MetaAttributePath(pathElements);
	}

	public boolean hasTail() {
		return pathElements.length > 1;
	}

	public MetaAttribute getHead() {
		return pathElements[0];
	}

	public int length() {
		return pathElements.length;
	}

	public MetaAttribute getElement(int index) {
		return pathElements[index];
	}

	public MetaAttributePath getTail() {
		int tailLength = pathElements.length - 1;
		MetaAttribute[] tail = newArray(tailLength);
		System.arraycopy(pathElements, 1, tail, 0, tailLength);
		return to(tail);
	}

	public MetaAttribute getLast() {
		if (pathElements != null && pathElements.length > 0) {
			return pathElements[pathElements.length - 1];
		}
		return null;
	}

	public MetaAttributePath concat(MetaAttributePath path2) {
		ArrayList<MetaAttribute> list = new ArrayList<MetaAttribute>();
		list.addAll(Arrays.asList(this.pathElements));
		list.addAll(Arrays.asList(path2.pathElements));
		return to(list.toArray(newArray(0)));
	}

	public MetaAttributePath concat(String... pathElements) {
		if (pathElements.length == 0)
			throw new IllegalStateException("cannot concat paths for empty path");
		MetaDataObject currentType = getLast().getType().asDataObject();
		MetaAttribute[] attrs = new MetaAttribute[pathElements.length];
		for (int i = 0; i < attrs.length; i++) {
			if (currentType == null)
				throw new IllegalArgumentException("cannot concat " + this + " with " + Arrays.toString(pathElements));
			attrs[i] = currentType.getAttribute(pathElements[i]);
			if (i < attrs.length - 1) {
				currentType = attrs[i].getType().asDataObject();
			}
		}
		return concat(attrs);
	}

	public MetaAttributePath concat(MetaAttribute... pathElements) {
		ArrayList<MetaAttribute> list = new ArrayList<MetaAttribute>();
		list.addAll(Arrays.asList(this.pathElements));
		list.addAll(Arrays.asList(pathElements));
		return to(list.toArray(newArray(0)));
	}

	public boolean isSubPath(MetaAttributePath path) {
		if (path.pathElements.length > pathElements.length) {
			return false;
		}

		int counter = 0;
		for (MetaAttribute element : path.pathElements) {
			if (!element.equals(pathElements[counter])) {
				return false;
			}
			counter++;
		}
		return true;
	}

//	public IReadOnlyDataObject getAttributeOwner(IReadOnlyDataObject po) {
//		if (po != null) {
//			MetaAttributePath path = this;
//			while (path.hasTail()) {
//				IMetaAttribute head = path.getHead();
//				po = (IReadOnlyDataObject) po._get(head);
//				path = path.getTail();
//				if (po == null) {
//					break;
//				}
//			}
//			return po;
//		}
//		return null;
//	}

	public String render(String delimiter) {
		if (pathElements.length == 0) {
			return "";
		} else if (pathElements.length == 1) {
			return pathElements[0].getName();
		} else {
			StringBuilder builder = new StringBuilder(pathElements[0].getName());
			for (int i = 1; i < pathElements.length; i++) {
				builder.append(delimiter);
				builder.append(pathElements[i].getName());
			}
			return builder.toString();
		}
	}

	public String toString() {
		return render(PATH_SEPARATOR);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(pathElements);
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
		MetaAttributePath other = (MetaAttributePath) obj;
		if (!Arrays.equals(pathElements, other.pathElements))
			return false;
		return true;
	}

	@Override
	public Iterator<MetaAttribute> iterator() {
		return Collections.unmodifiableList(Arrays.asList(pathElements)).iterator();
	}

}
