package org.clickframes.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.clickframes.appspec.AppspecPrinter;
import org.clickframes.util.ClickframeUtils;

/**
 * This is a base class for the Appspec model representation. For convenience,
 * each model element should extends this class
 * 
 * @author Steven Boscarine
 * 
 */
public abstract class AbstractElement implements AppspecElement {
	private String id;
	private String title;
	private AppspecElement parent;
	private AsGUID guid;
	private Map<String, Object> x;
	private String description;

	/**
	 * Use me when ID is required.
	 */
	protected AbstractElement(String id, AppspecElement parent) {
		init(id, null, parent);
	}

	private void init(String id, String title, AppspecElement parent) {
		this.id = ClickframeUtils.cleanId(id);
		this.title = ClickframeUtils.normalize(title);
		this.parent = parent;
		this.guid = new AsGUID(parent, this);
		this.x = new HashMap<String, Object>();
	}

	/**
	 * Use me when ID is NOT required.
	 */
	protected AbstractElement(String id, String title, AppspecElement parent) {
		if (id == null && title == null) {
			throw new AppspecConstraintViolationException(
					"Both id and title are missing: " + parent.getGuid());
		}
		init(id, title, parent);
	}

	@Override
	public final String getId() {
		if (id == null) {
			return ClickframeUtils.toCompactId(this.title);
		}
		return id;
	}

	@Override
	public final AppspecElement getParent() {
		return parent;
	}

	@Override
	public AsGUID getGuid() {
		return this.guid;
	}

	/**
	 * if the id is "fooBar, the name is FooBar"
	 * 
	 * @return
	 * 
	 * @author Vineet Manohar
	 */
	@Override
	public final String getName() {
		return StringUtils.capitalize(getId());
	}

	public Map<String, Object> getX() {
		return x;
	}

	public void setX(Map<String, Object> x) {
		this.x = x;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof AppspecElement) {
			AppspecElement other = (AppspecElement) obj;

			return other.getId().equals(this.getId());
		}

		return false;
	}

	public int compareTo(Entity o) {
		return this.getId().compareTo(o.getId());
	}

	@Override
	public String toString() {
		return this.title + "(" + this.id + ")";
	}

	@Deprecated
	/**
	 * @deprecated use getTitleEscaped()
	 */
	public String getEscapedTitle() {
		return getTitleEscaped();
	}

	public String getTitleEscaped() {
		return ClickframeUtils.escapeXml(getTitle());
	}

	public String getDescription() {
		if (description == null) {
			return getTitle();
		}

		return description;
	}

	public void setDescription(String description) {
		this.description = ClickframeUtils.normalize(description);
	}

	public String getTitle() {
		if (title == null) {
			return ClickframeUtils.toHumanReadable(this.id);
		}
		return title;
	}

	public void setTitle(String title) {
		this.title = ClickframeUtils.normalize(title);
	}

	private Map<String, Object> variableMap;

	public Map<String, Object> getVariableMap() {
		if (variableMap == null) {
			variableMap = AppspecPrinter.printVars(this,
					new HashSet<AppspecElement>(), getMetaName(), false);
		}

		return variableMap;
	}

	public Map<String, Object> getNestedVariableMap() {
		// return AppspecPrinter.printAppspecAsELVariables(this);
		return AppspecPrinter.printVars(this, new HashSet<AppspecElement>(),
				getMetaName(), true);
	}

	/**
	 * The element name e.g. "page", "input"
	 * 
	 * @return
	 */
	public abstract String getMetaName();
}