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
define('CLI', true);
//Should we debug?
define('DBG', true);
define('ACTIVATE', in_array('-activate', $argv));

//Our logger
$log = new logger();
$log->init();
//Setup Directory and Job arrays
$dirs = array();
$jobs = array();
//Scan directory and grab all the children we care about
$ids = array();
if($dh = opendir($estimates_location)){
	while($file = readdir($dh)){
		if(filetype($estimates_location . $file) == 'dir'){
			if($file != '.' && $file!='..'){
				$dirs[] = $estimates_location . $file;
			}
		}
	}
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

		}else{
			$log->warn("Bad Directory: $di");
		}
	}

	//scaling
	$scaler = new runtimeScaling();
	$scaler->getResources();
	foreach($jobs as $job){
		$scaler->forceScaleRuntime($job);
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
