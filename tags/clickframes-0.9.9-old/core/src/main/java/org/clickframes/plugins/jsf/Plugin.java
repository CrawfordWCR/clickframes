package org.clickframes.plugins.jsf;

import org.clickframes.jsf.EntityBeanFactory;
import org.clickframes.model.Entity;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.techspec.Context;

public class Plugin extends ClickframesPlugin{
	//need to add special variables.
	@Override
	public void entityContext(Context entityContext, Entity entity) {
        //Java representation of entity.  Encapsulates complex branching logic that is coming in future versions.
		entityContext.put("entityJava", EntityBeanFactory.createEntity(entity));
		//is this being used?
		entityContext.put("entity-java", EntityBeanFactory.createEntity(entity));
	}
}
