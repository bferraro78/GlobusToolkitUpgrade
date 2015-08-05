package edu.umd.grid.bio.lamarc.impl;

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
import edu.umd.grid.bio.lamarc.stubs.LAMARC.service.LAMARCServiceAddressingLocator;
import edu.umd.grid.bio.lamarc.stubs.LAMARCService.LAMARCArguments;

//Place service specific imports here between the protection comments.
//BEGIN PROTECT: ServiceImports
import edu.umd.grid.bio.lamarc.shared.LamarcParser;
    //END PROTECT: ServiceImports

    /* GSG_USER (file requires user editing)
     *
     * PROTECT CONFIG
     * PROTECT: ServiceImports
     * PROTECT: fileSetup
     * PROTECT: retrieveFiles
     * END CONFIG
     */

    public class LAMARCService extends GSBLService {
	
    /**
     * Logger.
     */
    static Log log = LogFactory.getLog(LAMARCService.class.getName());
 
    /**
     * Arguments bean.
     */    
    protected LAMARCArguments myBean;
	
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
    private String serviceName = "LAMARC";

    /**
     * Class constructor.
     */
    public LAMARCService() throws Exception {
        super("LAMARC");
        try {
	    if (runtimeConfig == null) {
		log.error("Runtime configuration for LAMARC is unavailable.");
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
            runtimeConfig = new GSBLRuntimeConfiguration(globusLocation + "/service_configurations/LAMARC.runtime.xml");
	    BufferedReader br = new BufferedReader(new FileReader(new File(globusLocation + "/service_configurations/container_status.location")));
	    container_status_location = br.readLine();
	    br.close();
	    br = new BufferedReader(new FileReader(new File(globusLocation + "/service_configurations/update_interval")));
	    update_interval_string = br.readLine();
	    br.close();
	    update_interval = (new Integer(Integer.valueOf(update_interval_string))).intValue();
        } catch(Exception e) {
            log.error("Error loading runtime configuration or container_status_location for LAMARC: " + e);
        }
    }
	
    /**
     * This is almost like our "main" method.
     * @param myBean argument bean
     * @return true if service was started successfully
     */
    public boolean runService(LAMARCArguments myBean) {
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
											    String data_file = myWorkingDir + myBean.getDataFile();
	    String param_file = myWorkingDir + myBean.getParamFile();
	    String xmlinput_file = myWorkingDir + LamarcParser.lamarc_xmlinput_filename_default;
	    try{
		LamarcParser lamarc_parser = new LamarcParser(myWorkingDir);
		    
		String lamarc_conv_args = lamarc_parser.getLamConvArgs(param_file);
		lamarc_conv_args = lamarc_conv_args + "--" + 
		    LamarcParser.lamconv_geneticdatafilename_tag + " " + data_file;
		lamarc_parser.writeLamarcXMLDataFile(lamarc_conv_args);
		    
		String lamarc_args = lamarc_parser.getLamarcArgs(param_file);
		lamarc_parser.writeLamarcXMLInputFile(xmlinput_file);
	    } catch (Exception e){
		e.printStackTrace();

	    }
		
	    String[] input_files = myBean.getInputFiles();
	    String[] output_files = myBean.getOutputFiles();
        
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

        private LAMARCArguments myBean = null;
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
		    myBean = (LAMARCArguments)(getArguments(rwd));
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
		    		    																				
	    String[] tmp_array = myBean.getOutputFiles();

	    String[] files = new String [tmp_array.length + 2];	    

	    //fill the array with all the files before stdout and stderr
	    for (int i = 0; i < tmp_array.length; i++) {
		files[i] = tmp_array[i];
	    }
            // Get stdout
            files[files.length - 2] = "stdout";

            // Get stderr
            files[files.length - 1] = "stderr";


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

