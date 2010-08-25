package org.clickframes.util;

import java.util.List;

import org.clickframes.util.Chunk.RegionChunk;
import org.clickframes.util.Chunk.TextChunk;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Vineet Manohar
 */
public class ChunkTest {
    @Test
    public void testParse() {
        String input = "line1\n" + "line2\n" + "<!-- cf:start reg1 -->\n" + "version1\n"
                + "<!-- cf:end -->\n" + "line 3";
        ChunkedText chunkedText = Chunk.parse("input.xml", input);
        List<Chunk> chunks = chunkedText.getChunks();
        Assert.assertNotNull(chunks);
        Assert.assertEquals(chunks.size(), 3);

        {
            Chunk chunk1 = chunks.get(0);
            Assert.assertTrue(chunk1 instanceof TextChunk);
        }

        {
            Chunk chunk2 = chunks.get(1);
            Assert.assertTrue(chunk2 instanceof RegionChunk);
            RegionChunk region = (RegionChunk) chunk2;

            Assert.assertEquals(region.getRegionName(), "reg1");
        }

        {
            Chunk chunk3 = chunks.get(2);
            Assert.assertTrue(chunk3 instanceof TextChunk);
        }
        
        // by region
        {
            RegionChunk region = chunkedText.getRegion("reg1");
            Assert.assertEquals(region.getRegionName(), "reg1");
            Assert.assertEquals(region.getHeader(), "<!-- cf:start reg1 -->");
            Assert.assertEquals(region.getFooter(), "<!-- cf:end -->");
        }
    }
}