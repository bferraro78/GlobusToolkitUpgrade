/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.exec.client.GramJob;
import org.globus.axis.message.addressing.EndpointReferenceType;

import java.io.File;
import java.util.ArrayList;

/**
 * Base class from which different types of Grid jobs can extend.
 */
public class GSBLJob {

	/**
	 * The scheduler that will run this job.
	 */
	private String myScheduler = "";

	/**
	 * The factory type (like Condor, BOINC, etc).
	 */
	private String myResource = "";

	/**
	 * The architecture and operating system this job will run on.
	 */
	private String myArch_os = "";

	/**
	 * The number of job replicates to submit.
	 */
	private String replicates = "1";

	/**
	 * The number of cpus a single job will run on. Typically "1", and for MPI
	 * jobs, currently hard-coded to "8".
	 */
	private String cpus = "1";

	/**
	 * The path to the program executable.
	 */
	private String myExecutable = "";

	/**
	 * The arguments to the executable.
	 */
	private String myArguments = "";

	/**
	 * The full path to the working directory of this job.
	 */
	private String myWorkingDir = "";

	/**
	 * The unique ID of this job.
	 */
	private String unique_id = "";

	/**
	 * Array of filenames that are to be staged to the executing host.
	 */
	private String[] myInput_files = null;

	/**
	 * Array of filenames that are to be staged from the executing host.
	 */
	private String[] myOutput_files = null;

	/* Host of job submission */
	private String host; // ex.) asparagine

	/**
	 * The underlying Globus GramJob.
	 */
	private GramJob myJob = null;

	private BuildRSL r = null;

	/**
	 * A Jakarta-commons logging logger.
	 */
	static Log log = LogFactory.getLog(GSBLJob.class.getName());

	/**
	 * Class constructor. Initializes all fields EXCEPT myJob, which is left to
	 * the subclasses.
	 * 
	 * @param executable
	 *            The full path to the program executable.
	 * @param arguments
	 *            The arguments to the executable.
	 * @param scheduler
	 *            The ManagedJobFactoryService this job is being assigned to.
	 * @param resource
	 *            One of BOINC, Condor, etc.
	 * @param arch_os
	 *            If resource == "Condor", this variable determines architecture
	 *            and operating system job is being assigned to.
	 * @param workingDir
	 *            Used to determine the location of our staging directory.
	 * @param runtime_estimate
	 *            Used in scheduling.
	 * @param shared_files
	 * @param perjob_files
	 * @param output_files
	 *            An array of relative filnames to stage back to the server.
	 *            They will be staged to the job's working directory.
	 * @param requirements
	 *            Used by Condor and our old matchmaking system. Could be used
	 *            again in the future.
	 * @param extraRSL
	 *            Some jobs have special requirements, like reading stdin, for
	 *            example.
	 */
	public GSBLJob(String executable, String arguments, String scheduler,
			String resource, String arch_os, String workingDir,
			int runtime_estimate, ArrayList<String> shared_files,
			ArrayList<String[]> perjob_files, String[] output_files,
			String requirements, String extraRSL, String unique_id) throws Exception {

		r = new BuildRSL(executable, arguments, scheduler, resource, arch_os,
				runtime_estimate, shared_files, perjob_files, output_files,
				workingDir, requirements, extraRSL, unique_id);

		// Write the RSL string
		r.writeRSL();

		host = r.getHost();

		// write any mappings to our working directory (something like
		// "/export/grid_files/[job_id]/").
	//	r.writeMappingsToAdd(); 

		// Instantiate the GramJob.
	//	myJob = new GramJob(r.getXML());

		// Fill in the number of cpus and job replicates.
		cpus = r.getCPUs();
		replicates = r.getReplicates();

		// If matchmaking, i.e., scheduling, is being invoked, fill in the
		// values appropriately.
		if (scheduler.equals("matchmaking")) {
			myScheduler = r.getScheduler();  // The new values.
			myResource = r.getResource();
			myArch_os = r.getArch_os();
		} else {
			myScheduler = scheduler;
			myResource = resource;
			myArch_os = arch_os;
		}

		if (log.isDebugEnabled()) {
			log.debug("Created new GSBLJob with RSL = " + r.getRSL());
		}

		// If an executable is not specified, alert the user.
		if (executable == null) {
			log.error("Unable to create GSBLJob: no executable specified.");
			throw new Exception("No executable supplied");
		} else {
			myExecutable = executable;
		}

		// Check for null arguments.
		if (arguments == null) {
			myArguments = "";
		} else {
			myArguments = arguments;
		}

		// Set the working directory.
		myWorkingDir = workingDir;
		if (log.isDebugEnabled()) {
			log.debug("GSBLJob using working dir of '" + myWorkingDir + "'.");
		}

		// Set unique_id from parameter
		this.unique_id = unique_id;

		/*
		 * Save the input and output files array. Java arrays are really
		 * references, so we need to copy the arrays element-wise into new
		 * arrays to prevent other people from being able to change our internal
		 * values.
		 */
		// Updated to use "shared_files".
		if (shared_files != null) {
			String[] shared_files_array = new String[shared_files.size()];
			myInput_files = shared_files.toArray(shared_files_array);
		}
		if (output_files != null) {
			myOutput_files = new String[output_files.length];
		}

		if (myInput_files != null) {
			for (int i = 0; i < myInput_files.length; i++) {
				File inFile = new File(myInput_files[i]);
				if (inFile.isAbsolute()) {
					log.error("Absolute pathname given for file to be staged in by GSBLJob: "
							+ inFile.getAbsolutePath());
					throw new Exception(
							"Absolute pathname given for file to be staged in by GSBLJob: "
									+ inFile.getAbsolutePath());
				}
				myInput_files[i] = "../" + myInput_files[i];
			}
		}
		if ((myOutput_files != null) && (myOutput_files.length > 0)) {
			System.arraycopy(output_files, 0, myOutput_files, 0,
					output_files.length);
		}

		log.debug("GSBLJob has been initialized.");
	}

	/**
	 * Constructor used to instantiate an existing job.
	 * 
	 * @param jobEPR
	 *            Endpoint reference of a Globus GramJob.
	 * @param rwd
	 *            Remote working directory, i.e., staging directory on the
	 *            server.
	 */
	public GSBLJob(String rwd) {
		myJob = new GramJob();
		try {
			// myJob.setEndpoint(jobEPR);
			// myJob.setSecurityTypeFromEndpoint(jobEPR);
			myWorkingDir = rwd;

			// Set the unique ID from the working directory.
			unique_id = myWorkingDir
					.substring(0, myWorkingDir.lastIndexOf("/"));
			unique_id = unique_id.substring(unique_id.lastIndexOf("/") + 1);

			// Set the number of replicates by querying the database.
			String result = GSBLUtils.selectDBStringField("job", "replicates",
					"unique_id", unique_id);
			if (result != null) {
				replicates = result;
			}

		} catch (Exception e) {
			log.error("Exception is: " + e);
		}
	}

	/**
	 * Get the underlying GramJob.
	 * 
	 * @return the GramJob.
	 */
	public GramJob getJob() {
		return myJob;
	}

	/**
	 * Get the scheduler.
	 * 
	 * @return myScheduler.
	 */
	public String getScheduler() {
		return myScheduler;
	}

	/**
	 * Get the resource.
	 * 
	 * @return myResource.
	 */
	public String getResource() {
		return myResource;
	}

	/**
	 * Return host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Get the arch_os.
	 * 
	 * @return myArch_os.
	 */
	public String getArch_os() {
		return myArch_os;
	}

	/**
	 * Get the cpus.
	 * 
	 * @return cpus.
	 */
	public String getCPUs() {
		return cpus;
	}

	/**
	 * Get the replicates.
	 * 
	 * @return replicates.
	 */
	public String getReplicates() {
		return replicates;
	}

	/**
	 * Get the executable field.
	 * 
	 * @return the executable as a String.
	 */
	public String getExecutable() {
		return myExecutable;
	}

	/**
	 * Get the arguments field.
	 * 
	 * @return the arguments as a String.
	 */
	public String getArguments() {
		return myArguments;
	}

	/**
	 * Map relative to absolute filenames. Given a relative filename such as
	 * "foo", returns the absolute filename corresponding to foo, e.g.
	 * "/export/grid_files/18485851/foo".
	 * 
	 * @param filename
	 *            The relative filename to map.
	 * @return an absolute filename as a String.
	 */
	public String getFilename(String filename) {
		if (log.isDebugEnabled()) {
			log.debug("GSBLJob mapping " + filename + " --> " + myWorkingDir
					+ filename);
		}
		return myWorkingDir + filename;
	}

	/**
	 * Get the working directory.
	 * 
	 * @return the working directory as a String.
	 */
	public String getWorkingDir() {
		return myWorkingDir;
	}

	/**
	 * Get the unique ID.
	 * 
	 * @return the unique ID as a String.
	 */
	public String getUnique_id() {
		return unique_id;
	}

	/**
	 * Method to return the full name of the file containing the job's stdout.
	 * Deriving classes may wish to override this.
	 */
	public String getStdoutFilename() {
		return getFilename("stdout");
	}

	/**
	 * Method to return the full name of the file containing the job's stderr.
	 * Deriving classes may wish to override this.
	 */
	public String getStderrFilename() {
		return getFilename("stderr");
	}

	public RSLxml getRSLString() {
		return r;
	}
}
