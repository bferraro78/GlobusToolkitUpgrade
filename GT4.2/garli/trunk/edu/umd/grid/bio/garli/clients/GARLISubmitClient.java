/**
 * @author Adam Bazinet
 * @author Ben Ferraro
 * @author Jordan Kiesel
 */
package edu.umd.grid.bio.garli.clients;

// Garli Classes.
import edu.umd.grid.bio.garli.impl.GARLIService;

// GSBL classes.
import edu.umd.umiacs.cummings.GSBL.GSBLClient;
import edu.umd.umiacs.cummings.GSBL.GSBLPropertiesManager;

// Stub classes.
import edu.umd.grid.bio.garli.stubs.GARLIService.GARLIArguments;
import edu.umd.grid.bio.garli.stubs.GARLI.service.GARLIServiceAddressingLocator;
import edu.umd.grid.bio.garli.stubs.GARLIFactory.service.GARLIFactoryServiceAddressingLocator;

// For getting environment variables.
import java.util.Properties;

// For logging.
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;

// Place service specific imports here between the protection comments.
// BEGIN PROTECT: SubImports
import edu.umd.grid.bio.garli.shared.GARLIParser;
import java.util.Arrays;
// END PROTECT: SubImports

public class GARLISubmitClient extends GSBLClient {

	/**
	 * Logger.
	 */
	private static Log log = LogFactory.getLog(GARLISubmitClient.class);

	/**
	 * GARLIArguments "Bean".
	 */
	protected GARLIArguments myBean;

	/**
	 * Job name, i.e., current working directory.
	 */
	private static String jobname;

	/**
	 * Keeps track of JobID.
	 */
	private static String jobID;

	/**
	 * The main method. Reads in arguments from a properties file, creates a
	 * client, and executes it.
	 */
	public static void main(String[] args) {
		/* Checks to see how many command line arguments are included.
		 * Two are needed (job properties file and a job name). */
		if (args.length != 2) {
			System.err.println("Requires 2 arguments: properties file, and job name.");
			System.exit(1);
		}

		String propertiesFile = args[0];
		jobname = args[1];

		/* Creates a bean, reads properties file, and updates bean with job
		 * properties. */
		GARLIArguments myBean = new GARLIArguments();
		try {
			GSBLPropertiesManager GPM =
					new GSBLPropertiesManager(propertiesFile);
			GPM.updateJavaBean(myBean);
		} catch (Exception e) {
			System.exit(1);
		}

		System.out.println("Creating GARLI job.");

		/* Aquires new Service Instance and initializes jobID. */
		GARLISubmitClient client = null;
		try {
			client = new GARLISubmitClient(myBean);
		} catch (Exception e) {
			System.exit(1);
		}

		/* Run the client. */
		try {
			client.execute();
		} catch (Exception e) {
			log.error("Error executing Grid service: " + e);
		}
	}  // End main function.

	/**
	 * Constructor.
	 */
	public GARLISubmitClient(GARLIArguments myBean)
			throws Exception {
		super("GARLI");
		this.myBean = myBean;
	}

	void execute() throws Exception {
		// Get a unique job id.
		jobID = super.getJobID();

		/* Store the directory the client submitted from as well as the
		 * hostname. */
		Properties env = new Properties();
		env.load(Runtime.getRuntime().exec("env").getInputStream());
		String cwd = (String)env.get("PWD");
		String hostname = (String)env.get("HOSTNAME");

		// Create a working directory on the service side.
		Integer replicates = myBean.getReplicates();
		String reps = "";
		int replica = 0;
		if ((replicates == null) || (replicates.intValue() <= 1)) {
			reps = "1";
			replica = 1;
		} else {
			reps = replicates.toString();
			replica = replicates.intValue();
		}
	
		/* If replicates > 1, then will created a output and populate it with
		 * sub-job folders within /export/work/drupal/user_files/admin/job# */
		createWorkingDir(jobID + "@--" + cwd
				+ "@--" + hostname + "@--" + reps);

		ArrayList<String> sharedFiles = new ArrayList<String>();
		ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
		ArrayList<String> perJobArguments = new ArrayList<String>();

		String[] allSharedFiles = null;
		String[] myPerJobArguments = null;
		String[] allPerJobFiles = null;
		
		// ----- ----- ----- YOUR CODE HERE ----- ----- ----- //
		// BEGIN PROTECT: beanSetup

		String confFileNames[] = null;
		boolean buildConfig = false;
		String configFile = myBean.getConfigFile();

		if ((configFile == "") || (configFile == null)) {  /* We will build a
				configuration file from the args passed in. */
			buildConfig = true;
		} else if ((confFileNames = parseDirectory(configFile, replica))
				== null) {  // Single job or homogeneous batch.

		} else {  // Heterogeneous job batch.
			perJobArguments.add("configFile");
			perJobFiles.add(confFileNames);
		}
		
		/* Check if we need to validate conifg file. */
		boolean doValidate = true;
		if (myBean.getNovalidate() != null) {
			if ((myBean.getNovalidate()).booleanValue() == true) {
				doValidate = false;
			}
		}

		/* Builds, validates, and parses config file (parse puts input/output
		 * files into a vector, but first parse() creates the output files). */
		GARLIParser gp = null;
		try {
			gp = new GARLIParser(myBean, cwd, buildConfig, doValidate, true);
		} catch (Exception e) {
			System.out.println("Unknown exception occurred while invoking the GARLI parser "
					+ e);
		}
		/* Specifies memory restriction in garliconf file. */
		String avail_mem = gp.getAvailMem();
		if (avail_mem == null) {
			log.error("Please specify \"available memory\" in the GARLI config file\n");
			System.exit(1);
		}

		if (doValidate) {
			String unique_patterns = gp.getUniquePatterns();
			String num_taxa = gp.getNumTaxa();
			String actual_memory = gp.getActualMem();

			myBean.setUniquepatterns(unique_patterns);
			myBean.setNumtaxa(num_taxa);
			myBean.setActualmemory(actual_memory);
		}

		/* Sets configuration file name to bean. */
		// myBean.setConfigFile(gp.getConfigFileName());  /* Now this should no longer be set to a directory. */

		sharedFiles.addAll(gp.getInputFiles());  /* gp.getInputFiles() currently only returns shared files. */

		String[] output_files = gp.getOutputFiles();   
		myBean.setOutputFiles(output_files);

		// END PROTECT: beanSetup
		// ----- ----- ----- END YOUR CODE ----- ----- ----- //
		
		myPerJobArguments = new String[perJobArguments.size()];
		myBean.setPerJobArguments(perJobArguments.toArray(myPerJobArguments));

		/* Sets the working directory for specific job as
		 * /export/work/drupal/user_files/admin/job#/ */
		String workingDir = cwd + "/";

		/* Sets bean's "sharedFiles" with shared files used by all instances
		 * submitted. */
		allSharedFiles = new String[sharedFiles.size()];
		myBean.setSharedFiles(sharedFiles.toArray(allSharedFiles));

		allPerJobFiles = new String[perJobFiles.size()];

		for (int i = 0; i < perJobFiles.size(); i++) {
			String[] tempcouples = perJobFiles.get(i);
			String filenames = "";
			for (int j = 0; j < tempcouples.length; j++) {
				if (j < (tempcouples.length - 1)) {
					// Add the ':' couple delimiter.
					filenames += (tempcouples[j] + ":");
				} else {
					filenames += tempcouples[j];
				}
			}
			allPerJobFiles[i] = filenames;
		}

		/* Sets bean's perjob files. */
		myBean.setPerJobFiles(allPerJobFiles);

		// Check for scheduler override.
		File scheduler_override = new File("scheduler_override");
		if (scheduler_override.exists()) {
			BufferedReader br =
					new BufferedReader(new FileReader(scheduler_override));
			myBean.setSchedulerOverride(br.readLine());
			br.close();
		}

		// Fill in job attributes.
		myBean.setAppName(new String("GARLI"));
		myBean.setOwner(System.getProperty("user.name"));
		myBean.setJobName(jobname);
		myBean.setJobID(jobID); // added jobID to bean so we can access jobID on service side
		myBean.setWorkingDir(workingDir);

		System.out.println("Submitting job.");

		/* Call to runService(). */
		GARLIService garli_service = new GARLIService(myBean);

		System.out.println("Job submitted with ID: " + jobID);

		// Destroy the service instance.
		this.destroy();
	}  // End execute.
}  // End class.
