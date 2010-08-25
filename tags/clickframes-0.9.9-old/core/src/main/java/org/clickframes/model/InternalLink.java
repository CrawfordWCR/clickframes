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

import org.clickframes.xmlbindings.FactsType;

public class InternalLink extends Link {
    private String pageRef;
    // private Page page;
    private Appspec appspec;

    public InternalLink(Appspec appspec, String id, String pageRef, String title, String description,
            FactsType factsType, Boolean negative, String message, List<String> emailRefs,
            boolean loginSuccessfulOutcome, boolean loginFailedOutcome, AppspecElement parent) {
        super(id, title, description, factsType, negative, message, emailRefs, loginSuccessfulOutcome,
                loginFailedOutcome, parent);
        this.pageRef = pageRef;
        this.appspec = appspec;
    }

    public String getPageRef() {
        return pageRef;
    }

    public void setPageRef(String pageRef) {
        this.pageRef = pageRef;
    }

    public Page getPage() throws PageNotFoundException {
        return appspec.getPage(pageRef);
    }

    // public void setPage(Page page) {
    // this.page = page;
    // }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getTargetTitle() {
        try {
            return getPage().getTitle();
        } catch (PageNotFoundException e) {
            throw new RuntimeException("Invalid target page", e);
        }
    }
    
	@Override
	public String getMetaName() {
		return "link";
	}
}