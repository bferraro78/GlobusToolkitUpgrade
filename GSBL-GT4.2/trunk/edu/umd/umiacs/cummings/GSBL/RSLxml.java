/**
 * @author Adam Bazinet
 */
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
 * Create an RSL XML document for use with a GT4ManagedJob.
 */
public class RSLxml {

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
	private String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

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
	public RSLxml(String myExecutable, String myArguments, String myScheduler,
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
		globusLocation = (String) env.get("GLOBUS_LOCATION");

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

	/**
	 * Construct the XML document.
	 */
	private void createXML() {

		// if matchmaking is set, perform scheduling
		if (scheduler.equals("matchmaking")) {
			try {
				Runtime r = Runtime.getRuntime();
				log.debug("Attempting to schedule job...\n");
				// if(reps >= 10) { // let resource information update, so sleep
				// for two and a half minutes
				try {
					Thread.sleep(150000);
				} catch (Exception e) {
					log.error("Exception: " + e);
				}
				// }

				String command = globusLocation + "/get_resource.pl "
						+ executable + " " + job_type + " ";
				if (max_memory.equals("")) {
					command = command + "0 ";
				} else {
					command = command + max_memory + " ";
				}

				if (runtime_estimate_seconds == -1) {
					command = command + "-1";
				} else {
					command = command
							+ (new Integer(runtime_estimate_seconds))
									.toString();
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

				// assign resource, arch, os, and scheduler
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

		// break apart architecture and operating system
		architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// add a hook for windows executables
		if (os.equals("WIN")) {
			if (executable.indexOf(".r") != -1) {
				executable = "setR.bat";
			} else if (!resource.equals("BOINC")) {
				executable = executable + ".exe";
			}
		}

		if (executable.indexOf(".r") != -1
				&& scheduler
						.equals("https://128.8.10.61:8443/wsrf/services/ManagedJobFactoryService")
				&& os.equals("OSX")) {
			executable = "one_proc_driver_GRIDIRON.r";
		}

		// add hook for mpi jobs
		if (job_type.equals("mpi") && executable.indexOf("java") == -1) {
			executable = executable + "_mpi";
		}

		document += header;

		// add starting job tag
		document += "<job>\n";

		String full_path_to_executable = globusLocation + "/applications/"
				+ architecture + "/" + os + "/" + executable;
		String md5sum_of_executable = RLSManager
				.getMD5Sum(full_path_to_executable);
		String md5sum_executable_prefix = md5sum_of_executable.substring(0, 3);

		// add executable tag
		if (executable.indexOf("java") == -1) { // specify the full path to the
												// executable in the
												// applications cache
			document += "<executable>${GLOBUS_SCRATCH_DIR}/applications/"
					+ md5sum_executable_prefix + "/" + md5sum_of_executable
					+ "/" + executable + "</executable>\n";

			// if((reps > 1 || job_type.equals("mpi")) &&
			// executable.indexOf("java") == -1) { // specify the full path to
			// the executable
			// document += "<executable>${GLOBUS_SCRATCH_DIR}/" + stagingDir +
			// "/" + executable + "</executable>\n";
			// } else if(executable.indexOf("java") == -1){
			// if(executable.length() > 4 &&
			// (executable.substring(executable.length() - 4)).equals("ckpt")) {
			// document += "<executable>${GLOBUS_SCRATCH_DIR}/" + stagingDir +
			// "/" + executable + "</executable>\n";
			// } else {
			// document += "<executable>" + executable + "</executable>\n";
			// }

		} else { // catch java
			if (scheduler
					.equals("https://128.8.141.68:8443/wsrf/services/ManagedJobFactoryService")
					|| scheduler
							.equals("https://128.8.120.157:8443/wsrf/services/ManagedJobFactoryService")) {
				// lysine or asparagine

				executable = "/fs/mikeproj/sw/RedHat9-32/bin/java";
				document += "<executable>" + executable + "</executable>\n";

			} else if (scheduler
					.equals("https://128.8.10.42:8443/wsrf/services/ManagedJobFactoryService")
					|| scheduler
							.equals("https://128.8.111.9:8443/wsrf/services/ManagedJobFactoryService")) {
				// deepthought or seil
				log.debug("this job assigned to DT or SEIL");

				executable = "/afs/glue.umd.edu/software/java/current/sys/bin/java";
				document += "<executable>" + executable + "</executable>\n";

				// if a job got tasked to SEIL, let's check to make sure memory
				// is <= 1000 ... if not, send to DT
				if (max_mem == null) {
					log.debug("max_mem is null!  (doh!)");
				} else {
					log.debug("max_mem is: " + max_mem.toString());
				}
				if (scheduler
						.equals("https://128.8.111.9:8443/wsrf/services/ManagedJobFactoryService")
						&& max_mem != null && max_mem.intValue() > 1000) {
					log.debug("set scheduler to DT");
					scheduler = "https://128.8.10.42:8443/wsrf/services/ManagedJobFactoryService";
				}

			} else if (scheduler
					.equals("https://130.85.58.2:8443/wsrf/services/ManagedJobFactoryService")) {
				executable = "/usr/bin/java";
				// java on bluegrit's blades in this location is 1.4.2, however
				document += "<executable>" + executable + "</executable>\n";
			} else { // default to ours (catches valine, now)
				executable = "/fs/mikeproj/sw/RedHat9-32/bin/java";
				document += "<executable>" + executable + "</executable>\n";
			}
		}

		// add directory tag
		if (reps > 1) {
			document += "<directory>${GLOBUS_SCRATCH_DIR}/" + stagingDir + "/"
					+ stagingDir + ".output</directory>\n";
		} else {
			document += "<directory>${GLOBUS_SCRATCH_DIR}/" + stagingDir
					+ "</directory>\n";
		}

		// add argument tags (and ensuring MARXAN doesn't get any arguments...
		// fix this hack at some point)
		if (executable.indexOf("Marxan") == -1) {
			for (int i = 0; i < arguments.length; i++) {
				if (!arguments[i].equals("")) {
					document += "<argument>" + arguments[i] + "</argument>\n";
				}
			}
		}

		// if environment variables need to be set, insert here
		if (environment != null && environment != "") {
			document += environment + "\n";
		}

		// if stdin is defined, insert here
		if (stdin != null && stdin != "") {
			document += stdin + "\n";
		}

		// add stdout tag
		document += "<stdout>${GLOBUS_SCRATCH_DIR}/" + stagingDir
				+ "/stdout</stdout>\n";

		// add stderr tag
		document += "<stderr>${GLOBUS_SCRATCH_DIR}/" + stagingDir
				+ "/stderr</stderr>\n";

		// add count element for multiple, mpi, and multiple mpi
		if (reps > 1 && job_type.equals("single")) {
			document += "<count>" + replicates + "</count>\n";
		} else if (job_type.equals("mpi")) {
			document += "<count>" + cpus + "</count>\n";
		}

		if (job_type.equals("mpi")) { // if job_type equals mpi, specify this
										// explicitly
			document += "<jobType>mpi</jobType>\n";
		} else if (reps == 1) { // specify single job explicitly (holding off on
								// multiple because I don't know about condor
								// implications)
			document += "<jobType>single</jobType>\n";
		}

		// if this is a Condor job with checkpointing enabled, add the jobType
		// tags with value 'condor'
		// due to poor Globus coding, this element must go after the <stderr>
		// tag for the RSL to be parsed correctly
		if (executable.length() > 4
				&& (executable.substring(executable.length() - 4))
						.equals("ckpt") && resource.equals("Condor")) {
			document += "<jobType>condor</jobType>\n";
		}

		document += "<fileStageIn><maxAttempts>50</maxAttempts>\n";

		// create the empty directory if it doesn't exist
		File emptyDir = new File(globusLocation + "/empty_dir/");
		try {
			emptyDir.mkdir();
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		// if reps > 1, create an 'output' folder in our working directory and
		// fill it with sub-job folders
		if (reps > 1) {
			File outputDir = new File(workingDir + stagingDir + ".output/");
			try {
				outputDir.mkdir();
				File tempJobDir = null;
				for (int i = 0; i < reps; i++) {
					tempJobDir = new File(workingDir + stagingDir
							+ ".output/job" + i + "/");
					tempJobDir.mkdir();
				}
			} catch (Exception e) {
				log.error("Exception: " + e);
			}
		}

		// first, we need to transfer the empty directory to cause it to be
		// created remotely
		document += "<transfer>\n";
		document += "<sourceUrl>gsiftp://" + rftServiceHost + ":2811/"
				+ globusLocation + "/empty_dir/</sourceUrl>";
		document += "<destinationUrl>file:///${GLOBUS_SCRATCH_DIR}/"
				+ stagingDir + "/</destinationUrl>";
		document += "</transfer>\n";

		// if reps > 1, transfer the 'output' folder along with sub-job folders
		if (reps > 1) {
			document += "<transfer>\n";
			document += "<sourceUrl>gsiftp://" + rftServiceHost + ":2811/"
					+ workingDir + stagingDir + ".output/</sourceUrl>";
			document += "<destinationUrl>file:///${GLOBUS_SCRATCH_DIR}/"
					+ stagingDir + "/" + stagingDir
					+ ".output/</destinationUrl>";
			document += "</transfer>\n";
		}

		if (executable.indexOf("java") == -1 && !resource.equals("BOINC")) {
			checkCache(md5sum_of_executable, executable, true);
		}

		// add file staging directives for input files
		if (sharedFiles != null && sharedFiles.size() > 0) {

			// add hack to transfer one_proc_driver_CLFSCONDOR.r if we're
			// running on a WINDOWS machine under Condor...
			if (executable.equals("setR.bat")) {
				document += "<transfer>\n";
				document += "<sourceUrl>gsiftp://" + rftServiceHost + ":2811"
						+ globusLocation + "/applications/" + architecture
						+ "/" + os + "/"
						+ "one_proc_driver_CLFSCONDOR.r</sourceUrl>";
				document += "<destinationUrl>file:///${GLOBUS_SCRATCH_DIR}/cache/one_proc_driver_CLFSCONDOR.r</destinationUrl>";
				document += "</transfer>\n";
			}

			for (int i = 0; i < sharedFiles.size(); i++) {
				String sharedFile = sharedFiles.get(i);
				String md5sum_of_file = "";
				String size_of_file = "";
				String filename = "";
				if (sharedFile.indexOf("/") == -1) {
					filename = sharedFile;
					log.error("file should have a '/' in it: " + sharedFile);
				} else {
					// chop off md5sum_prefix
					sharedFile = sharedFile
							.substring(sharedFile.indexOf("/") + 1);

					md5sum_of_file = sharedFile.substring(0,
							sharedFile.indexOf("/"));

					filename = sharedFile
							.substring(sharedFile.indexOf("/") + 1);
				}
				checkCache(md5sum_of_file, filename, false);
			}
		}

		if (perJobFiles != null && perJobFiles.size() > 0) {
			for (int i = 0; i < perJobFiles.size(); i++) {
				String[] files = perJobFiles.get(i);
				for (int j = 0; j < files.length; j++) {
					String perJobFile = files[j];
					String md5sum_of_file = "";
					String filename = "";
					if (perJobFile.indexOf("/") == -1) {
						log.error("file should have a '/' in it: " + perJobFile);
					} else {
						// chop off md5sum_prefix
						perJobFile = perJobFile.substring(perJobFile
								.indexOf("/") + 1);

						md5sum_of_file = perJobFile.substring(0,
								perJobFile.indexOf("/"));
						filename = perJobFile
								.substring(perJobFile.indexOf("/") + 1);
					}
					checkCache(md5sum_of_file, filename, false);
				}
			}
		}

		if (!application_upload_string.equals("")) {
			document += application_upload_string;
		}

		if (!file_upload_string.equals("")) {
			document += file_upload_string;
		}

		document += "</fileStageIn>\n";
		document += "<fileStageOut><maxAttempts>50</maxAttempts>\n";

		// if reps > 1, transfer back the entire output sub-directory
		if (reps > 1) {
			document += "<transfer>\n";
			document += "<sourceUrl>file:///${GLOBUS_SCRATCH_DIR}/"
					+ stagingDir + "/" + stagingDir + ".output/</sourceUrl>";
			document += "<destinationUrl>gsiftp://" + rftServiceHost + ":2811"
					+ workingDir + stagingDir + ".output/</destinationUrl>";
			document += "</transfer>\n";
		} else {
			// add file staging directives for stdout and stderr
			document += "<transfer>\n";
			document += "<sourceUrl>file:///${GLOBUS_SCRATCH_DIR}/"
					+ stagingDir + "/stdout</sourceUrl>";
			document += "<destinationUrl>gsiftp://" + rftServiceHost + ":2811"
					+ workingDir + "stdout</destinationUrl>";
			document += "</transfer>\n";
			document += "<transfer>\n";
			document += "<sourceUrl>file:///${GLOBUS_SCRATCH_DIR}/"
					+ stagingDir + "/stderr</sourceUrl>";
			document += "<destinationUrl>gsiftp://" + rftServiceHost + ":2811"
					+ workingDir + "stderr</destinationUrl>";
			document += "</transfer>\n";

			// add file staging directives for output files
			if (output_files != null && output_files.length > 0) {
				for (int i = 0; i < output_files.length; i++) {
					document += "<transfer>\n";
					document += "<sourceUrl>file:///${GLOBUS_SCRATCH_DIR}/"
							+ stagingDir + "/" + output_files[i]
							+ "</sourceUrl>";
					document += "<destinationUrl>gsiftp://" + rftServiceHost
							+ ":2811" + workingDir + output_files[i]
							+ "</destinationUrl>";
					document += "</transfer>\n";
				}
			}
		}

		document += "</fileStageOut>\n";

		// add file cleanup directives for Grid Services selectively
		// if(executable.indexOf("Marxan") == -1) {

		document += "<fileCleanUp><maxAttempts>50</maxAttempts>\n";
		document += "<deletion>\n";
		document += "<file>file:///${GLOBUS_SCRATCH_DIR}/" + stagingDir
				+ "/</file>\n";
		document += "</deletion>\n";
		document += "</fileCleanUp>\n";

		// }

		// if the resource is Condor, add appropriate extensions
		if (resource.equals("Condor")) {

			document += "<extensions>\n";
			// adding memory maximum for what used to be only GARLI
			if (!max_memory.equals("")) {
				document += "<min_memory>" + max_memory + "</min_memory>\n";
			}
			document += "<architecture>" + architecture + "</architecture>\n";
			document += "<operating_system>" + os + "</operating_system>\n";
			document += "<should_transfer_files>YES</should_transfer_files>\n";
			document += "<when_to_transfer_output>ON_EXIT</when_to_transfer_output>\n";

			// handle sharedFiles
			if (sharedFiles != null && sharedFiles.size() > 0) {
				document += "<transfer_input_files>";
				for (int i = 0; i < sharedFiles.size(); i++) {
					if (i == sharedFiles.size() - 1) {
						document += "${GLOBUS_SCRATCH_DIR}/cache/"
								+ sharedFiles.get(i);
					} else {
						document += "${GLOBUS_SCRATCH_DIR}/cache/"
								+ sharedFiles.get(i) + ",";
					}
				}
				// add hack to transfer one_proc_driver_CLFSCONDOR.r if we're
				// running on a WINDOWS machine under Condor...
				if (executable.equals("setR.bat")) {
					if (reps > 1) {
						document += ",${GLOBUS_SCRATCH_DIR}/cache/one_proc_driver_CLFSCONDOR.r";
					} else {
						document += ",${GLOBUS_SCRATCH_DIR}/cache/one_proc_driver_CLFSCONDOR.r";
					}
				}
				document += "</transfer_input_files>\n";
			}
			// handle perJobFiles
			transferPerjobFiles();
			transferOutputFiles();

			if (symlinks_string != null && !symlinks_string.equals("")) {
				document += "<symlinks>" + symlinks_string + "</symlinks>\n";
			}

			document += "<stream_output>FALSE</stream_output>\n";
			document += "<stream_error>FALSE</stream_error>\n";
			document += "</extensions>\n";
		} else if ((resource.equals("PBS") || resource.equals("SGE"))
				&& job_type.equals("single")) {
			document += "<extensions>\n";
			if (!max_memory.equals("")) {
				document += "<max_memory>" + max_memory + "</max_memory>\n";
			}

			// handle sharedfiles
			sharedInputFiles();

			// handle perjobfiles
			perjobInputFiles();
			document += "<nodes>" + replicates + "</nodes>\n";

			if (symlinks_string != null && !symlinks_string.equals("")) {
				document += "<symlinks>" + symlinks_string + "</symlinks>\n";
			}
			document += "</extensions>\n";
		} else if ((resource.equals("PBS") || resource.equals("SGE"))
				&& job_type.equals("mpi")) {
			document += "<extensions>\n";
			if (max_mem != null) {
				Integer memory = new Integer(max_mem.intValue() * num_cpus);
				document += "<max_memory>" + memory.toString()
						+ "</max_memory>\n";
			}

			sharedInputFiles();
			perjobInputFiles();

			document += "<nodes>" + cpus + "</nodes>\n";

			if (reps > 1) {
				// for multiple mpi jobs, add the replicates extension
				document += "<replicates>" + replicates + "</replicates>\n";
			}

			if (symlinks_string != null && !symlinks_string.equals("")) {
				document += "<symlinks>" + symlinks_string + "</symlinks>\n";
			}
			document += "</extensions>\n";
		} else if (resource.equals("BOINC")) {
			document += "<extensions>\n";
			if (!max_memory.equals("")) {
				document += "<maxMemory>" + max_memory + "</maxMemory>\n";
			}
			if (runtime_estimate_seconds != -1) {
				document += "<runtime_estimate>" + runtime_estimate_seconds
						+ "</runtime_estimate>\n";
			}
			if (sharedFiles != null && sharedFiles.size() > 0) {
				document += "<transfer_input_files>";
				for (int i = 0; i < sharedFiles.size(); i++) {
					if (i == sharedFiles.size() - 1) {
						document += "${GLOBUS_SCRATCH_DIR}/cache/"
								+ sharedFiles.get(i);
					} else {
						document += "${GLOBUS_SCRATCH_DIR}/cache/"
								+ sharedFiles.get(i) + ",";
					}
				}
				document += "</transfer_input_files>\n";
			}
			transferPerjobFiles();
			transferOutputFiles();

			if (symlinks_string != null && !symlinks_string.equals("")) {
				document += "<symlinks>" + symlinks_string + "</symlinks>\n";
			}

			document += "</extensions>\n";
		}

		// close the job tag
		document += "</job>\n";
	}

	private void transferPerjobFiles() {
		if (perJobFiles != null && perJobFiles.size() > 0) {
			document += "<transfer_perjob_files>";
			for (int i = 0; i < perJobFiles.size(); i++) {
				String[] tempcouples = perJobFiles.get(i);
				for (int j = 0; j < tempcouples.length; j++) {
					if (j < tempcouples.length - 1) { // add the ':' couple
														// delimiter
						document += "${GLOBUS_SCRATCH_DIR}/cache/"
								+ tempcouples[j] + ":";
					} else {
						document += "${GLOBUS_SCRATCH_DIR}/cache/"
								+ tempcouples[j];
					}
				}
				if (i < perJobFiles.size() - 1) { // add the ',' job
													// delimiter
					document += ",";
				}
			}
			document += "</transfer_perjob_files>\n";
		}
	}

	private void transferOutputFiles() {
		if (output_files != null && output_files.length > 0) {
			document += "<transfer_output_files>";
			for (int i = 0; i < output_files.length; i++) {
				if (i == output_files.length - 1) {
					document += output_files[i];
				} else {
					document += output_files[i] + ",";
				}
			}
			document += "</transfer_output_files>\n";
		}
	}

	private void perjobInputFiles() {
		if (perJobFiles != null && perJobFiles.size() > 0) {
			document += "<perjob_input_files>";
			for (int i = 0; i < perJobFiles.size(); i++) {
				String[] tempcouples = perJobFiles.get(i);
				for (int j = 0; j < tempcouples.length; j++) {
					if (j < tempcouples.length - 1) { // add the ':' couple
														// delimiter
						document += "${GLOBUS_SCRATCH_DIR}/cache/"
								+ tempcouples[j] + ":";
					} else {
						document += "${GLOBUS_SCRATCH_DIR}/cache/"
								+ tempcouples[j];
					}
				}
				if (i < perJobFiles.size() - 1) { // add the ',' job
													// delimiter
					document += ",";
				}
			}
			document += "</perjob_input_files>\n";
		}
	}

	private void sharedInputFiles() {
		if (sharedFiles != null && sharedFiles.size() > 0) {
			document += "<shared_input_files>";
			for (int i = 0; i < sharedFiles.size(); i++) {
				if (i == sharedFiles.size() - 1) {
					document += "${GLOBUS_SCRATCH_DIR}/cache/"
							+ sharedFiles.get(i);
				} else {
					document += "${GLOBUS_SCRATCH_DIR}/cache/"
							+ sharedFiles.get(i) + ",";
				}
			}
			document += "</shared_input_files>\n";
		}
	}

	/**
	 * Checks RLS for the existence of this file in the cache on the remote
	 * resource, and acts appropriately.
	 */
	private void checkCache(String md5sum_of_file, String filename,
			boolean application) {

		String md5sum_prefix = "";
		if (md5sum_of_file.length() > 3) {
			md5sum_prefix = md5sum_of_file.substring(0, 3);
		}

		String targetDir = "";
		if (application == true) {
			targetDir = "applications";
		} else {
			targetDir = "cache";
		}

		// adding hack for MARXAN newline file
		if (filename.equals("newline")) {
			// ensure the temp upload directory exists
			GSBLUtils.executeCommand("mkdir -p " + tempUploadLocation
					+ stagingDir + "/" + targetDir);
			// write out a file containing a newline
			String newline = "\n";
			try {
				FileWriter fileWriter = new FileWriter(tempUploadLocation
						+ stagingDir + "/" + targetDir + "/newline");
				BufferedWriter bfWriter = new BufferedWriter(fileWriter);
				bfWriter.write(newline);
				bfWriter.close();
			} catch (Exception e) {
				log.error("Exception: " + e);
			}
		} else {

			String filepath = md5sum_prefix + "/" + md5sum_of_file + "/"
					+ filename;
			String size_of_file = "";
			if (application == true) {
				size_of_file = RLSManager.getFileSize(globusLocation
						+ "/applications/" + architecture + "/" + os + "/"
						+ filename);
			} else {
				size_of_file = RLSManager.getFileSize(cacheDir + filepath);
			}

			// add file staging directive for files if they do not already exist
			// in the cache!
			ArrayList<String> pfns = rlsmanager.getPFNs(md5sum_of_file);
			// search through results to see if file exists on remote resource
			// get IP address of remote resource to which this job is being
			// scheduled
			String resourceIP = scheduler.substring(8,
					scheduler.indexOf(":", 8));
			boolean foundMatchingIP = false;
			boolean foundMatchingFile = false;
			for (String pfn : pfns) {
				if (pfn.indexOf(resourceIP) != -1
						&& pfn.indexOf("${GLOBUS_SCRATCH_DIR}") != -1) { // scratch
																			// dir
																			// should
																			// exist,
					// which avoids confusion with grid server
					// we have a matching IP, so this unique file should exist
					// on the remote resource
					foundMatchingIP = true;
					if (pfn.indexOf(filename) != -1) {
						// we have a matching file name, so we should not need
						// to create a new symlink
						foundMatchingFile = true;
					}
				}
			}

			// create string to use as location of file on remote resource (this
			// may not work as a URL for GridFTP transfers outside of GRAM)
			String fileLocation = "gsiftp://" + resourceIP
					+ ":2811/${GLOBUS_SCRATCH_DIR}/" + targetDir + "/"
					+ filepath;

			if (foundMatchingIP == false) { // we have to stage in the file,
											// register its new location with
											// RLS, and create a symlink

				// create an empty directory in our temporary cache whose name
				// is the md5sum of the file
				GSBLUtils.executeCommand("mkdir -p " + tempUploadLocation
						+ stagingDir + "/" + targetDir + "/" + md5sum_prefix
						+ "/" + md5sum_of_file);

				if (application == true) {
					GSBLUtils.executeCommand("cp " + globusLocation
							+ "/applications/" + architecture + "/" + os + "/"
							+ filename + " " + tempUploadLocation + stagingDir
							+ "/" + targetDir + "/" + md5sum_prefix + "/"
							+ md5sum_of_file + "/" + md5sum_of_file);
					if (application_upload_string.equals("")) {
						application_upload_string = "<transfer>\n<sourceUrl>gsiftp://"
								+ rftServiceHost
								+ ":2811"
								+ tempUploadLocation
								+ stagingDir
								+ "/"
								+ targetDir
								+ "/</sourceUrl>"
								+ "<destinationUrl>file:///${GLOBUS_SCRATCH_DIR}/"
								+ targetDir + "/</destinationUrl></transfer>\n";
					}
				} else {
					GSBLUtils.executeCommand("cp " + cacheDir + filepath + " "
							+ tempUploadLocation + stagingDir + "/" + targetDir
							+ "/" + md5sum_prefix + "/" + md5sum_of_file + "/"
							+ md5sum_of_file);
					if (file_upload_string.equals("")) {
						file_upload_string = "<transfer>\n<sourceUrl>gsiftp://"
								+ rftServiceHost
								+ ":2811"
								+ tempUploadLocation
								+ stagingDir
								+ "/"
								+ targetDir
								+ "/</sourceUrl>"
								+ "<destinationUrl>file:///${GLOBUS_SCRATCH_DIR}/"
								+ targetDir + "/</destinationUrl></transfer>\n";
					}
				}

				if (pfns.size() == 0) { // we need to create a new LFN -> PFN
										// mapping in RLS
					mappingsToAdd.add(md5sum_of_file + "," + fileLocation + ","
							+ "true," + size_of_file);
				} else { // we need to add a new LFN -> PFN mapping in RLS
					mappingsToAdd.add(md5sum_of_file + "," + fileLocation + ","
							+ "false," + size_of_file);
				}

				symlinks_string += "${GLOBUS_SCRATCH_DIR}/" + targetDir + "/"
						+ md5sum_prefix + "/" + md5sum_of_file + "/"
						+ md5sum_of_file + ":${GLOBUS_SCRATCH_DIR}/"
						+ targetDir + "/" + filepath + ",";

			} else if (foundMatchingIP == true && foundMatchingFile == false) {
				// we need to register the file with RLS and create a symlink

				mappingsToAdd.add(md5sum_of_file + "," + fileLocation + ","
						+ "false," + size_of_file);

				symlinks_string += "${GLOBUS_SCRATCH_DIR}/" + targetDir + "/"
						+ md5sum_prefix + "/" + md5sum_of_file + "/"
						+ md5sum_of_file + ":${GLOBUS_SCRATCH_DIR}/"
						+ targetDir + "/" + filepath + ",";

			} else if (foundMatchingIP == true && foundMatchingFile == true) {
				// we still need to update the requested time
				// and the in_use flag
				RLSManager.updateRequestedDate(fileLocation);
				// RLSManager.updateInUse(fileLocation, "1"); // 1 == "true"
			}
		}
	}

	/**
	 * Get the XML string created by this object.
	 * 
	 * @return A string representation of this XML document.
	 */
	public String getXML() {
		return document;
	}

	/**
	 * Write out the XML to "job_rsl.xml" in the working directory.
	 */
	public void writeXML() {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(workingDir
					+ "job_rsl.xml", true), true);
			pw.println(document);
			pw.close();
		} catch (java.io.IOException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Write out mappingToAdd to "mappingsToAdd" in the working directory
	 */
	public void writeMappingsToAdd() {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(workingDir
					+ "mappingsToAdd", true), true);
			for (String mapping : mappingsToAdd) {
				pw.println(mapping);
			}
			pw.close();
		} catch (java.io.IOException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Return arch_os.
	 */
	public String getArch_os() {
		return arch_os;
	}

	/**
	 * Return scheduler.
	 */
	public String getScheduler() {
		return scheduler;
	}

	/**
	 * Return resource.
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * Return cpus.
	 */
	public String getCPUs() {
		return cpus;
	}

	/**
	 * Return replicates.
	 */
	public String getReplicates() {
		return replicates;
	}

	/**
	 * Return reps.
	 */
	public int getReps() {
		return reps;
	}
}
