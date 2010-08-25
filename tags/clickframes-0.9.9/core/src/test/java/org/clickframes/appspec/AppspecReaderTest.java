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

package org.clickframes.appspec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.clickframes.model.Action;
import org.clickframes.model.AppspecConstraintViolationException;
import org.clickframes.model.CheckboxInput;
import org.clickframes.model.DropdownInput;
import org.clickframes.model.Email;
import org.clickframes.model.Entity;
import org.clickframes.model.OutputList;
import org.clickframes.model.EntityProperty;
import org.clickframes.model.ExternalLink;
import org.clickframes.model.Form;
import org.clickframes.model.InternalLink;
import org.clickframes.model.LengthValidation;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Page;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.model.PageParameter;
import org.clickframes.model.RadioInput;
import org.clickframes.model.RegexValidation;
import org.clickframes.model.RequiredValidation;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Vineet Manohar
 */
public class AppspecReaderTest extends AbstractPluginTest {
	@Test
	public void testProject() {
		assertEquals("Sample Project Title", techspecContext.getAppspec().getTitle());
	}

	@Test
	public void testProjectSecurity() throws AppspecConstraintViolationException {
		assertTrue(techspecContext.getAppspec().isSecurityEnabled(), "Security module not found");
		Page login = techspecContext.getAppspec().getPage("login");
		assertTrue(login.isLoginPage());
		Form loginForm = login.getLoginForm();
		assertNotNull(login);

		// get login action
		Action loginAction = loginForm.getLoginAction();
		assertNotNull(loginAction);
		assertTrue(loginAction.isLoginAction());

		// get successful outcome
		Link success = loginAction.getLoginSuccessfulOutcome();
		assertNotNull(success);

		Link failure = loginAction.getLoginFailedOutcome();
		assertNotNull(failure);
	}

	@Test
	public void testProjectPages() {
		List<Page> pages = techspecContext.getAppspec().getPages();
		assertEquals(4, pages.size(), "Wrong number of pages");

		Page page1 = pages.get(1);
		assertEquals("Page 1 Title", page1.getTitle(), "Wrong page title");
		assertTrue(page1.isLoginRequired(), "Login is required for page 1");

		assertEquals("Page 1 Description", page1.getDescription(), "Wrong page description");

		assertEquals(2, page1.getFacts().size(), "Could not read facts");
		assertEquals("Fact 1", page1.getFacts().get(0).getDescription(), "Could not read fact 1");

		Page page2 = pages.get(2);
		assertFalse(page2.isLoginRequired(), "Login is required for page 2");
	}

	@Test
	public void testProjectEmails() {
		List<Email> emails = techspecContext.getAppspec().getEmails();
		assertEquals(1, emails.size(), "Wrong number of emails");

		Email email1 = emails.get(0);
		assertEquals("Announcement Email", email1.getTitle(), "Wrong email title");
		assertEquals(2, email1.getFacts().size(), "Could not read facts");
		assertEquals("The email shall include some text.", email1.getFacts().get(0).getDescription(),
				"Could not read fact 1");

		List<Link> links = email1.getLinks();
		assertEquals(1, links.size(), "Wrong number of links in email1");
		assertEquals("http://www.google.com", links.get(0).getTargetTitle(), "Link 1 is not correct");

	}

	@Test
	public void testProjectEntities() {
		List<Entity> entities = techspecContext.getAppspec().getEntities();
		assertEquals(2, entities.size(), "Wrong number of entities");

		Entity entity = entities.get(0);
		assertEquals("User", entity.getTitle(), "Wrong entity title");
	}

	@Test
	public void testPageLinkSets() {
		List<LinkSet> linkSets = techspecContext.getAppspec().getPages().get(1).getLinkSets();

		assertEquals(2, linkSets.size());
		LinkSet linkSet = linkSets.get(0);
		assertEquals("linkSet1", linkSet.getId());

		List<Link> links = linkSet.getLinks();
		assertEquals(2, links.size(), "Wrong number of links in the link set");

		Link link1 = links.get(0);
		assertTrue(link1 instanceof ExternalLink, "First link in the link set is an external link");

		ExternalLink externalLink = (ExternalLink) link1;
		assertEquals("External Link 1 Title", externalLink.getTitle(), "Wrong title for the external link in linkset");
	}

	@Test
	public void testProjectPageActions() {
		Form form2 = techspecContext.getAppspec().getPages().get(1).getForms().get(1);
		List<Action> actions = form2.getActions();
		assertEquals(actions.size(), 2, "Wrong number of actions");

		Action action1 = actions.get(0);
		assertEquals(action1.getId(), "action1", "Wrong action id");
		assertEquals(action1.getTitle(), "Action 1 title", "Wrong action title");
	}

	@Test
	public void testPageOutputList() throws PageNotFoundException {
		Page page1 = techspecContext.getAppspec().getPage("page1");
		List<OutputList> outputLists = page1.getOutputLists();

		assertEquals(outputLists.size(), 1, "Wrong number of outputLists");

		OutputList allUsers = outputLists.get(0);
		assertEquals("allUsers", allUsers.getId(), "Wrong entity list id");
		assertEquals("All users", allUsers.getTitle(), "Wrong entity list title");
		assertEquals("Show all users in the system", allUsers.getDescription(), "Wrong entity list title");
		Entity entity = allUsers.getEntity();
		assertNotNull(entity);
		assertEquals(entity.getId(), "user", "Wrong entity associated with entity list");
		assertEquals(allUsers.getFacts().size(), 3);
	}

	@Test
	public void testPageForms() {
		Page page1 = techspecContext.getAppspec().getPages().get(1);
		assertEquals(page1.getForms().size(), 2);
		Form form1 = page1.getForms().get(0);
		assertEquals(form1.getInputs().size(), 2);
		assertEquals(form1.getActions().size(), 1);
	}

	@Test
	public void testEntityPropertyRef() throws AppspecConstraintViolationException {
		Page loginPage = techspecContext.getAppspec().getPage("login");
		Form loginForm = loginPage.getLoginForm();
		SingleUserInput passwordInput = loginForm.getLoginPasswordInput();
		EntityProperty entityProperty = passwordInput.getEntityProperty();
		Assert.assertNotNull(entityProperty);
		List<Validation> validations = passwordInput.getValidations();
		RequiredValidation requiredValidation = passwordInput.getRequiredValidation();
		Assert.assertEquals(requiredValidation.getDescription(), "Enter password to login");
		Assert.assertEquals(validations.size(), 2);
	}

	@Test
	public void testPageInputs() {
		Page page1 = techspecContext.getAppspec().getPages().get(1);
		Form form2 = page1.getForms().get(1);

		List<SingleUserInput> textInputs = form2.getTextInputs();
		assertEquals(1, textInputs.size(), "Wrong number of inputs");

		SingleUserInput input1 = textInputs.get(0);
		assertEquals("textInput1", input1.getId(), "Wrong input id");
		assertEquals("TextInput 1 title", input1.getTitle(), "Wrong input title");

		List<Validation> validations = input1.getValidations();
		assertEquals(validations.size(), 3, "Wrong number of validations found");
		Validation reqValidation = validations.get(0);
		assertTrue(reqValidation instanceof RequiredValidation, "First validation is of type required");
		assertEquals("required", reqValidation.getId(), "Id of validation not read correctly");

		assertTrue(validations.get(1) instanceof LengthValidation, "Second validation is of type length");
		LengthValidation lengthValidation = (LengthValidation) validations.get(1);

		assertTrue(lengthValidation.isMinDefined(), "Should have minimum length");
		assertFalse(lengthValidation.isMaxDefined(), "Does not have maximum length");
		assertEquals(8, lengthValidation.getMin(), "Minimum length should be 8");

		assertTrue(validations.get(2) instanceof RegexValidation, "Third validation is of type regex");
		RegexValidation regexValidation = (RegexValidation) validations.get(2);
		assertEquals("/^\\S+$/", regexValidation.getTypeArgs(), "Wrong regex found");

		List<DropdownInput> dropdownInputs = form2.getDropdownInputs();
		assertEquals(1, dropdownInputs.size(), "Wrong number of dropdown inputs");

		List<RadioInput> radioInputs = form2.getRadioInputs();
		assertEquals(1, radioInputs.size(), "Wrong number of radio inputs");

		Map<String, String> allowedValues = radioInputs.get(0).getAllowedValues();
		assertEquals(3, allowedValues.size(), "Wrong number of radio options");
	}

	@Test
	public void testPageParams() {
		Page page1 = techspecContext.getAppspec().getPages().get(1);
		List<PageParameter> params = page1.getParameters();
		assertEquals(1, params.size(), "Wrong number of params");

		PageParameter param1 = params.get(0);
		assertEquals("param 1", param1.getTitle(), "Wrong param title");
		assertEquals("Value of param 1", param1.getDescription(), "Wrong param description");
	}

	@Test
	public void testPageCheckboxInputs() {
		Page page1 = techspecContext.getAppspec().getPages().get(1);
		Form form2 = page1.getForms().get(1);
		List<CheckboxInput> checkboxInputs = form2.getCheckboxInputs();
		assertEquals(1, checkboxInputs.size(), "Wrong number of checkbox inputs");

		SingleUserInput input1 = checkboxInputs.get(0);
		assertEquals("CheckboxInput 1 title", input1.getTitle(), "Wrong input title");
	}

	@Test
	public void testPageLinks() {
		Page page1 = techspecContext.getAppspec().getPages().get(1);
		List<Link> links = page1.getLinks();

		assertEquals(2, links.size(), "Wrong number of links");

		Link link1 = links.get(0);

		assertEquals("page1Link1", link1.getId(), "Unable to get correct link id");
		assertEquals(link1.getTitle(), "Link to Page 2");
		assertTrue(link1 instanceof InternalLink, "First link is internal link");
		InternalLink internalLink1 = (InternalLink) link1;
		assertEquals("page2", internalLink1.getPageRef());
		assertNotNull(internalLink1.getPage(), "Page references should be resolved for internal links");
		assertEquals("page2", internalLink1.getPage().getId(), "Page references should be correctly resolved "
				+ "to correct pages for internal links");

		assertEquals("Link to Page 2", link1.getTitle(), "Wrong link title");

		assertEquals(1, link1.getFacts().size(), "Wrong number of facts for the link");
		assertEquals("Fact 1", link1.getFacts().get(0).getDescription(), "Wrong fact for link");
	}

	@Test
	public void testProjectOutcomes() {
		List<Link> outcomes = techspecContext.getAppspec().getOutcomes();

		assertEquals(1, outcomes.size(), "Wrong number of outcomes");

		Link outcome1 = outcomes.get(0);

		assertEquals("Global Outcome 1", outcome1.getTitle(), "Wrong outcome title");
		assertTrue(outcome1 instanceof InternalLink, "Outcome1 is an internal link");

		InternalLink internalLink1 = (InternalLink) outcome1;

		assertNotNull(internalLink1.getPage(), "Appspec references not resolved");
		assertEquals("page1", internalLink1.getPage().getId(), "Wrong pageRef");
		assertEquals("This outcome is shared in all states", outcome1.getDescription(), "Wrong description");
		assertEquals(0, outcome1.getFacts().size(), "Wrong number of facts in outcome");
	}

	@Test
	public void testProjectPageActionOutcomes() {
		List<Link> outcomes = techspecContext.getAppspec().getPages().get(1).getForms().get(1).getActions().get(0)
				.getOutcomes();

		assertEquals(2, outcomes.size(), "Wrong number of outcomes");

		Link outcome1 = outcomes.get(0);
		assertEquals("failure", outcome1.getId(), "Wrong outcome id");
		assertEquals("Outcome to page1", outcome1.getTitle(), "Wrong outcome title");
		assertTrue(outcome1 instanceof InternalLink, "Outcome1 is not an internal link");
		assertNotNull(outcome1.getMessage(), "Message not found");
		assertEquals(outcome1.getMessage(), "Oops, you did not say the magic word, you will stay on this page forever!");

		InternalLink internalLink1 = (InternalLink) outcome1;

		assertNotNull(internalLink1.getPage(), "Outcome destination page not found.");
		assertEquals("page1", internalLink1.getPage().getId(), "Wrong pageRef");
		assertEquals("This will take you to page1", outcome1.getDescription(), "Wrong description");
		assertEquals(2, outcome1.getFacts().size(), "Wrong number of facts in outcome");
		assertEquals("Fact 1", outcome1.getFacts().get(0).getDescription(), "Wrong fact in outcome");
	}

	@Test
	public void testProjectLinkSets() {
		List<LinkSet> linkSets = techspecContext.getAppspec().getLinkSets();
		assertEquals(2, linkSets.size(), "Wrong number of linkSets");
		assertEquals("linkSet1", linkSets.get(0).getId());
		assertEquals(1, linkSets.get(0).getFacts().size(), "Could not read linkset facts");
		assertEquals("Link Set 1", linkSets.get(0).getTitle());
		assertEquals(true, linkSets.get(1).isGlobal(), "linkSet2 is global");
	}

	@Test
	public void testProjectLinkSetsLinks() {
		List<LinkSet> linkSets = techspecContext.getAppspec().getLinkSets();
		List<Link> links = linkSets.get(0).getLinks();

		assertEquals(2, links.size());
		Link link = links.get(0);
		assertEquals("External Link 1 Title", link.getTitle());
		assertEquals("External Link 1 Description", link.getDescription());
		assertTrue(link instanceof ExternalLink, "First link is external link");
		ExternalLink externalLink = (ExternalLink) link;
		assertEquals("mailto:externalLink1", externalLink.getHref());

		Link link2 = links.get(1);
		assertTrue(link2 instanceof InternalLink, "Second link is internal link");
		InternalLink internalLink = (InternalLink) link2;
		assertEquals("page2", internalLink.getPageRef());
		assertNotNull(internalLink.getPage(), "Page references should be resolved for internal links");
		assertEquals("page2", internalLink.getPage().getId(), "Page references should be correctly resolved "
				+ "to correct pages for internal links");
	}
}