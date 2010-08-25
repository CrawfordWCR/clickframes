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

package org.clickframes.swf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Content;
import org.clickframes.model.Form;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;

/**
 * Extracts text from appspec into properties files for consumption by JSF applications (or other components).
 *
 * @author Steven Boscarine
 *
 */
public class PropertiesFileGenerator {
	/**
	 * Our token delimiter...for example, ${page.id}_${form.id}
	 */
	private static final String DELIM = "_";

	/**
	 * Extract properties from appspec and write to properties file. Presently testing for webflow plugin.
	 *
	 * @param path
	 *            File in which you'll write properties.
	 */
	public static void writeToPropertiesFile(File path, Appspec appspec) {
		Properties properties = new Properties();
		// properties for each page.
		for (Page page : appspec.getPages()) {
			String pid = page.getId();
			putProperty(properties, page.getTitle(), pid, "title");
			// properties for each form field.
			for (Form form : page.getForms()) {
				String fid = form.getId();
				for (SingleUserInput input : form.getInputs()) {
					final String inputId = input.getId();
					for (Validation validation : input.getValidations()) {
						String msg = input.getRequiredValidation().getDescription();
						if (msg == null || msg.trim().length() == 0) {
							msg = input.getRequiredValidation().getDefaultDescription();
						}

						if (msg == null || msg.trim().length() == 0) {
							msg = input.getTitle() + "is required (no default description defined)";
						}
						putProperty(properties, msg, fid, inputId, validation.getType() + "Message");

					}

					putProperty(properties, input.getTitle(), fid, inputId, "title");
					// for select, checkbox, & radio fields.
					for (String optionId : input.getAllowedValues().keySet()) {
						final String value = input.getAllowedValues().get(optionId);
						putProperty(properties, value, fid, inputId, optionId);
					}
				}
				for (Action action : form.getActions()) {
					putProperty(properties, action.getEscapedTitle(), fid, action.getId(), "title");
					for (Link outcome : action.getOutcomes()) {
						putProperty(properties, outcome.getMessage(), fid, action.getId(), outcome.getId(), "message");
					}
				}
			}
			for (Link link : page.getLinks()) {
				putProperty(properties, link.getTitle(), pid, link.getId());
			}
			for (Content content : page.getVerbatimContents()) {
				putProperty(properties, content.getText(), pid, content.getId());
			}
		}

		for (LinkSet linkset : appspec.getLinkSets()) {
			for (Link link : linkset.getLinks()) {
				putProperty(properties, link.getTitle(), linkset.getId(), link.getId());
			}
		}

		// Write properties file.
		try {
			properties.store(new FileOutputStream(path), appspec.getTitle());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Complain if property is not found in appspec.
	 */
	private static final void putProperty(Properties properties, String value, String... tokens) {
		String key = buildPropertyIdentifier(tokens);
		if (value == null || value.length() == 0) {
			value = "(" + key + ") is missing.  Please update your appspec";
		}
		properties.put(key, value);
	}

	/**
	 * @return single variable using our convention ${id0}_${id1}_${id2}...
	 */
	private static String buildPropertyIdentifier(String... tokens) {
		String key = tokens[0];
		if (tokens.length > 1) { // is this needed?
			for (int i = 1; i < tokens.length; i++) {
				key += (DELIM + tokens[i]);
			}
		}
		return key;
	}
}