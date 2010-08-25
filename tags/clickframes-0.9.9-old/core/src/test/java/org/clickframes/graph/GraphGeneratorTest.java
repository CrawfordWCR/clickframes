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

package org.clickframes.graph;

import org.clickframes.graph.GraphGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphGeneratorTest {
	@Test
	public void testWrapped() {
		String input = "Username does not apply to unique token";
		String expected = "Username does not \\napply to unique \\ntoken";
		testWrapped(input, expected);
	}

	private void testWrapped(String input, String expected) {
		String actual = GraphGenerator.wrapped(input);
		Assert.assertEquals(expected, actual, "Wrong output for wrapped");
	}
}
