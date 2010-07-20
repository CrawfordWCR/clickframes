package org.clickframes.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Vineet Manohar
 */
public class VelocityCodeGeneratorTest {
    private static String sampleWithChecksum;
    private static String sampleChecksum = "84861101";
    private static String sampleModifiedWithChecksum;
    private static String sampleNoChecksum;
    private static String sampleNoChecksumWithPlaceholder;
    private static String sampleNoChecksumWithEmbeddedRegion;
    private static String sampleWithEmbeddedRegion;
    private static String sampleWithEmbeddedRegionVersion2;
    private static String sampleWithEmbeddedRegionModified;
    private static String sampleWithEmbeddedRegionVersion2Automerged;

    @BeforeClass
    public void beforeClass() throws IOException {
        sampleWithChecksum = IOUtils.toString(this.getClass().getResourceAsStream("/sample-with-checksum.xml"));
        sampleModifiedWithChecksum = IOUtils.toString(this.getClass().getResourceAsStream(
                "/sample-modified-with-checksum.xml"));
        sampleNoChecksum = IOUtils.toString(this.getClass().getResourceAsStream("/sample-no-checksum.xml"));
        sampleNoChecksumWithPlaceholder = IOUtils.toString(this.getClass().getResourceAsStream(
                "/sample-no-checksum-with-placeholder.xml"));
        sampleNoChecksumWithEmbeddedRegion = IOUtils.toString(this.getClass().getResourceAsStream(
                "/sample-without-checksum-with-embedded-region.xml"));
        sampleWithEmbeddedRegion = IOUtils.toString(this.getClass().getResourceAsStream(
                "/sample-with-embedded-region.xml"));
        sampleWithEmbeddedRegionVersion2 = IOUtils.toString(this.getClass().getResourceAsStream(
                "/sample-with-embedded-region-version2.xml"));
        sampleWithEmbeddedRegionModified = IOUtils.toString(this.getClass().getResourceAsStream(
                "/sample-with-embedded-region-modified.xml"));
        sampleWithEmbeddedRegionVersion2Automerged = IOUtils.toString(this.getClass().getResourceAsStream(
                "/sample-with-embedded-region-version2-automerged.xml"));
    }

    @Test
    public void testIsModifiedNoChecksumWithEmbeddedRegion() {
        Assert.assertTrue(VelocityCodeGenerator.isModified(".xml", sampleNoChecksumWithEmbeddedRegion));
    }

    @Test
    public void testIsModifiedWithEmbeddedRegion() {
        Assert.assertFalse(VelocityCodeGenerator.isModified(".xml", sampleWithEmbeddedRegion));
    }

    @Test
    public void testIsModifiedWithEmbeddedRegionModified() {
        Assert.assertTrue(VelocityCodeGenerator.isModified(".xml", sampleWithEmbeddedRegionModified));
    }

    @Test
    public void testIsModifiedWhenNotModifed() {
        Assert.assertFalse(VelocityCodeGenerator.isModified(".xml", sampleWithChecksum));
    }

    @Test
    public void testIsModifiedWhenModified() {
        Assert.assertTrue(VelocityCodeGenerator.isModified(".xml", sampleModifiedWithChecksum));
    }

    @Test
    public void testIsModifiedWhenNoPlaceholderPresent() {
        Assert.assertTrue(VelocityCodeGenerator.isModified(".xml", sampleNoChecksum));
    }

    @Test
    public void testResolve() {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("name", "Vineet");
        String resolvedText = VelocityCodeGenerator.resolve(context, "My name is $name");
        Assert.assertEquals(resolvedText, "My name is Vineet");
    }

    @Test
    public void testCalculateContentChecksum() {
        assertEquals(VelocityCodeGenerator.calculateContentChecksum(".xml", sampleWithChecksum), sampleChecksum);
    }

    @Test
    public void testCalculateRawChecksum() {
        assertEquals(VelocityCodeGenerator.calculateRawChecksum(sampleNoChecksum), 2393544973L);
    }

    @Test
    public void testExistingChecksum() {
        assertEquals(VelocityCodeGenerator.getExistingChecksum(sampleWithChecksum), sampleChecksum);
    }

    @Test
    public void testGenerate() throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("name", "Vineet");

        File inputFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "sample-no-checksum.xml");
        File outputFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + ".xml");
        outputFile.deleteOnExit();
        VelocityCodeGenerator.generate(context, inputFile, outputFile);
        assertEquals(FileUtils.readFileToString(outputFile), sampleWithChecksum);
    }

    @Test
    public void testGenerateRegion() throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("name", "Vineet");

        // simulate that the user has modified the file with embedded region
        File outputFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + ".xml");
        FileUtils.writeStringToFile(outputFile, sampleWithEmbeddedRegionModified);
        outputFile.deleteOnExit();

        // simulate that a new version of the template, or new dynamic values
        // are being copied
        File inputFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "sample-with-embedded-region-version2.xml");

        VelocityCodeGenerator.generate(context, inputFile, outputFile);

        // assert that the output file has been automerged
        assertEquals(FileUtils.readFileToString(outputFile), sampleWithEmbeddedRegionVersion2Automerged);
    }

    @Test
    public void testReplaceRegions() {
        String actualMerged = VelocityCodeGenerator.replaceRegions("test.xml", sampleWithEmbeddedRegionVersion2,
                sampleWithEmbeddedRegionModified);

        // assert that the output file has been automerged
        assertEquals(actualMerged, sampleWithEmbeddedRegionVersion2Automerged);
    }

    @Test
    public void testShouldOverwriteOldOutputWhichIsModified() throws IOException {
        assertFalse(VelocityCodeGenerator.shouldOverwriteOldOutput(new File("src" + File.separator + "test"
                + File.separator + "resources" + File.separator + "sample-modified-with-checksum.xml")));
    }

    @Test
    public void testShouldOverwriteOldOutputWhichIsNotModifiedAndNoLogicalDifference() throws IOException {
        assertTrue(VelocityCodeGenerator.shouldOverwriteOldOutput(new File("src" + File.separator + "test"
                + File.separator + "resources" + File.separator + "sample-with-checksum.xml")));
    }

    @Test
    public void testShouldOverwriteOldOutputWhichDoesntExist() throws IOException {
        assertTrue(VelocityCodeGenerator.shouldOverwriteOldOutput(new File("src" + File.separator + "test"
                + File.separator + "resources" + File.separator + "sample-which-doesnt-exist.xml")));
    }

    @Test
    public void testGenerateText() {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("name", "Vineet");
        String inputText = "My name is $name";
        assertEquals(VelocityCodeGenerator.generateText(context, ".java", inputText),
                "My name is Vineet// clickframes::version=3382705350::clickframes");
    }

    @Test
    public void testPopulateChecksumWithNoPlaceholder() {
        assertEquals(VelocityCodeGenerator.populateChecksum(sampleNoChecksum, sampleChecksum), sampleNoChecksum);
    }

    @Test
    public void testPopulateChecksumWithPlaceholder() {
        assertEquals(VelocityCodeGenerator.populateChecksum(sampleNoChecksumWithPlaceholder, sampleChecksum),
                sampleWithChecksum);
    }

    @Test
    public void testAddPlaceholderIfApplicable() {
        assertEquals(VelocityCodeGenerator.addPlaceholderIfApplicable(".xml", "hello"),
                "hello<!-- clickframes::::clickframes -->");
    }
}