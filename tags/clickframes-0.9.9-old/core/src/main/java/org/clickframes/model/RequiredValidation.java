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


/**
 * Indicates that the input is required
 *
 * @author Vineet Manohar
 */
public class RequiredValidation extends Validation {
    public RequiredValidation(String id, String type, String typeArgs, String description, AppspecElement parent) {
        super(id, type, typeArgs, description, parent);
    }

    @Override
    public String getDefaultDescription() {
        return "Input is required";
    }

    @Override
    public String getNegativeExample() {
        if (super.getNegativeExample() != null) {
            return super.getNegativeExample();
        }

        // an empty value will fail required
        return "";
    }
}