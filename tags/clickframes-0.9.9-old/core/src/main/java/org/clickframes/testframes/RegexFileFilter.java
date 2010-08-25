package org.clickframes.testframes;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class RegexFileFilter implements FileFilter {
     private static FileFilter allSuitesFileFilter = new AllSuiteFilesFilter();

    private String regex;

    public RegexFileFilter(String regex) {
        this.regex = regex;
    }

    public boolean accept(File pathname) {
         if (!allSuitesFileFilter.accept(pathname)) {
            return false;
        }

        Pattern requiredPattern = Pattern.compile("^.*" + regex + ".*", Pattern.CASE_INSENSITIVE);

        String name = pathname.getAbsolutePath().toLowerCase();
        return requiredPattern.matcher(name).matches();
    }

    @Override
    public String toString() {
        return "case insensitive regex filter: " + "^.*" + regex + ".*";
    }
}