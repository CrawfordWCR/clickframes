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

package org.clickframes.engine;

/**
 * Constants
 *
 * @author Steven Boscarine
 *
 */
public class ClickframesEngineConstants {
    static final String GROUP_TOKEN = "${group.id}";
    static final String ENTITY_CLASS_TOKEN = "${entityClass}";
    static final String GROUP_CLASS_TOKEN = "${groupClass}";
    static final String CONTROLLER_PKG_TOKEN = "${controllerPackage}";
    static final String ENTITY_PKG_TOKEN = "${entityPackage}";
    static final String SERVICE_PKG_TOKEN = "${servicePackage}";
    static final String VALIDATOR_PKG_TOKEN = "${validatorPackage}";

    /**
     * Allows us to have templates with 2 different names output to the same
     * file name (${mvc}${pageId}.xhtml and ${pageId}.xhtml both output to
     * foo.xhtml)
     */
    static final String SPRING_MVC_FLAG = "(mvc)";
}