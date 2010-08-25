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

package org.clickframes.testing;

import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Link;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;

public class TestStep {
	static final String PRECONDITION_NONE = "none";

	private Appspec appspec;
	private Page page;
	/**
	 * the single user input represented by this testCase, or null if none
	 */
	private SingleUserInput singleUserInput;

	private String srIdentifier;
	private String summary;
	private String assignedTo;
	private String component;
	private TestStep preCondition;
	private String stepNumber;
	private String action;
	private String expectedResult;
	private String postCondition;

	public Appspec getProject() {
		return appspec;
	}

	public void setProject(Appspec appspec) {
		this.appspec = appspec;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	static TestStep createTestStep(Appspec appspec, Page page, String srIdentifier) {
		TestStep testStep = new TestStep();
		testStep.setPage(page);
		testStep.setProject(appspec);
		testStep.setSrIdentifier(srIdentifier);
		return testStep;
	}

	public static TestStep createTestStep(Appspec appspec, Page page, Action action, Link outcome, String title) {
		return createTestStep(appspec, page, title);
	}

	public SingleUserInput getSingleUserInput() {
		return singleUserInput;
	}

	public void setSingleUserInput(SingleUserInput singleUserInput) {
		this.singleUserInput = singleUserInput;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public TestStep getPreCondition() {
		return preCondition;
	}

	public void setPreCondition(TestStep preCondition) {
		this.preCondition = preCondition;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getStepNumber() {
		return stepNumber;
	}

	public void setStepNumber(String stepNumber) {
		this.stepNumber = stepNumber;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public String getPostCondition() {
		return postCondition;
	}

	public void setPostCondition(String postCondition) {
		this.postCondition = postCondition;
	}

	public String getSrIdentifier() {
		return srIdentifier;
	}

	public void setSrIdentifier(String srIdentifier) {
		this.srIdentifier = srIdentifier;
	}

	public String getTitle() {
		return "TC." + getSrIdentifier();
	}
}
