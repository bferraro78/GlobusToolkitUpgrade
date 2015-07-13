package edu.umd.grid.bio.marxan.impl;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.BeanToArguments;
import edu.umd.umiacs.cummings.GSBL.GSBLJobManager;
import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager;
import edu.umd.umiacs.cummings.GSBL.GSBLService;
import edu.umd.umiacs.cummings.GSBL.GSBLRuntimeConfiguration;
import edu.umd.umiacs.cummings.GSBL.GSBLJob;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;

// For garbage collection
import java.lang.System;

// stub classes
import edu.umd.grid.bio.marxan.stubs.MARXAN.service.MARXANServiceAddressingLocator;
import edu.umd.grid.bio.marxan.stubs.MARXANService.MARXANArguments;

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

    public class MARXANService extends GSBLService {
	
    /**
     * Logger.
     */
    static Log log = LogFactory.getLog(MARXANService.class.getName());
 
    /**
     * Arguments bean.
     */    
    protected MARXANArguments myBean;
	
    /**
     * Our job instance.
     */
    private GSBLJob job = null;
	
    /**
     * Runtime configuration.  This is common to all instances of the service.
     */
    static protected GSBLRuntimeConfiguration runtimeConfig = null;

    /**
     * container_status location.  This is common to all instances of the service.
     */
    static protected String container_status_location = null;

    /**
     * update interval, common to all instances of the service
     */
    static protected String update_interval_string = null;
    static protected int update_interval = 300000; // default is 5 minutes
    
    /**
     * This is the name of the service.
     */
    private String serviceName = "MARXAN";

    /**
     * Class constructor.
     */
    public MARXANService() throws Exception {
        super("MARXAN");
        try {
	    if (runtimeConfig == null) {
		log.error("Runtime configuration for MARXAN is unavailable.");
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
    
    // Load the runtime configuration and container_status_location.  We only want to do this once.
    static {
        try {
            Properties env = new Properties();
            env.load(Runtime.getRuntime().exec("env").getInputStream());
            String globusLocation = (String) env.get("GLOBUS_LOCATION");
            runtimeConfig = new GSBLRuntimeConfiguration(globusLocation + "/service_configurations/MARXAN.runtime.xml");
	    BufferedReader br = new BufferedReader(new FileReader(new File(globusLocation + "/service_configurations/container_status.location")));
	    container_status_location = br.readLine();
	    br.close();
	    br = new BufferedReader(new FileReader(new File(globusLocation + "/service_configurations/update_interval")));
	    update_interval_string = br.readLine();
	    br.close();
	    update_interval = (new Integer(Integer.valueOf(update_interval_string))).intValue();
        } catch(Exception e) {
            log.error("Error loading runtime configuration or container_status_location for MARXAN: " + e);
        }
    }
	
    /**
     * This is almost like our "main" method.
     * @param myBean argument bean
     * @return true if service was started successfully
     */
    public boolean runService(MARXANArguments myBean) {
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

	// ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
	// BEGIN PROTECT: fileSetup
			
	log.debug("Initial arg string: " + argumentString);
	log.debug("Replicates: " + reps);
																				
	// the argument string should/may only contain the --replicates flag now
	//argumentString = "";
	
	// adding argument string hack so something shows up in job_status
	// this is later ignored for MARXAN in RSLxml.java
	// ALB 8-22-07: doing away with this hack, not sure why it was a big deal in the first place
	// argumentString = output_files[0];

	sharedFiles.add("newline");
	
	// get the base directory from the working directory
	String basedir = myWorkingDir.substring(0, myWorkingDir.lastIndexOf("/"));
	basedir = basedir.substring(0, basedir.lastIndexOf("/"));

	extraRSL = "<stdin>${GLOBUS_SCRATCH_DIR}/cache/newline</stdin>";

	// rename all per-job files to remove the _[0-9]* suffix, both on disk and as the input files to the program
	for(int i = 0; i < perJobFiles.size(); i++) {
	    String[] files = perJobFiles.get(i);
	    for(int k = 0; k < files.length; k++) {
		String file = files[k];
		String newFilename = file.substring(0, file.lastIndexOf("_"));
		//File toRename = new File(basedir + "/cache/" + file);
		//File renameTo = new File(basedir + "/cache/" + newFilename);
		//if(!toRename.renameTo(renameTo)) {
		//    log.error("Error renaming " + toRename.toString() + " to " + renameTo.toString());
		//}
		// now change the input file to the program
		files[k] = newFilename;
	    }
	    perJobFiles.set(i, files);
	}	

	// it might be the case that when submitting cleanup jobs, you want to do singletons that point at the _[0-9]* files,
	// so remove the _[0-9]* suffix in this case as well
	for(int j = 0; j < sharedFiles.size(); j++) {
	    String file = sharedFiles.get(j);
	    if(file.indexOf("_") != -1) {
		String newFilename = file.substring(0, file.lastIndexOf("_"));
		sharedFiles.set(j, newFilename);
	    }
	}

	String [] output_files = myBean.getOutputFiles();
	
	// END PROTECT: fileSetup
	// ----- ----- ----- END YOUR CODE ----- ----- ----- //
        	
	try {
	    GSBLJob job = new GSBLJob(executable, argumentString, scheduler, resource, arch_os, myWorkingDir, sharedFiles, perJobFiles, output_files, requirements, extraRSL);

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

        private MARXANArguments myBean = null;
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
		Thread.sleep(30000);
	    } catch(Exception e) {
		log.error("Exception " + e);
	    }
            checkFinished();
        }

        // the main loop periodically updates the status of jobs that were known to be idle or running
        public void run() {
            while(true) {
		status = new String[2];
		status[0] = "1";
		status[1] = "2";
                jobIDs = getJobList(getName(),status); // get idle or running
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
	    }
        }

        /**
         * Update job status.
         */
        public void checkJobStatus() {
            try {
                job = new GSBLJob(rwd);
                myJob = new GSBLJobManager(job);
                myJob.checkJobStatus();
            } catch(Exception e) {
                log.error("Exception: " + e);
		updateDBStatus("5", rwd); // set job to failed if unable to refresh job state
            }
        }

        /**
         * Check for jobs that are finished, but not retrieved.
         */
        private void checkFinished() {
	    status = new String[1];
	    status[0] = "4";
            jobIDs = getJobList(getName(),status); // get finished
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
		    myBean = (MARXANArguments)(getArguments(rwd));
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

		if(myBean.getReplicates() == null || myBean.getReplicates().intValue() <= 1) {

                // ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
                // BEGIN PROTECT: retrieveFiles
		    		    		    		    		    
		    String[] tempSharedFiles = myBean.getOutputFiles();
		    if(tempSharedFiles == null) {
			tempSharedFiles = new String[0];
		    }
		    sharedFiles = new ArrayList<String>(Arrays.asList(tempSharedFiles));

		} else {
		    String unique_id = rwd.substring(0, rwd.length() - 1);
		    unique_id = unique_id.substring(unique_id.lastIndexOf("/"));
                    File targzfile = new File(rwd + unique_id + ".output/" + unique_id + ".tar.gz");
                    if(targzfile.exists()) {
                        // remove the other job* directories, which should be empty
                        for(int i = 0; i < myBean.getReplicates().intValue(); i++) {
                            File jobdir = new File(rwd + unique_id + ".output/job" + i);
                            jobdir.delete();
                        }
                    } else {
			String[] tempPerJobFiles = myBean.getPerJobFiles();
			
			if(tempPerJobFiles == null) {
			    tempPerJobFiles = new String[0];
			}
			
			// don't see running MARXAN on PBS anytime soon, which would be the only reason to delete per-job input files
			/*
			  for(int i = 0; i < tempPerJobFiles.length; i++) {
			  String filenames = tempPerJobFiles[i];
			  log.debug("filenames is: " + filenames);
			  String[] chunks = filenames.split(":");
			  
			  // we should delete the per-job input files from the output directory before returning the results
			  for(int j = 0; j < chunks.length; j++) { // (the length of chunks should equal the number of replicates specified)
			  String filename = new File(chunks[j]).getName();
			  filename = filename.substring(0, filename.lastIndexOf("_"));
			  File perjobfile = new File(rwd + unique_id + ".output/job" + new Integer(j).toString() + "/" + filename);
			  if(perjobfile.exists()) {
			  log.debug("deleting MARXAN perjob file: " + perjobfile.toString());
			  perjobfile.delete();
			  } else {
			  log.debug("can't delete MARXAN perjob file, which doesn't exist: " + perjobfile.toString());
			  }
			  }
			  }*/
		    }
			// END PROTECT: retrieveFiles
			// ----- ----- ----- END YOUR CODE ----- ----- ----- //
		}
		    
                ReliableFileTransferManager results = new ReliableFileTransferManager(sharedFiles, perJobFiles, ReliableFileTransferManager.OP_DOWNLOAD, cwd, rwd, null);

                results.beginTransfer();
                if (!results.waitComplete()) {
                    log.error("Unable to download file batch: " + sharedFiles.toString());
                    updateDBStatus("11", rwd);
                } else {
                    // set service status to "retrieved files"
                    updateDBStatus("10", rwd);

                    // delete our working directory iff all is well
                    deleteWorkingDir(rwd);
                }
            } catch (Exception e) {
                log.error("Exception while retrieving results: " + e);
                updateDBStatus("11", rwd);
            }
        }
    }
}

