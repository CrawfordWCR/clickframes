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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.clickframes.model.Appspec;
import org.clickframes.model.Email;
import org.clickframes.model.Entity;
import org.clickframes.model.Page;
import org.clickframes.model.PageParameter;
import org.clickframes.util.CodeGenerator;

/**
 * This class converts a project object to a directory, writing the different
 * pages as xhtml files
 *
 * @author Vineet Manohar
 */
public class HtmlGenerator {
    private static String htmlExtension = ".html";
    private static CodeGenerator codeGenerator = new CodeGenerator();

    private static void generate(Appspec appspec, Map<String, Object> params) throws IOException {
        codeGenerator.generateArtifact("style.css", ".", "style.css", "appspec", appspec);

        if (params == null) {
            params = new HashMap<String, Object>();
        }

        params.put("appspec", appspec);
        codeGenerator.generateCodeOrArtifact(true, params, "project.vm", "", "index" + appspec.getHtmlExtension());

        List<List<Boolean>> appspecPageParamMapping = new ArrayList<List<Boolean>>();

        List<PageParameter> allParameters = appspec.getAllPageParameters();
        for (int i = 0; i < appspec.getPages().size(); i++) {
            Page page = appspec.getPages().get(i);
            List<Boolean> pageParamMapping = new ArrayList<Boolean>();
            for (int j = 0; j < allParameters.size(); j++) {
                PageParameter pageParameter = allParameters.get(j);
                pageParamMapping.add(page.hasParameter(pageParameter));
            }
            appspecPageParamMapping.add(pageParamMapping);
        }
        params.put("pagesParams", appspecPageParamMapping);

        params.put("appspec", appspec);
        codeGenerator.generateCodeOrArtifact(true, params, "params.vm", "", "params" + appspec.getHtmlExtension());

        for (Page page : appspec.getPages()) {
            params.put("appspec", appspec);
            params.put("page", page);

            codeGenerator
                    .generateCodeOrArtifact(true, params, "page.vm", "", page.getId() + appspec.getHtmlExtension());
        }

        for (Email email : appspec.getEmails()) {
            params.put("appspec", appspec);
            params.put("email", email);
            codeGenerator.generateArtifact("email.vm", "", email.getId() + appspec.getHtmlExtension(), "appspec",
                    appspec, "email", email);
        }

        for (Entity entity : appspec.getEntities()) {
            String templatePath = "entity.vm";
            String generatedFilePath = entity.getId() + ".html";
            codeGenerator.generateArtifact(templatePath, "entities", generatedFilePath, "appspec", appspec, "entity",
                    entity);

            // params.put("appspec", appspec);
            // params.put("entity", entity);
            // codeGenerator.generateArtifact("entity.vm", "", email.getId() +
            // appspec.getHtmlExtension(), "appspec", appspec, "email", email);
        }
    }

    private static void generate(Appspec appspec) throws IOException {
        generate(appspec, null);
    }

    private static File getResourceDir(File clickframesDir) {
        File resourcesDir = new File(clickframesDir, "resources");
        return resourcesDir;
    }

    private static void generatePage(Appspec appspec, Page page, File outputDir) {
        String filename = generateFilename(page);
        File outputFile = new File(outputDir, filename);

        Template t;
        try {
            t = Velocity.getTemplate("page.vm");
            VelocityContext ctx = new VelocityContext();
            ctx.put("appspec", appspec);
            ctx.put("page", page);

            PrintWriter pw;
            try {
                pw = new PrintWriter(new FileOutputStream(outputFile));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Cannot write file to output directory: " + outputDir, e);
            }

            t.merge(ctx, pw);
            pw.close();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while writing the page", e);
        }
    }

    private static String generateFilename(Appspec appspec) {
        return "index" + htmlExtension;
    }

    private static String generateFilename(Page page) {
        return page.getId() + htmlExtension;
    }

    private static void createOutputDirectoryIfNeeded(File outputDir) {
        outputDir.mkdirs();
    }

    private static String getPageExtension() {
        return htmlExtension;
    }

    private static void setPageExtension(String pageExtension) {
        HtmlGenerator.htmlExtension = pageExtension;
    }
}