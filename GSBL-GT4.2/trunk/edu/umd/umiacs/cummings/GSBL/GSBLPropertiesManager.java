/**
 * @author Adam Bazinet
 * @author Daniel Myers
 */
package edu.umd.umiacs.cummings.GSBL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;
import java.io.Serializable;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.Integer;
import java.lang.Character;
import java.lang.String;
import java.lang.reflect.Method;

/**
 * Class for managing and manipulating grid client/service properties files.
 * Files have one key per line in the following format: key @-- value @-- type
 */
public class GSBLPropertiesManager {

	static Log log = LogFactory.getLog(GSBLPropertiesManager.class.getName());
	private Vector properties = new Vector();

	/**
	 * Constructor for a GSBLProperties object. Reads and processes a properties
	 * file.
	 * 
	 * @param filename
	 *            The path to the properties file.
	 * 
	 * @throws Exception
	 *             if the properties file could not be read.
	 */
	public GSBLPropertiesManager(String filename) throws Exception {
		readProperties(new File(filename));
	}

	/**
	 * Return a Collection view of the properties.
	 */
	public Collection getProperties() {
		return properties;
	}

	/**
	 * Update a Java bean from the properties. Assumes that the Bean has get/set
	 * methods for each property (where the first letter of the property has
	 * been upper-cased; e.g., the set method for the foo property is setFoo).
	 * 
	 * @param bean
	 *            the bean to update.
	 * 
	 * @throws Exception
	 *             if updating failed.
	 */
	public void updateJavaBean(Serializable bean) throws Exception {
		Iterator I = getProperties().iterator();
		while (I.hasNext()) {
			GSBLProperty prop = (GSBLProperty) I.next();

			// Compute the name of the method used to set this property in the
			// bean.
			// It will be setPropertyName, except we ensure that the first
			// character of
			// the property is upper-case.
			String setMethodName = "set"
					+ new String(new Character(Character.toUpperCase(prop
							.getKey().charAt(0))) + prop.getKey().substring(1));

			Class beanClass = bean.getClass();

			// Now, we need to examine the type information for this property to
			// figure out the
			// type of the argument to the set method.
			Class argumentType[] = new Class[1];
			Object arguments[] = new Object[1];

			// Record the argument
			if (prop.getType().getName().equals("java.lang.Boolean")) {
				arguments[0] = new Boolean(prop.getValue());
			} else if (prop.getType().getName().equals("java.lang.String")) {
				arguments[0] = (String)prop.getValue();
			} else if (prop.getType().getName().equals("java.lang.Integer")) {
				arguments[0] = new Integer(prop.getValue());
			} else if (prop.getType().getName().equals("java.lang.Double")) {
				arguments[0] = new Double(prop.getValue());
			} else {
				log.error("Unknown property type: '" + prop.getType() + "'");
				throw new Exception("Unknown property type: '" + prop.getType()
						+ "'");
			}

			// Record the type of the argument
			argumentType[0] = arguments[0].getClass();

			// Invoke the set method
			try {
				Method setter = beanClass
						.getMethod(setMethodName, argumentType);
				setter.invoke(bean, arguments);
			} catch (java.lang.NoSuchMethodException nme) {
				log.error("Cannot find set method: " + setMethodName);
				throw new Exception("Unable to update bean: no such method: "
						+ setMethodName);
			} catch (java.lang.IllegalAccessException iae) {
				log.error("Illegal access attempted to method: "
						+ setMethodName);
				throw new Exception(
						"Unable to update bean: no access to method: "
								+ setMethodName);
			}
		}
	}

	/**
	 * Read a properties file from disk.
	 * 
	 * @param propFile
	 *            the file from which to read properties.
	 * @throws Exception
	 *             if the properties file could not be read.
	 */
	protected void readProperties(File propFile) throws Exception {
		try {
			BufferedReader instream = new BufferedReader(new FileReader(
					propFile));
			String line = "";
			while ((line = instream.readLine()) != null) {
				// 5 is the length of " @-- "
				String key = line.substring(0, line.indexOf(" @-- "));
				String restOfLine = line.substring(line.indexOf(" @-- ") + 5);
				String value = restOfLine.substring(0,
						restOfLine.indexOf(" @-- "));
				restOfLine = restOfLine
						.substring(restOfLine.indexOf(" @-- ") + 5);

				Class type = null;
				try {
					type = Class.forName(restOfLine);
				} catch (ClassNotFoundException CE) {
					log.error("Unrecognized type " + type);
					throw new Exception(CE.getMessage());
				}

				GSBLProperty aProp = new GSBLProperty(key, value, type);

				properties.add(aProp);
			}
		} catch (Exception e) {
			log.error("Unable to process properties file '" + propFile + "': "
					+ e.getMessage());
			throw e;
		}
	}

	/** Test driver. */
	public static void main(String args[]) throws Exception {
		GSBLPropertiesManager GPM = new GSBLPropertiesManager(
				"sample.properties");
		MyBean MB = new MyBean();
		GPM.updateJavaBean(MB);
		System.err.println(MB);
	}
}

/** Test java bean */

class MyBean implements Serializable {
	String queryFile;
	int databaseSize;
	double evalue;
	boolean doeswork;

	public void setQueryFile(String val) {
		queryFile = val;
	}

	public String getQueryFile() {
		return queryFile;
	}

	public void setDatabaseSize(int x) {
		databaseSize = x;
	}

	public int getDatabaseSize() {
		return databaseSize;
	}

	public void setEvalue(double x) {
		evalue = x;
	}

	public double getEvalue(int x) {
		return evalue;
	}

	public void setDoeswork(boolean b) {
		doeswork = b;
	}

	public boolean getDoeswork() {
		return doeswork;
	}

	public String toString() {
		return queryFile + " " + databaseSize + " " + evalue + " " + doeswork
				+ "\n";
	}
}
