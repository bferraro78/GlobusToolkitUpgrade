/*
 * Copyright 1999-2006 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.globus.exec.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.soap.SOAPElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis.message.MessageElement;
import org.globus.axis.message.addressing.AttributedURIType;
import org.globus.axis.message.addressing.EndpointReferenceType;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.SubscribeResponse;
import org.oasis.wsn.FilterType;
import org.oasis.wsrf.resource.ResourceUnknownFaultType;
import org.oasis.wsrf.lifetime.Destroy;
import org.oasis.wsrf.lifetime.SetTerminationTime;
import org.oasis.wsrf.lifetime.SetTerminationTimeResponse;
import org.oasis.wsrf.properties.GetResourcePropertyResponse;
import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
import org.w3c.dom.Element;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.util.Util;
import org.globus.delegation.DelegationConstants;
import org.globus.delegation.DelegationUtil;
import org.globus.delegationService.DelegationPortType;
import org.globus.delegationService.DelegationServiceAddressingLocator;
import org.globus.exec.generated.CreateManagedJobInputType;
import org.globus.exec.generated.CreateManagedJobOutputType;
import org.globus.exec.generated.FaultType;
import org.globus.exec.generated.FaultResourcePropertyType;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.generated.ManagedJobFactoryPortType;
import org.globus.exec.generated.ManagedJobPortType;
import org.globus.exec.generated.MultiJobDescriptionType;
import org.globus.exec.generated.ReleaseInputType;
import org.globus.exec.generated.ServiceLevelAgreementType;
import org.globus.exec.generated.StateChangeNotificationMessageType;
import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.generated.TerminateInputType;
import org.globus.exec.generated.TerminateOutputType;
import org.globus.exec.generated.ResourceNotTerminatedFaultType;
import org.globus.exec.generated.DelegatedCredentialDestroyFaultType;
import org.globus.exec.utils.FaultUtils;
import org.globus.exec.utils.Resources;
import org.globus.exec.utils.ManagedExecutableJobConstants;
import org.globus.exec.utils.ManagedJobConstants;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.audit.AuditUtil;
import org.globus.exec.utils.client.ManagedJobClientHelper;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.exec.utils.rsl.RSLParseException;
import org.globus.exec.utils.NotificationUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.jaas.JaasGssUtil;
import org.globus.rft.generated.DeleteRequestType;
import org.globus.rft.generated.TransferRequestType;
import org.globus.rft.generated.TransferType;
import org.globus.security.gridmap.GridMap;
import org.globus.util.I18n;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.container.ContainerException;
import org.globus.wsrf.container.ServiceContainer;
import org.globus.wsrf.encoding.DeserializationException;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.config.ConfigException;
import org.globus.wsrf.security.authorization.client.Authorization;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.IdentityAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.GSISecureMsgAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSISecureConvAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSITransportAuthMethod;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.utils.XmlUtils;

/**
 * This class represents a simple gram job. It allows for submitting a
 * job,canceling it, sending a signal command and registering and unregistering
 * job state change listeners.
 * 
 * This class hides the middleware API from the consumer.
 */
public class GramJob implements NotifyCallback {

	private static Log logger = LogFactory.getLog(GramJob.class.getName());
	private static I18n i18n = I18n.getI18n(Resources.class.getName());
	private static final String BASE_SERVICE_PATH = "/wsrf/services/";
	private static final HashMap<StateEnumeration, Integer> stateOrdering;
	public static final Integer DEFAULT_MSG_PROTECTION = Constants.SIGNATURE;
	public static final int DEFAULT_TIMEOUT = 300000;
	public static final Authorization DEFAULT_AUTHZ = HostAuthorization
			.getInstance();

	private String securityType = null;
	private Integer msgProtectionType = DEFAULT_MSG_PROTECTION;
	private Authorization authorization = DEFAULT_AUTHZ;
	private Calendar terminationTime = null;

	// holds job credentials
	private GSSCredential proxy = null;
	private boolean limitedDelegation = true;
	private boolean delegationEnabled = false;
	private boolean personal = false;

	private JobDescriptionType jobDescription;
	private EndpointReferenceType jobEndpointReference;
	private String jobHandle;
	private String id = null;

	// job status:
	private FaultType[] fault;
	private StateEnumeration state;
	private Object stateMonitor = new Object();
	private boolean holding;
	private int error;
	private int exitCode;
	private Vector listeners;
	private boolean useDefaultNotificationConsumer = true;
	private NotificationConsumerManager notificationConsumerManager;
	private EndpointReferenceType notificationConsumerEPR;
	private EndpointReferenceType notificationProducerEPR;
	private int axisStubTimeOut;

	protected EndpointReferenceType delegationFactoryEndpoint = null;
	protected EndpointReferenceType stagingDelegationFactoryEndpoint = null;

	static {
		Util.registerTransport();
		stateOrdering = new HashMap<StateEnumeration, Integer>();
		// add the external states and give them an order
		stateOrdering.put(StateEnumeration.Unsubmitted, new Integer(0));
		stateOrdering.put(StateEnumeration.StageIn, new Integer(1));
		stateOrdering.put(StateEnumeration.Pending, new Integer(2));
		stateOrdering.put(StateEnumeration.Active, new Integer(3));
		stateOrdering.put(StateEnumeration.Suspended, new Integer(4));
		stateOrdering.put(StateEnumeration.StageOut, new Integer(5));
		stateOrdering.put(StateEnumeration.CleanUp, new Integer(6));
		// final states
		stateOrdering.put(StateEnumeration.UserTerminateFailed, new Integer(7));
		stateOrdering.put(StateEnumeration.UserTerminateDone, new Integer(8));
		stateOrdering.put(StateEnumeration.Done, new Integer(9));
		stateOrdering.put(StateEnumeration.Failed, new Integer(10));
	}

	/**
	 * Creates a gram job with no RSL. This default constructor is used in
	 * conjunction with {@link #setEndpoint()}.
	 */
	public GramJob() {

		this.state = null;
		this.holding = false;
		this.axisStubTimeOut = DEFAULT_TIMEOUT;
	}

	/**
	 * Creates a gram job with specified job description.
	 */
	public GramJob(JobDescriptionType jobDescription) {

		this();
		try {
			this.jobDescription = (JobDescriptionType) ObjectSerializer
					.clone(jobDescription);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a gram job with specified file containing the xml jdd.
	 * 
	 * @param rslFile
	 *            file with job specification
	 */
	public GramJob(File rslFile) throws RSLParseException,
			FileNotFoundException {

		// reading GT4 RSL
		this(RSLHelper.readRSL(rslFile));

	}

	/**
	 * Creates a gram job with specified rsl.
	 * 
	 * Currently the rsl is required to be in the new XML-based form. For
	 * backwords compatibility this should accept the old format, but the
	 * conversion algorithm isn't in place yet.
	 * 
	 * @param rsl
	 *            resource specification string
	 */
	public GramJob(String rsl) throws RSLParseException {

		// reading GT4 RSL
		this(RSLHelper.readRSL(rsl));
	}

	/**
	 * Add a listener to the GramJob. The listener will be notified whenever the
	 * state of the GramJob changes.
	 * 
	 * @param listener
	 *            The object that wishes to receive state updates.
	 * @see org.globus.gram.GramJobListener
	 */
	public void addListener(GramJobListener listener) {

		if (listeners == null)
			listeners = new Vector();
		listeners.addElement(listener);
	}

	/**
	 * Remove a listener from the GramJob. The listener will no longer be
	 * notified of state changes for the GramJob.
	 * 
	 * @param listener
	 *            The object that wishes to stop receiving state updates.
	 * @see org.globus.gram.GramJobListener
	 */
	public void removeListener(GramJobListener listener) {

		if (listeners == null)
			return;
		listeners.removeElement(listener);
	}

	/**
	 * Gets the credentials of this job.
	 * 
	 * @return job credentials. If null none were set.
	 * 
	 */
	public GSSCredential getCredentials() {

		return this.proxy;
	}

	/**
	 * Sets credentials of the job
	 * 
	 * @param newProxy
	 *            user credentials
	 * @throws IllegalArgumentException
	 *             if credentials are already set
	 */
	public void setCredentials(GSSCredential newProxy) {

		if (this.proxy != null) {
			throw new IllegalArgumentException("Credentials already set");
		} else {
			this.proxy = newProxy;
		}
	}

	/**
	 * Query the MJFS for delegation factory endpoints
	 */
	public EndpointReferenceType[] fetchDelegationFactoryEndpoints(
			EndpointReferenceType factoryEndpoint) throws Exception {

		ManagedJobFactoryPortType factoryPort = getManagedJobFactoryPortType(factoryEndpoint);

		GetMultipleResourceProperties_Element request = new GetMultipleResourceProperties_Element();
		request.setResourceProperty(new QName[] {
				ManagedJobFactoryConstants.RP_DELEGATION_FACTORY_ENDPOINT,
				ManagedJobFactoryConstants.RP_STAGING_DELEGATION_FACTORY_ENDPOINT });
		GetMultipleResourcePropertiesResponse response = factoryPort
				.getMultipleResourceProperties(request);

		SOAPElement[] any = response.get_any();

		EndpointReferenceType[] endpoints = new EndpointReferenceType[] {
				(EndpointReferenceType) ObjectDeserializer.toObject(any[0],
						EndpointReferenceType.class),
				(EndpointReferenceType) ObjectDeserializer.toObject(any[1],
						EndpointReferenceType.class) };

		return endpoints;
	}

	public void prependBaseURLtoStageInSources(String baseURL) {

		TransferRequestType stageInDirectives = this.jobDescription
				.getFileStageIn();
		if (stageInDirectives != null) {
			TransferType[] transferArray = stageInDirectives.getTransfer();
			for (int i = 0; i < transferArray.length; i++) {
				String source = transferArray[i].getSourceUrl();
				transferArray[i].setSourceUrl(catenate(baseURL, source));
			}
		} else {
			logger.debug("no stage in directives");
		}
	}

	public void prependBaseURLtoStageOutDestinations(String baseURL) {

		TransferRequestType stageOutDirectives = this.jobDescription
				.getFileStageOut();
		if (stageOutDirectives != null) {
			TransferType[] transferArray = stageOutDirectives.getTransfer();
			for (int i = 0; i < transferArray.length; i++) {
				String source = transferArray[i].getDestinationUrl();
				transferArray[i].setDestinationUrl(catenate(baseURL, source));
			}
		} else {
			logger.debug("no stage out directives");
		}
	}

	/**
	 * <b>Precondition</b>the job has not been submitted
	 * 
	 * @param path
	 *            String
	 */
	/*
	 * public void setDryRun(boolean enabled) {
	 * this.jobDescription.setDryRun(new Boolean(enabled)); }
	 */

	/**
	 * @return the job description
	 */
	public JobDescriptionType getDescription() throws Exception {

		if (this.jobDescription == null) {
			refreshRSLAttributes();
		}
		return this.jobDescription;
	}

	/**
	 * Get the current state of this job.
	 * 
	 * @return current job state
	 */
	public StateEnumeration getState() {

		return this.state;
	}

	public boolean isHolding() {

		return this.holding;
	}

	/**
	 * Submits an interactive i.e. non-batch job with limited delegation
	 * 
	 * @see #request(String, String, boolean, boolean) for explanation of
	 *      parameters
	 */
	public void submit(EndpointReferenceType factoryEndpoint) throws Exception {

		submit(factoryEndpoint, false, true, null);
	}

	/**
	 * Submits a job with limited delegation.
	 * 
	 * @see #request(URL, String, boolean, boolean) for explanation of
	 *      parameters
	 */
	public void submit(EndpointReferenceType factoryEndpoint, boolean batch)
			throws Exception {

		submit(factoryEndpoint, batch, true, null);
	}

	/**
	 * @todo add throws ...Exception for invalid credentials? Submits a job to
	 *       the specified service either as an interactive or batch job. It can
	 *       perform limited or full delegation.
	 * 
	 * @param factoryEndpoint
	 *            the resource manager service endpoint. The service address can
	 *            be specified in the following ways: <br>
	 *            host <br>
	 *            host:port <br>
	 *            host:port/service <br>
	 *            host/service <br>
	 *            host:/service <br>
	 *            host::subject <br>
	 *            host:port:subject <br>
	 *            host/service:subject <br>
	 *            host:/service:subject <br>
	 *            host:port/service:subject <br>
	 * 
	 * @param factoryEndpoint
	 *            the endpoint reference to the job factory service
	 * @param batch
	 *            specifies if the job should be submitted as a batch job.
	 * @param limitedDelegation
	 *            true for limited delegation, false for full delegation.
	 * @param jobId
	 *            For reliable service instance creation, use the specified
	 *            jobId to allow repeated, reliable attempts to submit the job
	 *            submission in the presence of an unreliable transport.
	 * 
	 * @see #request(String) for detailed resource manager contact
	 *      specification.
	 */
	public void submit(EndpointReferenceType factoryEndpoint, boolean batch,
			boolean limitedDelegation, String jobId) throws Exception {

		this.id = jobId;
		this.limitedDelegation = limitedDelegation;

		EndpointReferenceType factoryEndpointOverride = this.jobDescription
				.getFactoryEndpoint();
		if (factoryEndpointOverride != null) {
			factoryEndpoint = factoryEndpointOverride;
		} else {
			this.jobDescription.setFactoryEndpoint(factoryEndpoint);
		}

		if (factoryEndpoint != null) {
			setSecurityTypeFromEndpoint(factoryEndpoint);

			if (isDelegationEnabled()) {
				populateJobDescriptionEndpoints(factoryEndpoint);
			}
		}

		ManagedJobFactoryPortType factoryPort = getManagedJobFactoryPortType(factoryEndpoint);
		this.jobEndpointReference = createJobEndpoint(factoryPort, batch);
	}

	/**
	 * Returns true if the job has been requested. Useful to determine if
	 * terminate() can be called when it is not obvious.
	 */
	public boolean isRequested() {

		// TODO see if can replace with check on state (== ACTIVE?)
		return this.jobEndpointReference != null;
	}

	public void setPersonal(boolean personal) {

		this.personal = personal;
	}

	public boolean isPersonal() {

		return this.personal;
	}

	/**
	 * Registers a callback listener for this job. (Reconnects to the job)
	 * <b>Precondition</b> this.jobEndpointReference != null
	 * 
	 * @throws GramException
	 *             if error occurs during job registration.
	 * @throws GSSException
	 *             if user credentials are invalid.
	 */
	public void bind() throws Exception {

		logger.debug("subscribe() called");
		if (this.useDefaultNotificationConsumer) {
			setupNotificationConsumerManager();
		}

		// consumer started listening --> unsubscribe() may be called to recover
		try {
			if (this.useDefaultNotificationConsumer) {
				setupNotificationConsumer();
			}

			Subscribe request = new Subscribe();
			request.setConsumerReference(this.notificationConsumerEPR);
			TopicExpressionType topicExpression = new TopicExpressionType();
			topicExpression.setDialect(WSNConstants.SIMPLE_TOPIC_DIALECT);
			topicExpression
					.setValue(ManagedJobConstants.STATE_CHANGE_INFORMATION_TOPIC_QNAME);
			MessageElement element = (MessageElement) ObjectSerializer
					.toSOAPElement(topicExpression,
							WSNConstants.TOPIC_EXPRESSION);
			FilterType filter = new FilterType();
			filter.set_any(new MessageElement[] { element });
			request.setFilter(filter);

			ManagedJobPortType jobPort = ManagedJobClientHelper
					.getPort(this.jobEndpointReference);
			setStubSecurityProperties((Stub) jobPort);

			SubscribeResponse response = jobPort.subscribe(request);
			EndpointReferenceType subscriptionEndpoint = response
					.getSubscriptionReference();
			if (subscriptionEndpoint != null) {
				this.notificationProducerEPR = (EndpointReferenceType) ObjectSerializer
						.clone(subscriptionEndpoint);
			} else {
				this.notificationProducerEPR = null;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("notification producer endpoint:\n"
						+ this.notificationProducerEPR);
			}
		} catch (Exception e) {
			// may happen...? Let's not fail.
			logger.error(e);
			try {
				unbind();
			} catch (Exception unsubscribeE) {
				// let's not fail the unbind
				logger.error(unsubscribeE);
			}
		}
	}

	/**
	 * Unregisters a callback listener for this job. (disconnects from the job)
	 * Note: subscription resources are not explicitly destroyed because the are
	 * destroyed automatically on the server-side if the job resource is
	 * destroyed.
	 */
	public void unbind() throws NoSuchResourceException, Exception {

		// remove notification consumer, but only if the default
		// notification consumer is used
		if (this.notificationConsumerEPR != null
				&& this.useDefaultNotificationConsumer) {
			logger.debug("removing the notification consumer");
			if (this.notificationConsumerManager != null) {
				this.notificationConsumerManager
						.removeNotificationConsumer(notificationConsumerEPR);
				this.notificationConsumerEPR = null;
			}
		}

		// stop notification consumer manager
		if (this.notificationConsumerManager != null
				&& this.notificationConsumerManager.isListening()) {
			this.notificationConsumerManager.stopListening();
		}
	}

	/**
	 * Cancels a job.
	 * 
	 * @deprecated
	 */
	public void cancel() throws Exception {

		this.destroy();
	}

	/**
	 * Cancels a job.
	 * 
	 * @deprecated
	 */
	public void destroy() throws Exception {

		boolean destroyAfterCleanup = true;
		boolean continueNotifying = false;
		this.terminate(destroyAfterCleanup, continueNotifying,
				this.isDelegationEnabled());
		this.unbind();
	}

	/**
	 * Terminate a job.
	 * 
	 * @param destroyAfterCleanup
	 * @param continueNotifying
	 * @param destroyDelegatedCredentials
	 * @return
	 * @throws ResourceUnknownFaultType
	 * @throws DelegatedCredentialDestroyFaultType
	 * @throws ResourceNotTerminatedFaultType
	 */
	public synchronized boolean terminate(boolean destroyAfterCleanup,
			boolean continueNotifying, boolean destroyDelegatedCredentials)
			throws ResourceUnknownFaultType,
			DelegatedCredentialDestroyFaultType, ResourceNotTerminatedFaultType {

		boolean terminateCompleted = false;

		try {
			ManagedJobPortType jobPort = ManagedJobClientHelper
					.getPort(this.jobEndpointReference);
			setStubSecurityProperties((Stub) jobPort);

			if (logger.isDebugEnabled()) {
				logger.debug("Calling terminate on jobEndpointReference:\n"
						+ ObjectSerializer.toString(this.jobEndpointReference,
								ManagedJobConstants.ENDPOINT_REFERENCE_QNAME));
			}

			TerminateOutputType response = jobPort
					.terminate(new TerminateInputType(destroyAfterCleanup,
							continueNotifying, destroyDelegatedCredentials));
			terminateCompleted = response.isTerminationCompleted();
		} catch (ResourceUnknownFaultType e) {
			throw e;
		} catch (DelegatedCredentialDestroyFaultType e) {
			throw e;
		} catch (ResourceNotTerminatedFaultType e) {
			throw e;
		} catch (Exception e) {
			ResourceNotTerminatedFaultType fault = new ResourceNotTerminatedFaultType();
			FaultHelper faultHelper = new FaultHelper(fault);
			String errorMessage = i18n.getMessage(
					Resources.RESOURCE_TERMINATION_ERROR, e);
			faultHelper.setDescription(errorMessage);
			faultHelper.addFaultCause(e);
			throw fault;
		}
		return terminateCompleted;
	}

	public void destroyDelegatedCredentials() throws Exception {

		if (this.jobDescription != null) {
			// destroy the job credential
			EndpointReferenceType jobCredentialEndpoint = this.jobDescription
					.getJobCredentialEndpoint();
			if (jobCredentialEndpoint != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Calling destroy on jobCredentialEndpoint:\n"
							+ ObjectSerializer
									.toString(
											jobCredentialEndpoint,
											ManagedJobConstants.ENDPOINT_REFERENCE_QNAME));
				}
				destroyDelegatedCredential(jobCredentialEndpoint);
			}

			// not destroying staging credential because this client sets it
			// to the same value as the job credential

			// destroy the credential to RFT
			destroyTransferDelegatedCredential(this.jobDescription);

			// destroy sub-job delegated credentials if multi-job
			if (this.jobDescription instanceof MultiJobDescriptionType) {
				JobDescriptionType[] subJobDescriptions = ((MultiJobDescriptionType) this.jobDescription)
						.getJob();
				for (int index = 0; index < subJobDescriptions.length; index++) {
					EndpointReferenceType subJobCredentialEndpoint = subJobDescriptions[index]
							.getJobCredentialEndpoint();
					if (subJobCredentialEndpoint != null) {
						destroyDelegatedCredential(subJobCredentialEndpoint);
					}
					// destroy the credential to RFT
					destroyTransferDelegatedCredential(subJobDescriptions[index]);
				}
			}
		}
	}

	public void release() throws Exception {

		ManagedJobPortType jobPort = ManagedJobClientHelper
				.getPort(this.jobEndpointReference);

		setStubSecurityProperties((Stub) jobPort);

		org.apache.axis.client.Stub s = (org.apache.axis.client.Stub) jobPort;
		s.setTimeout(this.axisStubTimeOut);

		jobPort.release(new ReleaseInputType());
	}

	/**
	 * Sets the error code of the job. Note: User should not use this method.
	 * 
	 * @param code
	 *            error code
	 */
	protected void setError(int code) {

		this.error = error;
	}

	/**
	 * Gets the error of the job.
	 * 
	 * @return error number of the job.
	 */
	public int getError() {

		return error;
	}

	/**
	 * Return information about the cause of a job failure (when
	 * <code>getStateAsString.equals(StateEnumeration._Failed)</code>)
	 */
	public FaultType[] getFault() {

		return this.fault;
	}

	/**
	 * <b>Precondition</b>: isRequested()
	 */
	public EndpointReferenceType getEndpoint() {

		return this.jobEndpointReference;
	}

	public void setEndpoint(EndpointReferenceType endpoint) throws Exception {

		this.jobEndpointReference = endpoint;
		if (this.jobEndpointReference != null) {
			setSecurityTypeFromEndpoint(this.jobEndpointReference);
		}
	}

	public String getID() {

		return this.id;
	}

	/**
	 * Can be used instead of {@link #getEndpointReference} <b>Precondition</b>:
	 * isRequested()
	 */
	public String getHandle() {

		if (this.jobHandle == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Generating handle from endpoint "
						+ this.jobEndpointReference);
			}
			this.jobHandle = ManagedJobClientHelper.getHandle( // ResolverUtils.getResourceHandle(
					this.jobEndpointReference);
			if (logger.isDebugEnabled()) {
				logger.debug("New handle: " + this.jobHandle);
			}
		}
		return this.jobHandle;
	}

	/**
	 * Can be used instead of {@link #setEndpointReference}
	 */
	public void setHandle(String handle) throws Exception {

		this.jobHandle = handle;
		if (this.jobHandle != null) {
			this.jobEndpointReference = ManagedJobClientHelper
					.getEndpoint(handle);
			setSecurityTypeFromEndpoint(this.jobEndpointReference);
		}
	}

	public int getExitCode() {

		return exitCode;
	}

	/**
	 * Set timeout for HTTP socket. Default is 120000 (2 minutes).
	 * 
	 * @param timeout
	 *            the timeout value, in milliseconds.
	 */
	public void setTimeOut(int timeout) {

		this.axisStubTimeOut = timeout;
	}

	/**
	 * Returns string representation of this job.
	 * 
	 * @return string representation of this job. Useful for debugging.
	 */
	public String toString() {

		String jobDescString = "RSL: ";
		JobDescriptionType jobDesc = null;
		try {
			jobDesc = this.getDescription();
		} catch (Exception e) {
			String errorMessage = i18n
					.getMessage(Resources.FETCH_JOB_DESCRIPTION_ERROR);
			logger.error(errorMessage, e);
		}
		if (jobDesc != null) {
			jobDescString += RSLHelper.convertToString(jobDesc);
		}
		return jobDescString;
		/**
		 * @todo print ID of job (resource key?)
		 */
	}

	/**
	 * Deliver the notification message
	 * 
	 * @param topicPath
	 *            The topic path for the topic that generated the notification
	 * @param producer
	 *            The producer endpoint reference
	 * @param message
	 *            The notification message
	 */
	public void deliver(List topicPath, EndpointReferenceType producer,
			Object message) {

		if (logger.isDebugEnabled()) {
			logger.debug("receiving notification");
			if (message instanceof Element) {
				logger.debug("message is of type "
						+ message.getClass().getName());
				logger.debug("message contents: \n"
						+ XmlUtils.toString((Element) message));
			}
		}

		try {
			StateChangeNotificationMessageType changeNotification = NotificationUtil
					.getStateChangeNotification(message);
			StateEnumeration state = changeNotification.getState();
			boolean holding = changeNotification.isHolding();
			if (state.equals(StateEnumeration.Failed)) {
				setFault(FaultUtils.getConcreteFaults(changeNotification
						.getFault()));
			}
			if (state.equals(StateEnumeration.StageOut)
					|| state.equals(StateEnumeration.Done)
					|| state.equals(StateEnumeration.Failed)) {
				this.exitCode = changeNotification.getExitCode();

				if (logger.isDebugEnabled()) {
					logger.debug("Setting exit code to "
							+ Integer.toString(exitCode));
				}
			}

			synchronized (this.stateMonitor) {
				if ((this.notificationConsumerManager != null)
						&& !this.notificationConsumerManager.isListening()) {
					return;
				}

				setState(state, holding);
			}
		} catch (Exception e) {
			String errorMessage = "Notification message processing FAILED:"
					+ "Could not get value or set new status.";
			logger.error(errorMessage, e);
			// no propagation of error here?
		}
	}

	/**
	 * Asks the job service for the status of a job, i.e. its state and the
	 * cause if the state is 'Failed'. This is useful when subscribing to
	 * notifications is impossible but an immediate result is needed.
	 * <b>Precondition</b>job has been submitted
	 * 
	 * @throws Exception
	 *             if the service data cannot be fetched or the job state not
	 *             extracted from the data.
	 */
	public void refreshStatus() throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("refreshing state of job with endpoint "
					+ this.jobEndpointReference);
		}

		boolean singleJob = isSingleJob();
		int stateIndex = 0;
		int holdingIndex = 1;
		int exitCodeIndex = 2;

		ManagedJobPortType jobPort = ManagedJobClientHelper
				.getPort(this.jobEndpointReference);
		setStubSecurityProperties((Stub) jobPort);
		GetMultipleResourceProperties_Element multiRpRequest = new GetMultipleResourceProperties_Element();

		if (singleJob) {
			if (logger.isDebugEnabled()) {
				logger.debug("Including exitCode in the RP query.");
			}
			multiRpRequest.setResourceProperty(new QName[] {
					ManagedJobConstants.RP_STATE,
					ManagedJobConstants.RP_HOLDING,
					ManagedExecutableJobConstants.RP_EXIT_CODE });
		} else {
			multiRpRequest.setResourceProperty(new QName[] {
					ManagedJobConstants.RP_STATE,
					ManagedJobConstants.RP_HOLDING });
		}
		GetMultipleResourcePropertiesResponse response = jobPort
				.getMultipleResourceProperties(multiRpRequest);
		SOAPElement[] any = response.get_any();

		logger.debug("Deserializing \"state\".");
		StateEnumeration state = (StateEnumeration) ObjectDeserializer
				.toObject(any[stateIndex], StateEnumeration.class);

		logger.debug("Deserializing \"holding\".");
		Boolean holding = (Boolean) ObjectDeserializer.toObject(
				any[holdingIndex], Boolean.class);

		if (singleJob && any.length == 3) {
			logger.debug("Deserializing \"exitCode\".");
			Integer exitCodeWrapper = (Integer) ObjectDeserializer.toObject(
					any[exitCodeIndex], Integer.class);
			this.exitCode = exitCodeWrapper.intValue();
		}

		// Get the fault RP only if we are in a failed state. Note that the
		// RP fault may already contain a fault if the job is in an earlier
		// job and not yet in a final state. But in this case we don't get the
		// fault because it might be confusing if a job is e.g. in state Submit
		// AND has a fault
		if (state.equals(StateEnumeration.Failed)
				|| state.equals(StateEnumeration.UserTerminateFailed)) {
			GetResourcePropertyResponse singleRpResponse = jobPort
					.getResourceProperty(ManagedJobConstants.RP_FAULT);
			any = singleRpResponse.get_any();

			if (any != null && any.length > 0) {
				FaultType[] fault = new FaultType[any.length];
				logger.debug("Deserializing \"fault\".");
				for (int i = 0; i < any.length; i++) {
					fault[i] = deserializeFaultRP(any[i]);
				}
				// set the fault
				this.setFault(fault);
			}
		}

		synchronized (this.stateMonitor) {
			this.setState(state, holding.booleanValue());
		}
	}

	public static List getJobs(EndpointReferenceType factoryEndpoint)
			throws Exception {

		throw new RuntimeException("NOT IMPLEMENTED YET");
	}

	public void setAuthorization(Authorization authz) {

		this.authorization = authz;
	}

	public Authorization getAuthorization() {

		return this.authorization;
	}

	public void setSecurityType(String securityType) {

		this.securityType = securityType;
	}

	public String getSecurityType() {

		return this.securityType;
	}

	public void setMessageProtectionType(Integer protectionType) {

		this.msgProtectionType = protectionType;
	}

	public Integer getMessageProtectionType() {

		return this.msgProtectionType;
	}

	public String getDelegationLevel() {

		return (this.limitedDelegation) ? GSIConstants.GSI_MODE_LIMITED_DELEG
				: GSIConstants.GSI_MODE_FULL_DELEG;
	}

	public void setDelegationEnabled(boolean delegationEnabled) {

		this.delegationEnabled = delegationEnabled;
	}

	public boolean isDelegationEnabled() {

		return this.delegationEnabled;
	}

	/**
	 * Set the duration of a job, specified in hours and minutes. This is a
	 * convenience function if someone does not want to set the termination time
	 * by providing a date.
	 * 
	 * @param hours
	 *            Hours to add to the Calendar.HOUR field of the Calendar
	 *            terminationTime
	 * @param minutes
	 *            Minutes to add to the Calendar.MINUTE field of the Calendar
	 *            terminationTime
	 */
	public synchronized void setDuration(int hours, int minutes) {

		this.terminationTime = Calendar.getInstance();
		this.terminationTime.add(Calendar.HOUR, hours);
		this.terminationTime.add(Calendar.MINUTE, minutes);
	}

	/**
	 * Set the termination time of a job.
	 * 
	 * @param date
	 *            The date/time desired for termination of this job service. If
	 *            the value is null then the termination time will be set to
	 *            now.
	 */
	public synchronized void setTerminationTime(Date date) {

		if (this.terminationTime == null) {
			this.terminationTime = Calendar.getInstance();
		}

		if (date != null) {
			this.terminationTime.setTime(date);
		} else {
			this.terminationTime.setTime(Calendar.getInstance().getTime());
		}
	}

	/**
	 * Get the termination time of the job.
	 */
	public synchronized Calendar getTerminationTime() {

		return this.terminationTime;
	}

	/**
	 * Set TerminationTime RP of managed job service based on parameters
	 * specified as JavaBean properties on this object. <b>Precondition</b>job
	 * has been requested
	 * 
	 * @throws Exception
	 * @return Calendar
	 */
	public void setServiceTerminationTime() throws Exception {

		Calendar terminationTime = this.getTerminationTime();
		SetTerminationTime request = new SetTerminationTime();
		request.setRequestedTerminationTime(terminationTime);

		SetTerminationTimeResponse response = ManagedJobClientHelper.getPort(
				this.jobEndpointReference).setTerminationTime(request);

		if (logger.isDebugEnabled()) {
			Calendar newTermTime = response.getNewTerminationTime();
			logger.debug("requested: " + terminationTime.getTime());
			logger.debug("scheduled: " + newTermTime.getTime());
		}
	}

	public boolean isSingleJob() {

		AttributedURIType address = this.jobEndpointReference.getAddress();
		String path = address.getPath();
		if (path.indexOf("ManagedExecutableJobService") > 0) {
			return true;
		}

		return false;
	}

	public boolean isMultiJob() {

		AttributedURIType address = this.jobEndpointReference.getAddress();
		String path = address.getPath();
		if (path.indexOf("ManagedMultiJobService") > 0) {
			return true;
		}

		return false;
	}

	public EndpointReferenceType getNotificationConsumerEPR() {

		return notificationConsumerEPR;
	}

	public void setNotificationConsumerEPR(
			EndpointReferenceType notificationConsumerEPR) {

		this.useDefaultNotificationConsumer = false;
		try {
			if (notificationConsumerEPR != null) {
				this.notificationConsumerEPR = (EndpointReferenceType) ObjectSerializer
						.clone(notificationConsumerEPR);
			} else {
				this.notificationConsumerEPR = null;
			}
		} catch (Exception e) {
			this.notificationConsumerEPR = notificationConsumerEPR;
		}
	}

	/**
	 * Gets submitted RSL from remote Managed Job Service. It is actually not
	 * only the final, but substituted RSL. To obtain it call
	 * <code>getJDDAttributes</code> afterwards. <b>Precondition</b>job has been
	 * submitted
	 */
	private void refreshRSLAttributes() throws Exception {

		ManagedJobPortType jobPort = ManagedJobClientHelper
				.getPort(this.jobEndpointReference);

		setStubSecurityProperties((Stub) jobPort);

		GetResourcePropertyResponse response = jobPort
				.getResourceProperty(ManagedJobConstants.RP_SERVICE_LEVEL_AGREEMENT);

		SOAPElement[] any = response.get_any();
		ServiceLevelAgreementType sla = (ServiceLevelAgreementType) ObjectDeserializer
				.toObject(any[0], ServiceLevelAgreementType.class);
		this.jobDescription = sla.getJob();
		if (this.jobDescription == null) {
			this.jobDescription = sla.getMultiJob();
		}
	}

	private boolean checkStateUpdate(StateEnumeration newState) {

		if (stateOrdering.containsKey(state)) {
			return (((Integer) stateOrdering.get(getState())).intValue() < ((Integer) stateOrdering
					.get(newState)).intValue()) ? true : false;
		} else {
			logger.warn("notification message with unknown state");
			// update state to get an error message
			return true;
		}
	}

	/**
	 * Sets the state of the job and update the local state listeners. Users
	 * should not call this function. <b>Precondition</b>state != null
	 * 
	 * @param state
	 *            state of the job
	 */
	private void setState(StateEnumeration state, boolean holding) {

		// ignore less advanced state notifications
		if (this.state != null && !checkStateUpdate(state)) {
			return;
		}

		this.state = state;
		this.holding = holding;

		if (listeners == null) {
			return;
		}
		int size = listeners.size();
		for (int i = 0; i < size; i++) {
			GramJobListener listener = (GramJobListener) listeners.elementAt(i);
			listener.stateChanged(this);
		}
	}

	private void setSecurityTypeFromEndpoint(EndpointReferenceType epr) {

		if (this.securityType != null) {
			return;
		}

		if (epr.getAddress().getScheme().equals("http")) {
			if (logger.isDebugEnabled()) {
				logger.debug("using secure conversation");
			}
			this.securityType = Constants.GSI_SEC_CONV;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("using transport-level security");
			}
			this.securityType = Constants.GSI_TRANSPORT;
		}
	}

	/**
	 * Delegate credentials and populate the job description with the EPRs of
	 * the delegated credentials
	 */
	private void populateJobDescriptionEndpoints(
			EndpointReferenceType mjFactoryEndpoint) throws Exception {

		// get job delegation factory endpoints
		EndpointReferenceType[] delegationFactoryEndpoints = fetchDelegationFactoryEndpoints(mjFactoryEndpoint);

		// delegate to single/multi-job
		EndpointReferenceType delegationEndpoint = delegate(
				delegationFactoryEndpoints[0], this.limitedDelegation);
		this.jobDescription.setJobCredentialEndpoint(delegationEndpoint);

		// separate credentials for job and staging delegation are
		// not supported
		if (jobDescription.getFileStageIn() != null
				|| jobDescription.getFileStageOut() != null
				|| jobDescription.getFileCleanUp() != null) {
			this.jobDescription
					.setStagingCredentialEndpoint(delegationEndpoint);
		}

		// delegate to RFT and populate the job descriptions with the
		// EPR of the delegated credential
		populateStagingDescriptionEndpoints(mjFactoryEndpoint,
				delegationFactoryEndpoints[1], this.jobDescription);

		// delegate to sub-job if multi-job
		if (this.jobDescription instanceof MultiJobDescriptionType) {

			JobDescriptionType[] subJobDescriptions = ((MultiJobDescriptionType) this.jobDescription)
					.getJob();

			// delegate for subjobs and populate the job descriptions of the
			// multi job with the subjob credential endpoints
			for (int index = 0; index < subJobDescriptions.length; index++) {
				EndpointReferenceType subJobFactoryEndpoint = subJobDescriptions[index]
						.getFactoryEndpoint();
				if (logger.isDebugEnabled()) {
					Element eprElement = ObjectSerializer.toElement(
							subJobFactoryEndpoint,
							RSLHelper.FACTORY_ENDPOINT_ATTRIBUTE_QNAME);
					logger.debug("Sub-Job Factory EPR: "
							+ XmlUtils.toString(eprElement));
				}
				if (subJobFactoryEndpoint != null) {
					if (subJobFactoryEndpoint.getAddress() == null) {
						logger.error("Sub-Job Factory Endpoint Address is null.");
					}
					EndpointReferenceType[] subJobDelegationFactoryEndpoints = fetchDelegationFactoryEndpoints(subJobFactoryEndpoint);
					EndpointReferenceType subJobCredentialEndpoint = delegate(
							subJobDelegationFactoryEndpoints[0], true);
					subJobDescriptions[index]
							.setJobCredentialEndpoint(subJobCredentialEndpoint);

					// separate credentials for job and staging delegation are
					// not supported
					subJobDescriptions[index]
							.setStagingCredentialEndpoint(subJobCredentialEndpoint);
					if (logger.isDebugEnabled()) {
						logger.debug("sub-job delegated credential endpoint:\n"
								+ subJobCredentialEndpoint);
					}
					// delegate to sub-job RFT
					populateStagingDescriptionEndpoints(subJobFactoryEndpoint,
							subJobDelegationFactoryEndpoints[1],
							subJobDescriptions[index]);
				}
			}
		}
	}

	/**
	 * Delegate credentials to RFT and populated the job description with the
	 * EPR of the delegated credential.
	 */
	private void populateStagingDescriptionEndpoints(
			EndpointReferenceType mjFactoryEndpoint,
			EndpointReferenceType delegationFactoryEndpoint,
			JobDescriptionType jobDescription) throws Exception {

		// set staging factory endpoints and delegate
		TransferRequestType stageOut = jobDescription.getFileStageOut();
		TransferRequestType stageIn = jobDescription.getFileStageIn();
		DeleteRequestType cleanUp = jobDescription.getFileCleanUp();

		if ((stageOut != null) || (stageIn != null) || (cleanUp != null)) {
			String factoryAddress = mjFactoryEndpoint.getAddress().toString();
			factoryAddress = factoryAddress.replaceFirst(
					"ManagedJobFactoryService",
					"ReliableFileTransferFactoryService");

			// delegate to RFT
			EndpointReferenceType transferCredentialEndpoint = delegate(
					delegationFactoryEndpoint, true);

			// set delegated credential endpoint for stage-out
			if (stageOut != null) {
				stageOut.setTransferCredentialEndpoint(transferCredentialEndpoint);
			}

			// set delegated credential endpoint for stage-in
			if (stageIn != null) {
				stageIn.setTransferCredentialEndpoint(transferCredentialEndpoint);
			}

			// set delegated credential endpoint for clean up
			if (cleanUp != null) {
				cleanUp.setTransferCredentialEndpoint(transferCredentialEndpoint);
			}
		}
	}

	private EndpointReferenceType delegate(
			EndpointReferenceType delegationFactoryEndpoint,
			boolean limitedDelegation) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("Delegation Factory Endpoint:\n"
					+ delegationFactoryEndpoint);
		}

		// Credential to sign with
		GlobusCredential credential = null;
		if (this.proxy != null) {
			// user-specified credential
			credential = ((GlobusGSSCredentialImpl) this.proxy)
					.getGlobusCredential();
		} else {
			// default credential
			credential = GlobusCredential.getDefaultCredential();
		}

		ClientSecurityDescriptor secDesc = new ClientSecurityDescriptor();
		if (this.securityType.equals(Constants.GSI_SEC_MSG)) {
			secDesc.setGSISecureMsg(this.getMessageProtectionType());
		} else if (this.securityType.equals(Constants.GSI_SEC_CONV)) {
			secDesc.setGSISecureConv(this.getMessageProtectionType());
		} else {
			secDesc.setGSISecureTransport(this.getMessageProtectionType());
		}
		secDesc.setAuthz(getAuthorization());

		if (this.proxy != null)
			secDesc.setGSSCredential(this.proxy);

		// Get the public key to delegate on.
		X509Certificate[] certsToDelegateOn = DelegationUtil
				.getCertificateChainRP(delegationFactoryEndpoint, secDesc);
		X509Certificate certToSign = certsToDelegateOn[0];

		// FIXME remove when there is a DelegationUtil.delegate(EPR, ...)
		String protocol = delegationFactoryEndpoint.getAddress().getScheme();
		String host = delegationFactoryEndpoint.getAddress().getHost();
		int port = delegationFactoryEndpoint.getAddress().getPort();
		String factoryUrl = protocol + "://" + host + ":" + port
				+ BASE_SERVICE_PATH + DelegationConstants.FACTORY_PATH;

		EndpointReferenceType credentialEndpoint = null;

		// Set lifetime of the credential only if the job has a lifetime.
		// Otherwise DelegationUtil will determine the lifetime from the
		// the timeLeft value of the credential
		if (this.terminationTime != null) {
			int credentialLifetime = (int) ((terminationTime.getTimeInMillis() - (Calendar
					.getInstance().getTimeInMillis()) / 1000) + 5000);
			credentialEndpoint = DelegationUtil.delegate(factoryUrl,
					credential, certToSign, credentialLifetime,
					!limitedDelegation, secDesc);
		} else {
			credentialEndpoint = DelegationUtil.delegate(factoryUrl,
					credential, certToSign, !limitedDelegation, secDesc);
		}

		return credentialEndpoint;
	}

	private String catenate(String baseURL, String path) {

		final String SEPARATOR = "/";
		String newPath = path;
		if (path.indexOf("://") < 0) { // not a URL already
			if (baseURL.endsWith(SEPARATOR)) {
				baseURL = baseURL.substring(0, baseURL.length() - 1);
			}
			// assert !baseURL.endsWith(SEPARATOR)
			if (!path.startsWith(SEPARATOR)) {
				baseURL = baseURL + SEPARATOR;
			}
			newPath = baseURL + path;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("path " + path
						+ " is a URL already. No prepending of URL");
			}
		}
		return newPath;
	}

	private EndpointReferenceType createJobEndpoint(
			ManagedJobFactoryPortType factoryPort, boolean batch)
			throws Exception {

		// Create a service instance base on creation info
		logger.debug("creating ManagedJob instance");

		((org.apache.axis.client.Stub) factoryPort)
				.setTimeout(this.axisStubTimeOut);

		CreateManagedJobInputType jobInput = new CreateManagedJobInputType();
		jobInput.setInitialTerminationTime(getTerminationTime());
		if (this.id != null) {
			jobInput.setJobID(new AttributedURIType(this.id));
		}
		if (this.jobDescription instanceof MultiJobDescriptionType) {
			jobInput.setMultiJob((MultiJobDescriptionType) this
					.getDescription());
		} else {
			jobInput.setJob(this.getDescription());
		}

		if (!batch) {
			if (this.useDefaultNotificationConsumer) {
				setupNotificationConsumerManager();
			}

			try {
				if (this.useDefaultNotificationConsumer) {
					setupNotificationConsumer();
				}

				Subscribe subscriptionRequest = new Subscribe();
				subscriptionRequest
						.setConsumerReference(this.notificationConsumerEPR);
				TopicExpressionType topicExpression = new TopicExpressionType();
				topicExpression.setDialect(WSNConstants.SIMPLE_TOPIC_DIALECT);
				topicExpression
						.setValue(ManagedJobConstants.STATE_CHANGE_INFORMATION_TOPIC_QNAME);
				MessageElement element = (MessageElement) ObjectSerializer
						.toSOAPElement(topicExpression,
								WSNConstants.TOPIC_EXPRESSION);
				FilterType filter = new FilterType();
				filter.set_any(new MessageElement[] { element });
				subscriptionRequest.setFilter(filter);
				jobInput.setSubscribe(subscriptionRequest);
			} catch (Exception e) {
				// may happen...? Let's not fail.
				logger.error(e);
				try {
					unbind();
				} catch (Exception unbindE) {
					// let's not fail the unbind
					logger.error(unbindE);
				}
			}
		}

		CreateManagedJobOutputType response = factoryPort
				.createManagedJob(jobInput);
		EndpointReferenceType jobEPR = (EndpointReferenceType) ObjectSerializer
				.clone(response.getManagedJobEndpoint());

		if (logger.isDebugEnabled()) {
			logger.debug("Job Handle: " + AuditUtil.eprToGridId(jobEPR));
			logger.debug("Job EPR: " + jobEPR);
		}

		EndpointReferenceType subscriptionEndpoint = response
				.getSubscriptionEndpoint();

		if (subscriptionEndpoint != null) {
			this.notificationProducerEPR = (EndpointReferenceType) ObjectSerializer
					.clone(subscriptionEndpoint);
		}

		return jobEPR;
	}

	private void setupNotificationConsumerManager() throws ConfigException,
			GSSException, ContainerException {

		logger.debug("Security Type: " + this.securityType);

		if (this.securityType.equals(Constants.GSI_SEC_MSG)
				|| this.securityType.equals(Constants.GSI_SEC_CONV)) {
			logger.debug("Setting up a non-secure consumer manager.");

			// start an embedded container with a notification consumer
			this.notificationConsumerManager = NotificationConsumerManager
					.getInstance();
		} else {
			logger.debug("Setting up a secure consumer manager.");

			// embedded container properties
			Map properties = new HashMap();

			// make sure the embedded container speaks GSI Transport Security
			properties.put(ServiceContainer.CLASS,
					"org.globus.wsrf.container.GSIServiceContainer");

			// specify the credentials to use for the embedded container
			if (this.proxy != null) {
				// user-specified credential
				ContainerSecurityDescriptor containerSecDesc = new ContainerSecurityDescriptor();
				containerSecDesc.setSubject(JaasGssUtil
						.createSubject(this.proxy));
				properties.put(ServiceContainer.CONTAINER_DESCRIPTOR,
						containerSecDesc);
			}

			// start a secure embedded container with a notif. consumer
			this.notificationConsumerManager = NotificationConsumerManager
					.getInstance(properties);
		}

		this.notificationConsumerManager.startListening();
	}

	private void setupNotificationConsumer() throws ConfigException,
			ResourceException {

		logger.debug("Setting up notification consumer.");

		List topicPath = new LinkedList();
		topicPath.add(ManagedJobConstants.STATE_CHANGE_INFORMATION_TOPIC_QNAME);

		ResourceSecurityDescriptor securityDescriptor = new ResourceSecurityDescriptor();
		// TODO implement "service-side host authorization"
		String authz = null;
		if (authorization == null) {
			authz = Authorization.AUTHZ_NONE;
		} else if (authorization instanceof HostAuthorization) {
			authz = Authorization.AUTHZ_NONE;
		} else if (authorization instanceof SelfAuthorization) {
			authz = Authorization.AUTHZ_SELF;
		} else if (authorization instanceof IdentityAuthorization) {
			GridMap gridMap = new GridMap();
			gridMap.map(((IdentityAuthorization) authorization).getIdentity(),
					"HaCk");
			securityDescriptor.setDefaultGridMap(gridMap);

			authz = Authorization.AUTHZ_GRIDMAP;
		} else {
			logger.error("Unsupported authorization method class "
					+ authorization.getClass().getName());
			return;
		}
		securityDescriptor.setPDP(authz);
		Vector authMethod = new Vector();
		logger.debug("Security Type: " + this.securityType);
		if (this.securityType.equals(Constants.GSI_SEC_MSG)) {
			authMethod.add(GSISecureMsgAuthMethod.BOTH);
		} else if (this.securityType.equals(Constants.GSI_SEC_CONV)) {
			authMethod.add(GSISecureConvAuthMethod.BOTH);
		} else {
			authMethod.add(GSITransportAuthMethod.BOTH);
		}
		securityDescriptor.setDefaultAuthMethods(authMethod);

		this.notificationConsumerEPR = this.notificationConsumerManager
				.createNotificationConsumer(topicPath, this, securityDescriptor);

		if (logger.isDebugEnabled()) {
			logger.debug("notification consumer endpoint:\n"
					+ this.notificationConsumerEPR);
		}
	}

	private ManagedJobFactoryPortType getManagedJobFactoryPortType(
			EndpointReferenceType factoryEndpoint) throws Exception {

		ManagedJobFactoryPortType factoryPort = ManagedJobFactoryClientHelper
				.getPort(factoryEndpoint);

		setStubSecurityProperties((Stub) factoryPort);

		return factoryPort;
	}

	private void setFault(FaultType[] fault) throws Exception {

		this.fault = fault;
	}

	private FaultType deserializeFaultRP(SOAPElement any)
			throws DeserializationException {

		return FaultUtils
				.getConcreteFault((FaultResourcePropertyType) ObjectDeserializer
						.toObject(any, FaultResourcePropertyType.class));
	}

	private void destroyTransferDelegatedCredential(
			JobDescriptionType jobDescription) throws Exception {

		TransferRequestType stageOut = jobDescription.getFileStageOut();
		TransferRequestType stageIn = jobDescription.getFileStageIn();
		DeleteRequestType cleanUp = jobDescription.getFileCleanUp();
		EndpointReferenceType transferCredentialEndpoint = null;

		if (stageOut != null) {
			transferCredentialEndpoint = stageOut
					.getTransferCredentialEndpoint();
		} else if (stageIn != null) {
			transferCredentialEndpoint = stageIn
					.getTransferCredentialEndpoint();
		} else if (cleanUp != null) {
			transferCredentialEndpoint = cleanUp
					.getTransferCredentialEndpoint();
		}

		if (transferCredentialEndpoint != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Calling destroy on transferCredentialEndpoint for"
						+ " job "
						+ this.id
						+ ":\n"
						+ ObjectSerializer.toString(transferCredentialEndpoint,
								ManagedJobConstants.ENDPOINT_REFERENCE_QNAME));
			}

			destroyDelegatedCredential(transferCredentialEndpoint);
		}
	}

	private void destroyDelegatedCredential(
			EndpointReferenceType credentialEndpoint) throws Exception {

		DelegationPortType delegatedCredentialPort = new DelegationServiceAddressingLocator()
				.getDelegationPortTypePort(credentialEndpoint);
		setStubSecurityProperties((Stub) delegatedCredentialPort);
		try {
			delegatedCredentialPort.destroy(new Destroy());
		} catch (ResourceUnknownFaultType resUnknownFault) {
			logger.warn("Unable to destroy resource");
			if (logger.isDebugEnabled()) {
				resUnknownFault.printStackTrace();
			}
			// not an error - the job may have
			// been automatically destroyed by soft state
		}
	}

	private void setStubSecurityProperties(Stub stub) {

		if (logger.isDebugEnabled()) {
			logger.debug("setting factory stub security...using authz method "
					+ getAuthorization());
		}

		ClientSecurityDescriptor secDesc = new ClientSecurityDescriptor();

		// set security type
		if (this.securityType.equals(Constants.GSI_SEC_MSG)) {
			secDesc.setGSISecureMsg(this.getMessageProtectionType());
		} else if (this.securityType.equals(Constants.GSI_SEC_CONV)) {
			secDesc.setGSISecureConv(this.getMessageProtectionType());
		} else {
			secDesc.setGSISecureTransport(this.getMessageProtectionType());
		}

		// set authorization
		secDesc.setAuthz(getAuthorization());

		if (this.proxy != null) {
			// set proxy credential
			secDesc.setGSSCredential(this.proxy);
		}

		stub._setProperty(Constants.CLIENT_DESCRIPTOR, secDesc);
	}

}
