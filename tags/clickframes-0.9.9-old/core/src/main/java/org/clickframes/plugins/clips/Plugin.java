package org.clickframes.plugins.clips;

import java.util.ArrayList;
import java.util.List;

import org.clickframes.model.Appspec;
import org.clickframes.model.Page;
import org.clickframes.model.PageParameter;
import org.clickframes.plugins.ClickframesPlugin;

public class Plugin extends ClickframesPlugin {
    public void globalContext(org.clickframes.techspec.Context appspecContext,
            org.clickframes.techspec.TechspecContext techspecContext) {
        Appspec appspec = techspecContext.getAppspec();

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
        appspecContext.put("pagesParams", appspecPageParamMapping);
    }
}