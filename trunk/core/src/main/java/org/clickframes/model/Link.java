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

import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.FactsType;
import org.clickframes.xmlbindings.LinkType;
import org.clickframes.xmlbindings.LinksType;

/**
 * @author Vineet Manohar
 */
public abstract class Link extends AbstractElement {
    public static final boolean DEFAULT_OUTCOME_TYPE = false;
    private List<Fact> facts = new ArrayList<Fact>();
    /**
     * TODO: refactor field to outcome subclass
     * 
     * applies only to outcomes
     */
    private String message;

    /**
     * TODO: refactor field to outcome subclass
     */
    private boolean loginSuccessfulOutcome;

    /**
     * TODO: refactor field to outcome subclass
     */
    private boolean loginFailedOutcome;

    /**
     * TODO: refactor field to outcome subclass
     * 
     * applies only to outcomes
     */
    private boolean negative;

    private List<String> emailRefs = new ArrayList<String>();
    private List<Email> emails = new ArrayList<Email>();

    protected Link(String id, String title, String description, FactsType factsType, Boolean negative, String message,
            List<String> emailRefs, boolean loginSuccessfulOutcome, boolean loginFailedOutcome, AppspecElement parent) {
        super(id, title, parent);
        setDescription(description);

        if (emailRefs != null) {
            setEmailRefs(emailRefs);
        }

        this.facts.addAll(Fact.createList(factsType));

        if (negative != null) {
            setNegative(negative);
        }
        if (message != null) {
            setMessage(ClickframeUtils.normalize(message));
        }

        this.loginSuccessfulOutcome = loginSuccessfulOutcome;
        this.loginFailedOutcome = loginFailedOutcome;
    }
    
    public String getKey() {
    	if (getParent() == null || getParent().getParent() == null) {
    		return null;
    	}
    	return getParent().getUppercaseId() + "_" + getParent().getParent().getUppercaseId() + "_" + getUppercaseId();
    }

    public abstract boolean isInternal();

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public abstract String getTargetTitle();

    public String getMessage() {
        if (message == null) {
            return getTitle();
        }

        return message;
    }

    public String getMessageEscaped() {
        return ClickframeUtils.escapeXml(getMessage());
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isLoginSuccessfulOutcome() {
        return loginSuccessfulOutcome;
    }

    public void setLoginSuccessfulOutcome(boolean loginSuccessfulOutcome) {
        this.loginSuccessfulOutcome = loginSuccessfulOutcome;
    }

    public boolean isLoginFailedOutcome() {
        return loginFailedOutcome;
    }

    public void setLoginFailedOutcome(boolean loginFailedOutcome) {
        this.loginFailedOutcome = loginFailedOutcome;
    }

    public void setEmailRefs(List<String> emailRefs) {
        this.emailRefs = emailRefs;
    }

    public List<String> getEmailRefs() {
        return emailRefs;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void addEmail(Email email) {
        getEmails().add(email);
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public static Link create(Appspec appspec, LinkType linkType, AppspecElement parent) {
        if (linkType.getHref() != null && linkType.getPageRef() != null) {
            throw new IllegalArgumentException("Cannot process a link type with both href and pageRef set: "
                    + linkType.getHref() + ", " + linkType.getPageRef());
        }
        if (linkType.getHref() != null) {
            return new ExternalLink(linkType.getId(), linkType.getHref(), linkType.getTitle(), linkType
                    .getDescription(), linkType.getFacts(), null, null, null, false, false, parent);
        }
        if (linkType.getPageRef() != null) {
            return new InternalLink(appspec, linkType.getId(), linkType.getPageRef(), linkType.getTitle(), linkType
                    .getDescription(), linkType.getFacts(), null, null, null, false, false, parent);
        }

        throw new IllegalArgumentException("Cannot process a link type with neither href nor pageRef set: "
                + linkType.getHref() + ", " + linkType.getPageRef());
    }

    public static List<Link> createList(Appspec appspec, LinksType linksType, AppspecElement parent) {
        List<Link> list = new ArrayList<Link>();

        if (linksType != null) {
            for (LinkType linkType : linksType.getLink()) {
                Link link = create(appspec, linkType, parent);
                list.add(link);
            }
        }

        return list;
    }
    
}