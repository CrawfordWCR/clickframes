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

import org.clickframes.AppspecReader;
import org.clickframes.xmlbindings.ActionsType;
import org.clickframes.xmlbindings.OutcomeType;

/**
 * Represents an action on the page. Actions are generally user triggered
 * requests and usually map to html submit buttons.
 *
 * @author Vineet Manohar
 */
public class Action extends AbstractElement{
	public Action(String title, String id, AppspecElement parent) {
		super(id, title, parent);
	}
    private String htmlId;
    private List<Link> outcomes = new ArrayList<Link>();

    /**
     * action semantics, "create", "read", "update", "delete"
     */
    private ActionType type;

    /**
     * @return if this action is a login action, return the successful outcome
     *         or throw an exception if it isn't
     * @throws AppspecConstraintViolationException
     */
    public Link getLoginSuccessfulOutcome() throws AppspecConstraintViolationException {
        if (!isLoginAction()) {
            throw new RuntimeException(
                    "login successful outcome is valid only for login actions, first call isLoginAction before calling this method");
        }
        for (Link outcome : outcomes) {
            if (outcome.isLoginSuccessfulOutcome()) {
                return outcome;
            }
        }

        throw new AppspecConstraintViolationException("A login action must have a successful outcome: id=" + getId()
                + ", title=" + getTitle());
    }

    /**
     * @return if this action is a login action, return the failure outcome or
     *         throw an exception if it isn't
     * @throws AppspecConstraintViolationException
     */
    public Link getLoginFailedOutcome() throws AppspecConstraintViolationException {
        if (!isLoginAction()) {
            throw new RuntimeException(
                    "login failed outcome is valid only for login actions, first call isLoginAction before calling this method");
        }
        for (Link outcome : outcomes) {
            if (outcome.isLoginFailedOutcome()) {
                return outcome;
            }
        }

        throw new AppspecConstraintViolationException("A login action must have a failed outcome: id=" + getId()
                + ", title=" + getTitle());
    }

    public List<Link> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<Link> outcomes) {
        this.outcomes = outcomes;
    }


    public Link getOutcome(String outcomeId) throws OutcomeNotFoundException {
        for (Link outcome : getOutcomes()) {
            if (outcome.getId().equals(outcomeId)) {
                return outcome;
            }
        }

        throw new OutcomeNotFoundException(outcomeId);
    }

    /**
     * Returns the first successful outcome. If no successful outcomes exist,
     * returns the first negative outcome.
     *
     * @return
     */
    public Link getDefaultOutcome() {
        for (Link outcome : getOutcomes()) {
            if (!outcome.isNegative()) {
                return outcome;
            }
        }
        return getOutcomes().get(0);
    }

    public boolean isLoginAction() {
        return getType().equals(ActionType.LOGIN);
    }

    public String getHtmlId() {
        return htmlId;
    }

    public void setHtmlId(String htmlId) {
        this.htmlId = htmlId;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public List<Email> getEmails() {
        List<Email> emails = new ArrayList<Email>();
        for (Link outcome : getOutcomes()) {
            emails.addAll(outcome.getEmails());
        }
        return emails;
    }

    static Action create(Appspec appspec, org.clickframes.xmlbindings.ActionType actionType, AppspecElement parent) {
        Action action = new Action(actionType.getTitle(), actionType.getId(), parent);
        if (actionType.getOutcomes() != null) {
            for (OutcomeType outcomeType : actionType.getOutcomes().getOutcome()) {
                Link outcome = AppspecReader.toOutcome(appspec, outcomeType, parent);
                action.getOutcomes().add(outcome);
            }
        }

        // CRUD semantics
        action.setType(ActionType.fromValue(actionType.getType()));

        return action;
    }

    public static List<Action> createList(Appspec appspec, ActionsType actions, AppspecElement parent) {
        List<Action> list = new ArrayList<Action>();

        if (actions != null) {
            for (org.clickframes.xmlbindings.ActionType actionType : actions.getAction()) {
                Action action = Action.create(appspec, actionType, parent);
                list.add(action);
            }
        }

        return list;
    }

	@Override
	public String getMetaName() {
		return "action";
	}
}