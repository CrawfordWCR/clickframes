/*
 * Clickframes: Full lifecycle software development automation.
 * Copyright (C)  2009 Children's Hospital Boston
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.clickframes;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.clickframes.xmlbindings.AppspecType;
import org.xml.sax.SAXException;

public class AppspecJaxbWrapper {
    private static JAXBContext appspecTypeJAXBContext = null;
    private static SchemaFactory schemaFactory = null;

    public static AppspecType readAppspecType(InputStream is) throws JAXBException {
        if (appspecTypeJAXBContext == null) {
            schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            appspecTypeJAXBContext = JAXBContext.newInstance(AppspecType.class);
        }

        Unmarshaller appspecTypeUnmarshaller = appspecTypeJAXBContext.createUnmarshaller();

        try {
            appspecTypeUnmarshaller.setSchema(schemaFactory.newSchema(AppspecJaxbWrapper.class.getResource("/clickframes.xsd")));
        } catch (SAXException e) {
            throw new RuntimeException("Error while reading clickframes schema", e);
        }

        JAXBElement<AppspecType> root = appspecTypeUnmarshaller.unmarshal(new StreamSource(is), AppspecType.class);
        AppspecType projectType = root.getValue();
        return projectType;
    }
}