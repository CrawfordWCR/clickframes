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
import org.clickframes.util.CodeGenerator;

/**
 * Thread wrapper for CodeGenerator calls.
 * Allows us to generate templates in parallel.
 * @author Steven Boscarine
 *
 */
public class AppspecWorker implements Callable<Boolean> {
	@SuppressWarnings("unused")
	private final Log logger = LogFactory.getLog(getClass());
	private static CodeGenerator codeGenerator = new CodeGenerator();

	private final String filename;
	private final String relativePath;
	private final String templatePath;
	private final Map<String,Object> params;

	public AppspecWorker(String filename, String relativePath, String templatePath, Map<String,Object> params) {
		super();
		this.filename = cleanFilename(filename);
		this.relativePath = relativePath;
		this.templatePath = templatePath;
		this.params = params;
		String destination = relativePath + filename;
		if (destination.contains("${")) {
			throw new IllegalArgumentException(destination + " has unparsed tokens.  Your engine has failed.");
		}
	}

	private static String cleanFilename(String in){
		//TODO:  replace with RegEx
		if(in.endsWith(".vm")){
			return in.substring(0, in.length()-3);
		}
		return in;
	}

	@Override
	public Boolean call() {
		try {
			codeGenerator.generateCodeAutoscan(templatePath, relativePath, filename, params);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}