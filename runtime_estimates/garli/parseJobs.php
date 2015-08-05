<?php
require_once('estimate.php');
require_once('runtimeScaling.php');
require_once('garliEstimate.php');
require_once('settings.php');
require_once('logger.php');

/*
 *We're going to assume that all jobs are garli jobs
 *In the future, we'll need a way to figure out which jobs belong to which service
 */

//Are we running from CLI?

define('CLI', PHP_SAPI==='cli' && !in_array('-html', $argv));
//This switch bypasses an optimization
define('AGELESS', PHP_SAPI==='cli' && in_array('-ageless', $argv));
//Should we debug?
if(!CLI && $_GET['debug']=='false'){
	unset($_GET['debug']);
}
define('DBG', isset($_GET['debug']) || (in_array('-debug', $argv) || isset($_GET['debug'])));

//Our logger
$log = new logger();
$log->init();
//Setup Directory and Job arrays
$dirs = array();
$dirsRaw = array();
$jobs = array();
//Scan directory and grab all the children we care about
$ids = array();
if(false !==($dh = opendir($estimates_location))){
	while($file = readdir($dh)){
		if(filetype($estimates_location . $file) == 'dir'){
			if($file != '.' && $file!='..'){
				$dirsRaw[] = $estimates_location . $file;
				$ids[] = $file;
			}
		}
	}
	sort($ids);
	$minId = $ids[0];
	$minId = str_replace('/', '',$minId);
	
	$conn = mysql_connect($mysql_host, $mysql_user, $mysql_pass);
	$conn = mysql_select_db($mysql_db, $conn);
	$results = mysql_query("SELECT MIN(id) AS id FROM portal_job WHERE profiled!=1 AND id>=$minId AND status=10");
	$lowerBound = '0/';
	if($results != false && !AGELESS){
		while($v = mysql_fetch_array($results)){
			$lowerBound = $v['id'];
	       	}
	}
	foreach($dirsRaw as $raw){
		if($dirsRaw>$lowerBound){
			$dirs[] = $raw;
		}
	}
}
if(AGELESS){
    $dirs = $dirsRaw;
}

$log->debug("Directories: ");
$log->debug(print_r($dirs, true));
$log->notify('-----Starting to parse jobs '.time().' -------');

//For each directory, make sure it passes some basic tests and turn it into a parsed class
if(count($dirs)>0){
	foreach($dirs as $di){
		if(file_exists($di.'/garli.conf') && file_exists($di.'/garli.screen.log')){
			$est = new garliEstimate();
			//Get our id, set our id, and see if we've been parsed
			$id = explode('/', $di);
			$id = $id[count($id)-1];
			$est->setId($id);
			if(!($est->isParsed())){
				//if we haven't been parsed, parse and add to our list
			    $est->parse($di.'/garli.conf', $di.'/garli.screen.log');
				//and of course, mark that it's parsed
				if($est->verify()){
					$est->setParsed();
					$jobs[] = $est;
				}else{
					$log->warn("Failed to Verify!");
					$log->warn(print_r($est, TRUE));
				}
			}
		}else{
			$log->warn("Bad Directory: $di");
		}
	}

	//scaling
	$scaler = new runtimeScaling();
	$scaler->getResources();
	foreach($jobs as $job){
		$scaler->scaleRuntime($job);
		if(!$job->verify()){
			$job->profiled=false;
			$log->warn("Job ".$job->unique_id." scaled to 0! IP Change detected!");
		}
		$job->save();
	}
	
}

	$log->debug(print_r($jobs, true));
	$log->notify('-----Finished parsing jobs '.time().' -------');
	$log->close();

?>
