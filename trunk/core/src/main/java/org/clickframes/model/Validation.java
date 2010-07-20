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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.xmlbindings.ValidationType;
import org.clickframes.xmlbindings.ValidationsType;

/**
 * @author Vineet Manohar
 */
public class Validation extends AbstractElement{
    private final Log logger = LogFactory.getLog(getClass());
    private String type;
    private String typeArgs;
    protected String description;
    private String messageHtmlId;
    private String negativeExample;

    public Validation(String id, String type, String typeArgs, String description, AppspecElement parent) {
    	super(id, type, parent);
        setType(type);
        setTypeArgs(typeArgs);
        setDescription(description);
    }


    /**
     * @param type
     *            e.g. length(min=2,max=8)
     * @param description
     * @return
     *
     * @author Vineet Manohar
     * @param string
     */
    private static Validation create(String id, String typeAttribute, String description, AppspecElement parent) {
        Pattern typePattern = Pattern.compile("^([^\\(]+)(\\((.*)\\))?$");
        Matcher m = typePattern.matcher(typeAttribute);

        if (!m.matches()) {
            throw new RuntimeException("Bad format for validation type: " + typeAttribute);
        }

        String type = m.group(1);
        String typeArgs = m.group(3);

        if (type.equals("required")) {
            return new RequiredValidation(id, type, typeArgs, description, parent);
        }
        if (type.equals("length")) {
            return new LengthValidation(id, type, typeArgs, description, parent);
        }
        if (type.equals("regex")) {
            return new RegexValidation(id, type, typeArgs, description, parent);
        }
        if (type.equals("matchesInput")) {
            return new MatchesInputValidation(id, type, typeArgs, description, parent);
        }

        return new CustomValidation(id, type, typeArgs, description, parent);
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return validation message, cleaned up to not throw errors in a
     *         JavaScript String declaration.
     */
    public String getJavaScriptValidationMessage() {
        if (getDescription() == null) {
            logger.error("Your description is NULL.  Your code monkey has failed you.  Beat him!");
            return null;
        }
        return getDescription().replace("'", "\\'");
    }

    public String getAuthoredOrGeneratedDescription() {
        if (description != null) {
            return description;
        }
        return getDefaultDescription();
    }

    public String getDefaultDescription() {
        return "Invalid input";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeArgs() {
        return typeArgs;
    }

    public void setTypeArgs(String typeArgs) {
        this.typeArgs = typeArgs;
    }

    public Map<String, String> getArgsAsMap() {
        Map<String, String> args = new HashMap<String, String>();

        if (typeArgs != null) {
            String[] nvps = typeArgs.split(",");
            for (String nvp : nvps) {
                String[] nv = nvp.split("=", 2);
                String name = null;
                String value = null;
                if (nv.length > 0) {
                    name = nv[0];
                }
                if (nv.length > 1) {
                    value = nv[1];
                }

                if (name != null) {
                    args.put(name, value);
                }
            }
        }

        return args;
    }

    public String getArgAsString(String name) {
        return getArgsAsMap().get(name);
    }

    public boolean hasArg(String name) {
        return getArgsAsMap().containsKey(name);
    }

    /**
     * @param name
     * @return null if argument with this name not present
     *
     * @author Vineet Manohar
     */
    public Integer getArgAsInteger(String name) {
        String arg = getArgsAsMap().get(name);
        if (arg == null) {
            return null;
        }
        return Integer.parseInt(arg);
    }

    /**
     * This field tell you where in the HTML the error message is expected to
     * appear when this validation fails. This is used by test plugins.
     *
     * @return the id of the message (div or span element) in the rendered HTML
     *
     * @author Vineet Manohar
     */
    public String getMessageHtmlId() {
        return messageHtmlId;
    }

    public void setMessageHtmlId(String messageHtmlId) {
        this.messageHtmlId = messageHtmlId;
    }

    public static List<Validation> createList(ValidationsType validationsType, AppspecElement parent) {
        List<Validation> retVal = new ArrayList<Validation>();

        if (validationsType != null) {
            for (ValidationType validationType : validationsType.getValidation()) {
                retVal.add(create(validationType, parent));
            }
        }

        return retVal;
    }

    private static Validation create(ValidationType validationType, AppspecElement parent) {
        String typeAttribute = validationType.getType();

        String id = validationType.getId();
        String description = validationType.getDescription();
        Validation validation = create(id, typeAttribute, description, parent);

        if (validationType.getNegativeExample() != null) {
            validation.setNegativeExample(validationType.getNegativeExample().getValue());
        }
        return validation;
    }

    public String getNegativeExample() {
        return negativeExample;
    }

    public void setNegativeExample(String negativeExample) {
        this.negativeExample = negativeExample;
    }
    
	@Override
	public String getMetaName() {
		return "validation";
	}
}