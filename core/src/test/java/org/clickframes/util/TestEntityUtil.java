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

import junit.framework.Assert;

import org.clickframes.util.EntityUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestEntityUtil {
	@Test(dataProvider="FIT")
	public void test(String fullyQualifiedClassName, String className, String packageName){
		Assert.assertEquals(className, EntityUtil.getClassNameFromFullyQualifiedClassName(fullyQualifiedClassName));
		Assert.assertEquals(packageName, EntityUtil.getPackageNameFromFullyQualifiedClassName(fullyQualifiedClassName));
	}

	@DataProvider(name="FIT")
	public Object[][] createFITTable(){
		return new String[][]{

				{"java.lang.String","String","java.lang"},
				{"org.clickframes.util.EntityUtil","EntityUtil","org.clickframes.util"},
				{"","",""},
				{null,null,null}
		};
	}
}
