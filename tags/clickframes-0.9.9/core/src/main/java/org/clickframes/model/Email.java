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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.clickframes.AppspecReader;
import org.clickframes.xmlbindings.EmailType;
import org.clickframes.xmlbindings.EmailsType;

/**
 * Email requirement type for the ClickFrames model.
 */
public class Email extends AbstractElement {

	private boolean loginRequired;
    private String emailSubject;
    private String emailText;
    private List<LinkSet> linkSets = new ArrayList<LinkSet>();
    private List<String> linkSetIds = new ArrayList<String>();
    private List<Link> links = new ArrayList<Link>();

    private List<Fact> facts = new ArrayList<Fact>();
    private List<Output> outputs = new ArrayList<Output>();
    private List<PageParameter> parameters = new ArrayList<PageParameter>();

    protected Email(String id, AppspecElement parent) {
		super(id, parent);
	}
    
    public boolean isLoginRequired() {
        return loginRequired;
    }

    public void setLoginRequired(boolean loginRequired) {
        this.loginRequired = loginRequired;
    }

    public String getEmailText() {
        return emailText;
    }

    public void setEmailText(String emailText) {
        this.emailText = emailText;
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

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    private static void resolveEmailLinkSets(Appspec appspec, Email email) {
        for (String linkSetId : email.getLinkSetIds()) {
            LinkSet linkSet = appspec.getLinkSet(linkSetId);
            email.getLinkSets().add(linkSet);
        }
    }

    public static void resolveEmail(Appspec appspec, Email email) throws ResourceNotFoundException,
            PageNotFoundException {
        AppspecReader.resolveLinks(appspec, email.getLinks());
        resolveEmailLinkSets(appspec, email);
    }

    public static Email create(Appspec appspec, EmailType emailType, AppspecElement parent) {
        Email email = new Email(emailType.getId(), parent);
        email.setTitle(emailType.getTitle());
        email.setDescription(emailType.getDescription());
        email.setEmailSubject(emailType.getEmailSubject());
        email.setEmailText(emailType.getEmailText());
        email.getFacts().addAll(Fact.createList(emailType.getFacts()));

        email.getLinks().addAll(Link.createList(appspec, emailType.getLinks(), parent));

        email.getLinkSetIds().addAll(LinkSet.createLinkSetIds(emailType.getLinkSetRefs()));
        email.getOutputs().addAll(Output.createList(appspec, emailType.getOutputs(), email));
        email.getParameters().addAll(PageParameter.createList(appspec, emailType.getParams()));

        return email;
    }

    static List<Email> createList(Appspec appspec, EmailsType emailsType) {
        List<Email> retVal = new ArrayList<Email>();
        if (emailsType != null) {
            for (EmailType emailType : emailsType.getEmail()) {
                retVal.add(create(appspec, emailType, appspec));
            }
        }
        return retVal;
    }

    static void resolveEmails(Appspec appspec) throws ResourceNotFoundException, PageNotFoundException {
        for (Email email : appspec.getEmails()) {
            resolveEmail(appspec, email);
        }
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Output> outputs) {
        this.outputs = outputs;
    }

    public List<PageParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<PageParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Email) {
            Email other = (Email) obj;
            return other.getId().equals(this.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

	@Override
	public String getMetaName() {
		return "email";
	}
}