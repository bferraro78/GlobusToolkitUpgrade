/**
 * GSIServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Mar 01, 2007 (10:42:15 CST) WSDL2Java emitter.
 */

package edu.umd.grid.bio.gsi.stubs.GSI.service;

public class GSIServiceLocator extends org.apache.axis.client.Service implements edu.umd.grid.bio.gsi.stubs.GSI.service.GSIService {

    public GSIServiceLocator() {
    }


    public GSIServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GSIServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GSIPortTypePort
    private java.lang.String GSIPortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getGSIPortTypePortAddress() {
        return GSIPortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GSIPortTypePortWSDDServiceName = "GSIPortTypePort";

    public java.lang.String getGSIPortTypePortWSDDServiceName() {
        return GSIPortTypePortWSDDServiceName;
    }

    public void setGSIPortTypePortWSDDServiceName(java.lang.String name) {
        GSIPortTypePortWSDDServiceName = name;
    }

    public edu.umd.grid.bio.gsi.stubs.GSIService.GSIPortType getGSIPortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GSIPortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGSIPortTypePort(endpoint);
    }

    public edu.umd.grid.bio.gsi.stubs.GSIService.GSIPortType getGSIPortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.umd.grid.bio.gsi.stubs.GSIService.bindings.GSIPortTypeSOAPBindingStub _stub = new edu.umd.grid.bio.gsi.stubs.GSIService.bindings.GSIPortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getGSIPortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGSIPortTypePortEndpointAddress(java.lang.String address) {
        GSIPortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.umd.grid.bio.gsi.stubs.GSIService.GSIPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.umd.grid.bio.gsi.stubs.GSIService.bindings.GSIPortTypeSOAPBindingStub _stub = new edu.umd.grid.bio.gsi.stubs.GSIService.bindings.GSIPortTypeSOAPBindingStub(new java.net.URL(GSIPortTypePort_address), this);
                _stub.setPortName(getGSIPortTypePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("GSIPortTypePort".equals(inputPortName)) {
            return getGSIPortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GSIService/service", "GSIService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GSIService/service", "GSIPortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GSIPortTypePort".equals(portName)) {
            setGSIPortTypePortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
