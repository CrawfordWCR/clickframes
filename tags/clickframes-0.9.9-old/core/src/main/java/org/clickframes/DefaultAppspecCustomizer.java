package org.clickframes;

import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Form;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;

public class DefaultAppspecCustomizer extends AppspecCustomizer {
    @Override
    public String getInputHtmlId(Appspec appspec, Page page, Form form, SingleUserInput singleUserInput) {
        return form.getId() + ":" + singleUserInput.getId();
    }

    @Override
    public String getFormActionHtmlId(Appspec appspec, Page page, Form form, Action action) {
        return form.getId() + ":" + action.getId();
    }

    @Override
    public String getPageActionHtmlId(Appspec appspec, Page page, Action action) {
        return page.getId() + ":" + action.getId();
    }

    @Override
    public String getValidationMessageHtmlId(Appspec appspec, Page page, Form form, SingleUserInput singleUserInput,
            Validation validation) {
        return singleUserInput.getHtmlId() + "_message";
    }
}
