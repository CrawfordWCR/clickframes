package org.clickframes.techspec.manifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.techspec.TechspecContext;
import org.clickframes.techspec.TechspecRunner;
import org.clickframes.techspec.manifest.xmlbindings.EntryType;
import org.clickframes.techspec.manifest.xmlbindings.Manifest;
import org.clickframes.techspec.manifest.xmlbindings.ObjectFactory;
import org.clickframes.util.ClickframeUtils;

/**
 * Represents a techspec manifest
 *
 * @author Vineet Manohar
 */
public class TechspecManifest {
    private static final Log log = LogFactory.getLog(TechspecManifest.class);

    public Map<String, TechspecManifestEntry> entries = new LinkedHashMap<String, TechspecManifestEntry>();

    public static TechspecManifest readTechspecManifest(InputStream is) throws JAXBException {
        Manifest manifest = TechspecManifestJaxbWrapper.inputStreamToJava(is);

        return create(manifest);
    }

    public static TechspecManifest create() {
        TechspecManifest techspecManifest = new TechspecManifest();

        return techspecManifest;
    }

    private Manifest toManifest() {
        Manifest manifest = new ObjectFactory().createManifest();

        for (TechspecManifestEntry entry : entries.values()) {
            manifest.getEntries().add(entry.toEntryType());
        }

        return manifest;
    }

    public String toXml() throws JAXBException {
        return TechspecManifestJaxbWrapper.toXml(toManifest());
    }

    private static TechspecManifest create(Manifest manifest) {
        TechspecManifest techspecManifest = new TechspecManifest();

        for (EntryType entryType : manifest.getEntries()) {
            TechspecManifestEntry entry = TechspecManifestEntry.create(entryType);
            if (entry.exists()) {
                // only load entry if file still exists
                techspecManifest.getEntries().put(entry.getPath(), entry);
            }
        }
        return techspecManifest;
    }

    public Map<String, TechspecManifestEntry> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, TechspecManifestEntry> entries) {
        this.entries = entries;
    }

    /**
     * A utility for printing a list of entries
     *
     * @param entries
     *
     * @author Vineet Manohar
     */
    public static void print(List<TechspecManifestEntry> entries) {
        for (TechspecManifestEntry entry : entries) {
            log.info(entry);
        }
    }

    /**
     * find which entries which be orphanned if this techspec was run
     *
     * @param techspecContext
     * @return
     *
     * @author Vineet Manohar
     * @param allAutoscanTemplates
     */
    public List<TechspecManifestEntry> getOrphans(TechspecContext techspecContext, List<String> allAutoscanTemplates) {
        File originalOutputDirectory = techspecContext.getTechspec().getOutputDirectory();
        File tmpDirectory = new File(originalOutputDirectory, "target" + File.separator + "clickframes-tmp");
        try {
            // run the techspec in a temp directory
            if (tmpDirectory.exists()) {
                FileUtils.cleanDirectory(tmpDirectory);
            }
            techspecContext.getTechspec().setOutputDirectory(tmpDirectory);
            TechspecRunner.run(techspecContext.getTechspec(), techspecContext.getAppspec(), allAutoscanTemplates);

            List<TechspecManifestEntry> orphans = new ArrayList<TechspecManifestEntry>();
            // check all existing entries
            for (TechspecManifestEntry entry : getEntries().values()) {
                // was this entry regenerated
                File file = entry.getFile(tmpDirectory);
                // if not, this entry is orphan
                if (!file.exists()) {
                    orphans.add(entry);
                }
            }

            return orphans;
        } catch (IOException e) {
            throw new RuntimeException("Could not clean the tmp directory:" + tmpDirectory.getAbsolutePath());
        } finally {
            techspecContext.getTechspec().setOutputDirectory(originalOutputDirectory);
        }
    }

    /**
     * find which entries which entries are modified
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public List<TechspecManifestEntry> getModified() {
        List<TechspecManifestEntry> retVal = new ArrayList<TechspecManifestEntry>();

        // check all existing entries
        for (TechspecManifestEntry entry : getEntries().values()) {
            if (entry.isModified()) {
                retVal.add(entry);
            }
        }

        return retVal;
    }

    /**
     * find which entries are not modified
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public List<TechspecManifestEntry> getNotModified() {
        List<TechspecManifestEntry> retVal = new ArrayList<TechspecManifestEntry>();

        for (TechspecManifestEntry entry : getEntries().values()) {
            if (!entry.isModified()) {
                retVal.add(entry);
            }
        }

        return retVal;
    }

    /**
     * find which entries are deleted from file system
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public List<TechspecManifestEntry> getDeletedFromFileSystem() {
        List<TechspecManifestEntry> retVal = new ArrayList<TechspecManifestEntry>();

        for (TechspecManifestEntry entry : getEntries().values()) {
            if (!entry.exists()) {
                retVal.add(entry);
            }
        }

        return retVal;
    }

    /**
     * find all entries
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public List<TechspecManifestEntry> getAll() {
        List<TechspecManifestEntry> retVal = new ArrayList<TechspecManifestEntry>();

        for (TechspecManifestEntry entry : getEntries().values()) {
            retVal.add(entry);
        }

        return retVal;
    }

    /**
     * add or update path for path relative to current dir
     *
     * @param newFile
     *
     * @author Vineet Manohar
     */
    public boolean addOrUpdateEntry(File basePath, File newFile) {
        String path1;
        String path2;
        try {
            path1 = basePath.getCanonicalPath();
            path2 = newFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Could not calculate canonical path for file : " + newFile.getAbsolutePath(), e);
        }

        // first convert both to forward '/'
        String slash = ClickframeUtils.getFileSeparatorLiteral();

        path1 = path1.replaceAll(slash, "/");
        path2 = path2.replaceAll(slash, "/");

        String relativePath = path2.replaceAll("^" + path1 + "/", "");
        TechspecManifestEntry entry = entries.get(relativePath);
        boolean added = false;
        if (entry == null) {
            entry = TechspecManifestEntry.create(relativePath);
            added = true;
        }
        entries.put(entry.getPath(), entry);
        return added;
    }
}