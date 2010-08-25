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

package org.clickframes;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.AppspecConstraintViolationException;
import org.clickframes.model.AppspecElement;
import org.clickframes.model.OutputList;
import org.clickframes.model.EntityNotFoundException;
import org.clickframes.model.ExternalLink;
import org.clickframes.model.Fact;
import org.clickframes.model.Form;
import org.clickframes.model.InternalLink;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Page;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.model.ResourceNotFoundException;
import org.clickframes.xmlbindings.AppspecType;
import org.clickframes.xmlbindings.LinkSetType;
import org.clickframes.xmlbindings.LinkType;
import org.clickframes.xmlbindings.OutcomeType;

public class AppspecReader {
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(AppspecReader.class);

    public static Appspec readProject(InputStream is) throws JAXBException, AppspecConstraintViolationException {
        AppspecType appspecType = AppspecJaxbWrapper.readAppspecType(is);
        return Appspec.create(appspecType);
    }

    public static void validateProject(Appspec appspec) throws AppspecConstraintViolationException {
        validateSecurityContraints(appspec);
    }

    private static void validateSecurityContraints(Appspec appspec) throws AppspecConstraintViolationException {
        // login-required needs security enabled
        if (!appspec.isSecurityEnabled()) {
            for (Page page : appspec.getPages()) {
                if (page.isLoginRequired()) {
                    throw new AppspecConstraintViolationException("Page '" + page.getId()
                            + "' has login-required='true', but there is no login page. Please indicate the "
                            + "login username, password fields and login button on the login page by adding "
                            + "attribute loginUsername=true, loginPassword=true and loginAction=true respectively");
                }
            }
        }
    }

    public static void copyOutcomes(AppspecType appspecType, Appspec appspec) {
        if (appspecType.getOutcomes() != null) {
            for (OutcomeType outcomeType : appspecType.getOutcomes().getOutcome()) {
                Link outcome = toOutcome(appspec, outcomeType, appspec);
                appspec.getOutcomes().add(outcome);
            }
        }
    }

    public static void copyLinkSets(AppspecType appspecType, Appspec appspec) {
        if (appspecType.getLinkSets() != null) {
            for (LinkSetType linkSetType : appspecType.getLinkSets().getLinkSet()) {
                LinkSet linkSet = new LinkSet(linkSetType.getId(), appspec);
                toLinkSet(appspec, linkSetType, linkSet);
                appspec.getLinkSets().add(linkSet);
            }
        }
    }

    private static void toLinkSet(Appspec appspec, LinkSetType linkSetType, LinkSet linkSet) {
        // linkSet.setId(linkSetType.getId());
        linkSet.setTitle(linkSetType.getTitle());
        linkSet.getFacts().addAll(Fact.createList(linkSetType.getFacts()));
        if (linkSetType.isGlobal() != null) {
            linkSet.setGlobal(linkSetType.isGlobal());
        }

        if (linkSetType.getLinks() != null) {
            for (LinkType linkType : linkSetType.getLinks().getLink()) {
                Link link = Link.create(appspec, linkType, linkSet);
                linkSet.getLinks().add(link);
            }
        }
    }

    public static Link toOutcome(Appspec appspec, OutcomeType outcomeType, AppspecElement parent) {
        if (outcomeType.getHref() != null && outcomeType.getPageRef() != null) {
            throw new IllegalArgumentException("Cannot process a link type with both href and pageRef set: "
                    + outcomeType.getHref() + ", " + outcomeType.getPageRef() + ".");
        }
        if (outcomeType.getHref() != null) {
            return new ExternalLink(outcomeType.getId(), outcomeType.getHref(), outcomeType.getTitle(), outcomeType
                    .getDescription(), outcomeType.getFacts(), outcomeType.isNegative(), outcomeType.getMessage(),
                    outcomeType.getEmailRef(), outcomeType.isLoginSuccessfulOutcome(), outcomeType
                            .isLoginFailedOutcome(), parent);
        }
        if (outcomeType.getPageRef() != null) {
            return new InternalLink(appspec, outcomeType.getId(), outcomeType.getPageRef(), outcomeType.getTitle(),
                    outcomeType.getDescription(), outcomeType.getFacts(), outcomeType.isNegative(), outcomeType
                            .getMessage(), outcomeType.getEmailRef(), outcomeType.isLoginSuccessfulOutcome(),
                    outcomeType.isLoginFailedOutcome(), parent);
        }

        // if both are missing, then stay on current page
        if (parent instanceof Form) {
            Form currentForm = (Form) parent;
            Page currentPage = currentForm.getPage();
            if (currentPage == null) {
                throw new RuntimeException("Developer error: form.getPage() returns null: " + currentForm.getGuid());
            }
            InternalLink samePage = new InternalLink(appspec, outcomeType.getId(), currentPage.getId(), outcomeType
                    .getTitle(), outcomeType.getDescription(), outcomeType.getFacts(), outcomeType.isNegative(),
                    outcomeType.getMessage(), outcomeType.getEmailRef(), outcomeType.isLoginSuccessfulOutcome(),
                    outcomeType.isLoginFailedOutcome(), parent);
            return samePage;
        }

        throw new IllegalArgumentException(
                "Cannot process a link type with both href and pageRef absent. If this parent of this link was a Page element, then no href or pageRef would be interpretted as 'stay on page', but the parent is : "
                        + parent.getGuid());
    }

    public static void resolveOutcomes(Appspec appspec, List<Link> outcomes) throws ResourceNotFoundException,
            PageNotFoundException {
        for (Link outcome : outcomes) {
            resolveOutcome(appspec, outcome);
        }
    }

    public static void resolveLinkSets(Appspec appspec, List<LinkSet> linkSets) throws ResourceNotFoundException,
            PageNotFoundException {
        for (LinkSet linkSet : linkSets) {
            resolveLinkSet(appspec, linkSet);
        }
    }

    private static void resolveLinkSet(Appspec appspec, LinkSet linkSet) throws ResourceNotFoundException,
            PageNotFoundException {
        resolveLinkSetFacts(linkSet);
        resolveLinks(appspec, linkSet.getLinks());
    }

    public static void resolveLinks(Appspec appspec, List<Link> links) throws ResourceNotFoundException,
            PageNotFoundException {
        for (Link link : links) {
            resolveLink(appspec, link);
        }
    }

    private static void resolveLink(Appspec appspec, Link link) throws ResourceNotFoundException, PageNotFoundException {
        if (link instanceof InternalLink) {
            InternalLink internalLink = (InternalLink) link;

            resolveLinkFacts(internalLink);
        }
        resolveLinkEmails(appspec, link);
    }

    private static void resolveLinkEmails(Appspec appspec, Link link) throws ResourceNotFoundException {
        for (String emailRef : link.getEmailRefs()) {
            link.addEmail(appspec.getEmail(emailRef));
        }
    }

    private static void resolvePageFacts(Page page) {
        for (Fact fact : page.getFacts()) {
            resolveFact(page, fact);
        }
    }

    public static String resolvePageVariable(Page page, String text) {
        return text.replaceAll("PAGE", "'" + page.getTitle() + "' page");
    }

    private static String resolveLinkSetVariable(LinkSet linkSet, String text) {
        return text.replaceAll("LINKSET", linkSet.getTitle());
    }

    private static void resolveLinkFacts(InternalLink link) throws PageNotFoundException {
        for (Fact fact : link.getFacts()) {
            resolveFact(link.getPage(), fact);
        }
    }

    private static void resolveFact(Page page, Fact fact) {
        String description = fact.getDescription();
        String resolvedDescription = resolvePageVariable(page, description);
        fact.setDescription(resolvedDescription);
    }

    private static void resolveLinkSetFacts(LinkSet linkSet) {
        for (Fact fact : linkSet.getFacts()) {
            resolvedFact(linkSet, fact);
        }
    }

    private static void resolvedFact(LinkSet linkSet, Fact fact) {
        String description = fact.getDescription();
        String resolvedDescription = resolveLinkSetVariable(linkSet, description);
        fact.setDescription(resolvedDescription);
    }

    public static void resolvePage(Appspec appspec, Page page) throws ResourceNotFoundException,
            EntityNotFoundException, PageNotFoundException {
        resolveForms(appspec, page.getForms());
        resolvePageLinks(appspec, page);
        resolveLinks(appspec, page.getLinks());
        resolvePageFacts(page);
        resolvePageOutputLists(appspec, page);
    }

    private static void resolvePageOutputLists(Appspec appspec, Page page) throws EntityNotFoundException {
        for (OutputList outputList : page.getOutputLists()) {
            resolvePageOutputList(appspec, outputList);
        }
    }

    private static void resolvePageOutputList(Appspec appspec, OutputList outputList) throws EntityNotFoundException {
        outputList.setEntity(appspec.getEntity(outputList.getEntityRef()));
    }

    private static void resolveForms(Appspec appspec, List<Form> forms) throws ResourceNotFoundException,
            PageNotFoundException {
        for (Form form : forms) {
            resolveActions(appspec, form.getActions());
        }
    }

    private static void resolvePageLinks(Appspec appspec, Page page) {
        for (String linkSetId : page.getLinkSetIds()) {
            LinkSet linkSet = appspec.getLinkSet(linkSetId);
            page.getLinkSets().add(linkSet);
        }
    }

    private static void resolveActions(Appspec appspec, List<Action> actions) throws ResourceNotFoundException,
            PageNotFoundException {
        for (Action action : actions) {
            resolveAction(appspec, action);
        }
    }

    private static void resolveAction(Appspec appspec, Action action) throws ResourceNotFoundException,
            PageNotFoundException {
        for (Link outcome : action.getOutcomes()) {
            resolveOutcome(appspec, outcome);
        }
    }

    private static void resolveOutcome(Appspec appspec, Link outcome) throws ResourceNotFoundException,
            PageNotFoundException {
        // the outcome is implemented as a link
        resolveLink(appspec, outcome);
    }

    public static void copySecurity(AppspecType appspecType, Appspec appspec) throws PageNotFoundException {
        // new stuff
        if (appspecType.getLoginPage() != null) {
            appspec.setLoginPage(appspec.getPage(appspecType.getLoginPage()));
        } else {
            // the first login page encountered becomes the default
            for (Page page : appspec.getPages()) {
                if (page.isLoginPage()) {
                    appspec.setLoginPage(page);
                    break;
                }
            }
        }
    }

    /**
     * assumes that the pages have already been copied
     * 
     * @param AppspecType
     * @param appspec
     * 
     * @author Vineet Manohar
     * @throws PageNotFoundException
     */
    public static void copyDefaultPage(AppspecType AppspecType, Appspec appspec) throws PageNotFoundException {
        if (AppspecType.getDefaultPage() != null) {
            appspec.setDefaultPage(appspec.getPage(AppspecType.getDefaultPage()));
        } else {
            if (appspec.getPages().size() > 0) {
                appspec.setDefaultPage(appspec.getPages().get(0));
            }
        }
    }
}