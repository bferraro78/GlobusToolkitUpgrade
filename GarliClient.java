package edu.umd.grid.bio.garli.clients;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.GSBLClient;
import edu.umd.umiacs.cummings.GSBL.GSBLPropertiesManager;

// stub classes
import edu.umd.grid.bio.garli.stubs.GARLIService.GARLIPortType;
import edu.umd.grid.bio.garli.stubs.GARLIService.GARLIArguments;
import edu.umd.grid.bio.garli.stubs.GARLI.service.GARLIServiceAddressingLocator;
import edu.umd.grid.bio.garli.stubs.GARLIFactory.service.GARLIFactoryServiceAddressingLocator;

// For getting environment variables
import java.util.Properties;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;

// Place service specific imports here between the protection comments.
// BEGIN PROTECT: SubImports
import edu.umd.grid.bio.garli.shared.GARLIParser;
import java.util.Arrays;


public class GarliSubmit extends GSBLClient {

	/**
     * Logger.
     */
    private static Log log = LogFactory.getLog(GARLISubmitClient.class);

    /**
     * GARLIArguments "Bean"
     */
    protected GARLIArguments myBean;
    
    /**
     * job name, i.e., current working directory
     */
    private static String jobname;

    private static String resourceID; // keeps track of jobID


	/**
     * The main method. Reads in arguments from a properties file, creates a client, and executes it.
     */
	public static void main(String [] args) {
		
		if (args.length != 2) {
			System.err.println("Requires 2 arguments: properties file, and job name.");
            System.exit(1);
        }

        String propertiesfile = args[0];
        jobname = args[1];


		// Creates a bean, reads properties file, and updates bean with job properties
        GARLIArguments myBean = new GARLIArguments();
        try {
            GSBLPropertiesManager GPM = new GSBLPropertiesManager(propertiesFile);
            GPM.updateJavaBean(myBean);
        } catch (Exception e) {
            System.exit(1);
        }

        System.out.println("Creating GARLI job.");


        GARLISubmitClient client = null;
        try {
            client = new GARLISubmitClient(factoryURI, myBean);
        } catch (Exception e) {
            System.exit(1);
        }

        /* Run the client */
        try {
            client.execute();
        } catch (Exception e) {
            log.error("Error executing Grid service: " + e);
        }



	} // end main function


    /**
     * Constructor.
     */
    public GARLISubmitClient(String factoryURI, GARLIArguments myBean) throws Exception {
        super("GARLI", factoryURI, GARLIFactoryServiceAddressingLocator.class, GARLIServiceAddressingLocator.class);
        this.myBean = myBean;
    }


    void execute() throws Exception { 
        // get a unique resource id
        resourceID = getResourceID();

        // on the service side, store the directory the client submitted from as well as the hostname
        Properties env = new Properties();
        env.load(Runtime.getRuntime().exec("env").getInputStream());
        String cwd = (String)env.get("PWD");
        String hostname = (String)env.get("HOSTNAME");

        // create a working directory on the service side
        Integer replicates = myBean.getReplicates();
        String reps = "";
        int replica = 0;
        if(replicates == null || replicates.intValue() <= 1) {
            reps = "1";
            replica = 1;
        } else {
            reps = replicates.toString();
            replica = replicates.intValue();
        }
        ((GARLIPortType)instancePortType).createWorkingDir(resourceID + "@--" + cwd + "@--" + hostname + "@--" + reps);

        ArrayList<String> sharedFiles = new ArrayList<String>();
        ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
        ArrayList<String> perJobArguments = new ArrayList<String>();

        String[] allSharedFiles = null;
        String[] myPerJobArguments = null;
        String[] allPerJobFiles = null;

        // ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
        // BEGIN PROTECT: beanSetup

        String confFileNames[] = null;
        String configFile = myBean.getConfigFile();
        boolean buildConfig = false;

        if(configFile == "" || configFile == null) { // we will build a configuration file from the args passed in
            buildConfig = true;
            } else if((confFileNames = parseDirectory(configFile, replica)) == null) { // single job or homogeneous batch

        } else { // heterogeneous job batch
            perJobArguments.add("configFile");
            perJobFiles.add(confFileNames);
        }

        boolean doValidate = true;
        if(myBean.getNovalidate() != null) {
            if((myBean.getNovalidate()).booleanValue() == true) {
                doValidate = false;
            }
        }

        GARLIParser gp = null;
        try {
            gp = new GARLIParser(myBean, "", buildConfig, doValidate, true);
        } catch (Exception e) {
            System.out.println("Unknown exception occurred while invoking the GARLI parser " + e);
        } 

        String avail_mem = gp.getAvailMem();
        if (avail_mem == null) {
            log.error("Please specify \"available memory\" in the GARLI config file\n");
            System.exit(1);
        }

        if(doValidate) {
            String unique_patterns = gp.getUniquePatterns();
            String num_taxa = gp.getNumTaxa();
            String actual_memory = gp.getActualMem();

            myBean.setUniquepatterns(unique_patterns);
            myBean.setNumtaxa(num_taxa);
            myBean.setActualmemory(actual_memory);
        }

        myBean.setConfigFile(gp.getConfigFileName()); // now this should no longer be set to a directory

        sharedFiles.addAll(gp.getInputFiles()); // gp.getInputFiles() currently only returns shared files

        String [] output_files = gp.getOutputFiles();   
        myBean.setOutputFiles(output_files);

            // END PROTECT: beanSetup
            // ----- ----- ----- END YOUR CODE ----- ----- ----- //


        myPerJobArguments = new String[perJobArguments.size()];
        myBean.setPerJobArguments(perJobArguments.toArray(myPerJobArguments));

        String workingDir = getWorkingDirBase() + resourceID + "/";

        allSharedFiles = new String[sharedFiles.size()];
        myBean.setSharedFiles(sharedFiles.toArray(allSharedFiles));


        allPerJobFiles = new String[perJobFiles.size()];
        
        for(int i = 0; i < perJobFiles.size(); i++) {
             String[] tempcouples = perJobFiles.get(i);
             String filenames = "";
             for(int j = 0; j < tempcouples.length; j++) {
                   if(j < tempcouples.length-1) { // add the ':' couple delimiter
                   filenames += tempcouples[j] + ":";
                }else{
                 filenames += tempcouples[j];
                }
            }
            allPerJobFiles[i] = filenames;
        }

        myBean.setPerJobFiles(allPerJobFiles);

        // check for scheduler override
        File scheduler_override = new File("scheduler_override");
        if(scheduler_override.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(scheduler_override));
            myBean.setSchedulerOverride(br.readLine());
            br.close();
        }

        // fill in job attributes
        myBean.setAppName(new String("GARLI"));
        myBean.setOwner(System.getProperty("user.name"));
        myBean.setJobName(jobname);

        System.out.println("Submitting job.");

                // instancePortType object comes from super-class
        ((GARLIPortType)instancePortType).runService(myBean);
        System.out.println("Job submitted with ID: " + resourceID);

            // destroy the service instance
        this.destroy();
    } // end execute





















} // end class