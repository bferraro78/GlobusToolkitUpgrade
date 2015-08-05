<?php
require_once('garliMultiEstimate.php');
require_once('gatekeeper.php');
require_once('settings.php');
require_once('logger.php');

error_reporting(1); //supress anything except true errors

$log = new logger();
$gme = new garliMultiEstimate();
$log->init();
$log->loud = false; //set to true if you need debugging
$path = './';
$estimator = './estimate_garli_runtime.pl';
define(CLI, true); //If this assertion is wrong, then something has gone very, very wrong
define(DBG, true); //Verbose logging
if($loc = array_search('-confPath', $argv)){
	$path = $argv[$loc+1]; //Presume the given path comes after the -path argument
	$path = str_replace(array('"', '\\'), '', $path); //clean the path to be a valid php file path
}

if($loc = array_search('-dataf', $argv)){
	$dataf = $argv[$loc+1]; //Presume the given path comes after the -path argument
	$dataf = str_replace(array('"', '\\'), '', $dataf); //clean the path to be a valid php file path
}


//Figure out the correct file
$log->debug('Incoming DataF: '.$dataf);
$files = explode(',', $dataf);
$log->debug('Files:'.$files);
$dataf = null;
$possibilities = array();
if(count($files)==1){
	$dataf = $files[0]; //Only have one option, it must be this option
}else{
	foreach($files as $file){
		if($file!=null && $file!=''){
			if(strpos('.tre',$file)===false){
				//not a tree file
				$possibilities[] = $file;
			}
			if(strpos('.nex', $file)!==false){
				$dataf = $file;
				break;
			}
		}
	}
	if(count($possibilities)==1){
		$dataf = $possibilities[0];
	}
}

if(!isset($dataf) || $dataf == ''){
	$log->error("Couldn't figure out Datafile!");
	die(1); //die with a general error
}

$log->debug('Discovered dataf:'.$dataf);
//Clean up our file
$lastindex = strripos($dataf, '/');
$log->debug('Last Index:'.$lastindex);
$dataf = substr($dataf, 0, $lastindex);
$dataf = '/export/grid_files/cache/'.$dataf;
$log->debug('Finalized dataf:'.$dataf);
chdir($dataf);
$cwd = getcwd();
//Parse and clean

$gme->parse($path.'/');
GateKeeper::cleanMultiEstimate($gme);
$gme->constructEstimateLines();
chdir($_ENV["GLOBUS_LOCATION"] . '/runtime_estimates/garli');
//run lines and sum estimates
$sum = 0;
$num = 0;
foreach($gme->estimateLines as $line){
	$estimate = trim(exec($estimator . ' ' . $line));
	$sum += $estimate;
	$num++;
}
$sum = intval($sum/$num);
print($sum);

?>