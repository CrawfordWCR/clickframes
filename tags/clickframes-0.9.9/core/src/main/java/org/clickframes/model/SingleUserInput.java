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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.DefaultType;
import org.clickframes.xmlbindings.EntityPropertyRefType;
import org.clickframes.xmlbindings.ExampleValueType;
import org.clickframes.xmlbindings.FactType;
import org.clickframes.xmlbindings.FactsType;
import org.clickframes.xmlbindings.InputType;
import org.clickframes.xmlbindings.InputTypeEnum;
import org.clickframes.xmlbindings.InputsType;
import org.clickframes.xmlbindings.OptionType;
import org.clickframes.xmlbindings.OptionsType;
import org.clickframes.xmlbindings.ValidationsType;
import org.mortbay.log.LogFactory;

public class SingleUserInput extends AbstractElement {
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(SingleUserInput.class);

    public static SingleUserInput create(Appspec appspec, InputType inputType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        switch (inputType.getType()) {
            case CHECKBOX:
                return new CheckboxInput(appspec, inputType, parent);
            case UPLOAD:
                return new FileInput(appspec, inputType, parent);
            case PASSWORD:
                return new PasswordInput(appspec, inputType, parent);
            case TEXT:
                return new TextInput(appspec, inputType, parent);
            case TEXTAREA:
                return new TextareaInput(appspec, inputType, parent);
            case DROPDOWN:
                return new DropdownInput(appspec, inputType, parent);
            case MULTIPLE:
                return new MultiDropdownInput(appspec, inputType, parent);
            case RADIO:
                return new RadioInput(appspec, inputType, parent);
            case DATE:
                return new DateInput(appspec, inputType, parent);
            default:
                throw new IllegalArgumentException("Input type not supported by core implementation: '"
                        + inputType.getType() + "', " + getTitle(inputType));
        }
    }

    public static List<SingleUserInput> createAll(Appspec appspec, InputsType inputsType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        List<SingleUserInput> suis = new ArrayList<SingleUserInput>();
        if (inputsType != null) {
            for (InputType inputType : inputsType.getInput()) {
                SingleUserInput sui = create(appspec, inputType, parent);

                FactsType factsType = getFacts(inputType);
                if (factsType != null) {
                    for (FactType factType : factsType.getFact()) {
                        sui.getFacts().add(Fact.create(factType));
                    }
                }
                suis.add(sui);
            }
        }
        return suis;
    }

    public static CheckboxInput createCheckboxInput(Appspec appspec, InputType inputType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        return validateInputType(new CheckboxInput(appspec, inputType, parent), inputType, InputTypeEnum.CHECKBOX);
    }

    public static DropdownInput createDropdownInput(Appspec appspec, InputType inputType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        return validateInputType(new DropdownInput(appspec, inputType, parent), inputType, InputTypeEnum.DROPDOWN);
    }

    public static FileInput createFileInput(Appspec appspec, InputType inputType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        return validateInputType(new FileInput(appspec, inputType, parent), inputType, InputTypeEnum.UPLOAD);
    }

    public static PasswordInput createPasswordInput(Appspec appspec, InputType inputType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        return validateInputType(new PasswordInput(appspec, inputType, parent), inputType, InputTypeEnum.PASSWORD);
    }

    public static TextareaInput createTextareaInput(Appspec appspec, InputType inputType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        return validateInputType(new TextareaInput(appspec, inputType, parent), inputType, InputTypeEnum.TEXTAREA);
    }

    public static TextInput createTextInput(Appspec appspec, InputType inputType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        return validateInputType(new TextInput(appspec, inputType, parent), inputType, InputTypeEnum.TEXT);
    }

    private static String getDescription(InputType inputType) {
        return inputType.getDescription();
    }

    /**
     * get the entityPropertyRef child of input type
     * 
     * @param inputType
     * @return
     * 
     * @author Vineet Manohar
     */
    private static EntityPropertyRefType getEntityPropertyRef(InputType inputType) {
        return inputType.getEntityPropertyRef();
    }

    private static FactsType getFacts(InputType inputType) {
        return inputType.getFacts();
    }

    private static ExampleValueType getPositiveExample(InputType inputType) {
        return inputType.getPositiveExample();
    }

    private static String getTitle(InputType inputType) {
        return inputType.getTitle();
    }

    private static ValidationsType getValidations(InputType inputType) {
        return inputType.getValidations();
    }

    private static <E extends SingleUserInput> E validateInputType(E output, InputType input, InputTypeEnum type) {
        validateInputType(input, type);
        return output;
    }

    private static void validateInputType(InputType input, InputTypeEnum type) {
        if (input.getType() == null || !input.getType().equals(type.name())) {
            throw new IllegalArgumentException("Invalid type: " + input.getType());
        }
    }

    protected static DefaultType getDefault(InputType inputType) {
        return inputType.getDefault();
    }

    protected static OptionsType getOptions(InputType inputType) {
        return inputType.getOptions();
    }

    private String description;

    private String type;

    private String htmlId;

    private EntityProperty entityProperty;

    private String positiveExample;

    private List<Validation> validations = new ArrayList<Validation>();

    /**
     * Valid for radio, dropdown, checkbox, etc. <Value submitted to server,
     * Label shown to user>
     */
    private Map<String, String> allowedValues = new LinkedHashMap<String, String>();

    private String defaultValue;

    private List<Fact> facts = new ArrayList<Fact>();

    protected SingleUserInput(Appspec appspec, InputTypeEnum typeEnum, InputType inputType, AppspecElement parent)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        super(inputType.getId(), getTitle(inputType), parent);
        // type
        setType(typeEnum.value());

        // id
        // setId(inputType.getId());

        // entity property
        setEntityProperty(EntityProperty.getEntityProperty(appspec, getEntityPropertyRef(inputType)));

        // title
        // attempt 1: from entity property
        if (getEntityProperty() != null) {
            setTitle(getEntityProperty().getTitle());
        }
        // attempt 2: from local title
        if (!StringUtils.isEmpty(getTitle(inputType))) {
            setTitle(getTitle(inputType));
        }
        // attempt 3: default title - from id
        if (StringUtils.isEmpty(getTitle())) {
            setTitle(getDefaultTitle());
        }

        // description
        // if (getEntityProperty() != null) {
        // setDescription(getEntityProperty().getDescription());
        // }
        setDescription(getDescription(inputType));

        // example value
        if (getPositiveExample(inputType) != null) {
            setPositiveExample(getPositiveExample(inputType).getValue());
        } else {
            // if positive example is not set try to set it from entityProperty
            if (entityProperty != null) {
                setPositiveExample(entityProperty.getPositiveExample());
            }
        }

        // index
        Map<String, Validation> validationMap = new LinkedHashMap<String, Validation>();

        // if inherit validations
        if (getEntityPropertyRef(inputType) != null && getEntityPropertyRef(inputType).isInheritValidations()) {
            // copy the list of entity property validations
            for (Validation validation : entityProperty.getValidations()) {
                validationMap.put(validation.getId(), validation);
            }
        }

        // now copy all the local validations, overriding existing ones with the
        // same id
        ValidationsType validationsType = getValidations(inputType);
        for (Validation validation : Validation.createList(validationsType, this)) {
            validationMap.put(validation.getId(), validation);
        }

        OptionsType optionsType = getOptions(inputType);

        // set options if they are specified
        if (optionsType != null) {
            for (OptionType opt : optionsType.getOption()) {
                if (opt.getOptionValue() != null && opt.getOptionValue().length() > 0) {
                    // they specified <option value="foo">fubar</option>
                    getAllowedValues().put(opt.getOptionValue(), opt.getValue());
                } else {
                    // they specified <option >fubar</option>
                    getAllowedValues().put(opt.getValue(), opt.getValue());

                }
            }
        } else {
            // if options are not specified, load them from the entity property
            if (entityProperty != null) {
                getAllowedValues().putAll(getEntityProperty().getAllowedValues());
            }
        }

        // Set the default option
        if (getDefault(inputType) != null && getAllowedValues().containsKey(getDefault(inputType).getValue())) {
            setDefaultValue(getDefault(inputType).getValue());
        } else if (entityProperty != null && entityProperty.getDefaultValue() != null) {
            setDefaultValue(entityProperty.getDefaultValue());
        } else if (optionsType != null && optionsType.getOption().size() > 0) {
            // if no default was set, use the first option specified
            OptionType first = optionsType.getOption().get(0);
            if (first.getOptionValue() != null && first.getOptionValue().length() > 0) {
                setDefaultValue(first.getOptionValue());
            } else {
                setDefaultValue(first.getValue());
            }
        }

        // finally convert it to a list
        validations.addAll(validationMap.values());
    }

    public Map<String, String> getAllowedValues() {
        return ClickframeUtils.clean(allowedValues);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public EntityProperty getEntityProperty() {
        return entityProperty;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public String getHtmlId() {
        return htmlId;
    }

    // public String getId() {
    // if (this.id == null) {
    // this.id = ClickframeUtils.toCompactId(getTitle());
    // }
    // return ClickframeUtils.clean(this.id);
    // }

    public String getJavaType() {
        return "String";
    }

    public String getMaxLengthHTMLString() {
        String out = "";
        // #if ($validation.type == "length")
        // @org.hibernate.validator.Length(#if ($validation.minDefined)
        // min=$validation.min,#end
        // #if ($validation.maxDefined) max=$validation.max, #end
        // message="$validation.authoredOrGeneratedDescription")
        // #end
        for (Validation validation : getValidations()) {
            if (validation instanceof LengthValidation) {
                LengthValidation vdtion = (LengthValidation) validation;

                int maxlen = vdtion.getMax();
                if (maxlen > 0) {
                    out += ("maxlength=\"" + maxlen + "\"");
                }
            }
        }
        return out;
    }

    public String getPositiveExample() {
        return positiveExample;
    }

    public List<Validation> getRegexValidations() {
        List<Validation> regexValidations = new ArrayList<Validation>();

        for (Validation validation : validations) {
            if (validation instanceof RegexValidation) {
                regexValidations.add(validation);
            }
        }

        return regexValidations;
    }

    public RequiredValidation getRequiredValidation() {
        for (Validation validation : validations) {
            if (validation instanceof RequiredValidation) {
                return (RequiredValidation) validation;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    /**
     * @return MatchesInputValidation if asscociated with this input, or null
     * 
     * @author Vineet Manohar
     */
    public MatchesInputValidation getMatchesInputValidation() {
        for (Validation validation : validations) {
            if (validation instanceof MatchesInputValidation) {
                return (MatchesInputValidation) validation;
            }
        }

        return null;
    }

    public List<Validation> getValidations() {
        return this.validations;
    }

    public boolean isLoginPassword() {
        return this.entityProperty != null && entityProperty.isLoginPassword();
    }

    public boolean isLoginUsername() {
        return this.entityProperty != null && entityProperty.isLoginUsername();
    }

    public boolean isRegexValidationsDefined() {
        return getRegexValidations().size() > 0;
    }

    public boolean isRequired() {
        return getRequiredValidation() != null;
    }

    public boolean isValidationDefined() {
        return validations.size() > 0;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDescription(String description) {
        this.description = ClickframeUtils.normalize(description);
    }

    public void setEntityProperty(EntityProperty entityProperty) {
        this.entityProperty = entityProperty;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public void setHtmlId(String htmlId) {
        this.htmlId = htmlId;
    }

    public void setPositiveExample(String positiveExample) {
        this.positiveExample = positiveExample;
    }

    public void setValidations(List<Validation> validations) {
        this.validations = validations;
    }

    private String getDefaultTitle() {
        return getId();
    }

    protected void setType(String type) {
        this.type = type;
    }

	@Override
	public String getMetaName() {
		return "input";
	}
}