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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.AppspecCustomizer;
import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Fact;
import org.clickframes.model.Form;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.util.CodeGenerator;

/**
 * generates selenium tests from clickframes
 *
 * @author Vineet Manohar
 */
public class TestGenerator {
    private static final Log log = LogFactory.getLog(TestGenerator.class);
    private static CodeGenerator codeGenerator = new CodeGenerator();

    private static final String TEST_FOLDER = "src/test/selenium/clickframes";
    public static final String PAGE_PREFIX = "_pg";
    public static final String SUITE_PREFIX = "TS";

    public static void generateTestStrategy(Appspec appspec, Map<String, Object> params) throws IOException {
        params.put("appspec", appspec);
        codeGenerator.generateCodeOrArtifact(true, params, "testStrategy.html.vm", "", "testStrategy.html");
    }

    /**
     * Generate tests for the entire application
     *
     * @param appspec
     * @throws IOException
     *
     * @author Vineet Manohar
     */
    public static void generateAllTests(Appspec appspec, AppspecCustomizer appspecCustomizer) throws IOException {
        log.info("Running post processor...");
        appspecCustomizer.process(appspec);

        log.info("Generating tests...");

        {
            // project -- application level one time test suites
            String templateName = "src/test/selenium/clickframes/applicationInitialize.html.vm";
            String fileName = "TC" + "_applicationInitialize.htm";
            codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec);
        }

        {
            // project -- applicationLoginConditional.html.vm
            String templatePath = "src/test/selenium/clickframes/applicationLoginConditional.html.vm";
            String generatedFilePath = "TC" + "_applicationLoginConditional.htm";
            codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec);
        }

        {
            // project -- application level one time test suites - to support
            // login
            String templateName = "src/test/selenium/clickframes/applicationLogin.html.vm";
            String fileName = "TC" + "_applicationLogin.htm";
            codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec);
        }

        generateProjectLinkSetTestCases(appspec);

        for (Page page : appspec.getPages()) {
            generatePageTests(appspec, page);
        }
    }

    private static void generateProjectLinkSetTestCases(Appspec appspec) throws IOException {
        for (LinkSet linkSet : appspec.getLinkSets()) {
            for (Link link : linkSet.getLinks()) {
                {
                    // project -- global link set -- lsLinkExists.html.vm
                    String templatePath = "src/test/selenium/clickframes/linkset/link/lsLinkExists.html.vm";
                    String fileName = "TC" + "_ls" + linkSet.getId() + "_lnk" + link.getId() + "_lsLinkExists.htm";
                    codeGenerator.generateCode(templatePath, TEST_FOLDER, fileName, "appspec", appspec, "linkSet",
                            linkSet, "link", link);
                }
                if (link.isInternal()) {
                    // project -- linkSet -- internal links -- test cases
                    String templateName = "src/test/selenium/clickframes/linkset/link/lsInternalLink_verifyDestination.html.vm";
                    String fileName = "TC" + "_ls" + linkSet.getId() + "_lnk" + link.getId() + "_verifyDestination.htm";
                    codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "linkSet",
                            linkSet, "link", link);
                } else {
                    // project -- linkSet -- external links -- test cases
                    String templateName = "src/test/selenium/clickframes/linkset/link/lsExternalLink_verifyDestination.html.vm";
                    String fileName = "TC" + "_ls" + linkSet.getId() + "_lnk" + link.getId() + "_verifyDestination.htm";
                    codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "linkSet",
                            linkSet, "link", link);
                }
            }
        }
    }

    /**
     * Generate tests for a given page
     *
     * @param appspec
     * @param page
     * @throws IOException
     *
     * @author Vineet Manohar
     */
    static void generatePageTests(Appspec appspec, Page page) throws IOException {
        {
            // project -- page -- navigateTo
            String templateName = "src/test/selenium/clickframes/page/navigate.html.vm";
            String fileName = "TC" + PAGE_PREFIX + page.getId() + "_navigate.htm";
            codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page);
        }

        {
            // project -- page -- elementsExist.suite.html.vm
            String templateName = "src/test/selenium/clickframes/page/elementsExist.suite.html.vm";
            String fileName = SUITE_PREFIX + PAGE_PREFIX + page.getId() + "_elementsExist.suite.html";
            codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page);
        }

        // all link sets on the page and global link sets
        for (LinkSet linkSet : page.getAllApplicableLinkSets()) {
            { // project -- page -- linkSet
                String templateName = "src/test/selenium/clickframes/page/verifyLinks.suite.html.vm";
                String fileName = SUITE_PREFIX + PAGE_PREFIX + page.getId() + "_ls" + linkSet.getId()
                        + "_verifyLinks.suite.html";
                codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                        "linkSet", linkSet);
            }
        }

        generateTestsForPageLinks(appspec, page);
        generateTestsForPageFacts(appspec, page);
        generateTestsForPageFormInputs(appspec, page);
    }

    private static void generateTestsForPageFormInputs(Appspec appspec, Page page) throws IOException {
        for (Form form : page.getForms()) {
            for (SingleUserInput input : form.getInputs()) {
                {
                    // project -- page -- form -- input -- inputExists.html.vm
                    String templateName = "src/test/selenium/clickframes/page/form/input/inputExists.html.vm";
                    String fileName = "TC" + PAGE_PREFIX + page.getId() + "_fm" + form.getId() + "_inp" + input.getId()
                            + "_inputExists.htm";
                    codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                            "form", form, "input", input);
                }
                {
                    // project -- page -- form -- input -- populate
                    String templateName = "src/test/selenium/clickframes/page/form/input/" + input.getType()
                            + "_populate.html.vm";
                    String fileName = "TC" + PAGE_PREFIX + page.getId() + "_fm" + form.getId() + "_inp" + input.getId()
                            + "_populate.htm";
                    codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                            "form", form, "input", input);
                }
                {
                    // project -- page -- form -- input -- store value positive
                    String templateName = "src/test/selenium/clickframes/page/form/input/storeValuePositive.html.vm";
                    String fileName = "TC" + PAGE_PREFIX + page.getId() + "_fm" + form.getId() + "_inp" + input.getId()
                            + "_storeValuePositive1.htm";
                    codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                            "form", form, "input", input);
                }
                {
                    // project -- page -- form -- input --
                    // verifyPositive.html.vm
                    String templateName = "src/test/selenium/clickframes/page/form/input/verifyPositive.html.vm";
                    String fileName = "TC" + PAGE_PREFIX + page.getId() + "_fm" + form.getId() + "_inp" + input.getId()
                            + "_verifyPositive.htm";
                    codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                            "form", form, "input", input);
                }
                {
                    // project -- page -- form -- input --
                    // verifyPositive.suite.html.vm
                    String templateName = "src/test/selenium/clickframes/page/form/input/verifyPositive.suite.html.vm";
                    String fileName = SUITE_PREFIX + PAGE_PREFIX + page.getId() + "_fm" + form.getId() + "_inp"
                            + input.getId() + "_verifyPositive1.suite.html";
                    codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                            "form", form, "input", input);
                }

                generateTestsForPageFormInputValidations(appspec, page, form, input);
            }
        }
    }

    private static void generateTestsForPageFormInputValidations(Appspec appspec, Page page, Form form,
            SingleUserInput input) throws IOException {
        for (Validation validation : input.getValidations()) {
            {
                // project -- page -- form -- input -- validation --
                // storeValue.html.vm
                String templatePath = "src/test/selenium/clickframes/page/form/input/validation/storeValue.html.vm";
                String generatedFilePath = "TC" + PAGE_PREFIX + page.getId() + "_fm" + form.getId() + "_inp"
                        + input.getId() + "_val" + validation.getId() + "_storeValue1.htm";
                codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec, "page",
                        page, "form", form, "input", input, "validation", validation);
            }

            {
                // project -- page -- form -- input -- validation --
                // verifyValidation.html.vm
                String templatePath = "src/test/selenium/clickframes/page/form/input/validation/verifyValidation.html.vm";
                String generatedFilePath = "TC" + PAGE_PREFIX + page.getId() + "_fm" + form.getId() + "_inp"
                        + input.getId() + "_val" + validation.getId() + "_verifyValidation.htm";
                codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec, "page",
                        page, "form", form, "input", input, "validation", validation);
            }

            {
                // project -- page -- form -- input -- validation --
                // verifyValidation.suite.html.vm
                String templatePath = "src/test/selenium/clickframes/page/form/input/validation/verifyValidation.suite.html.vm";
                String generatedFilePath = SUITE_PREFIX + PAGE_PREFIX + page.getId() + "_fm" + form.getId() + "_inp"
                        + input.getId() + "_val" + validation.getId() + "_verifyValidation1.suite.html";
                codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec, "page",
                        page, "form", form, "input", input, "validation", validation);
            }
        }
    }

    private static void generateTestsForPageFormActions(Appspec appspec, Page page, Form form, Action action)
            throws IOException {
        String formPrefix = form != null ? "_fm" + form.getId() : "";

        {
            // project -- page -- form -- action -- execute.html.vm
            String templatePath = "src/test/selenium/clickframes/page/form/action/execute.html.vm";
            String generatedFilePath = "TC" + PAGE_PREFIX + page.getId() + formPrefix + "_act" + action.getId()
                    + "_execute.htm";
            codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec, "page", page,
                    "form", form, "action", action);
        }

        {
            // project -- page -- form -- action -- actionExists.html.vm
            String templatePath = "src/test/selenium/clickframes/page/form/action/actionExists.html.vm";
            String generatedFilePath = "TC" + PAGE_PREFIX + page.getId() + formPrefix + "_act" + action.getId()
                    + "_actionExists.htm";
            codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec, "page", page,
                    "form", form, "action", action);
        }

        for (Link outcome : action.getOutcomes()) {
            {
                // project -- page -- form -- action -- storeValues.html.vm
                String templatePath = "src/test/selenium/clickframes/page/form/action/outcome/storeValues.html.vm";
                String generatedFilePath = "TC" + PAGE_PREFIX + page.getId() + formPrefix + "_act" + action.getId()
                        + "_out" + outcome.getId() + "_storeValues1.htm";
                codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec, "page",
                        page, "form", form, "action", action, "outcome", outcome);
            }
            {
                // project -- page -- form -- action --
                // verifyOutcome.html.vm
                String templatePath = "src/test/selenium/clickframes/page/form/action/outcome/verifyOutcome.html.vm";
                String generatedFilePath = "TC" + PAGE_PREFIX + page.getId() + formPrefix + "_act" + action.getId()
                        + "_out" + outcome.getId() + "_verifyOutcome.htm";
                codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec, "page",
                        page, "form", form, "action", action, "outcome", outcome);
            }
            {
                // project -- page -- form -- action --
                // verifyOutcome.suite.html.vm
                String templatePath = "src/test/selenium/clickframes/page/form/action/outcome/verifyOutcome.suite.html.vm";
                String generatedFilePath = SUITE_PREFIX + PAGE_PREFIX + page.getId() + formPrefix + "_act"
                        + action.getId() + "_out" + outcome.getId() + "_verifyOutcome1.suite.html";
                codeGenerator.generateCode(templatePath, TEST_FOLDER, generatedFilePath, "appspec", appspec, "page",
                        page, "form", form, "action", action, "outcome", outcome);
            }
        }
    }

    private static void generateTestsForPageFacts(Appspec appspec, Page page) throws IOException {
        for (Fact fact : page.getFacts()) {
            // TS - one suite per link
            // project -- page -- link
            // TODO: Nicole set the template name
            String templateName = "src/test/selenium/clickframes/page/link/verifyDestination.suite.html.vm";
            // TODO: Nicole set the file name
            String fileName = SUITE_PREFIX + PAGE_PREFIX + page.getId() + "_fct" + fact.getId()
                    + "_verifyDestination.suite.html";
            codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page, "fact",
                    fact);
        }
    }

    private static void generateTestsForPageLinks(Appspec appspec, Page page) throws IOException {
        // all links on the page
        for (Link link : page.getLinks()) {
            {
                // TS - one suite per link
                // project -- page -- link
                String templateName = "src/test/selenium/clickframes/page/link/verifyDestination.suite.html.vm";
                String fileName = SUITE_PREFIX + PAGE_PREFIX + page.getId() + "_lnk" + link.getId()
                        + "_verifyDestination.suite.html";
                codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                        "link", link);
            }

            {
                // project -- page -- link -- linkExists.html.vm
                String templateName = "src/test/selenium/clickframes/page/link/linkExists.html.vm";
                String fileName = "TC" + PAGE_PREFIX + page.getId() + "_lnk" + link.getId() + "_linkExists.htm";
                codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                        "link", link);
            }

            if (link.isInternal()) {
                // TC - one test case for internal link
                // project -- page -- internal link test case
                String templateName = "src/test/selenium/clickframes/page/link/internalLink_verifyDestination.html.vm";
                String fileName = "TC" + PAGE_PREFIX + page.getId() + "_lnk" + link.getId() + "_verifyDestination.htm";
                codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                        "link", link);
            } else {
                // TC - one test case for external link
                // project -- page -- external link test case
                String templateName = "src/test/selenium/clickframes/page/link/externalLink_verifyDestination.html.vm";
                String fileName = "TC" + PAGE_PREFIX + page.getId() + "_lnk" + link.getId() + "_verifyDestination.htm";
                codeGenerator.generateCode(templateName, TEST_FOLDER, fileName, "appspec", appspec, "page", page,
                        "link", link);
            }
        }
    }
}