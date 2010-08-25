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

package org.clickframes.renderer;

import java.util.HashMap;
import java.util.Map;

public class ProjectEstimate {
	private Map<String, PageEstimate> pageEstimates = new HashMap<String, PageEstimate>();

	public Map<String, PageEstimate> getPageEstimates() {
		return pageEstimates;
	}

	public void setPageEstimates(Map<String, PageEstimate> pageEstimates) {
		this.pageEstimates = pageEstimates;
	}

	public void addPageEstimate(PageEstimate pageEstimate) {
		this.pageEstimates.put(pageEstimate.getPage().getId(), pageEstimate);
	}

	public int getPoints() {
		int retVal = 0;
		for (PageEstimate pageEstimate : pageEstimates.values()) {
			retVal += pageEstimate.getPoints();
		}
		return retVal;
	}
}
