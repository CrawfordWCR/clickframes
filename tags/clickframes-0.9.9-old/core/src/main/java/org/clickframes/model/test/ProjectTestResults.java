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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Appspec test results
 *
 * @author Vineet Manohar
 */
public class ProjectTestResults {
	private int failedCount;
	private int passedCount;

	private Date startTime;
	private Date endTime;

	private List<ProjectTestResult> projectTestResultList = new ArrayList<ProjectTestResult>();

	public int getTotalCount() {
		return failedCount + passedCount;
	}

	public int getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(int failedCount) {
		this.failedCount = failedCount;
	}

	public int getPassedCount() {
		return passedCount;
	}

	public void setPassedCount(int passedCount) {
		this.passedCount = passedCount;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public long getExecutionTimeInMillis() {
		return endTime.getTime() - startTime.getTime();
	}

	public List<ProjectTestResult> getProjectTestResultList() {
		return projectTestResultList;
	}

	public void setProjectTestResultList(List<ProjectTestResult> projectTestResultList) {
		this.projectTestResultList = projectTestResultList;
	}
}
