package org.clickframes.model.util;

import org.clickframes.model.AppspecElement;
import org.clickframes.util.ClickframeUtils;

public class GUIDHelper {
	public static final String GUID_DELIMITER = "_";

	public static final String createGuid(AppspecElement parent, AppspecElement child) {
		String parentGuid = parent == null ? "" : parent.getGuid().toString();

		return parentGuid + GUID_DELIMITER + child.getId();

	}
	public static final String createGuid(AppspecElement object, String suffix) {
		return object.getGuid().toString() + GUID_DELIMITER + suffix;

	}

	//TODO:  SHould I be moved or deleted?
	public static final String createId(String id, String title){
        if (id == null) {
            return ClickframeUtils.toCompactId(title);
        }
        return id;
	}
}
