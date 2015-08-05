#!/usr/bin/php

<?
mysql_pconnect("latticedb.umiacs.umd.edu","gt4admin","Daim7juz")
     or die("Unable to connect to SQL server");

mysql_select_db("grid_production") 
     or die("Unable to select database");

$today = date('Y-m-d');
$query_string = "select id,runtime from portal_job where (created <= '$today'  and created > '2012-06-01') and runtime != 'NULL';";
$result = mysql_query($query_string) or die("Select failed!");
$difference = 0;
$count = 0;
while($row = mysql_fetch_array($result)) {
  $id = $row['id'];
  $runtime = $row['runtime'];
  $query_string2 = "select runtime_estimate from job where portal_job_id = " . $id . " limit 1;";
  $result2 = mysql_query($query_string2) or die("Select failed!");
  $runtime_estimate = mysql_result($result2, 0);
  if($runtime_estimate > 0 && $runtime > 0) {
    $difference = $difference + abs($runtime_estimate - $runtime);
    $count = $count + 1;
  }
 }

$average_difference = intval($difference / $count);

echo "average difference between runtime estimate and actual runtime: $average_difference\n";
exec("echo \"$average_difference\" >> /export/work/globus-4.2.1/runtime_estimates/garli/convergence.txt");

?>