package org.clickframes.model;

import java.util.Map;

/**
 * Common properties to each Appsec model element.
 * 
 * @author sboscarine
 * 
 */
public interface AppspecElement {
    /**
     * @return identifier for this element.
     */
    public String getId();
    
    /**
     * @return uppercase version of id
     */
    public String getUppercaseId();

    /**
     * @return id, capitalized, for this element.
     */
    public String getName();

    /**
     * @return title - the print friendly name for this element. For example, if
     *         id is "firstName" the title is "First Name"
     */
    public String getTitle();

    /**
     * @return xml escaped title 
     */
    public String getTitleEscaped();

    /**
     * @return globally unique (in the context of the appspec) identifier for
     *         this element.
     */
    public AsGUID getGuid();

    public AppspecElement getParent();

    /**
     * X - stands for extension. This method returns the runtime extension for
     * any appspec element. This is a very useful and practical way to deal with
     * plugins wanting to customize appspec elements.
     * 
     * For example, a plugin may want pages to be named as ${page.name}Page in
     * the generated code.
     * 
     * So it can define an extension called "className". by calling
     * page.getX().put("className", page.getName() + "Page").
     * 
     * The templates can then use ${page.x.name} instead of defining the same
     * relationship in template.
     * 
     * This promotes code use and makes plugin authoring easy.
     * 
     * @return a map of string, Object key-value pairs. The key is the extension
     *         name, the value is the extension value
     * 
     * @author Vineet Manohar
     */
    public Map<String, Object> getX();
}
