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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.clickframes.util.CodeGenerator;
import org.clickframes.util.CodeOverwritePolicy;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CodeGeneratorTest {
	private static CodeGenerator codeGenerator = new CodeGenerator();

	@Test
	public void testModifiedSinceLastGenerationOnNonModifiedFile() throws IOException {
		File tmpFile = File.createTempFile("junit", "sample-with-checksum.xml");
		IOUtils.copy(this.getClass().getResourceAsStream("/sample-with-checksum.xml"), new FileOutputStream(tmpFile));
		Assert.assertFalse(codeGenerator.modifiedSinceLastGeneration(tmpFile),
				"File not changed, but detected as changed");
	}

	@Test
	public void testModifiedSinceLastGenerationOnModifiedFile() throws IOException {
		File tmpFile = File.createTempFile("junit", "sample-modified-with-checksum.xml");
		IOUtils.copy(this.getClass().getResourceAsStream("/sample-modified-with-checksum.xml"), new FileOutputStream(
				tmpFile));
		Assert
				.assertTrue(codeGenerator.modifiedSinceLastGeneration(tmpFile),
						"File changed, but detected as unchanged");
	}

	@Test
	public void testStoreChecksumInFile() throws IOException {
		File tmpFile = File.createTempFile("junit", "sample-no-checksum.xml");
		IOUtils.copy(this.getClass().getResourceAsStream("/sample-no-checksum.xml"), new FileOutputStream(tmpFile));
		CodeGenerator.storeChecksumInFile(tmpFile);
		Assert.assertFalse(codeGenerator.modifiedSinceLastGeneration(tmpFile),
				"File not changed, but detected as changed");
	}


	//TODO:  confirm me.
	@Test
	public void testGenerateCodeAndOverwriteUnchangedFile() throws IOException {
		File tmpFile = File.createTempFile("junit", ".tmp");
		tmpFile.deleteOnExit();

		File tmpDir = new File(tmpFile.getParentFile(), UUID.randomUUID().toString());
		try {
			String filename = "sample.xml";
			File realDir = new File(tmpDir, "real");
			File targetDir = new File(tmpDir, "target");
			File realFile = new File(realDir, filename);

			// generate sample1
			codeGenerator.generateCode(CodeOverwritePolicy.OVERWRITE_IF_UNCHANGED, new HashMap<String, Object>(),
					realDir, targetDir, "sample1.vm", filename);

			// assert that sample1 was written
			Assert.assertTrue(FileUtils.readFileToString(realFile).contains("This is sample1"),
					"Sample 1 was not written successfully, expected text not found in the written file");

			// try to overwrite sample1 with sample2, with overwrite if
			// unchanged
			codeGenerator.generateCode(CodeOverwritePolicy.OVERWRITE_IF_UNCHANGED, new HashMap<String, Object>(),
					realDir, targetDir, "sample2.vm", filename);

			// assert that sample1 was written
			Assert.assertTrue(FileUtils.readFileToString(realFile).contains("This is sample2"),
					"Sample 2 was not written successfully, expected text not found in the written file");

			// now try to overwrite sample2 with sample1, with overwrite not
			// allowed
			codeGenerator.generateCode(CodeOverwritePolicy.OVERWRITE_NOT_ALLOWED, new HashMap<String, Object>(),
					realDir, targetDir, "sample1.vm", "sample.xml");

			// assert that sample1 was not written
			Assert.assertTrue(!FileUtils.readFileToString(realFile).contains("This is sample1"),
					"Sample 1 was written successfully, expected not to find in the written file");
		} finally {
			FileUtils.deleteDirectory(tmpDir);
		}
	}

	@Test
	public void testGenerateCodeAndOverwriteFormattedButUnchangedFile() throws IOException {
		// write sample1
		// verify timestamp of creation
		// modify sample1 with whitespace
		// write sample1 again
		// verify that timestamp is not changed
		File tmpFile = File.createTempFile("junit", ".tmp");
		tmpFile.deleteOnExit();

		File tmpDir = new File(tmpFile.getParentFile(), UUID.randomUUID().toString());
		try {
			String filename = "sample.xml";
			File realDir = new File(tmpDir, "real");
			File targetDir = new File(tmpDir, "target");
			File realFile = new File(realDir, filename);

			// generate sample1
			codeGenerator.generateCode(CodeOverwritePolicy.OVERWRITE_IF_UNCHANGED, new HashMap<String, Object>(),
					realDir, targetDir, "sample1.vm", filename);

			// assert that sample1 was written
			Assert.assertTrue(FileUtils.readFileToString(realFile).contains("This is sample1"),
					"Sample 1 was not written successfully, expected text not found in the written file");

			long creationTime = realFile.lastModified();

			codeGenerator.generateCode(CodeOverwritePolicy.OVERWRITE_IF_UNCHANGED, new HashMap<String, Object>(),
					realDir, targetDir, "sample1-formatted.vm", filename);

			long lastModified = realFile.lastModified();

			// assert that sample1 was not written
			Assert.assertTrue(lastModified == creationTime,
					"File should not have been changed because only formatting changed");
		} finally {
			FileUtils.deleteDirectory(tmpDir);
		}
	}
}
