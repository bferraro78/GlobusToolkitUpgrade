<?php
require_once('settings.php');
/*
 * The Gate Keeper prevents values that don't yet exist
 * In the Random Forest Object from being used in Estimates
 */

class GateKeeper {
	private static $allowedValues = array();
	private static $defaultValuesToCheck = array("datatype", "ratematrix", "statefrequencies");

	//Expects an array of database columns to get allowed values for
	//These allowed values will end up in the static $allowedValues
	static function fillAllowedValues(){
		$values = GateKeeper::$defaultValuesToCheck;
		global $mysql_host, $mysql_user, $mysql_pass, $mysql_db;
		$con = mysql_connect($mysql_host, $mysql_user, $mysql_pass);
		if(!$con){
			print("Error: No DB Connection!\n");
		}
		mysql_select_db($mysql_db, $con);
		foreach($values as $val){
			GateKeeper::$allowedValues[$val] = array();
			//print("SELECT DISTINCT ".$val." FROM garli LEFT JOIN ( portal_job ) ON ( garli.portal_job_id = portal_job.id ) WHERE created < DATE_SUB( NOW(), INTERVAL 7 DAY) AND profiled = 1\n");
			$results = mysql_query("SELECT DISTINCT ".$val." FROM garli LEFT JOIN ( portal_job ) ON ( garli.portal_job_id = portal_job.id ) WHERE created < DATE_SUB( NOW(), INTERVAL 7 DAY) AND profiled = 1", $con);
			if($results){
				while($v = mysql_fetch_array($results)){
					GateKeeper::$allowedValues[$val][] = $v[$val];
				}
			}else{
				print("Error:No results from query!\n");
			}
		}
		mysql_close($con);
	}

	static function isAllowed($cat, $val){
		if($val != null && in_array($val, GateKeeper::$allowedValues[$cat])){
			return true;
		}else{
			return false;
		}
	}

	//This function is mostly done based on manual cases
	//and is heavily based off similar java code
	//don't be surprised if you have to rewrite it (it's pretty scary)
	static function cleanMultiEstimate(&$multi){

		GateKeeper::fillAllowedValues();

		$maxdatatype = count($multi->datatype);
		for($i=0; $i<$maxdatatype; $i++){
			if(!GateKeeper::isAllowed('datatype', $multi->datatype[$i])){
				$multi->datatype[$i] = 'aminoacid';
			}
		}

		$maxratematrix = count($multi->ratematrix);
		for($i=0; $i<$maxratematrix; $i++){
			if(!GateKeeper::isAllowed('ratematrix', $multi->ratematrix[$i])){
				if($multi->datatype[$i] == 'nucleotide'){
					$multi->ratematrix[$i] = '6rate'; 
				}else if($multi->datatype[$i] == 'aminoacid'){
					$multi->ratematrix[$i] = 'dayhoff';
				}else{
					$multi->ratematrix[$i] = '2rate';
				}
			}
		}

		$maxstatefrequencies = count($multi->statefrequencies);
		for($i=0; $i<$maxstatefrequencies; $i++){
			if(!GateKeeper::isAllowed('statefrequencies', $multi->statefrequencies[$i])){
				if($multi->datatype[$i] == 'nucleotide'){
					$multi->statefrequencies[$i] = 'estimate'; 
				}else if($multi->datatype[$i] == 'aminoacid'){
					$multi->statefrequencies[$i] = 'dayhoff';
				}else{
					$multi->statefrequencies[$i] = 'empirical';
				}
			}
		}

		$maxnumratecats = count($multi->numratecats);
		for($i=0; $i<$maxnumratecats; $i++){
			if($multi->numratecats[$i] != null){
				if($multi->datatype[$i] == 'nucleotide' || $multi->datatype[$i] == 'aminoacid'){
					$multi->numratecats[$i] = '4';
				}else{
					$multi->numratecats[$i] = '1';
				}
			}
		}

		$maxinvariantsites = count($multi->invariantsites);
		for($i=0; $i<$maxinvariantsites; $i++){
			if($multi->invariantsites[$i] != null){
				if($multi->datatype[$i] == 'nucleotide' || $multi->datatype[$i] == 'aminoacid'){
					$multi->invariantsites[$i] = 'estimate';
				}else{
					$multi->invariantsites[$i] = 'none';
				}
			}
		}

	}
}
?>