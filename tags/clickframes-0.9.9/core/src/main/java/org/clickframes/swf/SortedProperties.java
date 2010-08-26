package org.clickframes.swf;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
/**
 * Experiment in sorting.
 * @author sboscarine
 *
 */
public class SortedProperties extends Properties {
	@Override
	@SuppressWarnings("unchecked")
	public Enumeration keys() {
		Enumeration keysEnum = super.keys();
		Vector<String> keyList = new Vector<String>();
		while (keysEnum.hasMoreElements()) {
			keyList.add((String) keysEnum.nextElement());
		}
		Collections.sort(keyList);
		return keyList.elements();
	}

	private static final long serialVersionUID = 1L;
}