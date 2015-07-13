/**
 * GSIServiceAddressingLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Mar 01, 2007 (10:42:15 CST) WSDL2Java emitter.
 */

package edu.umd.grid.bio.gsi.stubs.GSI.service;

public class GSIServiceAddressingLocator extends edu.umd.grid.bio.gsi.stubs.GSI.service.GSIServiceLocator implements edu.umd.grid.bio.gsi.stubs.GSI.service.GSIServiceAddressing {
    public edu.umd.grid.bio.gsi.stubs.GSIService.GSIPortType getGSIPortTypePort(org.globus.axis.message.addressing.EndpointReferenceType reference) throws javax.xml.rpc.ServiceException {
	org.globus.axis.message.addressing.AttributedURIType address = reference.getAddress();
	if (address == null) {
		throw new javax.xml.rpc.ServiceException("No address in EndpointReference");
	}
	java.net.URL endpoint;
	try {
		endpoint = new java.net.URL(address.toString());
	} catch (java.net.MalformedURLException e) {
		throw new javax.xml.rpc.ServiceException(e);
	}
	edu.umd.grid.bio.gsi.stubs.GSIService.GSIPortType _stub = getGSIPortTypePort(endpoint);
	if (_stub != null) {
		org.globus.axis.message.addressing.AddressingHeaders headers =
			new org.globus.axis.message.addressing.AddressingHeaders();
		headers.setTo(address);
		headers.setReferenceParameters(reference.getParameters());
		((javax.xml.rpc.Stub)_stub)._setProperty(org.globus.axis.message.addressing.Constants.ENV_ADDRESSING_SHARED_HEADERS, headers);
	}
	return _stub;
    }


}
