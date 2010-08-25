package org.clickframes.techspec;

import java.util.ArrayList;
import java.util.List;

import org.clickframes.xmlbindings.techspec.PropertiesType;
import org.clickframes.xmlbindings.techspec.PropertyType;

/**
 * Represents a property in techspec
 *
 * @author Vineet Manohar
 */
public class Property {
    private String name;
    private String value;
    private String condition;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Property> createList(PropertiesType properties) {
        List<Property> list = new ArrayList<Property>();

        if (properties != null) {
            for (PropertyType propertyType : properties.getProperty()) {
                list.add(create(propertyType));
            }
        }

        return list;
    }

    private static Property create(PropertyType propertyType) {
        Property property = new Property();

        property.setCondition(propertyType.getCondition());
        property.setName(propertyType.getName());
        property.setValue(propertyType.getValue());

        return property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}