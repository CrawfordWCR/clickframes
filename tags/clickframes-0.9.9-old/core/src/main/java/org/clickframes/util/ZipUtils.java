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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ZipUtils {
	@SuppressWarnings("unchecked")
	/*
	 * * zip the directory to the target file
	 */
	public static void zipDir(File directory, File targetFile) throws IOException {
		targetFile.getParentFile().mkdirs();

		ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(targetFile));

		for (File child : (Collection<File>) FileUtils.listFiles(directory, null, true)) {
			zipStream.putNextEntry(new ZipEntry(getRelativePath(child, directory)));
			IOUtils.copy(new FileInputStream(child), zipStream);
		}

		zipStream.finish();
	}

	private static String getRelativePath(File child, File directory) {
		String s1 = child.getAbsolutePath();
		String s2 = directory.getAbsolutePath() + File.separator;

		int j = 0;
		for (int i = 0; i < s2.length(); i++) {
			if (s2.charAt(i) == s1.charAt(j)) {
				j++;
			} else {
				break;
			}
		}

		return s1.substring(j, s1.length());
	}
}
