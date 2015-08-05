package edu.umd.grid.bio.garli.impl;

import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class RFMatrixUpdater{
    	public static final RFMatrixUpdater INSTANCE = new RFMatrixUpdater();
    	private HashMap<String, String[]> myJobs;	//keys are unique IDs and values are attribute arrays


    /**
     * Logger.
     */
    static Log log = LogFactory.getLog(RFMatrixUpdater.class.getName());
    	
    	//private constructor
    	private RFMatrixUpdater(){
    		myJobs = new HashMap();
    	}
    	
    	//access method
    	private static RFMatrixUpdater getInstance(){
    		return INSTANCE;
    	}
    	
    	public void addJob(String id, String unique_patterns, String num_taxa,
    			String actual_mem, String datatype, String ratematrix, String statefrequencies,
    			String ratehetmodel, String numratecats, String invariantsites){

	    log.debug("inside addJob before testing myJobs.containsKey");

    		if(!myJobs.containsKey(id)){
    			String[] attributes = new String[10];
    			attributes[0] = null;	//held for runtime given later in finishJob
    			attributes[1] = unique_patterns;
    			attributes[2] = num_taxa;
    			attributes[3] = actual_mem;
    			attributes[4] = datatype;
    			attributes[5] = ratematrix;
    			attributes[6] = statefrequencies;
			attributes[7] = ratehetmodel;
			attributes[8] = numratecats;
			attributes[9] = invariantsites;
			myJobs.put(id, attributes);
    	    	
			//test with arbitrary runtime
			
			try{
			    finishJob(id, "5");
			}
			catch(IOException e){
			    //do nothing
			}
    		}

	    log.debug("inside addJob after testing myJobs.containsKey");
    	}
    		
    	//need to find appropriate time to call this. must be at completion of job.
    	public void finishJob(String id, String runtime) throws java.io.IOException{

	    log.debug("inside finishJob before testing myJobs.containsKey");


    		if(myJobs.containsKey(id)){
    			String[] attributes = myJobs.get(id);
    			attributes[0] = runtime;
    			
    			File matrix = new File("gridtest@valine/grid/garli_runs/profiling/training/rf_matrix");
    			
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(matrix, true)));
    			
    			String nextLine = "" + attributes[0] + "\t" +
    				attributes[1] + "\t" + attributes[2] + "\t" +
    				attributes[3] + "\t" + attributes[4] + "\t" +
    				attributes[5] + "\t" + attributes[6] + "\t" +
    				attributes[7] + "\t" + attributes[8] + "\t" +
    				attributes[9] + "\n";
    			writer.write(nextLine);
    			
    			writer.close();
    			
    			myJobs.remove(id);
    		}

	    log.debug("inside finishJob after testing myJobs.containsKey");
    	}
}
