package org.clickframes.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Chunk {
    public abstract String toText();

    public static class RegionChunk extends Chunk {
        private String header;
        private String footer;
        private String text;
        public String regionName;

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getFooter() {
            return footer;
        }

        public void setFooter(String footer) {
            this.footer = footer;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String toText() {
            return header + text + footer;
        }

        @Override
        public String toString() {
            return toText();
        }
    }

    public static class TextChunk extends Chunk {
        private String text;

        public TextChunk(String text) {
            setText(text);
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return toText();
        }

        public String toText() {
            return text;
        }
    }

    /**
     * convert text into a mixed list of text chunks and regions. Each region
     * has a name. e.g. consider the following
     * 
     * line1
     * 
     * line2
     * 
     * <!-- clickframes::start=reg1::clickframes -->
     * 
     * version1
     * 
     * <!-- clickframes::end=reg1::clickframes -->
     * 
     * line 3
     * 
     * The above will be converted to chunk, region, chunk
     * 
     * @param filename
     * @param src
     * @return
     * 
     * @author Vineet Manohar
     */
    public static ChunkedText parse(String filename, String src) {
        List<Chunk> chunks = new ArrayList<Chunk>();
        // look for regions
        // Pattern pattern = Pattern.compile("<!-- cf:(start|end)(.*)-->");
        Pattern pattern = Pattern.compile(VelocityCodeGenerator.getCommentForFilename(filename, "cf:(start|end)(.*)"));
        Matcher m = pattern.matcher(src);

        String lastFullMatch = null;
        String currentRegionName = null;

        while (m.find()) {
            String boundaryType = m.group(1);
            StringBuffer previousChunk = new StringBuffer();
            m.appendReplacement(previousChunk, "");
            if (boundaryType.equals("start")) {
                chunks.add(Chunk.createTextChunk(previousChunk.toString()));
                currentRegionName = m.group(2).trim();
            } else if (boundaryType.equals("end")) {
                String footer = m.group();
                String header = lastFullMatch;

                chunks.add(Chunk.createRegionChunk(currentRegionName, header, previousChunk.toString(), footer));
            }

            lastFullMatch = m.group();
        }

        StringBuffer sb = new StringBuffer();
        m.appendTail(sb);
        chunks.add(Chunk.createTextChunk(sb.toString()));

        return new ChunkedText(chunks);
    }

    private static Chunk createRegionChunk(String regionName, String header, String text, String footer) {
        RegionChunk chunk = new RegionChunk();
        chunk.setRegionName(regionName);
        chunk.setHeader(header);
        chunk.setText(text);
        chunk.setFooter(footer);
        return chunk;
    }

    private static Chunk createTextChunk(String text) {
        return new TextChunk(text);
    }

    public static String getCommentedRegionHeaderOrFooterRegex(String filename) {
        String comment = VelocityCodeGenerator.getCommentForFilename(filename,
                getPlaceholderWithValue("(start|end)\\s*(.*)"));
        return comment;
    }

    private static String getPlaceholderWithValue(String value) {
        return "cf:" + value;
    }
}
