/**
 * File: $Source: /fs/mikeproj/src/master/boinc-utils/lattice_validate_util.C,v $
 * Checked in: $Date: 2007/04/09 19:31:39 $
 * Checked in by: $Author: gt4admin $
 * 
 * Revision Log:
 * $Log: lattice_validate_util.C,v $
 * Revision 1.1.1.1  2007/04/09 19:31:39  gt4admin
 * Initial checkin of assimilator, validator, and utility scripts for boinc
 *
 * Revision 1.2  2005/05/23 15:05:26  pknut777
 * updated lattice_validator so it does not compare stdout... this might not be a good idea, but i'm going to try it for a while
 *
 * Revision 1.1.1.1  2004/10/01 19:49:12  smclella
 *
 *
 */

#include <cassert>

#include "error_numbers.h"
#include "parse.h"
#include "lattice_parse.h"

#include "sched_util.h"
#include "sched_config.h"
#include "sched_msgs.h"
#include "validate_util.h"
#include "lattice_validate_util.h"

using std::vector;
using std::string;

extern SCHED_CONFIG config;

int get_output_files_path(RESULT const& result, string*& path, int& num_results ) {

    char buf[256];
    bool flag;
    int max_results = 20;
    path = new string[max_results];
    num_results = 0;    
    
    char *xml_doc = new char[ sizeof(result.xml_doc_in) ];
    strcpy( xml_doc, result.xml_doc_in );

    flag = parse_str_return_buffer( xml_doc, "<name>", buf, sizeof(buf) );
   
    if( !flag ) {
        return ERR_XML_PARSE;
    }

    // ALB, 5/23/05 commenting out stdout because it doesn't always match well
    //path[num_results] = config.upload_dir;
    //path[num_results] += '/';
    //path[num_results] += buf;

    //num_results ++;
   
    while( (parse_str_return_buffer( xml_doc, "<name>", buf, sizeof(buf))) &&
            num_results < max_results ) {
        //do not count stderr
        string tmpBuf( buf );
        if( tmpBuf.substr( tmpBuf.size()-1 ) != "1" ) { 
            path[num_results] = config.upload_dir;
            path[num_results] += '/';
            path[num_results] += buf;

            num_results ++;
        }
    }

    return 0;
}
