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

public class LengthValidation extends Validation {
	enum attributes {
		min, max
	}

	public LengthValidation(String id, String type, String typeArgs, String description, AppspecElement parent) {
		super(id, type, typeArgs, description, parent);
	}

	public boolean isMinDefined() {
		return hasArg(attributes.min.name());
	}

	public boolean isMaxDefined() {
		return hasArg(attributes.max.name());
	}

	public int getMin() {
		Integer argAsInteger = getArgAsInteger(attributes.min.name());
		//NPE fix caused by autoboxing.  Consider converting to Integer
		if(argAsInteger == null)
			return 0;
		return argAsInteger;
	}

	public int getMax() {
		Integer argAsInteger = getArgAsInteger(attributes.max.name());
		//NPE fix caused by autoboxing.  Consider converting to Integer
		if(argAsInteger == null)
			return 0;
		return argAsInteger;
	}

	@Override
	public String getDefaultDescription() {
		return "Input must meet the given length requirements";
	}
}
