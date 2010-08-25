package org.clickframes.objectfactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Entity;
import org.clickframes.model.Field;
import org.clickframes.model.Form;
import org.clickframes.model.Page;
import org.clickframes.model.RegexValidation;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.model.mvc.GeneralClass;
import org.clickframes.model.mvc.SimpleMethod;

public class SWFValidatorFactory {
	private static final Log logger = LogFactory.getLog(SWFValidatorFactory.class);

	public static final GeneralClass createValidator(Page page, Form form) {
		String entityClassName = form.getName();
		GeneralClass validator = new GeneralClass(entityClassName + "Validator");
		String viewState = page.getId();
		Field context = new Field("ValidationContext", "context");
		String fid = form.getId(); // form id.
		Field model = new Field(entityClassName, fid);
		// TODO: Figure out how to get entity package from here.
		// validator.getImports().add("${entityPackage}." + entityClassName);
		SimpleMethod method = new SimpleMethod("void", "validate" + StringUtils.capitalize(viewState), model, context);
		method.getLines().add("MessageContext messages = context.getMessageContext();");

		for (SingleUserInput input : form.getInputs()) {
			if (input.isRequired()) {
				String iid = input.getId(); // input id.
				// needs to look like:
				// validate(contactUs.getContactSubject(), messages, "contactUs:subject",
				// "contactUs_subject_requiredMessage");

				// not necessarily same as input id.
				String beanFieldName = input.getName();
				// if (input.getEntityProperty() != null) {
				// beanFieldName = input.getEntityProperty().getName();
				// }

				String methodCall = "validate(" + fid + ".get" + beanFieldName + "(), messages, \"" + fid + ":" + iid
						+ "\", \"" + fid + "_" + iid + "_requiredMessage\");";
				method.getLines().add(methodCall);
			}
		}
		validator.getMethods().add(method);
		return validator;
	}

	// FYI: This doesn't cover scenarios where properties come from both entity ref and form field in same form.
	public static final GeneralClass createValidatorFromEntity(Entity entity) {
		String entityClassName = entity.getName();
		GeneralClass validator = new GeneralClass(entityClassName + "Validator");
		for (Form form : entity.getReferringForms()) {
			Page page = form.getPage();
			String viewState = page.getId(); // SWF <view-state>
			Field context = new Field("ValidationContext", "context");
			String formId = form.getId(); // form id.
			Field model = new Field(entityClassName, formId);
			// TODO: Figure out how to get entity package from here.
			// validator.getImports().add("${entityPackage}." + entityClassName);
			SimpleMethod method = new SimpleMethod("void", "validate" + StringUtils.capitalize(viewState), model,
					context);
			method.getLines().add("MessageContext messages = context.getMessageContext();");

			for (SingleUserInput input : form.getInputs()) {
				String inputId = input.getId(); // input id.
				// when you have a form with some form fields bound to an entity and others unbound.
				if (input.getEntityProperty() == null) {
					logger
							.info("skipping "
									+ input.getName()
									+ " forms with both entities and regular fields are not yet supported in this plugin.  Does this field map to an entity property?");
					continue;
				}

				// Am I inconsistent?
				final String fieldName = input.getEntityProperty().getName();

				if (input.isRequired()) {

					// String fieldName = input.getName();
					// FIXME comment needed

					method.getLines().add(
							"validate(" + formId + ".get" + fieldName + "(), messages, \"" + formId + ":" + inputId
									+ "\", \"" + formId + "_" + inputId + "_requiredMessage\");");
				}
				for (Validation validation : input.getValidations()) {

					if (validation instanceof RegexValidation) {
						RegexValidation rvalidation = (RegexValidation) validation;
						String validationString = "\"" + rvalidation.getJavaSafeRegex() + "\"";
						method.getLines().add(
								"validateRegEx(" + validationString + ", " + formId + ".get" + fieldName
										+ "(), messages, \"" + formId + ":" + inputId + "\", \"" + formId + "_"
										+ inputId + "_" + rvalidation.getType() + "Message\");");
					}
				}
			}
			validator.getMethods().add(method);
		}
		return validator;
	}
}