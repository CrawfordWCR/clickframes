package org.clickframes;

import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Form;
import org.clickframes.model.Link;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;

/**
 * A class which runs on the appspec and processes various elements
 *
 * @author Vineet Manohar
 */
public abstract class AppspecProcessor {
    public void process(Appspec appspec) {
        for (Page page : appspec.getPages()) {
            processPage(appspec, page);

            for (Form form : page.getForms()) {
                processPageForms(appspec, page, form);

                for (Action action : form.getActions()) {
                    processFormActions(appspec, page, form, action);

                    for (Link outcome : action.getOutcomes()) {
                        processOutcomes(appspec, page, form, action, outcome);
                    }
                }

                for (SingleUserInput singleUserInput : form.getInputs()) {
                    processFormInputs(appspec, page, form, singleUserInput);

                    for (Validation validation : singleUserInput.getValidations()) {
                        processInputValidations(appspec, page, form, singleUserInput, validation);
                    }
                }
            }
        }
    }

    public abstract void processInputValidations(Appspec appspec, Page page, Form form, SingleUserInput singleUserInput,
            Validation validation);

    public abstract void processOutcomes(Appspec appspec, Page page, Form form, Action action, Link outcome);

    public abstract void processFormInputs(Appspec appspec, Page page, Form form, SingleUserInput singleUserInput);

    public abstract void processFormActions(Appspec appspec, Page page, Form form, Action action);

    public abstract void processPageForms(Appspec appspec, Page page, Form form);

    public abstract void processPageActions(Appspec appspec, Page page, Action action);

    public abstract void processPage(Appspec appspec, Page page);
}
