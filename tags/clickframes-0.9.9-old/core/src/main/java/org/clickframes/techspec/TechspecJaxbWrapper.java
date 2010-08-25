package org.clickframes.techspec;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * JAXB Wrapper for techspec.xsd
 *
 * @author Vineet Manohar
 */
public class TechspecJaxbWrapper {
    private static boolean initialized = false;
    private static JAXBContext jaxbContext = null;
    private static Log log = LogFactory.getLog(TechspecJaxbWrapper.class);

    private static Schema schema;

    private static SchemaFactory schemaFactory = null;

    static {
        try {
            init();
        } catch (JAXBException e) {
            throw new RuntimeException("Could not intialize class", e);
        } catch (SAXException e) {
            throw new RuntimeException("Could not intialize class", e);
        }
    }

    private static Marshaller createMarshaller() throws JAXBException {
        return createMarshaller(schema);
    }

    private static Marshaller createMarshaller(Schema schema) throws JAXBException, PropertyException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setSchema(schema);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        return marshaller;
    }

    private static Unmarshaller createUnmarshaller() throws JAXBException {
        return createUnmarshaller(schema);
    }

    private static Unmarshaller createUnmarshaller(Schema schema) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        return unmarshaller;
    }

    private static void init() throws JAXBException, SAXException {
        if (initialized) {
            log.info("jaxb context already initialized");
            return;
        }

        jaxbContext = JAXBContext.newInstance(org.clickframes.xmlbindings.techspec.Techspec.class);
        schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        schema = schemaFactory.newSchema(TechspecJaxbWrapper.class.getResource("/techspec.xsd"));

        // log.info("jaxb context initialized");
        initialized = true;
    }

    public static String toXml(org.clickframes.xmlbindings.techspec.Techspec techspecType) throws JAXBException {
        StringWriter writer = new StringWriter();
        createMarshaller().marshal(techspecType, writer);
        return writer.toString();
    }

    public static void validate(org.clickframes.xmlbindings.techspec.Techspec techspecType) throws JAXBException {
        createMarshaller().marshal(techspecType, new StringWriter());
    }

    public static org.clickframes.xmlbindings.techspec.Techspec xmlToObject(String xml) throws JAXBException {
        StringReader stringReader = new StringReader(xml);
        return (org.clickframes.xmlbindings.techspec.Techspec) createUnmarshaller().unmarshal(stringReader);
    }

    public static org.clickframes.xmlbindings.techspec.Techspec inputStreamToJava(InputStream is) throws JAXBException {
        return (org.clickframes.xmlbindings.techspec.Techspec) createUnmarshaller().unmarshal(is);
    }
}