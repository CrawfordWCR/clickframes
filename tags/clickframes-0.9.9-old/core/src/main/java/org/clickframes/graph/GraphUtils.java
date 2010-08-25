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

package org.clickframes.graph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.VelocityHelper;
import org.clickframes.util.ImageMapArea;

import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.out.DOTtoGIF;
import com.oy.shared.lm.out.GRAPHtoDOT;

public class GraphUtils {
    public static final String DOT = "fdp";
    // public static final String DOT = "dot";
    private static final Log log = LogFactory.getLog(GraphUtils.class);

    public static File toImageIfAvailable(Graph graph) throws IOException {
        if (isGraphingAvailable(GraphingEngine.DOT)) {
            return toImage(null, graph);
        }

        return null;
    }

    public static boolean isGraphingAvailable(GraphingEngine graphingEngine) {
        String[] cmd = new String[] {
        // "\"" + graphingEngine.getCommand() + "\"", "-V" };
                "" + graphingEngine.getCommand() + "", "-V" };

        Process p;
        try {
            Runtime runtime = Runtime.getRuntime();
            p = runtime.exec(cmd);
            p.waitFor();

            return true;
        } catch (IOException e) {
            log.error("Exception while executing " + DOT + ", not installed? " + e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            return false;
        }

    }

    public static File toImage(GraphingEngine graphingEngine, Graph graph) throws IOException {
        File dotFile = File.createTempFile("clickframes", ".dot");
        File imgFile = File.createTempFile("clickframes", ".png");
        dotFile.deleteOnExit();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GRAPHtoDOT.transform(graph, baos);
        String dot = baos.toString();
        dot = dot
                .replaceAll(
                        "label=.http.:././www..softwaresecretweapons..com.l.; URL=.http.:././www..softwaresecretweapons..com.l.;",
                        "");
        dot = dot.replaceAll("softwaresecretweapons..com", "clickframes\\.org");
        FileUtils.writeStringToFile(dotFile, dot);
        DOTtoGIF.transform(graphingEngine.getCommand(), dotFile.getAbsolutePath(), imgFile.getAbsolutePath());
        return imgFile;
    }

    public static void toHtmlAndPng(GraphingEngine graphingEngine, Graph graph, File htmlFile, File pngFile)
            throws IOException {
        File dotFile = File.createTempFile("clickframes", ".dot");
        dotFile.deleteOnExit();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GRAPHtoDOT.transform(graph, baos);
        String dot = baos.toString();
        dot = dot
                .replaceAll(
                        "label=.http.:././www..softwaresecretweapons..com.l.; URL=.http.:././www..softwaresecretweapons..com.l.;",
                        "");
        dot = dot.replaceAll("softwaresecretweapons..com", "clickframes\\.org");
        FileUtils.writeStringToFile(dotFile, dot);
        DOTtoGIF.transform(graphingEngine.getCommand(), dotFile.getAbsolutePath(), pngFile.getAbsolutePath());

        // base referer
        // poly http://www.google.com 293,84 292,77 286,70 278,65,268,61 257,60
        // 246,61 236,65 228,70 223,77 221,84 223,91 228,98,236,103 246,107
        // 257,108 268,107 278,103 286,98 292,91
        createHtmlFile(graphingEngine, dotFile, htmlFile, pngFile.getName());
    }

    private static File createHtmlFile(GraphingEngine graphingEngine, File dotFile, File retVal, String imageFilename)
            throws IOException {
        // run this command 'dot test.dot -Timap'
        // in the output look for lines like: shape url coords
        Process p;
        String error = null;
        try {
            String[] cmd = new String[] { graphingEngine.getCommand(), dotFile.getAbsolutePath(), "-Timap" };

            boolean reportErrors = false;
            p = Runtime.getRuntime().exec(cmd);

            List<String> lines = null;
            InputStream in = p.getInputStream();

            boolean finished = false; // Set to true when p is finished
            while (!finished) {
                try {
                    while (in.available() > 0) {
                        // Print the output of our system call.
                        // Character c = new Character((char) in.read());
                        // System.out.print(c);
                        lines = IOUtils.readLines(in);
                    }
                    // Ask the process for its exitValue. If the process
                    // is not finished, an IllegalThreadStateException
                    // is thrown. If it is finished, we fall through and
                    // the variable finished is set to true.
                    p.exitValue();
                    finished = true;
                } catch (IllegalThreadStateException e) {
                    Thread.currentThread();
                    // Sleep a little to save on CPU cycles
                    Thread.sleep(500);
                }
            }

            Pattern pattern = Pattern.compile("^(\\S+) (\\S+) (.+)$");
            List<ImageMapArea> areas = new ArrayList<ImageMapArea>();
            for (String line : lines) {
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    String shape = m.group(1);
                    String href = m.group(2);
                    String coords = m.group(3).trim();
                    ImageMapArea area = new ImageMapArea();
                    area.setShape(shape);
                    area.setHref(href.replaceAll("\\\\l", "").replaceAll("\\\\", ""));
                    area.setCoords(coords);
                    areas.add(area);
                }
            }

            Map<String, Object> params = new HashMap<String, Object>();

            params.put("imageName", imageFilename);
            params.put("areaList", areas);

            return VelocityHelper.runMacro(params, "imageMap.vm", retVal);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        isGraphingAvailable(GraphingEngine.DOT);
    }

    public static boolean isGraphingAvailable() {
        return isGraphingAvailable(GraphingEngine.DOT);
    }
}