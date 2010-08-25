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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.ParamType;
import org.clickframes.xmlbindings.ParamsType;

public class PageParameter implements Comparable<PageParameter> {
    private String id;
    private String title;
    private String description;
    private EntityProperty entityProperty;
    private boolean required;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return StringUtils.capitalize(getId());
    }

    public String getDescription() {
        if (description != null) {
            return description.trim();
        }
        return null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHtmlDescription() {
        return StringEscapeUtils.escapeHtml(getDescription());
    }

    public String getId() {
        if (this.id != null) {
            return this.id;
        }

        return ClickframeUtils.toCompactId(title);
    }

    public static PageParameter create(Appspec appspec, ParamType paramType) throws EntityNotFoundException, EntityPropertyNotFoundException {
        PageParameter pageParameter = new PageParameter();
        pageParameter.setDescription(paramType.getDescription());
        pageParameter.setTitle(paramType.getTitle());
        pageParameter.setId(paramType.getId());
        pageParameter.setEntityProperty(EntityProperty.getEntityProperty(appspec, paramType.getEntityPropertyRef()));
        pageParameter.setRequired(paramType.isRequired());

        return pageParameter;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PageParameter) {
            return this.getId().equals(((PageParameter) obj).getId());
        }
        return false;
    }

    @Override
    public int compareTo(PageParameter o) {
        return this.getId().compareTo(o.getId());
    }

    public static List<PageParameter> createList(Appspec appspec, ParamsType params) throws EntityNotFoundException, EntityPropertyNotFoundException {
        List<PageParameter> inputs = new ArrayList<PageParameter>();
        if (params != null) {
            for (ParamType paramType : params.getParam()) {
                inputs.add(PageParameter.create(appspec, paramType));
            }
        }

        return inputs;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EntityProperty getEntityProperty() {
        return entityProperty;
    }

    public void setEntityProperty(EntityProperty entityProperty) {
        this.entityProperty = entityProperty;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}