package org.clickframes.testframes.selenium;

import java.io.File;

/**
 * @author Vineet Manohar
 */
public class TestCaseNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private File file;

    public TestCaseNotFoundException(File file, Throwable t) {
        super("The test case '" + file.getAbsolutePath() + "' was not found", t);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
