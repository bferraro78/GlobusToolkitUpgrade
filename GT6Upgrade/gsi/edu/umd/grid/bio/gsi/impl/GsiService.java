package edu.umd.grid.bio.gsi.impl;

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
import edu.umd.grid.bio.gsi.stubs.Gsi.service.GsiServiceAddressingLocator;
import edu.umd.grid.bio.gsi.stubs.GsiService.GsiArguments;

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

    public class GsiService extends GSBLService {
	
    /**
     * Logger.
     */
    static Log log = LogFactory.getLog(GsiService.class.getName());
 
    /**
     * Arguments bean.
     */    
    protected GsiArguments myBean;
	
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
    private String serviceName = "Gsi";

    /**
     * Class constructor.
     */
    public GsiService() throws Exception {
        super("Gsi");
        try {
	    if (runtimeConfig == null) {
		log.error("Runtime configuration for Gsi is unavailable.");
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
            runtimeConfig = new GSBLRuntimeConfiguration(globusLocation + "/service_configurations/Gsi.runtime.xml");
	    BufferedReader br = new BufferedReader(new FileReader(new File(globusLocation + "/service_configurations/container_status.location")));
	    container_status_location = br.readLine();
	    br.close();
	    br = new BufferedReader(new FileReader(new File(globusLocation + "/service_configurations/update_interval")));
	    update_interval_string = br.readLine();
	    br.close();
	    update_interval = (new Integer(Integer.valueOf(update_interval_string))).intValue();
        } catch(Exception e) {
            log.error("Error loading runtime configuration or container_status_location for Gsi: " + e);
        }
    }
	
    /**
     * This is almost like our "main" method.
     * @param myBean argument bean
     * @return true if service was started successfully
     */
    public boolean runService(GsiArguments myBean) {
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
																        	
	// determine the tree type from the extension
	String treefile = myBean.getTreeFile();
	int index;
	if((index = perJobArguments.indexOf("treeFile")) != -1) { // if multiple treefiles have been specified, use the entire string
	    treefile = getArgument("treeFile", perJobFiles, index);
	}

	String extension = treefile.substring(treefile.lastIndexOf(".") + 1);
	String treetype = "phylip";

	log.debug("EXTENSION: " + extension);
	if(extension.equals("nex") || extension.equals("nexus") || extension.equals("nxs")) {
	    treetype = "nexus";
	}
	log.debug("TREETYPE: " + treetype);
	// doctor the argument string to work with our R script
	argumentString = "";

	if(reps > 1) { // add in the replicates argument
	    argumentString += "--replicates \"" + replicates.toString() + "\"";
	    argumentString += " ";
	}
	
	// add treefile(s) to the argument string
	argumentString += treefile;

	argumentString += " ";

	if((index = perJobArguments.indexOf("assignmentFile")) != -1) { // if multiple assignment files have been specified, add them all to the argument string
	    argumentString += getArgument("assignmentFile", perJobFiles, index);
	} else {
	    argumentString += myBean.getAssignmentFile();
	}
	argumentString += " ";

	if(myBean.getUseLengths() == null) {
	    argumentString += "F ";
	} else {
	    argumentString += "T ";
	}
	argumentString += treetype;
	argumentString += " ";
	if(myBean.getTreeNumber() != null) {
	    argumentString += myBean.getTreeNumber().toString();
	    argumentString += " ";
	} else {
	    argumentString += "0 ";
	}
	if(myBean.getNumPerms() != null) {
	    argumentString += myBean.getNumPerms().toString() + " ";
	} else {
	    argumentString += "10000 ";
	}
	if(myBean.getPermType() != null) {
	    if((myBean.getPermType()).equals("individual")) {
		argumentString += "individual";
	    } else {
		argumentString += "coordinated";
	    }
	} else {
	    argumentString += "coordinated";
	}
	
	//print argument string
	log.debug("args are: " + argumentString);
	
	String[] input_files = myBean.getInputFiles();
	String[] output_files = null;
	
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

        private GsiArguments myBean = null;
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
		    myBean = (GsiArguments)(getArguments(rwd));
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
		    		    		    
		    sharedFiles = new ArrayList<String>();
       
		    String outputFile = new File(myBean.getOutputFile()).getName();
		    log.debug("output file is: " + outputFile);
		    String errorFile = outputFile + ".err";
		    log.debug("error file is: " + errorFile);

		    // all of our output is in stdout
		    BufferedReader reader = new BufferedReader(new FileReader(rwd + "stdout"));
		    BufferedWriter writer = new BufferedWriter(new FileWriter(rwd + outputFile));
		    String line = null;
		    int counter = 0;
		    while((line = reader.readLine()) != null) {
			// remove the first line
			//if(counter == 0) {
			    // increment counter
			    //counter++;
			//} else {
			    // output line
			    writer.write(line);
			    writer.newLine();
			    // increment counter
			    counter++;
			    //}
		    }
		    writer.flush();
		    writer.close();
		    reader.close();

		    // all of our output is in stderr
		    reader = new BufferedReader(new FileReader(rwd + "stderr"));
		    writer = new BufferedWriter(new FileWriter(rwd + errorFile));
		    line = null;
		    while((line = reader.readLine()) != null) {
			// output line
			writer.write(line);
			writer.newLine();
		    }
		    writer.flush();
		    writer.close();
		    reader.close();

		    sharedFiles.add(outputFile);
		    sharedFiles.add(errorFile);

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

