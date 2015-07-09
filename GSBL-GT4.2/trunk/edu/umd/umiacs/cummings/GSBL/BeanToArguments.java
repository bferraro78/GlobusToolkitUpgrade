/**
 * @author Daniel Myers
 */
package edu.umd.umiacs.cummings.GSBL;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.*;
import org.jdom.xpath.XPath;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.Serializable;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to convert an arguments bean to a command-line arguments string. Uses
 * the XML description of the service's arguments to map bean fields to argument
 * flags.
 */

public class BeanToArguments {

	/**
	 * The namespace of the client description XML documents.
	 */
	static private Namespace tns = Namespace
			.getNamespace("http://cummings.umiacs.umd.edu/GSBL/service_description");

	/**
	 * A map containing records for every option, both flag and non-flag.
	 */
	Map argumentRecords;

	/**
	 * A map containing records only for non-flag options.
	 */
	Map nonFlagArguments;

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(BeanToArguments.class.getName());

	/**
	 * Initialize a BeanToArguments converter.
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
	public BeanToArguments(String descriptionFileName) throws JDOMException,
			IOException, ClassNotFoundException {

		// Setup an XML reader to parse the description file.
		SAXBuilder builder = new SAXBuilder(
				"org.apache.xerces.parsers.SAXParser", true);
		builder.setFeature("http://apache.org/xml/features/validation/schema",
				true);

		Document doc = null;
		try {
			doc = builder.build(descriptionFileName);
		} catch (JDOMException jd) {
			log.error("XML description file " + descriptionFileName
					+ " is not valid: " + jd);
			throw jd;
		} catch (IOException ie) {
			log.error("Unable to open XML description file: " + ie);
			throw ie;
		}

		// Initialize the argument maps
		argumentRecords = new HashMap();
		nonFlagArguments = new HashMap();

		// Ok, now that we've parsed the XML document, we can create an internal
		// representation of the arguments.
		processArgumentDescription(doc);
	}

	/**
	 * Read an XML file describing the arguments and construct an internal
	 * representation of it.
	 * 
	 * @param document
	 *            a JDOM document representing the XML description file.
	 */
	
	private void processArgumentDescription(Document document)
			throws ClassNotFoundException, JDOMException {
		// We use XPath to retrieve all the argument elements.
		// tns: in XPath query specifies the GSBL/client namespace
		XPath xp;
		List arguments;
		try {
			xp = XPath.newInstance("//tns:argument");
			arguments = xp.selectNodes(document.getRootElement());
		} catch (JDOMException je) {
			log.error("Error executing XPath query to retrieve arguments: "
					+ je);
			throw je;
		}
		Iterator I = arguments.iterator();
		while (I.hasNext()) {
			Element argument = (Element) I.next();
			String key = argument.getAttribute("key").getValue();
			String flag = argument.getChildTextNormalize("flag", tns);
			String type = argument.getChildTextNormalize("type", tns);

			// Store this argument
			ArgumentRecord info = new ArgumentRecord(flag, type);
			argumentRecords.put(key, info);

			// If this is a non-flag argument, record it in the nonFlagArguments
			// map.
			// We'll use this later to iterate over the non-flag arguments.
			if (flag.matches("arg[0-9]+")) {
				nonFlagArguments.put(flag, key);
			}
		}
	}

	/**
	 * Create an argument string from a bean. Use the information in the
	 * arguments description file to build an argument string that expresses the
	 * values of the Java Bean.
	 * 
	 * @param bean
	 *            the Java Bean containing argument values.
	 * @throws NoSuchMethodException
	 *             if the bean did not support a key in the description file.
	 * @throws IllegalAccessException
	 *             if there was an error calling a bean accessor method.
	 * @throws InvocationTargetException
	 *             if there was an error calling a bean accessor method.
	 * @throws Exception
	 *             if the argument flag found for a key was of zero length.
	 * @return the command-line arguments as a String.
	 */
	public String getArgumentStringFromBean(Serializable bean)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, Exception {
		Iterator keyIter = argumentRecords.keySet().iterator();
		String argumentStr = "";

		// For each possible argument, we want to check if this
		// particular bean has a value for the argument. If it does,
		// then we'll add the necessary text to the argument string.
		while (keyIter.hasNext()) {
			String key = (String) keyIter.next();
			String flag = getFlagForArgument(key);

			// Skip processing non-flag arguments
			if (flag.matches("arg[0-9]+")) {
				log.debug("Skip processing argument " + flag);
				continue;
			}
			Object beanValue = getBeanValue(bean, key);

			// If the bean has a value for this argument, we go ahead and look
			// up the argument flag.
			if (beanValue != null) {
				String dashes;
				if (flag.length() == 1) {
					dashes = "-";
				} else if (flag.length() > 1) {
					dashes = "--";
				} else {
					log.error("Error: flag has fewer than one character.");
					throw new Exception("Flag has fewer than one character.");
				}

				// If the argument takes a value, then we add the flag and the
				// value to the argument string.
				if (argumentTakesValue(key)) {
					argumentStr += dashes + flag + " \"" + beanValue + "\" ";
				}

				// On the other hand, if it doesn't take a value, then we add
				// the flag to the argument string iff the bean value is "true".
				// We know it will be a Boolean object, so the cast should never
				// fail.
				else {
					Boolean value = (Boolean) beanValue;
					if (value.equals(Boolean.TRUE)) {
						argumentStr += dashes + flag + " ";
					}
				}
			}
		}

		// Now, for each of the non-flag arguments, we add it to the
		// argument string.
		int i = 0;
		String key = null;
		while ((key = (String) nonFlagArguments.get("arg" + i)) != null) {
			Object value = getBeanValue(bean, key);
			// Stop once we see a null command-line argument.
			if (value == null) {
				break;
			}
			argumentStr += "\"" + value + "\" ";
			i++;
		}

		return argumentStr.trim();
	}

	/**
	 * Get the value of a field in a java bean using reflection.
	 * 
	 * @param bean
	 *            the bean to draw from
	 * @param key
	 *            the key to extract
	 * @return the value of the key
	 * @throws NoSuchMethodException
	 *             if the bean did not support the key.
	 * @throws IllegalAccessException
	 *             if there was an error calling the bean accessor method.
	 * @throws InvocationTargetException
	 *             if there was an error calling the bean accessor method.
	 * @throws SecurityException
	 *             if there was an error calling the bean accessor method.
	 * @return the value of the key.
	 */
	protected Object getBeanValue(Serializable bean, String key)
			throws NoSuchMethodException, IllegalAccessException,
			SecurityException, InvocationTargetException {
		// First, we need the name of the get method for the given key (ew).
		String getMethodName = "get"
				+ new String(
						new Character(Character.toUpperCase(key.charAt(0)))
								+ key.substring(1));

		// Now, we need a Method object corresponding to this method.
		Class beanClass = bean.getClass();
		Class[] argumentTypes = new Class[0];
		Method getMethod;
		try {
			getMethod = beanClass.getMethod(getMethodName, null);
		} catch (NoSuchMethodException nme) {
			log.error("Unable to find a Method for method name: "
					+ getMethodName + ": " + nme);
			throw nme;
		} catch (SecurityException se) {
			log.error("Security exception while looking up Method for: "
					+ getMethodName + ": " + se);
			throw se;
		}

		// Now that we have a Method, we can call it.
		Object retval;
		try {
			retval = getMethod.invoke(bean, null);
		} catch (IllegalAccessException iie) {
			log.error("Illegal access to method " + getMethodName + ": " + iie);
			throw iie;
		} catch (InvocationTargetException ite) {
			log.error("InvocationTarget exception calling " + getMethodName
					+ ": " + ite);
			throw ite;
		}
		return retval;
	}

	/**
	 * Return the command-line argument corresponding to a given key. E.g., if
	 * the outputFile key is a synonym for -O (resp. --output-file), will return
	 * O (resp output-file).
	 * 
	 * @param key
	 *            the key of interest
	 * @throws Exception
	 *             if the key was unrecognized.
	 * @return the command-line flag as a String.
	 */
	protected String getFlagForArgument(String key) throws Exception {
		ArgumentRecord info = (ArgumentRecord) argumentRecords.get(key);
		if (info == null) {
			log.error("Could not find an argument record for " + key);
			throw new Exception("Could not find an argument record for " + key);
		}
		return info.getFlag();
	}

	/**
	 * Returns true iff the argument corresponding to key can take a value.
	 * 
	 * @param key
	 *            the key of interest.
	 * @return true iff the key's argument can take a value.
	 * @throws Exception
	 *             if the key was not recognized.
	 */
	protected boolean argumentTakesValue(String key) throws Exception {
		ArgumentRecord info = (ArgumentRecord) argumentRecords.get(key);
		if (info == null) {
			log.error("Could not find an argument record for " + key);
			throw new Exception("Could not find an argument record for " + key);
		}
		return info.takesValue();
	}

	/**
	 * Test driver.
	 */
	public static void main(String args[]) throws Exception {
		MySampleBean MSB = new MySampleBean();
		BeanToArguments B2A = new BeanToArguments(args[0]);
		System.out.println(B2A.getArgumentStringFromBean(MSB));
	}

}

/**
 * Test java bean.
 */
class MySampleBean implements Serializable {
	Boolean showAllSequences;
	Integer maxAlignDisplay;
	Double alignDisplayMaxEval;
	String queryFile;
	String databaseFile;

	public MySampleBean() {
		showAllSequences = new Boolean(true);
		maxAlignDisplay = new Integer(47);
		alignDisplayMaxEval = null;
		queryFile = null; // "/tmp/1.fa";
		databaseFile = "/tmp/2.fa";
	}

	public Boolean getShowAllSequences() {
		return showAllSequences;
	}

	public Integer getMaxAlignDisplay() {
		return maxAlignDisplay;
	}

	public Double getAlignDisplayMaxEval() {
		return alignDisplayMaxEval;
	}

	public String getQueryFile() {
		return queryFile;
	}

	public String getDatabaseFile() {
		return databaseFile;
	}
}

/**
 * Structure for storing information about an argument. Stores twhe flag
 * corresponding to the argument, whether or not the flag takes a value (e.g.,
 * -x foo or just -x), and the type of the value it takes (will be null if there
 * is no value).
 */
class ArgumentRecord {
	String flag;
	Class type;
	boolean takesValue;

	/** Logger */
	static Log log = LogFactory.getLog(ArgumentRecord.class.getName());

	/**
	 * Construct a new argument record with the given flag and type.
	 * 
	 * @param flag
	 *            the flag for the record.
	 * @param the
	 *            type of the flag's argument.
	 * @throws ClassNotFoundException
	 *             if the type was unrecognized.
	 */
	ArgumentRecord(String flag, String type) throws ClassNotFoundException {
		this.flag = flag;
		if (type.equals("none")) {
			takesValue = false;
			try {
				this.type = Class.forName("java.lang.Boolean");
			} catch (ClassNotFoundException ce) {
				log.error("Cannot find a class for java.lang.Boolean (!)");
				throw ce;
			}
		} else {
			takesValue = true;
			try {
				this.type = Class.forName(type);
			} catch (ClassNotFoundException ce) {
				log.error("Unable to find a class for argument type " + type);
				throw ce;
			}
		}
	}

	/**
	 * Get the flag corresponding to this argument.
	 * 
	 * @return the flag
	 */
	String getFlag() {
		return flag;
	}

	/**
	 * Get the type of the value taken by this argument.
	 * 
	 * @return the type
	 */
	Class getType() {
		return type;
	}

	/**
	 * True if this argument takes a value; false otherwise.
	 */
	boolean takesValue() {
		return takesValue;
	}
}
