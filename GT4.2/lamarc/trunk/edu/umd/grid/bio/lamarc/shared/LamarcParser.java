package edu.umd.grid.bio.lamarc.shared;

import java.io.*;
import java.lang.*;
import java.text.*;

public class LamarcParser{
    // indent parsing
    private final String tab_spacing = "     ";

    // xml tags
    private final String data_xml_start_tag = "<data>";
    private final String data_xml_end_tag = "</data>";

    //parameter input formatting tags
    private final String paramfile_lamconv_begin_tag = "--LAMCONV_PARAM_BEGIN";
    private final String paramfile_lamconv_end_tag = "--LAMCONV_PARAM_END";
    private final String paramfile_lamarc_begin_tag = "--LAMARC_PARAM_BEGIN";
    private final String paramfile_lamarc_end_tag = "--LAMARC_PARAM_END";

    // parameter tag defaults
    // lam_conv parameters
    public static final String lamconv_geneticdatafilename_tag = "GENETICDATAFILENAME";
    public static final String lamconv_outfilename_tag = "OUTFILENAME";
    public static final String lamconv_geneticdatatype_tag = "GENETICDATATYPE";
    public static final String lamconv_geneticdataformat_tag = "GENETICDATAFORMAT";
    public static final String lamconv_genetricdatainterleaved_tag = "GENETICDATAINTERLEAVED";
    public static final String lamconv_regionlength_tag = "REGIONLENGTH";
    public static final String lamconv_regionoffset_tag = "REGIONOFFSET";
    public static final String lamconv_locusmppos_tag = "LOCUSMAPPOS";
    public static final String lamconv_microregions_tag = "MICROREGIONS";

    //lamarc parameters
    public static final String forces_coalescence_start_value_tag = "FORCES_COALESCENCE_START_VALUE";
    public static final String forces_coalescence_method_tag = "FORCES_COALESCENCE_METHOD";
    public static final String forces_coalescence_profiles_tag = "FORCES_COALESCENCE_PROFILES";
    public static final String forces_coalescence_max_events_tag = "FORCES_COALESCENCE_MAX_EVENTS";
    
    public static final String forces_migration_start_value_tag = "FORCES_MIGRATION_START_VALUE";
    public static final String forces_migration_method_tag = "FORCES_MIGRATION_METHOD";
    public static final String forces_migration_profiles_tag = "FORCES_MIGRATION_PROFILES";
    public static final String forces_migration_max_events_tag = "FORCES_MIGRATION_MAX_EVENTS";


    public static final String forces_recombination_start_value_tag = "FORCES_RECOMBINATION_START_VALUE";
    public static final String forces_recombination_method_tag = "FORCES_RECOMBINATION_METHOD";
    public static final String forces_recombination_profiles_tag = "FORCES_RECOMBINATION_PROFILES";
    public static final String forces_recombination_max_events_tag = "FORCES_RECOMBINATION_MAX_EVENTS";

    public static final String forces_growth_start_value_tag = "FORCES_GROWTH_START_VALUE";
    public static final String forces_growth_method_tag = "FORCES_GROWTH_METHOD";
    public static final String forces_growth_profiles_tag = "FORCES_GROWTH_PROFILES";
    public static final String forces_growth_max_events_tag = "FORCES_GROWTH_MAX_EVENTS";

    public static final String chains_replicates_tag = "CHAINS_REPLICATES";
    public static final String chains_heating_temperatures_tag = "CHAINS_HEATING_TEMPERATURES";
    public static final String chains_heating_swap_interval_tag = "CHAINS_HEATING_SWAP_INTERVAL";
    public static final String chains_heating_adaptive_tag = "CHAINS_HEATING_ADAPTIVE";
    public static final String chains_strategy_resimulating_tag = "CHAINS_STRATEGY_RESIMULATING";
    public static final String chains_strategy_haplotyping_tag = "CHAINS_STRATEGY_HAPLOTYPING";
    public static final String chains_initial_number_tag = "CHAINS_INITIAL_NUMBER";
    public static final String chains_initial_samples_tag = "CHAINS_INITIAL_SAMPLES";
    public static final String chains_initial_discard_tag = "CHAINS_INITIAL_DISCARD";
    public static final String chains_initial_interval_tag = "CHAINS_INITIAL_INTERVAL";
    public static final String chains_final_number_tag = "CHAINS_FINAL_NUMBER";
    public static final String chains_final_samples_tag = "CHAINS_FINAL_SAMPLES";
    public static final String chains_final_discard_tag = "CHAINS_FINAL_DISCARD";
    public static final String chains_final_interval_tag = "CHAINS_FINAL_INTERVAL";

    public static final String format_verbosity_tag = "FORMAT_VERBOSITY";
    public static final String format_progress_reports_tag = "FORMAT_PROGRESS_REPORTS";
    public static final String format_echo_tag = "FORMAT_ECHO";
    public static final String format_plotting_profile_tag = "FORMAT_PLOTTING_PROFILE";
    public static final String format_plotting_posterior_tag = "FORMAT_PLOTTING_POSTERIOR";
    public static final String format_seed_tag = "FORMAT_SEED";
    public static final String format_parameter_file_tag = "FORMAT_PARAMETER_FILE";
    public static final String format_results_file_tag = "FORMAT_RESULTS_FILE";
    public static final String format_in_summary_file_tag = "FORMAT_IN_SUMMARY_FILE";
    public static final String format_use_in_summary_file_tag = "FORMAT_USE_IN_SUMMARY_FILE";
    public static final String format_out_summary_file_tag = "FORMAT_OUT_SUMMARY_FILE";
    public static final String format_use_out_summary_file_tag = "FORMAT_USE_OUT_SUMMARY_FILE";


    // lamarc operating defaults
    private final double forces_coalescence_start_value_default = 0.01;
    private final String forces_coalescence_method_default = "USER";
    private final String forces_coalescence_profiles_default = "none";
    private final long forces_coalescence_max_events_default = 1000;
    
    private final double forces_migration_start_value_default = 0.01;
    private final String forces_migration_method_default = "USER";
    private final String forces_migration_profiles_default = "none";
    private final long forces_migration_max_events_default = 1000;


    private final double forces_recombination_start_value_default = 0.01;
    private final String forces_recombination_method_default = "USER";
    private final String forces_recombination_profiles_default = "none";
    private final long forces_recombination_max_events_default = 1000;

    private final double forces_growth_start_value_default = 0.01;
    private final String forces_growth_method_default = "USER";
    private final String forces_growth_profiles_default = "none";
    private final long forces_growth_max_events_default = 1000;

    private final long chains_replicates_default = 1;
    private final double chains_heating_temperatures_default = 1;
    private final long chains_heating_swap_interval_default = 1;
    private final boolean chains_heating_adaptive_default = false;
    private final double chains_strategy_resimulating_default = 1.0;
    private final double chains_strategy_haplotyping_default = 0.0;
    private final long chains_initial_number_default = 10;
    private final long chains_initial_samples_default = 500;
    private final long chains_initial_discard_default = 1000;
    private final long chains_initial_interval_default = 20;
    private final long chains_final_number_default = 1;
    private final long chains_final_samples_default = 10000;
    private final long chains_final_discard_default = 1000;
    private final long chains_final_interval_default = 20;

    private final String format_verbosity_default = "verbose";
    private final String format_progress_reports_default = "normal";
    private final boolean format_echo_default = true;
    private final boolean format_plotting_profile_default = false;
    private final boolean format_plotting_posterior_default = false;
    private final long format_seed_default = 1005;
    private final String format_parameter_file_default = "paramfile";
    private final String format_results_file_default = "outfile";
    private final String format_in_summary_file_default = "insumfile";
    private final boolean format_use_in_summary_file_default = false;
    private final String format_out_summary_file_default = "outsumfile";
    private final boolean format_use_out_summary_file_default = false;

    public static final String lamarc_xmlinput_filename_default = "infile";

    // lam_conv operating defaults
    public static final String lam_conv_xmloutput_filename_default = "outfile";

    // lamarc operating parameters
    private double [] forces_coalescence_start_value_param;
    private String [] forces_coalescence_method_param;
    private String [] forces_coalescence_profiles_param;
    private long forces_coalescence_max_events_param;
    
    private double [] forces_migration_start_value_param;
    private String [] forces_migration_method_param;
    private String [] forces_migration_profiles_param;
    private long forces_migration_max_events_param;

    private double [] forces_recombination_start_value_param;
    private String [] forces_recombination_method_param;
    private String [] forces_recombination_profiles_param;
    private long forces_recombination_max_events_param;

    private double [] forces_growth_start_value_param;
    private String [] forces_growth_method_param;
    private String [] forces_growth_profiles_param;
    private long forces_growth_max_events_param;

    private long[] chains_replicates_param;
    private double[] chains_heating_temperatures_param;
    private long [] chains_heating_swap_interval_param;
    private boolean chains_heating_adaptive_param;
    private double chains_strategy_resimulating_param;
    private double chains_strategy_haplotyping_param;
    private long chains_initial_number_param;
    private long chains_initial_samples_param;
    private long chains_initial_discard_param;
    private long chains_initial_interval_param;
    private long chains_final_number_param;
    private long chains_final_samples_param;
    private long chains_final_discard_param;
    private long chains_final_interval_param;
    
    private String format_verbosity_param;
    private String format_progress_reports_param;
    private boolean format_echo_param;
    private boolean format_plotting_profile_param;
    private boolean format_plotting_posterior_param;
    private long format_seed_param;
    private String format_parameter_file_param;
    private String format_results_file_param;
    private String format_in_summary_file_param;
    private boolean format_use_in_summary_file_param;
    private String format_out_summary_file_param;
    private boolean format_use_out_summary_file_param;

    // lam_conv operating parameters
    private String lam_conv_xmloutput_filename_param;

    //enable flags
    private boolean forces_migration_enabled;
    private boolean forces_recombination_enabled;
    private boolean forces_growth_enabled;
    private boolean chains_strategy_haplotyping_enabled;

    //operating args
    private String lam_conv_args;
    private String lamarc_args;
    private String working_dir;

    //executables
    private final String lam_conv_executable = "lam_conv";

    

    public LamarcParser(String working_dir){
	initParams();
	this.working_dir = working_dir;
    }
    
    public boolean proccArgs(String args){
	boolean is_error = false;
	args = args.trim();
	String params[] = args.split("--");

	for(int i=0; i<params.length; i++){
	    if(params[i].trim().length()>0){
		String vals[] = params[i].split(" ");

		//forces coalescence
		if(forces_coalescence_start_value_tag.compareToIgnoreCase(vals[0])==0){
		    forces_coalescence_start_value_param = new double[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			if(vals[j].compareTo("-") == 0){
			    forces_coalescence_start_value_param[j-1] = 0;
			} else {
			    forces_coalescence_start_value_param[j-1] = Double.valueOf(vals[j]).doubleValue();
			}
		    }
		} else if(forces_coalescence_method_tag.compareToIgnoreCase(vals[0])==0){
		    forces_coalescence_method_param = new String[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			forces_coalescence_method_param[j-1] = vals[j];
		    }
		} else if(forces_coalescence_profiles_tag.compareToIgnoreCase(vals[0])==0){
		    forces_coalescence_profiles_param = new String[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			forces_coalescence_profiles_param[j-1] = vals[j];
		    }
		} else if(forces_coalescence_max_events_tag.compareToIgnoreCase(vals[0])==0){
		    forces_coalescence_max_events_param = Long.valueOf(vals[1]).longValue();
		} 
	    
		//forces migration
		else if(forces_migration_start_value_tag.compareToIgnoreCase(vals[0])==0){
		    forces_migration_start_value_param = new double[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			if(vals[j].compareTo("-") == 0){
			forces_migration_start_value_param[j-1] = 0;
			} else {
			    forces_migration_start_value_param[j-1] = Double.valueOf(vals[j]).doubleValue();
			}
		    }
		    forces_migration_enabled = true;
		} else if(forces_migration_method_tag.compareToIgnoreCase(vals[0])==0){
		    forces_migration_method_param = new String[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			forces_migration_method_param[j-1] = vals[j];
		    }
		    forces_migration_enabled = true;
		} else if(forces_migration_profiles_tag.compareToIgnoreCase(vals[0])==0){
		    forces_migration_profiles_param = new String[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			forces_migration_profiles_param[j-1] = vals[j];
		    }
		    forces_migration_enabled = true;
		} else if(forces_migration_max_events_tag.compareToIgnoreCase(vals[0])==0){
		    forces_migration_max_events_param = Long.valueOf(vals[1]).longValue();
		    forces_migration_enabled = true;
		} 
		
		//forces recombination
		else if(forces_recombination_start_value_tag.compareToIgnoreCase(vals[0])==0){
		    forces_recombination_start_value_param = new double[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			if(vals[j].compareTo("-") == 0){
			    forces_recombination_start_value_param[j-1] = 0;
			} else {
			    forces_recombination_start_value_param[j-1] = Double.valueOf(vals[j]).doubleValue();
			}
		    }
		    forces_recombination_enabled = true;
		} else if(forces_recombination_method_tag.compareToIgnoreCase(vals[0])==0){
		    forces_recombination_method_param = new String[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			forces_recombination_method_param[j-1] = vals[j];
		    }
		    forces_recombination_enabled = true;
		} else if(forces_recombination_profiles_tag.compareToIgnoreCase(vals[0])==0){
		    forces_recombination_profiles_param = new String[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			forces_recombination_profiles_param[j-1] = vals[j];
		    }
		    forces_recombination_enabled = true;
		} else if(forces_recombination_max_events_tag.compareToIgnoreCase(vals[0])==0){
		    forces_recombination_max_events_param = Long.valueOf(vals[1]).longValue();
		    forces_recombination_enabled = true;
		}
		
		//forces growth
		else if(forces_growth_start_value_tag.compareToIgnoreCase(vals[0])==0){
		    forces_growth_start_value_param = new double[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			if(vals[j].compareTo("-") == 0){
			    forces_growth_start_value_param[j-1] = 0;
			} else {
			    forces_growth_start_value_param[j-1] = Double.valueOf(vals[j]).doubleValue();
			}
		    }
		    forces_growth_enabled = true;
		} else if(forces_growth_method_tag.compareToIgnoreCase(vals[0])==0){
		    forces_growth_method_param = new String[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			forces_growth_method_param[j-1] = vals[j];
		    }
		    forces_growth_enabled = true;
		} else if(forces_growth_profiles_tag.compareToIgnoreCase(vals[0])==0){
		    forces_growth_profiles_param = new String[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			forces_growth_profiles_param[j-1] = vals[j];
		    }
		    forces_growth_enabled = true;
		} else if(forces_growth_max_events_tag.compareToIgnoreCase(vals[0])==0){
		    forces_growth_max_events_param = Long.valueOf(vals[1]).longValue();
		    forces_growth_enabled = true;
		}
		
		//chains
		else if(chains_replicates_tag.compareToIgnoreCase(vals[0])==0){
		    chains_replicates_param = new long[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			if(vals[j].compareTo("-") == 0){
			    chains_replicates_param[j-1] = 0;
			} else {
			    chains_replicates_param[j-1] = Long.valueOf(vals[j]).longValue();
			}
		    }	
		} else if(chains_heating_temperatures_tag.compareToIgnoreCase(vals[0])==0){
		    chains_heating_temperatures_param = new double[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			if(vals[j].compareTo("-") == 0){
			    chains_heating_temperatures_param[j-1] = 0;
			} else {
			    chains_heating_temperatures_param[j-1] = Double.valueOf(vals[j]).doubleValue();
			}
		    }	
		} else if(chains_heating_swap_interval_tag.compareToIgnoreCase(vals[0])==0){
		    chains_heating_swap_interval_param = new long[vals.length - 1];
		    for(int j=1; j<vals.length; j++){
			if(vals[j].compareTo("-") == 0){
			    chains_heating_swap_interval_param[j-1] = 0;
			} else {
			    chains_heating_swap_interval_param[j-1] = Long.valueOf(vals[j]).longValue();
			}
		    }
		} else if(chains_heating_adaptive_tag.compareToIgnoreCase(vals[0])==0){
		    chains_heating_adaptive_param = Boolean.valueOf(vals[1]).booleanValue();
		} else if(chains_strategy_resimulating_tag.compareToIgnoreCase(vals[0])==0){
		    chains_strategy_resimulating_param = Double.valueOf(vals[1]).doubleValue();
		} else if(chains_strategy_haplotyping_tag.compareToIgnoreCase(vals[0])==0){
		    chains_strategy_haplotyping_param = Double.valueOf(vals[1]).doubleValue();
		    chains_strategy_haplotyping_enabled = true;
		} else if(chains_initial_number_tag.compareToIgnoreCase(vals[0])==0){
		    chains_initial_number_param = Long.valueOf(vals[1]).longValue();
		} else if(chains_initial_samples_tag.compareToIgnoreCase(vals[0])==0){
		    chains_initial_samples_param = Long.valueOf(vals[1]).longValue();
		} else if(chains_initial_discard_tag.compareToIgnoreCase(vals[0])==0){
		    chains_initial_discard_param = Long.valueOf(vals[1]).longValue();
		} else if(chains_initial_interval_tag.compareToIgnoreCase(vals[0])==0){
		    chains_initial_interval_param = Long.valueOf(vals[1]).longValue();
		} else if(chains_final_number_tag.compareToIgnoreCase(vals[0])==0){
		    chains_final_number_param = Long.valueOf(vals[1]).longValue();
		} else if(chains_final_samples_tag.compareToIgnoreCase(vals[0])==0){
		    chains_final_samples_param = Long.valueOf(vals[1]).longValue();
		} else if(chains_final_discard_tag.compareToIgnoreCase(vals[0])==0){
		    chains_final_discard_param = Long.valueOf(vals[1]).longValue();
		} else if(chains_final_interval_tag.compareToIgnoreCase(vals[0])==0){
		    chains_final_interval_param = Long.valueOf(vals[1]).longValue();
		} 
		
		//formats
		else if(format_verbosity_tag.compareToIgnoreCase(vals[0])==0){
		    format_verbosity_param = vals[1];
		} else if(format_progress_reports_tag.compareToIgnoreCase(vals[0])==0){
		    format_progress_reports_param = vals[1];
		} else if(format_echo_tag.compareToIgnoreCase(vals[0])==0){
		    format_echo_param = Boolean.valueOf(vals[1]).booleanValue();
		} else if(format_plotting_profile_tag.compareToIgnoreCase(vals[0])==0){
		    format_plotting_profile_param = Boolean.valueOf(vals[1]).booleanValue();
		} else if(format_plotting_posterior_tag.compareToIgnoreCase(vals[0])==0){
		    format_plotting_posterior_param = Boolean.valueOf(vals[1]).booleanValue();
		} else if(format_seed_tag.compareToIgnoreCase(vals[0])==0){
		    format_seed_param = Long.valueOf(vals[1]).longValue();
		} else if(format_parameter_file_tag.compareToIgnoreCase(vals[0])==0){
		    format_parameter_file_param = vals[1];
		} else if(format_results_file_tag.compareToIgnoreCase(vals[0])==0){
		    format_results_file_param = vals[1];
		} else if(format_in_summary_file_tag.compareToIgnoreCase(vals[0])==0){
		    format_in_summary_file_param = vals[1];
		} else if(format_use_in_summary_file_tag.compareToIgnoreCase(vals[0])==0){
		    format_use_in_summary_file_param = Boolean.valueOf(vals[1]).booleanValue();
		} else if(format_out_summary_file_tag.compareToIgnoreCase(vals[0])==0){
		    format_out_summary_file_param = vals[1];
		} else if(format_use_out_summary_file_tag.compareTo(vals[0])==0){
		    format_use_out_summary_file_param = Boolean.valueOf(vals[1]).booleanValue(); 
		} 
		
		//default
		else{
		    System.err.println("ERROR: unknown parameter -- " + vals[0]);
		    is_error = true;
		}
	    }
	}

	return is_error;
    }

    public void initParams(){
	//initializing lamarc parameters
	forces_coalescence_start_value_param = new double[1];
	forces_coalescence_start_value_param[0] = forces_coalescence_start_value_default;
	forces_coalescence_method_param = new String[1];
	forces_coalescence_method_param[0] = forces_coalescence_method_default;;
	forces_coalescence_profiles_param = new String[1];
	forces_coalescence_profiles_param = new String[1];
	forces_coalescence_profiles_param[0] = forces_coalescence_profiles_default;
	forces_coalescence_max_events_param = forces_coalescence_max_events_default;
	
	forces_migration_start_value_param = new double[1];
	forces_migration_start_value_param[0] = forces_migration_start_value_default;
	forces_migration_method_param = new String[1];
	forces_migration_method_param[0] = forces_migration_method_default;
	forces_migration_profiles_param = new String[1];
	forces_migration_profiles_param[0] = forces_migration_profiles_default;;
	forces_migration_max_events_param = forces_migration_max_events_default;

	forces_recombination_start_value_param = new double[1];
	forces_recombination_start_value_param[0] = forces_recombination_start_value_default; 
	forces_recombination_method_param = new String[1];
	forces_recombination_method_param[0] = forces_recombination_method_default;;
	forces_recombination_profiles_param = new String[1];
	forces_recombination_profiles_param[0] = forces_recombination_profiles_default;
	forces_recombination_max_events_param = forces_recombination_max_events_default;
	
	forces_growth_start_value_param = new double[1];
	forces_growth_start_value_param[0] = forces_growth_start_value_default;
	forces_growth_method_param = new String[1];
	forces_growth_method_param[0] = forces_growth_method_default;
	forces_growth_profiles_param = new String[1];
	forces_growth_profiles_param[0] = forces_growth_profiles_default;;
	forces_growth_max_events_param = forces_growth_max_events_default;
	
	chains_replicates_param = new long[1];
	chains_replicates_param[0] = chains_replicates_default;
	chains_heating_temperatures_param = new double[1];
	chains_heating_temperatures_param[0] = chains_heating_temperatures_default;
	chains_heating_swap_interval_param = new long[1];
	chains_heating_swap_interval_param[0] = chains_heating_swap_interval_default;
	chains_heating_adaptive_param = chains_heating_adaptive_default;
	chains_strategy_resimulating_param = chains_strategy_resimulating_default;
	chains_strategy_haplotyping_param = chains_strategy_haplotyping_default;
	chains_initial_number_param = chains_initial_number_default;
	chains_initial_samples_param = chains_initial_samples_default;
	chains_initial_discard_param = chains_initial_discard_default;
	chains_initial_interval_param = chains_initial_interval_default;
	chains_final_number_param = chains_final_number_default;
	chains_final_samples_param = chains_final_samples_default;
	chains_final_discard_param = chains_final_discard_default;
	chains_final_interval_param = chains_final_interval_default;
	
	format_verbosity_param = format_verbosity_default;
	format_progress_reports_param = format_progress_reports_default;
	format_echo_param = format_echo_default;
	format_plotting_profile_param = format_plotting_profile_default;
	format_plotting_posterior_param = format_plotting_posterior_default;
	format_seed_param = format_seed_default;
	format_parameter_file_param = format_parameter_file_default;
	format_results_file_param = format_results_file_default;
	format_in_summary_file_param = format_in_summary_file_default;
	format_use_in_summary_file_param = format_use_in_summary_file_default;
	format_out_summary_file_param = format_out_summary_file_default;
	format_use_out_summary_file_param = format_use_out_summary_file_default;

	forces_migration_enabled = false;
	forces_recombination_enabled = false;
	forces_growth_enabled = false;
	chains_strategy_haplotyping_enabled = false;

	//initializing lam_conv parameters
	lam_conv_xmloutput_filename_param = working_dir + lam_conv_xmloutput_filename_default;
    }

    public void writeLamarcXMLInputFile(String lamarc_xmlinput_file)throws IOException{
	String spaces = tab_spacing;

	BufferedReader bfi = new BufferedReader(new FileReader(lam_conv_xmloutput_filename_param)); 
	BufferedWriter bfw = new BufferedWriter(new FileWriter(lamarc_xmlinput_file));



	bfw.write("<lamarc>\n");
	bfw.write("<!-- Created from the lattice lamarc service dispatch module -->\n");
	
	//write Force settings
	writeForceCoalescence(bfw, spaces, forces_coalescence_start_value_param, forces_coalescence_method_param, 
			      forces_coalescence_profiles_param, forces_coalescence_max_events_param);
	if(forces_migration_enabled){
	    writeForceMigration(bfw, spaces, forces_migration_start_value_param, forces_migration_method_param, 
				forces_migration_profiles_param, forces_migration_max_events_param);
	}
	if(forces_recombination_enabled){
	    writeForceRecombination(bfw, spaces, forces_recombination_start_value_param, forces_recombination_method_param, 
				    forces_recombination_profiles_param, forces_recombination_max_events_param);
	}
	if(forces_growth_enabled){
	    writeForceGrowth(bfw, spaces, forces_growth_start_value_param, forces_growth_method_param, 
			     forces_growth_profiles_param, forces_growth_max_events_param);
	}
	writeXMLBreak(bfw, spaces);

	//write Chain settings
	bfw.write(spaces + "<chains>\n");
	writeChainReplication(bfw, spaces + tab_spacing, chains_replicates_param);
	writeChainHeating(bfw, spaces + tab_spacing, chains_heating_temperatures_param, chains_heating_swap_interval_param, chains_heating_adaptive_param);
	writeChainStrategy(bfw, spaces + tab_spacing, chains_strategy_resimulating_param, chains_strategy_haplotyping_param);
	writeChainInitial(bfw, spaces + tab_spacing, chains_initial_number_param, chains_initial_samples_param, 
			  chains_initial_discard_param, chains_initial_interval_param);
	writeChainFinal(bfw, spaces + tab_spacing, chains_final_number_param, chains_final_samples_param, 
			chains_final_discard_param, chains_final_interval_param);
	bfw.write(spaces + "</chains>\n");
	writeXMLBreak(bfw, spaces);

	//write Format settings
	bfw.write(spaces + "<format>\n");
	writeFormatVerbosity(bfw, spaces + tab_spacing, format_verbosity_param);
	writeFormatProgressReports(bfw, spaces + tab_spacing, format_progress_reports_param);
	writeFormatEcho(bfw, spaces + tab_spacing, format_echo_param);
	writeFormatPlotting(bfw, spaces + tab_spacing, format_plotting_profile_param, format_plotting_posterior_param);
	writeFormatSeed(bfw, spaces + tab_spacing, format_seed_param);
	writeFormatParameterFile(bfw, spaces + tab_spacing, format_parameter_file_param);
	writeFormatResultsFile(bfw, spaces + tab_spacing, format_results_file_param);
	writeFormatInSummaryFile(bfw, spaces + tab_spacing, format_use_in_summary_file_param, format_in_summary_file_param); 
	writeFormatOutSummaryFile(bfw, spaces + tab_spacing, format_use_out_summary_file_param, format_out_summary_file_param); 
	bfw.write(spaces + "</format>\n");
	writeXMLBreak(bfw, spaces);

	//write Data
	bfw.write(spaces + "<data>\n");
	writeData(bfw, spaces, bfi);
	bfw.write(spaces + "</data>\n");

	bfw.write("</lamarc>\n");

	bfw.close();
	bfi.close();
    } 

    private void writeForceCoalescence(BufferedWriter bfw, 
				       String spaces, 
				       double[] start_values, 
				       String[] method, 
				       String[] profiles,     
				       long max_events) throws IOException{
	bfw.write(spaces + "<coalescence>\n");

	bfw.write(spaces + tab_spacing + "<start-values>");
	for(int i=0; i<start_values.length; i++){
	    bfw.write(" "+ Double.toString(start_values[i]));
	}
	bfw.write(" </start-values>\n");
	bfw.write(spaces + tab_spacing + "<method> ");
	for(int i=0; i<method.length; i++){
	    bfw.write(method[i] + " ");
	}
	bfw.write("</method>\n");

	if(profiles != null){
	    bfw.write(spaces + tab_spacing + "<profiles> ");
	    for(int i=0;i<profiles.length;i++){
		bfw.write(profiles[i] + " ");
	    }
	    bfw.write("</profiles>\n");
	} else {
	    bfw.write(spaces + tab_spacing + "<profiles> none </profiles>\n");
	}

	bfw.write(spaces + tab_spacing + "<max-events> " + Long.toString(max_events) + " </max-events>\n");


	bfw.write(spaces + "</coalescence>\n");
    }

     private void writeForceGrowth(BufferedWriter bfw, 
				   String spaces, 
				   double[] start_values, 
				   String[] method, 
				   String[] profiles,     
				   long max_events) throws IOException{
	bfw.write(spaces + "<growth>\n");

	bfw.write(spaces + tab_spacing + "<start-values>");
	for(int i=0; i<start_values.length; i++){
	    bfw.write(" "+ Double.toString(start_values[i]));
	}
	bfw.write(" </start-values>\n");
	bfw.write(spaces + tab_spacing + "<method> ");
	for(int i=0; i<method.length; i++){
	    bfw.write(method[i] + " ");
	}
	bfw.write("</method>\n");

	if(profiles != null){
	    bfw.write(spaces + tab_spacing + "<profiles> ");
	    for(int i=0;i<profiles.length;i++){
		bfw.write(profiles[i] + " ");
	    }
	    bfw.write("</profiles>\n");
	} else {
	    bfw.write(spaces + tab_spacing + "<profiles> none </profiles>\n");
	}

	bfw.write(spaces + tab_spacing + "<max-events> " + Long.toString(max_events) + " </max-events>\n");


	bfw.write(spaces + "</growth>\n");
    }

    private void writeForceMigration(BufferedWriter bfw, 
				     String spaces, 
				     double[] start_values, 
				     String[] method, 
				     String[] profiles,     
				       long max_events) throws IOException{
	bfw.write(spaces + "<migration>\n");
	
	bfw.write(spaces + tab_spacing + "<start-values>");
	for(int i=0; i<start_values.length; i++){
	    bfw.write(" "+ Double.toString(start_values[i]));
	}
	bfw.write(" </start-values>\n");
	bfw.write(spaces + tab_spacing + "<method> ");
	for(int i=0; i<method.length; i++){
	    bfw.write(method[i] + " ");
	}
	bfw.write("</method>\n");

	if(profiles != null){
	    bfw.write(spaces + tab_spacing + "<profiles> ");
	    for(int i=0;i<profiles.length;i++){
		bfw.write(profiles[i] + " ");
	    }
	    bfw.write("</profiles>\n");
	} else {
	    bfw.write(spaces + tab_spacing + "<profiles> none </profiles>\n");
	}

	bfw.write(spaces + tab_spacing + "<max-events> " + Long.toString(max_events) + " </max-events>\n");


	bfw.write(spaces + "</migration>\n");
    }

     private void writeForceRecombination(BufferedWriter bfw, 
				       String spaces, 
				       double[] start_values, 
				       String[] method, 
				       String[] profiles,     
				       long max_events) throws IOException{
	bfw.write(spaces + "<recombination>\n");

	bfw.write(spaces + tab_spacing + "<start-values>");
	for(int i=0; i<start_values.length; i++){
	    bfw.write(" "+ Double.toString(start_values[i]));
	}
	bfw.write(" </start-values>\n");
	bfw.write(spaces + tab_spacing + "<method> ");
	for(int i=0; i<method.length; i++){
	    bfw.write(method[i] + " ");
	}
	bfw.write("</method>\n");

	if(profiles != null){
	    bfw.write(spaces + tab_spacing + "<profiles> ");
	    for(int i=0;i<profiles.length;i++){
		bfw.write(profiles[i] + " ");
	    }
	    bfw.write("</profiles>\n");
	} else {
	    bfw.write(spaces + tab_spacing + "<profiles> none </profiles>\n");
	}

	bfw.write(spaces + tab_spacing + "<max-events> " + Long.toString(max_events) + " </max-events>\n");


	bfw.write(spaces + "</recombination>\n");
    }

    private void writeChainReplication(BufferedWriter bfw, 
				   String spaces, 
				   long[] replicates) throws IOException{
	bfw.write(spaces + "<replicates> ");
	for(int i=0;i<replicates.length;i++){
	    bfw.write(replicates[i] + " ");
	}
	bfw.write("</replicates>\n"); 
    }
				   
    private void writeChainHeating(BufferedWriter bfw, 
				   String spaces, 
				   double[] temperatures,
				   long[] swap_intervals,
				   boolean adaptive) throws IOException{
	bfw.write(spaces + "<heating>\n");

	bfw.write(spaces + tab_spacing + " <temperatures> ");
	for(int i=0; i<temperatures.length; i++){
	    bfw.write(" " + Double.toString(temperatures[i]));
	}
	bfw.write(" </temperatures>\n");

	bfw.write(spaces + tab_spacing + "<swap-intervals>");
	for(int i=0; i<swap_intervals.length; i++){
	    bfw.write(" " + Long.toString(swap_intervals[i]));
	}
	bfw.write(" </swap-intervals>\n");

	bfw.write(spaces + tab_spacing + "<adaptive> " + Boolean.toString(adaptive) + " </adaptive>\n");

	bfw.write(spaces + "</heating>\n");
    }

    private void writeChainStrategy(BufferedWriter bfw, 
				    String spaces,
				    double resimulating,
				    double haplotyping)throws IOException{
	bfw.write(spaces + "<strategy>\n");
	
	bfw.write(spaces + tab_spacing + "<resimulating> " + resimulating + " </resimulating>\n");
	
	if(chains_strategy_haplotyping_enabled){
	    bfw.write(spaces + tab_spacing + "<haplotyping> " + haplotyping + " </haplotyping>\n");
	}

	bfw.write(spaces + "</strategy>\n");
    }
				    
    private void writeChainInitial(BufferedWriter bfw, 
				   String spaces,
				   long number,
				   long samples,
				   long discard,
				   long interval)throws IOException{
	bfw.write(spaces + "<initial>\n");
	
	bfw.write(spaces + tab_spacing + "<number> " + Long.toString(number) + " </number>\n");
	bfw.write(spaces + tab_spacing + "<samples> " + Long.toString(samples) + " </samples>\n");
	bfw.write(spaces + tab_spacing + "<discard> " + Long.toString(discard) + " </discard>\n");
	bfw.write(spaces + tab_spacing + "<interval> " + Long.toString(interval) + " </interval>\n");

	bfw.write(spaces + "</initial>\n");
    }

    private void writeChainFinal(BufferedWriter bfw, 
				 String spaces,
				 long number,
				 long samples,
				 long discard,
				 long interval)throws IOException{
	bfw.write(spaces + "<final>\n");
	
	bfw.write(spaces + tab_spacing + "<number> " + Long.toString(number) + " </number>\n");
	bfw.write(spaces + tab_spacing + "<samples> " + Long.toString(samples) + " </samples>\n");
	bfw.write(spaces + tab_spacing + "<discard> " + Long.toString(discard) + " </discard>\n");
	bfw.write(spaces + tab_spacing + "<interval> " + Long.toString(interval) + " </interval>\n");

	bfw.write(spaces + "</final>\n");
    }

    private void writeFormatVerbosity(BufferedWriter bfw, 
				      String spaces,
				      String verbosity)throws IOException{
	bfw.write(spaces + "<verbosity> " + verbosity + " </verbosity>\n");
    } 

    private void writeFormatProgressReports(BufferedWriter bfw, 
				      String spaces,
				      String verbosity)throws IOException{
	bfw.write(spaces + "<progress-reports> " + verbosity + " </progress-reports>\n");
    } 

    private void writeFormatEcho(BufferedWriter bfw, 
				      String spaces,
				      boolean echo) throws IOException {
	bfw.write(spaces + "<echo> " + Boolean.toString(echo) + " </echo>\n");
    } 
    
    private void writeFormatPlotting(BufferedWriter bfw, 
				     String spaces,
				     boolean profile,
				     boolean posterior) throws IOException {
	bfw.write(spaces + "<plotting>\n");

	bfw.write(spaces + tab_spacing + "<profile> " + Boolean.toString(profile) + " </profile>\n");
	bfw.write(spaces + tab_spacing + "<posterior> " + Boolean.toString(posterior) + " </posterior>\n");

	bfw.write(spaces + "</plotting>\n");
    } 

    private void writeFormatSeed(BufferedWriter bfw, 
				 String spaces,
				 long seed) throws IOException {
	bfw.write(spaces + "<seed> " + Long.toString(seed) + " </seed>\n");
    }   

    private void writeFormatParameterFile(BufferedWriter bfw, 
					  String spaces,
					  String parameter_file) throws IOException {
	bfw.write(spaces + "<parameter-file> " + parameter_file + " </parameter-file>\n");
    } 

    private void writeFormatResultsFile(BufferedWriter bfw, 
					String spaces,
					String results_file) throws IOException {
	bfw.write(spaces + "<results-file>" + results_file + "</results-file>\n");
    }  

    private void writeFormatInSummaryFile(BufferedWriter bfw, 
					  String spaces,
					  boolean use_insummary_file,
					  String insummary_file) throws IOException {
	bfw.write(spaces + "<use-in-summary-file> " + Boolean.toString(use_insummary_file) + " </use-in-summary-file>\n");
	bfw.write(spaces + "<in-summary-file> " + insummary_file + " </in-summary-file>\n");
    }

    private void writeFormatOutSummaryFile(BufferedWriter bfw, 
					  String spaces,
					  boolean use_outsummary_file,
					  String outsummary_file) throws IOException {
	bfw.write(spaces + "<use-out-summary-file> " + Boolean.toString(use_outsummary_file) + " </use-out-summary-file>\n");
	bfw.write(spaces + "<out-summary-file> " + outsummary_file + " </out-summary-file>\n");
    }

    private void writeXMLBreak(BufferedWriter bfw, 
			       String spaces) throws IOException {
	bfw.write(spaces + "<!-- -->\n");
    }

    private void writeData(BufferedWriter bfw, 
			   String spaces,
			   BufferedReader bfi)throws IOException{
	String read_line;
	boolean data_xml_start_tag_found = false;
	boolean data_xml_end_tag_found = false;
	
	while(((read_line = bfi.readLine())!=null) && data_xml_start_tag_found == false){
	    read_line = read_line.trim();
	      
	    if(read_line.compareToIgnoreCase(data_xml_start_tag) == 0) data_xml_start_tag_found = true;
	}

	if(data_xml_start_tag_found == true){
	    bfw.write(spaces + read_line + "\n");
	    
	    while(((read_line = bfi.readLine())!= null) &&
		  (read_line.compareToIgnoreCase(data_xml_end_tag) != 0)){
		bfw.write(spaces + read_line + "\n");
	    }
	    return;
	} else {
	    return;
	}
    }

    public String getLamConvArgs(String param_file) throws IOException{
	BufferedReader bfi = new BufferedReader(new FileReader(param_file));
	String args = "";
	String line_read;
	boolean data_outfile_found = false;

	while(((line_read = bfi.readLine()) != null) &&
	      line_read.compareToIgnoreCase(paramfile_lamconv_begin_tag) != 0); //get the start of lamconv params
	
	while(((line_read = bfi.readLine()) != null) &&
	      line_read.compareToIgnoreCase(paramfile_lamconv_end_tag) != 0){
	    if(line_read.trim().length()>0 && line_read.trim().charAt(0) != '#'){
		String param[] = line_read.split("=");

		if(param.length == 2){
		    if(param[0].trim().compareTo(lamconv_outfilename_tag) == 0){
			lam_conv_xmloutput_filename_param = working_dir + param[1].trim();
			args = args + "--" + param[0].trim() + " " + lam_conv_xmloutput_filename_param + " ";
			data_outfile_found = true;
		    } else {
			args = args + "--" + param[0].trim() + " " +  param[1].trim() + " ";
		    }
		} else {
		    System.err.println("Format error encounterd in " + param_file + ":" + line_read);
		}
	    }
	}

	if(data_outfile_found == false) System.err.println("WARNING: Did not find lam_conv output file name. Using default.");

	return args;
    }
    
    public String getLamarcArgs(String param_file) throws IOException{
	BufferedReader bfi = new BufferedReader(new FileReader(param_file));
	String args = "";
	String line_read;

	while(((line_read = bfi.readLine()) != null) &&
	      line_read.compareToIgnoreCase(paramfile_lamarc_begin_tag) != 0); //get the start of lamarc params
	
	while(((line_read = bfi.readLine()) != null) &&
	      line_read.compareToIgnoreCase(paramfile_lamarc_end_tag) != 0){
	    if(line_read.trim().length()>0 && line_read.trim().charAt(0) != '#'){
		String param[] = line_read.split("=");
		if(param.length == 2){
		    args = args + "--" + param[0].trim() + " " + param[1].trim() + " ";
		} else {
		    System.err.println("Format error encounterd in " + param_file + ":" + line_read);
		}
	    }
	}

	return args;
    }

    public void writeLamarcXMLDataFile(String args){
	try{
	    Runtime runtime = Runtime.getRuntime();
	    runtime.exec(lam_conv_executable + " " + args).waitFor();
	    BufferedReader bfi = new BufferedReader(new FileReader(lam_conv_xmloutput_filename_param));
	} catch (Throwable t){
	    t.printStackTrace();
	}
    }
}
