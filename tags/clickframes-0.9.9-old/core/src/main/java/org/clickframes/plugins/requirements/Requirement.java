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

package org.clickframes.plugins.requirements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Content;
import org.clickframes.model.Email;
import org.clickframes.model.EmailValidation;
import org.clickframes.model.ExternalLink;
import org.clickframes.model.Fact;
import org.clickframes.model.Form;
import org.clickframes.model.InternalLink;
import org.clickframes.model.LengthValidation;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Page;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.model.PageParameter;
import org.clickframes.model.RegexValidation;
import org.clickframes.model.RequiredValidation;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.renderer.RequirementsGenerator;
import org.clickframes.util.ClickframeUtils;

public class Requirement {
	private Appspec appspec;
	private Page page;
	/**
	 * the single user input represented by this requirement, or null if none
	 */
	private SingleUserInput singleUserInput;

	private String identifier;
	private String title;
	private String description;

	public Appspec getProject() {
		return appspec;
	}

	public void setProject(Appspec appspec) {
		this.appspec = appspec;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static Requirement createRequirement(Appspec appspec, Page page, Action action, Link outcome, String title) {
		return Requirement.createRequirement(appspec, page, title);
	}

	public SingleUserInput getSingleUserInput() {
		return singleUserInput;
	}

	public void setSingleUserInput(SingleUserInput singleUserInput) {
		this.singleUserInput = singleUserInput;
	}

    public static void addReq(List<Requirement> requirements, Appspec appspec, Page page, String title, String id) {
        Requirement req = createRequirement(appspec, page, title);
        req.setIdentifier(id);
        requirements.add(req);
    }

    public static String indefiniteArticle(String type) {
        if ("aeiouAEIOU".contains(type.substring(0, 1))) {
            return "an ";
        }

        return "a ";
    }

    public static boolean isOutcomeToSamePage(Page page, Link outcome) throws PageNotFoundException {
        if (outcome instanceof ExternalLink) {
            return false;
        }
        InternalLink internalOutcome = (InternalLink) outcome;
        return isSamePage(page, internalOutcome.getPage());
    }

    public static boolean isSamePage(Page page1, Page page2) {
        return page1.getId().equals(page2.getId());
    }

    public static void generateRequirementsForProjectLinkSet(List<Requirement> requirements, Appspec appspec,
            LinkSet linkSet, String identifierPrefix) {
        if (linkSet.isGlobal()) {
            Requirement requirement = createRequirement(appspec, null, "The '" + linkSet.getTitle()
                    + "' link set shall appear on all pages");
            requirements.add(requirement);
            requirement.setIdentifier(identifierPrefix + ".global");
        }

        // facts
        for (int factNo = 1; factNo <= linkSet.getFacts().size(); factNo++) {
            Fact fact = linkSet.getFacts().get(factNo - 1);
            Requirement requirement = createRequirement(appspec, null, fact.getDescription());
            requirement.setIdentifier(identifierPrefix + ".fact" + factNo);
            requirements.add(requirement);
        }

        int linkNo = 1;
        for (Link link : linkSet.getLinks()) {
            Requirement requirement = createRequirement(appspec, null, "The '" + linkSet.getTitle()
                    + "' link set shall have a link labeled '" + link.getTitle()
                    + "' which redirects the user to the '" + link.getTargetTitle() + "' page.");
            requirement.setIdentifier(identifierPrefix + "." + ClickframeUtils.toCompactId(link.getTitle()));
            requirements.add(requirement);
            linkNo++;
        }
    }

    /**
     * @param requirements
     * @param appspec
     * @param page
     * @param action
     * @param outcome
     *
     * @author Vineet Manohar
     * @param identifierPrefix
     */
    static void generateRequirementsForActionOutcomeFacts(List<Requirement> requirements, Appspec appspec,
            Page page, Action action, Link outcome, String identifierPrefix) {
        int factNo = 1;
        for (Fact fact : outcome.getFacts()) {
            Requirement requirement = createRequirement(appspec, page, action, outcome,
                    "When user press the '" + action.getTitle() + "' button and the '" + outcome.getTitle()
                            + "' outcome occurs, then " + fact.getDescription());
            requirement.setIdentifier(identifierPrefix + ".fact" + factNo);
            requirements.add(requirement);
            factNo++;
        }
    }

    /**
     * /** #foreach ($outcome in $action.outcomes) #writeReq("$pageNo.$reqNo",
     * $page, "When user performs #action($action) action and
     * #outcome($outcome), user shall be redirected to
     * #outcomeTarget($outcome)", "&nbsp;") #set( $reqNo = $reqNo + 1 )
     *
     * @param requirements
     * @param appspec
     * @param page
     * @param action
     *
     * @author Vineet Manohar
     * @param identifierPrefix
     * @throws PageNotFoundException
     */
    public static void generateRequirementsForActionOutcomes(List<Requirement> requirements, Appspec appspec,
            Page page, Action action, String identifierPrefix) throws PageNotFoundException {
        for (Link outcome : action.getOutcomes()) {
            String title;

            if (isOutcomeToSamePage(page, outcome)) {
                title = "When user presses the '" + action.getTitle() + "' button and the '" + outcome.getTitle()
                        + "' outcome occurs, user shall remain on the same page.";
            } else {
                title = "When user presses the '" + action.getTitle() + "' button and the '" + outcome.getTitle()
                        + "' outcome occurs, user shall be redirected to the '" + outcome.getTargetTitle() + "' page.";
            }

            Requirement requirement = createRequirement(appspec, page, action, outcome, title);
            String identifier = identifierPrefix + "." + ClickframeUtils.toCompactId(outcome.getTitle());
            requirement.setIdentifier(identifier);

            requirements.add(requirement);

            generateRequirementsForActionOutcomeFacts(requirements, appspec, page, action, outcome, identifier);
        }
    }

    private static void generateRequirementsForEmailFacts(List<Requirement> requirements, Appspec appspec, Email email) {
        int factNumber = 1;
        for (Fact fact : email.getFacts()) {
            Requirement.addReq(requirements, appspec, null, fact.getDescription(), email.getId() + ".fact" + factNumber);
            factNumber++;
        }
    }

    private static void generateRequirementsForEmailLinks(List<Requirement> requirements, Appspec appspec, Email email) {
        int linkNumber = 1;
        for (Link link : email.getLinks()) {
            Requirement.addReq(requirements, appspec, null, "The '" + email.getTitle() + "' e-mail shall have a link labeled '"
                    + link.getTitle() + "' which redirects the user to the '" + link.getTargetTitle() + "' page.",
                    email.getId() + ".link" + linkNumber);
        }
    }

    public static void generateRequirementsForEmailLinksets(List<Requirement> requirements, Appspec appspec, Email email) {

        for (LinkSet linkSet : email.getLinkSets()) {
            Requirement.addReq(requirements, appspec, null, "The '" + email.getTitle()
                    + "' e-mail shall display all links from the '" + linkSet.getTitle() + "' link set.", email.getId()
                    + ".linkSets." + linkSet.getId());
        }
    }

    public static void generateRequirementsForFormActions(List<Requirement> requirements, Appspec appspec, Page page,
            Form form) throws PageNotFoundException {
        for (Action action : form.getActions()) {
            String idPrefix = page.getId() + "." + form.getId() + "." + action.getId();
            Requirement.addReq(requirements, appspec, page, "The '" + form.getId() + "' form on the '" + page.getTitle()
                    + "' page shall contain a '" + action.getTitle() + "' button.", idPrefix);
            generateRequirementsForActionOutcomes(requirements, appspec, page, action, idPrefix);
        }
    }

    static void generateRequirementsForFormInputFacts(List<Requirement> requirements, Appspec appspec,
            Page page, Form form, SingleUserInput input, String idPrefix) {

        int factNumber = 1;
        for (Fact fact : input.getFacts()) {
            Requirement.addReq(requirements, appspec, page, fact.getDescription(), idPrefix + ".fact" + factNumber);
            factNumber++;
        }
    }

    private static void generateRequirementsForFormInputOptions(List<Requirement> requirements, Appspec appspec,
            Page page, Form form, SingleUserInput input, String idPrefix) {

        for (Entry<String, String> option : input.getAllowedValues().entrySet()) {
            Requirement.addReq(requirements, appspec, page, "The '" + input.getTitle() + "' field shall have the option '"
                    + option.getValue() + "' with the value '" + option.getKey() + "'.", idPrefix + ".option."
                    + option.getKey());
        }
    }

    public static void generateRequirementsForFormInputs(List<Requirement> requirements, Appspec appspec, Page page,
            Form form) {

        for (SingleUserInput input : form.getInputs()) {
            String text = "The '" + form.getId() + "' form on the '" + page.getTitle() + "' page shall provide "
                    + indefiniteArticle(input.getType()) + input.getType() + " field entitled '" + input.getTitle()
                    + "'";
            if (input.getDescription() != null && input.getDescription().length() > 0) {
                text += ", which is " + ClickframeUtils.lowerCaseFirstChar(input.getDescription());
            } else {
                text += ".";
            }

            String idPrefix = page.getId() + "." + form.getId() + ".input." + input.getId();

            Requirement.addReq(requirements, appspec, page, text, idPrefix);

            generateRequirementsForFormInputValidations(requirements, appspec, page, form, input, idPrefix);
            generateRequirementsForFormInputOptions(requirements, appspec, page, form, input, idPrefix);
            generateRequirementsForFormInputFacts(requirements, appspec, page, form, input, idPrefix);
        }
    }

    private static void generateRequirementsForFormInputValidations(List<Requirement> requirements, Appspec appspec,
            Page page, Form form, SingleUserInput input, String idPrefix) {

        for (Validation validation : input.getValidations()) {

            String fieldDescription = "the " + input.getType() + " field '" + input.getTitle() + "' in the '"
                    + form.getId() + "' form on the '" + page.getTitle() + "' page";

            if (validation instanceof RequiredValidation) {
                Requirement.addReq(requirements, appspec, page, "Input for " + fieldDescription + " shall be required.", idPrefix
                        + ".validation.required");
                Requirement.addReq(requirements, appspec, page, "The message '" + validation.getAuthoredOrGeneratedDescription()
                        + "' shall be displayed if no input is provided for " + fieldDescription + ".", idPrefix
                        + ".validation.required.message");
            } else if (validation instanceof EmailValidation) {
                Requirement.addReq(requirements, appspec, page, "Input for " + fieldDescription
                        + " must be in the form of an e-mail.", idPrefix + ".validation.email");
                Requirement.addReq(requirements, appspec, page, "The message '" + validation.getAuthoredOrGeneratedDescription()
                        + "' shall be displayed if e-mail validation fails for " + fieldDescription + ".", idPrefix
                        + ".validation.email.message");
            } else if (validation instanceof LengthValidation) {
                LengthValidation lengthValidation = (LengthValidation) validation;
                if (lengthValidation.isMinDefined() && lengthValidation.isMaxDefined()
                        && lengthValidation.getMin() == lengthValidation.getMax()) {
                    Requirement.addReq(requirements, appspec, page, "Input for " + fieldDescription + " shall be exactly "
                            + lengthValidation.getMin() + " characters.", idPrefix + "validation.length");
                    Requirement.addReq(requirements, appspec, page, "The message '"
                            + validation.getAuthoredOrGeneratedDescription()
                            + "' shall be displayed if length validation fails for " + fieldDescription + ".", idPrefix
                            + ".validation.length.message");
                } else {
                    if (lengthValidation.isMinDefined()) {
                        Requirement.addReq(requirements, appspec, page, "Input for " + fieldDescription + " shall be at least "
                                + lengthValidation.getMin() + " characters.", idPrefix + "validation.minlength");
                        Requirement.addReq(requirements, appspec, page, "The message '"
                                + validation.getAuthoredOrGeneratedDescription()
                                + "' shall be displayed if minimum length validation fails for " + fieldDescription
                                + ".", idPrefix + ".validation.minlength.message");
                    }
                    if (lengthValidation.isMaxDefined()) {
                        Requirement.addReq(requirements, appspec, page, "Input for " + fieldDescription + " shall be at most "
                                + lengthValidation.getMin() + " characters.", idPrefix + "validation.maxlength");
                        Requirement.addReq(requirements, appspec, page, "The message '"
                                + validation.getAuthoredOrGeneratedDescription()
                                + "' shall be displayed if maximum length validation fails for " + fieldDescription
                                + ".", idPrefix + ".validation.maxlength.message");
                    }
                }
            } else if (validation instanceof RegexValidation) {
                RegexValidation regexValidation = (RegexValidation) validation;
                Requirement.addReq(requirements, appspec, page, "Input for " + fieldDescription
                        + " must satisfy the regular expression '" + regexValidation.getRegex() + "'.", idPrefix
                        + ".validation.regex");
                Requirement.addReq(requirements, appspec, page, "The message '" + validation.getAuthoredOrGeneratedDescription()
                        + "' shall be displayed if regular expression validation fails for " + fieldDescription + ".",
                        idPrefix + ".validation.regex.message");
            } else {
                Requirement.addReq(requirements, appspec, page, "Input for " + fieldDescription + " shall be validated as type '"
                        + validation.getType() + "'.", idPrefix + ".validation.regex");
                Requirement.addReq(requirements, appspec, page, "The message '" + validation.getAuthoredOrGeneratedDescription()
                        + "' shall be displayed if " + validation.getType() + " validation fails for "
                        + fieldDescription + ".", idPrefix + ".validation.regex.message");
            }
        }
    }

    public static void generateRequirementsForPageBlurbs(List<Requirement> requirements, Appspec appspec, Page page) {

        for (Content content : page.getVerbatimContents()) {
            Requirement.addReq(requirements, appspec, page, "The '" + page.getTitle() + "' page shall include the content: \""
                    + ClickframeUtils.normalize(content.getText()) + "\"", page.getId() + ".content." + content.getId());
        }
        for (Content content : page.getNonVerbatimContents()) {
            Requirement.addReq(requirements, appspec, page, "The '" + page.getTitle()
                    + "' page shall include the complex content: \"" + ClickframeUtils.normalize(content.getText())
                    + "\"", page.getId() + ".content." + content.getId());
        }
    }

    /**
     * @param requirements
     * @param appspec
     * @param page
     * @return
     *
     * @author Vineet Manohar
     */
    static List<Requirement> generateRequirementsForPageFacts(List<Requirement> requirements, Appspec appspec,
            Page page) {
        int factNo = 1;
        for (Fact fact : page.getFacts()) {
            Requirement requirement = createRequirement(appspec, page, fact.getDescription());
            requirement.setIdentifier(page.getId() + ".fact" + factNo);
            requirements.add(requirement);
            factNo++;
        }

        return requirements;
    }

    static List<Requirement> generateRequirementsForPageForms(List<Requirement> requirements, Appspec appspec,
            Page page)
            throws PageNotFoundException {
        for (int formNo = 1; formNo <= page.getForms().size(); formNo++) {
            Form form = page.getForms().get(formNo - 1);
            String title = "PAGE shall include a form identified as '" + form.getId() + "'.";
            Requirement requirement = createRequirement(appspec, page, title);
            requirement.setIdentifier(page.getId() + ".form." + form.getId());

            generateRequirementsForFormInputs(requirements, appspec, page, form);
            generateRequirementsForFormActions(requirements, appspec, page, form);
        }
        return requirements;
    }

    private static void generateRequirementsForPageLinkFacts(List<Requirement> requirements, Appspec appspec,
            Page page, Link link, String identifier) {
        int factNumber = 1;
        for (Fact fact : link.getFacts()) {
            Requirement.addReq(requirements, appspec, page, fact.getDescription(), identifier + ".fact" + factNumber);
            factNumber++;
        }
    }

    /**
     * * #foreach ($link in $page.links) #writeReq("$pageNo.$reqNo", $page,
     * "Page shall have a link labeled #link($link) which redirects the user to
     * $link.page.title", "&nbsp;") #set( $reqNo = $reqNo + 1 ) #end
     *
     * @param requirements
     * @param appspec
     * @param page
     *
     * @author Vineet Manohar
     * @param identifierPrefix
     */
    public static void generateRequirementsForPageLinks(List<Requirement> requirements, Appspec appspec, Page page,
            String identifierPrefix) {
        int linkNo = 1;
        for (Link link : page.getLinks()) {
            Requirement requirement = createRequirement(appspec, page, "The '" + page.getTitle()
                    + "' page shall have a link labeled '" + link.getTitle() + "' which redirects the user to the '"
                    + link.getTargetTitle() + "' page.");
            requirement.setIdentifier(identifierPrefix + "." + ClickframeUtils.toCompactId(link.getTitle()));
            requirements.add(requirement);
            linkNo++;

            generateRequirementsForPageLinkFacts(requirements, appspec, page, link, requirement.getIdentifier());
        }
    }

    public static void generateRequirementsForPageLinkSets(List<Requirement> requirements, Appspec appspec, Page page,
            String identifierPrefix) {
        for (LinkSet linkSet : page.getLinkSets()) {
            Requirement requirement = createRequirement(appspec, page, "The '" + page.getTitle()
                    + "' page shall display all links from the '" + linkSet.getTitle() + "' link set.");
            requirement.setIdentifier(identifierPrefix + "." + ClickframeUtils.toCompactId(linkSet.getTitle()));
            requirements.add(requirement);
        }
    }

    static void generateRequirementsForPageParams(List<Requirement> requirements, Appspec appspec, Page page) {

        for (PageParameter param : page.getParameters()) {
            Requirement.addReq(requirements, appspec, page, "The '" + page.getTitle() + "' page shall accept the URL parameter '"
                    + param.getId() + "'.", page.getId() + ".param." + param.getId());
        }
    }

    public static void generateRequirementsForProjectOutcomeFacts(List<Requirement> requirements, Appspec appspec,
            Link outcome, String identifierPrefix) {
        int factNo = 1;
        for (Fact fact : outcome.getFacts()) {
            Requirement requirement = createRequirement(appspec, null, null, outcome,
                    "If during any action, the '" + outcome.getTitle() + "' outcome occurs, then "
                            + fact.getDescription());
            requirement.setIdentifier(identifierPrefix + ".fact" + factNo);
            requirements.add(requirement);
            factNo++;
        }
    }

    public static void generateRequirementsForEmails(List<Requirement> requirements, Appspec appspec) {
        for (Email email : appspec.getEmails()) {
            Requirement.addReq(requirements, appspec, null, "The application shall provide an e-mail entitled '" + email.getTitle()
                    + "'.", email.getId());
            Requirement.addReq(requirements, appspec, null, "The '" + email.getTitle() + "' e-mail shall have the subject: \""
                    + email.getEmailSubject() + "\".", email.getId() + ".subject");
            // TODO: Figure out how to format email text appropriately
            Requirement.addReq(requirements, appspec, null, "The '" + email.getTitle() + "' e-mail shall have the message: \""
                    + ClickframeUtils.normalize(email.getEmailText()) + "\"", email.getId() + ".message");

            generateRequirementsForEmailFacts(requirements, appspec, email);
            generateRequirementsForEmailLinks(requirements, appspec, email);
            generateRequirementsForEmailLinksets(requirements, appspec, email);
        }
    }

    public static void generateRequirementsForPages(List<Requirement> requirements, Appspec appspec) throws PageNotFoundException {

        for (Page page : appspec.getPages()) {
            Requirement requirement = createRequirement(appspec, page,
                    "System shall have a page entitled '" + page.getTitle() + "'.");
            requirement.setDescription(page.getDescription());
            String identifier = page.getId();
            requirement.setIdentifier(identifier);
            requirements.add(requirement);

            generateRequirementsForPageParams(requirements, appspec, page);
            generateRequirementsForPageFacts(requirements, appspec, page);
            generateRequirementsForPageForms(requirements, appspec, page);
            generateRequirementsForPageLinks(requirements, appspec, page, identifier);
            generateRequirementsForPageLinkSets(requirements, appspec, page, identifier);
            generateRequirementsForPageBlurbs(requirements, appspec, page);
            // generateRequirementsForPageBlurbRefs(requirements, appspec,
            // page);
        }
    }

    public static void generateRequirementsForProjectLinkSets(List<Requirement> requirements, Appspec appspec) {
        for (LinkSet linkSet : appspec.getLinkSets()) {
            Requirement requirement = createRequirement(appspec, null,
                    "The application shall have a link set entitled '" + linkSet.getTitle() + "'.");
            requirements.add(requirement);
            requirement.setIdentifier(ClickframeUtils.toCompactId(linkSet.getTitle()));

            Requirement.generateRequirementsForProjectLinkSet(requirements, appspec, linkSet, requirement
                    .getIdentifier());
        }
    }

    public static void generateRequirementsForProjectOutcomes(List<Requirement> requirements, Appspec appspec) {
        for (Link outcome : appspec.getOutcomes()) {
            Requirement requirement = createRequirement(appspec, null,
                    "If during any action, the outcome '" + outcome.getTitle()
                            + "' occurs, user shall be redirected to the '" + outcome.getTargetTitle() + "' page.");
            requirements.add(requirement);
            requirement.setIdentifier(ClickframeUtils.toCompactId(outcome.getTitle()));

            generateRequirementsForProjectOutcomeFacts(requirements, appspec, outcome, requirement.getIdentifier());
        }
    }

    public static List<Requirement> generateRequirementsForProject(Appspec appspec) throws PageNotFoundException {
        List<Requirement> requirements = new ArrayList<Requirement>();

        Requirement.generateRequirementsForProjectOutcomes(requirements, appspec);
        Requirement.generateRequirementsForProjectLinkSets(requirements, appspec);
        Requirement.generateRequirementsForPages(requirements, appspec);
        Requirement.generateRequirementsForEmails(requirements, appspec);

        return requirements;
    }

    public static void renderSpreadsheet(List<Requirement> requirements, File xls) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        HSSFCellStyle identifierStyle = RequirementsGenerator.createIdentifierColumnStyle(wb);
        HSSFCellStyle requirementStyle = RequirementsGenerator.createRequirementColumnStyle(wb);
        HSSFCellStyle prStyle = RequirementsGenerator.createRequirementColumnStyle(wb);
        HSSFCellStyle headerStyle = RequirementsGenerator.createHeaderColumnStyle(wb);

        short rowHeight = 480;

        short rowNo = 0;
        {
            // header
            HSSFRow row = sheet.createRow(rowNo++);
            row.setHeight(rowHeight);

            HSSFCell cell1 = row.createCell((short) 0);
            cell1.setCellValue(new HSSFRichTextString("Requirement Identifier"));
            cell1.setCellStyle(headerStyle);

            HSSFCell cell2 = row.createCell((short) 1);
            cell2.setCellValue(new HSSFRichTextString("Software Requirement"));
            cell2.setCellStyle(headerStyle);

            HSSFCell cell3 = row.createCell((short) 2);
            cell3.setCellValue(new HSSFRichTextString("Product Requirement"));
            cell3.setCellStyle(headerStyle);
        }

        rowNo++;

        for (Requirement requirement : requirements) {
            HSSFRow row = sheet.createRow(rowNo++);
            row.setHeight(rowHeight);

            HSSFCell identifierCell = row.createCell((short) 0);
            identifierCell.setCellValue(new HSSFRichTextString(requirement.getIdentifier()));
            identifierCell.setCellStyle(identifierStyle);

            HSSFCell requirementCell = row.createCell((short) 1);
            requirementCell.setCellValue(new HSSFRichTextString(requirement.getTitle()));
            requirementCell.setCellStyle(requirementStyle);

            if (requirement.getPage() != null) {
                String pr = requirement.getPage().getProperties().get("pr");

                HSSFCell prCell = row.createCell((short) 2);
                prCell.setCellValue(new HSSFRichTextString(pr));
                prCell.setCellStyle(prStyle);
            }
        }

        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        wb.write(new FileOutputStream(xls));
    }

    static Requirement createRequirement(Appspec appspec, Page page, String title) {
    	Requirement requirement = new Requirement();
    	requirement.setPage(page);
    	requirement.setProject(appspec);
    	requirement.setTitle(title);
    	return requirement;
    }
}
