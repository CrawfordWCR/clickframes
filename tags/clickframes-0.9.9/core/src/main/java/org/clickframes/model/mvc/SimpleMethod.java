package org.clickframes.model.mvc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.clickframes.model.Field;

public class SimpleMethod implements Comparable<SimpleMethod> {

	private final String visibility;
	private final String returnType;
	private final String name;
	private final Field[] args;
	private List<String> lines = new ArrayList<String>();
	private String annotation;

	public SimpleMethod(String visibility, String returnType, String name, List<String> lines, Field... args) {
		this.visibility = visibility;
		this.returnType = returnType;
		this.name = name;
		this.args = args;
		this.lines = lines;
	}

	/**
	 * Used for public method.
	 */
	public SimpleMethod(String returnType, String name, List<String> lines, Field... args) {
		this("public", returnType, name, lines, args);
	}

	/**
	 * Used for public method with only one line of it's implementation.
	 */
	public SimpleMethod(String returnType, String name, String implementation, Field... args) {
		this(returnType, name, new ArrayList<String>(1), args);
		lines.add(implementation);
	}

	/**
	 * Used for public method without implementation.
	 *
	 * @param returnType
	 * @param name
	 * @param args
	 */
	public SimpleMethod(String returnType, String name, Field... args) {
		this(returnType, name, new ArrayList<String>(0), args);
	}

	public final String getVisibility() {
		return visibility;
	}

	public final String getReturnType() {
		return returnType;
	}

	public final String getName() {
		return name;
	}

	public final Field[] getArgs() {
		return args;
	}

	public final String getArgsAsString() {
		StringBuilder sb = new StringBuilder();

		if (args != null) {
			int i = 0;
			for (Field field : args) {
				if (i++ != 0) {
					sb.append(", ");
				}
				sb.append(field.getType() + " " + field.getName());
			}
		}
		return sb.toString();
	}

	public final List<String> getLines() {
		return lines;
	}

	public final String getAnnotation() {
		return annotation;
	}

	public final void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	/**
	 * Factory method
	 */
	public static final SimpleMethod createGetter(Field field) {
		String methodName = "get" + StringUtils.capitalize(field.getName());
		String implementation = "return " + field.getName() + ";";
		return new SimpleMethod(field.getType(), methodName, implementation);

		// field.getName(), form, field.getType(), methodName);
	}

	/**
	 * Factory method
	 */
	public static final SimpleMethod createSetter(Field field) {
		String methodName = "set" + StringUtils.capitalize(field.getName());
		String implementation = "this." + field.getName() + " = " + field.getName() + ";";
		return new SimpleMethod("void", methodName, implementation, field);
	}

	//need to make SimpleMethod comparable so dynamic form service will generate methods in deterministic order.
	@Override
	public int compareTo(SimpleMethod o) {
		if(getName() != null && o != null){
			return getName().compareTo(o.getName());
		}
		// TODO Auto-generated method stub
		return 0;
	}
}
