package org.clickframes.clipr.domain;

import java.io.File;
import java.util.UUID;

import org.clickframes.model.Appspec;

public class App {
    private static File dir;
    private static String baseUrl;
    private String id;

    public static String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        App.baseUrl = baseUrl;
    }

    public static File getAppsDir() {
        return dir;
    }

    public void setDir(File appsDir) {
        App.dir = appsDir;
    }

    private static String generateShortRandomString() {
        String random = UUID.randomUUID().toString();

        random = random.replaceAll("^(..).*(......)$", "$1$2");
        return random;
    }

    /**
     * determine the appId for the appspec
     * 
     * @param appspec
     * @return
     */
    public static String getNewAppId(Appspec appspec) {
        // ignore apspec and generate a brand new short random id
        String appId = generateShortRandomString();

        // TODO: make sure that it is unique
        return appId;
    }

    public String getUrl() {
        return baseUrl + "/" + id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
