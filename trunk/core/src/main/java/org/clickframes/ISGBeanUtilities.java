package org.clickframes;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ISGBeanUtilities {
	/**
	 * Uses reflection to print contents of bean to String for debugging purposes.
	 *
	 * @param o
	 *            instance of object to be printed.
	 */
	public static String printMe(Object o) {
		// Gather data via reflection, save to map
		Map<String, String> data = callEveryBeanMethodOnObject(o);
		// print data.
		final StringBuilder sb = new StringBuilder();
		sb.append(o.getClass().getSimpleName()); // append class name
		if (data.size() == 1) { // Foo(bar)
			sb.append("(");
			for (String val : data.values()) {
				sb.append(val);
			}
			sb.append(")");
		} else { // Foo{id=bar, name=fubar}
			sb.append("{");
			for (String methodName : data.keySet()) {
				sb.append("\n\t" + methodName + " = " + data.get(methodName));
			}
			sb.append("\n}");
		}
		return sb.toString();
	}

	/**
	 * Call every method on object that begins with "get" or "is"
	 *
	 * @return map of parameter to value.
	 */
	public static Map<String, String> callEveryBeanMethodOnObject(Object o) {
		Map<String, String> data = new LinkedHashMap<String, String>();
		for (Method m : o.getClass().getMethods()) {
			final String methodName = m.getName();
			if ((methodName.startsWith("get") || methodName.startsWith("is")) && m.getParameterTypes().length == 0
					&& methodName != "getClass") {
				try {
					Object value = m.invoke(o, (Object[]) null);
					if (value instanceof byte[]) { // byte arrays are "special"
						System.err.println("\n\nI was called\n\n");
						data.put(methodName, "byte[" + ((byte[]) value).length + "]"); // cast to String
						continue; // Skip for now. Is this causing gnome-terminal display issues?
					}//else
					data.put(methodName, "" + value); // cast to String
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return data;
	}

	public static boolean compareTwoBeans(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) { // from statement above, we know both aren't null.
			return false;
		}
		for (Method method0 : o1.getClass().getMethods()) {
			final String methodName = method0.getName();
			if ((methodName.startsWith("get") || methodName.startsWith("is"))
					&& method0.getParameterTypes().length == 0 && methodName != "getClass") {
				try {
					Method method1 = o2.getClass().getMethod(methodName, (Class[]) null);
					Object value1 = method0.invoke(o1, (Object[]) null);
					Object value2 = method1.invoke(o2, (Object[]) null);
					if (value1 == null) {
						return value1 == value2; // both are null and therefore equal.
					}
					// handle byte arrays[]
					if (method0.getReturnType() == byte[].class) {
						return Arrays.equals((byte[]) value1, (byte[]) value2);
					}

					if (!value1.equals(value2)) {
						return false;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return true;
	}

	public static int generateHashCode(Object o) {
		return printMe(o).hashCode(); // TODO: is this reckless?
	}

}