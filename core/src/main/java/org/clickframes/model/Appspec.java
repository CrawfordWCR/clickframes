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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.clickframes.AppspecReader;
import org.clickframes.ISGBeanUtilities;
import org.clickframes.InternalLinkResolutionException;
import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.AppspecType;

/**
 * The Java model representing an appspec.
 * 
 * @author Vineet Manohar
 */
public class Appspec extends AbstractElement {
    public Appspec(String id, String title) {
        super(id, title, null);
    }

    private List<LinkSet> linkSets = new ArrayList<LinkSet>();
    private List<Link> outcomes = new ArrayList<Link>();

    private List<Page> pages = new ArrayList<Page>();
    private List<Permission> permissions = new ArrayList<Permission>();

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    private List<PageBlock> pageBlocks = new ArrayList<PageBlock>();

    public List<PageBlock> getPageBlocks() {
        return pageBlocks;
    }

    public void setPageBlocks(List<PageBlock> pageBlocks) {
        this.pageBlocks = pageBlocks;
    }

    private Map<String, Page> pageIndex;
    private Map<String, Entity> entityIndex;

    private List<Email> emails = new ArrayList<Email>();
    private Map<String, Email> emailIndex;

    private Map<String, LinkSet> linkSetIndex;

    private Map<String, Content> contentIndex = new HashMap<String, Content>();

    private String description;
    private String htmlExtension = ".html";

    private Page defaultPage;

    private Page loginPage;

    private List<Entity> entities = new ArrayList<Entity>();

    @Override
    public boolean equals(Object obj) {
        return ISGBeanUtilities.compareTwoBeans(this, obj);
    }

    /**
     * @return unique page parameters from all pages returned in the natural
     *         order
     * 
     * @author Vineet Manohar
     */
    public List<PageParameter> getAllPageParameters() {
        Map<String, PageParameter> paramIds = new TreeMap<String, PageParameter>();

        for (Page page : getPages()) {
            for (PageParameter param : page.getParameters()) {
                paramIds.put(param.getId(), param);
            }
        }

        List<PageParameter> list = new ArrayList<PageParameter>();
        list.addAll(paramIds.values());
        Collections.sort(list);
        return list;
    }

    public Page getDefaultPage() {
        return defaultPage;
    }

    public Page getLoginPage() {
        return loginPage;
    }

    public String getDescription() {
        return description;
    }

    public Email getEmail(String emailId) throws ResourceNotFoundException {
        indexEmailsIfNeeded();
        if (emailIndex.containsKey(emailId)) {
            return emailIndex.get(emailId);
        }
        throw new ResourceNotFoundException(emailId);
    }

    public List<Email> getEmails() {
        return emails;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Entity getEntity(String entityId) throws EntityNotFoundException {
        indexEntitiesIfNeeded();
        if (entityIndex.containsKey(entityId)) {
            return entityIndex.get(entityId);
        }
        throw new EntityNotFoundException(entityId);
    }

    public List<LinkSet> getGlobalLinkSets() {
        List<LinkSet> retVal = new ArrayList<LinkSet>();

        for (LinkSet linkSet : linkSets) {
            if (linkSet.isGlobal()) {
                retVal.add(linkSet);
            }
        }
        return retVal;
    }

    public String getHtmlExtension() {
        return htmlExtension;
    }

    public LinkSet getLinkSet(String linkSetId) {
        indexLinkSetsIfNeeded();
        if (linkSetIndex.containsKey(linkSetId)) {
            return linkSetIndex.get(linkSetId);
        }
        throw new IllegalArgumentException("No such linkSet: " + linkSetId);
    }

    public List<LinkSet> getLinkSets() {
        return linkSets;
    }

    public List<Link> getOutcomes() {
        return outcomes;
    }

    public Page getPage(String pageId) throws PageNotFoundException {
        indexPagesIfNeeded();
        if (pageIndex.containsKey(pageId)) {
            return pageIndex.get(pageId);
        }
        throw new PageNotFoundException(pageId);
    }

    public List<Page> getPages() {
        return pages;
    }

    public boolean hasEmail(String emailId) {
        try {
            getEmail(emailId);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    public boolean hasPage(String pageId) {
        try {
            getPage(pageId);
            return true;
        } catch (PageNotFoundException e) {
            return false;
        }
    }

    public boolean isEmailEnabled() {
        // TODO
        // return getEmails().size() > 0;
        return true;
    }

    public boolean isErrorHandlingEnabled() {
        return hasPage("error");
    }

    public boolean isSecurityEnabled() {
        return getLoginPage() != null;
    }

    public void setDefaultPage(Page defaultPage) {
        this.defaultPage = defaultPage;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public void setHtmlExtension(String htmlExtension) {
        this.htmlExtension = htmlExtension;
    }

    public void setLinkSets(List<LinkSet> linkSets) {
        this.linkSets = linkSets;
    }

    public void setOutcomes(List<Link> outcomes) {
        this.outcomes = outcomes;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    private void indexEmails() {
        emailIndex = new HashMap<String, Email>();
        for (Email email : emails) {
            emailIndex.put(email.getId(), email);
        }
    }

    private void indexEmailsIfNeeded() {
        if (emailIndex == null) {
            indexEmails();
        }
    }

    private void indexEntities() {
        entityIndex = new HashMap<String, Entity>();
        for (Entity entity : entities) {
            entityIndex.put(entity.getId(), entity);
        }
    }

    private void indexEntitiesIfNeeded() {
        if (entityIndex == null) {
            indexEntities();
        }
    }

    private void indexLinkSets() {
        linkSetIndex = new HashMap<String, LinkSet>();
        for (LinkSet linkSet : linkSets) {
            linkSetIndex.put(linkSet.getId(), linkSet);
        }
    }

    private void indexLinkSetsIfNeeded() {
        if (linkSetIndex == null) {
            indexLinkSets();
        }
    }

    private void indexPages() {
        pageIndex = new HashMap<String, Page>();
        for (Page page : pages) {
            pageIndex.put(page.getId(), page);
        }
    }

    private void indexPagesIfNeeded() {
        if (pageIndex == null) {
            indexPages();
        }
    }

    public Content getContent(String contentId) throws AppspecConstraintViolationException {
        if (!getContentIndex().containsKey(contentId)) {
            throw new AppspecConstraintViolationException("No global content found with id: " + contentId);
        }

        return getContentIndex().get(contentId);
    }

    public Map<String, Content> getContentIndex() {
        return contentIndex;
    }

    public void setContentIndex(Map<String, Content> contentIndex) {
        this.contentIndex = contentIndex;
    }

    public static Appspec create(AppspecType appspecType) throws AppspecConstraintViolationException {
        String id = appspecType.getId();
        String title = appspecType.getTitle();
        id = id != null ? id : ClickframeUtils.toCompactId(title);
        Appspec appspec = new Appspec(id, title);
        appspec.setDescription(appspecType.getDescription());

        // this must occur before page, as page content depends on this
        for (Content content : Content.createList(appspecType.getContents(), appspec)) {
            appspec.getContentIndex().put(content.getId(), content);
        }

        appspec.getEntities().addAll(Entity.createList(appspec, appspecType.getEntities(), appspec));

        appspec.getEmails().addAll(Email.createList(appspec, appspecType.getEmails()));

        AppspecReader.copyOutcomes(appspecType, appspec);

        AppspecReader.copyLinkSets(appspecType, appspec);

        appspec.getPermissions().addAll(Permission.createList(appspec, appspecType.getPermissions()));

        // page blocks
        appspec.getPageBlocks().addAll(PageBlock.createList(appspec, appspecType.getPageBlocks()));

        // build pages after entities
        // build pages after pageBlocks
        appspec.getPages().addAll(Page.createList(appspec, appspecType.getPages()));

        AppspecReader.copySecurity(appspecType, appspec);

        AppspecReader.copyDefaultPage(appspecType, appspec);

        AppspecReader.validateProject(appspec);

        resolveAppspec(appspec);

        return appspec;
    }

    public void setLoginPage(Page loginPage) {
        this.loginPage = loginPage;
    }

    public static void resolveAppspec(Appspec appspec) throws InternalLinkResolutionException,
            ResourceNotFoundException, EntityNotFoundException, PageNotFoundException {
        Page.resolvePages(appspec);
        Email.resolveEmails(appspec);
        AppspecReader.resolveLinkSets(appspec, appspec.getLinkSets());
        AppspecReader.resolveOutcomes(appspec, appspec.getOutcomes());
        resolveEntities(appspec);
    }

    /**
     * This method will automatically try to guess the entity from the login
     * form
     * 
     * @return the entity which represents a system User
     * 
     * @author Vineet Manohar
     */
    public Entity getLoginEntity() {
        Page loginPage = getLoginPage();

        if (loginPage != null) {
            Form loginForm;
            try {
                loginForm = loginPage.getLoginForm();
            } catch (AppspecConstraintViolationException e) {
                throw new RuntimeException(
                        "Login form should be available on the login page, possible developer error", e);
            }
            if (loginForm != null) {
                SingleUserInput input;
                try {
                    input = loginForm.getLoginUsernameInput();
                } catch (AppspecConstraintViolationException e) {
                    throw new RuntimeException(
                            "Login username field should be available on the login form, possible developer error", e);
                }
                if (input.getEntityProperty() != null) {
                    return input.getEntityProperty().getEntity();
                }
            }
        }

        return null;
    }

    /**
     * This method will automatically try to guess the entity from the login
     * form and login user entity property
     * 
     * @return the entity which represents a system User
     * 
     * @author Vineet Manohar
     */
    public EntityProperty getLoginUsernameEntityProperty() {
        Page loginPage = getLoginPage();

        if (loginPage != null) {
            Form loginForm;
            try {
                loginForm = loginPage.getLoginForm();
            } catch (AppspecConstraintViolationException e) {
                throw new RuntimeException(
                        "Login form should be available on the login page, possible developer error", e);
            }
            if (loginForm != null) {
                SingleUserInput input;
                try {
                    input = loginForm.getLoginUsernameInput();
                } catch (AppspecConstraintViolationException e) {
                    throw new RuntimeException(
                            "Login username field should be available on the login form, possible developer error", e);
                }
                if (input.getEntityProperty() != null) {
                    return input.getEntityProperty();
                }
            }
        }

        return null;
    }

    /**
     * This method will automatically try to guess the entity from the login
     * form and login password entity property
     * 
     * @return the entity which represents a system User
     * 
     * @author Vineet Manohar
     */
    public EntityProperty getLoginPasswordEntityProperty() {
        Page loginPage = getLoginPage();

        if (loginPage != null) {
            Form loginForm;
            try {
                loginForm = loginPage.getLoginForm();
            } catch (AppspecConstraintViolationException e) {
                throw new RuntimeException(
                        "Login form should be available on the login page, possible developer error", e);
            }
            if (loginForm != null) {
                SingleUserInput input;
                try {
                    input = loginForm.getLoginPasswordInput();
                } catch (AppspecConstraintViolationException e) {
                    throw new RuntimeException(
                            "Login username field should be available on the login form, possible developer error", e);
                }
                if (input.getEntityProperty() != null) {
                    return input.getEntityProperty();
                }
            }
        }

        return null;
    }

    /**
     * Tells entity which forms link to it.
     * 
     * @param appspec
     */
    public static void resolveEntities(Appspec appspec) {
        // referrring forms
        for (Page page : appspec.getPages()) {
            for (Form form : page.getForms()) {
                form.setPage(page);
                for (SingleUserInput input : form.getInputs()) {
                    EntityProperty property = input.getEntityProperty();
                    if (property != null) {
                        property.getEntity().getReferringForms().add(form);
                    }
                }
            }
        }

        // foreign keys
        for (Entity entity : appspec.getEntities()) {
            for (EntityProperty property : entity.getProperties()) {
                if (property.getForeignEntityId() != null) {
                    property.setForeignEntity(appspec.getEntity(property.getForeignEntityId()));
                }
            }
        }
    }

    public PageBlock getPageBlock(String id) {
        for (PageBlock pageBlock : pageBlocks) {
            if (pageBlock.getId().equals(id)) {
                return pageBlock;
            }
        }

        throw new PageBlockNotFoundException(id);
    }

    public Permission getPermission(String id) {
        for (Permission permission : permissions) {
            if (permission.getId().equals(id)) {
                return permission;
            }
        }

        throw new PermissionNotFoundException(id);
    }
    

	@Override
	public String getMetaName() {
		return "appspec";
	}
}