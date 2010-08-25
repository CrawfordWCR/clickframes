package org.clickframes.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Method {
	private final String returnType;
	private final String methodName;
	private final List<String> arguments = new ArrayList<String>();
	private final Form form;
	private final String name;
	private String annotation;
	private List<String> body = new ArrayList<String>();
	private final String argString;
	//getters and setters.
	public Method(String name, Form form, String returnValue, String methodName, Field... args) {
		this.name = name;
		this.form = form;
		this.returnType = returnValue;
		this.methodName = methodName;
		StringBuilder sb = new StringBuilder();

		if(args != null){
			int i = 0;
			for(Field field : args){
				if(i++ != 0){
					sb.append(", ");
				}
				sb.append(field.getType() + " " + field.getName());
			}
		}
		this.argString=sb.toString();
	}
	//service methods.
	public Method(Form form, String returnValue, String methodName, Field... args) {
		this("", form, returnValue, methodName, args);
	}

	public String getReturnType() {
		return returnType;
	}

	/**
	 * @return Form object, just in case extra info is needed.
	 */
	public Form getForm() {
		return form;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<String> getArguments() {
		return arguments;
	}

	/**
	 * Factory method
	 */
	public static final Method createGetter(Field field, Form form){
		String methodName = "get" + StringUtils.capitalize(field.getName());
		return new Method(field.getName(), form, field.getType(), methodName);
	}

	/**
	 * Factory method
	 */
	public static final Method createSetter(Field field, Form form){
		String methodName = "set" + StringUtils.capitalize(field.getName());
		return new Method(field.getName(), form, "void", methodName, field);
	}

	public String getName() {
		return name;
	}
	public String getArgString() {
		return argString;
	}
	public String getAnnotation() {
		return annotation;
	}
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}


	//need to remove duplicates from set
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
	@Override
	public String toString() {
		return getAnnotation() + " " + getMethodName() + "(" + getArgString() + "){" + body + "}";
	}
}