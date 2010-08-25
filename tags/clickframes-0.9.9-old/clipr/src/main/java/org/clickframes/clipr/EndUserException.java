package org.clickframes.clipr;

import javax.xml.bind.JAXBException;

/**
 * @author Vineet Manohar
 */
public class EndUserException extends Exception {
	private static final long serialVersionUID = 1L;

	public EndUserException(String message, Exception e) {
		super(message, e);
	}
}