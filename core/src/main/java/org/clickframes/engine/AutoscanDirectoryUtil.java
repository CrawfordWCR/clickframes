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
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Utility to recursively scan directories Also contains filter to distinguish
 * allowed extensions Filter is needed to prevent .svn folders from being
 * mistaken for templates.
 *
 * @author Steven Boscarine
 *
 */
public class AutoscanDirectoryUtil {
	private static final IOFileFilter EXCLUDE_SVN_FOLDERS = FileFilterUtils.makeSVNAware(null);

	public static Collection<File> recursivelyScanDirectory(File file) {
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(file, EXCLUDE_SVN_FOLDERS, EXCLUDE_SVN_FOLDERS);
		return files;
	}
	//
	//
	//
	//
	//
	// private static final String[] DEFAULT_EXCLUDED_FOLDERNAMES = { ".svn",
	// ".cvs" };
	// private static final String[] DEFAULT_EXCLUDED_EXTENSIONS = {
	// ".svnignore" };
	// @SuppressWarnings("unused")
	// private static final Log logger =
	// LogFactory.getLog(AutoscanDirectoryUtil.class);
	//
	// public static List<File> recursivelyScanDirectory(File file) {
	// List<File> files = new ArrayList<File>();
	// recursivelyScanDirectory(files, file);
	// return files;
	// }
	//
	// private static void recursivelyScanDirectory(List<File> files, File file)
	// {
	// if (file.isDirectory()) {
	// for (File tmp : file.listFiles(FILTER)) {
	// if (tmp.isDirectory()) {
	// recursivelyScanDirectory(files, tmp);
	// } else {
	// files.add(tmp);
	// }
	// }
	// }
	// }
	//
	// private static final FileFilter FILTER = new SourceFilter();
	//
	// /** File filter to make sure only valid templates are returned. Presently
	// only filters subversion config. */
	// private static class SourceFilter implements FileFilter {
	// private final String[] excludedFolderNames;
	// private final String[] excludedExtensions;
	//
	// public SourceFilter(String[] excludedFolderNames, String[]
	// excludedExtensions) {
	// this.excludedFolderNames = excludedFolderNames;
	// this.excludedExtensions = excludedExtensions;
	// }
	//
	// public SourceFilter() {
	// this(DEFAULT_EXCLUDED_FOLDERNAMES, DEFAULT_EXCLUDED_EXTENSIONS);
	// }
	//
	// @Override
	// public boolean accept(File pathname) {
	// if (pathname.isDirectory()) {
	// for (String folderName : excludedFolderNames) {
	// if (pathname.getName().equalsIgnoreCase(folderName)) {
	// return false;
	// }
	// }
	// return true;
	// }
	// for (String extension : excludedExtensions) {
	// if (pathname.getName().toLowerCase().endsWith(extension.toLowerCase())) {
	// return false;
	// }
	// }
	// return true;
	// }
	// }
}