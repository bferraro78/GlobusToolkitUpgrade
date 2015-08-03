/**
 * @author Adam Bazinet
 * @author Ben Ferraro
 * @author Jordan Kiesel
 */
package edu.umd.umiacs.cummings.GSBL;

/*
import org.globus.axis.util.Util;

import org.globus.axis.message.addressing.Address;
import org.globus.axis.message.addressing.EndpointReferenceType;
import org.globus.axis.message.addressing.ReferenceParametersType;
import org.apache.axis.message.MessageElement;
*/
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.oasis.wsrf.lifetime.Destroy;
import org.xml.sax.InputSource;
import org.globus.wsrf.encoding.ObjectSerializer;

//import edu.umd.umiacs.cummings.GSBL.GT42GSBLFactoryService_wsdl.CreateResource;
//import edu.umd.umiacs.cummings.GSBL.GT42GSBLFactoryService_wsdl.CreateResourceResponse;

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

	// Associated port types.
	//protected Object factoryPortType;
	//protected Object instancePortType;

	// Endpoint reference for our factory.
	//protected EndpointReferenceType factoryEPR = null;

	// Endpoint reference for our instance.
	//protected EndpointReferenceType instanceEPR = null;

	// The resource ID should be unique.
	protected String jobID = null;

	// The logger.
	static Log log = LogFactory.getLog(GSBLClient.class.getName());

	/**
	 * Constructor for the GSBLClient class that acquires a new service
	 * instance.
	 * 
	 * @param svcName
	 *            The name of the grid service, e.g. "Ssearch34".
	 * @param factorySvcLocatorClass
	 *            The actual class object of the factory service locator.
	 * @param instanceSvcLocatorClass
	 *            The actual class object of the instance service locator.
	 * @throws Exception
	 *             If the client could not be initialized.
	 */

	public GSBLClient(String svcName) throws Exception {

		try {
		    //Object factoryLocator = factorySvcLocatorClass.newInstance();
		    //Object instanceLocator = instanceSvcLocatorClass.newInstance();

			// Must be used with secure containers!
		    //Util.registerTransport();

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
	public GSBLClient(String svcName, String EPR)
			throws Exception {
		log.debug("Connecting to existing instance at: " + EPR);
		try {
			/*
			Object instanceLocator = instanceSvcLocatorClass.newInstance();

			// Must be used with secure containers!
			Util.registerTransport();

			instanceEPR = (EndpointReferenceType) ObjectDeserializer
					.deserialize(new InputSource(new StringReader(EPR)),
							EndpointReferenceType.class);

			// Get instance portType.
			Object[] args = new Object[1];
			args[0] = instanceEPR;
			instancePortType = callObjectMethod(instanceLocator, "get" + svcName
					+ "PortTypePort", args);
			*/

			doFinalClientSetup();

		} catch (Exception e) {
			log.error("Exception: " + e);
			throw new Exception(e);
		}
	}

	/* This function has been moved from GSBLService to GSBLCLient due to the fact
	   that we no longer need to create this working directory on the "service side".
	   All we want to do is create a .output folder and populate it with job replicate 
	   folders. */
	public boolean createWorkingDir(String info) {
		// Break apart info.
		String[] chunks = info.split("@--");
		String unique_id = chunks[0];
		String cwd = chunks[1];
		String hostname = chunks[2];
		int reps = Integer.parseInt(chunks[3]);

		String myWorkingDir = cwd + "/";

		try {

			if (reps > 1) {
				/* If reps > 1, create an 'output' folder in our working
				directory and fill it with sub-job folders. */
				File outputDir =
						new File(myWorkingDir + unique_id + ".output/");
				
				System.out.println("Output Dir");

				try {
					outputDir.mkdir();
					File tempJobDir = null;
					for (int i = 0; i < reps; i++) {
						tempJobDir = new File(myWorkingDir + unique_id
								+ ".output/job" + i + "/");
						tempJobDir.mkdir();
					}
				} catch (Exception e) {
					log.error("Exception: " + e);
				}
			}			
		} catch (Exception e) {
			log.error("Unable to create GSBLService temporary directory: "
					+ myWorkingDir, e);
		}
		return true;
	}


	/**
	 * Function called by constructors to finish client setup. Currently, it
	 * simply initializes the jobID.
	 */
	private void doFinalClientSetup() throws Exception {
		jobID = getInstanceJobID();
	}

	/**
	 * This method is used to destroy a Grid service instance.
	 */
	public synchronized void destroy() throws Exception {
		// Currently this method is empty... .
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
	public Object callMethod(String methodName, Object args[])
			throws Exception {
		Object retval = null;
		try {
			//callObjectMethod(instancePortType, methodName, args);
		} catch (Exception e) {
			// Logging done by "callObjectMethod".
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
			Object[] arguments) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, InvocationTargetException {
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

	/*
	  Return EndpointReference for this Grid service instance.
	public EndpointReferenceType getInstanceEPR() {
		return instanceEPR;
	}
	*/

	/*
	 * Return the InstancePortType object.
	 
	public Object getInstancePortType() {
		return instancePortType;
	}
	*/

	/**
	 * Generate a JobID in this form XXXXXXXXX.XXXXXXXXX
	 */
	public String getInstanceJobID() {
		Random generator = new Random();

		int r = generator.nextInt(900000000) + 100000000;
		Double d = new Double(generator.nextDouble());
		Integer i = r;

		String part1 = i.toString();
		String part2 = d.toString();
	 
		String jobIDString = part1 + part2; 
		return jobIDString;
	}

	/**
	 * Return the job ID.
	 */
	public String getJobID() {
		return jobID;
	}

	/**
	 * Return the working directory base.
	 */
	public String getWorkingDirBase() {
		return workingDirBase;
	}

	/*
	 Return the factoryHost.
	public String getFactoryHost() {
		return factoryHost;
	}
	*/
		
	/**
	 * This function returns the filenames within a directory, checking them
	 * against the number of replicates.
	 * 
	 * @param pathToDir
	 *            Path to the directory to read.
	 * @param reps
	 *            Number of job replicates.
	 * 
	 * @return an array of paths to files within the directory.
	 */
	public String[] parseDirectory(String pathToDir, int reps)
			throws Exception {
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
			} else if (reps > filenames.length) {  /* There must be at least
						reps number of files in this directory */
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
