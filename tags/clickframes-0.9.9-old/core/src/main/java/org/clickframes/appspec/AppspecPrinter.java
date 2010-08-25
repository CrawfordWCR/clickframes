package org.clickframes.appspec;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.AppspecElement;

/**
 * This will need to be moved.
 * 
 * @author sboscarine
 * 
 */
public class AppspecPrinter {
	private static final Log logger = LogFactory.getLog(AppspecPrinter.class);
	private static final String ROOT_APPSPEC_VARIABLE_NAME = "appspec";

	/**
	 * @return Map<Appspec Variable Name, Value>
	 */
	public static Map<String, Object> printAppspecAsELVariables(AppspecElement appspec, boolean expandCollection) {
		return printVars(appspec, new HashSet<AppspecElement>(), ROOT_APPSPEC_VARIABLE_NAME, expandCollection);
	}

	private static Set<String> forbidden = new HashSet<String>();
	static {
		forbidden.add("getClass");
		forbidden.add("getLoginPage");
		forbidden.add("getLoginUsernameInput");
		forbidden.add("getLoginPasswordInput");
		forbidden.add("getLoginAction");
		forbidden.add("getLoginForm");
		forbidden.add("getLoginSuccessfulOutcome");
		forbidden.add("isLoginAction");
		forbidden.add("getLoginFailedOutcome");
		forbidden.add("getNestedVariableMap");
		forbidden.add("getVariableMap");

	}

	public static Map<String, Object> printVars(AppspecElement in, Set<AppspecElement> elements, String var, boolean expandCollection) {
		Map<String, Object> out = new TreeMap<String, Object>();
		if (elements.contains(in)) {
			return out;
		}
		elements.add(in);
		for (Method m : in.getClass().getMethods()) {
			final String methodName = m.getName();
			if ((methodName.startsWith("get") || methodName.startsWith("is")) && m.getParameterTypes().length == 0
					&& !forbidden.contains(methodName)) {
				final Class<?> returnType = m.getReturnType();
				try {
					Object value = m.invoke(in, (Object[]) null);
					final String varName = buildVarName(var, m);
					if (returnType.isInstance(AppspecElement.class)) {
						final Map<String, Object> tmp = printVars((AppspecElement) value, elements, varName, expandCollection);
						out.putAll(tmp);
					// } else if (returnType.isAssignableFrom(Collection.class)) {
					} else if (expandCollection && Collection.class.isAssignableFrom(returnType)) {
						out.putAll(printCollection((Collection<?>) value, elements, varName, expandCollection));
					} else {
						// out.put(varName, value + "");
						out.put(varName, value);
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return out;
	}

	private static Map<String, Object> printCollection(Collection<?> in, Set<AppspecElement> elements, String var, boolean expandCollection) {
		Map<String, Object> out = new HashMap<String, Object>();
		int i = 0;
		for (Object o : in) {
			if (o instanceof AppspecElement) {
				AppspecElement element = (AppspecElement) o;
				// out.putAll(printVars(element, elements, buildVarName(var, "" + i++), expandCollection));
				out.putAll(printVars(element, elements, buildVarName(var, element.getId()), expandCollection));
			} else {
				logger.error(o.getClass()
						+ " needs to be an AppspecElement to be printed.  All instances will be ignored.");
			}

		}
		return out;
	}

	private static String buildVarName(String var, Method m) {
		return var + "." + convertJavaMethodCallToELMethodCall(m.getName());
	}

	private static String buildVarName(String var, String pos) {
		return var + "[" + pos + "]";
	}

	/**
	 * 
	 * @param in
	 *            getMyProperty() or getMyProperty or isMyProperty
	 * @return myProperty
	 */
	private static String convertJavaMethodCallToELMethodCall(String in) {
		if (in == null){
			return null;
		}
		String out = in;
		if (in.startsWith("is")) {
			out=  out.substring(2,3).toLowerCase() +  out.substring(3);
		}else if(in.startsWith("get")){
			out=  out.substring(3,4).toLowerCase() +  out.substring(4);
		}
		out = out.replaceAll("()", "");
		return out;
	}
}
