package org.clickframes;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.clickframes.engine.manifest.ManifestFileGenerator;

/**
 * @goal manifest
 * 
 * @phase process-sources
 */
public class ManifestCreationMojo extends AbstractMojo {
	/**	@parameter expression="${project.basedir}/src/main/resources/clickframes"	 */
	protected File autoscanDirectory;

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(ManifestCreationMojo.class);

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ManifestFileGenerator.generateAllManifestFiles(autoscanDirectory);
		if (!autoscanDirectory.exists()) {
			throw new IllegalArgumentException("Cannot find " + autoscanDirectory.getAbsolutePath());
		}
	}
}