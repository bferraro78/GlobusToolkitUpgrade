/**
 * File: $Source: /fs/mikeproj/src/master/boinc-utils/lattice_validate_util.h,v $
 * Chceked in: $Date: 2007/04/09 19:31:39 $
 * Checked in by: $Author: gt4admin $
 * 
 * Revision Log:
 * $Log: lattice_validate_util.h,v $
 * Revision 1.1.1.1  2007/04/09 19:31:39  gt4admin
 * Initial checkin of assimilator, validator, and utility scripts for boinc
 *
 * Revision 1.1.1.1  2004/10/01 19:49:12  smclella
 *
 *
 */

#ifndef LATTICE_VALIDATE_UTIL
#define LATTICE_VALIDATE_UTIL

#include "boinc_db.h"
#include <vector>
#include <string>

extern int get_output_files_path(RESULT const& result, std::string*& path, int& num_results );

#endif
