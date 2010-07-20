package org.clickframes.swf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Appspec;
import org.clickframes.model.Page;

public class MultiPagePropertiesFileGenerator extends MonolithicPropertiesFileGenerator {
	private Log logger = LogFactory.getLog(getClass());

	public MultiPagePropertiesFileGenerator(boolean importExisting, Appspec appspec, File folder) {
		this.importExisting = importExisting;
		this.appspec = appspec;
		this.folder = folder;
	}

	private final boolean importExisting;
	private final Appspec appspec;
	private final File folder;

	public void writeToPropertiesFile() {
		try {
			if(!folder.exists()){
				folder.mkdirs();
			}
			// properties for each page.
			for (Page page : appspec.getPages()) {
				File file = new File(folder, page.getId() + ".properties");
				logger.debug("writing properties to " + file);
				SortedProperties properties = new SortedProperties();
				if (importExisting) {
					properties.load(new FileInputStream(file));
				}
				handlePage(properties, page);
				properties.store(new FileOutputStream(file), page.getTitle());
			}
			//handle global properties.
			File global = new File(folder, "global.properties");
			SortedProperties properties = new SortedProperties();
			if (importExisting) {
				properties.load(new FileInputStream(global));
			}
			properties.store(new FileOutputStream(global), appspec.getTitle());
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
}
