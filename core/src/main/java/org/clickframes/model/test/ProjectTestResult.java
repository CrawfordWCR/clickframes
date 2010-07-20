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

package org.clickframes.model.test;

/**
 * More details about a suite
 *
 * @author Vineet Manohar
 */
public class ProjectTestResult {
	private String testName;
	private String browser;
	private boolean passed;
	private String htmlOutput;

	public String getBrowser() {
		return browser != null ? browser.replace("*", "") : browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public String getHtmlOutput() {
		return htmlOutput;
	}

	public void setHtmlOutput(String htmlOutput) {
		this.htmlOutput = htmlOutput;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}
}