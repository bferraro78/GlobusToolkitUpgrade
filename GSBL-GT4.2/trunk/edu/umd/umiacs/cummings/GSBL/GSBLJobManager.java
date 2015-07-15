/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.axis.message.addressing.EndpointReferenceType;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;

// authorization classes
import org.globus.wsrf.security.authorization.client.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;

// credential management classes
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;

import org.globus.wsrf.impl.security.authentication.Constants;

// classes for setting date/times on GramJob durations/terminations
import java.net.URL;
import java.util.Date;

// for io
import java.io.*;

// for running the state check using globusrun-ws
import java.lang.Runtime;
import java.util.Properties;

/**
 * Class that manages the submission and processing of a {@link GSBLJob}.
 */
// public class GSBLJobManager implements GramJobListener {
public class GSBLJobManager {

	private static String tempUploadLocation = "/tmp/temp_uploads/";

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(GSBLJobManager.class.getName());

	/**
	 * The {@link GSBLJob} to manage.
	 */
	GSBLJob managedJob = null;

	/**
	 * For RLS queries.
	 */
	private RLSManager rlsmanager = null;

	/**
	 * Indicates whether or not the job has been submitted.
	 */
	public boolean submitted = false;

	/**
	 * Indicates if the job is finished.
	 */
	public boolean jobCompleted = false;

	/**
	 * Indicates if the job completed successfully.
	 */
	public boolean jobFailed = false;

	/**
	 * We are using host authorization here.
	 */
	private Authorization _authorization = new HostAuthorization();

	/**
	 * type of XML Security
	 */
	private Integer _xmlSecurity = Constants.SIGNATURE;

	/**
	 * GramJob timeout.
	 */
	private int _timeout = GramJob.DEFAULT_TIMEOUT;

	/**
	 * Delegation enabled?
	 */
	private boolean _delegationEnabled = true;

	/**
	 * Limited delegation?
	 */
	private boolean _limitedDelegation = true;

	/**
	 * Submission ID used to identify this job. Should be unique.
	 */
	private String submissionID = null;

	/**
	 * The MJFS where to submit the job.
	 */
	private String factory;

	/**
	 * The factory URL.
	 */
	private URL factoryURL;

	/**
	 * The resource, or "type" of job this is, e.g., "Fork", "Condor", "BOINC"
	 */
	private String factoryType;

	/**
	 * Job working directory.
	 */
	private String myWorkingDir;

	/**
	 * The factory endpoint.
	 */
	private EndpointReferenceType factoryEndpoint = null;

	/**
	 * The duration after which the job service should be destroyed.
	 */
	private Date serviceDuration = null; // the default lifetime is unlimited as
											// of GT4.2!

	/**
	 * The date/time desired for termination of this job service.
	 */
	private Date serviceTerminationDate = null; // the default lifetime is
												// unlimited as of GT4.2!

	/**
	 * This constant specifies the base timeout before polling for job status,
	 * currently sixty seconds.
	 */
	private static final long STATE_CHANGE_BASE_TIMEOUT_MILLIS = 8000;

	/**
	 * Used to see if the job state has changed.
	 */
	private static StateEnumeration previousJobState = StateEnumeration.Pending;

	/**
	 * Class constructor. Builds an object that will manage the submission of a
	 * GramJob to an MMJFS.
	 * 
	 * @param job
	 *            The {@link GSBLJob} to submit.
	 * @param myFactory
	 *            Where to submit the job.
	 * @param myFactoryType
	 *            The resource, or "type" of job this is, e.g., "Fork",
	 *            "Condor", "BOINC"
	 */
	public GSBLJobManager(GSBLJob job, String myFactory, String myFactoryType) {
		factory = myFactory;
		factoryType = myFactoryType;

		try {
			factoryURL = ManagedJobFactoryClientHelper.getServiceURL(factory)
					.getURL();
			factoryEndpoint = ManagedJobFactoryClientHelper.getFactoryEndpoint(
					factoryURL, factoryType);
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
		managedJob = job;
		myWorkingDir = managedJob.getWorkingDir();

		rlsmanager = new RLSManager();
	}

	/**
	 * This constructor is used to manage an existing job.
	 * 
	 * @param job
	 *            the GSBLJob to manage
	 */
	public GSBLJobManager(GSBLJob job) {
		this.managedJob = job;
		myWorkingDir = managedJob.getWorkingDir();

		rlsmanager = new RLSManager();
	}

	/**
	 * Prepares job for submission and submits it.
	 * 
	 * @exception Exception
	 *                if job submission fails.
	 */
	public synchronized void submit() throws Exception {
		if (submitted == true) {
			log.error("Attempt to resubmit GSBLJobManager-managed job.");
			throw new Exception("job already submitted!");
		}

		// get a reference to the underlying GramJob
		GramJob job = managedJob.getJob();

		try {
			ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
					.getInstance();
			String handle = "X509_USER_PROXY=/tmp/x509up_u10463"; 
			// this should be the proxy of your Globus user

			GSSCredential proxy = manager.createCredential(handle.getBytes(),
					ExtendedGSSCredential.IMPEXP_MECH_SPECIFIC,
					GSSCredential.DEFAULT_LIFETIME, null,
					GSSCredential.INITIATE_AND_ACCEPT);
			job.setCredentials(proxy);
			// set attributes of the GramJob
			// job.setTimeOut(_timeout);
			job.setAuthorization(_authorization);
			job.setMessageProtectionType(_xmlSecurity);
			job.setDelegationEnabled(_delegationEnabled);

			// set Job duration and termination time (this should be no longer
			// necessary in 4.2, as the default is unlimited runtime)
			/*
			 * long lifetime = 2 * 7 * 24 * 60 * 60 * 1000; // this represents
			 * two weeks serviceDuration = new Date(System.currentTimeMillis() +
			 * lifetime); job.setDuration(serviceDuration);
			 * 
			 * serviceTerminationDate = new Date(System.currentTimeMillis() +
			 * lifetime); job.setTerminationTime(serviceTerminationDate);
			 */

			// get a unique id for this job
			UUIDGen uuidgen = UUIDGenFactory.getUUIDGen();
			submissionID = "uuid:" + uuidgen.nextUUID();

			// add ourselves as a listener for job status
			// job.addListener(this);

			// submit the job in a non-batch mode
			boolean batch = false;
			job.submit(factoryEndpoint, batch, this._limitedDelegation,
					submissionID);

			if (log.isDebugEnabled()) {
				log.debug("Submitted GSBLJob to: " + factory);
			}
		} catch (Exception e) {
			log.error("Unable to submit GSBLJob: " + e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

		// write the job EPR to a file so we can resume listening if we get
		// interrupted
		EndpointReferenceType thejobEPR = job.getEndpoint();
		String jobEPR = ObjectSerializer.toString(thejobEPR,
				GSBLQNames.RESOURCE_REFERENCE);
		String eprFilename = managedJob.getFilename("jobEPR.txt");
		FileWriter fileWriter = new FileWriter(eprFilename);
		BufferedWriter bfWriter = new BufferedWriter(fileWriter);
		bfWriter.write(jobEPR);
		bfWriter.close();

		log.debug("jobEPR written to file: " + eprFilename);

		submitted = true;
	}

	



	public void checkJobStatus(int update_interval, int update_max) {
		String unique_id = managedJob.getUnique_id();
		try {
			Properties env = new Properties();
			env.load(Runtime.getRuntime().exec("env").getInputStream());
			String globusLocation = (String) env.get("GLOBUS_LOCATION");
			Runtime r = Runtime.getRuntime();
			File stateFile = new File(myWorkingDir + "last_known_status.txt");
			stateFile.delete();
			Process proc = r.exec(globusLocation + "/check_job_state.pl "
					+ myWorkingDir);
			int elapsedTime = 0;
			while (!stateFile.exists()) {
				try {
					Thread.sleep(3000);
				} catch (Exception e) {
					log.error("Exception while sleeping during job state check: "
							+ e);
				}
				elapsedTime += 3;
				if (elapsedTime > 30) {
					log.error("State check did not produce a state file (and might be hung), destroying state check process... ");
					proc.destroy();
					break;
				}
			}
			if (stateFile.exists()) {
				BufferedReader br = new BufferedReader(
						new FileReader(stateFile));
				String jobState = null;
				jobState = br.readLine();
				br.close();

				if (jobState != null) {
					if (jobState.equals("Idle")) {
						log.debug("updating job status: 1 for " + myWorkingDir);
						// update the status of this job in the database? (1 =
						// idle)
						GSBLService.updateDBStatus("1", myWorkingDir,
								update_interval, update_max);
					} else if (jobState.equals("Running")) {
						log.debug("updating job status: 2 for " + myWorkingDir);
						// update the status of this job in the database (2 =
						// running)
						GSBLService.updateDBStatus("2", myWorkingDir,
								update_interval, update_max);
					} else if (jobState.equals("Finished")) {
						log.debug("updating job status: 4 for " + myWorkingDir);
						// update the status of this job in the database (4 =
						// finished)
						GSBLService.updateDBStatus("4", myWorkingDir,
								update_interval, update_max);
						this.jobCompleted = true;
						this.jobFailed = false;
					} else if (jobState.equals("Failed")) {
						// update the status of this job in the database (5 =
						// failed)
						log.debug("updating job status: 5 for " + myWorkingDir);
						GSBLService.updateDBStatus("5", myWorkingDir,
								update_interval, update_max);

						// update the status of this job in the Drupal database
						// GSBLUtils.updateDrupalStatus(null, unique_id, "5",
						// managedJob.getReplicates());
						this.jobCompleted = true;
						this.jobFailed = true;

					// } else if (jobState.equals("Unknown")) {  // Begin JTK.
					// 	Boolean jobFinished = false;
					// 	File dir = new File("/export/grid_files/" + unique_id + "/");
                        
     //                    if (managedJob.getReplicates() == 1) {
     //                        FileFilter fileFilter = new WildcardFileFilter("*.tre");
     //                        File[] files = dir.listFiles(fileFilter);
                            
     //                        if (files.length > 0) {
     //                            if (files[0].length() > 0) {
     //                            	jobFinished = true;
     //                            }
     //                        }
     //                	} else {  //Batch job.
     //                        File batchFile = new File(dir + unique_id
     //                                + ".tar.gz");
     //                        if (batchFile.exists()) {
     //                            jobFinished = true;
     //                        }
     //                    }
                        
     //                    if (jobFinished) {
     //                        log.debug("updating job status: 4 for " + myWorkingDir);
     //                        // update the status of this job in the database (4 =
     //                        // finished)
     //                        GSBLService.updateDBStatus("4", myWorkingDir,
     //                                update_interval, update_max);
     //                        this.jobCompleted = true;
     //                        this.jobFailed = false;
     //                    }  // End JTK.

					} else {
						log.debug("jobState for " + myWorkingDir + " is: "
								+ jobState);
					}




					// do cache dir cleanup check and add necessary RLS mappings
					if (jobState.equals("Idle") || jobState.equals("Running")
							|| jobState.equals("Finished")
							|| jobState.equals("Failed")) {

						// this file gets created when the cache is cleaned up
						File tempCacheDirCleanedUp = new File(myWorkingDir
								+ "tempCacheCleaned");

						if (!tempCacheDirCleanedUp.exists()) {
							File tempCacheDirectory = new File(
									tempUploadLocation + unique_id);
							if (tempCacheDirectory.exists()) {
								GSBLUtils.executeCommand("rm -rf "
										+ tempCacheDirectory);
							}
							GSBLUtils.executeCommand("touch " + myWorkingDir
									+ "tempCacheCleaned");
						}

						// attempt to discover the local job ID
						File jobid_file = new File(myWorkingDir + "localJobID");
						if (!jobid_file.exists()) {
							GSBLUtils.executeCommand(globusLocation
									+ "/get_local_jobid.pl " + myWorkingDir);
						}

						// only add mappings if job submission did not fail
						if (jobState.equals("Idle")
								|| jobState.equals("Running")
								|| jobState.equals("Finished")) {

							// this file gets created when mappings are added
							File addedMappings = new File(myWorkingDir
									+ "addedMappings");
							// read from mappingsToAdd file, which was created
							// by RSLxml
							File mappingsToAdd = new File(myWorkingDir
									+ "mappingsToAdd");

							if (!addedMappings.exists()
									&& mappingsToAdd.exists()) {
								BufferedReader br2 = new BufferedReader(
										new FileReader(mappingsToAdd));
								String line = "";
								while ((line = br2.readLine()) != null) {
									log.debug("DEBUGGING MAPPINGS: " + line);
									if (!line.equals("")) {
										String[] chunks = line.split(",");
										for (int i = 0; i < chunks.length; i++) {
											log.debug("chunks["
													+ new Integer(i).toString()
													+ "]: " + chunks[i]);
										}
										String md5sum = chunks[0];
										String file = chunks[1];
										String is_app = chunks[2];
										String size = chunks[3];
										if (is_app.equals("true")) {
											rlsmanager.addMapping(md5sum, file,
													true, size);
										} else {
											rlsmanager.addMapping(md5sum, file,
													false, size);
										}
									}
								}
								br2.close();
								GSBLUtils.executeCommand("touch "
										+ myWorkingDir + "addedMappings");
							}
						}
					}
				} else {
					log.debug("jobState is null!");
				}
			}
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
	}





	/*
	 * public void checkJobStatus() throws Exception { GramJob job =
	 * managedJob.getJob();
	 * 
	 * ExtendedGSSManager manager = (ExtendedGSSManager)
	 * ExtendedGSSManager.getInstance(); String handle =
	 * "X509_USER_PROXY=/tmp/x509up_u10463";
	 * 
	 * GSSCredential proxy = manager.createCredential(handle.getBytes(),
	 * ExtendedGSSCredential.IMPEXP_MECH_SPECIFIC,
	 * GSSCredential.DEFAULT_LIFETIME, null, GSSCredential.INITIATE_AND_ACCEPT);
	 * job.setCredentials(proxy);
	 * 
	 * // pull state from remote job try { job.refreshStatus(); }
	 * catch(Exception e) { log.error("Could not refresh job state: " + e); }
	 * 
	 * stateChanged(job); }
	 */

	/**
	 * When the state of a GramJob changes, this method should always be called.
	 */
	/*
	 * public void stateChanged(GramJob job) { StateEnumeration jobState =
	 * job.getState(); boolean holding = job.isHolding();
	 * 
	 * if(jobState != null) { if(jobState.equals(StateEnumeration.Pending)) {
	 * log.debug("updating job status: 1 for " + managedJob.getWorkingDir()); //
	 * update the status of this job in the database (1 = idle) } else
	 * if(jobState.equals(StateEnumeration.Active)) {
	 * log.debug("updating job status: 2 for " + managedJob.getWorkingDir()); //
	 * update the status of this job in the database (2 = running)
	 * GSBLService.updateDBStatus("2", managedJob.getWorkingDir()); } else
	 * if(jobState.equals(StateEnumeration.Done)) {
	 * log.debug("updating job status: 4 for " + managedJob.getWorkingDir()); //
	 * update the status of this job in the database (4 = finished)
	 * GSBLService.updateDBStatus("4", managedJob.getWorkingDir());
	 * this.jobCompleted = true; this.jobFailed = false; try { // job is
	 * finished, so destroy the GramJob instance job.destroy(); }
	 * catch(Exception e) { log.error("Exception: " + e); } } else
	 * if(jobState.equals(StateEnumeration.Failed)) {
	 * log.debug("Job failed!  Exit Code: " +
	 * Integer.toString(job.getExitCode())); // update the status of this job in
	 * the database (5 = failed) log.debug("updating job status: 5 for " +
	 * managedJob.getWorkingDir()); GSBLService.updateDBStatus("5",
	 * managedJob.getWorkingDir()); this.jobCompleted = true; this.jobFailed =
	 * true; try { // job is finished, so destroy the GramJob instance
	 * job.destroy(); } catch(Exception e) { log.error("Exception: " + e); } } }
	 * }
	 */

	/**
	 * Get the GSBLJob job we're managing.
	 */
	public GSBLJob getJob() {
		return managedJob;
	}
}
