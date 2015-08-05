/**
 * GSIPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Mar 01, 2007 (10:42:15 CST) WSDL2Java emitter.
 */

package edu.umd.grid.bio.gsi.stubs.GSIService;

public interface GSIPortType extends java.rmi.Remote {
    public boolean runService(edu.umd.grid.bio.gsi.stubs.GSIService.GSIArguments parameters) throws java.rmi.RemoteException;
    public boolean createWorkingDir(java.lang.String parameters) throws java.rmi.RemoteException;
}
