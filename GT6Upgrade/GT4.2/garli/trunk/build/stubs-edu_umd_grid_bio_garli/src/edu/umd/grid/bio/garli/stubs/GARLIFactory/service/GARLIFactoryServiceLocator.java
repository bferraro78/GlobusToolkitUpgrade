/**
 * GARLIFactoryServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Mar 01, 2007 (10:42:15 CST) WSDL2Java emitter.
 */

package edu.umd.grid.bio.garli.stubs.GARLIFactory.service;

public class GARLIFactoryServiceLocator extends org.apache.axis.client.Service implements edu.umd.grid.bio.garli.stubs.GARLIFactory.service.GARLIFactoryService {

    public GARLIFactoryServiceLocator() {
    }


    public GARLIFactoryServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GARLIFactoryServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GARLIFactoryPortTypePort
    private java.lang.String GARLIFactoryPortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getGARLIFactoryPortTypePortAddress() {
        return GARLIFactoryPortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GARLIFactoryPortTypePortWSDDServiceName = "GARLIFactoryPortTypePort";

    public java.lang.String getGARLIFactoryPortTypePortWSDDServiceName() {
        return GARLIFactoryPortTypePortWSDDServiceName;
    }

    public void setGARLIFactoryPortTypePortWSDDServiceName(java.lang.String name) {
        GARLIFactoryPortTypePortWSDDServiceName = name;
    }

    public edu.umd.grid.bio.garli.stubs.GARLIFactoryService.GARLIFactoryPortType getGARLIFactoryPortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GARLIFactoryPortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGARLIFactoryPortTypePort(endpoint);
    }

    public edu.umd.grid.bio.garli.stubs.GARLIFactoryService.GARLIFactoryPortType getGARLIFactoryPortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.umd.grid.bio.garli.stubs.GARLIFactoryService.bindings.GARLIFactoryPortTypeSOAPBindingStub _stub = new edu.umd.grid.bio.garli.stubs.GARLIFactoryService.bindings.GARLIFactoryPortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getGARLIFactoryPortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGARLIFactoryPortTypePortEndpointAddress(java.lang.String address) {
        GARLIFactoryPortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.umd.grid.bio.garli.stubs.GARLIFactoryService.GARLIFactoryPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.umd.grid.bio.garli.stubs.GARLIFactoryService.bindings.GARLIFactoryPortTypeSOAPBindingStub _stub = new edu.umd.grid.bio.garli.stubs.GARLIFactoryService.bindings.GARLIFactoryPortTypeSOAPBindingStub(new java.net.URL(GARLIFactoryPortTypePort_address), this);
                _stub.setPortName(getGARLIFactoryPortTypePortWSDDServiceName());
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
        if ("GARLIFactoryPortTypePort".equals(inputPortName)) {
            return getGARLIFactoryPortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GARLIFactoryService/service", "GARLIFactoryService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GARLIFactoryService/service", "GARLIFactoryPortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GARLIFactoryPortTypePort".equals(portName)) {
            setGARLIFactoryPortTypePortEndpointAddress(address);
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
