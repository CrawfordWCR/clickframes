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

package org.clickframes.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.VelocityHelper;
import org.clickframes.techspec.Techspec;

/**
 * @author Vineet Manohar
 */
public class CodeGenerator {
    @SuppressWarnings("unused")
    private final Log logger = LogFactory.getLog(getClass());
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("^(.*)/([^/]+).vm$");
    private static final Pattern POM_PATTERN = Pattern.compile("^([^/]+).vm$");
    private static final Log log = LogFactory.getLog(CodeGenerator.class);

    /**
     * No-op constructor for now.
     */
    public CodeGenerator() {
        // No-op constructor for now.
    }

    /**
     *
     * @param params
     *            map of substitution variables
     * @param realDir
     *            code should be generated in this directory for creation only,
     *            no overwrite allowed
     * @param targetDir
     *            this is the fallback directory, overwriting existing file is
     *            allowed in this directory
     * @param templateName
     *            the template to be used to generated code
     * @param filename
     *            the name of the output file
     *
     * @author Vineet Manohar
     * @return
     * @throws IOException
     */
    File generateCode(CodeOverwritePolicy policy, Map<String, Object> params, File realDir, File targetDir,
            String templateName, String filename) throws IOException {
        File generatedFile = File.createTempFile("clickframes-", filename);
        generatedFile.deleteOnExit();

        VelocityHelper.runMacro(params, templateName, generatedFile);

        File realFile = new File(realDir, filename);
        File targetFile = new File(targetDir, filename);
        File destinationFile;

        switch (policy) {
            case OVERWRITE_ALLOWED:
                destinationFile = realFile;
                break;

            case OVERWRITE_NOT_ALLOWED:
                destinationFile = targetFile;
                break;

            case OVERWRITE_IF_UNCHANGED:
                if (!realFile.exists()) {
                    destinationFile = realFile;
                    break;
                }

                if (!modifiedSinceLastGeneration(realFile)) {
                    // quit, if the new file is 'logically' same as the next
                    // file. e.g. if
                    // only white space changes have been made
                    if (generatedFileLogicallySameAsExistingFile(generatedFile, realFile)) {
                        return realFile;
                    }

                    destinationFile = realFile;
                    break;
                }

                destinationFile = targetFile;
                break;
            default:
                throw new RuntimeException("Developer exception: CodeOverwritePolicy not implemented for this policy: "
                        + policy);
        }

        if (log.isDebugEnabled() && destinationFile.equals(realFile)) {
            if (realFile.exists()) {
                // log.debug(filename + " updated, being replaced.");
                // log.debug(realFile.getAbsolutePath() +
                // " updated, being replaced.");
            } else {
                // log.debug(filename + " created.");
            }
        }

        destinationFile.getParentFile().mkdirs();

        // cannot rename to an existing file with windows
        if (destinationFile.exists()) {
            destinationFile.delete();
        }

        storeChecksumInFile(generatedFile);
        FileUtils.copyFile(generatedFile, destinationFile);

        return destinationFile;
    }

    /**
     * use case: the user formats the white-space of existing file. the
     * generated file is defined to be the same as the existing file if the only
     * changes made to the existing file is white space and formatting related,
     * such that our checksum algorithm computes the same checksum
     *
     * @param generatedFileWithoutChecksum
     * @param existingUnmodifiedFileWithChecksum
     * @return
     *
     * @author Vineet Manohar
     * @throws IOException
     */
    private boolean generatedFileLogicallySameAsExistingFile(File generatedFileWithoutChecksum,
            File existingUnmodifiedFileWithChecksum) throws IOException {
        // existing file with checksum
        String existingFileWithChecksumAsString = FileUtils.readFileToString(existingUnmodifiedFileWithChecksum);

        // is checksum string present
        Pattern pattern = Pattern.compile(".*clickframes::(version=(\\d+))::clickframes.*", Pattern.DOTALL);
        Matcher m = pattern.matcher(existingFileWithChecksumAsString);
        if (!m.matches()) {
            throw new IllegalArgumentException("File does not have checksum stored: "
                    + existingUnmodifiedFileWithChecksum);
        }
        long checksumFoundInFile = Long.parseLong(m.group(2));

        // generated file without checksum
        String generatedContentWithoutChecksum = FileUtils.readFileToString(generatedFileWithoutChecksum);
        long checksumOfGeneratedContent = checksum(generatedContentWithoutChecksum);

        return checksumFoundInFile == checksumOfGeneratedContent;
    }

    public boolean modifiedSinceLastGeneration(File file) throws IOException {
        String fileAsString = FileUtils.readFileToString(file);
        // is checksum string present
        Pattern pattern = Pattern.compile(".*clickframes::(version=(\\d+))::clickframes.*", Pattern.DOTALL);
        Matcher m = pattern.matcher(fileAsString);
        if (!m.matches()) {
            // no match found, can't guarentee that it has not changed
            return true;
        }
        long checksumFoundInFile = Long.parseLong(m.group(2));

        // calculate checksum of the remaining file
        String remainingFile = removeCommentForFilename(file.getName(), m.group(1), fileAsString);
        long checksum = checksum(remainingFile);
        return checksum != checksumFoundInFile;
    }

    public static void storeChecksumInFile(File file) throws IOException {
        long checksum = checksum(FileUtils.readFileToString(file));

        @SuppressWarnings("unchecked")
        List<String> lines = FileUtils.readLines(file);

        // add checksum at the end of the file
        lines.add(commentForFilename(file.getName(), "version=" + checksum));

        FileUtils.writeLines(file, lines);
    }

    private static String commentForFilename(String filename, String comment) {
        if (filename.endsWith(".xml") || filename.endsWith(".xhtml") || filename.endsWith(".htm")
                || filename.endsWith(".html") || filename.endsWith(".php")) {
            return "<!-- clickframes::" + comment + "::clickframes -->";
        }
        if (filename.endsWith(".java")) {
            return "// clickframes::" + comment + "::clickframes";
        }
        if (filename.endsWith(".js")) {
            return "/* clickframes::" + comment + "::clickframes */";
        }
        if (filename.endsWith(".properties")) {
            return "# clickframes::" + comment + "::clickframes";
        }
        if (filename.endsWith(".jsp")) {
            return "<%-- clickframes::" + comment + "::clickframes--%>";
        }
        if (filename.endsWith(".txt")) {
            return "";
        }
        if (filename.endsWith(".css")) {
            return "/* " + comment + " */";
        }
        if (filename.endsWith(".vm")) {
            return "## " + comment;
        }
        if (filename.endsWith(".htaccess")) {
            return "# " + comment;
        }

        throw new RuntimeException("File extension not supported: " + filename);
    }

    private static long checksum(String text) throws IOException {
        File file = File.createTempFile("clickframes-", ".tmp");
        FileUtils.writeStringToFile(file, normalizeWhitespaces(text));
        return FileUtils.checksumCRC32(file);
    }

    private static String normalizeWhitespaces(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private static String removeCommentForFilename(String filename, String comment, String text) {
        String commentForFile = commentForFilename(filename, comment);
        return text.replaceAll(commentForFile, "");
    }

    public void generateCodeOrArtifact(boolean artifactOld, Map<String, Object> params, String inputPath,
            String outputDirectory, String outputFilename) throws IOException {
        Techspec techspec = (Techspec) params.get("techspec");

        if (techspec.getOutputDirectory() == null) {
            throw new RuntimeException(
                    "Techspec must be configured correctly to generated code! OutputDirectory is null", null);
        }

        File targetDirectory = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator("target/clickframes-modified" + outputDirectory));
        File realDirectory;

        realDirectory = new File(techspec.getOutputDirectory(), ClickframeUtils
                .convertSlashToPathSeparator(outputDirectory));

        CodeOverwritePolicy codeOverwritePolicy = CodeOverwritePolicy.OVERWRITE_IF_UNCHANGED;
        generateCode(codeOverwritePolicy, params, realDirectory, targetDirectory, inputPath, outputFilename);
    }

    public void generateCode(String inputPath, String outputDirectory, String outputFilename, Object... parameters)
            throws IOException {
        Map<String, Object> params = convertParametersToMap(parameters);
        generateCodeOrArtifact(false, params, inputPath, outputDirectory, outputFilename);
    }

    // Am I really needed?
    public void generateCodeAutoscan(String inputPath, String outputDirectory, String outputFilename,
            Map<String, Object> params) throws IOException {

        generateCodeOrArtifact(false, params, inputPath, outputDirectory, outputFilename);
    }

    public void generateArtifact(String inputPath, String outputDirectory, String outputFilename, Object... parameters)
            throws IOException {
        Map<String, Object> params = convertParametersToMap(parameters);

        generateCodeOrArtifact(true, params, inputPath, outputDirectory, outputFilename);
    }

    /**
     * the template name is used to determine the name of the output file
     *
     * @throws IOException
     */
    public void generateCode(String templatePath, Object... parameters) throws IOException {
        // String filename = null;
        // String relativePath = null;

        // if template is "src/main/webapp/index.html.vm"
        // filename index.html
        // relative path is src/main/webapp
        Matcher m = TEMPLATE_PATTERN.matcher(templatePath);
        if (m.matches()) {
            String relativePath = m.group(1);
            String filename = m.group(2);
            generateCode(templatePath, relativePath, filename, parameters);
            return;
        }

        // if template is "pom.xml.vm"
        // filename pom.xml
        // relative path is ""
        m = POM_PATTERN.matcher(templatePath);
        if (m.matches()) {
            String relativePath = ".";
            String filename = m.group(1);
            generateCode(templatePath, relativePath, filename, parameters);
            return;
        }

        throw new RuntimeException("Invalid format for template path: " + templatePath
                + ", example pattern is 'src/main/webapp/index.html.vm' - must end in .vm");
    }

    /**
     * @param parameters
     *            an alternating list of key value pairs, e.g. {"key1", value1,
     *            "key2", value2} all keys are Strings
     *
     * @return
     *
     * @author Vineet Manohar
     */
    private static Map<String, Object> convertParametersToMap(Object[] parameters) {
        if (parameters.length % 2 == 1) {
            throw new RuntimeException("Name value pair list must be also even in number, found: " + parameters.length
                    + ": " + parameters);
        }
        Map<String, Object> params = new HashMap<String, Object>();
        for (int i = 0; i < parameters.length; i += 2) {
            Object key = parameters[i];
            Object value = parameters[i + 1];
            if (!(key instanceof String)) {
                throw new RuntimeException("Names passed to code generator must always be of type String, found "
                        + key.getClass() + " (" + key + ")");
            }
            params.put((String) key, value);
        }

        return params;
    }
}