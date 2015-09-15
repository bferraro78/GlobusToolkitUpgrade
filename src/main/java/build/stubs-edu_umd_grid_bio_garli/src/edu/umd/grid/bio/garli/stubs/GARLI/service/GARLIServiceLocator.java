/**
 * GARLIServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Mar 01, 2007 (10:42:15 CST) WSDL2Java emitter.
 */

package edu.umd.grid.bio.garli.stubs.GARLI.service;

public class GARLIServiceLocator extends org.apache.axis.client.Service implements edu.umd.grid.bio.garli.stubs.GARLI.service.GARLIService {

    public GARLIServiceLocator() {
    }


    public GARLIServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GARLIServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GARLIPortTypePort
    private java.lang.String GARLIPortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getGARLIPortTypePortAddress() {
        return GARLIPortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GARLIPortTypePortWSDDServiceName = "GARLIPortTypePort";

    public java.lang.String getGARLIPortTypePortWSDDServiceName() {
        return GARLIPortTypePortWSDDServiceName;
    }

    public void setGARLIPortTypePortWSDDServiceName(java.lang.String name) {
        GARLIPortTypePortWSDDServiceName = name;
    }

    public edu.umd.grid.bio.garli.stubs.GARLIService.GARLIPortType getGARLIPortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GARLIPortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGARLIPortTypePort(endpoint);
    }

    public edu.umd.grid.bio.garli.stubs.GARLIService.GARLIPortType getGARLIPortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.umd.grid.bio.garli.stubs.GARLIService.bindings.GARLIPortTypeSOAPBindingStub _stub = new edu.umd.grid.bio.garli.stubs.GARLIService.bindings.GARLIPortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getGARLIPortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGARLIPortTypePortEndpointAddress(java.lang.String address) {
        GARLIPortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.umd.grid.bio.garli.stubs.GARLIService.GARLIPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.umd.grid.bio.garli.stubs.GARLIService.bindings.GARLIPortTypeSOAPBindingStub _stub = new edu.umd.grid.bio.garli.stubs.GARLIService.bindings.GARLIPortTypeSOAPBindingStub(new java.net.URL(GARLIPortTypePort_address), this);
                _stub.setPortName(getGARLIPortTypePortWSDDServiceName());
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
        if ("GARLIPortTypePort".equals(inputPortName)) {
            return getGARLIPortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GARLIService/service", "GARLIService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GARLIService/service", "GARLIPortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GARLIPortTypePort".equals(portName)) {
            setGARLIPortTypePortEndpointAddress(address);
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
