package edu.umd.umiacs.cummings.GSBL;

import java.lang.Runtime;
import java.lang.Integer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//for determining host name
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Properties;

import java.io.*;

/**
 * Create an RSL for globusrun command
 */
public class buildRSL {

	private static String rftServiceHost = "localhost";
	private static String tempUploadLocation = "/tmp/temp_uploads/";

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(RSLxml.class.getName());

	/**
	 * The XML string.
	 */
	private String document = "";

	/**
	 * The GLOBUS_LOCATION.
	 */
	private static String globusLocation = "";

	/**
	 * For RLS queries.
	 */
	private RLSManager rlsmanager = null;

	/**
	 * Job submission variables.
	 */
	private String executable;
	private String[] arguments;
	private String scheduler;
	private String resource;
	private String arch_os;
	private String architecture;
	private String os;
	private String workingDirBase; // something like /export/grid_files/
	private String workingDir; // something like
								// /export/grid_files/[0-9]*.[0-9]*
	private String stagingDir; // something like [0-9]*.[0-9]*
	private String cacheDir; // something like /export/grid_files/cache/

	private ArrayList<String> sharedFiles;
	private ArrayList<String[]> perJobFiles;

	private String[] output_files;
	private String extraRSL;
	private String environment; // used for environment variables
	private String stdin; // used for programs that take input on stdin

	private String symlinks_string = "";
	private String application_upload_string = "";
	private String file_upload_string = "";
	private ArrayList<String> mappingsToAdd = null;

	private String job_type = "single"; // default is a single (serial) job

	private String replicates = "1"; // default is one job replicate
	private int reps = 1; // an integer form of the above

	private String cpus = "1"; // default is one processor; for an mpi job, 8
								// processors
	private int num_cpus = 1;

	private String max_memory = ""; // default is not to specify anything
	private Integer max_mem = null; // default is not to specify anything

	private int runtime_estimate_seconds = -1; // -1 means an estimate has not
												// been given

	/**
	 * XML header.
	 */
	private String header = "globusrun -r ";

	/**
	 * Create an RSL XML document using the parameters provided.
	 * 
	 * @param myExecutable
	 *            path to executable.
	 * @param myArguments
	 *            arguments to executable.
	 * @param myScheduler
	 * @param myResource
	 *            resource this job will execute on, e.g., Fork, Condor, BOINC
	 * @param myArchOs
	 * @param myRuntimeEstimate
	 * @param sharedlist
	 * @param perjoblist
	 * @param myOutput_files
	 *            array of output files from job execution.
	 * @param myWorkingDir
	 *            directory where output files from this job are put
	 * @param myRequirements
	 *            requirements for the job (will be used when scheduling is
	 *            implemented)
	 * @param myExtraRSL
	 *            extra RSL to be included in the job description. must be
	 *            preformatted XML.
	 */
	public buildRSL(String myExecutable, String myArguments, String myScheduler,
			String myResource, String myArchOs, int myRuntimeEstimate,
			ArrayList<String> sharedlist, ArrayList<String[]> perjoblist,
			String[] myOutput_files, String myWorkingDir,
			String myRequirements, String myExtraRSL) {

		// initialize RLS manager
		rlsmanager = new RLSManager();

		// initialize mappings-to-add
		mappingsToAdd = new ArrayList<String>();

		// determine the globus location
		Properties env = new Properties();
		try {
			env.load(Runtime.getRuntime().exec("env").getInputStream());
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		globusLocation = ""; //(String) env.get("GLOBUS_LOCATION");

		// determine the local hostname
		try {
			rftServiceHost = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		executable = myExecutable;

		// hacks for replicates, mpi, and mem
		if (myArguments.matches(".*--replicates \"[0-9]+\" .*")) {
			replicates = myArguments.substring(myArguments
					.indexOf("replicates") + 11, myArguments.indexOf(" ",
					myArguments.indexOf("replicates") + 11));
			log.debug("Number of replicates is: " + replicates);
			// strip the quotes off the replicates value
			replicates = replicates.substring(1, replicates.length() - 1);
			reps = Integer.parseInt(replicates);
			// remove the replicates argument
			myArguments = myArguments.replaceFirst("--replicates \"[0-9]+\" ",
					"");
		}
		if (myArguments.matches(".*-replicates \"[0-9]+\" .*")) {
			// this block mainly for the IM grid service
			replicates = myArguments.substring(myArguments
					.indexOf("replicates") + 11, myArguments.indexOf(" ",
					myArguments.indexOf("replicates") + 11));
			log.debug("Number of replicates is: " + replicates);
			// strip the quotes off the replicates value
			replicates = replicates.substring(1, replicates.length() - 1);
			reps = Integer.parseInt(replicates);
			// remove the replicates argument
			myArguments = myArguments.replaceFirst("-replicates \"[0-9]+\" ",
					"");
		}
		if (myArguments.matches(".*--mpi \"[0-9]+\" .*")) {
			cpus = myArguments.substring(myArguments.indexOf("mpi") + 4,
					myArguments.indexOf(" ", myArguments.indexOf("mpi") + 4));
			log.debug("This is an MPI job!  Number of processors: " + cpus);
			if (cpus.length() <= 2) {
				// this was probably an empty value
				cpus = "8"; // default to 8 cpus
				num_cpus = 8;
			} else {
				// strip the quotes off the mpi value
				cpus = cpus.substring(1, cpus.length() - 1);
				num_cpus = Integer.parseInt(cpus);
			}
			job_type = "mpi";
			// remove the mpi argument
			myArguments = myArguments.replaceFirst("--mpi \"[0-9]+\" ", "");
		}
		if (myArguments.matches(".*--mem \"[0-9]+\" .*")) {
			max_memory = myArguments.substring(myArguments.indexOf("mem") + 4,
					myArguments.indexOf(" ", myArguments.indexOf("mem") + 4));
			log.debug("Maximum memory has been specified! memory: "
					+ max_memory);
			// strip the quotes off the mem value
			max_memory = max_memory.substring(1, max_memory.length() - 1);
			max_mem = new Integer(max_memory);

			// remove the mem argument
			myArguments = myArguments.replaceFirst("--mem \"[0-9]+\" ", "");
		}

		String[] tempArguments = myArguments.split(" ");

		// go through this arguments array and combine quoted elements
		for (int i = 0; i < tempArguments.length; i++) {
			if (tempArguments[i].matches(".*\".*")
					&& !tempArguments[i].matches(".*\".*\".*")) {
				for (int j = i + 1; j < tempArguments.length; j++) {
					if (tempArguments[j].matches(".*\".*")) {
						for (int k = i + 1; k <= j; k++) {
							tempArguments[i] += " " + tempArguments[k];
							tempArguments[k] = "";
						}
						i = j + 1;
						break;
					}
				}
			}
		}

		// go through the arguments array and remove quotes
		for (int i = 0; i < tempArguments.length; i++) {
			if (tempArguments[i].indexOf("\"") >= 0) {
				tempArguments[i] = tempArguments[i].substring(0,
						tempArguments[i].indexOf("\""))
						+ tempArguments[i].substring(tempArguments[i]
								.indexOf("\"") + 1);
				i -= 1;
			}
		}

		// we need to strip off path information associated with file name
		// arguments
		for (int j = 0; j < tempArguments.length; j++) {
			String tempArg = tempArguments[j];
			if (tempArg.lastIndexOf("/") != -1
					&& tempArg.lastIndexOf("/") != tempArg.length() - 1) {
				// it's possible we have path information to strip off
				String putativeFileName = tempArg.substring(tempArg
						.lastIndexOf("/") + 1);
				// now search shared and per-job input files for this string
				boolean found_match = false;
				for (int i = 0; i < sharedlist.size(); i++) {
					File myFile = new File(sharedlist.get(i));
					String myFileName = myFile.getName();
					if (myFileName.equals(putativeFileName)) {
						found_match = true;
						break;
					}
				}
				if (found_match == false) {
					for (int i = 0; i < perjoblist.size(); i++) {
						for (int k = 0; k < perjoblist.get(i).length; k++) {
							File myFile = new File(perjoblist.get(i)[k]);
							String myFileName = myFile.getName();
							if (myFileName.equals(putativeFileName)) {
								found_match = true;
								break;
							}
							if (found_match == true) {
								break;
							}
						}
					}
				}
				if (found_match == true) {
					tempArguments[j] = putativeFileName;
				}
			}
		}

		arguments = tempArguments;
		scheduler = myScheduler;
		resource = myResource;
		arch_os = myArchOs;
		sharedFiles = sharedlist;
		perJobFiles = perjoblist;
		output_files = myOutput_files;

		runtime_estimate_seconds = myRuntimeEstimate;
		log.debug("runtime estimate is: "
				+ (new Integer(runtime_estimate_seconds)).toString());

		workingDir = myWorkingDir;
		workingDirBase = workingDir.substring(0, workingDir.length() - 1);
		workingDirBase = workingDirBase.substring(0,
				workingDirBase.lastIndexOf("/"));
		stagingDir = workingDir.substring(0, workingDir.length() - 1);
		stagingDir = stagingDir.substring(stagingDir.lastIndexOf("/") + 1);
		cacheDir = workingDirBase + "/cache/";

		extraRSL = myExtraRSL;
		if (extraRSL != null && extraRSL != "") {

			// check to see if we have nested environment tags into the extra
			// RSL. they should be in the following form:
			// <environment><name> ... </name><value> ...
			// </value></environment><environment><name> ... </name><value> ...
			// </value></environment>
			String startEnv = "<environment>";
			String lastEnv = "</environment>";
			int firstEnvIndex = extraRSL.indexOf(startEnv);
			int lastEnvIndex = extraRSL.lastIndexOf(lastEnv);

			if (firstEnvIndex != -1 && lastEnvIndex != -1) {
				environment = extraRSL.substring(firstEnvIndex, lastEnvIndex
						+ lastEnv.length());
			} else {
				environment = "";
			}

			// check to see if stdin is included for this job. it should be in
			// the following form:
			// <stdin>file:/// ... </stdin>
			String startStdin = "<stdin>";
			String lastStdin = "</stdin>";
			int firstStdinIndex = extraRSL.indexOf(startStdin);
			int lastStdinIndex = extraRSL.indexOf(lastStdin);

			if (firstStdinIndex != -1 && lastStdinIndex != -1) {
				stdin = extraRSL.substring(firstStdinIndex, lastStdinIndex
						+ lastStdin.length());
			} else {
				stdin = "";
			}
		}

		createXML();
	}