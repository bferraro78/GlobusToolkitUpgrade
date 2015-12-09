// The contents of this file are subject to the BOINC Public License
// Version 1.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at
// http://boinc.berkeley.edu/license_1.0.txt
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is the Berkeley Open Infrastructure for Network Computing.
//
// The Initial Developer of the Original Code is the SETI@home project.
// Portions created by the SETI@home project are Copyright (C) 2002
// University of California at Berkeley. All Rights Reserved.
//
// Contributor(s):
// Adam Bazinet (pknut777@umiacs.umd.edu)
//
// NOTE: currently, for this program to work, the NEXUSvalidator program must be in /usr/bin!

#include "util.h"
#include "sched_util.h"
#include "sched_msgs.h"
#include "validate_util.h"
#include "parse.h"
#include "version.h"
#include <stdlib.h>

#define ERR_XML_PARSE -112
using std::string;
using std::vector;

// TODO: use md5 hash


string parse_filename(XML_PARSER& xp, string& num) {
  string searchtag = "OUTFILE_" + num + "/";
  char tag[256];
  bool is_tag, found=false;
  while (!xp.get(tag, sizeof(tag), is_tag)) {
    if (!is_tag) continue;
    if (!strcmp(tag, searchtag.c_str())) {
      found = true;
    }
    if(!strcmp(tag, "/file_ref")){
      return "";
    }
    if (found && strcmp(tag, "name")) {
      //we know we are in the right file_ref box, so 
      xp.get(tag, sizeof(tag), is_tag);
      if(!is_tag){
	return tag;
      }
    }
  }
  return "";
}

string get_real_filename(string b_filename){
  //first, split the boinc filename into the job number and the file number
  int index = b_filename.find("_");
  string job_id = b_filename.substr(0,index);
  string job_id_fragment = job_id.substr(0,job_id.find_last_of("."));
  if(job_id_fragment.find_last_of(".") != string::npos) { // it's a batch, so just use the leading fragment as the job id
    job_id = job_id_fragment;
  }

  //if(job_id.find_last_of(".") > (job_id.size()-6)){
  //its a batch, so get rid of the .# at the end
  //job_id = job_id.substr(0, index-2);
  //}

  string file_num = b_filename.substr(b_filename.size()-1);	
  char *boinc_loc = getenv("BOINC_LOCATION");
  char open_file[256];
  string file_name;
  sprintf(open_file, "%s/templates/ru.%s", boinc_loc, job_id.c_str());
  //fprintf(stderr, "Opening file: %s\n", open_file);
  FILE* fp = fopen(open_file, "r");
  if(fp == NULL){
    //this means the workunit has already assimilated and deleted the 
    //result unit template.  This is normal when the last result comes in
    //fprintf(stderr, "ERROR: File not found!\n");
    return "";
  }
  char tag[256], r_filename[256];
  bool is_tag = false;
  MIOFILE mf;
  mf.init_file(fp);
  XML_PARSER xp(&mf);
  //parse until a file_name tag is found
  //see if that filename tag matches the one we are looking for.
  //if so, grab the real filename and break;
  while(!xp.get(tag, sizeof(tag), is_tag)) {
    if (!is_tag) continue;
    if (!strcmp(tag, "file_name")) {
      file_name = parse_filename(xp, file_num);
      if(file_name.compare("") != 0){
	//we found the real filename we were looking for
	//fprintf(stderr, "Found name: %s\n", file_name.c_str());
	return file_name;
      }
    }
    //fprintf(stderr, "TAG: %s\n", tag);
  }
  return "";
}



/**
 * Let's just make sure the GARLI tree file is valid, for now
 * We'll do our validation in this function instead of in compare_results
 * because we're only going to send out one result per workunit
 *
 * Created: 10/30/09
 */
int init_result(RESULT& result, void*& data) {

  int retval;
  int num_results;
  vector<string> path;

  retval = get_output_file_paths(result, path);

  if( retval ) {
    log_messages.printf( SCHED_MSG_LOG::MSG_CRITICAL,
			 "[RESULT#%d %s] init_result: can't get output filename\n",
			 result.id, result.name );
    if(retval == ERR_XML_PARSE){
      log_messages.printf(SCHED_MSG_LOG::MSG_CRITICAL, 
			  "Error code %d with xml file %s\n", retval, result.xml_doc_in);
    }
    return retval;
  }
	
  num_results = path.size();


  string *s = new string;
  string *tmp = new string;
  data = (void*) s;
    
  string pathtobin( "/usr/bin/" );
  string dos( "dos2unix " );
  string mac( "mac2unix " );
  string nexusvalidator( "NEXUSvalidator " );
  string runCmd;
  //string devnull( " &>/dev/null" );

  int returnval = 0;
	
  for( int i=0; i < num_results; i++ ) {
		
    string end = path[i].substr(path[i].size()-2);
    string file = path[i].substr(path[i].find_last_of("/")+1);
    string real_filename = get_real_filename(file);
		
    // we only want to validate the tree file
    if((real_filename.find(".best.tre")!=-1 || real_filename.find(".boot.tre")!=-1) && real_filename.find(".phy")==-1){
      log_messages.printf(SCHED_MSG_LOG::MSG_DEBUG,
			  "Allowing %s to be validated\n", real_filename.c_str());

      runCmd = pathtobin + dos + path[i];// + devnull;
      system( runCmd.c_str() );

      runCmd = pathtobin + mac + path[i];// + devnull;
      system( runCmd.c_str() );
      
      runCmd = pathtobin + nexusvalidator + path[i];// + devnull;
      int retval = system( runCmd.c_str() );

      printf("return val from NEXUSvalidator system call: %d\n", retval);

      if(retval != 0) {
	returnval = retval;
      }
    	    
      read_file_string( path[i].c_str(), *tmp );
      *s += *tmp;
    }else{
      *s = " ";
    }
  }
  delete tmp;
  return returnval;
}

int compare_results(
		    RESULT & r1, void* data1,
		    RESULT const& r2, void* data2,
		    bool& match) {

  match = true;

  log_messages.printf( SCHED_MSG_LOG::MSG_NORMAL,
		       "[%s vs %s] Comparing result units (not really)\n", r1.name, r2.name );
  
  log_messages.printf( SCHED_MSG_LOG::MSG_NORMAL,
		       "[%s vs %s] Results match! (for GARLI, always true)\n", r1.name, r2.name );

  return 0;
}

int cleanup_result(RESULT const& /*result*/, void* data) {
  string* s = (string*) data;
  delete s;
  return 0;
}

double compute_granted_credit(WORKUNIT& wu, vector<RESULT>& results) {
       
  return median_mean_credit(wu, results);
}
