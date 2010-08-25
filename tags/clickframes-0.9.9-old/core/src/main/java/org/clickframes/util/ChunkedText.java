package org.clickframes.util;

import java.util.List;

import org.clickframes.util.Chunk.RegionChunk;

public class ChunkedText {
    private List<Chunk> chunks;

    public ChunkedText(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    public RegionChunk getRegion(String regionName) {
        for (Chunk chunk : chunks) {
            if (chunk instanceof RegionChunk) {
                RegionChunk region = (RegionChunk) chunk;
                if (region.getRegionName().equals(regionName)) {
                    return region;
                }
            }
        }
        return null;
    }
    
    public String toText() {
        StringBuilder sb = new StringBuilder();
        
        for (Chunk chunk : chunks) {
            sb.append(chunk.toText());
        }

        return sb.toString();
    }
}