/**
 * File: $Source: /fs/mikeproj/src/master/boinc-utils/lattice_parse.h,v $
 * Checked in: $Date: 2007/04/09 19:31:39 $
 * Checked in by: $Author: gt4admin $
 *
 * Revision Log:
 * $Log: lattice_parse.h,v $
 * Revision 1.1.1.1  2007/04/09 19:31:39  gt4admin
 * Initial checkin of assimilator, validator, and utility scripts for boinc
 *
 * Revision 1.1.1.1  2004/10/01 19:49:12  smclella
 *
 *
 */

#ifndef LATTICE_PARSE_H
#define LATTICE_PARSE_H

#ifndef _WIN32
#include <cstdio>
#include <cstdlib>
#include <string>
#endif

extern bool parse_str_return_buffer(char*&, const char*, char*, int);

#endif
