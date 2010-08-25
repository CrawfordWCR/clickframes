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

import java.util.HashMap;
import java.util.Map;

import org.clickframes.VelocityHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VelocityHelperTest {
    @Test
    public void testRunMacroNotOverridden() {
        String actual = VelocityHelper.runMacro(null, "/fixed.text");
        Assert.assertEquals("fixed.text", actual);
    }

    @Test
    public void testRunMacroWithNoSlashAtTheBeginningNotOverridden() {
        String actual = VelocityHelper.runMacro(null, "fixed.text");
        Assert.assertEquals("fixed.text", actual);
    }

    @Test
    public void testEvaluate() {
        Map<String, Object> params = new HashMap<String, Object>();
        Page page = new Page();
        page.setId("viewIssues");
        params.put("id", "1");
        params.put("page", page);
        Assert.assertTrue(VelocityHelper.evaluateCondition(params, "$id == \"1\""));
        Assert.assertFalse(VelocityHelper.evaluateCondition(params, "$id != \"1\""));
        Assert.assertFalse(VelocityHelper.evaluateCondition(params, "$id == \"2\""));
        Assert.assertFalse(VelocityHelper.evaluateCondition(params, "$page == \"2\""));
        Assert.assertFalse(VelocityHelper.evaluateCondition(params, "$page.id == \"2\""));
        Assert.assertTrue(VelocityHelper.evaluateCondition(params, "$page.id != \"2\""));
        Assert.assertTrue(VelocityHelper.evaluateCondition(params, "${page.id} == \"viewIssues\""));
        Assert.assertTrue(VelocityHelper.evaluateCondition(params, "${page.id} == 'viewIssues'"));
        Assert.assertTrue(VelocityHelper.evaluateCondition(params, "$page.id == 'viewIssues'"));
        Assert.assertFalse(VelocityHelper.evaluateCondition(params, "${page.id} != \"viewIssues\""));
        Assert.assertTrue(VelocityHelper.evaluateCondition(params, "${page.id} != 'notFound'"));
        Assert.assertFalse(VelocityHelper.evaluateCondition(params, "$page.id == \"notFound\""));
        Assert.assertTrue(VelocityHelper.evaluateCondition(params, "$page.id != \"notFound\""));
    }

    public static class Page {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}