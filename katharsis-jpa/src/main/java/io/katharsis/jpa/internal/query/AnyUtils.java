package io.katharsis.jpa.internal.query;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.query.AnyTypeObject;

public class AnyUtils {

	/**
	 * Sets the value of the given anytype.
	 * 
	 * @param dataObject
	 *            the anytype for which the value is set.
	 * @param value
	 *            the new value
	 * @return return the attributename of the newly set attribute.
	 */
	public static void setValue(MetaLookup metaLookup, AnyTypeObject dataObject, Object value) {
		MetaDataObject meta = metaLookup.getMeta(dataObject.getClass()).asDataObject();
		if (value == null) {
			for (MetaAttribute attr : meta.getAttributes()) {
				attr.setValue(dataObject, null);
			}
		} else {
			for (MetaAttribute attr : meta.getAttributes()) {
				if (attr.getType().getImplementationClass().isAssignableFrom(value.getClass())) {
					attr.setValue(dataObject, value);
					return;
				}
			}
			throw new IllegalArgumentException("cannot assign " + value);
		}
	}

	/**
	 * Gets the meta attribute for a dataobject.
	 * 
	 * @param dataObject
	 *            the data object
	 * @param attributeName
	 *            the attribute name
	 * @return the meta attribute or <code>null</code> if attributeName is
	 *         <code>null</code>.
	 */
	private static MetaAttribute getMetaAttribute(MetaLookup metaLookup, AnyTypeObject dataObject,
			String attributeName) {
		if (attributeName == null)
			return null;
		MetaDataObject meta = metaLookup.getMeta(dataObject.getClass()).asDataObject();
		return meta.getAttribute(attributeName);
	}

	/**
	 * Gets the java type name for the given attribute.
	 * 
	 * @param dataObject
	 *            the data object
	 * @param attributeName
	 *            the attribute name
	 * @return the java type name of the attribute or <code>null</code> if
	 *         attributeName is <code>null</code>.
	 */
	public static String getType(MetaLookup metaLookup, AnyTypeObject dataObject, String attributeName) {
		MetaAttribute attr = getMetaAttribute(metaLookup, dataObject, attributeName);
		if (attr == null)
			return null;
		return attr.getType().getImplementationClass().getName();
	}

	/**
	 * Gets the value of the given attribute.
	 * 
	 * @param dataObject
	 *            the data object
	 * @param attributeName
	 *            the attribute name
	 * @return the value of the attribute or <code>null</code> if attributeName
	 *         is <code>null</code>.
	 */
	public static Object getValue(MetaLookup metaLookup, AnyTypeObject dataObject, String attributeName) {
		MetaAttribute attr = getMetaAttribute(metaLookup, dataObject, attributeName);
		if (attr == null)
			return null;
		return attr.getValue(dataObject);
	}

	/**
	 * Finds a matching attribute for a given value.
	 * 
	 * @param meta
	 *            the metadataobject
	 * @param value
	 *            the value
	 * @return the attribute which will accept the given value
	 * @throw IllegalArgumentException if value is <code>null</code> or no
	 *        corresponding attribute can be found.
	 */
	public static MetaAttribute findAttribute(MetaDataObject meta, Object value) {
		if (value == null) {
			throw new IllegalArgumentException("null as value not supported");
		}

		for (MetaAttribute attr : meta.getAttributes()) {

			if (attr.isDerived()) {
				// we only consider persisted classes, not derived ones like
				// "value" itself
				continue;
			}
			if (attr.getType().getImplementationClass().isAssignableFrom(value.getClass())) {
				return attr;
			}
		}
		throw new IllegalArgumentException("cannot find anyType attribute for value '" + value + '\'');
	}

}
