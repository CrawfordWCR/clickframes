package org.clickframes.testframes.selenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.clickframes.VelocityHelper;
import org.clickframes.testframes.StepFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utilities related to selenium test cases and test suites
 *
 * @author Vineet Manohar
 */
public class SeleniumUtils {
    public static File testSuiteToTestCase(File sourceSuiteFile, File targetSuiteFile) throws IOException {
        return flattenTestSuiteToTestCase(sourceSuiteFile, targetSuiteFile, null);
    }

    public static File flattenTestSuiteToTestCase(File testSuiteFile, File outputFile, StepFilter stepFilter)
            throws IOException {
        SeleniumTestSuite suite = readTestSuite(testSuiteFile);

        SeleniumTestCase flattenedTestCase = new SeleniumTestCase();
        flattenedTestCase.setName(suite.getName());

        flattenedTestCase.getSteps().add(
                new SeleniumTestStep("echo", "############# Test suite has " + suite.getTestCases().size()
                        + " test cases #############", testSuiteFile.getCanonicalPath()));

        for (SeleniumTestCase testCase : suite.getTestCases()) {
            flattenedTestCase.getSteps().add(
                    new SeleniumTestStep("echo", "############# Including test case (" + testCase.getFile().getName()
                            + ") #############", testCase.getFile().getCanonicalPath()));
            for (SeleniumTestStep step : testCase.getSteps()) {
                SeleniumTestStep newStep;

                if (stepFilter != null) {
                    newStep = stepFilter.filter(testCase, step);
                } else {
                    newStep = step;
                }

                if (newStep != null) {
                    flattenedTestCase.getSteps().add(newStep);
                }
            }
        }

        return toFile(flattenedTestCase, outputFile);
    }

    static File toFile(SeleniumTestCase flattenedTestCase, File outputFile) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("testCase", flattenedTestCase);
        String macro = "org/clickframes/testframes/selenium/test-case.html.vm";

        VelocityHelper.runMacro(params, macro, outputFile);
        return outputFile;
    }

    public static SeleniumTestSuite readTestSuite(File testSuiteFile) throws IOException {
        SeleniumTestSuite suite = new SeleniumTestSuite();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));

        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not create DOM parser", e);
        }

        Document doc;
        try {
            doc = db.parse(new FileInputStream(testSuiteFile));
        } catch (SAXException e) {
            throw new RuntimeException("Invalid input, not a valid XML", e);
        }

        Element root = doc.getDocumentElement();
        root.normalize();

        // name
        {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            try {
                XPathExpression expr = xpath.compile("//head/title");
                NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    Element title = (Element) node;

                    // set name
                    suite.setName(title.getTextContent());
                    // testCase.setFile(new File(a.getAttribute("href")));
                    // suite.getTestCases().add(testCase);
                }
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Wrong xpath, developer error", e);
            }
        }

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        try {
            XPathExpression expr = xpath.compile("//td/a");
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element a = (Element) node;
                // try {
                SeleniumTestCase testCase = SeleniumTestCase.create(a.getTextContent(), new File(testSuiteFile
                        .getParent(), a.getAttribute("href")));
                suite.getTestCases().add(testCase);
                // } catch (InvalidTestSuiteException e) {
                // stop or continue?
                // }
            }
        } catch (TestCaseNotFoundException e) {
            throw new InvalidTestSuiteException("Could not load test suite: '" + testSuiteFile.getAbsolutePath()
                    + "' because an included test case '" + e.getFile().getAbsolutePath() + "' could not be found", e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Wrong xpath, developer error", e);
        }

        return suite;
    }

    /**
     *
     * @param listOfFlattenedSuites
     * @param masterTestSuite
     * @return the masterTestSuite
     *
     * @author Vineet Manohar
     */
    public static File testCasesToTestSuite(Map<File, String> testCases, File masterTestSuite) {
        SeleniumTestSuite seleniumTestSuite = new SeleniumTestSuite();
        seleniumTestSuite.setName("Master Suite");
        for (File testCase : testCases.keySet()) {
            String name = testCases.get(testCase);
            if (name == null) {
                name = testCase.getName();
            }
            SeleniumTestCase seleniumTestCase = SeleniumTestCase.create(name, testCase);
            seleniumTestSuite.getTestCases().add(seleniumTestCase);
        }

        return toFile(seleniumTestSuite, masterTestSuite);
    }

    private static File toFile(SeleniumTestSuite seleniumTestSuite, File outputFile) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("testSuite", seleniumTestSuite);
        String macro = "org/clickframes/testframes/selenium/test-suite.html.vm";

        VelocityHelper.runMacro(params, macro, outputFile);
        return outputFile;
    }
}