package org.clickframes.testframes;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class AllNonSuiteFilesFilter implements FileFilter {
    private static Pattern suitePattern = Pattern.compile(".*[Ss]uite.*\\.html");

    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        String name = pathname.getName();
        return !suitePattern.matcher(name).matches();
    }
}