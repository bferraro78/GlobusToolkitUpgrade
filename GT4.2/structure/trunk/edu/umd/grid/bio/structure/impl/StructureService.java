package edu.umd.grid.bio.structure.impl;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.BeanToArguments;
import edu.umd.umiacs.cummings.GSBL.GSBLJobManager;
import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager;
import edu.umd.umiacs.cummings.GSBL.GSBLService;
import edu.umd.umiacs.cummings.GSBL.GSBLRuntimeConfiguration;
import edu.umd.umiacs.cummings.GSBL.GSBLJob;
import edu.umd.umiacs.cummings.GSBL.GSBLUtils;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.io.*;
import java.util.*;

// For garbage collection
import java.lang.System;

// stub classes
import edu.umd.grid.bio.structure.stubs.Structure.service.StructureServiceAddressingLocator;
import edu.umd.grid.bio.structure.stubs.StructureService.StructureArguments;

//Place service specific imports here between the protection comments.
//BEGIN PROTECT: ServiceImports

    //END PROTECT: ServiceImports

    /* GSG_USER (file requires user editing)
     *
     * PROTECT CONFIG
     * PROTECT: ServiceImports
     * PROTECT: fileSetup
     * PROTECT: retrieveFiles
     * END CONFIG
     */

    public class StructureService extends GSBLService {
	
    /**
     * Logger.
     */
    static Log log = LogFactory.getLog(StructureService.class.getName());
 
    /**
     * Arguments bean.
     */    
    protected StructureArguments myBean;
	
    /**
     * Our job instance.
     */
    private GSBLJob job = null;
	
    /**
     * Runtime configuration.  This is common to all service instances by virtue of being static.
     */
    static protected GSBLRuntimeConfiguration runtimeConfig = null;

    /**
     * directory where data and scripts for determining runtime estimates resides
     */
    static protected String runtime_estimates_location = null;

    /**
     * container_status location.
     */
    static protected String container_status_location = null;

    /**
     * update interval - how frequently the service checks on the status of its jobs
     */
    static protected String update_interval_string = null;
    static protected int update_interval = 300000; // default is 5 minutes
    static protected int update_max = 4800000; // default is 80 minutes

    /**
     * URL for updating Drupal job status
     */
    static protected String drupalUpdateURL = null;
    
    /**
     * This is the name of the service.
     */
    private String serviceName = "Structure";

    /**
     * Class constructor.
     */
    public StructureService() throws Exception {
        super("Structure");
        try {
	    if (runtimeConfig == null) {
		log.error("Runtime configuration for Structure is unavailable.");
		System.exit(1);
	    }

	    // start the monitoring thread
            MonitorJobs monitorJobs = new MonitorJobs();
            Thread t = new Thread(monitorJobs);
	    t.setPriority(Thread.NORM_PRIORITY);
            t.start();
        	
        } catch(Exception e) {
	    log.error("Exception: " + e);
	    e.printStackTrace();
        }
    }
    
    // Load things from config files.  We only want to do this once.
    static {
        try {
	    Properties env = new Properties();
	    env.load(Runtime.getRuntime().exec("env").getInputStream());
	    String globusLocation = (String) env.get("GLOBUS_LOCATION");
            runtimeConfig = new GSBLRuntimeConfiguration(globusLocation + "/service_configurations/Structure.runtime.xml");
	    runtime_estimates_location = globusLocation + "/runtime_estimates/";
	    container_status_location = GSBLUtils.getConfigElement("container_status.location");

	    update_interval_string = GSBLUtils.getConfigElement("update_interval");
	    // split interval string into min and max
            update_interval = Integer.parseInt(update_interval_string.substring(0, update_interval_string.indexOf(" ")));
            update_max = Integer.parseInt(update_interval_string.substring(update_interval_string.indexOf(" ") + 1));

	    drupalUpdateURL = GSBLUtils.getConfigElement("drupal_update_url");
	    
        } catch(Exception e) {
            log.error("Error loading config file information for Structure: " + e);
        }
    }
	
    /**
     * This is almost like our "main" method.
     * @param myBean argument bean
     * @return true if service was started successfully
     */
    public boolean runService(StructureArguments myBean) {
    	this.myBean = myBean;

	// create symlinks
	if(myBean.getSymlinks() != null && !myBean.getSymlinks().equals("")) {
	    makeSymlinks(myBean.getSymlinks());
	}

	String[] tempSharedFiles = myBean.getSharedFiles();
	if(tempSharedFiles == null) {
	    tempSharedFiles = new String[0];
	}
	ArrayList<String> sharedFiles = new ArrayList<String>(Arrays.asList(tempSharedFiles));

	String[] tempPerJobArguments = myBean.getPerJobArguments();
	if(tempPerJobArguments == null) {
	    tempPerJobArguments = new String[0];
	}
	ArrayList<String> perJobArguments = new ArrayList<String>(Arrays.asList(tempPerJobArguments));

	String[] tempPerJobFiles = myBean.getPerJobFiles();

	if(tempPerJobFiles == null) {
	    tempPerJobFiles = new String[0];
	}
	ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
	
	for(int i = 0; i < tempPerJobFiles.length; i++) {
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
	    BeanToArguments BTA = new BeanToArguments(runtimeConfig.getArgumentDescription());
	    argumentString = BTA.getArgumentStringFromBean(myBean);
	} catch(Exception e) {
	    log.error("Exception: " + e);
	}

	int reps = 1;
	Integer replicates = myBean.getReplicates();
	try {
	    if(replicates != null && replicates.intValue() > 1) {
		reps = replicates.intValue();
	    }
	} catch(Exception e) {
	    log.error("Exception setting replicates: " + e);
	}

	int runtime_estimate_seconds = -1; // -1 means we don't have an estimate

	// ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
	// BEGIN PROTECT: fileSetup
																			

		log.debug("Initial arg string: " + argumentString);
		log.debug("Replicates: " + reps);
		
		//String[] input_files = myBean.getInputFiles();
		String[] output_files = myBean.getOutputFiles();

		int i;
		if((i=perJobArguments.indexOf("mainparams")) != -1) {
		    String csv = getArgument("mainparams", perJobFiles, i);
		    log.debug("CSV: " + csv + " i: " + i);
		    log.debug("mainparams file from bean: " + myBean.getMainparams());
		    int first = argumentString.indexOf("\"" + myBean.getMainparams() + "\"");
		    int last = argumentString.indexOf(" ", first);
		    String beginning = argumentString.substring(0,first);
		    if(last == -1) {
			argumentString = beginning + " " + csv;
		    } else {
			String end = argumentString.substring(last+1);
			argumentString = beginning + " " + csv + " " + end;
		    }
		}
		if((i=perJobArguments.indexOf("extraparams")) != -1) {
		    String csv = getArgument("extraparams", perJobFiles, i);
		    log.debug("CSV: " + csv + " i: " + i);
		    log.debug("extraparams file from bean: " + myBean.getExtraparams());
		    int first =argumentString.indexOf("\"" + myBean.getExtraparams() + "\"");
		    int last = argumentString.indexOf(" ", first);
		    String beginning = argumentString.substring(0,first);
		    if(last == -1) {
			argumentString = beginning + " " + csv;
		    } else {
			String end = argumentString.substring(last+1);
			argumentString = beginning + " " + csv + " " + end;
		    }
		}
		if((i=perJobArguments.indexOf("inputfile")) != -1) {
		    String csv = getArgument("inputfile", perJobFiles, i);
		    log.debug("CSV: " + csv + " i: " + i);
		    log.debug("inputfile file from bean: " + myBean.getInputFile());
		    int first =argumentString.indexOf("\"" + myBean.getInputFile() + "\"");
		    int last = argumentString.indexOf(" ", first);
		    String beginning = argumentString.substring(0,first);
		    if(last == -1) {
			argumentString = beginning + " " + csv;
		    } else {
			String end = argumentString.substring(last+1);
			argumentString = beginning + " " + csv + " " + end;
		    }
		}		

	// END PROTECT: fileSetup
	// ----- ----- ----- END YOUR CODE ----- ----- ----- //
        	
	try {
	    if(myBean.getSchedulerOverride() != null && !(myBean.getSchedulerOverride().equals(""))) {
		String[] chunks = myBean.getSchedulerOverride().split(" ", 3);
		log.debug("SCHEDULER OVERRIDE: resource is: " + chunks[0]);
		resource = chunks[0];
		log.debug("SCHEDULER OVERRIDE: arch_os is: " + chunks[1]);
		arch_os = chunks[1];
		log.debug("SCHEDULER OVERRIDE: scheduler is: " + chunks[2]);
		scheduler = chunks[2];
	    }

	    GSBLJob job = new GSBLJob(executable, argumentString, scheduler, resource, arch_os, myWorkingDir, runtime_estimate_seconds, sharedFiles, perJobFiles, output_files, requirements, extraRSL);

	    if(scheduler.equals("matchmaking")) {
		scheduler = job.getScheduler(); // these values could have changed!
		resource = job.getResource();
		arch_os = job.getArch_os();
	    }

	    GSBLJobManager myJob = new GSBLJobManager(job, scheduler, resource);
            // this is a non-blocking call
            myJob.submit();

	    //add this job entry to the database
	    addToDB(myBean.getOwner(), myBean.getAppName(), myBean.getJobName(), myWorkingDir, argumentString, scheduler, resource, arch_os, job.getCPUs(), job.getReplicates());
	} catch (Exception e) {
	    log.error("Could not create GSBL job " + e);
	}
	return true;
    }
    /**
     * A threaded inner class which is responsible for periodically checking job status.
     */
    class MonitorJobs implements Runnable {

        private StructureArguments myBean = null;
	private GSBLJob job = null;
        private GSBLJobManager myJob = null;
        private Object [] jobIDs = null;
        private BufferedReader br = null;
        private String rwd = "";
        private String cwd = "";
	private String [] status;

        /**
         * This constructor checks for finished jobs.
         *
         * @param service associated Grid service instance
         */
        public MonitorJobs() {
	    try {
		Thread.sleep(30000); // sleep for 30 seconds and then check the status of finished jobs
	    } catch(Exception e) {
		log.error("Exception " + e);
	    }
            checkFinished();
        }

        // the main loop periodically updates the status of jobs that were known to be idle or running
        public void run() {
	    int timecounter = 0;
            while(true) {
		status = new String[2];
		status[0] = "1";
		status[1] = "2";
		jobIDs = getJobList(getName(), status, timecounter); // get the status of idle and running jobs that are due to be checked
                for(int i = 0; i < jobIDs.length; i++) {
                    rwd = getWorkingDirBase() + (String)(jobIDs[i]) + "/";
                    // set bean
                    checkJobStatus();
                }
                checkFinished();

		try {
                    System.gc(); // suggest java clean things up
                    Thread.sleep(update_interval); // take a breather
                } catch(Exception e) {
                    log.error("Exception: " + e);
                }
		if(timecounter + update_interval <= update_max) {
                    timecounter += update_interval;
                } else {
                    timecounter = 0;
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
            } catch(Exception e) {
                log.error("Exception: " + e);
		updateDBStatus("5", rwd, update_interval, update_max); // set job to failed if unable to refresh job state
            }
        }

        /**
         * Check for jobs that are finished, but not retrieved.
         */
        private void checkFinished() {
	    status = new String[1];
	    status[0] = "4";
            jobIDs = getJobList(getName(), status, 0); // get finished
            for(int i = 0; i < jobIDs.length; i++) {
		// first make sure the grid is up!
                String up_or_down = "UP";
                try {
                    br = new BufferedReader(new FileReader(container_status_location));
                    up_or_down = br.readLine();
                    br.close();
                } catch(Exception e) {
                    log.error("Exception: " + e);
                }
		
                if(up_or_down.equals("UP")) {
		    rwd = getWorkingDirBase() + (String)(jobIDs[i]) + "/";
		    try {
			br = new BufferedReader(new FileReader(rwd + "cwd.txt"));
			cwd = br.readLine();
			br.close();
		    } catch(Exception e) {
			log.error("Exception: " + e);
		    }
		    // set bean
		    myBean = (StructureArguments)(getArguments(rwd));
		    retrieveFiles();
		} else {
                    log.debug("The grid is down, skipping file retrieve...");
                }
	    }
        }

        /**
         * Download job output files from the server to the client.
         */
        private void retrieveFiles() {
            try {
		ArrayList<String> sharedFiles = null;
		ArrayList<String[]> perJobFiles = null;

		String unique_id = rwd.substring(0, rwd.length() - 1);
		unique_id = unique_id.substring(unique_id.lastIndexOf("/")+1);

		if(myBean.getReplicates() == null || myBean.getReplicates().intValue() <= 1) {

                // ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
                // BEGIN PROTECT: retrieveFiles
		    		    		    
		    sharedFiles = new ArrayList<String>();
		    String[] files = myBean.getOutputFiles();
		    String outfile = files[0]; // there should only be one output file!
		    log.debug("Output file: " + outfile);
		    sharedFiles.add(outfile);

		} else {
		    // because of the funky PBS symlink problem, we should also clean up shared files as well
		    String[] tempSharedFiles = myBean.getSharedFiles();
		    if(tempSharedFiles == null) {
			tempSharedFiles = new String[0];
		    }
		    for(int i = 0; i < tempSharedFiles.length; i++) {
			String filename = tempSharedFiles[i];
			for(int j = 0; j < myBean.getReplicates().intValue(); j++) {
			    File sharedfile = new File(rwd + unique_id + ".output/job" + new Integer(j).toString() + "/" + new File(filename).getName().toString());
			    if(sharedfile.exists()) {
				log.debug("deleting Structure shared file: " + sharedfile.toString());
				sharedfile.delete();
			    } else {
				log.debug("can't delete Structure shared file, which doesn't exist: " + sharedfile.toString());
			    }
			}
		    }	
		  
		    
		    // we should delete the per-job input files from the output directory before returning the results
		    // this code should be probably be templatized/functionized!
		    String[] tempPerJobFiles = myBean.getPerJobFiles();
		    
		    if(tempPerJobFiles == null) {
			tempPerJobFiles = new String[0];
		    }
		    for(int i = 0; i < tempPerJobFiles.length; i++) {
			String filenames = tempPerJobFiles[i];
			String[] chunks = filenames.split(":");
			for(int j = 0; j < chunks.length; j++) { // (the length of chunks should equal the number of replicates specified)
			    File perjobfile = new File(rwd + unique_id + ".output/job" + new Integer(j).toString() + "/" + new File(chunks[j]).getName().toString());
			    if(perjobfile.exists()) {
				log.debug("deleting Structure perjob file: " + perjobfile.toString());
				perjobfile.delete();
			    } else {
				log.debug("can't delete Structure perjob file, which doesn't exist: " + perjobfile.toString());
			    }
			}
		    }		    

                // END PROTECT: retrieveFiles
                // ----- ----- ----- END YOUR CODE ----- ----- ----- //
		}

                ReliableFileTransferManager results = new ReliableFileTransferManager(sharedFiles, perJobFiles, ReliableFileTransferManager.OP_DOWNLOAD, cwd, rwd, null);

                results.beginTransfer();
                if (!results.waitComplete()) {
                    log.error("Unable to download file batch: " + sharedFiles.toString());
                    updateDBStatus("11", rwd, update_interval, update_max);
                } else {
                    // set service status to "retrieved files"
                    updateDBStatus("10", rwd, update_interval, update_max);

                    // delete our working directory iff all is well
                    deleteWorkingDir(rwd);

		    String numreps = "1";
		    if(myBean.getReplicates() != null && myBean.getReplicates().intValue() > 1) {
			numreps = myBean.getReplicates().toString();
		    }

		    // update Drupal database
		    GSBLUtils.updateDrupalStatus(drupalUpdateURL, unique_id, "Completed", numreps);
                }
            } catch (Exception e) {
                log.error("Exception while retrieving results: " + e);
                updateDBStatus("11", rwd, update_interval, update_max);
            }
        }
    }
}
