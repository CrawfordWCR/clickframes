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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.clickframes.xmlbindings.FormType;
import org.clickframes.xmlbindings.InputTypeEnum;

/**
 * represents a form - grouping of data in a view page
 * 
 * @author Vineet Manohar
 */
public class Form extends AbstractElement {
    private boolean defaultForm;
    private List<SingleUserInput> inputs = new ArrayList<SingleUserInput>();
    private List<Action> actions = new ArrayList<Action>();

    public List<Action> getUpdateActions() {
        return getActionsOfType(ActionType.UPDATE);
    }

    /**
     * The page that owns this form. This is used by SWF Validator generator. To
     * generate support code from an entity.
     */
    private Page page;

    public Form(String id, AppspecElement parent) {
        super(id, parent);
        
        if (parent instanceof Page) {
            this.page = (Page) parent;
        }
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Action getAction(String actionId) throws ActionNotFoundException {
        for (Action action : getActions()) {
            if (action.getId().equals(actionId)) {
                return action;
            }
        }

        throw new ActionNotFoundException(actionId);
    }

    public List<SingleUserInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<SingleUserInput> inputs) {
        this.inputs = inputs;
    }

    /**
     * get all the entities referenced from this form
     * 
     * @return a list of all entities, or an empty list of no entities were
     *         referenced
     * 
     * @author Jon
     */
    public List<Entity> getEntities() {
        Set<Entity> entities = new HashSet<Entity>();
        for (SingleUserInput input : getInputs()) {
            if (input.getEntityProperty() != null) {
                entities.add(input.getEntityProperty().getEntity());
            }
        }
        return new ArrayList<Entity>(entities);
    }

    /**
     * Returns a mappping of entities bound to this form to a list of inputs
     * bound to the entity
     * 
     * This method is useful when you are mapping the form back to a list of
     * constructed entities. It answers the question: which entities does this
     * form populate, and which fields in the populated entities are filled
     * 
     * @return A map; where key is an entity bound to this form; and value is
     *         the list of all inputs bound to that entity
     * 
     * @author Vineet Manohar
     */
    public Map<Entity, List<SingleUserInput>> getEntityInputs() {
        Map<Entity, List<SingleUserInput>> map = new HashMap<Entity, List<SingleUserInput>>();
        for (Entity entity : getEntities()) {
            List<SingleUserInput> inputs = new ArrayList<SingleUserInput>();

            for (SingleUserInput input : getInputs()) {
                if (input.getEntityProperty() != null && input.getEntityProperty().getEntity().equals(entity)) {
                    inputs.add(input);
                }
            }

            map.put(entity, inputs);
        }

        return map;
    }

    /**
     * 
     * @param inputId
     * @return an input with that id
     * 
     * @author Vineet Manohar
     * @throws InputNotFoundException
     */
    public SingleUserInput getInput(String inputId) throws InputNotFoundException {
        for (SingleUserInput input : getInputs()) {
            if (input.getId().equals(inputId)) {
                return input;
            }
        }

        throw new InputNotFoundException(inputId);
    }

    @SuppressWarnings("unchecked")
    public <E extends SingleUserInput> List<E> getInputsByType(InputTypeEnum type) {
        List<E> inputs = new ArrayList<E>();
        for (SingleUserInput i : this.getInputs()) {
            if (i.getType().equals(type.value())) {
                inputs.add((E) i);
            }
        }
        return inputs;
    }

    public List<SingleUserInput> getTextInputs() {
        return getInputsByType(InputTypeEnum.TEXT);
    }

    public List<SingleUserInput> getTextareaInputs() {
        return getInputsByType(InputTypeEnum.TEXTAREA);
    }

    public List<DropdownInput> getDropdownInputs() {
        return getInputsByType(InputTypeEnum.DROPDOWN);
    }

    public List<DropdownInput> getMultiDropdownInputs() {
        return getInputsByType(InputTypeEnum.MULTIPLE);
    }

    public List<SingleUserInput> getDropdownOrRadioInputs() {
        List<SingleUserInput> retVal = new ArrayList<SingleUserInput>();
        retVal.addAll(getMultiDropdownInputs());
        retVal.addAll(getDropdownInputs());
        retVal.addAll(getRadioInputs());
        return retVal;
    }

    public List<SingleUserInput> getTextOrPasswordInputs() {
        List<SingleUserInput> retVal = new ArrayList<SingleUserInput>();
        retVal.addAll(getInputsByType(InputTypeEnum.TEXT));
        retVal.addAll(getInputsByType(InputTypeEnum.TEXTAREA));
        retVal.addAll(getInputsByType(InputTypeEnum.PASSWORD));
        return retVal;
    }

    public List<FileInput> getFileInputs() {
        return getInputsByType(InputTypeEnum.UPLOAD);
    }

    public List<CheckboxInput> getCheckboxInputs() {
        return getInputsByType(InputTypeEnum.CHECKBOX);
    }

    public List<RadioInput> getRadioInputs() {
        return getInputsByType(InputTypeEnum.RADIO);
    }

    public List<PasswordInput> getPasswordInputs() {
        return getInputsByType(InputTypeEnum.PASSWORD);
    }

    /**
     * Is this form a login form, a form that the user can fill out to login to
     * the website
     * 
     * @return true if there is a username and password field, and a submit
     *         action
     * 
     * @author Vineet Manohar
     */
    public boolean isLoginForm() {
        { // check username
            boolean usernameFound = false;
            for (SingleUserInput input : getInputs()) {
                if (input.isLoginUsername()) {
                    usernameFound = true;
                    break;
                }
            }

            if (!usernameFound) {
                return false;
            }
        }

        {
            // check password
            boolean passwordFound = false;
            for (SingleUserInput input : getInputs()) {
                if (input.isLoginPassword()) {
                    passwordFound = true;
                    break;
                }
            }

            if (!passwordFound) {
                return false;
            }
        }

        {
            // check login action
            boolean loginActionFound = false;
            for (Action action : actions) {
                if (action.isLoginAction()) {
                    loginActionFound = true;
                    break;
                }
            }

            return loginActionFound;
        }
    }

    /**
     * @return If this form is a login form, return the login action
     * 
     * @throws AppspecConstraintViolationException
     *             throw an exception if it isn't
     */
    public Action getLoginAction() throws AppspecConstraintViolationException {
        if (!isLoginForm()) {
            throw new RuntimeException(
                    "Login action is valid only for login forms. First call isLoginForm before calling this method.");
        }
        for (Action action : actions) {
            if (action.isLoginAction()) {
                return action;
            }
        }

        throw new AppspecConstraintViolationException("Login form (id = " + getId() + ") must have a login action.");
    }

    /**
     * @return if this page is a login page, return the login username input
     * 
     * @throws AppspecConstraintViolationException
     *             throw an exception if it isn't
     */
    public SingleUserInput getLoginUsernameInput() throws AppspecConstraintViolationException {
        if (!isLoginForm()) {
            throw new RuntimeException(
                    "Login username input is valid only for login forms. First call isLoginForm before calling this method.");
        }
        for (SingleUserInput input : getInputs()) {
            if (input.isLoginUsername()) {
                return input;
            }
        }

        throw new AppspecConstraintViolationException("Login form (id = " + getId()
                + ") must have a login username input.");

    }

    /**
     * @return if this page is a login page, return the login password input
     * 
     * @throws AppspecConstraintViolationException
     *             throw an exception if it isn't
     */
    public SingleUserInput getLoginPasswordInput() throws AppspecConstraintViolationException {
        if (!isLoginForm()) {
            throw new RuntimeException(
                    "Login password input is valid only for login forms. First call isLoginForm before calling this method.");
        }
        for (SingleUserInput input : getInputs()) {
            if (input.isLoginPassword()) {
                return input;
            }
        }

        throw new AppspecConstraintViolationException("Login form (id = " + getId()
                + ") must have a login password input.");
    }

    public static Form create(Appspec appspec, FormType formType, Page parent) throws EntityNotFoundException,
            EntityPropertyNotFoundException {
        Form form = new Form(formType.getId(), parent);

        form.setTitle(formType.getTitle());
        form.getInputs().addAll(SingleUserInput.createAll(appspec, formType.getInputs(), form));

        if (formType.getActions() != null) {
            for (org.clickframes.xmlbindings.ActionType actionType : formType.getActions().getAction()) {
                Action action = Action.create(appspec, actionType, form);
                form.getActions().add(action);
            }
        }

        return form;
    }

    public List<Action> getActionsOfType(ActionType... typeArray) {
        List<ActionType> types = Arrays.asList(typeArray);
        List<Action> retVal = new ArrayList<Action>();
        for (Action action : this.actions) {
            if (action.getType() != null && types.contains(action.getType())) {
                retVal.add(action);
            }
        }

        return retVal;
    }

    /**
     * get the input bound to this entity property, or null
     * 
     * @param entityProperty
     * @return
     * 
     * @author Vineet Manohar
     */
    public SingleUserInput getInputFor(EntityProperty entityProperty) {
        for (SingleUserInput input : inputs) {
            if (input.getEntityProperty() != null) {
                if (input.getEntityProperty().equals(entityProperty)) {
                    return input;
                }
            }
        }

        return null;
    }

    public final Page getPage() {
        return page;
    }

    public final void setPage(Page page) {
        this.page = page;
    }

    /**
     * @return a mapping of entities bound to this form which "update" them.
     *         This method is calculated by browsing all form actions and
     *         looking at the action type="update" page. Only those entities
     *         which are updated are returned.
     * 
     * @author Vineet Manohar
     */
    public Map<Entity, Form> getEntitiesUpdated() {
        Map<Entity, Form> map = new HashMap<Entity, Form>();

        List<Entity> inputEntities = getEntities();

        // if there are any "update" actions
        if (getActionsOfType(ActionType.UPDATE, ActionType.CREATE_OR_UPDATE).size() > 0) {
            for (Entity inputEntity : inputEntities) {
                map.put(inputEntity, this);
            }
        }

        return map;
    }

    /**
     * @return true if any action on this form is of type "update"
     * 
     * @author Vineet Manohar
     */
    public boolean isUpdateForm() {
        return getActionsOfType(ActionType.UPDATE).size() > 0
                || getActionsOfType(ActionType.CREATE_OR_UPDATE).size() > 0;
    }

    public boolean isDefaultForm() {
        return defaultForm;
    }

    public void setDefaultForm(boolean defaultForm) {
        this.defaultForm = defaultForm;
    }
    

	@Override
	public String getMetaName() {
		return "form";
	}
}