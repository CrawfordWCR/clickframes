package org.clickframes.model;

import org.apache.commons.lang.StringUtils;

public enum EntityPropertyType {
    BOOLEAN("boolean"), DATE("date"), ENTITY("entity"), FILE("file"), FLOAT("float"), INT("int"), TEXT("text");

    private String value;

    EntityPropertyType(String value) {
        this.value = value;
    }

    public static EntityPropertyType fromValue(org.clickframes.xmlbindings.EntityPropertyTypeEnum type) {
        return fromValue(type != null ? type.value() : null);
    }

    public static EntityPropertyType fromValue(String other) {
        if (other != null) {
            for (EntityPropertyType type : values()) {
                if (type.value.equals(other)) {
                    return type;
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

    public static EntityPropertyType getDefault() {
        return TEXT;
    }
}