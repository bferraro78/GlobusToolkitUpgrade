package edu.umd.grid.bio.garli.clients;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.GSBLClient;
import edu.umd.umiacs.cummings.GSBL.GSBLPropertiesManager;
//import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager; // NO MORE FILE TRANSFERS

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












	} // end main function






	// 1. read properties file
	// 2. update bean
	// 3. build conif file
	// 4. parse config file and get input output files









} // end class