/**
 * File: $Source: /fs/mikeproj/src/master/boinc-utils/lattice_parse.C,v $
 * Checked in: $Date: 2007/04/09 19:31:39 $
 * Checked in by: $Author: gt4admin $
 *
 * Revision Log:
 * $Log: lattice_parse.C,v $
 * Revision 1.1.1.1  2007/04/09 19:31:39  gt4admin
 * Initial checkin of assimilator, validator, and utility scripts for boinc
 *
 * Revision 1.2  2004/11/10 16:59:59  smclella
 * updating lattice folder
 *
 * Revision 1.1.1.1  2004/10/01 19:49:12  smclella
 *
 *
 */

#ifdef _WIN32
#include "boinc_win.h"
#endif

#ifndef _WIN32
#include <string.h>
#include <stdlib.h>
#include <string>
#endif

#include "util.h"
#include "parse.h"
#include "lattice_parse.h"

#ifdef _USING_FCGI_
#include "fcgi_stdio.h"
#endif

#include "sched_msgs.h"

using std::string;

bool parse_str_return_buffer( char*& buf, const char* tag, char* dest, int len ) {
    string str;
    string tmpStr;
    char const* p = strstr(buf, tag);
    if (!p) return false;
    p = strchr(p, '>');
    ++p;
    char const* q = strchr(p, '<');
    if (!q) return false;
    str.assign(p, q-p);
    strip_whitespace(str);
    xml_unescape(str, tmpStr);
    safe_strncpy( dest, tmpStr.c_str(), len );
    buf = strchr( q, '>' );
    ++buf;

    return true;
}
