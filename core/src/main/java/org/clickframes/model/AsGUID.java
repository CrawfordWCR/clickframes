package org.clickframes.model;

import org.clickframes.model.util.GUIDHelper;

/**
 * Appspec GUID
 *
 * Still a POC...likely to be renamed
 *
 * @author Steven Boscarine
 *
 */
public class AsGUID {
	private final AppspecElement parent;
	private final AppspecElement child;

	public AsGUID(AppspecElement parent, AppspecElement child) {
		this.parent = parent;
		this.child = child;
	}

	public final AppspecElement getParent() {
		return parent;
	}

	public final AppspecElement getChild() {
		return child;
	}
	@Override
	public String toString() {
		return GUIDHelper.createGuid(parent, child);
	}
}