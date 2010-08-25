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

package org.clickframes.model;

import java.util.List;

import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.FactsType;

public class ExternalLink extends Link {
    private String href;

    public ExternalLink(String id, String href, String title, String description, FactsType factsType,
            Boolean negative, String message, List<String> emails, boolean loginSuccessfulOutcome,
            boolean loginFailedOutcome, AppspecElement parent) {
        super(id, title, description, factsType, negative, message, emails, loginSuccessfulOutcome, loginFailedOutcome, parent);
        this.href = ClickframeUtils.normalize(href);

    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getTargetTitle() {
        return href;
    }
    
	@Override
	public String getMetaName() {
		return "link";
	}
}
