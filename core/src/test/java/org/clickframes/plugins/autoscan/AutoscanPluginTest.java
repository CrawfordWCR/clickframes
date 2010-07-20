package org.clickframes.plugins.autoscan;

import java.io.IOException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Vineet Manohar
 */
public class AutoscanPluginTest {

    @Test
    public void testManifestLookup() throws IOException {
        String resource = "/clickframes/AutoscanManifest.com.unittest.unittestplugin.txt";
        InputStream is = this.getClass().getResourceAsStream(resource);
        Assert.assertNotNull(is, "cannot find " + resource);

        is.close();
    }
}
