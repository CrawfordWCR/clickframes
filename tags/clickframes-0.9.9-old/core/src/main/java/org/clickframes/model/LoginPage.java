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

/**
 * represents a slice of a Page which allows you to login
 *
 * @author Vineet Manohar
 */
public class LoginPage {
	private Page page;
	private Form form;
	private SingleUserInput username;
	private SingleUserInput password;
	private Action action;
	private Link successfulOutcome;
	private Link failedOutcome;

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public Form getForm() {
		return this.form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Link getSuccessfulOutcome() {
		return successfulOutcome;
	}

	public void setSuccessfulOutcome(Link successfulOutcome) {
		this.successfulOutcome = successfulOutcome;
	}

	public Link getFailedOutcome() {
		return failedOutcome;
	}

	public void setFailedOutcome(Link failedOutcome) {
		this.failedOutcome = failedOutcome;
	}

	public SingleUserInput getUsername() {
		return username;
	}

	public void setUsername(SingleUserInput username) {
		this.username = username;
	}

	public SingleUserInput getPassword() {
		return password;
	}

	public void setPassword(SingleUserInput password) {
		this.password = password;
	}
}