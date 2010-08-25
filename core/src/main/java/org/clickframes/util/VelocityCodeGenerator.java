package org.clickframes.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.VelocityHelper;
import org.clickframes.techspec.Techspec;
import org.clickframes.util.Chunk.RegionChunk;

/**
 * Velocity based code generator
 * 
 * How it works:
 * 
 * Checksum placeholder: clickframes::checksum::clickframes Checksum calculated
 * placeholder: clickframes::34534345435::clickframes
 * 
 * Step 1: Embed checksum
 * 
 * 1) Put clickframes::checksum::clickframes anywhere in your template, usually
 * in a commented section
 * 
 * 2) The code generator first runs the template and generates a temp file. It
 * then calculates the checksum of the generated file (minus the checksum
 * placeholder) and replaces the placeholder in the generated file with the
 * calculated placeholder
 * 
 * 3) Overwrite existing files only if old one is not changed
 * 
 * Step 2: Use checksum to detect change
 * 
 * 1) Open existing file
 * 
 * 2) Remove calculated placeholder
 * 
 * 3) Remember the old checksum value
 * 
 * 4) Calculate checksum of remaining file.
 * 
 * 5) If same, then unchanged
 * 
 * @author Vineet Manohar
 */
public class VelocityCodeGenerator {
    private static Log log = LogFactory.getLog(VelocityCodeGenerator.class);

    /**
     * @param text
     * @return false if the file can be proven to not be modifed, true if either
     *         it is proven to be modified or it cannot be proven
     * 
     * @author Vineet Manohar
     */
    public static boolean isModified(String filename, String text) {
        String oldChecksum = getExistingChecksum(text);
        if (oldChecksum == null) {
            return true;
        }

        String newChecksum = calculateContentChecksum(filename, text);

        return !oldChecksum.equals(newChecksum);
    }

    /**
     * @return any existing checksum, or null if none exists
     * 
     * @author Vineet Manohar
     */
    static String getExistingChecksum(String text) {
        Pattern pattern = Pattern.compile(".*clickframes::(version=(\\d+))::clickframes.*", Pattern.DOTALL);
        Matcher m = pattern.matcher(text);
        if (!m.matches()) {
            return null;
        }
        long checksumFoundInFile = Long.parseLong(m.group(2));
        return String.valueOf(checksumFoundInFile);
    }

    /**
     * 
     * @param context
     *            a map of variable tokens to objects used to resolve the text
     * @param text
     *            the text to be resolved
     * @return resolved text
     * 
     * @author Vineet Manohar
     */
    static String resolve(Map<String, Object> context, String text) {
        return VelocityHelper.resolveText(context, text);
    }

    static String resolveTemplate(Map<String, Object> context, String templatePath) {
        return VelocityHelper.runMacro(context, templatePath);
    }

    /**
     * The checksum is calculated by removing whitespaces and checksum
     * placeholders, so that formatting and checksum values does not logically
     * "modify" a file
     * 
     * @param filename
     *            used to determine type of comments
     * @param text
     * @return a string representing the checksum of the text
     * 
     * @author Vineet Manohar
     */
    static String calculateContentChecksum(String filename, String text) {
        text = removePlaceHolders(filename, text);
        text = normalizeWhitespaces(text);
        return String.valueOf(calculateRawChecksum(text));
    }

    private static String normalizeWhitespaces(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    static long calculateRawChecksum(String text) {
        // to calculate checksum, you need to pretent to copy input from one
        // stream to another - that's how the underlying api works
        CRC32 crc = new CRC32();

        InputStream in;
        in = new CheckedInputStream(new ByteArrayInputStream(text.getBytes()), crc);
        try {
            IOUtils.copy(in, new NullOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Cannot use CodeGenerator as checksum calculation failed", e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return crc.getValue();
    }

    static String removePlaceHolders(String filename, String text) {
    	String versionRegex = getVersionCommentRegex();

        String versionRegexWithSurroundingComments = getCommentForFilename(filename, versionRegex);

        if (versionRegexWithSurroundingComments != null) {
            // first replace placeholders with comments, if possible
        	String textOrig = text;
        	text = text.replaceAll(versionRegexWithSurroundingComments, "");
        }

        // then replace any remaining placeholders including hand-authored
        // comments
        {
            String emptyPlaceholder = getEmptyVersionPlaceholder();
            String emptyPlaceholderWithSurroundingComments = getCommentForFilename(filename, emptyPlaceholder);
            if (emptyPlaceholderWithSurroundingComments != null) {
            	text = text.replaceAll(emptyPlaceholderWithSurroundingComments, "");
            }
        }

        // remove any region headers and footers
        // <!-- cf:start xxx -->
        // <!-- cf:end -->
        {
            String regionHeaderOrFooterStamp = Chunk.getCommentedRegionHeaderOrFooterRegex(filename);
            if (regionHeaderOrFooterStamp != null) {
                text = text.replaceAll(regionHeaderOrFooterStamp, "");
                // text = text.replaceAll("<!-- cf:(start|end)\\s*(.*) -->", "");
            }
        }

        return text;
    }

    /**
     * @return a regular expression for matching placeholder in text
     * 
     * @author Vineet Manohar
     */
    static String getEmptyVersionPlaceholder() {
        // return getPlaceholderWithValue("version=[^:]*");
        return getPlaceholderWithValue("");
    }

    /**
     * @author Vineet Manohar
     */
    static String getVersionCommentRegex() {
        return getPlaceholderWithValue("version=[^:]*");
    }

    static String getPlaceholderWithValue(String value) {
        return "clickframes::" + value + "::clickframes";
    }

    /**
     * Uses the context to replace variables in the inputfile and overwrite the
     * output file if not modified
     * 
     * @param context
     * @param inputFile
     * @param outputFile
     * @throws IOException
     * 
     * @author Vineet Manohar
     */
    static void generate(Map<String, Object> context, File inputFile, File outputFile) throws IOException {
        String newOutputText = generateText(context, inputFile);

        boolean overwrite = shouldOverwriteOldOutput(outputFile);

        if (overwrite) {
            FileUtils.writeStringToFile(outputFile, newOutputText);
        } else {
            // Overwriting the file was not possible. If possible replace
            // regions at least.
            boolean regionsPresent = hasRegions(inputFile);
            if (!regionsPresent) {
                return;
            }

            // replace regions with new values
            String oldOutputText = FileUtils.readFileToString(outputFile);

            String replacedRegions = replaceRegions(outputFile.getName(), newOutputText, oldOutputText);
            FileUtils.writeStringToFile(outputFile, replacedRegions);
        }
    }

    static boolean copyRegions(File src, File dest) throws IOException {
        boolean regionsPresent = hasRegions(src);
        if (!regionsPresent) {
            return false;
        }

        // replace regions with new values
        String oldOutputText = FileUtils.readFileToString(dest);

        String replacedRegions = replaceRegions(dest.getName(), FileUtils.readFileToString(src), oldOutputText);

        if (!isSameLogicalContent(dest.getName(), oldOutputText, dest.getName(), replacedRegions)) {
            // copy only if anything has changed
            FileUtils.writeStringToFile(dest, replacedRegions);
            return true;
        }
        
        return false;
    }

    /**
     * copy regions from src to dest, leaving the rest of dest unchanged
     * 
     * @param src
     * @param dest
     * @return
     * 
     * @author Vineet Manohar
     */
    static String replaceRegions(String filename, String src, String dest) {
        // convert text into a mixed list of text chunks and regions. Each
        // region has a name.
        // e.g. consider the following

        // line1
        // line2
        // <!-- clickframes::start=reg1::clickframes -->
        // verion1
        // <!-- clickframes::end=reg1::clickframes -->
        // line 3
        // 
        // The above will be converted to chunk, region, chunk
        ChunkedText chunkedSrc = Chunk.parse(filename, src);

        ChunkedText chunkedDest = Chunk.parse(filename, dest);

        for (Chunk chunk : chunkedDest.getChunks()) {
            if (chunk instanceof RegionChunk) {
                RegionChunk destRegion = (RegionChunk) chunk;

                // update this region from the src
                RegionChunk srcRegion = chunkedSrc.getRegion(destRegion.getRegionName());
                if (srcRegion != null) {
                    destRegion.setText(srcRegion.getText());
                }
            }
        }

        return chunkedDest.toText();
    }

    private static boolean hasRegions(File inputFile) throws IOException {
        String text = FileUtils.readFileToString(inputFile);

        Pattern pattern = Pattern.compile(".*cf:(start|end).*", Pattern.DOTALL);
        Matcher m = pattern.matcher(text);
        if (!m.matches()) {
            return false;
        }

        return true;
    }

    static void generate(Map<String, Object> context, String filename, String inputText, String outputPath)
            throws IOException {
        String newOutputText = generateText(context, filename, inputText);

        Techspec techspec = (Techspec) context.get("techspec");

        if (techspec.getOutputDirectory() == null) {
            throw new RuntimeException(
                    "Techspec must be configured correctly to generated code! OutputDirectory is null");
        }

        File generatedOutputFile = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator("target/clickframes-modified/" + outputPath));

        File outputFile = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator(outputPath));

        boolean exists = outputFile.exists();
        boolean overwrite = shouldOverwriteOldOutput(outputFile);

        // simplest case, fresh content
        if (!exists) {
            copyNow(newOutputText, outputFile);
            // log.info(outputFile.getAbsoluteFile() + " created.");
            return;
        }

        // continue, if exists
        if (overwrite) {
            if (!isSameLogicalContent(filename, newOutputText, filename, FileUtils.readFileToString(outputFile))) {
                copyNow(newOutputText, outputFile);
                // log.info(outputFile.getAbsolutePath() +
                // " updated, being replaced.");
            }

            return;
        }

        copyNow(newOutputText, generatedOutputFile);
    }

    /**
     * Copy source to dest, if dest does not exist or if it does dest then is
     * not modified and there are new changes worth copying over.
     * 
     * @param source
     * @param dest
     * @return true if copied, false if not copied
     * 
     * @author Vineet Manohar
     * @throws IOException
     */
    static boolean copyIfContentNotModified(File source, File dest) throws IOException {
        if (!isFileNameSafeForVelocity(dest.getName())) {
            return copyIfBinaryContentNotModified(source, dest);
        }

        if (!dest.exists() || (!isModified(dest) && !isSameLogicalContent(source, dest))) {
            boolean destExists = dest.exists();

            copyFile(source, dest);

            if (!destExists) {
                log.info(dest.getCanonicalPath() + " created");
            } else {
                log.info(dest.getCanonicalPath() + " updated");
            }

            return true;
        }

        // try to replace regions
        if (copyRegions(source, dest)) {
            log.info(dest.getCanonicalPath() + " updated (partially)");
        }

        return false;
    }

    static boolean copyIfBinaryContentNotModified(File source, File dest) throws IOException {
        boolean destExists = dest.exists();
        if (!destExists) {
            copyFile(source, dest);
            if (!destExists) {
                log.info(dest.getCanonicalPath() + " (binary file) created");
            } else {
                log.info(dest.getCanonicalPath() + " (binary file) updated");
            }

            return true;
        }
        return false;
    }

    /**
     * 
     * @param source
     * @param dest
     * @throws IOException
     * @return true if move was successful, false if target was modified and
     *         source could not be moved
     * @author Vineet Manohar
     */
    public static boolean moveIfContentNotModified(File source, File dest) throws IOException {
        copyIfContentNotModified(source, dest);

        if (isSameLogicalContent(source, dest)) {
            // source and destination have same logical content
            // they may differ in whitespace
            source.delete();
            return true;
        }

        return false;
    }

    static void generateFromTemplateBinary(Map<String, Object> context, String filename, String templatePath,
            String outputPath) throws IOException {
        Techspec techspec = (Techspec) context.get("techspec");

        File outputFile = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator(outputPath));

        // TODO: implement binary resource
        log.warn("Binary resources are not currently fully supported: " + templatePath + ", filename = " + filename);

        InputStream is = VelocityCodeGenerator.class.getResourceAsStream(templatePath);
        if (is != null) {
            outputFile.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(outputFile);
            IOUtils.copy(is, os);
            os.close();
        }
    }

    /**
     * @param context
     * @param filename
     * @param templatePath
     * @param outputPath
     * @throws IOException
     *
     * @author Vineet Manohar
     */
    public static void generateFromTemplate(Map<String, Object> context, String filename, String templatePath,
            String outputPath) throws IOException {
        if (!isFileNameSafeForVelocity(filename)) {
            generateFromTemplateBinary(context, filename, templatePath, outputPath);
            return;
        }

        String newOutputText = generateTextFromTemplate(context, filename, templatePath);

        Techspec techspec = (Techspec) context.get("techspec");

        if (techspec.getOutputDirectory() == null) {
            throw new RuntimeException(
                    "Techspec must be configured correctly to generated code! OutputDirectory is null");
        }

        File generatedOutputFile = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator("target/clickframes-modified/" + outputPath));

        File outputFile = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator(outputPath));

        boolean exists = outputFile.exists();
        boolean overwrite = shouldOverwriteOldOutput(outputFile);

        // simplest case, fresh content
        if (!exists) {
            copyNow(newOutputText, outputFile);
            // log.info(outputFile.getAbsoluteFile() + " created.");
            return;
        }

        // continue, if exists
        if (overwrite) {
            if (!isSameLogicalContent(filename, newOutputText, filename, FileUtils.readFileToString(outputFile))) {
                copyNow(newOutputText, outputFile);
                // log.info(outputFile.getAbsolutePath() +
                // " updated, being replaced.");
            }

            return;
        }

        copyNow(newOutputText, generatedOutputFile);
    }

    private static boolean isSameLogicalContent(File file1, File file2) throws IOException {
        return isSameLogicalContent(file1.getName(), FileUtils.readFileToString(file1), file2.getName(), FileUtils
                .readFileToString(file2));
    }

    /**
     * @param src
     * @param dest
     * @return true if the embedded checksum in both file (not the calculated
     *         checksum) is same
     * @throws IOException
     * 
     * @author Vineet Manohar
     */
    public static boolean isSameEmbeddedChecksum(File src, File dest) throws IOException {
        return StringUtils.equals(getExistingChecksum(FileUtils.readFileToString(src)), getExistingChecksum(FileUtils
                .readFileToString(dest)));
    }

    private static boolean isSameLogicalContent(String filename1, String text1, String filename2, String text2) {
        String checksum1 = calculateContentChecksum(filename1, text1);
        String checksum2 = calculateContentChecksum(filename2, text2);
        return checksum1.equals(checksum2);
    }

    private static void copyNow(String newOutputText, File finalOutputFile) throws IOException {
        if (finalOutputFile.getParentFile() != null) {
            finalOutputFile.getParentFile().mkdirs();
        }
        FileUtils.writeStringToFile(finalOutputFile, newOutputText);
    }

    private static void copyFile(File src, File dest) throws IOException {
        if (dest.getParentFile() != null) {
            dest.getParentFile().mkdirs();
        }
        FileUtils.copyFile(src, dest);
    }

    /**
     * @param context
     * @param inputFile
     * @param outputPath
     *            the output path may contain slashes which need to be converted
     *            to platform specific delimiters. The output paths are also
     *            relative to techspec output directory.
     * @throws IOException
     * 
     * @author Vineet Manohar
     */
    static void generateFromFile(Map<String, Object> context, File inputFile, String outputPath) throws IOException {
        generate(context, inputFile.getName(), FileUtils.readFileToString(inputFile), outputPath);
    }

    static boolean shouldOverwriteOldOutput(File file) throws IOException {
        return !file.exists() || !isModified(file);
    }

    /**
     * assumes that the file exists. Please call file.exists() before calling
     * this method.
     * 
     * @param file
     * @return
     * @throws IOException
     * 
     * @author Vineet Manohar
     */
    public static boolean isModified(File file) throws IOException {
        return isModified(file.getName(), FileUtils.readFileToString(file));
    }

    static String generateText(Map<String, Object> context, File inputFile) throws IOException {
        String inputText = FileUtils.readFileToString(inputFile);
        return generateText(context, inputFile.getName(), inputText);
    }

    static String generateText(Map<String, Object> context, String filename, String inputText) {
        // 1) generate template in tmp directory

        // add placeholder, if doesn't exist
        inputText = addPlaceholderIfApplicable(filename, inputText);

        // resolve
        String newOutputText = resolve(context, inputText);

        // calculate checksum
        String checksum = calculateContentChecksum(filename, newOutputText);

        // store checksum
        newOutputText = populateChecksum(newOutputText, checksum);

        return newOutputText;
    }

    static String generateTextFromTemplate(Map<String, Object> context, String filename, String templatePath) {
        // 1) generate template in tmp directory

        // resolve
        String newOutputText = resolveTemplate(context, templatePath);

        // add placeholder, if doesn't exist
        newOutputText = addPlaceholderIfApplicable(filename, newOutputText);

        // calculate checksum
        String checksum = calculateContentChecksum(filename, newOutputText);

        // store checksum
        newOutputText = populateChecksum(newOutputText, checksum);

        return newOutputText;
    }

    /**
     * replace existing placeholder with actual checksum value. Placeholder may
     * or may not exist.
     * 
     * @param outputText
     * @param checksum
     * @return
     * 
     * @author Vineet Manohar
     */
    static String populateChecksum(String outputText, String checksum) {
        String placeholderValue = "clickframes::" + "version=" + checksum + "::clickframes";
        String placeholderRegex = getEmptyVersionPlaceholder();
        outputText = outputText.replaceAll(placeholderRegex, placeholderValue);

        return outputText;
    }

    /**
     * If placeholder is already present don't do anything. If placeholder is
     * not present, but placeholder type can be determined by extension, place
     * it somewhere. If placeholder cannot be determined by extension, don't do
     * anything.
     * 
     * @param inputText
     * @return
     * 
     * @author Vineet Manohar
     */
    static String addPlaceholderIfApplicable(String filename, String inputText) {
        String emptyPlaceholder = getPlaceholderWithValue("");

        // placehlolder doesn't exist
        if (!inputText.contains(emptyPlaceholder)) {
            String comments = getCommentForFilename(filename, emptyPlaceholder);
            if (comments != null) {
                inputText = inputText.concat(comments);
            }
        }

        return inputText;
    }

    static String getCommentForFilename(String filename, String comment) {
        if (filename.endsWith(".xml") || filename.endsWith(".xhtml") || filename.endsWith(".htm")
                || filename.endsWith(".html")) {
            return "<!-- " + comment + " -->";
        }
        if (filename.endsWith(".java")) {
            return "// " + comment + "";
        }
        if (filename.endsWith(".js") || filename.endsWith(".css") || filename.endsWith(".php") || filename.endsWith(".sql")) {
            return "/\\* " + comment + " \\*/";
        }
        if (filename.endsWith(".properties")) {
            return "# " + comment + "";
        }
        if (filename.endsWith(".jsp")) {
            return "<%-- " + comment + "--%>";
        }
        if (filename.endsWith(".txt")) {
            return "";
        }
        if (filename.endsWith(".vm")) {
            return "## " + comment;
        }

        return null;
    }

    public static boolean isFileNameSafeForVelocity(String fileName) {
        return !fileName.matches("^.*\\.(gif|png)$");
    }

	public static void generateIgnoredFile(Techspec techspec, String filename,
			String templatePath, String outputPath) throws IOException {
		
        if (techspec.getOutputDirectory() == null) {
            throw new RuntimeException(
                    "Techspec must be configured correctly to generated code! OutputDirectory is null");
        }

        File generatedOutputFile = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator("target/clickframes-modified/" + outputPath));

        File outputFile = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator(outputPath));

		
		copyIfContentNotModified(generatedOutputFile, outputFile);
		
	}
}