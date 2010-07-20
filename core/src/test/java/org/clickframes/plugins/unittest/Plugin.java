package org.clickframes.plugins.unittest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Entity;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.plugins.PluginContext;
import org.clickframes.techspec.Context;

/**
 *
 * @author Vineet Manohar
 *
 */
public class Plugin extends ClickframesPlugin {
    private static final Log log = LogFactory.getLog(Plugin.class);

    @Override
    public void execute(PluginContext pluginContext) {
        log.info("Executing plugin: Unittest");
    }

    @Override
    public void entityContext(Context entityContext, Entity entity) {
        entityContext.put("entityJava", this);
    }

    public String getClassName() {
        return "foofoofoo";
    }
}