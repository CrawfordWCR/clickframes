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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Uses JDK 6 compiler API to aide people writing templates.
 * @author Steven Boscarine.
 *
 */
public class MavenJDK6Compiler {
	private static final JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
	private static final StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null);
	private File sourceDirectory;

	public MavenJDK6Compiler(File sourceDirectory) {
		super();
		this.sourceDirectory = sourceDirectory;
	}

	@SuppressWarnings("unused")
	private final Log logger = LogFactory.getLog(getClass());

	@SuppressWarnings("unchecked")
	private Iterable<JavaFileObject> getClassesToCompile() {
		Collection<File> allFiles = AutoscanDirectoryUtil.recursivelyScanDirectory(sourceDirectory);
		// getJavaFileObjects' param is a vararg
		Iterable fileObjects = sjfm.getJavaFileObjects(allFiles.toArray(new File[allFiles.size()]));
		return fileObjects;
	}

	public boolean compile() throws IOException {

		final Boolean task = jc.getTask(null, sjfm, null, buildClassPath(), null, getClassesToCompile()).call();
		// Add more compilation tasks
		sjfm.close(); // HACK!!!!!
		return task;
	}

	private List<String> buildClassPath() {
		List<String> optionList = new ArrayList<String>();
		optionList.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));
		return optionList;
	}
}
