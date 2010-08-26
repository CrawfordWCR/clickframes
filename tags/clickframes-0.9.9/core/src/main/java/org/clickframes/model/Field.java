package org.clickframes.model;

import org.apache.commons.lang.StringUtils;

public class Field {
	private final String annotation;
	private final String type;
	private final String name;
	private String comment;

	/**
	 * Use me for Injected resources. For example,
	 *
	 * <pre>
	 * Field(&quot;Resource&quot;, &quot;EntityManager&quot;, &quot;em&quot;)
	 * </pre>
	 *
	 * is passed to a template
	 *
	 * <pre>
	 * @${field.annotation}
	 * private ${field.type} ${field.name}
	 * </pre>
	 *
	 * to render
	 *
	 * <pre>
	 * &#064;Resource
	 * private EntityManager em;
	 * </pre>
	 */
	public Field(String annotation, String type, String name) {
		this.annotation = annotation;
		this.type = type;
		this.name = name;
	}

	/**
	 * Use me for conventional fields. For example,
	 *
	 * <pre>
	 * Field(&quot;String&quot;, &quot;firstName&quot;)
	 * </pre>
	 *
	 * is passed to a template
	 *
	 * <pre>
	 * private ${field.type} ${field.name}
	 * </pre>
	 *
	 * to render
	 *
	 * <pre>
	 * private String firstName;
	 * </pre>
	 */
	public Field(String type, String name) {
		this("", type, name);
	}

	/**
	 * Use me to create a field with the same name as your type. For example,
	 *
	 * <pre>
	 * Field(&quot;RegistrationService&quot;)
	 * </pre>
	 *
	 * is passed to a template
	 *
	 * <pre>
	 * private ${field.type} ${field.name}
	 * </pre>
	 *
	 * to render
	 *
	 * <pre>
	 * private RegistrationService registrationService;
	 * </pre>
	 */
	public Field(String type) {
		this("", type, StringUtils.uncapitalize(type));
	}

	public final String getAnnotation() {
		return annotation;
	}

	public final String getType() {
		return type;
	}

	public final String getName() {
		return name;
	}

	/**
	 * @return only the contents of comment.
	 */
	public final String getComment() {
		return comment;
	}

	/**
	 * @return full comment...everything you put above the field, including comment syntax
	 */
	public final String getFormattedComment() {
		if(comment == null || comment.trim().length()==0){
			return "";
		}
		return "/**\t" + comment.trim() + "\t*/";
	}

	public final void setComment(String comment) {
		this.comment = comment;
	}
}