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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Content;
import org.clickframes.model.Form;
import org.clickframes.model.Link;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;

/**
 * Extracts text from appspec into properties files for consumption by JSF applications (or other components).
 *
 * @author Steven Boscarine
 *
 */

public class MonolithicPropertiesFileGenerator {
	private static final boolean MERGE_EXISTING_PROPERTIES=true;
	private static final Log logger = LogFactory.getLog(MonolithicPropertiesFileGenerator.class);
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
		SortedProperties properties = new SortedProperties();
		if(MERGE_EXISTING_PROPERTIES && path.exists()){
			try {
				properties.load(new FileInputStream(path));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		// properties for each page.
		for (Page page : appspec.getPages()) {
			handlePage(properties, page);
		}
		// Write properties file.
		try {
			properties.store(new FileOutputStream(path), appspec.getTitle());
			logger.debug("wrote file to " + path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected static void handlePage(Properties properties, Page page) {
		String pid = page.getId();
		putProperty(properties, page.getTitle(), pid, "title");
		// properties for each form field.
		for (Form form : page.getForms()) {
			String fid = form.getId();
			for (SingleUserInput input : form.getInputs()) {
				final String inputId = input.getId();

				for(Validation v:input.getValidations() ){
					String msg = v.getDefaultDescription();
					putProperty(properties, msg, fid, inputId, v.getType() + "Message");

				}


				//TODO: remove this block.  Being kept for default description warning.
				if (input.getRequiredValidation() != null) {
					String msg = input.getRequiredValidation().getDescription();
					if (msg == null || msg.trim().length() == 0) {
						msg = input.getRequiredValidation().getDefaultDescription();
					}

					if (msg == null || msg.trim().length() == 0) {
						msg = input.getTitle() + "is required (no default description defined)";
					}
					//putProperty(properties, msg, fid, inputId, "requiredMessage");
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
		// for (String blurbId : page.getSimpleBlurbs().keySet()) {
		// putProperty(properties, page.getSimpleBlurbs().get(blurbId), pid,
		// blurbId);
		// }
		for (Content content : page.getVerbatimContents()) {
			putProperty(properties, content.getText(), pid, content.getId());
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