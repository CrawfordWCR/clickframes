package org.clickframes.testframes;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import org.clickframes.model.Appspec;

public class PageSuiteFilter implements FileFilter {
    private Appspec appspec;
    private String pageId;

    private static FileFilter allSuitesFileFilter = new AllSuiteFilesFilter();

    public PageSuiteFilter(Appspec appspec, String pageId) {
        this.pageId = pageId;
        this.appspec = appspec;
    }

    public boolean accept(File pathname) {
        if (!allSuitesFileFilter.accept(pathname)) {
            return false;
        }

        // TS_pgpage3_elementsExist.suite.html
        Pattern pagePattern = Pattern.compile("^.*pg" + pageId + ".*$");

        String name = pathname.getAbsolutePath();
        return pagePattern.matcher(name).matches();
    }
}