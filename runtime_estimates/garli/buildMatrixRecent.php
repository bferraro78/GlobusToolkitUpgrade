<?php
require_once('settings.php');
require_once('logger.php');
require_once('estimate.php');
require_once('garliEstimate.php');

define('CLI', PHP_SAPI==='cli' && !in_array('-html', $argv));
//Should we debug?
if(!CLI && $_GET['debug']=='false'){
	unset($_GET['debug']);
}
define('DBG', isset($_GET['debug']) || (in_array('-debug', $argv) || isset($_GET['debug'])));

//Our logger
$log = new logger();
$log->init();

$log->notify('------Starting to build matrix '.time().' ------');
$estimates = array();
$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
mysql_select_db($mysql_db, $conn);

$results = mysql_query('SELECT * FROM portal_job WHERE profiled = 1 AND created > "2013-07-30 00:00:00"', $conn); # this is the date significant scheduling changes were made
if($results != false){
	while($v = mysql_fetch_array($results)){
		$estimates[] = new garliEstimate();
		$estimates[count($estimates)-1]->unique_id = $v['id'];
		$estimates[count($estimates)-1]->build();
	}
}

$outputLines = array();

foreach($estimates as $est){
  if($est->verify() && ($est->resolveResourceType() != "BOINC")) {
    $outputLines[] = $est->returnMatrixLine();
  }
}

$fhandle = fopen('output_lines_recent', 'w');
foreach($outputLines as $line){
	fwrite($fhandle, $line);
}
fclose($fhandle);

shell_exec("cat garli_header output_lines_recent > matrix_recent");
shell_exec("R --vanilla --slave -f updateMatrixRecent.r");

$log->notify('------Finished building matrix '.time().' ------');
$log->close();

?>
