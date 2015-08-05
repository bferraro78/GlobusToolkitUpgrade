/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

/**
 * Class representing a single property (key, value, and type).
 */
public class GSBLProperty {

	/** The key of the property. */
	protected String myKey;

	/** The value of the property. */
	protected String myValue;

	/** The type of the property. */
	protected Class myType;

	/**
	 * Constructor for a GSBLProperties object.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param type
	 *            the type of the value
	 */
	public GSBLProperty(String key, String value, Class type) {
		myKey = key;
		myValue = value;
		myType = type;
	}

	/** Get the key */
	public String getKey() {
		return myKey;
	}

	/** Get the value */
	public String getValue() {
		return myValue;
	}

	/** Get the type */
	public Class getType() {
		return myType;
	}

	/** Set the key */
	public void setKey(String key) {
		myKey = key;
	}

	/** Set the value */
	public void setValue(String value) {
		myValue = value;
	}

	/** Set the type */
	public void setType(Class type) {
		myType = type;
	}

	/** Return a string representation of the property. */
	public String toString() {
		return (myKey + " " + myValue + " " + myType);
	}
}
