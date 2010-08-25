package org.clickframes.jsf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.clickframes.model.Appspec;
import org.clickframes.model.Field;
import org.clickframes.model.Form;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.mvc.GeneralClass;
import org.clickframes.model.mvc.SimpleMethod;
import org.clickframes.xmlbindings.InputTypeEnum;

/**
 * Create List<SelectItem> methods
 *
 * @author sboscarine
 *
 */
public class DynamicInputFactory {

	// private static final String LAST_LINE = "return options;";
	private static final String LAST_LINE = "return adapter.getPropery(options);";
	private static final String FIRST_LINE = "List<SelectItem> options = new ArrayList<SelectItem>();";
	private static final String RETURN_TYPE = "List<SelectItem>";

	public static GeneralClass createFormService(Appspec appspec) {
		GeneralClass service = new GeneralClass("FormService");
		for (Page page : appspec.getPages()) {
			for (Form form : page.getForms()) {
				service.getMethods().addAll(createDynamicInputMethod(page, form));
			}
		}
		service.getImports().add("java.util.ArrayList");
		service.getImports().add("java.util.List");
		service.getImports().add("javax.faces.model.SelectItem");

		// emergency
		service.getImports().add("javax.annotation.Resource");
		service.getFields().add(new Field("@Resource", "DynamicFormServiceAdapter", "adapter"));
		return service;
	}

	public static Set<SimpleMethod> createDynamicInputMethod(Page page, Form form) {
		Set<SimpleMethod> returnValue = new TreeSet<SimpleMethod>();
		for (SingleUserInput input : form.getInputsByType(InputTypeEnum.RADIO)) {
			returnValue.add(createMethod(page, form, input));
		}
		for (SingleUserInput input : form.getInputsByType(InputTypeEnum.DROPDOWN)) {
			returnValue.add(createMethod(page, form, input));
		}
		for (SingleUserInput input : form.getInputsByType(InputTypeEnum.CHECKBOX)) {
			returnValue.add(createMethod(page, form, input));
		}

		return returnValue;
	}

	private static SimpleMethod createMethod(Page page, Form form, SingleUserInput input) {
		List<String> lines = new ArrayList<String>();
		lines.add(FIRST_LINE);
//		System.out.println(input.getId() + "\t" + input.getType());
		if (input.getType().equalsIgnoreCase("dropdown")) {
			lines.add("options.add(new SelectItem(\"\", \"Please select an option\"));");
		}
		for (String value : input.getAllowedValues().keySet()) {
			String msgKey = "#{messages." + form.getId() + "_" + input.getId() + "_" + value + "}";
			lines.add("options.add(new SelectItem(\"" + value + "\", \"" + msgKey + "\"));");
		}
		lines.add(LAST_LINE);
		String methodName = createOptionMethodName(page, form, input);

		SimpleMethod method = new SimpleMethod(RETURN_TYPE, methodName, lines);
		return method;
	}

	// reused by consuming classes
	public static String createOptionMethodName(Page page, Form form, SingleUserInput input) {
		return "get" + page.getName() + form.getName() + input.getName() + "Options";
	}

}
