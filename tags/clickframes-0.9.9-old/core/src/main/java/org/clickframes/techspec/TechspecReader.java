package org.clickframes.techspec;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

/**
 * Reads a techspec xml and converts it into the techspec model object
 *
 * @author Vineet Manohar
 *
 */
public class TechspecReader {
    public static Techspec readTechspec(InputStream is, Techspec parent) throws JAXBException {
        org.clickframes.xmlbindings.techspec.Techspec techspecType = TechspecJaxbWrapper.inputStreamToJava(is);
        Techspec techspec = Techspec.create(techspecType, parent);
        return techspec;
    }
}