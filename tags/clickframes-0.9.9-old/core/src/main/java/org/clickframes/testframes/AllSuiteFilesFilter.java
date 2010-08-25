package org.clickframes.testframes;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class AllSuiteFilesFilter implements FileFilter {
    // private static Pattern suitePattern = Pattern.compile(".*[Ss]uite.*\\.html");
    private static Pattern suitePattern = Pattern.compile(".*[Ss]uite.*");

    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        String name = pathname.getName();
        return suitePattern.matcher(name).matches();
    }
}