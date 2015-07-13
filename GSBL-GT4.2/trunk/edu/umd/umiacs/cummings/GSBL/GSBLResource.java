/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

//import java.rmi.RemoteException;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceIdentifier;
import org.globus.wsrf.ResourceProperties;
//import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;
//import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListAccessor;
//import org.globus.wsrf.impl.ResourcePropertyTopic;
//import org.globus.wsrf.impl.SimpleResourceProperty;
import org.globus.wsrf.impl.SimpleResourcePropertySet;
//import org.globus.wsrf.impl.ReflectionResourceProperty;
import org.globus.wsrf.impl.SimpleTopicList;

/**
 * Currently this class exists to satisfy the Globus model. Our services
 * currently do not make use of resource properties, but an example is left
 * commented out for future reference.
 */
public class GSBLResource implements Resource, ResourceIdentifier,
		ResourceProperties, TopicListAccessor {

	/* Resource Property set */
	private ResourcePropertySet propSet;

	/* Resource key. This uniquely identifies this resource. */
	private Object key;

	/* Resource properties */
	// private ResourceProperty jobStatusRP;

	/* Topic list */
	private TopicList topicList;

	/* Initializes RPs and returns a unique identifier for this resource */
	public Object initialize() throws Exception {
		this.key = new Integer(hashCode());
		this.propSet = new SimpleResourcePropertySet(
				GSBLQNames.RESOURCE_PROPERTIES);

		/*
		 * try { jobStatusRP = new
		 * SimpleResourceProperty(GSBLQNames.RP_JOBSTATUS); jobStatusRP.add(new
		 * Integer(1));
		 * 
		 * } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
		 */

		/* Configure the Topics */
		this.topicList = new SimpleTopicList(this);

		/*
		 * jobStatusRP = new ResourcePropertyTopic(jobStatusRP);
		 * ((ResourcePropertyTopic) jobStatusRP).setSendOldValue(true);
		 * 
		 * this.topicList.addTopic((Topic) jobStatusRP);
		 * this.propSet.add(jobStatusRP);
		 */

		return key;
	}

	/* Required by interface ResourceProperties */
	public ResourcePropertySet getResourcePropertySet() {
		return this.propSet;
	}

	/* Required by interface TopicListAccessor */
	public TopicList getTopicList() {
		return topicList;
	}

	/* Get/Setters for the RPs */
	/*
	 * public int getJobStatus() { Integer jobStatus_obj = (Integer)
	 * jobStatusRP.get(0); return jobStatus_obj.intValue(); }
	 */

	/*
	 * public void setJobStatus(int status) { Integer jobStatus_obj = new
	 * Integer(status); jobStatusRP.set(0, jobStatus_obj); }
	 */

	/* Required by interface ResourceIdentifier */
	public Object getID() {
		return this.key;
	}
}