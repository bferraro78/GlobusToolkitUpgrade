package edu.umd.grid.bio.structure.clients;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.GSBLClient;
import edu.umd.umiacs.cummings.GSBL.GSBLPropertiesManager;
import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager;

// stub classes
import edu.umd.grid.bio.structure.stubs.StructureService.StructurePortType;
import edu.umd.grid.bio.structure.stubs.StructureService.StructureArguments;
import edu.umd.grid.bio.structure.stubs.Structure.service.StructureServiceAddressingLocator;
import edu.umd.grid.bio.structure.stubs.StructureFactory.service.StructureFactoryServiceAddressingLocator;

// For getting environment variables
import java.util.Properties;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;

// Place service specific imports here between the protection comments.
// BEGIN PROTECT: SubImports
import edu.umd.grid.bio.structure.shared.StructureParser;
    // END PROTECT: SubImports

/* GSG_USER (file requires user editing)
 *
 * PROTECT CONFIG
 * PROTECT: SubImports
 * PROTECT: beanSetup
 * END CONFIG
 */

/**
 * The client for the Structure Grid service.
 */
public class StructureSubmitClient extends GSBLClient {

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(StructureSubmitClient.class);

    /**
     * StructureArguments "Bean"
     */
    protected StructureArguments myBean;
    
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
        StructureArguments myBean = new StructureArguments();
        try {
            GSBLPropertiesManager GPM = new GSBLPropertiesManager(propertiesFile);
            GPM.updateJavaBean(myBean);
        } catch (Exception e) {
            System.exit(1);
        }

        System.out.println("Creating Structure job.");

        StructureSubmitClient client = null;
        try {
            client = new StructureSubmitClient(factoryURI, myBean);
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
    public StructureSubmitClient(String factoryURI, StructureArguments myBean) throws Exception {
        super("Structure", factoryURI, StructureFactoryServiceAddressingLocator.class, StructureServiceAddressingLocator.class);
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
    	((StructurePortType)instancePortType).createWorkingDir(resourceID + "@--" + cwd + "@--" + hostname + "@--" + reps);

	ArrayList<String> sharedFiles = new ArrayList<String>();
	ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
	ArrayList<String> perJobArguments = new ArrayList<String>();

	String[] allSharedFiles = null;
	String[] myPerJobArguments = null;
	String[] allPerJobFiles = null;

        // ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
        // BEGIN PROTECT: beanSetup
        
	String mainparamsFileNames[];
	String extraparamsFileNames[];
	String inputfileFileNames[];

	String mainparams = myBean.getMainparams();
	String extraparams = myBean.getExtraparams();
	String inputfile = myBean.getInputFile();

	StructureParser mainparamsParser;
	StructureParser extraparamsParser;

	// Default parameter filenames
        if(mainparams == null || mainparams.equals("")) {
	    mainparams = "mainparams";
	    // make sure the mainparams file exists
	    File f = new File(mainparams);
	    if(!f.exists()) {
		System.out.println("mainparams file doesn't exist!\n");
		System.exit(1);
	    }
	    mainparamsParser = new StructureParser(mainparams,"");
            myBean.setMainparams(mainparams);
	    sharedFiles.add(mainparams);
	} else {
	    // check to see if mainparams is a directory
	    if((mainparamsFileNames = parseDirectory(mainparams, replica)) == null) {
		mainparamsParser = new StructureParser(mainparams,"");
		sharedFiles.add(mainparams);
	    } else {
		// we need to init the parser with a file from the directory
		mainparamsParser = new StructureParser(mainparamsFileNames[0],"");
		perJobArguments.add("mainparams");
		perJobFiles.add(mainparamsFileNames);
	    }
	}

        if(extraparams == null || extraparams.equals("")) {
	    extraparams = "extraparams";
	    // make sure the extraparams file exists
	    File f = new File(extraparams);
	    if(!f.exists()) {
		System.out.println("extraparams file doesn't exist!\n");
		System.exit(1);
	    }
	    extraparamsParser = new StructureParser(extraparams,"");
            myBean.setExtraparams(extraparams);
	    sharedFiles.add(extraparams);
	} else {
	    // check to see if extraparams is a directory
	    if((extraparamsFileNames = parseDirectory(extraparams, replica)) == null) {
		extraparamsParser = new StructureParser(extraparams,"");
		sharedFiles.add(extraparams);
	    } else {
		// we need to init the parser with a file from the directory
		extraparamsParser = new StructureParser(extraparamsFileNames[0],"");
		perJobArguments.add("extraparams");
		perJobFiles.add(extraparamsFileNames);
	    }
	}
	
	
        // Set the input filename
        if(myBean.getInputFile() == null || myBean.getInputFile().equals("")) {
	    if(mainparamsParser.getInputFile() == null || mainparamsParser.getInputFile().equals("")) {
		if(extraparamsParser.getInputFile() == null || extraparamsParser.getInputFile().equals("")) {
		    // No input file specified; structure will fail, so we can catch it first
		    System.out.println("No input file specified!\n");
		    System.exit(1);
		} else {
		    myBean.setInputFile(extraparamsParser.getInputFile());
		    sharedFiles.add(extraparamsParser.getInputFile());
		}
	    } else {
		myBean.setInputFile(mainparamsParser.getInputFile());
		sharedFiles.add(mainparamsParser.getInputFile());
	    }
	} else {
	    // check to see if input file is a directory
	    if((inputfileFileNames = parseDirectory(inputfile, replica)) == null) {
		sharedFiles.add(inputfile);
	    } else {
		perJobArguments.add("inputfile");
		perJobFiles.add(inputfileFileNames);
	    }
	}
	
        // Set the output filename
        if(myBean.getOutputFile() == null || myBean.getOutputFile().equals("")) {
	    if(mainparamsParser.getOutputFile() == null || mainparamsParser.getOutputFile().equals("")) {
		if(extraparamsParser.getOutputFile() == null || extraparamsParser.getOutputFile().equals("")) {
		    myBean.setOutputFile("output");
		} else {
		    myBean.setOutputFile(extraparamsParser.getOutputFile());
		}
	    } else {
		myBean.setOutputFile(mainparamsParser.getOutputFile());
	    }
	}
	
        // Get input/output filenames into arrays
        /*String [] inputFilesWithPaths = {
            myBean.getMainparams(),
            myBean.getExtraparams(),
            myBean.getInputFile()
        };
        String [] inputFilesWithoutPaths = {
            new File(myBean.getMainparams()).getName(),
            new File(myBean.getExtraparams()).getName(),
            new File(myBean.getInputFile()).getName()
	    };*/

	// structure appends this funky _f suffix to the output file name
        String [] outputFilesWithoutPaths = {
            new File(myBean.getOutputFile()).getName() + "_f",
        };

        // Save the no-paths versions of the input/output files
        // myBean.setInputFiles(inputFilesWithoutPaths);
        myBean.setOutputFiles(outputFilesWithoutPaths);

	//String [] files = inputFilesWithPaths;
	
	
        // Upload all required input files to the service
	//   for (int i = 0; i < inputFilesWithPaths.length; i++) {
            // String filename = inputFilesWithPaths[i];
//             log.debug("Starting transfer for: " + filename);
//             GassFileTransferManager inputFile = new GassFileTransferManager(filename,
// 									    getGassServer().getURL(),
// 									    GassFileTransferManager.OP_UPLOAD,
// 									    this);

	    

//             inputFile.beginTransfer();
//             if (!inputFile.waitComplete()) {
//                 log.error("Unable to upload file: " + filename);
//                 destroy(true);
//                 System.exit(1);
//             }
//         }
	

                                                                                                                                                                        
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
        myBean.setAppName(new String("Structure"));
        myBean.setOwner(System.getProperty("user.name"));
        myBean.setJobName(jobname);
	myBean.setWorkingDir(workingDir);

        System.out.println("Submitting job.");

        // instancePortType object comes from super-class
        ((StructurePortType)instancePortType).runService(myBean);
        System.out.println("Job submitted with ID: " + resourceID);

	// destroy the service instance
	this.destroy();
    }
}
