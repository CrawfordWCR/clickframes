package org.clickframes.jsf;

import org.apache.commons.lang.StringUtils;
import org.clickframes.model.Entity;
import org.clickframes.model.EntityProperty;
import org.clickframes.model.Field;
import org.clickframes.model.mvc.GeneralClass;
import org.clickframes.model.mvc.SimpleMethod;

/**
 * JSF Entity Bean Factory
 * @author sboscarine
 *
 */
public class EntityBeanFactory {
	public static GeneralClass createEntity(Entity entity) {
		GeneralClass out = new GeneralClass(entity.getName());
		for(EntityProperty property : entity.getProperties()){
			//handle fields and imports.
			String type = property.getType().toValue();
			String fieldType = type;	//boolean or int
			if(type==null){
				fieldType = "String";
			}else if (type.equalsIgnoreCase("string") || type.equalsIgnoreCase("date")){
				fieldType = StringUtils.capitalize(type);
				if(type.equalsIgnoreCase("date")){
					out.getImports().add("java.util.Date");
				}
			}else if(type.equalsIgnoreCase("integer")){
				fieldType = "int";
			}else if(type.equalsIgnoreCase("textarea")){
				fieldType = "String";
			}else if(type.equalsIgnoreCase("text")){	//new field
				fieldType = "String";
			}else if(type.equalsIgnoreCase("enum")){	//FIXME
				fieldType = "String";
			}

			Field field = new Field(fieldType, property.getId());
			field.setComment(property.getDescription());
			out.getFields().add(field);
		}
		//generate getters first and then setters in order fields were declared.

		for(Field field : out.getFields()){
			out.getMethods().add(SimpleMethod.createGetter(field));
		}

		for(Field field : out.getFields()){
			out.getMethods().add(SimpleMethod.createSetter(field));
		}
		return out;
	}
}
