package org.clickframes.techspec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a place for storing properties or variables during the template
 * processing. This is similar to the attribute map in web applications, like
 * one in request scope, session scope etc
 *
 * It is a wrapper around the java Map class, but also has a notion of parent
 * context. If a key is not found, it is delegated to the parent context
 *
 * @author Vineet Manohar
 */
public class Context {
    private final Context parent;
    private Map<String, Object> map = Collections.synchronizedMap(new HashMap<String, Object>());

    /**
     * Create a context with a parent
     *
     * @param parent
     *            the parent context of this context, or null if there is no
     *            parent
     */
    public Context(Context parent) {
        this.parent = parent;
    }

    /**
     * Checks whether the specified key is present in this context, if not
     * delegates to the parent context.
     *
     * @param key
     *            the key to look for
     * @return true if the value associated with the key if present in either
     *         this class or parent in that order
     *
     * @author Vineet Manohar
     */
    public boolean containsKey(String key) {
        if (map.containsKey(key)) {
            return true;
        }

        if (parent != null) {
            return parent.containsKey(key);
        }

        return false;
    }

    /**
     * Checks whether the specified key is present in this context, if not
     * delegates to the parent context.
     *
     * @param key
     *            the key to look for
     * @return the value associated with the key if present in either this class
     *         or parent in that order
     *
     * @author Vineet Manohar
     */
    public Object get(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }

        if (parent != null) {
            return parent.get(key);
        }

        return null;
    }

    public Context getParent() {
        return parent;
    }

    /**
     * Stores a value in the given context
     *
     * @param key
     * @param value
     *
     * @author Vineet Manohar
     */
    public void put(String key, Object value) {
        map.put(key, value);
    }

    /**
     * Stores a value in the given context
     *
     * @param key
     * @param value
     *
     * @author Vineet Manohar
     */
    public void putAll(Map<String, Object> m) {
        map.putAll(m);
    }

    public Map<String, Object> getLocalMap() {
        return Collections.unmodifiableMap(map);
    }

    public Map<String, Object> getFullMap() {
        Map<String, Object> retVal = new HashMap<String, Object>();
        if (parent != null) {
            retVal.putAll(parent.getFullMap());
        }

        retVal.putAll(map);
        return Collections.unmodifiableMap(retVal);
    }

    //
    // @Override
    // public String toString() {
    // return getFullMap().toString();
    // }
}