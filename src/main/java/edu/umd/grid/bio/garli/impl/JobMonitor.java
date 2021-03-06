/**
 * @author Adam Bazinet
 * @author Ben Ferraro
 * @author Jordan Kiesel
 */

package edu.umd.grid.bio.garli.impl;

// GSBL classes.
import edu.umd.umiacs.cummings.GSBL.GSBLService;
import edu.umd.umiacs.cummings.GSBL.GSBLJob;
import edu.umd.umiacs.cummings.GSBL.GSBLUtils;

// For logging.
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;
import java.net.*;

// For garbage collection.
import java.lang.System;

import edu.umd.grid.bio.garli.GARLIArguments;

class JobMonitor extends GSBLService {

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(GARLIService.class.getName());

	/**
	 * container_status location.
	 */
	static protected String container_status_location = null;

	/**
	 * Update interval - how frequently the service checks on the status of its
	 * jobs.
	 */
	static protected int update_interval = 300000;  // Default is 5 minutes.
	static protected int update_max = 4800000;  // Default is 80 minutes.

	static protected String home = "";
	static protected String globusLocation = "";

	private GARLIArguments myBean = null;
	private Object[] jobIDs = null;
	private BufferedReader br = null;
	private String rwd = "";
	private String cwd = "";
	private String[] status;

	public static void main(String[] args) {
		JobMonitor jm = new JobMonitor();
		jm.run();
	}

	/**
	 * This checks for finished jobs.
	 */
	public JobMonitor() {
		super("GARLI");
		try {
			Thread.sleep(30000);  // Sleep for 30 seconds and then check the status of finished jobs.
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
		checkFinished();
	}
	
	// Load things from config files. We only want to do this once.
	static {
		try {
			String update_interval_string = GSBLUtils
				.getConfigElement("update_interval");
			// Split interval string into min and max.
			int spaceIndex = update_interval_string.indexOf(" ");
			update_interval = Integer.parseInt(update_interval_string
				.substring(0, spaceIndex));
			update_max = Integer.parseInt(update_interval_string
				.substring(spaceIndex + 1));

			Properties env = new Properties();
			env.load(Runtime.getRuntime().exec("env").getInputStream());
			home = (String) env.get("HOME");
			globusLocation = (String) env.get("GSBL_CONFIG_DIR");
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
	}

	// The main loop periodically updates the status of jobs that were known to be idle or running.
	
	public void run() {
		int timeCounter = 0;

		while (true) {
			status = new String[2];
			status[0] = "1";
			status[1] = "2";
			
			System.out.println("NAME:" + getName() + "\n");

			jobIDs = getJobList(getName(), status, timeCounter);  // Get the status of idle and running jobs that are due to be checked.

			System.out.println("LENGTH: " + jobIDs.length + "\n");

			for (int i = 0; i < jobIDs.length; i++) {
				rwd = (getWorkingDirBase() + ((String) jobIDs[i]) + "/");
				System.out.println("rwd" + rwd + "\n");
				
				checkJobStatus(i);  // Set bean.
			}
			checkFinished();

			try {
				System.gc();  // Suggest Java clean things up.
				Thread.sleep(update_interval);  // Take a breather.
			} catch (Exception e) {
				log.error("Exception: " + e);
			}
			if ((timeCounter + update_interval) <= update_max) {
				timeCounter += update_interval;
			} else {
				timeCounter = 0;
			}
		}
	}

	/**
	 * Update job status.
	 */
	public void checkJobStatus(int i) {
		try {
			File stateFile = new File((String) getWorkingDir((String) jobIDs[i]) + "/"
					+ jobIDs[i] + "/last_known_status.txt");

			System.out.println("State File:" + stateFile + "\n");

			FileWriter fw = new FileWriter(stateFile, false);
			String jobState = GSBLUtils.executeCommandReturnOutput("globusrun -status "
					+ (String) getGramID((String) jobIDs[i]));



			fw.write(jobState);
			fw.close();

			if (jobState != null) {
				if (jobState.contains("PENDING")) {
					log.debug("Updating job status: 1 for " + rwd);
					// Update the status of this job in the database (1 = idle).
					GSBLService.updateDBStatus("1", rwd, update_interval, update_max);
				} else if (jobState.contains("ACTIVE")) {
					log.debug("Updating job status: 2 for " + rwd);
					// Update the status of this job in the database (2 = running).
					GSBLService.updateDBStatus("2", rwd, update_interval, update_max);
				} else if (jobState.contains("DONE")) {
					log.debug("Updating job status: 4 for " + rwd);
					// Update the status of this job in the database (4 = finished).
					GSBLService.updateDBStatus("4", rwd, update_interval, update_max);
				} else if (jobState.contains("FAILED")) {
					log.debug("Updating job status: 5 for " + rwd);
					// Update the status of this job in the database (5 = failed).
					GSBLService.updateDBStatus("5", rwd, update_interval, update_max);
				} else {
					log.debug("jobState for " + rwd + " is: " + jobState);
				}
			} else {
				log.debug("jobState is null!");
			}
		} catch (Exception e) {
			log.error("Exception: " + e);
			//System.out.println("Job failed.");
			//updateDBStatus("5", rwd, update_interval, update_max);  // Set job to failed if unable to refresh job state.
		}
	}

	/**
	 * Check for jobs that are finished, but not retrieved.
	 */
	private void checkFinished() {
		status = new String[1];
		status[0] = "4";
		jobIDs = getJobList(getName(), status, 0);  // Get finished.
		
		for (int i = 0; i < jobIDs.length; i++) {
/*
			// First make sure the grid is up!
			String up_or_down = "UP";
			try {
				br = new BufferedReader(new FileReader(container_status_location));
				up_or_down = br.readLine();
				br.close();
			} catch (Exception e) {
				log.error("Exception: " + e);
			}

			if (up_or_down.equals("UP")) {
*/
			rwd = (getWorkingDirBase() + ((String) jobIDs[i]) + "/");
			try {
				cwd = (String) getWorkingDir((String) jobIDs[i]);
			} catch (Exception e) {
				log.error("Exception: " + e);
			}
			//myBean = ((GARLIArguments) getArguments(rwd));  // Set bean.

			transferFiles(i);
/*
			} else {
				log.debug("The grid is down, skipping file retrieve... .");
			}
*/
		}
	}

	private void transferFiles(int i) {
		String hostname = (String) getHostname((String) jobIDs[i]);

		// ADDED 2/17/16
		String scheduler = (String) GSBLService.getSchedulerName((String) jobIDs[i]);

		String globusUrlCopyCmd = ("globus-url-copy -cd -q -r -rst file://" + home
				+ "/" + jobIDs[i] + "/ gsiftp://" + hostname + cwd + jobIDs[i] + "/");
		System.out.println("Globus-url-copy command: " + globusUrlCopyCmd);

		// Transfer job folder and its contents.
		String output = GSBLUtils.executeCommandReturnOutput(globusUrlCopyCmd);

		if (!output.equals("")) {  // If command produced output, something failed.
			System.out.print(output);

			log.debug("Updating job status: 11 for " + rwd);
			// Update the status of this job in the database.
			GSBLService.updateDBStatus("11", rwd, update_interval, update_max);
		} else {
			// Instead of file clean up, we just use rm -rf to get rid of JobID directory.
			String fileName = ("cleanupRslString" + jobIDs[i]);
			File cleanupRsl = new File(fileName);
			try {
				FileWriter fw = new FileWriter(cleanupRsl);
				fw.write("&(executable = /usr/bin/rm) (arguments = -rf " + home + "/"
						+ jobIDs[i] + "/)");
				fw.close();
			} catch (java.io.IOException e) {
				log.error(e.getMessage());
			}
			
			String globusrunCmd = ("globusrun -batch -r " + hostname
					+ "/jobmanager-fork -f " + fileName);
			
			System.out.println("Globusrun command: " + globusrunCmd);
			System.out.println(GSBLUtils.executeCommandReturnOutput(globusrunCmd));
			
			log.debug("Updating job status: 10 for " + rwd);
			// Update the status of this job in the database.
			GSBLService.updateDBStatus("10", rwd, update_interval, update_max);
/*
			cleanupRsl.delete();
*/
		}
	}
}
