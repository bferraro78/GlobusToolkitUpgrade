/**
 * @author Daniel Myers
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

// Logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// For XML
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.*;
import org.jdom.xpath.XPath;
import java.util.List;
import java.util.Iterator;

// Exceptions
import java.io.IOException;

/**
 * Class to keep track of runtime configuration for GSBL services. Each class
 * has a static instance of one of these objects that contains the information
 * stored in the service's runtime configuration XML file. At present, the
 * runtime configuration file supports specification of GRAM-related parameters
 * (the executable to run, the scheduler to submit to, and the location of the
 * argument description XML file).
 */
public class GSBLRuntimeConfiguration {

	/**
	 * The location of the argument description file.
	 */
	String argument_description = null;

	/**
	 * The path of the executable.
	 */
	String executable = null;

	/**
	 * The scheduler URI.
	 */
	String scheduler = null;

	/**
	 * The resource the scheduler should use.
	 */
	String resource = null;

	/**
	 * The architecture and operating system.
	 */
	String arch_os = null;

	/**
	 * Requirements for the job's Condor-G classad.
	 */
	String requirements = null;

	/**
	 * Logger.
	 */
	static Log log = LogFactory
			.getLog(GSBLRuntimeConfiguration.class.getName());

	/**
	 * Initialize a runtimeArguments object.
	 * 
	 * @param descriptionFileName
	 *            the name of the XML file describing the arguments.
	 * @throws IOEXception
	 *             if the description file could not be opened.
	 * @throws JDOMException
	 *             if the description file was invalid.
	 * @throws ClassNotFoundException
	 *             if the XML parser could not be initialized.
	 */
	public GSBLRuntimeConfiguration(String descriptionFileName)
			throws JDOMException, IOException, ClassNotFoundException {

		Document doc = null;
		try {
			// Setup an XML reader to parse the description file.
			SAXBuilder builder = new SAXBuilder(
					"org.apache.xerces.parsers.SAXParser", true);
			builder.setFeature(
					"http://apache.org/xml/features/validation/schema", true);
			doc = builder.build(descriptionFileName);
		} catch (JDOMException jd) {
			log.error("XML description file " + descriptionFileName
					+ " is not valid: " + jd);
			throw jd;
		} catch (IOException ie) {
			log.error("Unable to open XML description file: " + ie);
			throw ie;
		} catch (Exception e) {
			log.error("Exception was: " + e);
		}

		try {
			// Parse the XML file to internal structures.
			xmlToInternal(doc);
		} catch (Exception e) {
			log.error("Exception was: " + e);
		}
	}

	/**
	 * Read an XML file describing the arguments and construct an internal
	 * representation of it.
	 * 
	 * @param document
	 *            a JDOM document representing the XML description file.
	 * @throws JDOMException
	 *             if a required field could not be found.
	 */
	private void xmlToInternal(Document document)
			throws ClassNotFoundException, JDOMException {

		// tns: in XPath query specifies the
		// GSBL/runtime_configuration namespace
		XPath xp;
		List nodes;
		Iterator iter;
		/* Determine the location of the argument description file */
		try {
			xp = XPath.newInstance("//tns:argument_description");
			nodes = xp.selectNodes(document.getRootElement());
		} catch (JDOMException je) {
			log.error("Error executing XPath query to retrieve argument description: "
					+ je);
			throw je;
		}
		iter = nodes.iterator();
		while (iter.hasNext()) {
			Element node = (Element) iter.next();
			argument_description = node.getTextNormalize();
		}

		/* Determine the value of the executable field */
		try {
			xp = XPath.newInstance("//tns:executable");
			nodes = xp.selectNodes(document.getRootElement());
		} catch (JDOMException je) {
			log.error("Error executing XPath query to retrieve executable: "
					+ je);
			throw je;
		}
		iter = nodes.iterator();
		while (iter.hasNext()) {
			Element node = (Element) iter.next();
			executable = node.getTextNormalize();
		}

		/* Determine the value of the scheduler field. */
		try {
			xp = XPath.newInstance("//tns:scheduler");
			nodes = xp.selectNodes(document.getRootElement());
		} catch (JDOMException je) {
			log.error("Error executing XPath query to retrieve scheduler: "
					+ je);
			throw je;
		}
		iter = nodes.iterator();
		while (iter.hasNext()) {
			Element node = (Element) iter.next();
			scheduler = node.getTextNormalize();
		}

		/* Determine the value of the resource field. */
		try {
			xp = XPath.newInstance("//tns:resource");
			nodes = xp.selectNodes(document.getRootElement());
		} catch (JDOMException je) {
			log.error("Error executing XPath query to retrieve resource: " + je);
			throw je;
		}
		iter = nodes.iterator();
		while (iter.hasNext()) {
			Element node = (Element) iter.next();
			resource = node.getTextNormalize();
		}

		/* Determine the value of the arch_os field. */
		try {
			xp = XPath.newInstance("//tns:arch_os");
			nodes = xp.selectNodes(document.getRootElement());
		} catch (JDOMException je) {
			log.error("Error executing XPath query to retrieve arch_os: " + je);
			throw je;
		}
		iter = nodes.iterator();
		while (iter.hasNext()) {
			Element node = (Element) iter.next();
			arch_os = node.getTextNormalize();
		}

		/*
		 * Determine the value of the requirements field if we are using
		 * matchmaking
		 */
		if (resource.equalsIgnoreCase("matchmaking")) {
			try {
				xp = XPath.newInstance("//tns:requirements");
				nodes = xp.selectNodes(document.getRootElement());
			} catch (JDOMException je) {
				log.error("Error executing XPath query to retrieve resource: "
						+ je);
				throw je;
			}
			iter = nodes.iterator();
			while (iter.hasNext()) {
				Element node = (Element) iter.next();
				requirements = node.getTextNormalize();
			}
			if (requirements.equals("")) {
				log.error("Configuration file specifies matchmaking but no requirements.");
				throw new JDOMException(
						"Configuration file specifies matchmaking but no requirements.");
			}
		}

	} // End xmlToInternal

	/**
	 * Returns the argument description file.
	 */
	public String getArgumentDescription() {
		return argument_description;
	}

	/**
	 * Returns the path of the executable.
	 */
	public String getExecutable() {
		return executable;
	}

	/**
	 * Returns the scheduler URI.
	 */
	public String getScheduler() {
		return scheduler;
	}

	/**
	 * Returns the resource the scheduler should use.
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * Returns the arch_os string.
	 */
	public String getArchOs() {
		return arch_os;
	}

	/**
	 * Returns the requirements of the job.
	 */
	public String getRequirements() {
		return requirements;
	}

} // End GSBLRuntimeConfiguration
