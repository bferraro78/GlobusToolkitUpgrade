package edu.umd.grid.bio.marxan.clients;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.GSBLClient;
import edu.umd.umiacs.cummings.GSBL.GSBLPropertiesManager;
import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager;

// stub classes
import edu.umd.grid.bio.marxan.stubs.MARXANService.MARXANPortType;
import edu.umd.grid.bio.marxan.stubs.MARXANService.MARXANArguments;
import edu.umd.grid.bio.marxan.stubs.MARXAN.service.MARXANServiceAddressingLocator;
import edu.umd.grid.bio.marxan.stubs.MARXANFactory.service.MARXANFactoryServiceAddressingLocator;

// For getting environment variables
import java.util.Properties;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;

// Place service specific imports here between the protection comments.
// BEGIN PROTECT: SubImports
import java.io.*;
import java.util.*;
    // END PROTECT: SubImports

/* GSG_USER (file requires user editing)
 *
 * PROTECT CONFIG
 * PROTECT: SubImports
 * PROTECT: beanSetup
 * END CONFIG
 */

/**
 * The client for the MARXAN Grid service.
 */
public class MARXANSubmitClient extends GSBLClient {

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(MARXANSubmitClient.class);

    /**
     * MARXANArguments "Bean"
     */
    protected MARXANArguments myBean;
    
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
        MARXANArguments myBean = new MARXANArguments();
        try {
            GSBLPropertiesManager GPM = new GSBLPropertiesManager(propertiesFile);
            GPM.updateJavaBean(myBean);
        } catch (Exception e) {
            System.exit(1);
        }

        System.out.println("Creating MARXAN job.");

        MARXANSubmitClient client = null;
        try {
            client = new MARXANSubmitClient(factoryURI, myBean);
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
    public MARXANSubmitClient(String factoryURI, MARXANArguments myBean) throws Exception {
        super("MARXAN", factoryURI, MARXANFactoryServiceAddressingLocator.class, MARXANServiceAddressingLocator.class);
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
    	((MARXANPortType)instancePortType).createWorkingDir(resourceID + "@--" + cwd + "@--" + hostname + "@--" + reps);

	ArrayList<String> sharedFiles = new ArrayList<String>();
	ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
	ArrayList<String> perJobArguments = new ArrayList<String>();

	String[] allSharedFiles = null;
	String[] myPerJobArguments = null;
	String[] allPerJobFiles = null;

        // ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
        // BEGIN PROTECT: beanSetup

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

		    // CHECKING RANDOM SEED
		    if(filename.indexOf("input.dat") != -1) {
			log.debug("inspecting index.dat...");
			boolean wasModified = false;
			// we need to open up this file and check the value of RANDSEED
			BufferedReader inputfile = new BufferedReader(new FileReader(input_file));
			// for storing a potentially new file
			StringBuffer inputBuffer = new StringBuffer();
			String line = "";
			while((line = inputfile.readLine()) != null) {
			    if(line.indexOf("RANDSEED") != -1) {
				String seed = line.substring(line.indexOf(" "));
				seed = seed.trim();
				if(seed.equals("-1")) {
				    if(replica <= 1) {
					Random generator = new Random();
					// generate a random seed between 0-99999
					int random_seed = generator.nextInt(100000);
					inputBuffer.append("RANDSEED " + (new Integer(random_seed)).toString() + "\n");
					wasModified = true;
				    } else { // if this is a batch of jobs, throw an error
					log.error("RANDSEED -1 is not allowed for a batch of jobs!\nModify your input.dat file(s) and try again!");
					System.exit(1);
				    }
				} else {
				    inputBuffer.append(line + "\n");
				}
			    } else {
				inputBuffer.append(line + "\n");
			    }
			}
			if(wasModified == true) {
			    try {
				// rewrite the input.dat file -- their seed will now be something other than -1
				BufferedWriter writer = new BufferedWriter(new FileWriter(input_file + "_deleteme"));
				writer.write(inputBuffer.toString());
				writer.close();
				
				File newinput = new File(input_file + "_deleteme");
				boolean didRename = false;
				didRename = newinput.renameTo(new File(input_file));
				if(didRename == false) {
				    log.error("Could not rewrite input.dat file... possible trouble ahead...");
				}
			    } catch (Exception e) {
				log.error("Exception: " + e);
				System.exit(1);
			    }
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
			    log.error("Filenames in MARXAN heterogeneous batches must end in _[0-9]* (!)");
			    System.exit(1);
			}

			// CHECKING RANDOM SEED
			if(filename.indexOf("input.dat") != -1) {
			    log.debug("inspecting index.dat...");
			    boolean wasModified = false;
			    // we need to open up this file and check the value of RANDSEED
			    BufferedReader inputfile = new BufferedReader(new FileReader(input_file));
			    // for storing a potentially new file
			    StringBuffer inputBuffer = new StringBuffer();
			    String line = "";
			    while((line = inputfile.readLine()) != null) {
				if(line.indexOf("RANDSEED") != -1) {
				    String seed = line.substring(line.indexOf(" "));
				    seed = seed.trim();
				    if(seed.equals("-1")) {
					if(replica <= 1) {
					    Random generator = new Random();
					    // generate a random seed between 0-99999
					    int random_seed = generator.nextInt(100000);
					    inputBuffer.append("RANDSEED " + (new Integer(random_seed)).toString() + "\n");
					    wasModified = true;
					} else { // if this is a batch of jobs, throw an error
					    log.error("RANDSEED -1 is not allowed for a batch of jobs!\nModify your input.dat file(s) and try again!");
					    System.exit(1);
					}
				    } else {
					inputBuffer.append(line + "\n");
				    }
				} else {
				    inputBuffer.append(line + "\n");
				}
			    }
			    if(wasModified == true) {
				try {
				    // rewrite the input.dat file -- their seed will now be something other than -1
				    BufferedWriter writer = new BufferedWriter(new FileWriter(input_file + "_deleteme"));
				    writer.write(inputBuffer.toString());
				    writer.close();
				    
				    File newinput = new File(input_file + "_deleteme");
				    boolean didRename = false;
				    didRename = newinput.renameTo(new File(input_file));
				    if(didRename == false) {
					log.error("Could not rewrite input.dat file... possible trouble ahead...");
				    }
				} catch (Exception e) {
				    log.error("Exception: " + e);
				    System.exit(1);
				}
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
        myBean.setAppName(new String("MARXAN"));
        myBean.setOwner(System.getProperty("user.name"));
        myBean.setJobName(jobname);
	myBean.setWorkingDir(workingDir);

        System.out.println("Submitting job.");

        // instancePortType object comes from super-class
        ((MARXANPortType)instancePortType).runService(myBean);
        System.out.println("Job submitted with ID: " + resourceID);

	// destroy the service instance
	this.destroy();
    }
}
