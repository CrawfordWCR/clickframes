package org.clickframes.engine.swf;

import static org.clickframes.xmlbindings.InputTypeEnum.CHECKBOX;
import static org.clickframes.xmlbindings.InputTypeEnum.DATE;
import static org.clickframes.xmlbindings.InputTypeEnum.DROPDOWN;
import static org.clickframes.xmlbindings.InputTypeEnum.PASSWORD;
import static org.clickframes.xmlbindings.InputTypeEnum.RADIO;
import static org.clickframes.xmlbindings.InputTypeEnum.TEXT;
import static org.clickframes.xmlbindings.InputTypeEnum.TEXTAREA;
import static org.clickframes.xmlbindings.InputTypeEnum.UPLOAD;

import org.apache.commons.lang.StringUtils;
import org.clickframes.model.BeanMetaInfo;
import org.clickframes.model.Field;
import org.clickframes.model.Form;
import org.clickframes.model.Method;
import org.clickframes.model.Page;
import org.clickframes.model.PageParameter;
import org.clickframes.model.SingleUserInput;
import org.clickframes.xmlbindings.InputTypeEnum;
/**
 * TODO:  Comment me!
 * @author sboscarine
 *
 */
public class SWFEntityBeanFactory {
	private static final String UPLOAD_OBJ = "UploadedFile";
	private static final String DATE_OBJ = "Date";

	public static BeanMetaInfo createSWFEntityBean(Page page, Form form) {
		BeanMetaInfo bean = new BeanMetaInfo(StringUtils.capitalize(form.getId()));
		//create a field in bean for each form field.
		for (SingleUserInput input : form.getInputs()) {
			Field field = createField(input);
			if(field.getType().equals(DATE_OBJ)){
				bean.getImports().add("java.util." + DATE_OBJ);
			}else if(field.getType().equals(UPLOAD_OBJ)){
				bean.getImports().add("org.apache.myfaces.custom.fileupload." + UPLOAD_OBJ);
			}
			bean.getFields().add(field);
			bean.getGetters().add(Method.createGetter(field, form));
			bean.getSetters().add(Method.createSetter(field, form));
		}
		//create a field in bean for each parameter from URL.
		for (PageParameter input : page.getParameters()) {
			Field field = new Field("String", input.getId());
			bean.getFields().add(field);
			bean.getGetters().add(Method.createGetter(field, form));
			bean.getSetters().add(Method.createSetter(field, form));
		}		return bean;
	}

	private static final Field createField(SingleUserInput input) {
		InputTypeEnum type = InputTypeEnum.valueOf(input.getType().toUpperCase());
		if (type == TEXT || type == TEXTAREA || type == PASSWORD || type == RADIO || type == DROPDOWN) {
			return new Field("String", input.getId());
		}else if(type == CHECKBOX){
			return new Field("boolean", input.getId());
		}else if(type == UPLOAD){
			return new Field(UPLOAD_OBJ, input.getId());
		}else if(type == DATE){
			return new Field(DATE_OBJ, input.getId());
//		}else if(type == InputTypeEnum.MULTIPLE){
		}
		else{
			// throw new RuntimeException("The SWF plugin doesn't know what to do with " + type + ".  This is a developer error.");
		    return new Field("String", input.getId());
		}
	}
}
