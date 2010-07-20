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

package org.clickframes.testframes;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.clickframes.AppspecEnvironmentConstraintException;
import org.clickframes.model.Appspec;
import org.clickframes.model.test.ProjectTestResult;
import org.clickframes.model.test.ProjectTestResults;
import org.clickframes.techspec.Techspec;
import org.clickframes.util.ClickframeUtils;
import org.clickframes.util.CodeGenerator;

import com.enjoyxstudy.selenium.htmlsuite.HTMLSuite;
import com.enjoyxstudy.selenium.htmlsuite.MultiHTMLSuiteRunner;

public class TestRunner {
    /**
     * enforced by windows
     */
    public static final int MAX_FILE_PATH = 255;

    private static CodeGenerator codeGenerator = new CodeGenerator();

    public static ProjectTestResults runPageTests(TestProfile testProfile, Techspec techspec, Appspec appspec,
            String pageId) throws Exception {
        appspec.getPage(pageId);
        return runAllTestSuits(testProfile, techspec, appspec, "pages/" + pageId, new PageSuiteFilter(appspec, pageId));
    }

    public static ProjectTestResults runAllTestSuits(TestProfile testProfile, Techspec techspec, Appspec appspec)
            throws Exception {
        return runAllTestSuits(testProfile, techspec, appspec, "all", new AllSuiteFilesFilter());
    }

    public static ProjectTestResults runTestSuitsWithRegex(TestProfile testProfile, Techspec techspec, Appspec appspec,
            String regex) throws Exception {
        return runAllTestSuits(testProfile, techspec, appspec, "regex", new RegexFileFilter(regex));
    }

    /**
     * Use this advanced interface to only run tests approved by this filter
     *
     * @param appspec
     * @param filenameFilter
     * @return
     * @throws Exception
     *
     * @author Vineet Manohar
     */
    private static ProjectTestResults runAllTestSuits(TestProfile testProfile, Techspec techspec, Appspec appspec,
            String filterName, FileFilter fileFilter) throws Exception {
        // prepare tests
        TestPreparationUtil.prepareAllTestSuites(testProfile, techspec, appspec, filterName, fileFilter);

        File suiteTargetDirectory = new File("target" + File.separator + "clickframes" + File.separator + "selenium"
                + File.separator + ClickframeUtils.convertSlashToPathSeparator(filterName) + File.separator + "tests");

        Properties properties = new Properties();

        properties.put("browser", testProfile.getBrowser());

        properties.put("suite", suiteTargetDirectory.getAbsolutePath());

        if (testProfile.getBaseUrl() != null) {
            properties.put("startURL", testProfile.getBaseUrl());
        }

        File resultsDir = new File("target" + File.separator + "clickframes" + File.separator + "selenium"
                + File.separator + ClickframeUtils.convertSlashToPathSeparator(filterName) + File.separator + "results");

        FileUtils.deleteDirectory(resultsDir);
        resultsDir.mkdirs();
        properties.put("result", resultsDir.getAbsolutePath());

        // prepare user extensions
        File userExtensionsFile = new File(System.getProperty("java.io.tmpdir"), "user-extensions.js");
        if (userExtensionsFile.exists()) {
            userExtensionsFile.delete();
        }

        IOUtils.copy(TestRunner.class.getResourceAsStream("/user-extensions.js"), new FileOutputStream(
                userExtensionsFile));
        properties.put("userExtensions", userExtensionsFile.getAbsolutePath());

        MultiHTMLSuiteRunner runner = null;

        boolean hasFiles = true;

        if (hasFiles) {
            runner = MultiHTMLSuiteRunner.execute(properties);
        }

        ProjectTestResults projectTestResults = createFrom(runner);

        // serialize the projectTestResults using java serialization
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(new File(resultsDir,
                "testResults.xml"))));
        e.writeObject(projectTestResults);
        e.close();

        // write test result summary
        writeTestResultSummaryFile(techspec, appspec, projectTestResults, filterName);

        // aggregrateTestResults(techspec, appspec, filterName);

        return projectTestResults;
    }

    private static void validateFilePathLength(File targetFile) {
        if (targetFile.getAbsolutePath().length() > MAX_FILE_PATH) {
            throw new AppspecEnvironmentConstraintException(
                    "The maximum file path should not exceed "
                            + MAX_FILE_PATH
                            + ", "
                            + targetFile.getAbsolutePath()
                            + " ("
                            + targetFile.getAbsolutePath().length()
                            + " chars). You can shorten your filename, or change your appspec base directory to a smaller path like C:\\dev");
        }
    }

    private static void writeTestResultSummaryFile(Techspec techspec, Appspec appspec,
            ProjectTestResults projectTestResults, String filterName) throws IOException {
        // Map<String, Object> additionalParams = new HashMap<String, Object>();
        // additionalParams.put("projectTestResults", projectTestResults);
        // Page page = null;
        // codeGenerator.generateArtifact(additionalParams, appspec, page, null,
        // null, null, "selenium/" + filterName
        // + "/results", "target/clickframes/selenium/results/index.html.vm",
        // "index.html");
        codeGenerator.generateArtifact("target/clickframes/selenium/results/index.html.vm",
                "target/clickframes/selenium/" + filterName + "/results", "index.html", "techspec", techspec,
                "appspec", appspec, "projectTestResults", projectTestResults);
    }

    private static ProjectTestResults createFrom(MultiHTMLSuiteRunner runner) throws IOException {
        ProjectTestResults projectTestResults = new ProjectTestResults();

        if (runner == null) {
            projectTestResults.setFailedCount(0);
            projectTestResults.setPassedCount(0);

            projectTestResults.setStartTime(new Date());
            projectTestResults.setEndTime(new Date());

            return projectTestResults;
        }

        // high level
        projectTestResults.setFailedCount(runner.getFailedCount());
        projectTestResults.setPassedCount(runner.getPassedCount());

        projectTestResults.setStartTime(new Date(runner.getStartTime()));
        projectTestResults.setEndTime(new Date(runner.getEndTime()));

        // per suite
        for (HTMLSuite suite : runner.getHtmlSuiteList()) {
            ProjectTestResult projectTestResult = new ProjectTestResult();

            projectTestResult.setTestName(suite.getSuiteFile().getName().replaceAll(".html$", ""));
            projectTestResult.setBrowser(suite.getBrowser());
            projectTestResult.setPassed(suite.isPassed());

            projectTestResult.setHtmlOutput(FileUtils.readFileToString(suite.getResultFile()));

            projectTestResults.getProjectTestResultList().add(projectTestResult);
        }

        return projectTestResults;
    }

    /**
     * test results generated in
     * target/clickframes/selenium/results/20090106134101/firefox/ have one
     * result file per suite. Example file is:
     * pages_accountConfirmation_linkSets_global_suite_result.html.
     *
     * @author Vineet Manohar
     * @param filterName
     * @throws IOException
     */
    private static File aggregrateTestResults(Techspec techspec, Appspec appspec, String filterName) throws IOException {
        return aggregrateTestResults(techspec.getOutputDirectory(), filterName);
    }

    static File aggregrateTestResults(File outputDirectory, String filterName) throws IOException {
        // firefox/pages_accountConfirmation_linkSets_global_suite_result.html
        // and so on
        File tmpFile = File.createTempFile("selenium-testresults", ".zip");
        ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(tmpFile));

        File resultsDirBase = new File(outputDirectory, "selenium" + File.separator
                + ClickframeUtils.convertSlashToPathSeparator(filterName) + File.separator + "results");

        // get the newest directory
        File resultsDir = getNewestDir(resultsDirBase);

        // firefox/
        // firefox/firefox/pages_accountConfirmation_linkSets_global_suite_result.html
        for (File browserOrIndexFile : resultsDir.listFiles()) {
            if (browserOrIndexFile.isDirectory()) {
                for (File testResultFile : (Collection<File>) FileUtils.listFiles(browserOrIndexFile,
                        new String[] { "html" }, false)) {
                    zipStream.putNextEntry(new ZipEntry(browserOrIndexFile.getName() + File.separator
                            + testResultFile.getName()));
                    IOUtils.copy(new FileInputStream(testResultFile), zipStream);
                }
            } else {
                if (browserOrIndexFile.getName().equals("testResults.xml")) {
                    zipStream.putNextEntry(new ZipEntry(browserOrIndexFile.getName()));
                    IOUtils.copy(new FileInputStream(browserOrIndexFile), zipStream);
                }
            }
        }

        zipStream.finish();

        File destination = new File(resultsDirBase, "testResults.zip");

        FileUtils.copyFile(tmpFile, destination);

        System.out.println(destination.getAbsolutePath());

        return destination;
    }

    /**
     * get the newest directory with this format 20090106134101
     *
     * @param resultsDirBase
     * @return
     *
     * @author Vineet Manohar
     */
    private static File getNewestDir(File resultsDirBase) {
        return resultsDirBase;
    }
}
