package edu.umd.grid.bio.hmmpfam.clients;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.GSBLClient;
import edu.umd.umiacs.cummings.GSBL.GSBLPropertiesManager;
import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager;

// stub classes
import edu.umd.grid.bio.hmmpfam.stubs.HMMPfamService.HMMPfamPortType;
import edu.umd.grid.bio.hmmpfam.stubs.HMMPfamService.HMMPfamArguments;
import edu.umd.grid.bio.hmmpfam.stubs.HMMPfam.service.HMMPfamServiceAddressingLocator;
import edu.umd.grid.bio.hmmpfam.stubs.HMMPfamFactory.service.HMMPfamFactoryServiceAddressingLocator;

// For getting environment variables
import java.util.Properties;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;

// Place service specific imports here between the protection comments.
// BEGIN PROTECT: SubImports

    // END PROTECT: SubImports

/* GSG_USER (file requires user editing)
 *
 * PROTECT CONFIG
 * PROTECT: SubImports
 * PROTECT: beanSetup
 * END CONFIG
 */

/**
 * The client for the HMMPfam Grid service.
 */
public class HMMPfamSubmitClient extends GSBLClient {

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(HMMPfamSubmitClient.class);

    /**
     * HMMPfamArguments "Bean"
     */
    protected HMMPfamArguments myBean;
    
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
        HMMPfamArguments myBean = new HMMPfamArguments();
        try {
            GSBLPropertiesManager GPM = new GSBLPropertiesManager(propertiesFile);
            GPM.updateJavaBean(myBean);
        } catch (Exception e) {
            System.exit(1);
        }

        System.out.println("Creating HMMPfam job.");

        HMMPfamSubmitClient client = null;
        try {
            client = new HMMPfamSubmitClient(factoryURI, myBean);
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
    public HMMPfamSubmitClient(String factoryURI, HMMPfamArguments myBean) throws Exception {
        super("HMMPfam", factoryURI, HMMPfamFactoryServiceAddressingLocator.class, HMMPfamServiceAddressingLocator.class);
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
    	((HMMPfamPortType)instancePortType).createWorkingDir(resourceID + "@--" + cwd + "@--" + hostname + "@--" + reps);

	ArrayList<String> sharedFiles = new ArrayList<String>();
	ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
	ArrayList<String> perJobArguments = new ArrayList<String>();

	String[] allSharedFiles = null;
	String[] myPerJobArguments = null;
	String[] allPerJobFiles = null;

        // ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
        // BEGIN PROTECT: beanSetup

	String hmmFileNames[];
	String fastaFileNames[];
		
	String hmmFile = myBean.getHmmFile();
	if(hmmFile == "" || hmmFile == null){
	    log.error("Pfam HMM file is empty!");
	    System.exit(1);
	}else if((hmmFileNames = parseDirectory(hmmFile, replica)) == null){
	    sharedFiles.add(hmmFile);
	}else{
	    perJobArguments.add("hmmFile");
	    perJobFiles.add(hmmFileNames);
	}

	String fastaFile = myBean.getSeqFastaFile();
	if(fastaFile == "" || fastaFile == null){
	    log.error("Sequence FASTA file is empty!");
	    System.exit(1);
	}else if((fastaFileNames = parseDirectory(fastaFile, replica)) == null){
	    sharedFiles.add(fastaFile);
	    // check to make sure this is not a gzip'd file
	    if((fastaFile.substring(fastaFile.length() - 2)).equals("gz")) {
		log.error("gzip'd FASTA files are not supported by the HMMPfam service!");
		System.exit(1);
	    }
			    
	}else{
	    perJobArguments.add("fastaFile");
	    perJobFiles.add(fastaFileNames);
	    // check to make sure there are no gzip'd files
	    for(int i = 0; i < fastaFileNames.length; i++) {
		if((fastaFileNames[i].substring(fastaFile.length() - 2)).equals("gz")) {
		    log.error("gzip'd FASTA files are not supported by the HMMPfam service!");
		    System.exit(1);
		}
	    }
	}

	//check flags for validity
	if(myBean.getAlignment() != null && myBean.getAlignment() < 0){
	    log.error("Alignment number must be positive!");
	    System.exit(1);
	}
	if(myBean.getSetEValue() != null && myBean.getSetEValue() < 0){
	    log.error("The E-value must be positive!");
	    System.exit(1);
	}
	if(myBean.getCalcEValue() != null && myBean.getCalcEValue() < 0){
	    log.error("Cannot calculate the E-value w/ a negative number of sequences!");
	    System.exit(1);
	}
		
	if(myBean.getCpu() != null && myBean.getCpu() < 0){
	    log.error("Number of CPUs has to be positive!");
	    System.exit(1);
	}
	if(myBean.getDomE() != null && myBean.getDomE() < 0){
	    log.error("the E-value cutoff for the per-domain ranked hitlist has to be a positive number!");
	    System.exit(1);
	}
	String format = myBean.getInformat();
	if(format != null && !format.equals("FASTA") && 
	   !format.equals("GENBANK") && !format.equals("EMBL") && 
	   !format.equals("GCG") && !format.equals("PIR") &&
	   !format.equals("STOCKHOLM") && !format.equals("SELEX") && 
	   !format.equals("MSF") && !format.equals("CLUSTAL") && 
	   !format.equals("PHYLIP")){
			
	    log.error(format + " is not a valid seqfile format!");
	    System.exit(1);
	}
		
	String output = myBean.getOutput();
	if(output != null){
	    //only 1 output file	
	    String[] outputs = new String[1];
	    outputs[0] = output;
	    myBean.setOutputFiles(outputs);
	} else {
	    log.error("You need to specify an output file!");
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

        // fill in job attributes
        myBean.setAppName(new String("HMMPfam"));
        myBean.setOwner(System.getProperty("user.name"));
        myBean.setJobName(jobname);
	myBean.setWorkingDir(workingDir);

        System.out.println("Submitting job.");

        // instancePortType object comes from super-class
        ((HMMPfamPortType)instancePortType).runService(myBean);
        System.out.println("Job submitted with ID: " + resourceID);

	// destroy the service instance
	this.destroy();
    }
}
