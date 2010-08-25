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

package org.clickframes.swf;

/**
 * new theory on overrides
 *
 * @author sboscarine
 *
 */
@Deprecated 	//will be redone in future, temporarily kept around for reference.
public class PageLevelOverride {
	private final String pageId;

	public PageLevelOverride(String pageId) {
		super();
		this.pageId = pageId;
	}

	/**
	 * Skips rendering of service layer.
	 */
	private String serviceLayerSpringId;

	/**
	 * create flows with different entity class.
	 */
	private String entityClass;

	/**
	 * Skip JSF, use Spring MVC (in Spring Web Flow plugin). In the future, this may be
	 */
	private boolean skipJSF;

	public String getPageId() {
		return pageId;
	}

	public String getServiceLayerSpringId() {
		return serviceLayerSpringId;
	}

	public String getEntityClass() {
		return entityClass;
	}

	public boolean isSkipJSF() {
		return skipJSF;
	}

	void setServiceLayerSpringId(String serviceLayerSpringId) {
		this.serviceLayerSpringId = serviceLayerSpringId;
	}

	void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	void setSkipJSF(boolean skipJSF) {
		this.skipJSF = skipJSF;
	}
}