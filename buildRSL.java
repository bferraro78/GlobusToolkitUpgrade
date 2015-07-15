package edu.umd.umiacs.cummings.GSBL;

import java.lang.Runtime;
import java.lang.Integer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// For determining host name.
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Properties;

import java.io.*;

/**
 * Create an RSL for globusrun command.
 */
public class buildRSL {

	private static String rftServiceHost = "localhost";
	private static String tempUploadLocation = "/tmp/temp_uploads/";

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(RSLxml.class.getName());

	/**
	 * The RSL string.
	 */
	private String document = "";

	/**
	 * The GLOBUS_LOCATION.
	 */
	private static String globusLocation = "";  // Not needed in GT6.

	/**
	 * For RLS queries.
	 */
	private RLSManager rlsmanager = null;  // Might not be needed?

	/**
	 * Job submission variables.
	 */
	private String executable;
	private String[] arguments;
	private String scheduler;
	private String resource;
	private String unique_id; // job's unique_id, recently added
	private String arch_os;
	private String architecture;
	private String os;
	private String workingDirBase;  // Example: /export/grid_files/
	private String workingDir;  // Example: /export/grid_files/[0-9]*.[0-9]*
	private String stagingDir;  // Example: [0-9]*.[0-9]*
	private String cacheDir;  // Example: /export/grid_files/cache/

	private ArrayList<String> sharedFiles;
	private ArrayList<String[]> perJobFiles;

	private String[] output_files;
	private String extraRSL;
	private String environment;  // Used for environment variables.
	private String stdin;  // Used for programs that take input on stdin.

	private String symlinks_string = "";
	private String application_upload_string = "";
	private String file_upload_string = "";
	private ArrayList<String> mappingsToAdd = null;

	private String job_type = "single";  // Default is a single (serial) job.

	private String replicates = "1";  // Default is one job replicate.
	private int reps = 1;  // An integer form of the above.

	// Default is one processor; for an mpi job, 8 processors.
	private String cpus = "1";
	private int num_cpus = 1;

	// Default is not to specify anything.
	private String max_memory = "";
	private Integer max_mem = null;

	// -1 means an estimate has not been given.
	private int runtime_estimate_seconds = -1;

	/**
	 * RSL header.
	 */
	private String header = "globusrun -r ";

	/**
	 * Create an RSL document using the parameters provided.
	 * 
	 * @param myExecutable
	 * 				Path to executable.
	 * @param myArguments
	 * 				Arguments to executable.
	 * @param myScheduler
	 * @param myResource
	 * 				Resource this job will execute on, e.g. Fork, Condor, BOINC.
	 * @param myArchOs
	 * @param myRuntimeEstimate
	 * @param sharedlist
	 * @param perjoblist
	 * @param myOutput_files
	 * 				Array of output files from job execution.
	 * @param myWorkingDir
	 * 				Directory where output files from this job are put.
	 * @param myRequirements
	 * 				Requirements for the job (will be used when scheduling is
	 * 				implemented).
	 * @param myExtraRSL
	 * 				Extra RSL to be included in the job description. Must be
	 * 				preformatted XML.
	 */
	public buildRSL(String myExecutable, String myArguments, String myScheduler,
			String myResource, String myArchOs, int myRuntimeEstimate,
			ArrayList<String> sharedlist, ArrayList<String[]> perjoblist,
			String[] myOutput_files, String myWorkingDir,
			String myRequirements, String myExtraRSL, String myUnique_id) {

		// Initialize RLS manager.
		rlsmanager = new RLSManager();

		// Initialize mappings-to-add.
		mappingsToAdd = new ArrayList<String>();

		// Determine the globus location.
		Properties env = new Properties();
		try {
			env.load(Runtime.getRuntime().exec("env").getInputStream());
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		// env.get("GLOBUS_LOCATION") will evaluate to "" if undefined.
		globusLocation = (String) env.get("GLOBUS_LOCATION");

		/*
		// Determine the local hostname.
		try {
			rftServiceHost = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
		*/

		executable = myExecutable;

		// Hacks for replicates, mpi, and mem.
		if (myArguments.matches(".*--replicates \"[0-9]+\" .*")) {
			replicates = myArguments.substring((myArguments
					.indexOf("replicates") + 11), myArguments.indexOf(" ",
							(myArguments.indexOf("replicates") + 11)));
			log.debug("Number of replicates is: " + replicates);
			// Strip the quotes off the replicates value.
			replicates = replicates.substring(1, (replicates.length() - 1));
			reps = Integer.parseInt(replicates);
			// Remove the replicates argument.
			myArguments = myArguments.replaceFirst("--replicates \"[0-9]+\" ",
					"");
		}
		if (myArguments.matches(".*-replicates \"[0-9]+\" .*")) {
			// This block mainly for the IM grid service.
			replicates = myArguments.substring((myArguments
					.indexOf("replicates") + 11), myArguments.indexOf(" ",
							(myArguments.indexOf("replicates") + 11)));
			log.debug("Number of replicates is: " + replicates);
			// Strip the quotes off the replicates value.
			replicates = replicates.substring(1, (replicates.length() - 1));
			reps = Integer.parseInt(replicates);
			// Remove the replicates argument.
			myArguments = myArguments.replaceFirst("-replicates \"[0-9]+\" ",
					"");
		}
		if (myArguments.matches(".*--mpi \"[0-9]+\" .*")) {
			cpus = myArguments.substring((myArguments.indexOf("mpi") + 4),
					myArguments.indexOf(" ", (myArguments.indexOf("mpi") + 4)));
			log.debug("This is an MPI job!  Number of processors: " + cpus);
			if (cpus.length() < 3) {
				// This was probably an empty value
				cpus = "8";  // Default to 8 cpus.
				num_cpus = 8;
			} else {
				// Strip the quotes off the mpi value.
				cpus = cpus.substring(1, (cpus.length() - 1));
				num_cpus = Integer.parseInt(cpus);
			}
			job_type = "mpi";
			// Remove the mpi argument.
			myArguments = myArguments.replaceFirst("--mpi \"[0-9]+\" ", "");
		}
		if (myArguments.matches(".*--mem \"[0-9]+\" .*")) {
			max_memory = myArguments.substring((myArguments.indexOf("mem") + 4),
					myArguments.indexOf(" ", (myArguments.indexOf("mem") + 4)));
			log.debug("Maximum memory has been specified! memory: "
					+ max_memory);
			// Strip the quotes off the mem value.
			max_memory = max_memory.substring(1, (max_memory.length() - 1));
			max_mem = new Integer(max_memory);

			// Remove the mem argument.
			myArguments = myArguments.replaceFirst("--mem \"[0-9]+\" ", "");
		}

		String[] tempArguments = myArguments.split(" ");

		// Go through this arguments array and combine quoted elements.
		for (int i = 0; i < tempArguments.length; i++) {
			if (tempArguments[i].matches(".*\".*")
					&& !tempArguments[i].matches(".*\".*\".*")) {
				for (int j = (i + 1); j < tempArguments.length; j++) {
					if (tempArguments[j].matches(".*\".*")) {
						for (int k = i + 1; k <= j; k++) {
							tempArguments[i] += (" " + tempArguments[k]);
							tempArguments[k] = "";
						}
						i = (j + 1);
						break;
					}
				}
			}
		}

		// Go through the arguments array and remove quotes.
		for (int i = 0; i < tempArguments.length; i++) {
			if (tempArguments[i].indexOf("\"") >= 0) {
				tempArguments[i] = (tempArguments[i].substring(0,
						tempArguments[i].indexOf("\"")) + tempArguments[i]
						.substring(tempArguments[i].indexOf("\"") + 1);
				i--;
			}
		}

		/* We need to strip off path information associated with file name
		 * arguments.
		 */
		for (int j = 0; j < tempArguments.length; j++) {
			String tempArg = tempArguments[j];
			if ((tempArg.lastIndexOf("/") != -1)
					&& (tempArg.lastIndexOf("/") != (tempArg.length() - 1))) {
				// It's possible we have path information to strip off.
				String putativeFileName =
						tempArg.substring(tempArg.lastIndexOf("/") + 1);
				// Now search shared and per-job input files for this string.
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
		unique_id = myUnique_id;
		scheduler = myScheduler;
		resource = myResource;
		arch_os = myArchOs;
		sharedFiles = sharedlist;
		perJobFiles = perjoblist;
		output_files = myOutput_files;

		runtime_estimate_seconds = myRuntimeEstimate;
		log.debug("runtime estimate is: "
				+ (new Integer(runtime_estimate_seconds)).toString());

		workingDir = myWorkingDir;  /* /export/work/drupal/user_files/admin/job# */

		/*
		workingDirBase = workingDir.substring(0, (workingDir.length() - 1));
		workingDirBase =
				workingDirBase.substring(0, workingDirBase.lastIndexOf("/"));

		cacheDir = workingDirBase + "/cache/";
		*/

		extraRSL = myExtraRSL;
		if ((extraRSL != null) && (extraRSL != "")) {

			/* Check to see if we have nested environment tags into the extra
			 * RSL. they should be in the following form:
			 * <environment><name> ... </name><value> ... </value></environment>
			 */
			String startEnv = "<environment>";
			String lastEnv = "</environment>";
			int firstEnvIndex = extraRSL.indexOf(startEnv);
			int lastEnvIndex = extraRSL.lastIndexOf(lastEnv);

			if ((firstEnvIndex != -1) && (lastEnvIndex != -1)) {
				environment = extraRSL.substring(firstEnvIndex,
						(lastEnvIndex + lastEnv.length()));
			} else {
				environment = "";
			}

			/* Check to see if stdin is included for this job. It should be in
			 * the following form:
			 * <stdin>file:/// ... </stdin>
			 */
			String startStdin = "<stdin>";
			String lastStdin = "</stdin>";
			int firstStdinIndex = extraRSL.indexOf(startStdin);
			int lastStdinIndex = extraRSL.indexOf(lastStdin);

			if ((firstStdinIndex != -1) && (lastStdinIndex != -1)) {
				stdin = extraRSL.substring(firstStdinIndex, lastStdinIndex
						+ lastStdin.length());
			} else {
				stdin = "";
			}
		}

		createRSL();
	}


	public void createRSL() {
		StringBuilder doc = new StringBuilder();
		boolean stageIn = false;

		// If matchmaking is set, perform scheduling.
		if (scheduler.equals("matchmaking")) {
			try {
				Runtime r = Runtime.getRuntime();
				log.debug("Attempting to schedule job...\n");

				try {
					Thread.sleep(150000);
				} catch (Exception e) {
					log.error("Exception: " + e);
				}

				String command = (globusLocation + "/get_resource.pl "
						+ executable + " " + job_type + " ");
				if (max_memory.equals("")) {
					command += "0 ";
				} else {
					command += (max_memory + " ");
				}

				if (runtime_estimate_seconds == -1) {
					command += "-1";
				} else {
					command +=
							(new Integer(runtime_estimate_seconds)).toString();
				}

				Process proc = r.exec(command);
				String line = null;
				String resource_arch_os_scheduler = null;
				InputStream stdout = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(stdout);
				BufferedReader br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					resource_arch_os_scheduler = line;
				}
				br.close();

				// Assign resource, arch, os, and scheduler.
				String[] chunks = resource_arch_os_scheduler.split(" ", 3);
				log.debug("resource is: " + chunks[0]);
				resource = chunks[0];
				log.debug("arch_os is: " + chunks[1]);
				arch_os = chunks[1];
				log.debug("scheduler is: " + chunks[2]);
				scheduler = chunks[2];
			} catch (Exception e) {
				log.error("Exception: " + e);
			}
		}

		// Break apart architecture and operating system.
		architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// Add a hook for windows executables.
		if (os.equals("WIN")) {
			if (executable.indexOf(".r") != -1) {
				executable = "setR.bat";
			} else if (!resource.equals("BOINC")) {
				executable += ".exe";
			}
		}

		if ((executable.indexOf(".r") != -1) && scheduler
				.equals("https://128.8.10.61:8443/wsrf/services/ManagedJobFactoryService")
				&& os.equals("OSX")) {
			executable = "one_proc_driver_GRIDIRON.r";
		}

		// ex.) asparagine.umiacs.umd.edu
		String hostname = (String)env.get("HOSTNAME");
		// ex.) asparagine
		String host = hostname.substring(0, indexOf("."));

		doc.append(header).append(host);  // "globusrun -r asparagine"

		// Add executable.
		doc.append(" '&(executable = /fs/mikeproj/sw/RedHat9-32/bin/Garli-2.1_64)");
		
		// Add stdout.
		doc.append(" (stdout = ").append(workingDir).append("/stdout)");
		
		// Add stderr.
		doc.append(" (stderr = ").append(workingDir).append("/stderr)");

		// Stages in sharedFiles.
		if ((sharedFiles != null) && (sharedFiles.size() > 0)) {
			doc.append(" (file_stage_in =");
			stageIn = true;
			
			for (int i = 0; i < sharedFiles.size(); i++) {
				doc.append(" (gsiftp://".append(hostname).append("/")
						.append(workingDir).append("/")
						.append(sharedFiles.get(i)).append(" ")
						.append(workingDir).append("/")
						.append(sharedFiles.get(i)).append(")");
			}
		}

		// Stages in perJobFiles.
		if ((perJobFiles != null) && (perJobFiles.size() > 0)) {
			if (stageIn == false) {  // No sharedFiles.
				doc.append(" (file_stage_in =");
				stageIn = true;
			}
			for (int i = 0; i < perJobFiles.size(); i++) {
				String[] tempcouples = perJobFiles.get(i);
				for (int j = 0; j < tempcouples.length; j++) {
					doc.append(" (gsiftp://").append(hostname).append("/")
							.append(workingDir).append("/")
							.append(tempcouples[j]).append(" ")
							.append(workingDir).append("/")
							.append(tempcouples[j]).append(")");
				}
			}
		}

		if (stageIn == true) {
			doc.append(")");  // End file stage in.
		}

		// Stages outputFiles.
		doc.append(" (file_stage_out =");

		// If reps > 1, transfer back the entire output sub-directory.
		if (reps > 1) {

			doc.append(" (file:///").append(workingDir).append("/")
					.append(unique_id).append(".output/ ").append("gsiftp://")
					.append(hostname).append("/").append(workingDir)
					.append(unique_id).append(".output/)";


		} else {
			// Add file staging directives for stdout and stderr.
			doc.append(" (file:///").append(workingDir)
					.append("/stdout gsiftp://").append(hostname)
					.append("/").append(workingDir).append("/stdout) (file:///")
					.append(workingDir).append("/stderr gsiftp://")
					.append(hostname).append("/").append(workingDir)
					.append("/stderr)");
			
			// Add file staging directives for output files.
			if ((output_files != null) && (output_files.length > 0)) {
				for (int i = 0; i < output_files.length; i++) {
					doc.append(" (file:///").append(workingDir).append("/")
							.append(output_files[i]).append(" gsiftp://")
							.append(hostname).append("/").append(workingDir)
							.append("/").append(output_files[i]).append(")");
				}
			}
		}
		
		doc.append(")"); // End file stage out.
		
		document = doc.toString();
	}
}
