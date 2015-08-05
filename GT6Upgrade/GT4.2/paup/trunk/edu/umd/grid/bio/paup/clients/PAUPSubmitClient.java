package edu.umd.grid.bio.paup.clients;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.GSBLClient;
import edu.umd.umiacs.cummings.GSBL.GSBLPropertiesManager;
import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager;

// stub classes
import edu.umd.grid.bio.paup.stubs.PAUPService.PAUPPortType;
import edu.umd.grid.bio.paup.stubs.PAUPService.PAUPArguments;
import edu.umd.grid.bio.paup.stubs.PAUP.service.PAUPServiceAddressingLocator;
import edu.umd.grid.bio.paup.stubs.PAUPFactory.service.PAUPFactoryServiceAddressingLocator;

// For getting environment variables
import java.util.Properties;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;

// Place service specific imports here between the protection comments.
// BEGIN PROTECT: SubImports
import edu.umd.grid.bio.paup.shared.PAUPParser;import edu.umd.grid.bio.paup.shared.PAUPParser;
import java.io.*;
    // END PROTECT: SubImports

/* GSG_USER (file requires user editing)
 *
 * PROTECT CONFIG
 * PROTECT: SubImports
 * PROTECT: beanSetup
 * END CONFIG
 */

/**
 * The client for the PAUP Grid service.
 */
public class PAUPSubmitClient extends GSBLClient {

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(PAUPSubmitClient.class);

    /**
     * PAUPArguments "Bean"
     */
    protected PAUPArguments myBean;
    
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
        PAUPArguments myBean = new PAUPArguments();
        try {
            GSBLPropertiesManager GPM = new GSBLPropertiesManager(propertiesFile);
            GPM.updateJavaBean(myBean);
        } catch (Exception e) {
            System.exit(1);
        }

        System.out.println("Creating PAUP job.");

        PAUPSubmitClient client = null;
        try {
            client = new PAUPSubmitClient(factoryURI, myBean);
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
    public PAUPSubmitClient(String factoryURI, PAUPArguments myBean) throws Exception {
        super("PAUP", factoryURI, PAUPFactoryServiceAddressingLocator.class, PAUPServiceAddressingLocator.class);
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
    	((PAUPPortType)instancePortType).createWorkingDir(resourceID + "@--" + cwd + "@--" + hostname + "@--" + reps);

	ArrayList<String> sharedFiles = new ArrayList<String>();
	ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
	ArrayList<String> perJobArguments = new ArrayList<String>();

	String[] allSharedFiles = null;
	String[] myPerJobArguments = null;
	String[] allPerJobFiles = null;

        // ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
        // BEGIN PROTECT: beanSetup

	/* OLD PAUP
        PAUPParser paupFile = new PAUPParser(myBean.getInputFile(), "");

        // Store input/output files (no path info) in the Java Bean.
	String[] inputFiles = paupFile.getInputFiles(false);
	for(int i = 0; i < inputFiles.length; i++) {
	    sharedFiles.add(inputFiles[i]);
	}
        myBean.setOutputFiles(paupFile.getOutputFiles(false));

        // Now, upload the input files--we need the actual paths (if any) to do this.
        String [] files = paupFile.getInputFiles(true);

	// Update Java bean so that command file path will be non-path-qualified.
        myBean.setInputFile(new File(myBean.getInputFile()).getName());

	*/

	boolean parsed_command_file = false;

	String inputFile = myBean.getInputFile();
	String outputFile = myBean.getOutputFile();
	ArrayList<String> outputFileList = new ArrayList<String>();
	String[] output_files = null;
	String[] input_files = null; // used repeatedly for heterogeneous job batches

	try {
	    BufferedReader instream = new BufferedReader(new FileReader(inputFile));
	    String thisLine = null;
	    while( (thisLine = instream.readLine()) != null ) {
		// we need to check each line in the file to see if it is a file or a directory
		if((input_files = parseDirectory(thisLine, replica)) == null) {
		    String input_file = thisLine.trim();
		    String filename = (new File(input_file)).getName();
		    if(filename.equals(myBean.getCommandFile()) && parsed_command_file == false) {
			if(outputFile == "parse") {
			    PAUPParser paupFile = new PAUPParser(input_file, "");
			    myBean.setOutputFiles(paupFile.getOutputFiles(false));
			    parsed_command_file = true;
			}
		    }

		    // add this input file
		    sharedFiles.add( thisLine );

		} else {
		    // we know this is a directory full of files; now check each one to make sure it ends with _[0-9]*
		    for(int i = 0; i < input_files.length; i++) {
			String input_file = input_files[i];
			String filename = (new File(input_file)).getName();
			if(!filename.matches(".*_[0-9]+")) {
			    // this file wasn't named using our little convention, so let's bomb
			    log.error("Filenames in PAUP heterogeneous batches must end in _[0-9]* (!)");
			    System.exit(1);
			}
			if(filename.indexOf(myBean.getCommandFile()) != -1 && parsed_command_file == false) {
			    if(outputFile == "parse") {
				PAUPParser paupFile = new PAUPParser(input_file, "");
				myBean.setOutputFiles(paupFile.getOutputFiles(false));
				parsed_command_file = true;
			    }
			}
		    }
		    String filename = new File(input_files[0]).getName();
		    String fileItself = filename.substring(0,filename.lastIndexOf("_"));
		    perJobArguments.add(fileItself);
		    perJobFiles.add(input_files);
		}   
	    }
	    instream.close();
	} catch( Exception e) {
	    log.error("Exception: " + e);
	    System.exit(1);
	}

	if(outputFile != "parse") {
	    try {
		BufferedReader outstream = new BufferedReader(new FileReader(outputFile));
		String thisLine = null;
		while( (thisLine = outstream.readLine()) != null ) {
		    outputFileList.add( thisLine );
		}
		outstream.close();
	    } catch( Exception e) {
		log.error("Exception: " + e);
		System.exit(1);
	    }
	    
	    output_files = new String[outputFileList.size()];
	    for( int i = 0 ; i < outputFileList.size(); i++ ) {
		output_files[i] = (new File((String)outputFileList.get(i))).getName();
	    }	
	    
	    myBean.setOutputFiles( output_files );
	} else if(parsed_command_file == false) {
	    log.error("Couldn't find a command file from which to extract output files!");
	    System.exit(1);
	}
                                                                        
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

	// check for scheduler override
	File scheduler_override = new File("scheduler_override");
	if(scheduler_override.exists()) {
	    BufferedReader br = new BufferedReader(new FileReader(scheduler_override));
	    myBean.setSchedulerOverride(br.readLine());
	    br.close();
	}

        // fill in job attributes
        myBean.setAppName(new String("PAUP"));
        myBean.setOwner(System.getProperty("user.name"));
        myBean.setJobName(jobname);
	myBean.setWorkingDir(workingDir);

        System.out.println("Submitting job.");

        // instancePortType object comes from super-class
        ((PAUPPortType)instancePortType).runService(myBean);
        System.out.println("Job submitted with ID: " + resourceID);

	// destroy the service instance
	this.destroy();
    }
}
