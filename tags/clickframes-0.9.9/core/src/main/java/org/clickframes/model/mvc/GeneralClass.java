package org.clickframes.model.mvc;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.clickframes.ISGBeanUtilities;
import org.clickframes.model.Field;

/**
 * Model MVC class to simplify Velocity templates.
 *
 * @author Steven Boscarine
 *
 */
public class GeneralClass {
	private final String className;
	private String classComment="";
	private String classAnnotation;
	public GeneralClass(String className) {
		this.className = className;
	}

	/** Classes to import */
	private final Set<String> imports = new TreeSet<String>();
	/**
	 * Class level fields.
	 */
	private final Set<Field> fields = new LinkedHashSet<Field>();
	private final Set<SimpleMethod> methods = new LinkedHashSet<SimpleMethod>();

	public final Set<String> getImports() {
		return imports;
	}

	public final Set<Field> getFields() {
		return fields;
	}

	public final Set<SimpleMethod> getMethods() {
		return methods;
	}

	public final String getClassName() {
		return className;
	}

	@Override
	public String toString() {
		return ISGBeanUtilities.printMe(this);
	}

	public final String getClassComment() {
		return classComment;
	}

	public final String getClassAnnotation() {
		return classAnnotation;
	}

	public final void setClassComment(String classComment) {
		this.classComment = classComment;
	}

	public final void setClassAnnotation(String classAnnotation) {
		this.classAnnotation = classAnnotation;
	}
}
