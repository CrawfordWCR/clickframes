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

package org.clickframes.engine;

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.clickframes.engine.ClickframesEngineConstants.GROUP_CLASS_TOKEN;
import static org.clickframes.engine.ClickframesEngineConstants.GROUP_TOKEN;
import static org.clickframes.util.EntityUtil.getClassNameFromFullyQualifiedClassName;
import static org.clickframes.util.EntityUtil.getPackageNameFromFullyQualifiedClassName;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.clickframes.engine.swf.SWFEntityBeanFactory;
import org.clickframes.engine.swf.SWFServiceFactory;
import org.clickframes.jsf.DynamicInputFactory;
import org.clickframes.jsf.EntityBeanFactory;
import org.clickframes.model.Action;
import org.clickframes.model.BeanMetaInfo;
import org.clickframes.model.Content;
import org.clickframes.model.Email;
import org.clickframes.model.Entity;
import org.clickframes.model.Fact;
import org.clickframes.model.Form;
import org.clickframes.model.Group;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.objectfactory.MVCControllerFactory;
import org.clickframes.objectfactory.SWFValidatorFactory;
import org.clickframes.techspec.TechspecContext;
import org.mortbay.log.LogFactory;

/**
 * Creates maps of variables to replace in templates and filenames.
 *
 * For example, provides appspec object for ${appspec.id}
 *
 * Extend me to introduce your own custom variables.
 *
 * @author Steven Boscarine
 * @author Vineet Manohar
 *
 */
public class InternalVariableResolver {
    private static final String ENTITY_PACKAGE = "entityPackage";

    private final Log logger = LogFactory.getLog(getClass());

    private TechspecContext techspecContext;

    public InternalVariableResolver() {
        // for programmatic instantiation
    }

    public InternalVariableResolver(TechspecContext context) {
        this.techspecContext = context;
    }

    public Map<String, Object> getContentVariables(Page page, Content content) {
        final Map<String, Object> variablesMap = getPageVariables(page);
        variablesMap.put("content", content);
        if (content.isVerbatim()) {
            variablesMap.put("verbatimContent", content);
        } else {
            variablesMap.put("nonVerbatimContent", content);
        }
        return variablesMap;
    }

    public Map<String, Object> getPageVariables(Page page) {
        final String pageId = page.getId();
        final String groupId = pageId;
        final String groupIdCapitalized = capitalize(groupId);
        Map<String, Object> variables = getAppspecVariables();
        variables.put("page", page);
        // deprecated
        variables.put("groupClass", groupIdCapitalized);

        variables.put("group", new Group(page.getId()));

        variables.put(GROUP_TOKEN, groupId);
        variables.put(GROUP_CLASS_TOKEN, capitalize(groupId));
        // variables.put(BLURB_TOKEN, page.getId() + "_blurb"); // placeholder

        String serviceId = null;
        String defaultEntityPackage = (String) variables.get(ENTITY_PACKAGE);
        // create Bean representing values needed to generate service layer.
        variables.put("service", SWFServiceFactory.createSWFService(page, defaultEntityPackage));
        variables.put("swf-service", SWFServiceFactory.createSWFServiceNew(page, defaultEntityPackage));

        variables.put("mvc", MVCControllerFactory.createMVCController(page)); // MVC
        // Controller
        variables.put("mvc-service", MVCControllerFactory.createMVCService(page)); // MVC
        // Service

        // hack to fix SWF plugin 10/14
        // need a placeholder at page level.
        variables.put("entityGroup", "");

        if (serviceId == null) {
            serviceId = groupId + "Service";
        } else {
            logger.debug("serviceId = " + serviceId + "\t" + pageId);
        }
        variables.put("serviceId", serviceId);
        return variables;
    }

    public Map<String, Object> getEmailVariables(Email email) {
        Map<String, Object> variables = getAppspecVariables();
        variables.put("email", email);
        return variables;
    }

    public Map<String, Object> getEntityVariables(Entity entity) {
        Map<String, Object> variables = getAppspecVariables();
        variables.put("entity", entity);
        //Java representation of entity.  Encapsulates complex branching logic that is coming in future versions.
        variables.put("entity-java", EntityBeanFactory.createEntity(entity));
        return variables;
    }

    public Map<String, Object> getLinksetVariables(LinkSet linkset) {
        Map<String, Object> out = getAppspecVariables();
        out.put("linkSet.id", capitalize(linkset.getId()));
        out.put("linkset", linkset);
        return out;
    }

    public Map<String, Object> getFormVariables(Page page, Form form) {
        Map<String, Object> variables = getPageVariables(page);
        variables.put("form", form);

        final String fullyQualifiedClassName = null;
        if (fullyQualifiedClassName == null) {
            BeanMetaInfo entity = SWFEntityBeanFactory.createSWFEntityBean(page, form);
            variables.put("entityClass", entity.getClassName());
            variables.put("entity", entity);

        } else {
            variables.put("entityClass", getClassNameFromFullyQualifiedClassName(fullyQualifiedClassName));
            // override global variable
            variables.put(ENTITY_PACKAGE, getPackageNameFromFullyQualifiedClassName(fullyQualifiedClassName));
        }
        // Spring Web Flow specific. Should this be moved?
        variables.put("entityValidator", SWFValidatorFactory.createValidator(page, form));
        // JSF Varible
        return variables;
    }

    public Map<String, Object> getPageLinkVariables(Page page, Link link) {
        Map<String, Object> tokens = getPageVariables(page);
        tokens.put("link", link);
        return tokens;
    }

    public Map<String, Object> getAppspecVariables() {
        Map<String, Object> out = new HashMap<String, Object>();
        String pkg = techspecContext.getTechspec().getPackageName();
        // assert pkg != null;
        // out.put("techspec", techspecContext.getTechspec());
        // out.put("appspec", techspecContext.getAppspec());
        out.put("swfServlet", "spring");
        out.put("controllerPackage", pkg + ".web");
        out.put("servicePackage", pkg + ".service");
        out.put("validatorPackage", pkg + ".validator");
        out.put("packagePath", techspecContext.getTechspec().getPackagePath());
        out.put(ENTITY_PACKAGE, pkg + ".webform");
        // JSF2 varible
        out.put("formService", DynamicInputFactory.createFormService(techspecContext.getAppspec()));
        return out;
    }

    public void settechspecContext(TechspecContext techspecContext) {
        this.techspecContext = techspecContext;
    }

    public TechspecContext gettechspecContext() {
        return techspecContext;
    }

    public Map<String, Object> getPageFactsVariables(Fact fact) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pageFact", fact);
        return map;
    }

    public TechspecContext getTechspecContext() {
        return techspecContext;
    }

    public void setTechspecContext(TechspecContext techspecContext) {
        this.techspecContext = techspecContext;
    }

    public Map<String, Object> getFormActionVariables(Page page, Form form, Action action) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("action", action);
        return map;
    }

    public Map<String, Object> getInputVariables(Page page, Form form, SingleUserInput input) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("input", input);
        return map;
    }

    public Map<String, Object> getPageActionVariables(Page page, Action action) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("action", action);
        return map;
    }

    public Map<String, Object> getInputValidationVariables(Page page, Form form, SingleUserInput input,
            Validation validation) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("validation", validation);
        return map;
    }
}