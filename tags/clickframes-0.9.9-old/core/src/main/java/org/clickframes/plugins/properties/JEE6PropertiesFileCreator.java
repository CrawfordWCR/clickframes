package org.clickframes.plugins.properties;

import static org.clickframes.model.util.GUIDHelper.createGuid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.AppspecElement;
import org.clickframes.model.Content;
import org.clickframes.model.Form;
import org.clickframes.model.Link;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.swf.SortedProperties;

public class JEE6PropertiesFileCreator {
	private static final boolean MERGE_EXISTING_PROPERTIES=true;
	private static final Log logger = LogFactory.getLog(JEE6PropertiesFileCreator.class);

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
			logger.info("wrote properties file to " + path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected static void handlePage(Properties properties, Page page) {
//		putProperty(properties, page, "title", page.getTitle());
		putProperty(properties, page, page.getTitle());
		// properties for each form field.
		for (Form form : page.getForms()) {
			for (SingleUserInput input : form.getInputs()) {
//				putProperty(properties, input, "title", input.getTitle());
				putProperty(properties, input, input.getTitle());
				for(Validation v:input.getValidations() ){
					String msg = v.getDefaultDescription();
//					final String key = v.getType() + "Message";
					final String key = v.getType();
					putProperty(properties, v, key, msg);
				}
				// for select, checkbox, & radio fields.
				for (String optionId : input.getAllowedValues().keySet()) {
					final String value = input.getAllowedValues().get(optionId);
					putProperty(properties, input, optionId, value);
				}
			}
			for (Action action : form.getActions()) {
//				putProperty(properties, action, "title", action.getEscapedTitle());
				putProperty(properties, action, action.getEscapedTitle());
				for (Link outcome : action.getOutcomes()) {
					putProperty(properties, outcome, outcome.getMessage());
					putProperty(properties, outcome, "message", outcome.getMessage());
				}
			}
		}
		for (Link link : page.getLinks()) {
			putProperty(properties, link, link.getTitle());
		}
		for (Content content : page.getVerbatimContents()) {
			putProperty(properties, content, content.getText());
		}

	}

	private static void putProperty(Properties properties, AppspecElement obj, String propertyName, String value) {
		putProperty(properties, createGuid(obj, propertyName), value);
	}


	private static final void putProperty(Properties properties, AppspecElement obj, String value) {
		putProperty(properties, obj.getGuid().toString(), value);
	}
	/**
	 * Complain if property is not found in appspec.
	 */
	private static final void putProperty(Properties properties, String key, String value) {
		if (value == null || value.length() == 0) {
			value = "(" + key + ") is missing.  Please update your appspec";
		}
		properties.put(key, value);
	}
}