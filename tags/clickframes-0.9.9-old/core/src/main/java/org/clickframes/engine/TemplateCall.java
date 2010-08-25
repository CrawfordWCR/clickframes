/*
 * Clickframes: Full lifecycle software development automation.
 * Copyright (C)  2009 Children's Hospital Boston
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.clickframes.engine;

import java.util.Map;

/**
 * Value Object...just a handy self-documenting container for Strings.
 * @author Steven Boscarine
 */
public class TemplateCall {
	public final String templatePath;
	public final String relativePath;
	public final String filename;
	public final Map<String, Object> objects;

	public TemplateCall(String templatePath, String relativePath, String filename, Map<String, Object> objects) {
		super();
		this.templatePath = templatePath;
		this.relativePath = relativePath;
		this.filename = filename;
		this.objects = objects;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ " + "templatePath = " + templatePath + ", relativePath = "
				+ relativePath + ", filename = " + filename + " }";
	}
	/**
	 * Allows users to add data manually.
	 * @return
	 */
	public final Map<String, Object> getObjects() {
		return objects;
	}
}
