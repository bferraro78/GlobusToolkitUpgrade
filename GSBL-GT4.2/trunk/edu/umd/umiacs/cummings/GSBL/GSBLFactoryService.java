/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import edu.umd.umiacs.cummings.GSBL.GSBLResourceHome;

import java.rmi.RemoteException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceKey;
import org.globus.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.MessageContext;
import org.globus.wsrf.utils.AddressingUtils;
import org.globus.wsrf.container.ServiceHost;
import edu.umd.umiacs.cummings.GSBL.GT42GSBLFactoryService_wsdl.CreateResource;
import edu.umd.umiacs.cummings.GSBL.GT42GSBLFactoryService_wsdl.CreateResourceResponse;

/**
 * This is the base class for our Grid factory services.
 */
public class GSBLFactoryService {

	// The logger.
	static Log log = LogFactory.getLog(GSBLFactoryService.class.getName());

	/**
	 * Creates a unique GSBL resource.
	 */
	public CreateResourceResponse createResource(CreateResource request)
			throws RemoteException {
		ResourceContext ctx = null;
		GSBLResourceHome home = null;
		ResourceKey key = null;

		/* First, we create a new GSBLResource through the GSBLResourceHome */
		try {
			ctx = ResourceContext.getResourceContext();
			home = (GSBLResourceHome) ctx.getResourceHome();
			key = home.create();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("", e);
		}
		EndpointReferenceType epr = null;

		/*
		 * We construct the instance's endpoint reference. The instance's
		 * service path can be found in the WSDD file as a parameter.
		 */
		try {
			URL baseURL = ServiceHost.getBaseURL();
			String instanceService = (String) MessageContext
					.getCurrentContext().getService().getOption("instance");
			String instanceURI = baseURL.toString() + instanceService;
			// The endpoint reference includes the instance's URI and the
			// resource key
			epr = AddressingUtils.createEndpointReference(instanceURI, key);
		} catch (Exception e) {
			throw new RemoteException("", e);
		}

		/* Finally, return the endpoint reference in a CreateResourceResponse */
		CreateResourceResponse response = new CreateResourceResponse();
		response.setEndpointReference(epr);
		return response;
	}
}
