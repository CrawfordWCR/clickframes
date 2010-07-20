package org.clickframes.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.clickframes.xmlbindings.OutputType;
import org.clickframes.xmlbindings.OutputsType;

/**
 * An entity reference on a page, usually for the purpose of display
 *
 * @author Vineet Manohar
 */
public class Output {
    private String id;
    private String title;
    private Entity entity;
    private String description;
    private List<Fact> facts = new ArrayList<Fact>();

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

    public static List<Output> createList(Appspec appspec, OutputsType outputsType) {
        List<Output> outputs = new ArrayList<Output>();

        if (outputsType != null) {
            for (OutputType entityRefType : outputsType.getOutput()) {
                Output output = Output.create(appspec, entityRefType);
                outputs.add(output);
            }
        }

        return outputs;
    }

    public static Output create(Appspec appspec, OutputType outputType) throws EntityNotFoundException {
        Output output = new Output();
        output.setId(outputType.getId());
        output.setTitle(outputType.getTitle());
        output.setDescription(outputType.getDescription());
        output.setEntity(appspec.getEntity(outputType.getEntity()));
        output.getFacts().addAll(Fact.createList(outputType.getFacts()));
        return output;
    }
}