/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import edu.umd.umiacs.cummings.GSBL.GSBLResource;

import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.impl.ResourceHomeImpl;
import org.globus.wsrf.impl.SimpleResourceKey;

/**
 * The resource home manages GSBLResources.
 */
public class GSBLResourceHome extends ResourceHomeImpl {

	public ResourceKey create() throws Exception {
		// Create a resource and initialize it
		GSBLResource gsblResource = (GSBLResource) createNewInstance();
		gsblResource.initialize();
		// Get key
		ResourceKey key = new SimpleResourceKey(keyTypeName,
				gsblResource.getID());
		// Add the resource to the list of resources in this home
		add(key, gsblResource);
		return key;
	}

}