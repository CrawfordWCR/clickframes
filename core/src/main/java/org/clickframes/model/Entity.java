package org.clickframes.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.clickframes.xmlbindings.EntitiesType;
import org.clickframes.xmlbindings.EntityPropertyType;
import org.clickframes.xmlbindings.EntityType;

/**
 * Represents a user fusion of interaction object and a persistent object - a
 * domain object perhaps
 *
 *
 * @author Vineet Manohar
 */
public class Entity extends AbstractElementWithFacts implements Comparable<Entity> {
    private final Appspec appspec;

    public Appspec getAppspec() {
        return appspec;
    }

    private Entity(String id, String title, Appspec appspec) {
        super(id, title, appspec);
        this.appspec = appspec;
    }

    private List<EntityProperty> properties = new ArrayList<EntityProperty>();
    /** Forms that refer to this entity. */
    private final Set<Form> referringForms = new LinkedHashSet<Form>();

    public List<EntityProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<EntityProperty> properties) {
        this.properties = properties;
    }
    
    /**
     * Returns only entity properties that are persistent and non-multiple.
     * 
     * @return A list of simple properties, or an empty list if none.
     * @author Jonathan Abbett
     */
    public List<EntityProperty> getSimpleProperties() {
    	List<EntityProperty> simple = new ArrayList<EntityProperty>();
    	for (EntityProperty p : getProperties()) {
    		if (p.isMultiple() || !p.isPersistent()) {
    			continue;
    		}
    		simple.add(p);
    	}
    	return simple;
    }

    /**
     * alias for getProperty(String)
     *
     * @param property
     * @return
     * @throws EntityPropertyNotFoundException
     *
     * @author Vineet Manohar
     */
    public EntityProperty get(String property) throws EntityPropertyNotFoundException {
        return getProperty(property);
    }

    public EntityProperty getFirstFileProperty() throws EntityPropertyNotFoundException {
        if (getFileProperties().size() > 0) {
            return getFileProperties().get(0);
        }

        return null;
    }

    public List<EntityProperty> getFileProperties() throws EntityPropertyNotFoundException {
        List<EntityProperty> list = new ArrayList<EntityProperty>();

        for (EntityProperty entityProperty : properties) {
            if (entityProperty.getType() == org.clickframes.model.EntityPropertyType.FILE) {
                list.add(entityProperty);
            }
        }

        return list;
    }

    public EntityProperty getProperty(String property) throws EntityPropertyNotFoundException {
        for (EntityProperty entityProperty : properties) {
            if (entityProperty.getId().equals(property)) {
                return entityProperty;
            }
        }

        throw new EntityPropertyNotFoundException(this, property);
    }

    /**
     * factory method
     *
     * @param entityType
     * @return
     *
     * @author Vineet Manohar
     */
    public static Entity create(Appspec appspec, EntityType entityType, AppspecElement parent) {
        Entity entity = new Entity(entityType.getId(), entityType.getTitle(), appspec);
        entity.setDescription(entityType.getDescription());
        if (entityType.getProperties() != null) {
            for (EntityPropertyType entityPropertyType : entityType.getProperties().getProperty()) {
                EntityProperty entityProperty = EntityProperty.create(entityPropertyType, parent);
                entity.getProperties().add(entityProperty);
            }
        }

        entity.setFacts(entityType.getFacts());

        return entity;
    }

    /**
     * get all entity list elements in the entire application which refer to
     * this entity
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public Collection<OutputList> getReferringOutputLists() {
        List<OutputList> list = new ArrayList<OutputList>();

        for (Page page : appspec.getPages()) {
            for (OutputList outputList : page.getOutputLists()) {
                if (outputList.getEntity().getId().equals(getId())) {
                    list.add(outputList);
                }
            }
        }
        return list;
    }

    /**
     * get all output elements in the entire application which refer to this
     * entity
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public Collection<Output> getReferringOutputs() {
        List<Output> list = new ArrayList<Output>();

        for (Page page : appspec.getPages()) {
            for (Output output : page.getOutputs()) {
                if (output.getEntity().getId().equals(getId())) {
                    list.add(output);
                }
            }
        }
        return list;
    }
    
    public Collection<OutputList> getReferringOutputListsUnique() {
        Map<String, OutputList> map = new TreeMap<String, OutputList>();
        for (OutputList outputList : this.getReferringOutputLists()) {
            map.put(outputList.getId(), outputList);
        }
        return map.values();
    }
    
    public Collection<Output> getReferringOutputsUnique() {
        Map<String, Output> map = new TreeMap<String, Output>();
        for (Output output : this.getReferringOutputs()) {
            map.put(output.getId(), output);
        }
        return map.values();
    }

    public final Collection<Form> getReferringForms() {
        return referringForms;
    }

    static List<Entity> createList(Appspec appspec, EntitiesType entitiesType, AppspecElement parent) {
        List<Entity> entities = new ArrayList<Entity>();
        if (entitiesType != null) {
            for (EntityType entityType : entitiesType.getEntity()) {
                Entity entity = create(appspec, entityType, parent);
                entities.add(entity);
            }
        }
        return entities;
    }

    /**
     * Get the entity property which is set as primary key. If multiple entity
     * properties are set as primary key, returns the first one.
     *
     * @return the entity property which is defined as primary key, or null if
     *         none is defined
     *
     * @author Vineet Manohar
     */
    public EntityProperty getPrimaryKey() {
        for (EntityProperty entityProperty : properties) {
            if (entityProperty.isPrimaryKey()) {
                return entityProperty;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Entity) {
            Entity other = (Entity) obj;

            return other.getId().equals(this.getId());
        }

        return false;
    }

    /**
     * @return true if this is the "User" object of the system
     *
     * @author Vineet Manohar
     */
    public boolean getLoginEntity() {
        return appspec.getLoginEntity() != null && appspec.getLoginEntity().equals(this);
    }

    /**
     * @return a set of all foreign entities linked from this entity
     *
     * @author Vineet Manohar
     */
    public Set<Entity> getForeignEntities() {
        Set<Entity> retVal = new LinkedHashSet<Entity>();

        for (EntityProperty property : properties) {
            if (property.getForeignEntity() != null) {
                retVal.add(property.getForeignEntity());
            }
        }

        return retVal;
    }

    /**
     * @return a set of all foreign entities linked from this entity
     * 
     * @author Vineet Manohar
     */
    public Map<Entity, List<EntityProperty>> getForeignEntityMap() {
        Map<Entity, List<EntityProperty>> retVal = new HashMap<Entity, List<EntityProperty>>();

        for (EntityProperty property : properties) {
            if (property.getForeignEntity() != null) {
                if (!retVal.containsKey(property.getForeignEntity())) {
                    retVal.put(property.getForeignEntity(), new ArrayList<EntityProperty>());
                }
                retVal.get(property.getForeignEntity()).add(property);
            }
        }

        return retVal;
    }
    

	@Override
	public String getMetaName() {
		return "entity";
	}
}