package org.clickframes.model;

import java.util.ArrayList;
import java.util.List;

import org.clickframes.xmlbindings.FactsType;

public abstract class AbstractElementWithFacts extends AbstractElement {
    private List<Fact> facts = new ArrayList<Fact>();
    
    public List<Fact> getFacts() {
        return facts;
    }

    protected void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    protected void setFacts(FactsType facts) {
        getFacts().addAll(Fact.createList(facts));
    }

    protected AbstractElementWithFacts(String id, AppspecElement parent) {
        super(id, parent);
    }

    protected AbstractElementWithFacts(String id, String title, AppspecElement parent) {
        super(id, title, parent);
    }
}