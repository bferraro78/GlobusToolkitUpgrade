package edu.umd.grid.bio.migrate.clients;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.GSBLClient;
import edu.umd.umiacs.cummings.GSBL.GSBLPropertiesManager;
import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager;

// stub classes
import edu.umd.grid.bio.migrate.stubs.MigrateService.MigratePortType;
import edu.umd.grid.bio.migrate.stubs.MigrateService.MigrateArguments;
import edu.umd.grid.bio.migrate.stubs.Migrate.service.MigrateServiceAddressingLocator;
import edu.umd.grid.bio.migrate.stubs.MigrateFactory.service.MigrateFactoryServiceAddressingLocator;

// For getting environment variables
import java.util.Properties;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;

// Place service specific imports here between the protection comments.
// BEGIN PROTECT: SubImports
import edu.umd.grid.bio.migrate.shared.MigrateParser;
    // END PROTECT: SubImports

/* GSG_USER (file requires user editing)
 *
 * PROTECT CONFIG
 * PROTECT: SubImports
 * PROTECT: beanSetup
 * END CONFIG
 */

/**
 * The client for the Migrate Grid service.
 */
public class MigrateSubmitClient extends GSBLClient {

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(MigrateSubmitClient.class);

    /**
     * MigrateArguments "Bean"
     */
    protected MigrateArguments myBean;
    
    /**
     * job name, i.e., current working directory
     */
    private static String jobname;

    /**
     * The main method. Reads in arguments from a properties file, creates a client, and executes it.
     */
    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Requires 3 arguments: factory URI, properties file, and job name.");
            System.exit(1);
        }

        // argument processing
        String factoryURI = args[0];
        String propertiesFile = args[1];
        jobname = args[2];

        // fill in argument bean from temporary properties file
        MigrateArguments myBean = new MigrateArguments();
        try {
            GSBLPropertiesManager GPM = new GSBLPropertiesManager(propertiesFile);
            GPM.updateJavaBean(myBean);
        } catch (Exception e) {
            System.exit(1);
        }

        System.out.println("Creating Migrate job.");

        MigrateSubmitClient client = null;
        try {
            client = new MigrateSubmitClient(factoryURI, myBean);
        } catch (Exception e) {
            System.exit(1);
        }

        /* Run the client */
        try {
            client.execute();
        } catch (Exception e) {
            log.error("Error executing Grid service: " + e);
        }
    }

    /**
     * Constructor.
     */
    public MigrateSubmitClient(String factoryURI, MigrateArguments myBean) throws Exception {
        super("Migrate", factoryURI, MigrateFactoryServiceAddressingLocator.class, MigrateServiceAddressingLocator.class);
        this.myBean = myBean;
    }

    /**
     * Uploads files and starts the service.
     */
    void execute() throws Exception {
	// get a unique resource id
	String resourceID = getResourceID();
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
    	((MigratePortType)instancePortType).createWorkingDir(resourceID + "@--" + cwd + "@--" + hostname + "@--" + reps);

	ArrayList<String> sharedFiles = new ArrayList<String>();
	ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
	ArrayList<String> perJobArguments = new ArrayList<String>();

	String[] allSharedFiles = null;
	String[] myPerJobArguments = null;
	String[] allPerJobFiles = null;

        // ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
        // BEGIN PROTECT: beanSetup
                        	
        MigrateParser MP = new MigrateParser(myBean.getParamFile());
	
	String [] inputFiles = MP.getInputFiles(false);
	String [] outputFiles = MP.getOutputFiles(false);
	
	String [] files = inputFiles;

	// Change paramFile from absolute to relative filename
	myBean.setParamFile(new File(myBean.getParamFile()).getName());
	myBean.setInputFiles(inputFiles);
	myBean.setOutputFiles(outputFiles);                                                                                                                                                                
        // END PROTECT: beanSetup
        // ----- ----- ----- END YOUR CODE ----- ----- ----- //

	myPerJobArguments = new String[perJobArguments.size()];
	myBean.setPerJobArguments(perJobArguments.toArray(myPerJobArguments));

	String workingDir = getWorkingDirBase() + resourceID + "/";

        System.out.println("Transferring files.");
        
        ReliableFileTransferManager rftm = new ReliableFileTransferManager(sharedFiles, perJobFiles, ReliableFileTransferManager.OP_UPLOAD, cwd, workingDir, getFactoryHost());
        rftm.beginTransfer();
        if (!rftm.waitComplete()) {
            log.error("Unable to upload file batch!");	    
            System.exit(1);
        }

	sharedFiles = rftm.getSharedFilesArray(); // these will be updated with their uploaded location in the cache

	allSharedFiles = new String[sharedFiles.size()];
	myBean.setSharedFiles(sharedFiles.toArray(allSharedFiles));

	perJobFiles = rftm.getPerJobFilesArray(); // these will be updated with their uploaded location in the cache

	allPerJobFiles = new String[perJobFiles.size()];
	
	for(int i = 0; i < perJobFiles.size(); i++) {
	   String[] tempcouples = perJobFiles.get(i);
           String filenames = "";
	   for(int j = 0; j < tempcouples.length; j++) {
	       if(j < tempcouples.length-1) { //add the ':' couple delimiter
	           filenames += tempcouples[j] + ":";
	       }else{
		   filenames += tempcouples[j];
	       }
	   }
	   allPerJobFiles[i] = filenames;
	}

	myBean.setPerJobFiles(allPerJobFiles);

	myBean.setSymlinks(rftm.getSymlinksString()); // we need to tell the service which symlinks in the cache to create

        // fill in job attributes
        myBean.setAppName(new String("Migrate"));
        myBean.setOwner(System.getProperty("user.name"));
        myBean.setJobName(jobname);
	myBean.setWorkingDir(workingDir);

        System.out.println("Submitting job.");

        // instancePortType object comes from super-class
        ((MigratePortType)instancePortType).runService(myBean);
        System.out.println("Job submitted with ID: " + resourceID);

	// destroy the service instance
	this.destroy();
    }
}
