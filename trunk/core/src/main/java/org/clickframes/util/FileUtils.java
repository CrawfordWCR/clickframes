package org.clickframes.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * helpers
 *
 * @author Steven Boscarine TODO: Confirm Apache doesn't already have implementations for these methods.
 *
 */
public class FileUtils {
	/**
	 * @return contents of file as String.
	 */
	public static String readFile(File source) {
		try {
			StringBuilder sb = new StringBuilder(1000);
			BufferedReader reader = new BufferedReader(new FileReader(source));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				sb.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
