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
	
$query_string = "select id,runtime,created from portal_job where (created <= '$end_date'  and created > '2012-07-19') and (runtime != 'NULL' and runtime != 0);";
$result = mysql_query($query_string) or die("Select failed!");

while($row = mysql_fetch_array($result)) {
  $portal_job_array[] = $row;
  $id = $row['id'];

  //print "id: $id\n";

  $query_string2 = "select runtime_estimate, searchreps, bootstrapreps from job where portal_job_id = " . $id . " limit 1;";
  $result2 = mysql_query($query_string2) or die("Select failed!");
  $row2 = mysql_fetch_array($result2);
  $grid_job_array[] = $row2;
}

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
      print $runtime_estimate . "\t" . $runtime . "\n";
    }
  }
 }

?>
