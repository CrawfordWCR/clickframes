package org.clickframes.engine.manifest;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class AutoscanFolderFilter implements FileFilter {
	private static final IOFileFilter EXCLUDE_SVN_FOLDERS = FileFilterUtils.makeSVNAware(null);

	@Override
	public boolean accept(File arg0) {
		//File must be folder.
		if(!arg0.isDirectory()){
			return false;
		}
		//Folder can't be .svn folder
		if (!EXCLUDE_SVN_FOLDERS.accept(arg0))
			return false;
		File autoscanFolder = new File(arg0, ManifestFileGenerator.AUTOSCAN_FOLDER_NAME);
		if(!autoscanFolder.exists() || !autoscanFolder.isDirectory()){
			return false;
		}
		return true;
	}

}
