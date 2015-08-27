/**
 * @author Adam Bazinet
 * @author Ben Ferraro
 * @author Jordan Kiesel
 */

package edu.umd.grid.bio.garli.impl;

// GSBL classes.
import edu.umd.umiacs.cummings.GSBL.GSBLJobManager;
import edu.umd.umiacs.cummings.GSBL.GSBLService;
import edu.umd.umiacs.cummings.GSBL.GSBLJob;
import edu.umd.umiacs.cummings.GSBL.GSBLUtils;

// For logging.
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;

// For garbage collection.
import java.lang.System;

// Stub classes.
import edu.umd.grid.bio.garli.stubs.GARLI.service.GARLIServiceAddressingLocator;
import edu.umd.grid.bio.garli.stubs.GARLIService.GARLIArguments;

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

	private GARLIArguments myBean = null;
	private GSBLJob job = null;
	private GSBLJobManager myJob = null;
	private Object[] jobIDs = null;
	private BufferedReader br = null;
	private String rwd = "";
	private String cwd = "";
	private String[] status;

	private String hostname = "arginine.umiacs.umd.edu";  // Set for testing purposes.

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
/*
			container_status_location = GSBLUtils
				.getConfigElement("container_status.location");
*/
			String update_interval_string = GSBLUtils
				.getConfigElement("update_interval");
			// Split interval string into min and max.
			int spaceIndex = update_interval_string.indexOf(" ");
			update_interval = Integer.parseInt(update_interval_string
				.substring(0, spaceIndex));
			update_max = Integer.parseInt(update_interval_string
				.substring(spaceIndex + 1));
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		System.out.println("Update interval: " + update_interval);
		System.out.println("Update max: " + update_max);
	}

	// The main loop periodically updates the status of jobs that were known to be idle or running.
	
	public void run() {
		int timeCounter = 0;

		while (true) {
			status = new String[2];
			status[0] = "1";
			status[1] = "2";
			jobIDs = getJobList(getName(), status, timeCounter);  // Get the status of idle and running jobs that are due to be checked.

			System.out.println("Number of jobs: " + jobIDs.length);

			for (int i = 0; i < jobIDs.length; i++) {
				rwd = (getWorkingDirBase() + ((String) jobIDs[i]) + "/");
				checkJobStatus();  // Set bean.
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
	public void checkJobStatus() {
		try {
			job = new GSBLJob(rwd);
			myJob = new GSBLJobManager(job);
			myJob.checkJobStatus(update_interval, update_max);
		} catch (Exception e) {
			log.error("Exception: " + e);
			System.out.println("Job failed.");
			updateDBStatus("5", rwd, update_interval, update_max);  // Set job to failed if unable to refresh job state.
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
				rwd = (getWorkingDirBase() + ((String) jobIDs[i]) + "/");
				try {
					br = new BufferedReader(new FileReader(rwd + "cwd.txt"));
					cwd = br.readLine();
					br.close();
				} catch (Exception e) {
					log.error("Exception: " + e);
				}
				myBean = ((GARLIArguments) getArguments(rwd));  // Set bean.

				transferFiles(i);
			} else {
				log.debug("The grid is down, skipping file retrieve... .");
			}
		}
	}

	private void transferFiles(int i) {
		Properties env = new Properties();
		String home = (String) env.get("HOME");

		String globusUrlCopyCmd = ("globus-url-copy -cd -r file://" + home
				+ "/" + jobIDs[i] + "/ gsiftp://" + hostname + rwd);
		System.out.println("Globus-url-copy command: " + globusUrlCopyCmd);

		// Transfer job folder and its contents.
		GSBLUtils.executeCommand(globusUrlCopyCmd);
	}
}