package org.clickframes.objectfactory;

import static org.apache.commons.lang.StringUtils.capitalize;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Action;
import org.clickframes.model.ExternalLink;
import org.clickframes.model.Field;
import org.clickframes.model.Form;
import org.clickframes.model.InternalLink;
import org.clickframes.model.Link;
import org.clickframes.model.Page;
import org.clickframes.model.PageParameter;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.mvc.GeneralClass;
import org.clickframes.model.mvc.SimpleMethod;

/**
 * Way of handling complex logic in Java instead of velocity. Java is chosen because it can be refactored automatically
 * and allows us to use simpler velocity templates, making it easier to format the source code in a readable manner.
 *
 * @author Steven Boscarine
 *
 */
public class MVCControllerFactory {
	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(MVCControllerFactory.class);

	public static final GeneralClass createMVCController(Page page) {
		String id = page.getId();
		String className = capitalize(id) + "Controller";
		GeneralClass mvc = new GeneralClass(className);
		mvc.getMethods().add(createPopulationMethod(page));
		if (page.isFormsDefined()) {
			for (Form form : page.getForms()) {
				mvc.getMethods().add(createFormMethod(page, form));
			}
		}
		mvc.getImports().add("org.springframework.ui.ModelMap");
		return mvc;
	}

	public static final GeneralClass createMVCService(Page page) {
		String id = page.getId();
		String className = capitalize(id) + "Service";
		GeneralClass mvc = new GeneralClass(className);
		if (page.isFormsDefined()) {
			for (Form form : page.getForms()) {
				if (form.getActions().size() == 1) {
					mvc.getMethods().add(handleMVCServiceMethod(page, form, form.getActions().get(0)));
				}
			}
		}
		if(page.isEmailUsed()){
			mvc.getImports().add("javax.annotation.Resource");
			mvc.getImports().add("java.util.Map");
			mvc.getImports().add("java.util.HashMap");
		}
		return mvc;
	}

	private static SimpleMethod handleMVCServiceMethod(Page page, Form form, Action action) {
		Field[] args = createServiceArgs(page, form);
		String methodName = "handle" + capitalize(form.getId());
		List<String> lines = new ArrayList<String>();
		lines.add("logger.debug(\"Edit " + capitalize(page.getId()) + "Service." + methodName
				+ " to have this action do something useful.\");");
		SimpleMethod method = new SimpleMethod("void", methodName, lines, args);

		return method;
	}

	/**
	 * Population methods are the GET methods for MVC that populate a page with dynamic data.
	 */
	private static SimpleMethod createPopulationMethod(Page page) {
		Field[] args = createControllerGETMethodArgs(page);
		String methodName = "populate" + capitalize(page.getId());
		List<String> lines = new ArrayList<String>();
		lines.add("logger.debug(\"Edit " + capitalize(page.getId()) + "Controller." + methodName
				+ " to populate this page with dynamic data.\");");
		SimpleMethod method = new SimpleMethod("void", methodName, lines, args);
		method.setAnnotation("@RequestMapping(value=\"/" + page.getId() + "\", method = RequestMethod.GET)");
		return method;
	}

	private static SimpleMethod createFormMethod(Page page, Form form) {
		if (form.getActions().size() == 1) {
			return handleSingleActionForm(page, form, form.getActions().get(0));
		}
		return handleMultiActionForm(page, form);
	}

	private static SimpleMethod handleSingleActionForm(Page page, Form form, Action action) {
		Field[] args = createControllerPOSTMethodArgs(page, form);
		String methodName = "handle" + capitalize(form.getId());
		List<String> lines = new ArrayList<String>();
		lines.add("logger.debug(\"Edit " + capitalize(page.getId()) + "Controller." + methodName
				+ " to have this action do something useful.\");");

		createServiceMethodCall(page, args, methodName, lines);

		Link defaultOutcome = action.getDefaultOutcome();
		String outcomeStr = "";
		if (defaultOutcome instanceof InternalLink) {
			InternalLink outcome = (InternalLink) defaultOutcome;
			outcomeStr = outcome.getPageRef();
		} else if (defaultOutcome instanceof ExternalLink) {
			ExternalLink outcome = (ExternalLink) defaultOutcome;
			// SB: CONfirm this is correct MVC syntax.
			// outcomeStr="redirect:" + outcome.getHref();
		}
		lines.add("return \"" + outcomeStr + "\";");
		SimpleMethod method = new SimpleMethod("String", methodName, lines, args);
		method.setAnnotation("@RequestMapping(value=\"/" + page.getId() + "/" + form.getId()
				+ "\", method = RequestMethod.POST)");

		return method;
	}

	private static void createServiceMethodCall(Page page, Field[] args, String methodName, List<String> lines) {
		String params = "";
		for (Field field : args) {
			if (field.getName() != null && !field.getName().equals("model"))
				params += ("," + field.getName());
		}
		if (params.length() > 0) {
			params = params.substring(1);
		}
		lines.add(page.getId() + "Service." + methodName + "(" + params + ");");
	}

	private static SimpleMethod handleMultiActionForm(Page page, Form form) {
		Field[] args = createControllerPOSTMethodArgs(page, form);
		String methodName = "handle" + capitalize(form.getId());
		List<String> lines = new ArrayList<String>();
		lines.add("logger.debug(\"Edit " + capitalize(page.getId()) + "Controller." + methodName
				+ " to have this action do something useful.\");");
		lines.add("//We apologize but multi-action forms are not yet fully supported in Spring MVC");
		SimpleMethod method = new SimpleMethod("void", methodName, lines, args);
		method.setAnnotation("@RequestMapping(value=\"/" + page.getId() + "/" + form.getId()
				+ "\", method = RequestMethod.POST)");

		return method;
	}

	private static Field[] createControllerGETMethodArgs(Page page) {
		List<Field> fields = new ArrayList<Field>();
		fields.add(new Field("ModelMap", "model"));
		for (PageParameter param : page.getParameters()) {
			fields.add(new Field("String", param.getId()));
		}
		return fields.toArray(new Field[fields.size()]);
	}

	/**
	 * Loop through form fields to provide inputs.
	 */
	private static Field[] createControllerPOSTMethodArgs(Page page, Form form) {
		List<Field> fields = new ArrayList<Field>();
		// fields.add(new Field("ModelMap", "model"));
		for (SingleUserInput input : form.getInputs()) {
			fields.add(new Field("String", input.getId()));
		}
		return fields.toArray(new Field[fields.size()]);
	}

	private static Field[] createServiceArgs(Page page, Form form) {
		return createControllerPOSTMethodArgs(page, form);
	}
}