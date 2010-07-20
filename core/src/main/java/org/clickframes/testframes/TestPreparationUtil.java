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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.AppspecEnvironmentConstraintException;
import org.clickframes.VelocityHelper;
import org.clickframes.model.Appspec;
import org.clickframes.techspec.Techspec;
import org.clickframes.testframes.selenium.SeleniumTestCase;
import org.clickframes.testframes.selenium.SeleniumTestStep;
import org.clickframes.testframes.selenium.SeleniumUtils;
import org.clickframes.util.ClickframeUtils;

public class TestPreparationUtil {
    private static final Log log = LogFactory.getLog(TestPreparationUtil.class);

    /**
     * enforced by windows
     */
    public static final int MAX_FILE_PATH = 255;

    public static void preparePageTests(TestProfile testProfile, Techspec techspec, Appspec appspec, String pageId)
            throws Exception {
        appspec.getPage(pageId);
        prepareAllTestSuites(testProfile, techspec, appspec, "pages/" + pageId, new PageSuiteFilter(appspec, pageId));
    }

    public static void prepareAllTestSuits(TestProfile testProfile, Techspec techspec, Appspec appspec)
            throws Exception {
        prepareAllTestSuites(testProfile, techspec, appspec, "all", new AllSuiteFilesFilter());
    }

    public static void prepareTestSuitsWithRegex(TestProfile testProfile, Techspec techspec, Appspec appspec,
            String regex) throws Exception {
        prepareAllTestSuites(testProfile, techspec, appspec, "regex", new RegexFileFilter(regex));
    }

    /**
     * Use this advanced interface to only run tests approved by this filter
     *
     * @param appspec
     * @param filenameFilter
     *
     * @throws Exception
     *
     * @author Vineet Manohar
     */
    @SuppressWarnings("unchecked")
    static void prepareAllTestSuites(final TestProfile testProfile, Techspec techspec, Appspec appspec,
            String filterName, final FileFilter fileFilter) throws Exception {
        File suiteSourceDirectory = new File("src" + File.separator + "test" + File.separator + "selenium"
                + File.separator + "clickframes");

        File suiteTargetDirectory = new File("target" + File.separator + "clickframes" + File.separator + "selenium"
                + File.separator + ClickframeUtils.convertSlashToPathSeparator(filterName) + File.separator + "tests");

        // copy selected tests from source to suite directory
        FileUtils.deleteDirectory(suiteTargetDirectory);
        suiteTargetDirectory.mkdirs();

        // select for suites which match the given pattern

        IOFileFilter regexIOFilter = new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return fileFilter.accept(file);
            }

            @Override
            public boolean accept(File dir, String name) {
                return fileFilter.accept(new File(dir, name));
            }
        };

        Collection<File> filesFound;

        if (suiteSourceDirectory.exists() && suiteSourceDirectory.isDirectory()) {
            filesFound = FileUtils.listFiles(suiteSourceDirectory, regexIOFilter, FileFilterUtils
                    .makeSVNAware(FileFilterUtils.makeCVSAware(TrueFileFilter.INSTANCE)));
        } else {
            filesFound = new ArrayList<File>();
        }

        log.info(filesFound.size() + " files found for " + fileFilter + " => " + filesFound);

        Map<File, String> listOfFlattenedSuites = new LinkedHashMap<File, String>();

        StepFilter profileStepFilter = new StepFilter() {
            @Override
            public SeleniumTestStep filter(SeleniumTestCase testCase, SeleniumTestStep testStep) {
                // if testCase name is "applicationInitialize.html", apply
                // substitution vars

                if (testCase.getFile().getName().equals("applicationInitialize.html")) {
                    String target = testStep.getTarget();

                    // apply profile
                    target = VelocityHelper.resolveText(testProfile.getProperties(), target);

                    // modify step
                    testStep.setTarget(target);
                }

                return testStep;
            }
        };

        for (File sourceSuiteFile : filesFound) {
            int counter = 1;

            File targetSuiteFile;
            do {
                // name is 'some-suite.html'
                // remove suite - as multisuite runner uses suite as a keyword
                targetSuiteFile = new File(suiteTargetDirectory, sourceSuiteFile.getName().replaceAll(".html$",
                        counter++ + ".html").replaceAll("suite", ""));
            } while (targetSuiteFile.exists());

            validateFilePathLength(targetSuiteFile);

            // flatten
            SeleniumUtils.flattenTestSuiteToTestCase(sourceSuiteFile, targetSuiteFile, profileStepFilter);

            // store names
            listOfFlattenedSuites.put(targetSuiteFile, getRelativePath(sourceSuiteFile.getAbsolutePath()));
        }

        // write one master suite to suite target directory
        File masterTestSuite = new File(suiteTargetDirectory, "master-test-suite.html");
        SeleniumUtils.testCasesToTestSuite(listOfFlattenedSuites, masterTestSuite);

        File resultsDir = new File("target" + File.separator + "clickframes" + File.separator + "selenium"
                + File.separator + ClickframeUtils.convertSlashToPathSeparator(filterName) + File.separator + "results");

        FileUtils.deleteDirectory(resultsDir);

        resultsDir.mkdirs();

        // prepare user extensions
        File userExtensionsFile = new File(System.getProperty("java.io.tmpdir"), "user-extensions.js");
        if (userExtensionsFile.exists()) {
            userExtensionsFile.delete();
        }
    }

    private static String getRelativePath(String absolutePath) {
        // first convert both to forward '/'

        String slash = ClickframeUtils.getFileSeparatorLiteral();
        absolutePath = absolutePath.replaceAll(slash, "/");

        return absolutePath.replaceAll(new File("src" + File.separator + "test" + File.separator + "selenium"
                + File.separator + "clickframes").getAbsolutePath().replaceAll(slash, "/")
                + "/", "");
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
}