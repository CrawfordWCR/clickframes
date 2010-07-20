package org.clickframes.techspec;

import java.util.ArrayList;
import java.util.List;

import org.clickframes.xmlbindings.techspec.IncludeType;

/**
 * Represents a plugin filter
 *
 * @author Vineet Manohar
 */
public class Filter {
    private FilterType type;
    private String condition;

    public FilterType getType() {
        return type;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return type + (condition != null ? ": " + condition : "");
    }

    /**
     * currently the input only has one include statement
     *
     * @param include
     * @return
     *
     * @author Vineet Manohar
     */
    public static List<Filter> createList(IncludeType include) {
        List<Filter> retVal = new ArrayList<Filter>();

        Filter filter = create(include);
        if (filter != null) {
            retVal.add(filter);
        }

        return retVal;
    }

    private static Filter create(IncludeType include) {
        if (include == null) {
            return null;
        }

        Filter filter = new Filter();
        filter.setCondition(include.getCondition());
        filter.setType(FilterType.INCLUDE);
        return filter;
    }
}