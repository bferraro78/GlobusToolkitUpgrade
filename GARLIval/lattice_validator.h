#include <vector>
#include "boinc_db.h"
using namespace std;

int init_result(RESULT const & result, void*& data) ;

int compare_results(
    RESULT & r1, void* data1, RESULT const& r2, void* data2, bool& match) ;

int cleanup_result(RESULT const& /*result*/, void* data) ;
