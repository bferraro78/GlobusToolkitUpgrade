package edu.umd.grid.bio.garli.impl;

// GSBL classes
import edu.umd.umiacs.cummings.GSBL.BeanToArguments;
import edu.umd.umiacs.cummings.GSBL.GSBLJobManager;
//import edu.umd.umiacs.cummings.GSBL.ReliableFileTransferManager;
import edu.umd.umiacs.cummings.GSBL.GSBLService;
import edu.umd.umiacs.cummings.GSBL.GSBLRuntimeConfiguration;
import edu.umd.umiacs.cummings.GSBL.GSBLJob;
import edu.umd.umiacs.cummings.GSBL.GSBLUtils;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.io.*;
import java.util.*;

// For garbage collection
import java.lang.System;

// stub classes
import edu.umd.grid.bio.garli.stubs.GARLI.service.GARLIServiceAddressingLocator;
import edu.umd.grid.bio.garli.stubs.GARLIService.GARLIArguments;

//Place service specific imports here between the protection comments.
//BEGIN PROTECT: ServiceImports
import edu.umd.grid.bio.garli.shared.GARLIParser;


public class GARLIService extends GSBLService { 

    /**
     * Logger.
     */
    static Log log = LogFactory.getLog(GARLIService.class.getName());
 
    /**
     * Arguments bean.
     */    
    protected GARLIArguments myBean;
	
    /**
     * Our job instance.
     */
    private GSBLJob job = null;
	
    /**
     * Runtime configuration.  This is common to all service instances by virtue of being static.
     */
    static protected GSBLRuntimeConfiguration runtimeConfig = null;

    /**
     * directory where data and scripts for determining runtime estimates resides
     */
    static protected String runtime_estimates_location = null;

    /**
     * container_status location.
     */
    static protected String container_status_location = null;

    /**
     * update interval - how frequently the service checks on the status of its jobs
     */
    static protected String update_interval_string = null;
    static protected int update_interval = 300000; // default is 5 minutes
    static protected int update_max = 4800000; // default is 80 minutes

    /**
     * URL for updating Drupal job status
     */
    static protected String drupalUpdateURL = null;
    
    /**
     * This is the name of the service.
     */
    private String serviceName = "GARLI";


    // CONSTRUCTOR??


	// Load things from config files.  We only want to do this once.
    static {
        try {
	    Properties env = new Properties();
	    env.load(Runtime.getRuntime().exec("env").getInputStream());
	    String globusLocation = ""; //(String) env.get("GLOBUS_LOCATION");
            runtimeConfig = new GSBLRuntimeConfiguration(globusLocation + "/service_configurations/GARLI.runtime.xml");
	    runtime_estimates_location = globusLocation + "/runtime_estimates/";

	    //container_status_location = GSBLUtils.getConfigElement("container_status.location");

	    update_interval_string = GSBLUtils.getConfigElement("update_interval");
	    
	    // split interval string into min and max
            update_interval = Integer.parseInt(update_interval_string.substring(0, update_interval_string.indexOf(" ")));
            update_max = Integer.parseInt(update_interval_string.substring(update_interval_string.indexOf(" ") + 1));

	    drupalUpdateURL = GSBLUtils.getConfigElement("drupal_update_url");
	    
        } catch(Exception e) {
            log.error("Error loading config file information for GARLI: " + e);
        }
    }



       /**
     * This is almost like our "main" method.
     * @param myBean argument bean
     * @return true if service was started successfully
     */
    public boolean runService(GARLIArguments myBean) { 


    	










    } // end runService()





































} // end GARLIService class