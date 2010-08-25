package org.clickframes.plugins.requirements;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.clickframes.model.Appspec;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.plugins.PluginContext;
import org.clickframes.techspec.Context;
import org.clickframes.techspec.TechspecContext;

/**
 * Plugin to generate requirements object
 *
 * @author Vineet Manohar
 */
public class Plugin extends ClickframesPlugin {

    @Override
    public void execute(PluginContext pluginContext) {
        Appspec appspec = pluginContext.getAppspec();

        try {
            List<Requirement> requirements = Requirement.generateRequirementsForProject(appspec);

            // part 2: generate excel spreadsheet
            File excelFile = new File(pluginContext.getTechspec().getOutputDirectory(), "target" + File.separator
                    + "clickframes" + File.separator + "requirements" + File.separator + "requirements.xls");
            excelFile.getParentFile().mkdirs();
            Requirement.renderSpreadsheet(requirements, excelFile);
            // logger.info("Requirements written at : " + excelFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PageNotFoundException e) {
            throw new RuntimeException("Invalid appspec", e);
        }
    }

    @Override
    public void globalContext(Context appspecScope, TechspecContext techspecContext) {
        List<Requirement> requirements;
        try {
            requirements = Requirement.generateRequirementsForProject(techspecContext.getAppspec());
        } catch (PageNotFoundException e) {
            throw new RuntimeException("Invalid appspec", e);
        }
        appspecScope.put("requirements", requirements);
    }
}