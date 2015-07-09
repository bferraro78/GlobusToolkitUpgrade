/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import org.globus.axis.util.Util;

import org.globus.axis.message.addressing.Address;
import org.globus.axis.message.addressing.EndpointReferenceType;
import org.globus.axis.message.addressing.ReferenceParametersType;
import org.apache.axis.message.MessageElement;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.oasis.wsrf.lifetime.Destroy;
import org.xml.sax.InputSource;
import org.globus.wsrf.encoding.ObjectSerializer;

import edu.umd.umiacs.cummings.GSBL.GT42GSBLFactoryService_wsdl.CreateResource;
import edu.umd.umiacs.cummings.GSBL.GT42GSBLFactoryService_wsdl.CreateResourceResponse;

import java.rmi.RemoteException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.io.*;
import java.util.Random;

/**
 * Grid client base class.
 */
public class GSBLClient {

	/**
	 * A writable directory under which our temporary directories will be
	 * created.
	 */
	final static protected String workingDirBase = "/export/grid_files/";

	/**
	 * Hostname of the server we are submitting to.
	 */
	protected String factoryHost = null;

	// Associated port types
	protected Object factoryPortType;
	protected Object instancePortType;

	// Endpoint reference for our factory.
	protected EndpointReferenceType factoryEPR = null;

	// Endpoint reference for our instance.
	protected EndpointReferenceType instanceEPR = null;

	// The resource ID should be unique.
	protected String resourceID = null;

	// The logger.
	static Log log = LogFactory.getLog(GSBLClient.class.getName());

	/**
	 * Constructor for the GSBLClient class that acquires a new service
	 * instance.
	 * 
	 * @param svcName
	 *            The name of the grid service, e.g. "Ssearch34".
	 * @param factoryURI
	 *            The URI of the grid service factory.
	 * @param factorySvcLocatorClass
	 *            The actual class object of the factory service locator.
	 * @param instanceSvcLocatorClass
	 *            The actual class object of the instance service locator.
	 * @throws Exception
	 *             If the client could not be initialized.
	 */

	public GSBLClient(String svcName, String factoryURI,
			Class factorySvcLocatorClass, Class instanceSvcLocatorClass)
			throws Exception {

		// extract factory host from factoryURI
		String[] factoryChunks = factoryURI.split("/");
		factoryHost = factoryChunks[2];

		// remove the port
		factoryHost = factoryHost.substring(0, factoryHost.indexOf(":"));
		log.debug("factoryHost is: " + factoryHost);

		try {
			Object factoryLocator = factorySvcLocatorClass.newInstance();
			Object instanceLocator = instanceSvcLocatorClass.newInstance();

			// must be used with secure containers!
			Util.registerTransport();

			// Get factory portType
			factoryEPR = new EndpointReferenceType();
			factoryEPR.setAddress(new Address(factoryURI));
			Object[] args = new Object[1];
			args[0] = factoryEPR;
			factoryPortType = callObjectMethod(factoryLocator, "get" + svcName
					+ "FactoryPortTypePort", args);

			// Create resource and get endpoint reference of WS-Resource.
			// This resource is our "instance".
			args = new Object[1];
			args[0] = new CreateResource();
			CreateResourceResponse createResponse = (CreateResourceResponse) callObjectMethod(
					factoryPortType, "createResource", args);
			instanceEPR = createResponse.getEndpointReference();

			String endpointString = ObjectSerializer.toString(instanceEPR,
					GSBLQNames.RESOURCE_REFERENCE);

			// Get instance PortType
			args = new Object[1];
			args[0] = instanceEPR;
			instancePortType = callObjectMethod(instanceLocator, "get"
					+ svcName + "PortTypePort", args);

			// instancePortType is basically the handle to our service
			// store the EPR for future use
			/*
			 * args = new Object[1]; args[0] = endpointString;
			 * callMethod("setEPR", args);
			 */

		} catch (Exception e) {
			log.error("Error setting up GSBL client: createService call threw Exception: "
					+ e);
			throw new Exception(e);
		}
		try {
			doFinalClientSetup();
		} catch (Exception e) {
			log.error("Exception: " + e);
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Constructor for the GSBLClient class that connects to an existing grid
	 * service instance.
	 * 
	 * @param svcName
	 *            The name of the grid service, e.g. "Ssearch34".
	 * @param EPR
	 *            The endpoint reference of the remote instance.
	 * @param instanceSvcLocatorClass
	 *            The actual class object of the instance service locator.
	 * @throws Exception
	 *             If the client could not be initialized.
	 */
	public GSBLClient(String svcName, String EPR, Class instanceSvcLocatorClass)
			throws Exception {
		log.debug("Connecting to existing instance at: " + EPR);
		try {
			Object instanceLocator = instanceSvcLocatorClass.newInstance();

			// must be used with secure containers!
			Util.registerTransport();

			instanceEPR = (EndpointReferenceType) ObjectDeserializer
					.deserialize(new InputSource(new StringReader(EPR)),
							EndpointReferenceType.class);

			// Get instance PortType
			Object[] args = new Object[1];
			args[0] = instanceEPR;
			instancePortType = callObjectMethod(instanceLocator, "get"
					+ svcName + "PortTypePort", args);

			doFinalClientSetup();

		} catch (Exception e) {
			log.error("Exception: " + e);
			throw new Exception(e);
		}
	}

	/**
	 * Function called by constructors to finish client setup. Currently, it
	 * simply initializes the resourceID.
	 */

	private void doFinalClientSetup() throws Exception {
		resourceID = getInstanceResourceID();
	}

	// this old method no longer works... it's not clear it was doing anything
	// useful anyway, although the container
	// memory usage should be watched, because not calling destroy might =
	// memory leak

	/*
	 * public synchronized void destroy() throws Exception { try { Object []
	 * args = new Object[1]; args[0] = new Destroy(); callMethod("destroy",
	 * args); } catch (RemoteException RE) {
	 * log.error("Error destroying grid service: " + RE); throw new
	 * Exception(RE.getMessage()); } }
	 */

	/**
	 * This method is used to destroy a Grid service instance.
	 */
	public synchronized void destroy() throws Exception {
		// currently this method is empty...
	}

	/**
	 * A wrapper method for callObjectMethod. Used to call a method on a grid
	 * service instance, using java reflection.
	 * 
	 * @param methodName
	 *            The name of the method to call.
	 * @param args
	 *            An array of arguments to the method.
	 * 
	 * @throws Exception
	 *             If error in callObjectMethod.
	 */
	public Object callMethod(String methodName, Object args[]) throws Exception {
		Object retval = null;
		try {
			callObjectMethod(instancePortType, methodName, args);
		} catch (Exception e) {
			// Logging done by callObjectMethod
			throw new Exception(e);
		}
		return retval;
	}

	/**
	 * Call a method on an object using Java reflection methods.
	 * 
	 * @param object
	 *            The object to call the method on.
	 * @param methodName
	 *            The name of the method to call.
	 * @param arguments
	 *            An array of arguments to pass to the method.
	 * 
	 * @return The object returned from the method.
	 */
	protected static Object callObjectMethod(Object object, String methodName,
			Object[] arguments) throws NoSuchMethodException,
			SecurityException, IllegalAccessException,
			InvocationTargetException {
		Class objectClass = object.getClass();
		Class[] types = new Class[arguments.length];

		// Setup the type array to search with.
		for (int i = 0; i < arguments.length; i++) {
			types[i] = arguments[i].getClass();
		}
		Object retval = null;
		try {
			Method meth = objectClass.getMethod(methodName, types);
			retval = meth.invoke(object, arguments);
		} catch (NoSuchMethodException ME) {
			log.error("No such method " + methodName + " with types of "
					+ types);
			throw ME;
		} catch (SecurityException SE) {
			log.error("Security exception search for methods: "
					+ SE.getMessage());
			throw SE;
		} catch (IllegalAccessException IE) {
			log.error("Illegal access exception calling method " + methodName
					+ ": " + IE.getMessage());
			throw IE;
		} catch (InvocationTargetException ITE) {
			log.error("ERROR: InvocationTargetException calling method "
					+ methodName + ": " + ITE.getMessage());
			ITE.printStackTrace();
			throw ITE;
		}
		return retval;
	}

	/**
	 * Return EndpointReference for this Grid service instance.
	 */
	public EndpointReferenceType getInstanceEPR() {
		return instanceEPR;
	}

	/**
	 * Return the InstancePortType object.
	 */
	public Object getInstancePortType() {
		return instancePortType;
	}

	/**
	 * Extract the resource ID that is embedded in the EPR, and append a random
	 * number to it.
	 */
	public String getInstanceResourceID() {
		ReferenceParametersType rpt = instanceEPR.getParameters();
		ArrayList children = (ArrayList) ((MessageElement) rpt.get(0))
				.getChildren();
		Object anObject = children.get(0);
		Random generator = new Random();
		Double r = new Double(generator.nextDouble());
		String resourceIDString = anObject.toString() + r.toString();

		// make sure string doesn't have any 'E's or '-'s in it
		resourceIDString = resourceIDString.replace("E", "");
		resourceIDString = resourceIDString.replace("-", "");

		return resourceIDString;
	}

	/**
	 * Return the resource ID.
	 */
	public String getResourceID() {
		return resourceID;
	}

	/**
	 * Return the working directory base.
	 */
	public String getWorkingDirBase() {
		return workingDirBase;
	}

	/**
	 * Return the factoryHost.
	 */
	public String getFactoryHost() {
		return factoryHost;
	}

	/**
	 * This function returns the filenames within a directory, checking them
	 * against the number of replicates.
	 * 
	 * @param pathToDir
	 *            path to the directory to read
	 * @param reps
	 *            number of job replicates
	 * 
	 * @return an array of paths to files within the directory
	 */
	public String[] parseDirectory(String pathToDir, int reps) throws Exception {
		File dir = new File(pathToDir);
		if (!dir.exists()) {
			log.error("File " + dir.toString() + " does not exist!");
			System.exit(1);
		}
		String[] returnArray = null;
		if (dir.isDirectory()) {
			String[] filenames = dir.list();
			ArrayList<String> filenames_list = new ArrayList<String>(
					Arrays.asList(filenames));
			Collections.sort(filenames_list, new NaturalOrderComparator());
			filenames = filenames_list.toArray(filenames);

			if (reps <= 1) {
				log.error("--replicates > 1 must be defined!");
				throw new Exception("Replicates > 1 must be defined!");
			} else if (reps > filenames.length) { // there must be at least reps
													// number of files in this
													// directory
				log.error("Number of input files must equal or exceed the value of --replicates!");
				throw new Exception(
						"Number of input files must be equal to or exceed replicates!");
			}
			returnArray = new String[reps];

			for (int i = 0; i < reps; i++) {
				log.debug("returnArray[i] is: " + pathToDir + "/"
						+ filenames[i]);
				returnArray[i] = pathToDir + "/" + filenames[i];
			}
		}
		return returnArray;
	}
}
