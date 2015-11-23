/**
 * File: seg_boinc_module.c
 * Author: Andrew J Young (ajy4490@umiacs.umd.edu)
 * Author: Adam Bazinet (adam.bazinet@umiacs.umd.edu)
 * Description:
 * Scheduler Event Generator Module for the BOINC Job Manager. This will monitor
 * the job state by accessing the BOINC MySQL database as well as GSBL's job
 * database and send notifications to Globus when the status changes. This is
 * different form most other SEGs, which sift through log files instead.
 *
 * Update ALB/2014: For GARLI jobs with workphasedivision = 1, only consider a
 * job complete if the final output files are present; if they are not, prepare
 * and execute the apprpriate create_work call.
 **/

// Std includes.
#include <stdio.h>
//#include <stdlib.h>
//#include <unistd.h>
//#include <string.h>
#include <math.h>
// MySQL and C++ std includes.
#include <mysql.h>
#include <string>
#include <vector>
// XML includes.
#include <libxml/xmlmemory.h>
#include <libxml/parser.h>
//#include <time.h>

// Borrowed from boinc_db.h.
#define RESULT_SERVER_STATE_INACTIVE   	1
#define RESULT_SERVER_STATE_UNSENT     	2
#define RESULT_SERVER_STATE_IN_PROGRESS	4
#define RESULT_SERVER_STATE_OVER       	5

#define RESULT_OUTCOME_INIT        	0
#define RESULT_OUTCOME_SUCCESS     	1
#define RESULT_OUTCOME_COULDNT_SEND	2
#define RESULT_OUTCOME_CLIENT_ERROR	3  // An error happened on the client.
#define RESULT_OUTCOME_NO_REPLY    	4
#define RESULT_OUTCOME_DIDNT_NEED  	5
// We created the result but didn't need to send it because we already got a
// canonical result for the WU.
#define RESULT_OUTCOME_VALIDATE_ERROR 	6
// The outcome was initially SUCCESS, but the validator had a permanent error
// reading a result file, or the result file had a syntax error.
#define RESULT_OUTCOME_CLIENT_DETACHED	7

// Job states.
#define FAILED 	4
#define DONE   	3
#define ACTIVE 	2
#define PENDING	1

using namespace std;

// Cheap header. :-P
int parseBoincDbInfo(void);
int connectToDbs(void);
int getWorkunitState(int, int, int);
bool findStatus(vector<int>, int);
int countStatus(vector<int>, int);

MYSQL *mySqlBoincDb;  // BOINC MySQL db.
MYSQL *mySqlGridDb;  // Grid MySQL db.
FILE *fp;  // File pointer for debugging log.
char *host, *user, *pass, *db;  // BOINC db information.

int parseBoincDbInfo(void) {
	// Find the config file.
	char const *boincLoc = "/fs/mikedata/arginine/work/boinc_projects";//getenv("BOINC_LOCATION");  // JTK: Was returning (NULL).
	char configFile[128];
	sprintf(configFile, "%s/config.xml", boincLoc);
	fprintf(fp, "File: %s\n", configFile);
	// Open up the config file for parsing.
	xmlDocPtr doc = xmlParseFile(configFile);
	if (doc == NULL) {
		fprintf(fp, "ERROR: Unable to parse config.xml\n");
		return 1;
	}
	// Set the cur pointer.
	xmlNodePtr cur = xmlDocGetRootElement(doc);
	if (cur == NULL) {
		fprintf(fp, "ERROR: config.xml looks to be empty!\n");
		return 1;
	}

	// Make sure root node is <boinc>.
	if (xmlStrcmp(cur->name, (const xmlChar *)"boinc")) {
		fprintf(fp, "ERROR: root node is not <boinc>\n");
		fprintf(fp, "It is: %s\n", (char *)cur->name);
		return 1;
	}

	// Go into the child node node.
	cur = cur->xmlChildrenNode;
	// Loop in case the first child node isn't <config>. (It should be, but we
	// cannot make that assumption.)
	while (cur != NULL) {
		if (!xmlStrcmp(cur->name, (const xmlChar *)"config")) {
			// Drill in a level.
			xmlNodePtr cur2 = cur->xmlChildrenNode;
			// Scan through and try to find <db_host>, <db_user>, <db_passwd>, and
			// <db_name>. Once found, set the corresponding global pointers so they
			// can be used to connect to the database.
			while (cur2 != NULL) {
				if (!xmlStrcmp(cur2->name, (const xmlChar *)"db_host")) {
					host = (char *)xmlNodeListGetString(doc, cur2->xmlChildrenNode, 1);
					sscanf(host, "%s", host);	 // Trim off white space.
				} else if (!xmlStrcmp(cur2->name, (const xmlChar *)"db_user")) {
					user = (char *)xmlNodeListGetString(doc, cur2->xmlChildrenNode, 1);
					sscanf(user, "%s",user);
				} else if (!xmlStrcmp(cur2->name, (const xmlChar *)"db_passwd")) {
					pass = (char *)xmlNodeListGetString(doc, cur2->xmlChildrenNode, 1);
					sscanf(pass, "%s", pass);
				} else if (!xmlStrcmp(cur2->name, (const xmlChar *)"db_name")) {
					db = (char *)xmlNodeListGetString(doc, cur2->xmlChildrenNode, 1);
					sscanf(db, "%s", db);
				}
				cur2 = cur2->next;
			}
		}
		cur = cur->next;
	}
	xmlFreeDoc(doc);
	return 0;
}

int connectToDbs(void) {
	fp = fopen("/tmp/boinc_SEG_debug.log", "w");
	fprintf(fp, "Entered db connect function\n");

	// Get BOINC database info from XML file.
	if (parseBoincDbInfo()) {
		fprintf(fp, "Error parsing xml file\n");
		return 1;
	}
	mySqlBoincDb = mysql_init(NULL);
	// Connect to the BOINC database.
	if (!(mysql_real_connect(mySqlBoincDb, host, user, pass, db, 0, NULL, 0))) {
		fprintf(fp, "Error connecting to boinc db\n");
		fprintf(fp, "%s\n", mysql_error(mySqlBoincDb));
		fflush(fp);
		return 1;
	}

	// Read from the file and get the paramaters needed to connect. Get
	// $GSBL_CONFIG_DIR and use it to open the db.location file.
	// Changed from $GLOBUS_LOCATION -- BRF
	char const *gridLoc = "/opt/gsbl-config";//getenv("GSBL_CONFIG_DIR");  // JTK: Was returning (NULL).
	fprintf(fp, "%s\n", gridLoc);
	char buf[128];
	sprintf(buf, "%s/service_configurations/db.location", gridLoc);
	FILE *grid = fopen(buf, "r");
	if (grid == NULL) {
		fprintf(fp, "Error loading grid db file\n");
		return 1;
	}

	char gridHost[128];
	char gridUser[128];
	char gridPass[128];
	char gridDb[128];

	// Each line in the file represents the host, user, password, and db.
	fscanf(grid, "%s\n", gridHost);
	fscanf(grid, "%s\n", gridUser);
	fscanf(grid, "%s\n", gridPass);
	fscanf(grid, "%s\n", gridDb);

	fclose(grid);

	mySqlGridDb = mysql_init(NULL);
	// Connect to the grid database.
	if (!mysql_real_connect(mySqlGridDb, gridHost, gridUser, gridPass, gridDb, 0,
				NULL, 0)) {
		fprintf(fp, "Error connecting to grid db\n");
		fprintf(fp, "%s\n", mysql_error(mySqlGridDb));
		return 1;
	}
	return 0;
}

/**
 * Finds a single job within the grid database for BOINC, and returns its
 * status.
 */
int main(int argc, char **argv) {
	string uniqueId = argv[1];
  MYSQL_RES *jobResult;
	MYSQL_RES *workunitResult;
  MYSQL_ROW jobRow;
	MYSQL_ROW workunitRow;

	// Connect to both the BOINC and grid databases.
	if (connectToDbs()) {
		printf("Error: Connecting to databases failed.\n");
	} else {
		// Get the number of replicates of the active job from the grid db.
		string buf = ("SELECT replicates FROM job WHERE unique_id='" + uniqueId
				+ "'");

		if (mysql_query(mySqlGridDb, buf.c_str())) {
			fprintf(fp, "Error in query: %s\n", mysql_error(mySqlGridDb));
		}

		if (!(jobResult = mysql_store_result(mySqlGridDb))) {
			fprintf(fp, "Error in store: %s\n", mysql_error(mySqlGridDb));
		}
		fflush(fp);

		if (jobRow = mysql_fetch_row(jobResult)) {
			int replicates = atoi(jobRow[0]);

			if (replicates == 1) {  // Single job.
				string holder = ("SELECT id,name,assimilate_state,error_mask FROM workunit WHERE name='"
						+ uniqueId + "' OR name='" + uniqueId + "_initial' OR name='"
						+ uniqueId + "_main' OR name='" + uniqueId + "_final'");

				// Free job result.
				mysql_free_result(jobResult);

				// Debugging.
				fprintf(fp, "String: %s\n", holder.c_str());
				fflush(fp);
				if (mysql_query(mySqlBoincDb, holder.c_str())) {
					fprintf(fp, "%s\n", mysql_error(mySqlBoincDb));
				}
				workunitResult = mysql_store_result(mySqlBoincDb);
				if (workunitResult == NULL) {
					fprintf(fp, "Workunit result is null!\n");
				}

				// Get the status.
				if (workunitRow = mysql_fetch_row(workunitResult)) {
					int status = getWorkunitState(atoi(workunitRow[0]),
							atoi(workunitRow[2]), atoi(workunitRow[3]));

					printf("STATUS: %d\n", status);
				} else {
					printf("Error: Could not find job in the BOINC database.\n");
				}
				fflush(fp);
				mysql_free_result(workunitResult);
			} else if (replicates > 1) {  // Batch job.
				// Get all that are similar to our entry in job.
				// ALB 10/2014: Updating this query so that it also grabs workunits with
				// names ending in _final, but doesn't grab workunits ending in _initial
				// or _main_[1..n].
				string holder = ("SELECT id,name,assimilate_state,error_mask FROM workunit WHERE name LIKE '"
						+ uniqueId + "\%' AND name NOT LIKE '" + uniqueId
						+ "\%main\%' AND name NOT LIKE '" + uniqueId + "\%initial\%'");

				// ALB 09/2015: Turns out to properly assess the failed condition, we
				// need a version of the query that includes initial and main workunits.
				string holder2 = ("SELECT id,name,assimilate_state,error_mask FROM workunit WHERE name LIKE '"
						+ uniqueId + "\%'");

				// CODE FOR THE FIRST QUERY
				fprintf(fp, "Query 1: %s\n", holder.c_str());
				fflush(fp);
				if (mysql_query(mySqlBoincDb, holder.c_str())) {
					fprintf(fp, "Error: %s\n", mysql_error(mySqlBoincDb));
				}

				if ((workunitResult = mysql_store_result(mySqlBoincDb)) == NULL) {
					fprintf(fp, "Error: %s\n", mysql_error(mySqlBoincDb));
				}
				vector<int> batch;
				while ((workunitRow = mysql_fetch_row(workunitResult)) != NULL) {
					int status = getWorkunitState(atoi(workunitRow[0]),
							atoi(workunitRow[2]), atoi(workunitRow[3]));
					fprintf(fp, "batch: status of %s is %d\n", workunitRow[0], status);
					fflush(fp);
					// If there wasn't an error, add it to the vector.
					// ALB 11-29-14: Not sure you ever want to generate an event if status
					// is 0.
					if (status > 0) {
						batch.push_back(status);
					}
				}
				mysql_free_result(workunitResult);

				// CODE FOR THE SECOND QUERY.
				fprintf(fp, "Query 2: %s\n", holder2.c_str());
				fflush(fp);
				if (mysql_query(mySqlBoincDb, holder2.c_str())) {
					fprintf(fp, "Error: %s\n", mysql_error(mySqlBoincDb));
				}

				if ((workunitResult = mysql_store_result(mySqlBoincDb)) == NULL) {
					fprintf(fp, "Error: %s\n", mysql_error(mySqlBoincDb));
				}
				vector<int> batch2;
				while ((workunitRow = mysql_fetch_row(workunitResult)) != NULL) {
					int status = getWorkunitState(atoi(workunitRow[0]),
							atoi(workunitRow[2]), atoi(workunitRow[3]));
					fprintf(fp, "batch: status of %s is %d\n", workunitRow[0], status);
					fflush(fp);
					// If there wasn't an error, add it to the vector.
					// ALB 11-29-14: Not sure you ever want to generate an event if
					// status is 0.
					if (status > 0) {
						batch2.push_back(status);
					}
				}
				mysql_free_result(workunitResult);

				// Look through the batch for different states, starting with the
				// highest priority first, working downward.
				// ALB Sep-14-2015: Modifying this so finished workunits are checked
				// first, and increasing the number of failures that can be tolerated.
				if (batch2.size() > 0) {
					if (findStatus(batch, DONE)) {
						// Only return DONE if the number of workunits complete is greater
						// than or equal to the number of replicates; in the case of
						// _final workunits, not all _final workunits may have been
						// created yet.
						fprintf(fp, "batch.size(): %d  replicates: %d\n", batch.size(),
								replicates);
						fflush(fp);
						if (countStatus(batch, DONE) >= replicates) {
							printf("STATUS: %d\n", DONE);
						}
					} else if (findStatus(batch2, FAILED)) {
						// Only return FAILED if the number of failed workunits is greater
						// than the number of extra workunits we have submitted.
						// Using floor to be conservative.
						// 0.8, below, represents oversubmission of 125%; this should be
						// kept in sync with the $oversubmit variable in boinc.pm.
						if (countStatus(batch2, FAILED) > (floor(replicates / 0.8)
									- replicates)) {
							printf("STATUS: %d\n", FAILED);
						}
					} else if (findStatus(batch2, ACTIVE)) {
						printf("STATUS: %d\n", ACTIVE);
					} else if (findStatus(batch2, PENDING)) {
						printf("STATUS: %d\n", PENDING);
					}
				}
				fflush(fp);
				mysql_free_result(jobResult);
			}
		} else {
			printf("Error: Could not find job in the Grid database.\n");
		}
	}
	return 0;
}

/**
 * Simple function for finding a value within a vector.
 * Returns true if the status is found, false if not.
 */
bool findStatus(vector<int> vec, int status) {
	for (int i = 0; i < vec.size(); i++) {
		if (vec[i] == status) {
			return true;
		}
	}
	return false;
}

/**
 * Simple function for counting number of elements within a vector whose status
 * matches that given as input.
 */
int countStatus(vector<int> vec, int status) {
	int statusCount = 0;
	for (int i = 0; i < vec.size(); i++) {
		if (vec[i] == status) {
			statusCount++;
		}
	}
	return statusCount;
}

/**
 * Examines the workunit and corresponding result unit entries in the BOINC
 * database to determine the correct state for the given workunit for the MYSQL
 * row.
 */
int getWorkunitState(int id, int assimState, int errorMask) {
	MYSQL_RES *result;
	MYSQL_ROW row;
	int status = 0;
//	fprintf(fp, "Received id: %d\n", id);
	// Check to see if the state is already known from the WU.
	if (errorMask != 0) {
		// Job failed.
		status = 4;
	} else if (assimState == 2) {
		// Job is done!
		status = 3;
	} else {
		// Need to look at the result units' state and outcome.
		char buf[256];
		sprintf(buf, "SELECT server_state,outcome FROM result WHERE workunitid=%d",
				id);
		fprintf(fp, "String: %s\n", buf);
		fflush(fp);
		if (mysql_query(mySqlBoincDb, buf)) {
			fprintf(fp, "Query Error: %s\n", mysql_error(mySqlBoincDb));
			return -1;
		}
//		fprintf(fp, "About to store and compare results\n");
		if (!(result = mysql_store_result(mySqlBoincDb))) {
			fprintf(fp, "Store Error: %s\n", mysql_error(mySqlBoincDb));
			return -1;
		}
		while ((row = mysql_fetch_row(result)) != NULL) {
			int serverState = atoi(row[0]);
			int outcome = atoi(row[1]);
			fprintf(fp, "Received state: %d and outcome: %d\n", serverState, outcome);
			if ((serverState == RESULT_SERVER_STATE_IN_PROGRESS)
					|| (serverState == RESULT_SERVER_STATE_OVER)) {
				// It's active.
				status = 2;
				break;
/*			} else if ((serverState == RESULT_SERVER_STATE_OVER)
					&& (outcome == RESULT_OUTCOME_CLIENT_ERROR)
					&& (status != 1)) {
				// This result failed, but there could be more that are active let's just
				// use the workunit error mask for the time being, this particular
				// mechanism might be overzealous.
//				status = 4;
*/
			} else {
				// It's neither active nor failed, so keep its status set to pending.
				status = 1;
			}
		}
		mysql_free_result(result);
	}

//	mysql_free_result(result);
	return status;
}
