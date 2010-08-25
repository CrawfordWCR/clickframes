package org.clickframes.engine.swf;

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.clickframes.util.EntityUtil.getClassNameFromFullyQualifiedClassName;
import static org.clickframes.xmlbindings.InputTypeEnum.CHECKBOX;
import static org.clickframes.xmlbindings.InputTypeEnum.DROPDOWN;
import static org.clickframes.xmlbindings.InputTypeEnum.MULTIPLE;
import static org.clickframes.xmlbindings.InputTypeEnum.RADIO;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.jsf.DynamicInputFactory;
import org.clickframes.model.Action;
import org.clickframes.model.Field;
import org.clickframes.model.Form;
import org.clickframes.model.Method;
import org.clickframes.model.Page;
import org.clickframes.model.Service;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.mvc.GeneralClass;
import org.clickframes.model.mvc.SimpleMethod;
import org.clickframes.xmlbindings.InputTypeEnum;

/**
 * Reads appspec and creates bean representing Spring Web Flow service and flow. Handles logic for overrides as well
 * Logic became too complicated to live in constructor.
 *
 * @author sboscarine
 *
 */
public class SWFServiceFactory {
	private static final Log logger = LogFactory.getLog(SWFServiceFactory.class);

	/**
	 * Logic to create Service Tier code.
	 *
	 * @return bean with all values needed to create Service Layer class, validators, and flow files.
	 */
	public static final Service createSWFService(Page page, String defaultEntityPackage) {
		// set simple values
		String serviceClass = capitalize(page.getId()) + "Service";
		String validatorClass = capitalize(page.getId()) + "Validator";

		Service service = new Service(serviceClass, validatorClass);
		// form level logic
		for (Form form : page.getForms()) {
			if(form.getId() == null || form.getId().trim().length() == 0){
				logger.error("why is this id empty?");
				continue;
			}
			// Do I generate an entity bean?
			String overriddenEntityClass = null;
			// by default, entity class object matching form fields.
			String entityClass = capitalize(form.getId());
			// field object has variable name e.g. "MyClass myClass" or "Issue issue"
			Field entityAsFieldObject = new Field(entityClass);
			if (overriddenEntityClass != null) {
				// user wants to use custom class to bind to form, such as JPA or BDB bean.
				entityClass = getClassNameFromFullyQualifiedClassName(overriddenEntityClass);
				logger.debug("overriding to use " + overriddenEntityClass + "(" + entityClass + ")");
				service.getImports().add(overriddenEntityClass);
				entityAsFieldObject = new Field(entityClass, form.getId());
			} else {
				service.getImports().add(defaultEntityPackage + "." + entityClass);
			}
			service.getEntities().add(entityClass);
			// handle form level actions.
			for (Action action : form.getActions()) {
				// method to actually perform action.
				service.getServiceMethods().add(new Method(form, "void", action.getId(), entityAsFieldObject));
				// method to actually validate input (entity) to action.
				service.getValidatorMethods().add(
						new Method(form, "void", "validate" + capitalize(action.getId()), entityAsFieldObject));
				// method to initialize entity in Spring Web Flow.
				service.getFlowMethods().add(new Method(form, entityClass, "start" + entityClass + "Flow"));
			}
			// Add dynamic population methods for fields when appropriate (select, checkbox, radio, etc).
			// Gets rendered to JSF SelectItem
			for (SingleUserInput input : form.getInputs()) {
				// String needs to be converted to Enum. (This should be moved somewhere else)
				InputTypeEnum type = InputTypeEnum.valueOf(input.getType().toUpperCase());
				if (type == RADIO || type == DROPDOWN || type == CHECKBOX || type == MULTIPLE) {
					Method dynPopMethod = new Method(form, "List<SelectItem>", "get" + input.getName() + "Options");
					service.getDynamicDataMethods().add(dynPopMethod);
					service.getImports().add("java.util.ArrayList");
					service.getImports().add("java.util.List");
					service.getImports().add("javax.faces.model.SelectItem");
				}
			}
		}
		if (page.isEmailUsed()) {
			service.getImports().add("javax.annotation.Resource");
			service.getImports().add("java.util.Map");
			service.getImports().add("java.util.HashMap");
		}
		return service;
	}

	public static final GeneralClass createSWFServiceNew(Page page, String defaultEntityPackage) {
		String id = page.getId();
		String className = page.getName() + "Service";
		GeneralClass swf = new GeneralClass(className);
		boolean hasDynamicInput = false;

		// form level logic
		for (Form form : page.getForms()) {

			// by default, entity class object matching form fields.
			String entityClass = form.getName();
			swf.getImports().add(defaultEntityPackage + "." + entityClass);

			// method to initialize entity in Spring Web Flow.
			String initMethod = "start" + entityClass + "Flow";
			List<String> initLines = new ArrayList<String>(1);
			initLines.add("return new " + entityClass + "();");
			swf.getMethods().add(new SimpleMethod(entityClass, initMethod, initLines));

			for (Action action : form.getActions()) {
				String methodName = action.getId();
				List<String> lines = new ArrayList<String>(1);
				Field args = new Field(entityClass);
				lines.add("logger.debug(\"Edit " + className + "." + methodName
						+ " to have this action do something useful.\");");
				swf.getMethods().add(new SimpleMethod("void", methodName, lines, args));
			}

			for (SingleUserInput input : form.getInputsByType(InputTypeEnum.RADIO)) {
				swf.getMethods().add(createDynamicInputMethod(page, form, input));
				hasDynamicInput = true;
			}
			for (SingleUserInput input : form.getInputsByType(InputTypeEnum.DROPDOWN)) {
				swf.getMethods().add(createDynamicInputMethod(page, form, input));
				hasDynamicInput = true;
			}
			for (SingleUserInput input : form.getInputsByType(InputTypeEnum.CHECKBOX)) {
				swf.getMethods().add(createDynamicInputMethod(page, form, input));
				hasDynamicInput = true;
			}
		}


		if (page.isEmailUsed()) {
			 swf.getImports().add("javax.annotation.Resource");
			swf.getImports().add("java.util.Map");
			swf.getImports().add("java.util.HashMap");
			Field mailService = new Field("@Resource", "SpringEmailService", "mail");
			swf.getFields().add(mailService);

		}
		if (hasDynamicInput) {
			 swf.getImports().add("javax.annotation.Resource");
			swf.getImports().add("java.util.List");
			swf.getImports().add("javax.faces.model.SelectItem");
			Field formService = new Field("@Resource", "DynamicFormService", "formService");
			swf.getFields().add(formService);

		}
		return swf;
	}

	private static SimpleMethod createDynamicInputMethod(Page page, Form form, SingleUserInput input) {
		List<String> lines = new ArrayList<String>();
		String methodName = DynamicInputFactory.createOptionMethodName(page, form, input);
		lines.add("return formService." + methodName + "();");
		SimpleMethod method = new SimpleMethod("List<SelectItem>", "get" + input.getName() + "Options", lines);
		return method;
	}

}