/**
 * @author Adam Bazinet
 * @author Ben Ferraro
 * @author Jordan Kiesel
 */
package edu.umd.grid.bio.garli.impl;

// GSBL classes.
import edu.umd.umiacs.cummings.GSBL.BeanToArguments;
import edu.umd.umiacs.cummings.GSBL.GSBLJobManager;
import edu.umd.umiacs.cummings.GSBL.GSBLService;
import edu.umd.umiacs.cummings.GSBL.GSBLRuntimeConfiguration;
import edu.umd.umiacs.cummings.GSBL.GSBLJob;
import edu.umd.umiacs.cummings.GSBL.GSBLUtils;

// For logging.
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.io.*;
import java.util.*;

// For garbage collection.
import java.lang.System;

// Stub classes.
import edu.umd.grid.bio.garli.stubs.GARLI.service.GARLIServiceAddressingLocator;
import edu.umd.grid.bio.garli.stubs.GARLIService.GARLIArguments;

// Place service specific imports here between the protection comments.
// BEGIN PROTECT: ServiceImports
import edu.umd.grid.bio.garli.shared.GARLIParser;
// END PROTECT: ServiceImports

public class GARLIService extends GSBLService {

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(GARLIService.class.getName());

	/**
	 * Arguments bean.
	 */    
	protected GARLIArguments myBean;

	/**
	 * Our job instance.
	 */
	private GSBLJob job = null;

	/**
	 * Runtime configuration. This is common to all service instances by virtue
	 * of being static.
	 */
	static protected GSBLRuntimeConfiguration runtimeConfig = null;

	/**
	 * Directory where data and scripts for determining runtime estimates
	 * resides.
	 */
	static protected String runtime_estimates_location = null;

	/**
	 * container_status location.
	 */
	static protected String container_status_location = null;

	/**
	 * Update interval - how frequently the service checks on the status of its
	 * jobs.
	 */
	static protected String update_interval_string = null;
	static protected int update_interval = 300000;  // Default is 5 minutes.
	static protected int update_max = 4800000;  // Default is 80 minutes.

	/**
	 * URL for updating Drupal job status.
	 */
	static protected String drupalUpdateURL = null;

	/**
	 * This is the name of the service.
	 */
	private String serviceName = "GARLI";

	// CONSTRUCTOR??

	// Load things from config files. We only want to do this once.
	static {
		try {
			Properties env = new Properties();
			env.load(Runtime.getRuntime().exec("env").getInputStream());
			// env.get("GLOBUS_LOCATION") will evaluate to "" if undefined.
			String globusLocation = (String) env.get("GLOBUS_LOCATION");
			runtimeConfig = new GSBLRuntimeConfiguration(globusLocation
					+ "/service_configurations/GARLI.runtime.xml");
			runtime_estimates_location = globusLocation + "/runtime_estimates/";

			/* container_status_location =
					GSBLUtils.getConfigElement("container_status.location"); */

			update_interval_string =
					GSBLUtils.getConfigElement("update_interval");

			// Split interval string into min and max.
			update_interval = Integer.parseInt(update_interval_string
					.substring(0, update_interval_string.indexOf(" ")));
			update_max = Integer.parseInt(update_interval_string
					.substring(update_interval_string.indexOf(" ") + 1));

			drupalUpdateURL = GSBLUtils.getConfigElement("drupal_update_url");

		} catch (Exception e) {
			log.error("Error loading config file information for GARLI: " + e);
		}
	}

	/**
	 * This is almost like our "main" method.
	 * @param myBean argument bean.
	 * @return true if service was started successfully.
	 */
	public boolean runService(GARLIArguments myBean) {
		this.myBean = myBean;
		String unique_id = null;

		// Create symlinks.
		// (Might not need because no symlinks are set in "GarliClient.java".)
		if ((myBean.getSymlinks() != null)
				&& !myBean.getSymlinks().equals("")) {
			makeSymlinks(myBean.getSymlinks());
		}

		String[] tempSharedFiles = myBean.getSharedFiles();
		if (tempSharedFiles == null) {
			tempSharedFiles = new String[0];
		}
		ArrayList<String> sharedFiles =
				new ArrayList<String>(Arrays.asList(tempSharedFiles));

		String[] tempPerJobArguments = myBean.getPerJobArguments();
		if (tempPerJobArguments == null) {
			tempPerJobArguments = new String[0];
		}
		ArrayList<String> perJobArguments =
				new ArrayList<String>(Arrays.asList(tempPerJobArguments));

		String[] tempPerJobFiles = myBean.getPerJobFiles();

		if (tempPerJobFiles == null) {
			tempPerJobFiles = new String[0];
		}
		ArrayList<String[]> perJobFiles = new ArrayList<String[]>();

		for (int i = 0; i < tempPerJobFiles.length; i++) {
			String filenames = tempPerJobFiles[i];
			String[] chunks = filenames.split(":");
			perJobFiles.add(chunks);
		}

		String myWorkingDir = myBean.getWorkingDir();
		setArguments(myBean, myWorkingDir);	

		// Read elements of the runtime configuration file.
		String executable = runtimeConfig.getExecutable();
		String argumentDescription = runtimeConfig.getArgumentDescription();
		String scheduler = runtimeConfig.getScheduler();
		String resource = runtimeConfig.getResource();
		String arch_os = runtimeConfig.getArchOs();
		String requirements = runtimeConfig.getRequirements();
		String extraRSL = null;

		String argumentString = null;
		try {
			BeanToArguments BTA =
					new BeanToArguments(argumentDescription);
			argumentString = BTA.getArgumentStringFromBean(myBean);
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		int reps = 1;
		Integer replicates = myBean.getReplicates();
		try {
			if ((replicates != null) && (replicates.intValue() > 1)) {
				reps = replicates.intValue();
			}
		} catch (Exception e) {
			log.error("Exception setting replicates: " + e);
		}

		int runtime_estimate_seconds = -1;  /* -1 means we don't have an
				estimate. */
		int runtime_estimate_seconds_recent = -1;  /* -1 means we don't have an
				estimate. */
		int searchreps = -1;
		int bootstrapreps = -1;

		// ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
		// BEGIN PROTECT: fileSetup

		String avail_mem = "";
		String runtime_estimate = "";
		String runtime_estimate_recent = "";

		String unique_patterns = "";
		String num_taxa = "";
		String actual_mem = "";

		GARLIParser gp = null;
		try {
			gp = new GARLIParser(myBean, myWorkingDir, false, false, true);
			avail_mem = gp.getAvailMem();
			log.debug("after GARLI parser: avail_mem = " + avail_mem);
		} catch (FileNotFoundException e) {
			System.out.println("Error rewriting config file: "
					+ myBean.getConfigFile() + " during service");
		} catch (IOException ie) {
			System.out.println("IOException while rewriting new config file during service: "
					+ ie);
		} catch (Exception e) {
			System.out.println("Unknown exception occurred while invoking the GARLI parser "
					+ e);
		}

		boolean doValidate = true;
		if (myBean.getNovalidate() != null) {
			if ((myBean.getNovalidate()).booleanValue() == true) {
				doValidate = false;
			}
		}

		// Produce a runtime estimate.
		if (doValidate) {
			if (myBean.getUniquepatterns() != null) {
				unique_patterns = myBean.getUniquepatterns();
			}
			if (myBean.getNumtaxa() != null) {
				num_taxa = myBean.getNumtaxa();
			}
			if (myBean.getActualmemory() != null) {
				actual_mem = myBean.getActualmemory();
			}

			// NEW WAY IS ACCESSING JOBID THRU BEAN
			unique_id = myBean.getJobID();

				/* Get the unique ID from the working directory. -- OLD WAY -- 
					myWorkingDir.substring(0, myWorkingDir.lastIndexOf("/"));
					unique_id = unique_id.substring(unique_id.lastIndexOf("/") + 1);
				*/

			// Formulate working directory for command.
			String workingDir = runtime_estimates_location + executable + "/";

			// Formulate command.
			String command = workingDir + "estimate_" + executable
					+ "_runtime.pl " + unique_id + " " + unique_patterns + " "
					+ num_taxa + " " + actual_mem + " ";
			String command2 = workingDir + "estimate_" + executable
					+ "_runtime_recent.pl " + unique_id + " " + unique_patterns
					+ " " + num_taxa + " " + actual_mem + " ";

			String datatype = myBean.getDatatype();
			ArrayList<String> distinctDatatype =
					GSBLUtils.returnDistinctGarliValues("datatype");
			if (datatype == null) {
				datatype = "nucleotide";
			} else if (!distinctDatatype.contains(datatype)) {
				datatype = "aminoacid";
			}

			String ratematrix = myBean.getRatematrix();
			ArrayList<String> distinctRateMatrix =
					GSBLUtils.returnDistinctGarliValues("ratematrix");
			if ((ratematrix == null)
					|| !distinctRateMatrix.contains(ratematrix)) {  /* Either
					not supplied or we haven't seen it before ... use default
					value. */
				if (datatype.equals("nucleotide")) {
					ratematrix = "6rate";
				} else if (datatype.equals("aminoacid")) {
					ratematrix = "dayhoff";  /* I'm picking this default since
							GARLI doesn't give one. */
				} else {  // Must be a codon model.
					ratematrix = "2rate";
				}
			}

			String statefrequencies = myBean.getStatefrequencies();
			ArrayList<String> distinctStateFrequencies =
					GSBLUtils.returnDistinctGarliValues("statefrequencies");
			if ((statefrequencies == null)
					|| (!distinctStateFrequencies.contains(statefrequencies))) {
					/* Either not supplied or we haven't seen it before ... use
					default value. */
				if (datatype.equals("nucleotide")) {
					statefrequencies = "estimate";
				} else if (datatype.equals("aminoacid")) {
					statefrequencies = "dayhoff";  /* I'm picking this default
							since GARLI doesn't give one. */
				} else {  // Must be a codon model.
					statefrequencies = "empirical";
				}
			}

			String ratehetmodel = myBean.getRatehetmodel();
			if (ratehetmodel == null) {  /* Rate heterogeneity model was not
					supplied, use default value. */
				if (datatype.equals("nucleotide")
						|| datatype.equals("aminoacid")) {
					ratehetmodel = "gamma";
				} else {  // Must be a codon model.
					ratehetmodel = "none";
				}
			}

			String numratecats = (myBean.getNumratecats()).toString();
			if (numratecats == null) {  /* Number of rate categories was not
					supplied, use default value. */
				if (datatype.equals("nucleotide")
						|| datatype.equals("aminoacid")) {
					numratecats = "4";
				} else {  // Must be a codon model.
					numratecats = "1";
				}
			}

			String invariantsites = myBean.getInvariantsites();
			if (invariantsites == null) {  /* Invariantsites was not supplied,
					use default value. */
				if (datatype.equals("nucleotide")
						|| datatype.equals("aminoacid")) {
					invariantsites = "estimate";
				} else {  // Must be a codon model.
					invariantsites = "none";
				}
			}

			command = command + datatype + " " + ratematrix + " "
					+ statefrequencies + " " + ratehetmodel + " " + numratecats
					+ " " + invariantsites;
			command2 = command2 + datatype + " " + ratematrix + " "
					+ statefrequencies + " " + ratehetmodel + " " + numratecats
					+ " " + invariantsites;

			/* RFMatrixUpdater.INSTANCE.addJob(job.getUnique_id(), unique_patterns,
					num_taxa, actual_mem, datatype, ratematrix,
					statefrequencies, ratehetmodel, numratecats,
					invariantsites); */

			log.debug("command is: " + command);
			log.debug("command2 is: " + command2);

			// Execute command and get output (the runtime estimate).
			runtime_estimate = GSBLUtils
					.executeCommandReturnOneLine(command, workingDir, true);
			runtime_estimate_recent = GSBLUtils
					.executeCommandReturnOneLine(command2, workingDir, true);

			log.debug("GARLI runtime estimate is: " + runtime_estimate);
			log.debug("GARLI recent runtime estimate is: "
					+ runtime_estimate_recent);

			if (!runtime_estimate.equals("")
					&& !runtime_estimate.equals("-1")) {
				runtime_estimate_seconds = Integer.parseInt(runtime_estimate);
			}

			if (!runtime_estimate_recent.equals("")
					&& !runtime_estimate_recent.equals("-1")) {
				runtime_estimate_seconds_recent =
						Integer.parseInt(runtime_estimate_recent);
			}

			/* Scale runtime estimate linearly based on searchreps and
			 * bootstrapreps.
			 */
			searchreps = Integer.valueOf(gp.getSearchreps());
			bootstrapreps = Integer.valueOf(gp.getBootstrapreplicates());

			runtime_estimate_seconds *= searchreps;
			runtime_estimate_seconds_recent *= searchreps;
			if (bootstrapreps > 0) {
				runtime_estimate_seconds *= bootstrapreps;
				runtime_estimate_seconds_recent *= bootstrapreps;
			}
		}

		/* Redo argument string - remove all arguments except for config file
		 * name.
		 */
		argumentString = "\"" + myBean.getConfigFile() + "\"";

		// Prepend replicates to the argument string.
		if(replicates != null) {
			argumentString = "--replicates \"" + replicates.toString() + "\" "
					+ argumentString;
		}

		// Prepend memory requirement to the argument string.
		argumentString = "--mem \"" + avail_mem + "\" " + argumentString;

		log.debug("argument string before hetero batch mucking is: "
				+ argumentString);

		// If hetero batch, replace conf file argument with all conf files.
		int i;
		if ((i = perJobArguments.indexOf("configFile")) != -1) {
			String csv = getArgument("configFile", perJobFiles, i);
			log.debug("CSV: " + csv + "i: " + i);
			log.debug("Config File from bean: " + myBean.getConfigFile());
			int first = argumentString.indexOf("\"" + myBean.getConfigFile()
					+ "\"");
			int last = first + myBean.getConfigFile().length() + 1;
			String beginning = argumentString.substring(0,first);
			if (last == -1) {
				argumentString = beginning + " " + csv;
			} else {
				String end = argumentString.substring(last+1);
				argumentString = beginning + " " + csv + " " + end;
			}
		}

		log.debug("argument string after hetero batch mucking is: "
				+ argumentString);

		String [] output_files = myBean.getOutputFiles();

		// Estimate split code here.

		// End estimate split.

		// END PROTECT: fileSetup
		// ----- ----- ----- END YOUR CODE ----- ----- ----- //

		try {
			if ((myBean.getSchedulerOverride() != null)
					&& !(myBean.getSchedulerOverride().equals(""))) {
				String[] chunks = myBean.getSchedulerOverride().split(" ", 3);
				log.debug("SCHEDULER OVERRIDE: resource is: " + chunks[0]);
				resource = chunks[0];
				log.debug("SCHEDULER OVERRIDE: arch_os is: " + chunks[1]);
				arch_os = chunks[1];
				log.debug("SCHEDULER OVERRIDE: scheduler is: " + chunks[2]);
				scheduler = chunks[2];
			}

			GSBLJob job = new GSBLJob(executable, argumentString, scheduler,
					resource, arch_os, myWorkingDir,
					runtime_estimate_seconds_recent, sharedFiles, perJobFiles,
					output_files, requirements, extraRSL, unique_id);

			if (scheduler.equals("matchmaking")) {
				scheduler = job.getScheduler();  /* These values could have
						changed! */
				resource = job.getResource();
				arch_os = job.getArch_os();
			}

			/* globusrun command to execute */
			String globus_command = "globusrun -f -batch -r " + job.getHost();
			
			// Add job manager.
			if (resource.equals("Condor")) {
				globus_command += ("/jobmanager-condor");
			} else if (resource.equals("PBS")) {
				globus_command += ("/jobmanager-pbs");
			} else if (resource.equals("SGE")) {
				globus_command += ("/jobmanager-sge");
			} else if (resource.equals("BOINC")) {
				globus_command += ("/jobmanager-boinc");  // Does this exist? idk
			} else {
				globus_command += " ";
			}

			/* Executes a globusrun command */
			String[] command = {globus_command, "rslString"};
			GSBLUtils.executeCommand(command, false); 



			// COMMENTED OUT FOR TESTING PURPOSES
			/* 
			GSBLJobManager myJob = new GSBLJobManager(job, scheduler, resource);
			// This is a non-blocking call.
			myJob.submit();

			// Add this job entry to the database.
			addToDB(myBean.getOwner(), myBean.getAppName(), myBean.getJobName(),
					unique_id, argumentString, scheduler, resource, arch_os,
					job.getCPUs(), job.getReplicates(),
					(new Integer(runtime_estimate_seconds).toString()),
					(new Integer(runtime_estimate_seconds_recent).toString()),
					(new Integer(searchreps).toString()),
					(new Integer(bootstrapreps).toString()));
			*/
		} catch (Exception e) {
			log.error("Could not create GSBL job " + e);
		}
		return true;
	}  // End runService().
}  // End GARLIService class.
