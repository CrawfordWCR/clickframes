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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.clickframes.model.Appspec;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;

/**
 * 1) has a header step with step number 000. The action is computed from the action actionSteps. 2) has a list of
 * action actionSteps. action actionSteps are
 *
 * @author Vineet Manohar
 *
 */
public class TestScript {
	private List<TestStep> actionSteps = new ArrayList<TestStep>();

	private Appspec appspec;
	private Page page;
	private String header;
	private String identifier;

	public static TestScript create(Appspec appspec, Page page, String header) {
		TestScript retVal = new TestScript();
		retVal.appspec = appspec;
		retVal.page = page;
		retVal.header = header;
		retVal.identifier = page.getId();

		return retVal;
	}

	public static TestScript create(Appspec appspec, Page page, SingleUserInput input, String header) {
		TestScript retVal = new TestScript();
		retVal.appspec = appspec;
		retVal.page = page;
		retVal.header = header;
		retVal.identifier = page.getId() + "." + input.getId();

		return retVal;
	}

	private TestScript() {
		// force access through static factory methods
	}

	public List<TestStep> getSteps() {
		List<TestStep> steps = new ArrayList<TestStep>();
		steps.add(0, getHeaderStep());
		steps.addAll(actionSteps);
		return steps;
	}

	private TestStep getHeaderStep() {

		TestStep headerStep = TestStep.createTestStep(null, null, "");
		headerStep.setSummary("This test script verifies that the " + header);

		String action = "Execute the following steps:\n";
		for (TestStep step : actionSteps) {
			action += step.getStepNumber() + "\n";
		}
		headerStep.setAction(action);
		headerStep.setStepNumber("TC." + identifier + ".000");
		return headerStep;
	}

	public void addActionSteps(List<TestStep> steps) {
		for (TestStep step : steps) {
			addActionStep(step);
		}
	}

	public void addActionStep(TestStep step) {
		step.setStepNumber("TC." + identifier + "." + new DecimalFormat("000").format(actionSteps.size() + 1));
		actionSteps.add(step);
	}
}