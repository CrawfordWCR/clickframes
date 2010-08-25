package org.clickframes.model;

import java.util.ArrayList;
import java.util.List;

import org.clickframes.util.ClickframeUtils;
import org.clickframes.xmlbindings.FactType;
import org.clickframes.xmlbindings.FactsType;

/**
 * Represents a testable fact in the appspec
 *
 * @author Vineet Manohar
 */
public class Fact {
    private String id;
    private String description;

    public String getId() {
        if (this.id == null) {
            this.id = ClickframeUtils.toCompactId(description);
        }

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static List<Fact> createList(FactsType factsType) {
        List<Fact> retVal = new ArrayList<Fact>();
        if (factsType != null) {
            for (FactType factType : factsType.getFact()) {
                Fact fact = create(factType);
                retVal.add(fact);
            }
        }
        return retVal;
    }

    public static Fact create(FactType factType) {
        Fact fact = new Fact();
        fact.setId(factType.getId());
        fact.setDescription(ClickframeUtils.normalize(factType.getValue()));
        return fact;
    }

    @Override
    public String toString() {
        return getId();
    }
}
