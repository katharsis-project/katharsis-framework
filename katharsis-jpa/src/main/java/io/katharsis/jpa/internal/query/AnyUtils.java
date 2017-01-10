package io.katharsis.jpa.internal.query;

import io.katharsis.jpa.query.AnyTypeObject;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;

public class AnyUtils {

	private AnyUtils() {
	}

	/**
	 * Sets the value of the given anytype.
	 * 
	 * @param metaLookup to use to retrieve information
	 * @param dataObject the anytype for which the value is set.
	 * @param value the new value
	 */
	public static void setValue(MetaLookup metaLookup, AnyTypeObject dataObject, Object value) {
		MetaDataObject meta = metaLookup.getMeta(dataObject.getClass()).asDataObject();
		if (value == null) {
			for (MetaAttribute attr : meta.getAttributes()) {
				attr.setValue(dataObject, null);
			}
		}
		else {
			boolean found = false;
			for (MetaAttribute attr : meta.getAttributes()) {
				if (attr.getType().getImplementationClass().isAssignableFrom(value.getClass())) {
					attr.setValue(dataObject, value);
					found = true;
				}
				else {
					attr.setValue(dataObject, null);
				}
			}
			if (!found) {
				throw new IllegalStateException("cannot assign " + value + " to " + dataObject);
			}
		}
	}

	/**
	 * Finds a matching attribute for a given value.
	 * 
	 * @param meta
	 *            the metadataobject
	 * @param value
	 *            the value
	 * @return the attribute which will accept the given value
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
