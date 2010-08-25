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

package org.clickframes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.clickframes.AppspecReader;
import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.PageBlockRefType;
import org.clickframes.xmlbindings.PageBlockRefsType;
import org.clickframes.xmlbindings.PageBlockType;
import org.clickframes.xmlbindings.PageBlocksType;

/**
 * Page is a key element of the AppSpec. The entire application is divided into
 * user visible pages. A page defines a view of the user into the application. A
 * page defines a state of the application. If the application were a finite
 * state machine, a page would be a state.
 * 
 * @author Vineet Manohar
 */
public class PageBlock extends AbstractElement {
    private boolean loginRequired;
    private String description;
    private List<Form> forms = new ArrayList<Form>();
    private List<LinkSet> linkSets = new ArrayList<LinkSet>();
    private List<String> linkSetIds = new ArrayList<String>();
    private List<OutputList> outputLists = new ArrayList<OutputList>();

    public List<Output> outputs = new ArrayList<Output>();

    private List<Link> links = new ArrayList<Link>();
    private List<PageParameter> parameters = new ArrayList<PageParameter>();
    private List<Content> contents = new ArrayList<Content>();

    private List<Fact> facts = new ArrayList<Fact>();

    private Map<String, String> properties = new LinkedHashMap<String, String>();

    private Appspec appspec;

    private String navigationUrl;

    private boolean bookmarkable;

    /**
     * @return The default form of this page, or null otherwise
     * 
     * @author Vineet Manohar
     */
    public Form getDefaultForm() {
        for (Form form : forms) {
            if (form.isDefaultForm()) {
                return form;
            }
        }
        return null;
    }

    PageBlock(String id, Appspec appspec) {
        super(id, appspec);
        this.appspec = appspec;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = ClickframeUtils.normalize(description);
    }

    public List<LinkSet> getLinkSets() {
        return linkSets;
    }

    public void setLinkSets(List<LinkSet> linkSets) {
        this.linkSets = linkSets;
    }

    public List<String> getLinkSetIds() {
        return linkSetIds;
    }

    public void setLinkSetIds(List<String> linkSetIds) {
        this.linkSetIds = linkSetIds;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public boolean isControllerNeeded() {
        return isFormsDefined() || isActionsDefined() || isParametersDefined() || isLoginRequired() || isFormsDefined();
    }

    public boolean isActionsDefined() {
        return (this.getForms().size() > 0);
    }

    public boolean isFormsDefined() {
        return this.getForms().size() > 0;
    }

    public boolean isParametersDefined() {
        return this.getParameters().size() > 0;
    }

    public boolean hasParameter(PageParameter parameter) {
        return parameters.contains(parameter);
    }

    public List<PageParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<PageParameter> parameters) {
        this.parameters = parameters;
    }

    public boolean isLoginRequired() {
        return loginRequired;
    }

    public void setLoginRequired(boolean loginRequired) {
        this.loginRequired = loginRequired;
    }

    /**
     * aggregates global, login and page level linkSets
     */
    public List<LinkSet> getAllApplicableLinkSets() {
        List<LinkSet> retVal = new ArrayList<LinkSet>();

        retVal.addAll(linkSets);
        retVal.addAll(appspec.getGlobalLinkSets());
        return retVal;
    }

    /**
     * Is this page a login page?
     * 
     * @return
     * 
     * @author Vineet Manohar
     */
    public boolean isLoginPage() {
        for (Form form : getForms()) {
            if (form.isLoginForm()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return the first login form found on page
     * 
     * @throws AppspecConstraintViolationException
     * 
     * @author Vineet Manohar
     */
    public Form getLoginForm() throws AppspecConstraintViolationException {
        if (!isLoginPage()) {
            throw new RuntimeException(
                    "Login form is valid only for login pages. First call isLoginPage before calling this method.");
        }
        for (Form form : getForms()) {
            if (form.isLoginForm()) {
                return form;
            }
        }

        throw new AppspecConstraintViolationException("Login Page (id = " + getId() + ") must have a login form.");
    }

    public String getNavigationUrl() {
        return navigationUrl;
    }

    public String getDefaultNavigationUrl() {
        String url = getId() + "${pageSuffix}";
        for (int i = 0; i < getParameters().size(); i++) {
            PageParameter parameter = getParameters().get(i);

            if (i == 0) {
                url = url + "?";
            } else {
                url = url + "&";
            }
            url += parameter.getId() + "=${" + parameter.getId() + "}";
        }

        return url;
    }

    public List<Content> getVerbatimContents() {
        List<Content> retVal = new ArrayList<Content>();
        for (Content content : contents) {
            if (content.isVerbatim()) {
                retVal.add(content);
            }
        }
        return retVal;
    }

    public List<Content> getNonVerbatimContents() {
        List<Content> retVal = new ArrayList<Content>();
        for (Content content : contents) {
            if (!content.isVerbatim()) {
                retVal.add(content);
            }
        }
        return retVal;
    }

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public Form getForm(String formId) throws FormNotFoundException {
        for (Form form : getForms()) {
            if (form.getId().equals(formId)) {
                return form;
            }
        }

        throw new FormNotFoundException(formId);
    }

    /**
     * @return true if there is at least one file input in any form on the page,
     *         false otherwise
     */
    public boolean isAnyFileInputsOnPage() {
        for (Form form : getForms()) {
            if (form.getFileInputs().size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if there is at least one file input in any drop down input
     *         on the page, false otherwise
     */
    public boolean isAnyDropdownInputsOnPage() {
        for (Form form : getForms()) {
            if (form.getDropdownInputs().size() > 0 || form.getMultiDropdownInputs().size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return a list of all emails referenced by any outcome from this page, or
     *         an empty list if no emails are referenced
     * 
     * @author Vineet Manohar
     */
    public Set<Email> getEmails() {
        Set<Email> emails = new HashSet<Email>();

        for (Form form : forms) {
            for (Action action : form.getActions()) {
                emails.addAll(action.getEmails());
            }
        }

        return emails;
    }

    public boolean isEmailUsed() {
        return getEmails().size() > 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getId() + ")";
    }

    public List<OutputList> getOutputLists() {
        return outputLists;
    }

    public Map<Entity, List<OutputList>> getOutputListMap() {
        Map<Entity, List<OutputList>> retVal = new HashMap<Entity, List<OutputList>>();
        for (OutputList outputList : outputLists) {
            if (!retVal.containsKey(outputList.getEntity())) {
                retVal.put(outputList.getEntity(), new ArrayList<OutputList>());
            }

            retVal.get(outputList.getEntity()).add(outputList);
        }

        return retVal;
    }

    public void setOutputLists(List<OutputList> outputLists) {
        this.outputLists = outputLists;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public static PageBlock create(Appspec appspec, PageBlockType pageBlockType)
            throws AppspecConstraintViolationException {
        PageBlock pageBlock = new PageBlock(pageBlockType.getId(), appspec);

        pageBlock.setTitle(pageBlockType.getTitle());

        pageBlock.setDescription(pageBlockType.getDescription());

        pageBlock.getOutputLists().addAll(OutputList.createList(appspec, pageBlockType.getOutputLists(), pageBlock));

        pageBlock.getOutputs().addAll(Output.createList(appspec, pageBlockType.getOutputs(), pageBlock));

        return pageBlock;
    }

    /**
     * @return a collection of all entities which are bound to any input on this
     *         page
     * 
     * @author Vineet Manohar
     */
    public List<Entity> getInputEntities() {
        List<Entity> list = new ArrayList<Entity>();
        for (Form form : getForms()) {
            for (Entity entity : form.getEntities()) {
                if (!list.contains(entity)) {
                    list.add(entity);
                }
            }
        }

        return list;
    }

    /**
     * @return a mapping of entities on this page to the form which "update"
     *         them. This method is calculated by browsing all form actions and
     *         looking at the action type="update" page. Only those entities
     *         which are updated are returned.
     * 
     * @author Vineet Manohar
     */
    public Map<Entity, Form> getEntitiesUpdated() {
        Map<Entity, Form> map = new HashMap<Entity, Form>();
        for (Form form : getForms()) {
            map.putAll(form.getEntitiesUpdated());
        }

        return map;
    }

    /**
     * @return All entities referenced by this page, including entities bound to
     *         input forms, entity lists and entity refs
     * 
     * @author Vineet Manohar
     */
    public List<Entity> getInputOrOutputEntities() {
        List<Entity> list = getInputEntities();

        for (OutputList outputList : outputLists) {
            if (!list.contains(outputList.getEntity())) {
                list.add(outputList.getEntity());
            }
        }

        for (Output output : outputs) {
            if (!list.contains(output.getEntity())) {
                list.add(output.getEntity());
            }
        }

        return list;
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

    static void resolvePages(Appspec appspec) throws ResourceNotFoundException, EntityNotFoundException,
            PageNotFoundException {
        for (Page page : appspec.getPages()) {
            AppspecReader.resolvePage(appspec, page);
        }
    }

    public Map<String, Link> getAllOutcomes() {
        Map<String, Link> outcomes = new HashMap<String, Link>();

        for (Form f : forms) {
            for (Action a : f.getActions()) {
                for (Link o : a.getOutcomes()) {
                    outcomes.put(f.getId() + "_" + a.getId(), o);
                }
            }
        }

        return outcomes;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Output> outputs) {
        this.outputs = outputs;
    }

    public void setNavigationUrl(String navigationUrl) {
        this.navigationUrl = navigationUrl;
    }

    public boolean isBookmarkable() {
        return bookmarkable;
    }

    public void setBookmarkable(boolean bookmarkable) {
        this.bookmarkable = bookmarkable;
    }

    public static List<PageBlock> createList(Appspec appspec, PageBlockRefsType pageBlockRefs) {
        List<PageBlock> list = new ArrayList<PageBlock>();
        if (pageBlockRefs != null) {
            for (PageBlockRefType pageBlockRefType : pageBlockRefs.getPageBlockRef()) {
                PageBlock pageBlock = appspec.getPageBlock(pageBlockRefType.getId());
                list.add(pageBlock);
            }
        }

        return list;
    }

    public static List<PageBlock> createList(Appspec appspec, PageBlocksType pageBlocksType) {
        List<PageBlock> list = new ArrayList<PageBlock>();

        if (pageBlocksType != null) {
            for (PageBlockType pageBlockType : pageBlocksType.getPageBlock()) {
                PageBlock pageBlock = create(appspec, pageBlockType);
                list.add(pageBlock);
            }
        }

        return list;
    }
    
	@Override
	public String getMetaName() {
		return "pageBlock";
	}
}