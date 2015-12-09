#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string>
#include <vector>
#include <iostream>
#include <sstream>
#include <cstdio>

#include "boinc_db.h"
#include "sched_msgs.h"
#include "sched_util.h"
#include "sched_config.h"
#include "assimilate_handler.h"
#include "validate_util.h"
#include "validate_util2.h"
#include "util.h"

using std::string;
using std::vector;

extern SCHED_CONFIG config;

/* DSM This is the part of the assimilator that's custom to BOINC. It executes
 * the callback script for the work unit (located in $BOINC_LOCATION/templates).
 *
 * Updated by AJY (ajy4490@umiacs.umd.edu) to support GT4 and BOINC 5.9.0.
 * updated by ALB (adam.bazinet@umiacs.umd.edu) subsequently.
 */
int assimilate_handler(WORKUNIT& wu, vector<RESULT>& results,
		RESULT& canonical_result) {
	SCOPE_MSG_LOG scope_messages(log_messages, SCHED_MSG_LOG::MSG_NORMAL);
	scope_messages.printf("[%s] Assimilating\n", wu.name);
	if (wu.canonical_resultid) {
		scope_messages.printf("[%s] Found canonical result\n", wu.name);
		log_messages.printf_multiline(SCHED_MSG_LOG::MSG_DEBUG,
				canonical_result.xml_doc_out, "[%s] canonical result", wu.name);

		vector<string> path;
		int retval = get_output_file_paths(canonical_result, path);
		if (retval) {
			log_messages.printf(SCHED_MSG_LOG::MSG_CRITICAL,
					"[RESULT#%d %s] handler: can't get output filenames\n",
					canonical_result.id, canonical_result.name);
			return -1;
		}
		string command_line;
		int i;
		for (i = 0; i < path.size(); i++) {
			command_line += (path[i] + string(" "));
		}
/*
		string outfile = (string(config.upload_dir) + string("/")
				+ string(canonical_result.name));
*/
		string wu_name, batch_num;

		char major[128];
		char num[10];
		char minor[128];

		if (sscanf(wu.name, "%[0-9].%[0-9_a-z].%[0-9]", major, minor, num) == 3) {
			// Matched 3, so we know it's a batch.

			char buf[128];

			scope_messages.printf("BATCH JOB\n");

			sprintf(buf, "%s.%s", major, minor);
			wu_name = string(buf);

			// Gets batch number (last .*) to buf, then to batch_num.
			sprintf(buf, "%s", num);
			batch_num = string(buf);
		} else {
			// Single job, keep it simple.
			scope_messages.printf("Single Job\n");
			wu_name = string(wu.name);
			batch_num = "0";
		}
		scope_messages.printf("WU name: %s, BATCH: %s\n", wu_name.c_str(),
				batch_num.c_str());

		ostringstream strm;
		strm << canonical_result.granted_credit;

		string mainstring ("main");
		string scriptname (wu_name);
		string mainwunumber ("0");
		std::size_t mainfound = wu_name.find(mainstring);
		if (mainfound != std::string::npos) {
			log_messages.printf(SCHED_MSG_LOG::MSG_DEBUG,
					"workunit name contains 'main': %s\n", wu_name.c_str());
			scriptname = wu_name.substr(0, (mainfound + 4));
			mainwunumber = wu_name.substr(mainfound + 5);
		}

		char *boinc_dir = getenv("BOINC_LOCATION");
		string script = (string(boinc_dir) + string("/templates/boinc_script.")
			+ scriptname + string(" ") + batch_num + string(" ") + strm.str()
			+ string(" ") + mainwunumber + string(" ") + command_line);

		// Call the Perl script.
		log_messages.printf(SCHED_MSG_LOG::MSG_DEBUG, "calling perl script: %s\n",
				script.c_str());

		system(script.c_str());
/*
		string wu_name = config.upload_dir;
		wu_name.append("/../wu.");
		wu_name.append(wu.name);

		string ru_name = config.upload_dir;
		ru_name.append("/../ru.");
		ru_name.append(wu.name);

		unlink(wu_name.c_str());
		unlink(ru_name.c_str());
*/
	} else {
		scope_messages.printf("[%s] No canonical result\n", wu.name);
	}
	if (wu.error_mask & WU_ERROR_COULDNT_SEND_RESULT) {
		log_messages.printf(SCHED_MSG_LOG::MSG_CRITICAL,
				"[%s] Error: couldn't send a result\n", wu.name);
		return wu.error_mask;
	}
	if (wu.error_mask & WU_ERROR_TOO_MANY_ERROR_RESULTS) {
		log_messages.printf(SCHED_MSG_LOG::MSG_CRITICAL,
				"[%s] Error: too many error results\n", wu.name);
		// ALB -- this case sneaks through occasionally; let's not cause the
		// assimilator to exit
//		return wu.error_mask;
		return 0;
	}
	if (wu.error_mask & WU_ERROR_TOO_MANY_TOTAL_RESULTS) {
		log_messages.printf(SCHED_MSG_LOG::MSG_CRITICAL,
				"[%s] Error: too many total results\n", wu.name);
//		return wu.error_mask;
		return 0;
	}
	if (wu.error_mask & WU_ERROR_TOO_MANY_SUCCESS_RESULTS) {
		log_messages.printf(SCHED_MSG_LOG::MSG_CRITICAL,
				"[%s] Error: too many success results\n", wu.name);
//		return wu.error_mask;
		return 0;
	}
	return 0;
}
