<?php
require('settings.php');
require_once('logger.php');

class estimate {

	public $unique_id=-1;
	public $mem_used;
	public $profiled=false;
	public $runtime;
	public $log;

	function __construct(){
		$this->log = new logger();
		$this->log->init();
	}

	function __destruct(){
		$this->log->close();
	}

	function getResourceId(){
		$jobId = $this->unique_id;
		global $mysql_host, $mysql_user, $mysql_db, $mysql_pass;
		$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
		mysql_select_db($mysql_db, $conn);
		$results = mysql_query("SELECT * FROM job WHERE portal_job_id = $jobId AND portal_job_index = 0", $conn);
		if($results != false){
			while($v = mysql_fetch_array($results)){
				$ret = $v['scheduler'];
				$ret = explode(':', $ret);
				$ret = $ret[1];
				$ret = str_replace('/',' ', $ret);
				$ret = trim($ret);

				if($ret == '128.8.141.68'){ // lysine (UMIACS Condor pool)
				  $ret = '128.8.141.67'; // leucine (UMIACS Condor pool)
					$this->log->notify('Accounted for IP Shift: 128.8.141.68 -> 128.8.141.67');
				}
				return $ret;
			}
		}else{
			$this->log->error("Unable to resolve Resource ID on ".$this->unique_id);	
			return null;
		}
	}
	function resourceIdForceResolve(){
		$jobId = $this->unique_id;
		global $mysql_host, $mysql_user, $mysql_db, $mysql_pass;
		$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
		mysql_select_db($mysql_db, $conn);
		$rs1 = mysql_query("SELECT * FROM job WHERE portal_job_id = $jobId", $conn);
		$rs1r = mysql_fetch_array($rs1);
		$job_id = $rs1r['id'];
		$job_user = $rs1r['user_id'];
		$job_name = $rs1r['job_name'].'_estimate';
		$rs2 = mysql_query("SELECT * FROM job WHERE job_name = \"$job_name\" AND user_id = $job_user");
		if($rs2 != false){
				$v = mysql_fetch_array($rs2);
				$v2 = mysql_fetch_array($rs2);
				if($v && $v2){
					
					$this->log->warn('Ambiguity on Resource ID '.$this->unique_id.', attempting to seek smallest delta.');
					$answers = array($v, $v2);
					
					while($q = mysql_fetch_array($rs2)){
						$answers[] = $q;
					}
					
					//array_multisort($answers['id'], SORT_NUMERIC, SORT_ASC);
					$considered = array();
					$deltas = array();
					foreach($answers as $answer){
						$this->log->notify('Answer: '.$answer['id']);
						$possible = array();
						$possible['delta'] = abs($answer['id']-$job_id);
						$deltas[] = $possible['delta'];
						$possible['id'] = $answer['id'];
						$possible['scheduler'] = $answer['scheduler'];
						$this->log->notify('Answer Delta: '.$possible['delta']);
						$considered[] = $possible;
					}
					if(array_unique($deltas)!=$deltas){
						$this->log->error('Multiple Equal Deltas Detected. Aborting.');
						$considered=null;
						$v=null;
					}else{
						$delta = 100000;
						$solution =  null;
						foreach($considered as $possibility){
							if($possibility['delta']<$delta){
								$delta = $possibility['delta'];
								$solution = $possibility;
							}
						}

						$this->log->notify('Solution Found. Delta:'.$delta.' Solution:'.$solution['id']);
						$v = $solution;
					}

				}

				$this->log->notify('JOB TABLE ID: '.$v['id']);

				if(ACTIVATE && $v['id']){
					$this->log->notify('Set '.$v['id'].' to portal id '.$jobId.'.');
					mysql_query("UPDATE job SET portal_job_id = $jobId WHERE id = ".$v['id']);
				}

				$ret = $v['scheduler'];
				$ret = explode(':', $ret);
				$ret = $ret[1];
				$ret = str_replace('/',' ', $ret);
				$ret = trim($ret);

				if($ret == '128.8.141.68'){ // lysine (UMIACS Condor pool)
				  $ret = '128.8.141.67'; // leucine (UMIACS Condor pool)
					$this->log->notify('Accounted for IP Shift: 128.8.141.68 -> 128.8.141.67');
				}
				
				
				return $ret;
		}else{
			$this->log->error("Unable to resolve Resource ID on ".$this->unique_id . ' Job Name: '.$job_name.' User_id:'.$job_user);	
			return null;
		}
	}
	function resolveResourceType(){
		$resourceId = $this->getResourceId();
		global $mysql_host, $mysql_user, $mysql_db, $mysql_pass;
		$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
		mysql_select_db($mysql_db, $conn);
		$results = mysql_query("SELECT * FROM resource WHERE ip = '$resourceId' ", $conn);
		if($results){
			$v = mysql_fetch_array($results);
			$resourceType = $v['type'];
			$results = mysql_query("SELECT * FROM resource_type WHERE id = $resourceType");
			if($results){
				$v = mysql_fetch_array($results);
				return $v['type'];
			}
		}
		$this->log->error("Unable to resolve Resource Type on ".$this->unique_id);
		return null;
	}

	function isParsed(){
		global $mysql_user;
		global $mysql_host;
		global $mysql_pass;
		global $mysql_db;

		$jobId = $this->unique_id;
		$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
		mysql_select_db($mysql_db, $conn);
		$results = mysql_query("SELECT * FROM portal_job WHERE id = $jobId", $conn);
		if($results != false){
			while($v = mysql_fetch_array($results)){
			    if(isset($v['profiled']) && $v['profiled'] != NULL){
				$this->profiled = $v['profiled'];
			    }else{
			      return false;
			    }
			}
		}
		return $this->profiled;
	}

	function setParsed(){
		global $mysql_user;
		global $mysql_host;
		global $mysql_pass;
		global $mysql_db;

		$jobId = $this->unique_id;
		$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
		mysql_select_db($mysql_db, $conn);
		mysql_query("UPDATE portal_job SET profiled=true WHERE id = $jobId", $conn);
		$this->profiled = true;
	}

	function setRuntime($run){
		$this->runtime = $run;
	}

	function getRuntime(){
		return $this->runtime;
	}

	function setId($id){
		$this->unique_id = $id;
	}

	function buildCore(){
		global $mysql_user;
		global $mysql_host;
		global $mysql_pass;
		global $mysql_db;

		if($this->unique_id != -1){
			$jobId = $this->unique_id;
			$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
			mysql_select_db($mysql_db, $conn);
			$results = mysql_query("SELECT * FROM portal_job WHERE id = $jobId", $conn);
			if($results != false){
				while($v = mysql_fetch_array($results)){
					$this->profiled = $v['profiled'];
					$this->mem_used = $v['memused'];
					$this->runtime = $v['runtime'];
				}
			}
		}else{
			$this->log->error("Can't build core, no id!");
		}
	}

	function saveCore(){
		global $mysql_user;
		global $mysql_host;
		global $mysql_pass;
		global $mysql_db;

		if($this->unique_id != -1){
			$jobId = $this->unique_id;
			$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
			mysql_select_db($mysql_db, $conn);
			
			//This fixes any weirdness with nulls by forcing a boolean
			if($this->profiled == true){
			  $this->profiled = "true";
			}else{
			  $this->profiled = "false";
			}

			$results = mysql_query("UPDATE portal_job SET runtime = ".$this->runtime.
				", memused=".$this->mem_used.
				", profiled=".$this->profiled.
				" WHERE id = ".$jobId
				, $conn);

		}else{
			$this->log->error("Can't save core, no id!");
		}
	}
}
?>