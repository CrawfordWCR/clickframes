package org.clickframes.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AutoscanManifestUtil {
	private static final String MANIFEST_FILENAME = "AutoscanManifest.txt";
//	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(AutoscanManifestUtil.class);


	public static List<String> getFilenamesFromManifest(InputStream in) {
		try {
			@SuppressWarnings("unchecked")
			List<String> lines = IOUtils.readLines(in);
			return lines;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}



	public static File generateAutoscanManifestFromJavaMain(final String autoscanPath) throws IOException {
		// maven resources directory...(base directory of) where we look for autoscan templates.
		String resourcesDirectory = "src" + File.separator + "main" + File.separator + "resources";
		File autoscanDirectory = new File(resourcesDirectory + File.separator + autoscanPath);
		List<String> paths = extractPathsFromAutoscanDirectory(autoscanDirectory, autoscanPath);
		StringBuilder sb = new StringBuilder();
		for (String path: paths) {
			sb.append(path + "\n");
		}
		// now that we have a String representation of our files, let's write them to a file.
		final File file = new File(resourcesDirectory, MANIFEST_FILENAME);
		FileUtils.writeStringToFile(file, sb.toString());
		return file;
	}


	public static File generateAutoscanManifestFromMojo(File autoscanDirectory, File manifestFile, String autoscanPath) throws IOException {
		// maven resources directory...(base directory of) where we look for autoscan templates.
		List<String> paths = extractPathsFromAutoscanDirectory(autoscanDirectory, autoscanPath);
		StringBuilder sb = new StringBuilder();
		for (String path: paths) {
			sb.append(path + "\n");
		}
		// now that we have a String representation of our files, let's write them to a file.
		final String manifest = sb.toString();
		logger.debug("Manifest:\n" + manifest);
		FileUtils.writeStringToFile(manifestFile, manifest);
		return manifestFile;
	}

	/**
	 *
	 * @param autoscanDirectory file representation of autoscan directory
	 * @param autoscanPath path relative to src/main/resources "/autoscan"
	 * @return
	 */
	public static List<String> extractPathsFromAutoscanDirectory(final File autoscanDirectory, final String autoscanPath) {
		Collection<File> files = AutoscanDirectoryUtil.recursivelyScanDirectory(autoscanDirectory);
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
		if(startPosition==0){
			//autoscanPath and path are both absolute.
			//return path.replace(startPosition, "");	//worried about RegEx here.
			return path.substring(autoscanPath.length());

		}
		return path.substring(startPosition - 1);
	}

}