package org.clickframes.model;

import org.apache.commons.lang.StringUtils;

/**
 * Placeholder object.  We're not fully supporting groups yet as we're finalizing our strategy.
 *
 * However, this enabled plugin authors to write reasonable templates.
 * @author Steven Boscarine
 *
 */
public class Group {
	private final String id;
	private String className;

	public Group(String id) {
		this.id = id;
		this.className=StringUtils.capitalize(id);
	}

	public final String getId() {
		return id;
	}

	public final String getClassName() {
		return className;
	}

	public final void setClassName(String className) {
		this.className = className;
	}
}
