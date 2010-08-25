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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test of Regular Expressions to be used in JavaScript
 *
 * @author sboscarine
 *
 */
public class RegexTest {
	String myRegex = "projectId=(([a-z0-9-]{36}))";

	@Test(dataProvider = "FIT")
	public void test(String input, String expectedOutput) {
		Assert.assertEquals(extractFirst(myRegex, input), expectedOutput);
	}

	/**
	 * Simple Regex Code.
	 */
	public String extractFirst(String regex, String input) {
		// Compile and use regular expression
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		boolean matchFound = matcher.find();
		if (matchFound) {
			return matcher.group(2);
		}
		return null;
	}

	@DataProvider(name = "FIT")
	public Object[][] createFITTable() {
		return new String[][] {
				{ "http://localhost:8080/appframer/manageMembers.seam?projectId=8fb49555-d2cd-46de-9fed-cdefebd70078",
						"8fb49555-d2cd-46de-9fed-cdefebd70078" },
				{ "http://localhost:8080/appframer/projectPage.seam?projectId=8fb49555-d2cd-46de-9fed-cdefebd70078",
						"8fb49555-d2cd-46de-9fed-cdefebd70078" },
				{
						"http://localhost:8080/appframer/page.seam?projectId=8fb49555-d2cd-46de-9fed-cdefebd70078&pageId=changePassword",
						"8fb49555-d2cd-46de-9fed-cdefebd70078" },
				{
						"http://localhost:8080/appframer/testSuiteHistory.seam?projectId=8fb49555-d2cd-46de-9fed-cdefebd70078&testBatchId=b2bd6c25-aa65-4f9d-a2cf-71dd7305fdde&testSuiteId=TS_pgpdSubjectMatterExperts_lsuserNav_verifyLinks.suite",
						"8fb49555-d2cd-46de-9fed-cdefebd70078" }, };
	}
}
