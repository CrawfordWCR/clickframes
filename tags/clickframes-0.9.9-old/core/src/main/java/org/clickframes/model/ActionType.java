package org.clickframes.model;

import org.apache.commons.lang.StringUtils;
import org.clickframes.xmlbindings.ActionTypeEnum;

public enum ActionType {
    CREATE("create"), READ("read"), UPDATE("update"), CREATE_OR_UPDATE("createOrUpdate"), DELETE("delete"), LOGIN(
            "login"), LOGOUT("logout"), OTHER("other");

    private String value;

    ActionType(String value) {
        this.value = value;
    }

    public static ActionType fromValue(ActionTypeEnum type) {
        return fromValue(type != null ? type.value() : null);
    }

    public static ActionType fromValue(String value) {
        if (value != null) {
            for (ActionType color : values()) {
                if (color.value.equals(value)) {
                    return color;
                }
            }
        }

        return getDefault();
    }

    public String getName() {
        return StringUtils.capitalize(value);
    }

    public String toValue() {
        return value;
    }

    public static ActionType getDefault() {
        return OTHER;
    }
}