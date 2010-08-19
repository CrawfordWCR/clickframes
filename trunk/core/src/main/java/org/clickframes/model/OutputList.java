package org.clickframes.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.clickframes.xmlbindings.OutputListType;
import org.clickframes.xmlbindings.OutputListsType;

/**
 * @author Vineet Manohar
 */
public class OutputList extends AbstractElement {
	private String entityRef;
    private Entity entity;
    private List<Fact> facts = new ArrayList<Fact>();
    private List<Link> links = new ArrayList<Link>();
    private List<Action> actions = new ArrayList<Action>();

    protected OutputList(String id, AppspecElement parent) {
		super(id, parent);
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
        OutputList outputList = new OutputList(outputListType.getId(), parent);

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

    public static List<OutputList> createList(Appspec appspec, OutputListsType outputLists, AppspecElement parent) {
        List<OutputList> retVal = new ArrayList<OutputList>();
        if (outputLists != null) {
            for (OutputListType outputListType : outputLists.getOutputList()) {
                OutputList outputList = OutputList.create(appspec, outputListType, parent);
                retVal.add(outputList);
            }
        }

        return retVal;
    }

	@Override
	public String getMetaName() {
		return "outputList";
	}
}