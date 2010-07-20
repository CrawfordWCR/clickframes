package org.clickframes.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * New Object representing service layer.
 *
 * Created to allow user to override default settings.
 *
 * Encapsulates logic for generating Service
 *
 * This may get moved.
 *
 * @author Steven Boscarine
 *
 */
public class Service {
	/**
	 * Method per action in appspec. Gets mapped to service that takes entity as input.
	 */
	private final List<Method> serviceLayerMethods = new ArrayList<Method>();
	/**
	 * Methods that handle server side validation.
	 * This collection is presently SWF-specific, may be generalized in future
	 */
	private final List<Method> validatorMethods = new ArrayList<Method>();
	/**
	 * TODO:  Explain!
	 * This collection is presently SWF-specific, may be generalized in future
	 */
	private final List<Method> flowMethods = new ArrayList<Method>();
	/** Classes to import */
	private final Set<String> imports = new HashSet<String>();
	/** Used for SWF <on-start> initialization (as well as other templates); */
	private final Set<String> entities = new HashSet<String>();
	/** Name for service method class (presently page ID capitalized) */
	private final String serviceClass;

	/**
	 * @return Class that handles server-side validation. This value is presently SWF-specific, may be generalized
	 *         in future
	 */
	private final String validatorClass;

	/**
	 *  for populating selectItem
	 */
	private final Collection<Method> dynamicDataMethods = new HashSet<Method>();

	public Service(String serviceClass, String validatorClass) {
		super();
		this.serviceClass = serviceClass;
		this.validatorClass = validatorClass;
	}

	public List<Method> getServiceMethods() {
		return serviceLayerMethods;
	}

	public List<Method> getValidatorMethods() {
		return validatorMethods;
	}

	public Set<String> getImports() {
		return imports;
	}

	public Set<String> getEntities() {
		return entities;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public String getValidatorClass() {
		return validatorClass;
	}

	public List<Method> getFlowMethods() {
		return flowMethods;
	}

	public Collection<Method> getDynamicDataMethods() {
		return dynamicDataMethods;
	}
}