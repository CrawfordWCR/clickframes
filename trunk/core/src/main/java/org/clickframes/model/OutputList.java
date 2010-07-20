package org.clickframes.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.clickframes.xmlbindings.OutputListType;
import org.clickframes.xmlbindings.OutputListsType;

/**
 * @author Vineet Manohar
 */
public class OutputList {
    private String id;
    private String title;
    private String entityRef;
    private Entity entity;
    private String description;
    private List<Fact> facts = new ArrayList<Fact>();
    private List<Link> links = new ArrayList<Link>();
    private List<Action> actions = new ArrayList<Action>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * if the id is "fooBar, the name is FooBar"
     */
    public String getName() {
        return StringUtils.capitalize(id);
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public String getEntityRef() {
        return entityRef;
    }

    public void setEntityRef(String entityRef) {
        this.entityRef = entityRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public static OutputList create(Appspec appspec, OutputListType outputListType, AppspecElement parent) {
        OutputList outputList = new OutputList();

        outputList.setId(outputListType.getId());
        outputList.setTitle(outputListType.getTitle());
        outputList.setDescription(outputListType.getDescription());
        outputList.setEntityRef(outputListType.getEntityRef());
        outputList.getFacts().addAll(Fact.createList(outputListType.getFacts()));
        outputList.getLinks().addAll(Link.createList(appspec, outputListType.getLinks(), parent));
        outputList.getActions().addAll(Action.createList(appspec, outputListType.getActions(), parent));

        return outputList;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public static List<OutputList> createList(Appspec appspec, OutputListsType outputLists) {
        List<OutputList> retVal = new ArrayList<OutputList>();
        if (outputLists != null) {
            for (OutputListType outputListType : outputLists.getOutputList()) {
                OutputList outputList = OutputList.create(appspec, outputListType, appspec);
                retVal.add(outputList);
            }
        }

        return retVal;
    }
}