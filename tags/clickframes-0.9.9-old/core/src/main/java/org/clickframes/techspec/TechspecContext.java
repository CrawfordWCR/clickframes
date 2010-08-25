package org.clickframes.techspec;

import org.clickframes.model.Appspec;

/**
 * @author Vineet Manohar
 */
public class TechspecContext {
    private Techspec techspec;
    private Appspec appspec;

    public TechspecContext(Techspec techspec, Appspec appspec) {
        this.techspec = techspec;
        this.appspec = appspec;
    }

    public Techspec getTechspec() {
        return techspec;
    }

    public Appspec getAppspec() {
        return appspec;
    }
}
