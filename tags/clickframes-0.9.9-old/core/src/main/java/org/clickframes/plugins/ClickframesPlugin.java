package org.clickframes.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Action;
import org.clickframes.model.Email;
import org.clickframes.model.Entity;
import org.clickframes.model.EntityProperty;
import org.clickframes.model.Fact;
import org.clickframes.model.Form;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Output;
import org.clickframes.model.OutputList;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.techspec.Context;
import org.clickframes.techspec.TechspecContext;

/**
 * Clickframes plugin provides an easy way to extend the clickframes code
 * generation functionality. The methods in this class provide hooks for
 * customization. Each method is invoked in a different phase of code generation
 * lifecycle and lets you do a take a small action without writing a lot of
 * code.
 *
 * The execute() method lets you write custom java code which gets executed by
 * the CodeGeneration engine.
 *
 * The other methods are context methods. They get called in the process of Code
 * Generation which scanning and running templates. They will not be called if
 * you don't have any templates. You can override these methods (optionally) and
 * do something specific in that context. For example, you might want to define
 * a context sensitive variable which would then be used by the templates.
 *
 * For example, the pageContext is accessible via $context.get($page) call from
 * any template. You can "put" the variable "x" in this context:
 *
 * pageContext.put("x", "foo");
 *
 * You can then access this variable in templates:
 *
 * as $x in page templates
 *
 * and $context.get($page).x in all templates
 *
 * @author Vineet Manohar
 */
@SuppressWarnings("unused")
public class ClickframesPlugin {
    protected Log logger = LogFactory.getLog(getClass());

    public void actionOutcomeContext(Context actionOutcomeContext, Action action, Link outcome) {
        // default implementation does not do anything
    }

    public void actionOutcomeFactContext(Context factContext, Action action, Link outcome, Fact fact) {
        // default implementation does not do anything
    }

    public void emailContext(Context emailContext, Email email) {
        // default implementation does not do anything
    }

    public void emailFactContext(Context emailFactContext, Email email, Fact fact) {
        // default implementation does not do anything
    }

    public void entityContext(Context entityContext, Entity entity) {
        // default implementation does not do anything
    }

    /**
     * This method is guaranteed to get executed if your plugin is included in
     * techspec
     *
     * @param pluginContext
     *
     * @author Vineet Manohar
     */
    public void execute(PluginContext pluginContext) {
        // default implementation does not do anything
    }

    public void formActionContext(Context actionContext, Page page, Form form, Action action) {
        // default implementation does not do anything
    }

    public void globalContext(Context appspecContext, TechspecContext techspecContext) {
        // default implementation does not do anything
    }

    public void inputContext(Context inputContext, Form form, SingleUserInput input) {
        // default implementation does not do anything
    }

    public void inputFactContext(Context inputFactContext, Form form, SingleUserInput input, Fact fact) {
        // default implementation does not do anything
    }

    public void inputValidationContext(Context inputValidationContext, Form form, SingleUserInput input,
            Validation validation) {
        // default implementation does not do anything
    }

    public void linkSetContext(Context linkSetContext, LinkSet linkSet) {
        // default implementation does not do anything
    }

    public void linkSetFactContext(Context linkSetFactContext, LinkSet linkSet, Fact fact) {
        // default implementation does not do anything
    }

    public void linkSetLinkContext(Context linkSetLinkContext, LinkSet linkSet, Link link) {
        // default implementation does not do anything
    }

    public void linkSetLinkFactContext(Context linkSetLinkFactContext, LinkSet linkSet, Link link, Fact fact) {
        // default implementation does not do anything
    }

    public void outcomeContext(Context outcomeContext, Link outcome) {
        // default implementation does not do anything
    }

    public void outcomeFactContext(Context factContext, Link outcome, Fact fact) {
        // default implementation does not do anything
    }

    public void pageActionContext(Context actionContext, Page page, Action action) {
        // default implementation does not do anything
    }

    public void pageContentContext(Context contentContext, Page page) {
        // default implementation does not do anything
    }

    /**
     * The pageContext is accessible via $context.get($page) call from any
     * template. You can "put" the variable "x" in this context:
     *
     * pageContext.put("x", "foo");
     *
     * You can then access this variable in templates:
     *
     * as $x in page templates
     *
     * and $context.get($page).x or $context.get($page).get("x") in all
     * templates
     *
     * @param pageContext
     * @param page
     *
     * @author Vineet Manohar
     */
    public void pageContext(Context pageContext, Page page) {
        // default implementation does not do anything
    }

    public void pageFactContext(Context pageContext, Page page, Fact fact) {
        // default implementation does not do anything
    }

    public void pageFormContext(Context formContext, Page page, Form form) {
        // default implementation does not do anything
    }

    /**
     * The pageLinkContext is accessible via $context.get($link) call from any
     * template where you have a page link. You can "put" the variable "x" in
     * this context:
     *
     * pageLinkContext.put("x", "foo");
     *
     * You can then access this variable in templates:
     *
     * as $x in page templates
     *
     * and $context.get($link).x or $context.get($link).get("x") in all
     * templates
     *
     * @param page
     * @param link
     * @param pageLinkContext
     *
     * @author Vineet Manohar
     */
    public void pageLinkContext(Context pageLinkContext, Page page, Link link) {
        // default implementation does not do anything
    }

    public void pageLinkFactContext(Context pageLinkFactContext, Page page, Link link, Fact fact) {
        // default implementation does not do anything
    }

    public void entityPropertyContext(Context entityPropertyContext, EntityProperty entityProperty) {
        // default implementation does not do anything
    }

    public void outputContext(Context outputContext, Page page, Output output) {
        // default implementation does not do anything
    }

    public void outputListContext(Context outputListContext, Page page) {
        // default implementation does not do anything
    }

    public void outputListLinkContext(Context outputListLinkContext, Page page, OutputList outputList, Link link) {
        // default implementation does not do anything
    }
}