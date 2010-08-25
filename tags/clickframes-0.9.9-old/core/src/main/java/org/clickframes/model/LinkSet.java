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

import java.util.ArrayList;
import java.util.List;

import org.clickframes.xmlbindings.LinkSetRefType;
import org.clickframes.xmlbindings.LinkSetRefsType;

public class LinkSet extends AbstractElement{
	public LinkSet(String id, AppspecElement parent) {
		super(id, parent);
	}
    private List<Link> links = new ArrayList<Link>();
    private boolean global;
    private List<Fact> facts = new ArrayList<Fact>();


    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public static List<String> createLinkSetIds(LinkSetRefsType linkSetRefsType) {
        List<String> list = new ArrayList<String>();
        if (linkSetRefsType != null) {
            for (LinkSetRefType linkSetRefType : linkSetRefsType.getLinkSetRef()) {
                list.add(linkSetRefType.getId());
            }
        }
        return list;
    }

	@Override
	public String getMetaName() {
		return "linkSet";
	}
}