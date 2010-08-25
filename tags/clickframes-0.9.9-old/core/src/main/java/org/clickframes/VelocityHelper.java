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

package org.clickframes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class VelocityHelper {
    /**
     * to override a macro create a file with macroname followed by this suffix
     * and that new macro will be looked up first. e.g. to override page.vm
     * create a new template called page.vm.override
     */
    public static String OVERRIDE_SUFFIX = ".override";

    private static final Log log = LogFactory.getLog(VelocityHelper.class);

    private static boolean initialized = false;

    public static void initVelocity(File clickframesDirectory) throws Exception {
        initVelocity(clickframesDirectory, false);
    }

    public static void initVelocity(File clickframesDirectory, boolean force) throws Exception {
        if (initialized && !force) {
            return;
        }

        Properties p = new Properties();
        p.setProperty("resource.loader", "file, class");
        if (clickframesDirectory == null) {
            clickframesDirectory = new File("src" + File.separator + "main" + File.separator + "clickframes");
        }
        if (clickframesDirectory != null && clickframesDirectory.exists() && clickframesDirectory.isDirectory()) {
            // log.info("Added clickframes directory to velocity path: " + clickframesDirectory.getAbsolutePath());
            p.setProperty("file.resource.loader.path", clickframesDirectory.getAbsolutePath());
        }
        p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        p.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        Velocity.init(p);

        initialized = true;
        // log.info("Initialized velocity with properties: " + p);
    }

    public static File runMacro(Map<String, Object> params, String macro, File retVal) {
        try {
            initVelocity(null);
        } catch (Exception e1) {
            throw new RuntimeException("Could not initialize velocity", e1);
        }

        Template t;
        try {
            if (Velocity.resourceExists(macro + OVERRIDE_SUFFIX)) {
                t = Velocity.getTemplate(macro + OVERRIDE_SUFFIX);
            } else {
                t = Velocity.getTemplate(macro);
            }

            VelocityContext ctx = new VelocityContext();
            for (String key : params.keySet()) {
                ctx.put(key, params.get(key));
            }

            PrintWriter pw;
            try {
                File parent = retVal.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                pw = new PrintWriter(new FileOutputStream(retVal));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Cannot write file to output directory: " + retVal, e);
            }

            t.merge(ctx, pw);
            pw.close();

            return retVal;
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while writing the page, having problems with "
                    + retVal.getAbsolutePath(), e);
        }
    }

    public static String resolveText(Map<String, Object> params, String text) {
        try {
            initVelocity(null);
        } catch (Exception e1) {
            throw new RuntimeException("Could not initialize velocity", e1);
        }

        VelocityContext ctx = new VelocityContext();
        if (params != null) {
            for (String key : params.keySet()) {
                ctx.put(key, params.get(key));
            }
        }

        Writer w = new StringWriter();
        try {
            Velocity.evaluate(ctx, w, null, text);
        } catch (ParseErrorException e1) {
            throw new RuntimeException("Error while executing velocity for text: '" + text + "'", e1);
        } catch (MethodInvocationException e1) {
            throw new RuntimeException("Error while executing velocity", e1);
        } catch (ResourceNotFoundException e1) {
            throw new RuntimeException("Error while executing velocity", e1);
        } catch (IOException e1) {
            throw new RuntimeException("Error while executing velocity", e1);
        }

        try {
            w.close();
        } catch (IOException e) {
            throw new RuntimeException("Very unpredicable error", e);
        }

        return w.toString();
    }

    public static String runMacro(Map<String, Object> params, String macro) {
        try {
            initVelocity(null);
        } catch (Exception e1) {
            throw new RuntimeException("Could not initialize velocity", e1);
        }

        Template t;
        try {
            boolean allowOverrides = false;
            if (allowOverrides && Velocity.resourceExists(macro + OVERRIDE_SUFFIX)) {
                t = Velocity.getTemplate(macro + OVERRIDE_SUFFIX);
            } else {
                t = Velocity.getTemplate(macro);
            }

            VelocityContext ctx = new VelocityContext();
            if (params != null) {
                for (String key : params.keySet()) {
                    ctx.put(key, params.get(key));
                }
            }

            Writer writer = new StringWriter();

            t.merge(ctx, writer);
            writer.close();

            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while writing the page", e);
        }
    }

    public static Boolean evaluateCondition(Map<String, Object> params, String condition) {
        String statement = "#{if} (" + condition + ")true#{else}false#{end}";
        String result = resolveText(params, statement);
        if (result == null) {
            return null;
        }
        result = result.trim();
        return result.equals("true");
    }
}