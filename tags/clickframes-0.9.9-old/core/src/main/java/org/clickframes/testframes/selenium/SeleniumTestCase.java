package org.clickframes.testframes.selenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.clickframes.util.ClickframeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Represents a selenium test case
 * 
 * @author Vineet Manohar
 */
public class SeleniumTestCase {
    private String name;
    private File file;
    private List<SeleniumTestStep> steps = new ArrayList<SeleniumTestStep>();

    public String getName() {
        return name;
    }

    /**
     * This method is useful when printing the name to XML document
     * 
     * @return an xml escaped version of name
     * 
     * @author Vineet Manohar
     */
    public String getNameEscaped() {
        return ClickframeUtils.escapeXml(getName());
    }

    /**
     * @deprecated use getNameEscaped() - this method is easy to group with the
     *             original method
     * 
     * @return
     * 
     * @author Vineet Manohar
     */
    @Deprecated
    public String getEscapedName() {
        return getNameEscaped();
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<SeleniumTestStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SeleniumTestStep> steps) {
        this.steps = steps;
    }

    public static SeleniumTestCase readTestCase(File file, String name) {
        SeleniumTestCase testCase = new SeleniumTestCase();

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
            doc = db.parse(new FileInputStream(file));
        } catch (SAXException e) {
            throw new RuntimeException("Invalid input, '" + file.getAbsolutePath() + "' not a valid XML", e);
        } catch (FileNotFoundException e) {
            throw new TestCaseNotFoundException(file, e);
        } catch (IOException e) {
            throw new RuntimeException("The test case could not be opened: file=" + file.getAbsolutePath() + ", name="
                    + name, e);
        }

        Element root = doc.getDocumentElement();
        root.normalize();

        // set file
        testCase.setFile(file);

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

                    if (name == null) {
                        name = title.getTextContent();
                    }

                    // set name
                    testCase.setName(name);
                    // testCase.setFile(new File(a.getAttribute("href")));
                    // suite.getTestCases().add(testCase);
                }
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Wrong xpath, developer error", e);
            }
        }

        // steps
        {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            try {
                XPathExpression expr = xpath.compile("//tbody/tr");
                NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    Element tr = (Element) node;

                    NodeList tds = tr.getElementsByTagName("td");

                    String command = tds.item(0).getTextContent();
                    String target = tds.item(1).getTextContent();
                    String value = tds.item(2).getTextContent();

                    SeleniumTestStep step = new SeleniumTestStep(command, target, value);
                    testCase.getSteps().add(step);
                }
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Wrong xpath, developer error", e);
            }
        }

        return testCase;
    }

    public static SeleniumTestCase create(String name, File file) {
        return readTestCase(file, name);
    }
}
