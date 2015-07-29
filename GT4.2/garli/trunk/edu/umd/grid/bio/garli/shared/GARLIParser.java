package edu.umd.grid.bio.garli.shared;

import java.io.*;
import java.util.*;

import java.lang.Runtime;

// For logging.
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

// Stub classes.
import edu.umd.grid.bio.garli.stubs.GARLIService.GARLIArguments;
import edu.umd.grid.bio.garli.shared.Base64Coder;

/**
 * Class that parses the garli.conf file to determine which files will be
 * input/output.
 *
 * @author Adam Bazinet (adam.bazinet@umiacs.umd.edu)
 * @author Matthew Conte (conte@umiacs.umd.edu)
 *
 */

public class GARLIParser {

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(GARLIParser.class.getName());

	/**
	 * The filename of the config file to parse.
	 */
	protected String configFileName;

	/**
	 * Where to look for files.
	 */
	String myWorkingDir;

	/**
	 * The files needed for input.
	 */
	Vector inputFiles;

	/**
	 * The files produced as output.
	 */
	Vector outputFiles;

	/**
	 * String buffer for storing the config file.
	 */
	protected StringBuffer configBuffer;

	/**
	 * The minimum amount of memory that should be specified.
	 */
	protected String min_mem = null;

	/**
	 * The maximum amount of memory that should be specified.
	 */
	protected String max_mem = null;

	/**
	 * The actual amount of memory GARLI will use.
	 */
	protected String actual_mem = null;

	/**
	 * The number of unique patterns in the data set.
	 */
	protected String unique_patterns = null;

	/**
	 * The number of taxa in the data set.
	 */
	protected String num_taxa = null;

	/**
	 * The amount of memory currently specified.
	 */
	protected String avail_mem = null;

	/**
	 * The maximum amount of memory we are currently allowing (12000M).
	 */
	protected int max_sys_mem = 12000;

	/**
	 * Memory "tipping point" (1G).
	 */
	protected int med_sys_mem = 1024;

	/**
	 * Number of search replicates (in GARLI config, "searchreps").
	 */
	protected Integer searchreps = new Integer(1);

	/**
	 * Number of bootstrap replicates (in GARLI config, "bootstrapreps").
	 */
	protected Integer bootstrapreplicates = new Integer(0);

	/**
	 * Whether or not the data file is valid.
	 */
	protected boolean valid_dataf = false;

	/**
	 * GARLIArguments "Bean".
	 */
	protected GARLIArguments argBean = null;

	/**
	 * The GLOBUS_LOCATION.
	 */
	protected static String globusLocation = "";

	/**
	 * Whether or not we built the config file ourselves.
	 */
	protected boolean built_config = false;

	/**
	 * Whether or not we validated the input files.
	 */
	protected boolean validated_input = false;

	/**
	 * Constructor
	 * @param myBean		Argument bean.
	 * @param workingDir	The working directory to use cwd -
	 * 							"/export/work/drupal/user_files/admin/job*".
	 * @param buildConfig	Build a config file from command line arguments.
	 * @param doValidate	Validate the data file; get memory to be used.
	 * @param doParse		Parse the config file (true/false).
	 */
	public GARLIParser(GARLIArguments myBean, String workingDir,
			boolean buildConfig, boolean doValidate, boolean doParse)
					throws Exception {

		// Determine the Globus location.
		Properties env = new Properties();
		try {        	
			env.load(Runtime.getRuntime().exec("env").getInputStream());
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
		// Will be blank in GT6.
		globusLocation = (String) env.get("GLOBUS_LOCATION");

		// Get shared files.
		String[] tempSharedFiles = myBean.getSharedFiles();
		if (tempSharedFiles == null) {
			tempSharedFiles = new String[0];
		}
		ArrayList<String> sharedFiles =
				new ArrayList<String>(Arrays.asList(tempSharedFiles));

		// Get per-job files.
		String[] tempPerJobFiles = myBean.getPerJobFiles();

		if (tempPerJobFiles == null) {
			tempPerJobFiles = new String[0];
		}
		ArrayList<String[]> perJobFiles = new ArrayList<String[]>();
		for (int i = 0; i < tempPerJobFiles.length; i++) {
			String filenames = tempPerJobFiles[i];
			String[] chunks = filenames.split(":");
			perJobFiles.add(chunks);
		}

		argBean = myBean;

		this.inputFiles = new Vector();
		this.outputFiles = new Vector();

		if (buildConfig == false) {  // Config file already exists.

			// Determine if path-to-config-file is a directory.
			File dir = new File(workingDir + myBean.getConfigFile());

			if (!dir.exists()) {
				System.out.println("1");
				// String justTheName = dir.getName();
				/*
				 * Hack: (This whole service should be rewritten anyway.)
				 * Assumption: This is on the server side, and we need to find
				 * a garli.conf that now exists in the cache.
				 */
				/* String tempWorkingDir =
						workingDir.substring(0, (workingDir.length() - 1)); */
				/*
				String baseDir =
						workingDir.substring(0, workingDir.lastIndexOf("/"));
				baseDir = baseDir.substring(0, baseDir.lastIndexOf("/"));
				String cacheDir = baseDir + "/cache/";
				boolean foundConfigFile = false;
				*/

				this.configFileName = (workingDir + "/" + myBean.getConfigFile());

				/*
				for (String sharedFile : sharedFiles) {
					if (sharedFile.indexOf(justTheName) != -1) {  // We've got a
							// match.
						this.configFileName = (cacheDir + sharedFile);
						foundConfigFile = true;
						break;
					}
				}
				if (foundConfigFile == false) {  // Check per-job files.
					for (String[] perJobArray : perJobFiles) {
						for (int i = 0; i < perJobArray.length; i++) {
							String perJobFile = perJobArray[i];
							if (perJobFile.indexOf(justTheName) != -1) {
									// We've got a match.
								this.configFileName = cacheDir + perJobFile;
								foundConfigFile = true;
								break;
							}
						}
						if (foundConfigFile == true) {
							break;
						}
					}
				}

				if (foundConfigFile == false) {
					log.error("ERROR: no conf file found when searching through shared/per-job files!");
					this.configFileName = "";
				}
				*/

				log.debug("configFileName is: " + configFileName);
			} else if (dir.isDirectory()) {  /* We need to grab a sample
					garli.conf file out of here to use for parsing. */
					System.out.println("2");
				String[] filenames = dir.list();
				this.configFileName = workingDir + myBean.getConfigFile() + "/"
						+ filenames[0];
			} else {
				System.out.println("3");
				this.configFileName = workingDir + myBean.getConfigFile();
				// Add to input files (shared files, really) here.
				inputFiles.add(configFileName);
			}
		}

		configBuffer = new StringBuffer();
		myWorkingDir = workingDir;

		if (buildConfig) {
			this.built_config = true;
			buildConfig();
		}

		if (doValidate) {
			this.validated_input = true;
			validate();
		}

		if (doParse) {
			parse();
		}
	}

	/**
	 * Build a GARLI configuration file using a combination of defaults and CL
	 * arguments. Write it out to the working directory and call it garli.conf.
	 */
	protected void buildConfig() throws Exception {
		configBuffer.append("[general]\n");

		if (argBean.getDatafname() == null) {  // This must be specified!
			log.error("Please specify a data file with the --datafname argument!");
			System.exit(1);
		} else {
			configBuffer.append("datafname = " + argBean.getDatafname() + "\n");
		}

		if (argBean.getConstraintfile() != null) {
			configBuffer.append("constraintfile = "
					+ argBean.getConstraintfile() + "\n");
		}

		if(argBean.getStreefname_userdata() != null) {
	    	configBuffer.append("streefname = " + argBean.getStreefname_userdata() + "\n");
		} else if(argBean.getStreefname() != null && (argBean.getStreefname().equalsIgnoreCase("stepwise") || argBean.getStreefname().equalsIgnoreCase("random"))) {
	    	configBuffer.append("streefname = " + argBean.getStreefname() + "\n");
		} else {
	    	configBuffer.append("streefname = stepwise\n");
		}

		if (argBean.getAttachmentspertaxon() != null) {
			configBuffer.append("attachmentspertaxon = "
					+ argBean.getAttachmentspertaxon() + "\n");
		} else {
			configBuffer.append("attachmentspertaxon = 50\n");
		}

		if (argBean.getOfprefix() != null) {
			configBuffer.append("ofprefix = " + argBean.getOfprefix() + "\n");
		} else {
			configBuffer.append("ofprefix = garli\n");
		}

		configBuffer.append("randseed = -1\n");
		configBuffer.append("availablememory = 512\n");
		// add a default value for workphasedivision
		configBuffer.append("workphasedivision = 0\n");
		configBuffer.append("logevery = 100000\n");
		configBuffer.append("saveevery = 100000\n");

		if (argBean.getRefinestart() != null) {
			if ((argBean.getRefinestart()).booleanValue() == true) {
				configBuffer.append("refinestart = 1\n");
			} else {
				configBuffer.append("refinestart = 0\n");
			}
		} else {
			configBuffer.append("refinestart = 1\n");
		}

		configBuffer.append("outputeachbettertopology = 0\n");
		configBuffer.append("outputcurrentbesttopology = 0\n");
		configBuffer.append("enforcetermconditions = 1\n");
		if((argBean.getAnalysistype() != null) && (argBean.getAnalysistype().equalsIgnoreCase("bootstrap"))) {
	    	configBuffer.append("genthreshfortopoterm = 10000\n"); // Derrick suggests halving this value for bootstrap runs
		} else {
	    	configBuffer.append("genthreshfortopoterm = 20000\n");
		}
		configBuffer.append("scorethreshforterm = 0.05\n");
		configBuffer.append("significanttopochange = 0.01\n");

		if (argBean.getOutputphyliptree() != null) {
			if ((argBean.getOutputphyliptree()).booleanValue() == true) {
				configBuffer.append("outputphyliptree = 1\n");
			} else {
				configBuffer.append("outputphyliptree = 0\n");
			}
		} else {
			configBuffer.append("outputphyliptree = 0\n");
		}

		configBuffer.append("outputmostlyuselessfiles = 0\n");
		configBuffer.append("writecheckpoints = 0\n");
		configBuffer.append("restart = 0\n");

		if (argBean.getOutgroup() != null) {
			String decodedOutgroup =
					Base64Coder.decodeString(argBean.getOutgroup());
			configBuffer.append("outgroup = " + decodedOutgroup + "\n");
		}

		if (argBean.getOutputsitelikelihoods() != null) {
			if ((argBean.getOutputsitelikelihoods()).booleanValue() == true) {
				configBuffer.append("outputsitelikelihoods = 1\n");
			} else {
				configBuffer.append("outputsitelikelihoods = 0\n");
			}
		} else {
			configBuffer.append("outputsitelikelihoods = 0\n");
		}

		if (argBean.getCollapsebranches() != null) {
			if ((argBean.getCollapsebranches()).booleanValue() == true) {
				configBuffer.append("collapsebranches = 1\n");
			} else {
				configBuffer.append("collapsebranches = 0\n");
			}
		} else {
			configBuffer.append("collapsebranches = 1\n");
		}

		if (argBean.getLinkmodels() != null) {
			if ((argBean.getLinkmodels()).booleanValue() == true) {
				configBuffer.append("linkmodels = 1\n");
			} else {
				configBuffer.append("linkmodels = 0\n");
			}
		}  // No default value, as this is optional.

		if (argBean.getSubsetspecificrates() != null) {
			if ((argBean.getSubsetspecificrates()).booleanValue() == true) {
				configBuffer.append("subsetspecificrates = 1\n");
			} else {
				configBuffer.append("subsetspecificrates = 0\n");
			}
		}  // No default value, as this is optional.

		if ((argBean.getSearchreps() != null)
				&& (argBean.getAnalysistype() != null)
				&& !argBean.getAnalysistype().equalsIgnoreCase("bootstrap")) {
			configBuffer.append("searchreps = " + argBean.getSearchreps()
					+ "\n\n");
		} else {
			configBuffer.append("searchreps = 1\n\n");
		}

		if (argBean.getOptimizeinputonly() != null) {
			if ((argBean.getOptimizeinputonly()).booleanValue() == true) {
				configBuffer.append("optimizeinputonly = 1\n");
			} else {
				configBuffer.append("optimizeinputonly = 0\n");
			}
		}  // No default value, as this is optional.

		if (argBean.getModelsdata() != null) {
			try {
				String decoded =
						Base64Coder.decodeString(argBean.getModelsdata());
				configBuffer.append(decoded + "\n");
			} catch (Exception e) {
				configBuffer.append("Length of Models Data: "
						+ (argBean.getModelsdata().length() % 4));
			}
		} else {
			if (argBean.getDatatype() != null) {
				configBuffer.append("datatype = " + argBean.getDatatype()
						+ "\n");
			} else {
				configBuffer.append("datatype = nucleotide\n");
			}

			if (argBean.getRatematrix() != null) {
				if ((argBean.getRatematrix()).equals("lg")) {
					// This method does not work with non-NEXUS files.
					// writeAARateMatrix("lg");
					configBuffer.append("parametervaluestring = r 243.500 38.656 101.598 24.819 202.114 35.106 14.657 52.486 38.675 109.961 27.080 115.206 94.882 41.586 462.446 209.301 249.250 17.679 21.420 6.120 0.342 108.123 55.689 62.662 31.366 1.298 58.110 87.426 51.728 7.374 8.297 52.294 272.397 111.863 191.672 65.557 114.020 512.992 1.704 82.657 90.697 1.046 27.681 1.475 2.499 496.584 38.588 51.201 12.126 121.332 41.661 3.714 2.924 13.217 1.840 34.127 41.467 4.330 176.791 6.816 16.996 52.994 41.030 403.888 35.606 59.867 59.141 23.971 7.616 11.743 8.764 66.732 108.855 2.340 253.635 175.976 8.758 9.241 3.508 5.158 35.396 16.142 64.046 240.373 763.432 30.472 0.852 29.019 4.330 13.651 140.640 19.268 26.214 38.171 170.218 12.701 7.503 26.266 5.349 10.652 68.211 35.836 43.286 441.125 49.779 470.891 237.387 96.850 57.157 11.643 58.408 519.152 15.561 405.499 418.074 18.734 7.658 7.127 12.423 6.271 101.128 1041.770 10.923 22.747 13.451 64.234 209.847 38.184 316.401 618.860 73.241 111.216 18.118 4.882 12.907 617.519 6.694 24.365 56.980 29.529 17.833 29.635 166.574 60.617 29.314 36.294 9.768 163.622 47.361 33.942 197.646 185.746 68.105 47.085 15.827 165.890 73.554 392.126 195.720 8.187 4.439 59.873 61.073 32.531 130.905 55.905 29.006 9.306 8.767 274.689 119.723 105.666 20.576 23.107 25.174 83.950 56.641 16.717 58.071 30.761 633.164 9.623 24.345 39.184 214.061 13.776 24.050 18.539 24.390 308.333 ");
					if ((argBean.getStatefrequencies() != null) && (argBean
							.getStatefrequencies()).equals("empirical")) {
							// Do not write equilibrium frequencies.
						configBuffer.append("\n");
					} else {
						configBuffer.append("e 0.079066 0.012937 0.053052 0.071586 0.042302 0.057337 0.022355 0.062157 0.0646 0.099081 0.022951 0.041977 0.04404 0.040767 0.055941 0.061197 0.053287 0.069147 0.012066 0.034155\n");
					}
					configBuffer.append("ratematrix = fixed\n");
				} else if ((argBean.getRatematrix()).equals("mtart")) {
					// writeAARateMatrix("mtart");
					configBuffer.append("parametervaluestring = r 254 1 0.2 13 200 0.2 26 0.2 4 121 0.2 49 0.2 0.2 673 244 340 0.2 1 11 0.2 184 81 12 63 0.2 79 312 98 0.2 0.2 36 664 183 350 22 72 862 0.2 12 0.2 7 2 1 0.2 500 0.2 0.2 4 44 0.2 0.2 0.2 0.2 0.2 44 15 7 106 2 0.2 183 8 262 0.2 31 43 14 11 8 1 14 118 11 263 322 20 15 0.2 5 36 14 52 54 792 0.2 3 0.2 1 56 121 0.2 3 0.2 226 0.2 3 2 9 0.2 0.2 6 0.2 180 1 314 41 11 19 0.2 0.2 191 3 515 515 21 0.2 11 2 7 204 1855 0.2 12 4 106 467 17 349 209 144 70 26 16 117 885 13 12 16 2 8 48 85 21 20 79 5 67 5 112 289 281 71 71 17 262 0.2 398 166 23 8 251 39 0.2 87 47 32 0.2 18 154 52 44 0.2 7 87 3 0.2 0.2 0.2 4 660 61 2 30 544 0.2 46 0.2 2 38 ");
					if ((argBean.getStatefrequencies() != null) && (argBean
							.getStatefrequencies()).equals("empirical")) {
							// Do not write equilibrium frequencies.
						configBuffer.append("\n");
					} else {
						configBuffer.append("e 0.054116 0.009709 0.02016 0.024289 0.088668 0.068183 0.024518 0.092638 0.021718 0.148658 0.061453 0.039903 0.041826 0.018781 0.018227 0.09103 0.049194 0.0577 0.029786 0.039443\n");
					}
					configBuffer.append("ratematrix = fixed\n");
				} else if((argBean.getRatematrix()).equals("dcmut")) {
					// writeAARateMatrix("dcmut");
					configBuffer.append("parametervaluestring = r 0.360016 1.199805 1.961167 0.183641 2.386111 0.228116 0.653416 0.258635 0.406431 0.71784 0.984474 2.48592 0.887753 0.267828 4.05187 3.680365 2.059564 0 0.244139 0 0 0 0.107278 0.282729 0.438074 0 0 0 0 0.18755 0 0.232374 1.598356 0.162366 0.484678 0 0.953164 11.388659 0 1.240981 0.868241 0.239248 0.716913 0 0 8.931515 0.13394 1.348551 0 0.956097 0.66093 0.178316 0 0 0 0.811907 0.439469 0.609526 0.830078 0.11288 0.304803 1.493409 0.507003 7.086022 0 0.793999 0.340156 0.36725 0 0.214717 0.153478 0.475927 1.951951 0 1.56516 0.92186 0.138503 0.110506 0 0.136906 0.459901 0.136655 0.123606 0.762354 6.952629 0.106802 0 0.267683 0.071514 0.170372 1.385352 0.347153 0.281581 0.087791 2.322243 0.306662 0.538165 0 0 0.076981 0.270475 0.443504 0 5.290024 0.933709 6.011613 2.383148 0.353643 0.226333 0.438715 0.270564 1.2654 0.460857 2.556685 3.332732 0.768024 0.119152 0.180393 0.632629 0.247955 1.900739 8.810038 0 0.374834 0.180629 2.411739 3.148371 0.335419 1.519078 4.610124 0.954557 1.350599 0.10385 0 0.132142 5.230115 0.341113 0.316258 0.730772 0.154924 0.171432 0.33109 1.745156 0.461776 0.286572 0 0.170205 1.127499 0.896321 0.619951 1.031534 2.565955 0 0 0.419244 1.028509 0.327059 4.885892 2.271697 0.158067 0.224968 0.94694 1.526188 1.028313 2.427202 0.782857 0.485026 0 0 2.439939 0.561828 0.525651 0.346983 0 0 1.53159 0.265745 0.240368 2.001375 0.078012 5.436674 0.303836 0.740819 0.336289 1.561997 0 0.417839 0 0.279379 0.60807 ");
					if ((argBean.getStatefrequencies() != null) && (argBean
							.getStatefrequencies()).equals("empirical")) {
							// Do not write equilibrium frequencies.
						configBuffer.append("\n");
					} else {
						configBuffer.append("e 0.087127 0.033474 0.046872 0.04953 0.039772 0.088612 0.033619 0.036886 0.080481 0.085357 0.014753 0.040432 0.05068 0.038255 0.040904 0.069577 0.058542 0.064718 0.010494 0.029916\n");
					}
					configBuffer.append("ratematrix = fixed\n");
				} else if ((argBean.getRatematrix()).equals("cprev")) {
					// writeAARateMatrix("cprev");
					configBuffer.append("parametervaluestring = r 669 175 499 68 665 66 145 236 197 185 227 490 157 105 2440 1340 968 14 56 10 10 726 303 441 280 48 396 159 538 285 10 823 2331 576 592 435 1466 3691 22 431 331 10 412 10 47 4435 170 400 43 590 266 75 18 281 145 379 162 148 2629 82 113 1055 185 3122 152 568 369 200 63 142 25 127 454 72 1268 327 97 43 10 53 487 148 317 468 2370 19 40 263 20 21 653 28 133 243 691 92 91 82 10 29 305 66 10 1405 152 1269 715 303 32 25 69 1971 345 1745 1772 168 117 92 136 216 1040 4797 42 89 218 193 2430 302 3313 4482 868 918 249 10 247 1351 113 219 286 203 516 156 865 159 189 61 100 202 125 93 645 475 86 215 173 768 357 2085 1393 83 40 754 323 87 1202 260 122 49 97 1745 396 241 54 53 391 385 314 92 230 323 2151 167 73 522 760 29 71 10 119 346 ");
					if ((argBean.getStatefrequencies() != null) && (argBean
							.getStatefrequencies()).equals("empirical")) {
							// Do not write equilibrium frequencies.
						configBuffer.append("\n");
					} else {
						configBuffer.append("e 0.0755 0.0091 0.0371 0.0495 0.0506 0.0838 0.0246 0.0806 0.0504 0.1011 0.022 0.041 0.0431 0.0382 0.0621 0.0622 0.0543 0.066 0.0181 0.0307\n");
					}
					configBuffer.append("ratematrix = fixed\n");
				} else {
					configBuffer.append("ratematrix = "
							+ argBean.getRatematrix() + "\n");
				}
			} else {
				configBuffer.append("ratematrix = 6rate\n");
			}

			if (argBean.getStatefrequencies() != null) {
				configBuffer.append("statefrequencies = "
						+ argBean.getStatefrequencies() + "\n");
			} else {
				configBuffer.append("statefrequencies = estimate\n");
			}

			if (argBean.getRatehetmodel() != null) {
				configBuffer.append("ratehetmodel = "
						+ argBean.getRatehetmodel() + "\n");
			} else {
				configBuffer.append("ratehetmodel = gamma\n");
			}

			if (argBean.getRatehetmodel() != null) {
				if ((argBean.getRatehetmodel()).equals("none")) {
					configBuffer.append("numratecats = 1\n");
				} else {
					if (argBean.getNumratecats() != null) {
						configBuffer.append("numratecats = "
								+ argBean.getNumratecats() + "\n");
					} else {
						configBuffer.append("numratecats = 4\n");
					}
				}
			} else {
				if (argBean.getNumratecats() != null) {
					configBuffer.append("numratecats = "
							+ argBean.getNumratecats() + "\n");
				} else {
					configBuffer.append("numratecats = 4\n");
				}
			}

			if (argBean.getInvariantsites() != null) {
				configBuffer.append("invariantsites = "
						+ argBean.getInvariantsites() + "\n");
			} else {
				configBuffer.append("invariantsites = estimate\n\n");
			}

			if (argBean.getGeneticcode() != null) {
				configBuffer.append("geneticcode = " + argBean.getGeneticcode()
						+ "\n");
			}
		}

		configBuffer.append("[master]\n");

		if (argBean.getNindivs() != null) {
			configBuffer.append("nindivs = " + argBean.getNindivs() + "\n");
		} else {
			configBuffer.append("nindivs = 4\n");
		}

		configBuffer.append("holdover = 1\n");

		if (argBean.getSelectionintensity() != null) {
			configBuffer.append("selectionintensity = "
					+ argBean.getSelectionintensity() + "\n");
		} else {
			configBuffer.append("selectionintensity = 0.5\n");
		}

		configBuffer.append("holdoverpenalty = 0\n");
		configBuffer.append("stopgen = 5000000\n");
		configBuffer.append("stoptime = 5000000\n");

		if (argBean.getStartoptprec() != null) {
			configBuffer.append("startoptprec = " + argBean.getStartoptprec()
					+ "\n");
		} else {
			configBuffer.append("startoptprec = 0.5\n");
		}

		if (argBean.getMinoptprec() != null) {
			configBuffer.append("minoptprec = " + argBean.getMinoptprec()
					+ "\n");
		} else {
			configBuffer.append("minoptprec = 0.01\n");
		}

		if (argBean.getNumberofprecreductions() != null) {
			configBuffer.append("numberofprecreductions = " + argBean.getNumberofprecreductions() + "\n");
		} else {
			if ((argBean.getAnalysistype() != null) &&
					argBean.getAnalysistype().equalsIgnoreCase("bootstrap")) {
				/*
				 * Derrick's recommendation is that most of the time for BS runs
				 * this could be reduced to 1, but that for several hundred taxa
				 * set it to 2-5, so setting it conservatively for now.
				 */
				configBuffer.append("numberofprecreductions = 5\n");
			} else {
				configBuffer.append("numberofprecreductions = 10\n");
			}
		}

		if (argBean.getTreerejectionthreshold() != null) {
			configBuffer.append("treerejectionthreshold = "
					+ argBean.getTreerejectionthreshold() + "\n");
		} else {
			configBuffer.append("treerejectionthreshold = 50.0\n");
		}

		if (argBean.getTopoweight() != null) {
			configBuffer.append("topoweight = " + argBean.getTopoweight()
					+ "\n");
		} else {
			configBuffer.append("topoweight = 1.0\n");
		}

		if (argBean.getModweight() != null) {
			configBuffer.append("modweight = " + argBean.getModweight() + "\n");
		} else {
			configBuffer.append("modweight = 0.05\n");
		}

		if (argBean.getBrlenweight() != null) {
			configBuffer.append("brlenweight = " + argBean.getBrlenweight()
					+ "\n");
		} else {
			configBuffer.append("brlenweight = 0.2\n");
		}

		if (argBean.getRandnniweight() != null) {
			configBuffer.append("randnniweight = " + argBean.getRandnniweight()
					+ "\n");
		} else {
			configBuffer.append("randnniweight = 0.1\n");
		}

		if (argBean.getRandsprweight() != null) {
			configBuffer.append("randsprweight = " + argBean.getRandsprweight()
					+ "\n");
		} else {
			configBuffer.append("randsprweight = 0.3\n");
		}

		if (argBean.getLimsprweight() != null) {
			configBuffer.append("limsprweight = " + argBean.getLimsprweight()
					+ "\n");
		} else {
			configBuffer.append("limsprweight = 0.6\n");
		}

		configBuffer.append("intervallength = 100\n");
		configBuffer.append("intervalstostore = 5\n");

		if (argBean.getLimsprrange() != null) {
			configBuffer.append("limsprrange = " + argBean.getLimsprrange()
					+ "\n");
		} else {
			configBuffer.append("limsprrange = 6\n");
		}

		if (argBean.getMeanbrlenmuts() != null) {
			configBuffer.append("meanbrlenmuts = " + argBean.getMeanbrlenmuts()
					+ "\n");
		} else {
			configBuffer.append("meanbrlenmuts = 5\n");
		}

		if (argBean.getGammashapebrlen() != null) {
			configBuffer.append("gammashapebrlen = "
					+ argBean.getGammashapebrlen() + "\n");
		} else {
			configBuffer.append("gammashapebrlen = 1000\n");
		}

		if (argBean.getGammashapemodel() != null) {
			configBuffer.append("gammashapemodel = "
					+ argBean.getGammashapemodel() + "\n");
		} else {
			configBuffer.append("gammashapemodel = 1000\n");
		}

		if (argBean.getUniqueswapbias() != null) {
			configBuffer.append("uniqueswapbias = "
					+ argBean.getUniqueswapbias() + "\n");
		} else {
			configBuffer.append("uniqueswapbias = 0.1\n");
		}

		if (argBean.getDistanceswapbias() != null) {
			configBuffer.append("distanceswapbias = "
					+ argBean.getDistanceswapbias() + "\n");
		} else {
			configBuffer.append("distanceswapbias = 1.0\n\n");
		}

		if (argBean.getAnalysistype() != null) {
			if (argBean.getAnalysistype().equalsIgnoreCase("bootstrap")) {
				if ((argBean.getSearchreps() != null)
						&& (argBean.getSearchreps() > 0)){
					configBuffer.append("bootstrapreps = "
						+ argBean.getSearchreps() + "\n");
				} else {
					configBuffer.append("bootstrapreps = 1\n");
				}
			} else {
				configBuffer.append("bootstrapreps = 0\n");
			}
		} else {
			configBuffer.append("bootstrapreps = 0\n");
		}

		if (argBean.getResampleproportion() != null) {
			configBuffer.append("resampleproportion = "
					+ argBean.getResampleproportion() + "\n");
		} else {
			configBuffer.append("resampleproportion = 1.0\n");
		}

		if (argBean.getInferinternalstateprobs() != null) {
			if ((argBean.getInferinternalstateprobs()).booleanValue() == true) {
				configBuffer.append("inferinternalstateprobs = 1\n");
			} else {
				configBuffer.append("inferinternalstateprobs = 0\n");
			}
		} else {
			configBuffer.append("inferinternalstateprobs = 0\n");
		}

		argBean.setConfigFile("garli.conf");
		this.configFileName = "garli.conf";
		// Add to input files (shared files, really) here.
		inputFiles.add(configFileName);

		if (argBean.getPartitionsdata() != null) {
			writePartitionsData();
		}

		writeConfig();
	}

	/**
	 * Write out config file to disk.
	 */
	protected void writeConfig() throws Exception {
		// Write out the config file to the current working directory.
		BufferedWriter writer =
				new BufferedWriter(new FileWriter(this.configFileName));
		writer.write(configBuffer.toString());
		writer.close();
	}

	/**
	 * Write NEXUS sets block to the data file.
	 */
	private void writePartitionsData() throws Exception {
		// First we need to remove any existing sets blocks from the file.
		String line;
		BufferedReader br = new BufferedReader(
				new FileReader(argBean.getDatafname()));
		BufferedWriter bw = new BufferedWriter(
				new FileWriter(argBean.getDatafname() + "_temp"));
		int writeline = 1;

		while ((line = br.readLine()) != null) {
			if (line.toLowerCase().contains("begin sets".toLowerCase())) {
				writeline = 0;
			} 

			if (writeline == 1) {
				bw.write(line);
				bw.newLine();
			}

			if ((writeline == 0)
					&& line.toLowerCase().contains("end;".toLowerCase())) {
				writeline = 1;
			}
		}
		br.close();
		bw.close();

		// Adds the partitions data to the datafname file after decoding it.
		bw = new BufferedWriter(new FileWriter(argBean.getDatafname() + "_temp",
				true));  // Extra parameter sets append mode.
		bw.write(Base64Coder.decodeString(argBean.getPartitionsdata()));
		bw.close();

		// now read from the temp file and write to original file
		br = new BufferedReader(
				new FileReader(argBean.getDatafname() + "_temp"));
		bw = new BufferedWriter(new FileWriter(argBean.getDatafname()));

		while ((line = br.readLine()) != null) {
			bw.write(line);
			bw.newLine();
		}
		br.close();
		bw.close();

		(new File(argBean.getDatafname() + "_temp")).delete();
	}

	/**
	 * Write AA rate matrix to the data file.
	 */
	private void writeAARateMatrix(String matrixtype) throws Exception {
		// First we need to remove any existing garli blocks from the file.
		String line;
		BufferedReader br = new BufferedReader(
				new FileReader(argBean.getDatafname()));
		BufferedWriter bw = new BufferedWriter(
				new FileWriter(argBean.getDatafname() + "_temp"));
		int writeline = 1;

		while ((line = br.readLine()) != null) {
			if (line.toLowerCase().contains("begin garli".toLowerCase())) {
				writeline = 0;
			} 

			if (writeline == 1) {
				bw.write(line);
				bw.newLine();
			}

			if ((writeline == 0)
					&& line.toLowerCase().contains("end;".toLowerCase())) {
				writeline = 1;
			}
		}
		br.close();
		bw.close();

		bw = new BufferedWriter(new FileWriter(argBean.getDatafname() + "_temp",
				true));  // Extra parameter sets append mode.
		bw.write("begin garli;");
		bw.newLine();

		// Add the appropriate matrix.
		if (matrixtype.equals("lg")) {
			bw.write("r 243.500 38.656 101.598 24.819 202.114 35.106 14.657 52.486 38.675 109.961 27.080 115.206 94.882 41.586 462.446 209.301 249.250 17.679 21.420 6.120 0.342 108.123 55.689 62.662 31.366 1.298 58.110 87.426 51.728 7.374 8.297 52.294 272.397 111.863 191.672 65.557 114.020 512.992 1.704 82.657 90.697 1.046 27.681 1.475 2.499 496.584 38.588 51.201 12.126 121.332 41.661 3.714 2.924 13.217 1.840 34.127 41.467 4.330 176.791 6.816 16.996 52.994 41.030 403.888 35.606 59.867 59.141 23.971 7.616 11.743 8.764 66.732 108.855 2.340 253.635 175.976 8.758 9.241 3.508 5.158 35.396 16.142 64.046 240.373 763.432 30.472 0.852 29.019 4.330 13.651 140.640 19.268 26.214 38.171 170.218 12.701 7.503 26.266 5.349 10.652 68.211 35.836 43.286 441.125 49.779 470.891 237.387 96.850 57.157 11.643 58.408 519.152 15.561 405.499 418.074 18.734 7.658 7.127 12.423 6.271 101.128 1041.770 10.923 22.747 13.451 64.234 209.847 38.184 316.401 618.860 73.241 111.216 18.118 4.882 12.907 617.519 6.694 24.365 56.980 29.529 17.833 29.635 166.574 60.617 29.314 36.294 9.768 163.622 47.361 33.942 197.646 185.746 68.105 47.085 15.827 165.890 73.554 392.126 195.720 8.187 4.439 59.873 61.073 32.531 130.905 55.905 29.006 9.306 8.767 274.689 119.723 105.666 20.576 23.107 25.174 83.950 56.641 16.717 58.071 30.761 633.164 9.623 24.345 39.184 214.061 13.776 24.050 18.539 24.390 308.333 ;");
			bw.newLine();
			if ((argBean.getStatefrequencies() != null)
					&& (argBean.getStatefrequencies()).equals("empirical")) {
					// Comment out the equilibrium frequencies.
				bw.write("[e 0.079066 0.012937 0.053052 0.071586 0.042302 0.057337 0.022355 0.062157 0.0646 0.099081 0.022951 0.041977 0.04404 0.040767 0.055941 0.061197 0.053287 0.069147 0.012066 0.034155]");
				bw.newLine();
				bw.write(";");
				bw.newLine();
			} else {
				bw.write("e 0.079066 0.012937 0.053052 0.071586 0.042302 0.057337 0.022355 0.062157 0.0646 0.099081 0.022951 0.041977 0.04404 0.040767 0.055941 0.061197 0.053287 0.069147 0.012066 0.034155");
				bw.newLine();
				bw.write(";");
				bw.newLine();
			}
		} else if (matrixtype.equals("mtart")) {
			bw.write("r 254 1 0.2 13 200 0.2 26 0.2 4 121 0.2 49 0.2 0.2 673 244 340 0.2 1 11 0.2 184 81 12 63 0.2 79 312 98 0.2 0.2 36 664 183 350 22 72 862 0.2 12 0.2 7 2 1 0.2 500 0.2 0.2 4 44 0.2 0.2 0.2 0.2 0.2 44 15 7 106 2 0.2 183 8 262 0.2 31 43 14 11 8 1 14 118 11 263 322 20 15 0.2 5 36 14 52 54 792 0.2 3 0.2 1 56 121 0.2 3 0.2 226 0.2 3 2 9 0.2 0.2 6 0.2 180 1 314 41 11 19 0.2 0.2 191 3 515 515 21 0.2 11 2 7 204 1855 0.2 12 4 106 467 17 349 209 144 70 26 16 117 885 13 12 16 2 8 48 85 21 20 79 5 67 5 112 289 281 71 71 17 262 0.2 398 166 23 8 251 39 0.2 87 47 32 0.2 18 154 52 44 0.2 7 87 3 0.2 0.2 0.2 4 660 61 2 30 544 0.2 46 0.2 2 38 ;");
			bw.newLine();
			if ((argBean.getStatefrequencies() != null)
					&& (argBean.getStatefrequencies()).equals("empirical")) {
					// Comment out the equilibrium frequencies.
				bw.write("[e 0.054116 0.009709 0.02016 0.024289 0.088668 0.068183 0.024518 0.092638 0.021718 0.148658 0.061453 0.039903 0.041826 0.018781 0.018227 0.09103 0.049194 0.0577 0.029786 0.039443]");
				bw.newLine();
				bw.write(";");
				bw.newLine();
			} else {
				bw.write("e 0.054116 0.009709 0.02016 0.024289 0.088668 0.068183 0.024518 0.092638 0.021718 0.148658 0.061453 0.039903 0.041826 0.018781 0.018227 0.09103 0.049194 0.0577 0.029786 0.039443");
				bw.newLine();
				bw.write(";");
				bw.newLine();
			}
		} else if (matrixtype.equals("dcmut")) {
			bw.write("r 0.360016 1.199805 1.961167 0.183641 2.386111 0.228116 0.653416 0.258635 0.406431 0.71784 0.984474 2.48592 0.887753 0.267828 4.05187 3.680365 2.059564 0 0.244139 0 0 0 0.107278 0.282729 0.438074 0 0 0 0 0.18755 0 0.232374 1.598356 0.162366 0.484678 0 0.953164 11.388659 0 1.240981 0.868241 0.239248 0.716913 0 0 8.931515 0.13394 1.348551 0 0.956097 0.66093 0.178316 0 0 0 0.811907 0.439469 0.609526 0.830078 0.11288 0.304803 1.493409 0.507003 7.086022 0 0.793999 0.340156 0.36725 0 0.214717 0.153478 0.475927 1.951951 0 1.56516 0.92186 0.138503 0.110506 0 0.136906 0.459901 0.136655 0.123606 0.762354 6.952629 0.106802 0 0.267683 0.071514 0.170372 1.385352 0.347153 0.281581 0.087791 2.322243 0.306662 0.538165 0 0 0.076981 0.270475 0.443504 0 5.290024 0.933709 6.011613 2.383148 0.353643 0.226333 0.438715 0.270564 1.2654 0.460857 2.556685 3.332732 0.768024 0.119152 0.180393 0.632629 0.247955 1.900739 8.810038 0 0.374834 0.180629 2.411739 3.148371 0.335419 1.519078 4.610124 0.954557 1.350599 0.10385 0 0.132142 5.230115 0.341113 0.316258 0.730772 0.154924 0.171432 0.33109 1.745156 0.461776 0.286572 0 0.170205 1.127499 0.896321 0.619951 1.031534 2.565955 0 0 0.419244 1.028509 0.327059 4.885892 2.271697 0.158067 0.224968 0.94694 1.526188 1.028313 2.427202 0.782857 0.485026 0 0 2.439939 0.561828 0.525651 0.346983 0 0 1.53159 0.265745 0.240368 2.001375 0.078012 5.436674 0.303836 0.740819 0.336289 1.561997 0 0.417839 0 0.279379 0.60807 ;");
			bw.newLine();
			if ((argBean.getStatefrequencies() != null)
					&& (argBean.getStatefrequencies()).equals("empirical")) {
					// Comment out the equilibrium frequencies.
				bw.write("[e 0.087127 0.033474 0.046872 0.04953 0.039772 0.088612 0.033619 0.036886 0.080481 0.085357 0.014753 0.040432 0.05068 0.038255 0.040904 0.069577 0.058542 0.064718 0.010494 0.029916]");
				bw.newLine();
				bw.write(";");
				bw.newLine();
			} else {
				bw.write("e 0.087127 0.033474 0.046872 0.04953 0.039772 0.088612 0.033619 0.036886 0.080481 0.085357 0.014753 0.040432 0.05068 0.038255 0.040904 0.069577 0.058542 0.064718 0.010494 0.029916");
				bw.newLine();
				bw.write(";");
				bw.newLine();
			}
		} else if (matrixtype.equals("cprev")) {
			bw.write("r 669 175 499 68 665 66 145 236 197 185 227 490 157 105 2440 1340 968 14 56 10 10 726 303 441 280 48 396 159 538 285 10 823 2331 576 592 435 1466 3691 22 431 331 10 412 10 47 4435 170 400 43 590 266 75 18 281 145 379 162 148 2629 82 113 1055 185 3122 152 568 369 200 63 142 25 127 454 72 1268 327 97 43 10 53 487 148 317 468 2370 19 40 263 20 21 653 28 133 243 691 92 91 82 10 29 305 66 10 1405 152 1269 715 303 32 25 69 1971 345 1745 1772 168 117 92 136 216 1040 4797 42 89 218 193 2430 302 3313 4482 868 918 249 10 247 1351 113 219 286 203 516 156 865 159 189 61 100 202 125 93 645 475 86 215 173 768 357 2085 1393 83 40 754 323 87 1202 260 122 49 97 1745 396 241 54 53 391 385 314 92 230 323 2151 167 73 522 760 29 71 10 119 346 ;");
			bw.newLine();
			if ((argBean.getStatefrequencies() != null)
					&& (argBean.getStatefrequencies()).equals("empirical")) {
					// Comment out the equilibrium frequencies.
				bw.write("[e 0.0755 0.0091 0.0371 0.0495 0.0506 0.0838 0.0246 0.0806 0.0504 0.1011 0.022 0.041 0.0431 0.0382 0.0621 0.0622 0.0543 0.066 0.0181 0.0307]");
				bw.newLine();
				bw.write(";");
				bw.newLine();
			} else {
				bw.write("e 0.0755 0.0091 0.0371 0.0495 0.0506 0.0838 0.0246 0.0806 0.0504 0.1011 0.022 0.041 0.0431 0.0382 0.0621 0.0622 0.0543 0.066 0.0181 0.0307");
				bw.newLine();
				bw.write(";");
				bw.newLine();
			}
		}

		bw.write("end;");
		bw.newLine();
		bw.close();

		// Now read from the temp file and write to original file.
		br = new BufferedReader(
				new FileReader(argBean.getDatafname() + "_temp"));
		bw = new BufferedWriter(new FileWriter(argBean.getDatafname()));

		while ((line = br.readLine()) != null) {
			bw.write(line);
			bw.newLine();
		}
		br.close();
		bw.close();

		(new File(argBean.getDatafname() + "_temp")).delete();
	}

	/**
	 * Validate a GARLI data file by executing Garli-2.0 in "validate" mode.
	 * Also parse out memory usage information, number of unique patterns, and
	 * number of taxa.
	 */
	protected void validate() throws Exception {
		Runtime r = Runtime.getRuntime();
		String exec_me = globusLocation + "/validate_garli_conf.pl "
				+ this.configFileName;

		Process proc = r.exec(exec_me);
		String line = null;
		String outputString = null;
		InputStream stdout = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdout);
		BufferedReader br = new BufferedReader(isr);
		while ((line = br.readLine()) != null) {
			outputString = line;
		}
		br.close();

		/* Assign "min_mem", "max_mem", "unique_patterns", "num_taxa",
		 * "actual_mem", and "valid_dataf". */
		String[] chunks = outputString.split(" ", 6);
		if (chunks.length > 5){
			this.min_mem = chunks[0];
			this.max_mem = chunks[1];
			this.unique_patterns = chunks[2];
			this.num_taxa = chunks[3];
			this.actual_mem = chunks[4];

			if (chunks[5].toLowerCase().equals("true")) {
				this.valid_dataf = true;
			} else {
				this.valid_dataf = false;
			}
		} else {
			this.valid_dataf = false;
		}

		if (this.valid_dataf == false) {
			log.error("There was a problem validating the input data.  "
					+ outputString + "  Exiting...");
			System.exit(1);
		}

		try {
			Integer.parseInt(this.min_mem);
		} catch (NumberFormatException e) {
			log.error("Could not ascertain minimum memory requirement!  Exiting...");
			System.exit(1);
		}

		if (this.min_mem.equals("0")) {
			log.error("Could not ascertain minimum memory requirement!  Exiting...");
			System.exit(1);
		}

		try {
			Integer.parseInt(this.max_mem);
		} catch (NumberFormatException e) {
			log.error("Could not ascertain maximum memory requirement!  Exiting...");
			System.exit(1);
		}

		if (this.max_mem.equals("0")) {
			log.error("Could not ascertain maximum memory requirement!  Exiting...");
			System.exit(1);
		}

		try {
			Integer.parseInt(this.actual_mem);
		} catch (NumberFormatException e) {
			log.error("Could not ascertain actual memory requirement!  Exiting...");
			System.exit(1);
		}

		if (this.actual_mem.equals("0")) {
			log.error("Could not ascertain actual memory requirement!  Exiting...");
			System.exit(1);
		}

		try {
			Integer.parseInt(this.unique_patterns);
		} catch (NumberFormatException e) {
			log.error("Could not ascertain number of unique patterns!  Exiting...");
			System.exit(1);
		}

		if (this.unique_patterns.equals("0")) {
			log.error("Could not ascertain number of unique patterns!  Exiting...");
			System.exit(1);
		}

		try {
			Integer.parseInt(this.num_taxa);
		} catch (NumberFormatException e) {
			log.error("Could not ascertain number of taxa!  Exiting...");
			System.exit(1);
		}

		if (this.num_taxa.equals("0")) {
			log.error("Could not ascertain number of taxa!  Exiting...");
			System.exit(1);
		}
	}

	/**
	 * Parse the config file, setting the "inputFiles" and "outputFiles" vectors
	 * as we go.
	 */
	protected void parse() throws Exception {

		/* The "ofprefix" variable in the config file will determine the first
		 * portion of a lot of the output files. */
		String ofprefix = "";
		String datatype = "";
		boolean outputphyliptree = false;
		boolean outputeachbettertopology = false;
		boolean outputmostlyuselessfiles = false;
		boolean outputsitelikes = false;
		boolean bootstrapreps = false;
		boolean inferinternalstateprobs = false;
		String streefname = "";
		boolean optimizeinputonly = false;
		String constraintfile = "";  /* If this is set to something other than
				none, add it to the "inputFiles" vector. */
		boolean workphasedivision = false; // this should only be used with BOINC
		boolean writecheckpoints = false; // supporting only with workphase division for use with BOINC
		boolean restart = false;  // Not supporting at the moment.
		boolean ratehetmodel = false;  /* None, gamma, gammafixed. false for
				none, true for gamma or gammafixed. */
		String numratecats = "";  /* 1 to 20, must be set to 1 if ratehetmodel
				is set to none. */
		boolean uniqueswapbias = false;  /* 0.01 to 1.0. (If < 1.0 and
				"outputmostlyuselessfiles" is on then "swap.log" is output.) */
				// true if < 1.0, false if = 1.0.
		String ratematrix = "";  /* If set to "estimate", "AArmatrix.dat" is
				output. */
		Double usb;
		Integer logevery = new Integer(1);
		Integer saveevery = new Integer(1);

		try {
			BufferedReader instream =
					new BufferedReader(new FileReader(configFileName));
			String line;

			while ((line = instream.readLine()) != null) {

				String [] splitLine = line.split("=");

				for (int i = 0; i < splitLine.length; i++) {
					splitLine[i] = splitLine[i].trim();
				}

				String garliVar = splitLine[0];
				String garliVarVal = "";
				if (splitLine.length > 1) {
					garliVarVal = splitLine[1];
				}

				if (garliVar.equalsIgnoreCase("DATAFNAME")) {
					/* Check to see if the datafile specified in the config file
							is a path; we are no longer allowing paths! */
					String fullDataFileName = garliVarVal;
					String [] splitPath = fullDataFileName.split("/");

					if (splitPath.length > 1) {
						log.error("We are not currently supporting paths to data files; please fix your garli.conf file and try again.");
						System.exit(1);
					}

					/* Check to see if the datafile specified exists; only
					perform this action on the client. */
					File testDataFile = new File(fullDataFileName);
					/* No more client/server side. */
					// if (myWorkingDir.equals("")) {
					if (!testDataFile.exists()) {
						log.error("Data file " + testDataFile.toString()
								+ " does not exist!");
						System.exit(1);
					}
					// }

					// Add the data file.
					inputFiles.add(fullDataFileName);
				}

				if (garliVar.equalsIgnoreCase("RANDSEED")) {
					// In the latest version of GARLI, 0 is not allowed.
					if (garliVarVal.equals("0")) {
						log.error("randseed = 0 is not allowed!\nModify your garli.conf file and try again!");
						System.exit(1);
					}
				}

				if (garliVar.equalsIgnoreCase("datatype")) {
					datatype = garliVarVal;
					if (argBean.getDatatype() == null) {
						argBean.setDatatype(garliVarVal);
					}
				}
				if (garliVar.equalsIgnoreCase("statefrequencies")) {
					if (argBean.getStatefrequencies() == null) {
						argBean.setStatefrequencies(garliVarVal);
					}
				}
				if (garliVar.equalsIgnoreCase("invariantsites")) {
					if (argBean.getInvariantsites() == null) {
						argBean.setInvariantsites(garliVarVal);
					}
				}
				if (garliVar.equalsIgnoreCase("OFPREFIX")) {
					ofprefix = garliVarVal;
				}
				if (garliVar.equalsIgnoreCase("OUTPUTPHYLIPTREE")
						&& garliVarVal.equals("1")) {
					outputphyliptree = true;
				}
				if (garliVar.equalsIgnoreCase("outputeachbettertopology")
						&& garliVarVal.equals("1")) {
					outputeachbettertopology = true;
				}
				if (garliVar.equalsIgnoreCase("outputmostlyuselessfiles")
						&& garliVarVal.equals("1")) {
					outputmostlyuselessfiles = true;
				}
				if (garliVar.equalsIgnoreCase("outputsitelikelihoods")
						&& garliVarVal.equals("1")) {
					outputsitelikes = true;
				}
				if (garliVar.equalsIgnoreCase("bootstrapreps")
						&& !garliVarVal.equals("0")) {
					bootstrapreplicates = new Integer(garliVarVal);
					bootstrapreps = true;
				}
				if (garliVar.equalsIgnoreCase("inferinternalstateprobs")
						&& garliVarVal.equals("1")) {
					inferinternalstateprobs = true;
				}
				if (garliVar.equalsIgnoreCase("optimizeinputonly")
						&& garliVarVal.equals("1")) {
					optimizeinputonly = true;
				}

				if (garliVar.equalsIgnoreCase("availablememory")) {

					int specified_mem = (new Integer(garliVarVal)).intValue();

					if (this.validated_input) {
						int min_mem_int = (new Integer(min_mem)).intValue();
						int max_mem_int = (new Integer(max_mem)).intValue();

						if (built_config) {  /* if we built the config file, we
								must set the memory ourselves. */
							// Determine how much memory to use.
							if (min_mem_int > max_sys_mem) {
								log.error("Data file requires greater than "
										+ (new Integer(max_sys_mem)).toString()
										+ "M of memory and cannot be processed.\n");
								System.exit(1);
							}
							if (min_mem_int > med_sys_mem) {  /* Use the minimum
									amount of memory required. */
								log.debug("setting avail_mem to: " + min_mem);
								// Adding a small buffer here.
								avail_mem = (new Integer(min_mem_int + 100))
										.toString();
								// avail_mem = min_mem;
							} else if (max_mem_int > med_sys_mem) {
								log.debug("setting avail_mem to: "
										+ (new Integer(med_sys_mem))
										.toString());
								avail_mem = (new Integer(med_sys_mem))
										.toString();
							} else {
								log.debug("setting avail_mem to: " + max_mem);
								avail_mem = max_mem;
							}

							int avail_begin_index =
									configBuffer.indexOf("availablememory");
							int avail_end_index = configBuffer.indexOf("\n",
									avail_begin_index);
							configBuffer = configBuffer.replace(
									avail_begin_index, avail_end_index,
									"availablememory = " + avail_mem + "\n");

							// Write config out again.
							writeConfig();
						} else {  /* Verify the memory specified is within legal
								bounds. */
							if (specified_mem > max_sys_mem) {
								log.error("Config file specifies greater than "
										+ (new Integer(max_sys_mem)).toString()
										+ "M of memory and cannot be processed.\n");
								System.exit(1);
							} else if (specified_mem < min_mem_int) {
								log.error("Config file specifies less than the minimum memory requirement of "
										+ min_mem
										+ "M of memory and cannot be processed.\n");
								System.exit(1);
							} else {
								log.debug("setting avail_mem to: "
										+ garliVarVal);
								avail_mem = garliVarVal;
							}
						}
					} else {
						log.debug("setting avail_mem to: " + garliVarVal);
						avail_mem = garliVarVal;
					}
				}

				// v0.951 settings.
				if (garliVar.equalsIgnoreCase("streefname")) {

					streefname = garliVarVal;

					if (!garliVarVal.equals("random")
							&& !garliVarVal.equals("stepwise")) {

						/* Check to see if the treefile specified in the config
						file is a path; we are no longer allowing paths! */
						String fullTreeFileName = garliVarVal;
						String [] splitPath = fullTreeFileName.split("/");

						if (splitPath.length > 1) {
							log.error("We are not currently supporting paths to tree files; please fix your garli.conf file and try again.");
							System.exit(1);
						}

						/* Check to see if the treefile specified exists; only
						perform this action on the client. */
						File testTreeFile = new File(fullTreeFileName);
						// if (myWorkingDir.equals("")) {
						if (!testTreeFile.exists()) {
							log.error("Tree file " + testTreeFile.toString()
									+ " does not exist!");
							System.exit(1);
						}
						// }
						inputFiles.add(fullTreeFileName);
					}
				}
				if (garliVar.equalsIgnoreCase("constraintfile")
						&& !garliVarVal.equals("none")) {
					/* Check to see if the constraint file specified in the
					config file is a path; we are no longer allowing paths! */
					String fullConstraintFileName = garliVarVal;
					String [] splitPath = fullConstraintFileName.split("/");

					if (splitPath.length > 1) {
						log.error("We are not currently supporting paths to constraint files; please fix your garli.conf file and try again.");
						System.exit(1);
					}


					/* Check to see if the constraint file specified exists;
					only perform this action on the client. */
					File testConstraintFile = new File(fullConstraintFileName);
					// if (myWorkingDir.equals("")) {
					if (!testConstraintFile.exists()) {
						log.error("Constraint file "
								+ testConstraintFile.toString()
								+ " does not exist!");
						System.exit(1);
					}
					// }
					inputFiles.add(fullConstraintFileName);
				}
				if(garliVar.equalsIgnoreCase("workphasedivision") && garliVarVal.equals("1")) {
		    		workphasedivision = true;
				}
				if (garliVar.equalsIgnoreCase("writecheckpoints")
						&& garliVarVal.equals("1")) {
					writecheckpoints = true;
				}
				if (garliVar.equalsIgnoreCase("restart")
						&& garliVarVal.equals("1")) {
					restart = true;
				}
				if (garliVar.equalsIgnoreCase("ratehetmodel")) {
					if (argBean.getRatehetmodel() == null) {
						argBean.setRatehetmodel(garliVarVal);
					}
					if (!garliVarVal.equals("none")) {
						ratehetmodel = true;
					}
				}
				if (garliVar.equalsIgnoreCase("numratecats")) {
					if (argBean.getNumratecats() == null) {
						argBean.setNumratecats(Integer.valueOf(garliVarVal));
					}
					numratecats = garliVarVal;
				}
				if (garliVar.equalsIgnoreCase("uniqueswapbias")) {
					usb = new Double (garliVarVal);
					if (usb.doubleValue() < 1.000000) {
						uniqueswapbias = true;
					}
				}
				if (garliVar.equalsIgnoreCase("ratematrix")) {
					if (argBean.getRatematrix() == null) {
						argBean.setRatematrix(garliVarVal);
					}
					if (garliVarVal.equalsIgnoreCase("estimate")) {
						ratematrix = "estimate";
					}
				}
				if (garliVar.equalsIgnoreCase("searchreps")) {
					searchreps = new Integer (garliVarVal);
					if (searchreps.intValue() < 1) {
						log.error("searchreps must be >= 1!  fix your garli.conf file and try again.\n");
						System.exit(1);
					}
				}
				if (garliVar.equalsIgnoreCase("logevery")) {
					logevery = new Integer (garliVarVal);
					/* if (logevery.intValue() < 10000) {
						log.error("logevery must be >= 10000!  fix your garli.conf file and try again.\n");
						System.exit(1);
					} */
				}
				if (garliVar.equalsIgnoreCase("saveevery")) {
					saveevery = new Integer (garliVarVal);
					/* if (saveevery.intValue() < 10000) {
						log.error("saveevery must be >= 10000!  fix your garli.conf file and try again.\n");
						System.exit(1);
					} */
				}		    
			}
		} catch (FileNotFoundException fe) {
			log.error("1)datafname specified does not exist or");
			log.error("2)configFile: " + configFileName + " is not found\n"
					+ fe);
			throw fe;
		} catch (IOException ie) {
			log.error("IOException reading configFile " + configFileName + ": "
					+ ie);
		}

		// Ensure starting tree was provided if optimizeinputonly = 1.
		if (optimizeinputonly && (streefname.equals("random")
				|| streefname.equals("stepwise") || streefname.equals(""))) {
			log.error("optimizeinputonly requires a starting tree to be provided!");
			throw new Exception("optimizeinputonly requires a starting tree to be provided!");
		}

		// Now that we definitely have the 'ofprefix', add the output files.
		if (!optimizeinputonly) {
			if (bootstrapreps) {
				outputFiles.add(ofprefix + ".boot.tre");
				log.debug("bootstrap only");
			} else {
				outputFiles.add(ofprefix + ".best.tre");
				log.debug("no bootstrap");
			}
		}

		if (!optimizeinputonly) {
			if (searchreps.intValue() > 1) {
				if (!bootstrapreps) {
					outputFiles.add(ofprefix + ".best.all.tre");
					if (outputphyliptree) {
						outputFiles.add(ofprefix + ".best.all.phy");
					}
				}
			}
		} else {
			outputFiles.add(ofprefix + ".best.all.tre");
			if (outputphyliptree) {
				outputFiles.add(ofprefix + ".best.all.phy");
			}
		}

		outputFiles.add(ofprefix + ".screen.log");  // Universally present.

		if (!optimizeinputonly) {
			outputFiles.add(ofprefix + ".log00.log");
		}

		if (!optimizeinputonly) {
			if (outputphyliptree && !bootstrapreps) {
				outputFiles.add(ofprefix + ".best.phy");
			} else if (outputphyliptree && bootstrapreps) {
				outputFiles.add(ofprefix + ".boot.phy");
			}
		}

		if (!optimizeinputonly) {
			if (outputeachbettertopology && !bootstrapreps) {
				if (searchreps.intValue() == 1) {
					// Changed from .log to .tre in 0.96b6.r226.
					outputFiles.add(ofprefix + ".treelog00.tre");
					// Also, treelogs turned off for bootstrap runs in same.
				} else {
					for (int i = 1; i <= searchreps.intValue(); i++) {
						outputFiles.add(ofprefix + ".rep" + i
								+ ".treelog00.tre");
					}
				}
			}
		}

		if (outputmostlyuselessfiles && !optimizeinputonly) {
			outputFiles.add(ofprefix + ".fate00.log");
			outputFiles.add(ofprefix + ".problog00.log");
		}

		if (outputsitelikes || optimizeinputonly) {
			outputFiles.add(ofprefix + ".sitelikes.log");
		}

		if (inferinternalstateprobs && !optimizeinputonly) {
			outputFiles.add(ofprefix + ".internalstates.log");
		}

		if (!optimizeinputonly) {
			if (ratematrix.equals("estimate")
					&& (datatype.equalsIgnoreCase("aminoacid")
							|| datatype.equalsIgnoreCase("codon-aminoacid"))
					&& !bootstrapreps) {
				outputFiles.add(ofprefix + ".AArmatrix.dat");
			}
		}

		if(restart) {
	    	log.error("We are not currently supporting restarting from a GARLI checkpoint, please fix your garli.conf file.");
	    	throw new Exception("We are not currently supporting restarting from a GARLI checkpoint, please fix your garli.conf file.");
		}
	
		if(workphasedivision ^ writecheckpoints) {
	    	log.error("workphasedivision can not be 1 if writecheckpoints is 0 (and vice versa...)");
	    	throw new Exception("workphasedivision can not be 1 if writecheckpoints is 0 (and vice versa...)");
		}

		if (!numratecats.equals("1") && !ratehetmodel) {
			log.error("numratecats must be set to 1 (not " + numratecats
					+ " or anything else above 1) if ratehetmodel is set to none; please fix your garli.conf file.");
			throw new Exception("numratecats must be set to 1 (not "
					+ numratecats
					+ " or anything else above 1) if ratehetmodel is set to none; please fix your garli.conf file.");
		}

		if (!optimizeinputonly) {
			if (uniqueswapbias && outputmostlyuselessfiles) {
				outputFiles.add(ofprefix + ".swaplog00.log");
			}
		}
	}

	/**
	 * Overwrites the input file with a modified version.
	 * Should only be used on the server side!
	 *
	 * Currently this method doesn't not support rewriting all the config files
	 * in a hetero job batch, but it should be changed to!
	 */
	public void rewriteConfigFile() throws FileNotFoundException, IOException
	{
		try {
			// log.debug("Writing temp input file: " + filename + "_deleteme");

			BufferedWriter writer = new BufferedWriter(
					new FileWriter(configFileName + "_deleteme"));
			writer.write(configBuffer.toString());
			writer.close();

			// log.debug("About to move temp file..");

			// Overwrite input file with modified version.
			File newinput = new File(configFileName + "_deleteme");
			boolean didRename = false;
			didRename = newinput.renameTo(new File(configFileName));
			if (didRename == false) {
				log.error("Could not overwrite config file!");
				throw new FileNotFoundException("Could not find file: "
						+ configFileName + "_deleteme");
			}

		} catch (FileNotFoundException fe) {
			log.error("Config file not found while writing new config file: "
					+ fe);
			throw fe;
		} catch (IOException ie) {
			log.error("IOException while writing new config file: " + ie);
			throw ie;
		}
	}

	/**
	 * Returns a String array containing the input filenames.
	 */
	public Collection getInputFiles() {
		ArrayList<String> inFiles = new ArrayList<String>();
		for (int i = 0; i < inputFiles.size(); i++) {
			inFiles.add((String)inputFiles.elementAt(i));
		}
		return inFiles;
	}

	/**
	 * Returns a String array containing the output filenames.
	 */
	public String[] getOutputFiles() {
		String [] outFiles = new String[outputFiles.size()];
		for (int i = 0; i < outputFiles.size(); i++) {
			outFiles[i] = new File((String) outputFiles.elementAt(i)).getName();
		}
		return outFiles;
	}

	public String getAvailMem() {
		return avail_mem;
	}

	public void setAvailMem(String mem) {
		avail_mem = mem;
	}

	public String getActualMem() {
		return actual_mem;
	}

	public void setActualMem(String mem) {
		actual_mem = mem;
	}

	public String getUniquePatterns() {
		return unique_patterns;
	}

	public void setUniquePatterns(String unique_pats) {
		unique_patterns = unique_pats;
	}

	public String getNumTaxa() {
		return num_taxa;
	}

	public void setNumTaxa(String numtaxa) {
		num_taxa = numtaxa;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String filename) {
		configFileName = filename;
	}

	public Integer getSearchreps() {
		return searchreps;
	}

	public Integer getBootstrapreplicates() {
		return bootstrapreplicates;
	}

	public GARLIArguments getArgBean() {
		return argBean;
	}

	/* public static void main(String args[]) throws Exception {
		GARLIParser GP = new GARLIParser(args[0], "");
	} */
}
