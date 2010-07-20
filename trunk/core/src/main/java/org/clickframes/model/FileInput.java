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

import org.clickframes.xmlbindings.InputType;
import org.clickframes.xmlbindings.InputTypeEnum;

/**
 * @author Vineet Manohar
 */
public class FileInput extends SingleUserInput {
	public FileInput(Appspec appspec, InputType inputType, AppspecElement parent) throws EntityNotFoundException,
            EntityPropertyNotFoundException {
		super(appspec, InputTypeEnum.UPLOAD, inputType, parent);
	}

	@Override
	public String getJavaType() {
		return "java.util.List";
	}
}