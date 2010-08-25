package org.clickframes.engine.manifest;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Moved to new class for now.
 * Altering from original to prepare for multiple modules.
 * @author Steven Boscarine
 *
 */
public class ManifestFileGenerator {
	//TODO:  rename
	private static final FileFilter SVN_FILTER = new AutoscanFolderFilter();
	//TODO:  rename
	private static final IOFileFilter EXCLUDE_SVN_FOLDERS = FileFilterUtils.makeSVNAware(null);
	/**
	 * Container for autoscan templates. This naming convention is mandatory.
	 */
	protected static final String AUTOSCAN_FOLDER_NAME = "autoscan";
	/**
	 * Relative filename of manifest.
	 */
	private static final String AUTOSCAN_FILE_NAME = "AutoscanManifest";
	private static final String AUTOSCAN_FILE_EXTENSION = ".txt";

	private static final Log logger = LogFactory.getLog(ManifestFileGenerator.class);

	private static final String RESOURCES_DIR = "src" + File.separator + "main" + File.separator + "resources" + File.separator;
	public static String getAbsoluteResourcesPath(File autoscanPath){
		String fullPath=autoscanPath.getAbsolutePath();
		if(!fullPath.contains(RESOURCES_DIR)){
			throw new IllegalArgumentException("can't find resources directory in " + autoscanPath.getAbsolutePath());
		}
		String resourcesDir = fullPath.substring(0, fullPath.indexOf(RESOURCES_DIR) + RESOURCES_DIR.length());
		return resourcesDir;
	}
	
	public static List<File> generateAllManifestFiles(File directoryToScan) {
		String autoscanDirectory = getAbsoluteResourcesPath(directoryToScan);
		logger.info("Scanning: " + autoscanDirectory);
		File[] modules = directoryToScan.listFiles(SVN_FILTER);

		if (modules == null) {
		    return new ArrayList<File>();
		}

		List<File> manifestsGenerated = new ArrayList<File>(modules.length); // returned to user for Unit Tests
		for (File file : modules) {
			// e.g. mvc
			String architectureName = file.getName();
			// e.g. AutoscanManifest.mvc.txt
			String manifestFileName = AUTOSCAN_FILE_NAME + "." + architectureName + AUTOSCAN_FILE_EXTENSION;
			File manifestFile = new File(directoryToScan, manifestFileName);
			try {
				File manifest = generateAutoscanManifestFromMojo(file, manifestFile, autoscanDirectory);
				manifestsGenerated.add(manifest);
				logger.info("creating " + manifest.getAbsolutePath());
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return manifestsGenerated;
	}

	private static File generateAutoscanManifestFromMojo(File autoscanDirectory, File manifestFile, String autoscanPath)
			throws IOException {
		List<String> paths = extractPathsFromAutoscanDirectory(autoscanDirectory, autoscanPath);
		StringBuilder sb = new StringBuilder();
		for (String path : paths) {
			sb.append(path + "\n");
		}
		// now that we have a String representation of our files, let's write them to a file.
		final String manifest = sb.toString().trim();
		FileUtils.writeStringToFile(manifestFile, manifest);
		return manifestFile;
	}

	/**
	 * 
	 * @param autoscanDirectory
	 *            file representation of autoscan directory
	 * @param autoscanPath
	 *            path relative to src/main/resources "/autoscan"
	 * @return	list of Strings to write to manifest.
	 */
	public static List<String> extractPathsFromAutoscanDirectory(final File sourceDirectory, final String autoscanPath) {
		Collection<File> files = recursivelyScanDirectory(sourceDirectory);
		List<String> paths = new ArrayList<String>(files.size());
		for (File file : files) {
			String path = file.getAbsolutePath();
			String pathRelativeToAutoscanDirectory = generateRelativePath(autoscanPath, path);
			// I don't think pathRelativeToAutoscanDirectory in Windows \foo\bar.xml is valid in a Jar.
			// Needs / This works great on Linux, need to test in Windows.
			String pathAdjustedForJar = pathRelativeToAutoscanDirectory.replaceAll("\\\\", "/");
			paths.add(pathAdjustedForJar);
		}
		return paths;
	}

	private static String generateRelativePath(final String autoscanPath, String path) {
		int startPosition = path.indexOf(autoscanPath);
		if (startPosition == 0) {
			// autoscanPath and path are both absolute.
			// return path.replace(startPosition, ""); //worried about RegEx here.
			return path.substring(autoscanPath.length());

		}
		return path.substring(startPosition - 1);
	}

	//extracted to method suppress warning.
	private static Collection<File> recursivelyScanDirectory(File file) {
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(file, EXCLUDE_SVN_FOLDERS, EXCLUDE_SVN_FOLDERS);
		return files;
	}
}