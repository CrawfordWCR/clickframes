package org.clickframes.plugins.seam;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.clickframes.model.Entity;
import org.clickframes.model.Form;
import org.clickframes.model.Output;
import org.clickframes.model.PageParameter;
import org.clickframes.model.SingleUserInput;

public class PageEntity {
    private Entity entity;
    private Output output;
    private PageParameter pageParameter;
    private String id;
    private Map<Form, List<SingleUserInput>> inputs;

    public static enum Type {
        OUTPUT, PARAM_PRIMARY_KEY, PARAM_OTHER, INPUT
    };

    private Type type;

    public static PageEntity create(Entity entity, String variableName) {
        PageEntity retVal = new PageEntity();
        retVal.setEntity(entity);
        retVal.setId(variableName);
        return retVal;
    }

    public static PageEntity create(Entity entity) {
        PageEntity retVal = new PageEntity();
        retVal.setEntity(entity);
        return retVal;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public PageParameter getPageParameter() {
        return pageParameter;
    }

    public void setPageParameter(PageParameter pageParameter) {
        this.pageParameter = pageParameter;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return StringUtils.capitalize(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<Form, List<SingleUserInput>> getInputs() {
        return inputs;
    }

    public void setInputs(Map<Form, List<SingleUserInput>> inputs) {
        this.inputs = inputs;
    }
}
