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
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Vineet Manohar
 */
public class ClickframeUtils {
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(ClickframeUtils.class);

    /**
     * removes extra white space
     * 
     * @param text
     * @return
     * 
     * @author Vineet Manohar
     */
    public static String normalize(String text) {
        if (text == null) {
            return null;
        }

        return text.replaceAll("\\s+", " ").trim();
    }

    public static String toCompactId(String title) {
        // TODO: Is this acceptable behavior?
        if (title == null)
            return null;
        // word around hyphen '-' should be collapsed
        title = title.replaceAll("\\-", "");

        // all other non-word chars should be treated as spaces
        title = title.replaceAll("\\W", " ").trim();
        String[] arr = title.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            if (s.length() == 0) {
                continue;
            }

            String s1 = s.substring(0, 1);
            if (i > 0) {
                s1 = s1.toUpperCase();
            } else {
                s1 = s1.toLowerCase();
            }
            String s2 = s.substring(1, s.length());
            s2 = s2.toLowerCase();
            sb.append(s1).append(s2);
        }

        return sb.toString();
    }

    public static String lowerCaseFirstChar(String description) {
        StringBuilder s = new StringBuilder(description);
        s.setCharAt(0, Character.toLowerCase(s.charAt(0)));
        return s.toString();
    }

    public static String escapeXml(String str) {
        return StringEscapeUtils.escapeXml(str);
    }

    public static String convertSlashToPathSeparator(String path) {
        return path.replaceAll("/", escapedSeparator());
    }

    /**
     * Returns a file separator suitable for use within a regular expression,
     * i.e. four backslashes on Windows, and a normal File.separator in all
     * other environments.
     * 
     * @return the escaped File.separator
     */
    public static String escapedSeparator() {
        if (File.separator.equals("\\")) {
            return "\\\\";
        }
        return File.separator;
    }

    /**
     * Created to remove '.' from IDs
     * 
     * @return ID that is safe to use throughout application.
     */
    public static String cleanId(String in) {
        if (in == null) {
            return null;
        }
        // id's cannot have periods in them.
        String cleaned = in.replace('.', '_');
        return cleaned;
    }

    /**
     * TODO: look carefully into this, this doesn't look right
     * 
     * Can you put and remove values from the map while iterating on the keyset?
     * 
     * @param in
     * @return
     * 
     * @author Vineet Manohar
     */
    public static Map<String, String> clean(Map<String, String> in) {
        for (String key : in.keySet()) {
            String cleaned = cleanId(key);
            if (!in.containsKey(cleaned)) {
                String value = in.remove(key);
                in.put(cleaned, value);

            }
        }
        return in;
    }

    public static String getFileSeparatorLiteral() {
        return Matcher.quoteReplacement(File.separator);
    }

    /**
     * // source dir- /tmp/dir
     * 
     * // full source file - /tmp/dir/new/file
     * 
     * // dest dir - my/dir
     * 
     * // full dest file - ?
     * 
     * Calculate the destination for the sourceFile, it were to be moved from
     * source base directory to destination base directory - preserving the
     * relative path
     * 
     * @param dir1
     * @param file1
     * @param dir2
     * @return
     * 
     * @author Vineet Manohar
     */
    public static File getDestinationFile(File dir1, File file1, File dir2) {
        String dir1Path;
        String file1Path;
        String dir2Path;

        try {
            dir1Path = dir1.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Could not calculate canonical path for file : " + dir1.getAbsolutePath(), e);
        }

        try {
            file1Path = file1.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Could not calculate canonical path for file : " + file1.getAbsolutePath(), e);
        }

        try {
            dir2Path = dir2.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Could not calculate canonical path for file : " + dir2.getAbsolutePath(), e);
        }

        // first convert all to forward '/'
        String slash = ClickframeUtils.getFileSeparatorLiteral();
        dir1Path = dir1Path.replaceAll(slash, "/");
        file1Path = file1Path.replaceAll(slash, "/");
        dir2Path = dir2Path.replaceAll(slash, "/");

        String file2Path = file1Path.replaceAll("^" + dir1Path, dir2Path);

        // now convert '/' back to file system
        file2Path = file2Path.replaceAll("/", slash);

        return new File(file2Path);
    }

    /**
     * Reverses the effect of camel case - converts "fooBar" to "Foo Bar"
     * 
     * @param id
     * @return
     * 
     * @author Vineet Manohar
     */
    public static String toHumanReadable(String id) {
        // Split the word
        String[] strArray = StringUtils.splitByCharacterTypeCamelCase(id);

        // separate the tokens by spaces
        String retVal = StringUtils.join(strArray, ' ');

        retVal = StringUtils.capitalize(retVal);

        return retVal;
    }
}