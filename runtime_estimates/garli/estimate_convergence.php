#!/usr/bin/php

<?
mysql_pconnect("latticedb.umiacs.umd.edu","gt4admin","Daim7juz")
     or die("Unable to connect to SQL server");

mysql_select_db("grid_production") 
     or die("Unable to select database");

$today = date('Y-m-d');

// Start date
//$date = '2012-07-20';
$date = $today;
// End date
$end_date = $today;
	
$query_string = "select id, runtime, created from portal_job where (created <= '$end_date' and created > '2012-07-19') and (runtime != 'NULL' and runtime != 0);";
$result = mysql_query($query_string) or die("Select failed!");

while($row = mysql_fetch_array($result)) {
  $portal_job_array[] = $row;
  $id = $row['id'];

  $query_string2 = "select runtime_estimate, searchreps, bootstrapreps from job where portal_job_id = " . $id . " limit 1;";
  $result2 = mysql_query($query_string2) or die("Select failed!");
  $row2 = mysql_fetch_array($result2);
  $grid_job_array[] = $row2;
}

//exec("rm /export/work/globus-4.2.1/runtime_estimates/garli/convergence_folddifference.txt");
//exec("rm /export/work/globus-4.2.1/runtime_estimates/garli/convergence_reldiff.txt");

/*	while (strtotime($date) <= strtotime($end_date)) {
		echo "$date\n";
		$date = date ("Y-m-d", strtotime("+1 day", strtotime($date))); */

$total_foldchange = 0;
$total_reldiff = 0;
$count = 0;
               
		for($i = 0; $i < count($portal_job_array); $i++) {
		
			$portalrow = $portal_job_array[$i];
			$runtime = $portalrow['runtime'];
			$created = $portalrow['created'];

                        if(strtotime($created) <= strtotime($date) && strtotime($created) > strtotime('2012-07-19')) {
               
                        $gridrow = $grid_job_array[$i];

			  $runtime_estimate = $gridrow['runtime_estimate'];
			  $searchreps = $gridrow['searchreps'];
			  $bootstrapreps = $gridrow['bootstrapreps'];

  $divide_by = 1;
  if(isset($searchreps) && $searchreps != "" && $searchreps > $divide_by) {
    $divide_by = $searchreps;
  }
  if(isset($bootstrapreps) && $bootstrapreps != "" && $bootstrapreps > $divide_by) {
    $divide_by = $bootstrapreps;
  }
  $runtime_estimate = $runtime_estimate / $divide_by;
  
  if($runtime_estimate > 0 && $runtime > 0) {
    # calculate fold-change
    if($runtime_estimate >= $runtime) {
      $foldchange = $runtime_estimate / $runtime;
    } else {
      $foldchange = $runtime / $runtime_estimate;
    }
    
    # calculate relative difference
    $reldiff = abs($runtime_estimate - $runtime) / max($runtime_estimate, $runtime);

    #print "runtime estimate is: $runtime_estimate  actual runtime: $runtime  foldchange is: $foldchange  reldiff is: $reldiff\n";

    $total_foldchange = $total_foldchange + $foldchange;
    $total_reldiff = $total_reldiff + $reldiff;
    
    $count = $count + 1;
  }
}
}

$average_foldchange = $total_foldchange / $count;
$average_reldiff = $total_reldiff / $count;

echo "average foldchange: $average_foldchange  average relative difference: $average_reldiff\n";
exec("echo \"$average_foldchange\" >> /export/work/globus-4.2.1/runtime_estimates/garli/convergence_folddifference.txt");
exec("echo \"$average_reldiff\" >> /export/work/globus-4.2.1/runtime_estimates/garli/convergence_reldiff.txt");

//}


?>