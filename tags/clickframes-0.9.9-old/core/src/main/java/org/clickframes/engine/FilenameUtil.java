package org.clickframes.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Broken into separate class to facilitate unit testing.
 * This class simply determines the filename in a path.
 * @author Steven Boscarine
 *
 */
public class FilenameUtil {
	private static final Log logger = LogFactory.getLog(FilenameUtil.class);

	/**
	 *
	 * @param path full path (/foo/bar.txt)
	 * @return {folder, filename} ({/foo/, bar.txt})
	 */
	public static String[] splitFilenameAndFolder(String path) {
		if (path == null || path.length() == 0) {
			logger.error("path=" + path);
			throw new RuntimeException("This method requires non-null and non-empty input.");
		}
		// String separator = File.separator;
		// shouldn't use File.separator since we're grabbing from a jar
		String separator = "/";
		if (path.endsWith(separator)) {
			// folder was passed.
			return new String[] { path, "" };
		}

		String[] tokens = path.split(separator);
		String filename = tokens[tokens.length - 1];
		String folder = path.replace(filename, "");

		return new String[] { folder, filename };
	}
}