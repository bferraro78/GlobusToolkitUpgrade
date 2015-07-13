/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import javax.xml.namespace.QName;

public interface GSBLQNames {
	public static final String NS = "http://cummings.umiacs.umd.edu/GSBL";

	public static final QName RESOURCE_PROPERTIES = new QName(NS,
			"GSBLResourceProperties");

	public static final QName RESOURCE_REFERENCE = new QName(NS,
			"GSBLResourceReference");
}