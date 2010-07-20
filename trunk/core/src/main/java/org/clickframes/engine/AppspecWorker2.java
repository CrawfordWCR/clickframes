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

package org.clickframes.engine;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.util.VelocityCodeGenerator;

/**
 * Thread wrapper for CodeGenerator calls. Allows us to generate templates in
 * parallel.
 * 
 * @author Steven Boscarine
 * @author Vineet Manohar
 */
public class AppspecWorker2 implements Callable<Boolean> {
	private final Log logger = LogFactory.getLog(getClass());

	private final String filename;
	private final String outputPath;
	private final String templatePath;
	private final Map<String, Object> params;

	public AppspecWorker2(String filename, String relativePath,
			String templatePath, Map<String, Object> params) {
		super();

		// example values:
		// templatePath =
		// /clickframes/seam/autoscan/src/main/java/${packagePath}/controller/
		// EmailController.java.vm,

		// relativePath = src/main/java/demo/controller/,

		// filename = EmailController.java.vm }

		this.templatePath = cleanTemplatePath(templatePath);
		this.filename = cleanFilename(filename);
		this.params = params;
		outputPath = relativePath + this.filename;
		if (outputPath.contains("${")) {
			throw new RuntimeException(outputPath
					+ " has unparsed tokens.  Your engine has failed.");
		}
	}

	private String cleanTemplatePath(String templatePath) {
		if (templatePath.startsWith("/")) {
			return templatePath;
		}
		return "/" + templatePath;
	}

	private static String cleanFilename(String in) {
		if (in.endsWith(".vm")) {
			return in.substring(0, in.length() - 3);
		}
		return in;
	}

	@Override
	public Boolean call() {
		try {
			callSync();

			return true;
		} catch (Throwable t) {
			logger.error("Template call failed for template: " + templatePath,
					t);
			return false;
		}
	}

	public void callSync() throws IOException {
		// logger.debug("Processing template: " + templatePath);
		VelocityCodeGenerator.generateFromTemplate(params, filename,
				templatePath, outputPath);
		// logger.debug("Processed template: " + templatePath);
	}
}