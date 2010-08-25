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

package org.clickframes.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class
 * @author sboscarine
 *
 */
public class EntityUtil {
	private static final Log logger = LogFactory.getLog(EntityUtil.class);
	public static String getClassNameFromFullyQualifiedClassName(String input){
		if(input == null){
			logger.error("You give me null, I give you null right back.  Where does it end?");
			return null;
		}
		String[] tokens = input.split("\\.");
		return tokens[tokens.length-1];
	}
	public static String getPackageNameFromFullyQualifiedClassName(String input){
		if(input == null){
			logger.error("You give me null, I give you null right back.  What goes around, comes around!");
			return null;
		}
		return input.replaceAll("." + getClassNameFromFullyQualifiedClassName(input), "");
	}
}