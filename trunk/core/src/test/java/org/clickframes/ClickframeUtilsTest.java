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

package org.clickframes;

import org.clickframes.util.ClickframeUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ClickframeUtilsTest {
    @Test
    public void testToCompactId() {
        String title = "E-mail Addresses";
        String id = ClickframeUtils.toCompactId(title);
        Assert.assertEquals("emailAddresses", id, "Compact title not implmemented correctly");
    }

    @Test
    public void testToLongTitle() {
        Assert.assertEquals(ClickframeUtils.toHumanReadable("createAccount"), "Create Account",
                "Long title not implmemented correctly");
        Assert.assertEquals(ClickframeUtils.toHumanReadable("logout"), "Logout", "Long title not implmemented correctly");
    }

    @Test
    public void testEscapeXml() {
        String str = "Save & Continue";
        String escapedTitle = ClickframeUtils.escapeXml(str);
        Assert.assertEquals("Save &amp; Continue", escapedTitle, "Escape not implmemented correctly");
    }

    @Test
    public static void testNormalize() {
    	String text = "\n\nSome text\n\nfollowed by \n more text";
        String normalized = ClickframeUtils.normalize(text);
        Assert.assertFalse(text.equals(normalized));
    }
}
