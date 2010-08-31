package org.clickframes.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.clickframes.xmlbindings.EntityPropertyRefType;
import org.clickframes.xmlbindings.OptionType;
import org.clickframes.xmlbindings.OptionsType;

/**
 * @author Vineet Manohar
 */
public class EntityProperty extends AbstractElementWithFacts {
    private Entity entity;
    private EntityPropertyType type;
    private String defaultValue;
    private String description;
    private String positiveExample;
    private List<Validation> validations = new ArrayList<Validation>();
    private boolean multiple;
    private boolean persistent;
    private boolean primaryKey;
    private Map<String, String> allowedValues = new LinkedHashMap<String, String>();
    private String foreignEntityId;
    private Entity foreignEntity;
    private boolean loginPassword;
    private boolean loginUsername;

    public EntityProperty(String id, String title, AppspecElement parent) {
        super(id, title, parent);
    }

    public List<Validation> getValidations() {
        return validations;
    }

    public void setValidations(List<Validation> validations) {
        this.validations = validations;
    }

    /**
     * @return the containing entity
     *
     * @author Vineet Manohar
     */
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * Return an existing entity property from the appspec
     *
     * @param appspec
     * @param entityPropertyRef
     * @return existing entity property from the appspec, if exists - otherwise
     *         null
     *
     * @author Vineet Manohar
     * @throws EntityNotFoundException
     */
    static EntityProperty getEntityProperty(Appspec appspec, EntityPropertyRefType entityPropertyRef)
            throws EntityNotFoundException, EntityPropertyNotFoundException {
        if (entityPropertyRef == null) {
            return null;
        }

        Entity entity = appspec.getEntity(entityPropertyRef.getEntity());
        EntityProperty ep = entity.getProperty(entityPropertyRef.getProperty());
        ep.setEntity(entity);
        return ep;
    }

    /**
     * factory method
     *
     * @param entityPropertyType
     * @param parent
     * @return
     */
    public static EntityProperty create(org.clickframes.xmlbindings.EntityPropertyType entityPropertyType,
            AppspecElement parent) {
        EntityProperty entityProperty = new EntityProperty(entityPropertyType.getId(), entityPropertyType.getTitle(), parent);

        entityProperty.setFacts(entityPropertyType.getFacts());

        // type
        entityProperty.setType(EntityPropertyType.fromValue(entityPropertyType.getType()));

        // foreign key
        if (entityProperty.getType() == EntityPropertyType.ENTITY) {
            String foreignEntityId = entityPropertyType.getEntityRef().getEntity();
            entityProperty.setForeignEntityId(foreignEntityId);
        }

        entityProperty.getValidations().addAll(Validation.createList(entityPropertyType.getValidations(), entityProperty));
        entityProperty.setPersistent(entityPropertyType.isPersistent());
        entityProperty.setMultiple(entityPropertyType.isMultiple());
        entityProperty.setPrimaryKey(entityPropertyType.isPrimaryKey());
        entityProperty.setDescription(entityPropertyType.getDescription());

        if (entityPropertyType.getPositiveExample() != null) {
            entityProperty.setPositiveExample(entityPropertyType.getPositiveExample().getValue());
        }

        // default value
        // Set the default option
        if (entityPropertyType.getDefault() != null) {
            entityProperty.setDefaultValue(entityPropertyType.getDefault().getValue());
        }

        // options
        OptionsType optionsType = entityPropertyType.getOptions();

        if (optionsType != null) {
            for (OptionType opt : optionsType.getOption()) {
                if (!StringUtils.isEmpty(opt.getOptionValue())) {
                    entityProperty.getAllowedValues().put(opt.getOptionValue(), opt.getValue());
                } else {
                    entityProperty.getAllowedValues().put(opt.getValue(), opt.getValue());

                }
            }
        }

        // login username, password property
        entityProperty.setLoginUsername(entityPropertyType.isLoginUsername());
        entityProperty.setLoginPassword(entityPropertyType.isLoginPassword());
        return entityProperty;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPositiveExample() {
        return positiveExample;
    }

    public void setPositiveExample(String positiveExample) {
        this.positiveExample = positiveExample;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Map<String, String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(Map<String, String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public EntityPropertyType getType() {
        return type;
    }

    public void setType(EntityPropertyType type) {
        this.type = type;
    }

    public Entity getForeignEntity() {
        return foreignEntity;
    }

    public void setForeignEntity(Entity foreignEntity) {
        this.foreignEntity = foreignEntity;
    }

    public String getForeignEntityId() {
        return foreignEntityId;
    }

    public void setForeignEntityId(String foreignEntityId) {
        this.foreignEntityId = foreignEntityId;
    }

    public boolean isLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(boolean loginUsername) {
        this.loginUsername = loginUsername;
    }

    public boolean isLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(boolean loginPassword) {
        this.loginPassword = loginPassword;
    }

	@Override
	public String getMetaName() {
		return "property";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof EntityProperty) {
			EntityProperty other = (EntityProperty) obj;

			return other.getEntity().equals(this.getEntity()) && other.getId().equals(this.getId());
		}

		return false;
	}
}