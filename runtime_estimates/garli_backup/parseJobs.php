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

define('SINGLE', in_array('-single', $argv));
if(SINGLE){
	$singleId = $argv[array_search('-single', $argv)+1]; 
	$single = $argv[array_search('-single', $argv)+1];
	if(!$single || !file_exists($estimates_location . $single)){
		print('Specified directory does not exist: '.$estimates_location.$single);
		die(1);
	}
}

if(!isset($cutoff)){
	//assume 4200 for production
	$cutoff = 4200;
}

//Our logger
$log = new logger();
$log->init();
//Setup Directory and Job arrays
$dirs = array();
$dirsRaw = array();
$jobs = array();
$ids = array();

$log->debug('Max Memory: '.ini_get('memory_limit'));
$log->debug('Max Execution Time: '.ini_get('max_execution_time'));

//If this isn't a single job, then we want to scan and grab all directories
if(!SINGLE){
	//Scan directory and grab all the children we care about
	$con = mysql_connect($mysql_host, $mysql_user, $mysql_pass);
	mysql_select_db($mysql_db, $con);
	//Only selecting jobs where id is greater than 4200 to avoid older jobs where data has not been captured
	$results = mysql_query("SELECT id FROM portal_job WHERE id > ".$cutoff." AND status = 10 AND profiled IS NULL", $con);
	if($results != false){
		while($result = mysql_fetch_array($results)){
			$dirsRaw[] = $result['id'];
		}
	}else{
		$log->notify('Query returned nothing!');
	}
	foreach($dirsRaw as $dir){
		if(file_exists($estimates_location.$dir) && is_dir($estimates_location.$dir)){
			$dirs[] = $dir;
		}else{
			$log->error($estimates_location . $id.' does not exist as a directory.');
		}
	}
}else{ //Otherwise, push our single directory to the $dirs stack
	$dirs[] = $estimates_location . $single;
	$ids[] = $singleId;
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
		if($job->isMulti()){
			$job->profiled=false;
			$log->warn("Job ".$job->unique_id." is a multi-partition job!");
		}
		$job->save();
	}
	
}

	$log->debug(print_r($jobs, true));
	$log->notify('-----Finished parsing jobs '.time().' -------');
	$log->close();

?>
