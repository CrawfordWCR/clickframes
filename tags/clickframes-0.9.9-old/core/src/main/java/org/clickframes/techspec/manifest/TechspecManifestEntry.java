package org.clickframes.techspec.manifest;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.clickframes.techspec.manifest.xmlbindings.EntryType;
import org.clickframes.techspec.manifest.xmlbindings.ObjectFactory;
import org.clickframes.util.ClickframeUtils;
import org.clickframes.util.VelocityCodeGenerator;

/**
 * Represents a techspec manifest
 *
 * @author Vineet Manohar
 */
public class TechspecManifestEntry {
    private String path;
    private File file;

    public static TechspecManifestEntry create(EntryType entryType) {
        TechspecManifestEntry entry = new TechspecManifestEntry();
        entry.setPath(entryType.getPath());
        return entry;
    }

    public static TechspecManifestEntry create(String path) {
        TechspecManifestEntry entry = new TechspecManifestEntry();
        entry.setPath(path);
        return entry;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        this.file = new File(".", ClickframeUtils.convertSlashToPathSeparator(path));
    }

    public File getFile() {
        return this.file;
    }

    public File getFile(File baseDir) {
        return new File(baseDir, ClickframeUtils.convertSlashToPathSeparator(path));
    }

    @Override
    public String toString() {
        return path;
    }

    /**
     * whether this entry exists or not
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public boolean exists() {
        return this.file.exists();
    }

    /**
     * is this file modified or not
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public boolean isModified() {
        try {
            return VelocityCodeGenerator.isModified(this.getFile().getName(), FileUtils.readFileToString(this.file));
        } catch (IOException e) {
            throw new RuntimeException("Could not check modification status for entry " + this, e);
        }
    }

    public EntryType toEntryType() {
        EntryType entryType = new ObjectFactory().createEntryType();
        entryType.setPath(path);
        return entryType;
    }
}
