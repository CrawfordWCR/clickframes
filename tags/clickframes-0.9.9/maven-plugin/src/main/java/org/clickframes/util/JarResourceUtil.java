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

package org.clickframes.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * temp home of util to test jar scanning.
 *
 * @author sboscarine
 *
 */
public class JarResourceUtil {
	private static final Log logger = LogFactory.getLog(JarResourceUtil.class);


	public static File copyTemplatesFromJarToTempDirectory(String jarFileName, String autoscanDirectoryName)
			throws IOException {
		// String jarName = "clickframes-webflow-plugin";
		// String AUTOSCAN_DIR = "/autoscan/";
		final String classpath = System.getProperty("java.class.path");
		String[] classpathArray = classpath.split(File.pathSeparatorChar + "");

		logger.debug("classpath = " + classpath);
		for (String classpathJar : classpathArray) {
			logger.debug("jar = " + classpathJar);
			if (classpathJar.contains(jarFileName)) {
				JarFile jarFile = new JarFile(classpathJar);
				File temporaryFolder = new File(System.getProperty("java.io.tmpdir"), "autoscanTempDir");
				temporaryFolder.mkdirs();
				temporaryFolder.deleteOnExit();
				// list all entries in jar
				Enumeration<JarEntry> resources = jarFile.entries();
				while (resources.hasMoreElements()) {
					JarEntry je =  resources.nextElement();
					final String fileInJar = je.getName();
					// if autoscan target, copy to temp folder
					if (fileInJar.contains(autoscanDirectoryName)) {
						copyFile(jarFile, temporaryFolder, je, fileInJar);
					}
				}
				return temporaryFolder;
			}
		}
		throw new RuntimeException("We're sorry, we couldn't find your jar");
	}

	private static void copyFile(JarFile jarFile, File temporaryFolder, JarEntry je, final String fileInJar)
			throws IOException, FileNotFoundException {
		if (!je.isDirectory()) {
			File tempFile = new File(temporaryFolder, fileInJar);
			tempFile.getParentFile().mkdirs();
			tempFile.deleteOnExit();
			IOUtils.copy(jarFile.getInputStream(je), new FileOutputStream(tempFile));
		}
	}
}
