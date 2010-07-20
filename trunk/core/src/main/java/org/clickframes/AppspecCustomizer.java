package org.clickframes;

import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Form;
import org.clickframes.model.Link;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;

/**
 * A class which runs on the appspec for customization purposes. This feature
 * was originally developed to support re-use of test plugin for various input
 * id schemes. For example, seam and spring have different schemes for input ids
 *
 * @author Vineet Manohar
 */
public abstract class AppspecCustomizer extends AppspecProcessor {
    public abstract String getFormActionHtmlId(Appspec appspec, Page page, Form form, Action action);

    public abstract String getPageActionHtmlId(Appspec appspec, Page page, Action action);

    public abstract String getInputHtmlId(Appspec appspec, Page page, Form form, SingleUserInput singleUserInput);

    @Override
    public void processFormActions(Appspec appspec, Page page, Form form, Action action) {
        action.setHtmlId(getFormActionHtmlId(appspec, page, form, action));
    }

    @Override
    public void processFormInputs(Appspec appspec, Page page, Form form, SingleUserInput singleUserInput) {
        singleUserInput.setHtmlId(getInputHtmlId(appspec, page, form, singleUserInput));
    }

    @Override
    public void processOutcomes(Appspec appspec, Page page, Form form, Action action, Link outcome) {
        // no customization needed at this time
    }

    @Override
    public void processPage(Appspec appspec, Page page) {
        // no page level customization needed at this time
    }

    @Override
    public void processPageActions(Appspec appspec, Page page, Action action) {
        action.setHtmlId(getPageActionHtmlId(appspec, page, action));
    }

    @Override
    public void processPageForms(Appspec appspec, Page page, Form form) {
        // no customization needed at this time
    }

    @Override
    public void processInputValidations(Appspec appspec, Page page, Form form, SingleUserInput singleUserInput,
            Validation validation) {
        validation.setMessageHtmlId(getValidationMessageHtmlId(appspec, page, form, singleUserInput, validation));
    }

    public abstract String getValidationMessageHtmlId(Appspec appspec, Page page, Form form, SingleUserInput singleUserInput,
            Validation validation);
}
