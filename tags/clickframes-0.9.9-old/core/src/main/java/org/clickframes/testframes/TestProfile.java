package org.clickframes.testframes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * represents a test profile
 *
 * @author Vineet Manohar
 */
public class TestProfile {
    private static final Log log = LogFactory.getLog(TestProfile.class);

    private Map<String, Object> properties = new HashMap<String, Object>();
    private String profileName;

    /**
     * Creates a profile by loading properties file in this order
     *
     * 1) src/test/testbed/applicationInitialize.properties
     *
     * 1) src/test/testbed/<server name>/<profile name>.properties
     *
     * 2) USER_HOME/.<app id>/testbed/<profile name>.properties
     *
     * If not properties file is found, a on-the-fly profile is created
     *
     * @param profileName
     * @return
     *
     * @author Vineet Manohar
     */
    public static TestProfile create(String appId, String profileName, String baseUrl, String browser) {
        TestProfile profile = new TestProfile();

        File applicationInitializePropertiesFile = new File("src" + File.separator + "test" + File.separator + "testbed" + File.separator
                + "applicationInitialize.properties");
        profile.getProperties().putAll(readPropertiesIfFileExists("common file: ", applicationInitializePropertiesFile));

        File committedFile = new File("src" + File.separator + "test" + File.separator + "testbed" + File.separator
                + getLocalHostName() + File.separator + profileName + ".properties");
        profile.getProperties().putAll(readPropertiesIfFileExists("committed file: ", committedFile));

        File localFile = new File(System.getProperty("user.home") + File.separator + "." + appId + File.separator
                + "testbed" + File.separator + profileName + ".properties");
        profile.getProperties().putAll(readPropertiesIfFileExists("local file: ", localFile));

        if (baseUrl != null) {
            profile.setBaseUrl(baseUrl);
        }

        if (browser != null) {
            profile.setBrowser(browser);
        }

        if (StringUtils.isEmpty(profile.getBaseUrl())) {
            throw new RuntimeException(
                    "baseUrl is required. You can set the baseUrl in either the committed file or your local file\n\tcommitted file: "
                            + committedFile.getAbsolutePath() + "\n\tlocal file: " + localFile.getAbsolutePath());
        }

        if (StringUtils.isEmpty(profile.getBrowser())) {
            profile.setBrowser("*firefox");
        }

        return profile;
    }

    private static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    private static Map<String, Object> readPropertiesIfFileExists(String fileType, File localFile) {
        Map<String, Object> map = new HashMap<String, Object>();

        log.info("Reading " + fileType + " file: " + localFile.getAbsolutePath());

        if (localFile.exists()) {
            Properties localProperties = new Properties();
            try {
                localProperties.load(new FileInputStream(localFile));
                Set<Object> keys = localProperties.keySet();

                for (Object key : keys) {
                    map.put(key.toString(), localProperties.get(key));
                }
            } catch (FileNotFoundException e) {
                log.error(new RuntimeException(
                        "The local file was here less than a second ago but is gone, we might need file locking"));
            } catch (IOException e) {
                throw new RuntimeException("Error reading profile properties file: " + localFile.getAbsolutePath());
            }
        }
        return map;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getBaseUrl() {
        return getPropertyAsString("baseUrl");
    }

    private String getPropertyAsString(String propertyName) {
        if (properties.get(propertyName) != null) {
            return properties.get(propertyName).toString();
        }

        return null;
    }

    public void setProperty(String propertyName, Object propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    public String getBrowser() {
        return getPropertyAsString("browser");
    }

    public void setBrowser(String browser) {
        setProperty("browser", browser);
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setBaseUrl(String baseUrl) {
        setProperty("baseUrl", baseUrl);
    }
}