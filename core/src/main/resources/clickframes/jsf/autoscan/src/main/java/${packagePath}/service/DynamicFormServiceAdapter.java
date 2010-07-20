package ${servicePackage};
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Band-aid
 * Temporary measure as <h:selectItems> is rendering
 * 		options.add(new SelectItem("none", "#{messages.baselineForm_baseline30_none}"));
 * as #{messages.baselineForm_baseline30_none}
 * 
 * There's got to be a smarter way to do this.  
 * 
 * This breaks internationalization.
 * 
 * TODO:  Investigate view layer alternative.
 * @author Steven Boscarine
 * 
 */
@Service
public class DynamicFormServiceAdapter {
	@SuppressWarnings("unchecked")
	private Map data;

	@PostConstruct
	public void init() {
		try {
			Properties properties = new Properties();
			InputStream fileInputStream = new ClassPathResource("/messages.properties").getInputStream();
			properties.load(fileInputStream);
			data = properties;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<SelectItem> getPropery(List<SelectItem> list) {
		for (SelectItem item : list) {
			String rawKey = item.getLabel().trim();
			if (!rawKey.startsWith("#{messages.")) {
				continue; // skip. this one doesn't have unprocessed tokens.
			}
			String actualKey = getKeyFromEL(rawKey);
			item.setLabel((String) data.get(actualKey));
		}
		return list;
	}

	private static final String PATTERN_REGEX = "\\" + "#" + "\\{messages\\.(.*?)\\}";
	// Compile and use regular expression
	private static final Pattern pattern = Pattern.compile(PATTERN_REGEX);

	public static String getKeyFromEL(String inputStr) {
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.find()) {
			for (int i = 0; i <= matcher.groupCount(); i++) {
				if (i == 1) {
					return matcher.group(i);
				}
			}
		}
		return null;

	}

	@SuppressWarnings("unchecked")
	public final Map getData() {
		return data;
	}
}