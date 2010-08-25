package org.clickframes.plugins.webflow;

import org.clickframes.jsf.EntityBeanFactory;
import org.clickframes.model.Entity;
import org.clickframes.model.Form;
import org.clickframes.model.Page;
import org.clickframes.model.PageParameter;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.objectfactory.SWFValidatorFactory;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.techspec.Context;

/**
 * Quick fix for entity issue.
 *
 * @author sboscarine
 *
 */
public class Plugin extends ClickframesPlugin {
	@Override
	public void entityContext(Context entityContext, Entity entity) {
		// Java representation of entity. Encapsulates complex branching logic that is coming in future versions.
		entityContext.put("entity-java", EntityBeanFactory.createEntity(entity));
		entityContext.put("entityValidator", SWFValidatorFactory.createValidatorFromEntity(entity));

	}

	/**
	 * This will set the "navigationUrl"
	 */
	@Override
	public void pageContext(Context pageContext, Page page) {
        String url = "spring/${page.id}";
        for (int i = 0; i < page.getParameters().size(); i++) {
            PageParameter parameter = page.getParameters().get(i);

            if (i == 0) {
                url = url + "?";
            } else {
                url = url + "&";
            }
            url += parameter.getId() + "=${" + parameter.getId() + "}";
        }
	    pageContext.put("navigationUrl", url);
	}
    @Override
    public void inputValidationContext(Context inputValidationScope, Form form, SingleUserInput input,
            Validation validation) {
        // createAccountForm:password-message
        validation.setMessageHtmlId(form.getId() + ":" + input.getId() + "-message");

    }
}
