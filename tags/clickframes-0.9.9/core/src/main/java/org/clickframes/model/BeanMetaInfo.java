package org.clickframes.model;

import java.util.ArrayList;
import java.util.List;

import org.clickframes.model.Method;

/**
 * Value Object to make Velocity easier to read.
 *
 * @author Steven Boscarine
 *
 */
public class BeanMetaInfo {
	private final List<String> imports = new ArrayList<String>();
	private final List<Field> fields = new ArrayList<Field>();
	private final List<Method> getters = new ArrayList<Method>();
	private final List<Method> setters = new ArrayList<Method>();
	private final String className;

	public BeanMetaInfo(String className) {
		super();
		this.className = className;
	}

	public List<String> getImports() {
		return imports;
	}

	public List<Field> getFields() {
		return fields;
	}


	public String getClassName() {
		return className;
	}

	public List<Method> getGetters() {
		return getters;
	}

	public List<Method> getSetters() {
		return setters;
	}
}
