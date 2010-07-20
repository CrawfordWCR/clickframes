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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.PermissionType;
import org.clickframes.xmlbindings.PermissionsType;

/**
 * A logical permission, generally applied to pages
 * 
 * @author Vineet Manohar
 */
public class Permission extends AbstractElementWithFacts {
    private String description;

    Permission(String id, Appspec appspec) {
        super(id, appspec);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = ClickframeUtils.normalize(description);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getId() + ")";
    }

    public static Permission create(Appspec appspec, PermissionType permissionType)
            throws AppspecConstraintViolationException {
        Permission permission = new Permission(permissionType.getId(), appspec);

        permission.setDescription(permissionType.getDescription());

        permission.getFacts().addAll(Fact.createList(permissionType.getFacts()));

        return permission;
    }

    public static List<Permission> createList(Appspec appspec, PermissionsType permissionsType)
            throws AppspecConstraintViolationException {
        List<Permission> pages = new ArrayList<Permission>();
        if (permissionsType != null) {
            for (PermissionType permissionType : permissionsType.getPermission()) {
                Permission page = create(appspec, permissionType);
                pages.add(page);
            }
        }

        // sort pages alphabetically
        Collections.sort(pages, new Comparator<Permission>() {
            public int compare(Permission o1, Permission o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        return pages;
    }
    
	@Override
	public String getMetaName() {
		return "permission";
	}
}