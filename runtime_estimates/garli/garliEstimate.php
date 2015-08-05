<?php
require_once('estimate.php');
require_once('settings.php');

class garliEstimate extends estimate{
	public $unique_patterns;
	public $num_taxa;
	public $datatype;
	public $rate_matrix;
	public $statefrequencies;
	public $ratehetmodel;
	public $numratecats;
	public $invariantsites;
	private $catches = array();

	function __construct(){
		parent::__construct();
	}

	function __destruct(){
		parent::__destruct();
	}

	function parse($garliConf, $screenLog){
		//Handle the conf
		$conf = fopen($garliConf, 'r');
		if($conf){
			while(!feof($conf)){
				$line = fgets($conf);
				//Since every line is var=value we can use a helper function
				$this->consumeConfLine($line);
			}
			fclose($conf);
			//This line fills in our class with the bits from consumeConfLine()
			$this->parseCatches();
		}
		//now it's time for our screen log
		$screenLog = fopen($screenLog, 'r');
		if($screenLog){
			//these lines are not so standard, so, we'll need to do this manually
			while(!feof($screenLog)){
				$line = fgets($screenLog);
				if(preg_match('/Time used = /', $line)){
					$line = explode(' ', $line);
					//runtimes should be in seconds
					$this->runtime = ($line[3] * 3600) + ($line[5] * 60) + $line[8];
					$this->raw_runtime = $this->runtime;
				}else if(preg_match('/unique patterns in/', $line)){
					$line = explode(' ', $line);
					$this->unique_patterns = $line[2];
				}else if(preg_match('/will actually use approx/', $line)){
					$line = explode(' ', $line);
					$this->mem_used = $line[5];
				}else if(preg_match('/sequences./', $line)){
					$line = explode(' ', $line);
					$this->num_taxa = $line[2];
				}
			}
			fclose($screenLog);
		}
		
	}

	//This will catch the lines we care about and put them inside a nice array for us
	function consumeConfLine($line){
		$extractList = array('datatype', 'ratematrix', 'statefrequencies', 'ratehetmodel', 'numratecats', 'invariantsites');
		foreach($extractList as $catch){
			if(preg_match("/$catch/", $line)){
				$lineExploded = explode(' ', $line);
				$this->catches[$catch] = trim($lineExploded[2]);
				break;
			}
		}
	}

	//this will parse our $catches array into our class once it's filled
	function parseCatches(){
		$this->datatype = $this->catches['datatype'];
		$this->rate_matrix = $this->catches['ratematrix'];
		$this->statefrequencies = $this->catches['statefrequencies'];
		$this->ratehetmodel = $this->catches['ratehetmodel'];
		$this->numratecats = $this->catches['numratecats'];
		$this->invariantsites = $this->catches['invariantsites'];

	}

	//return this class formatted for insertion into a random forest matrix
	function returnMatrixLine(){
		$ret = $this->runtime . "\t" . $this->unique_patterns . "\t" . $this->num_taxa . "\t" . $this->mem_used . "\t" . $this->datatype . "\t" . $this->rate_matrix . "\t" . $this->statefrequencies . "\t" . $this->ratehetmodel . "\t" . $this->numratecats . "\t" . $this->invariantsites . "\n";
		return $ret;
	}

	function build(){
		$this->buildCore(); //This fills in the estimate parameters
		if($this->unique_id != -1){
			$jobId = $this->unique_id;
			global $mysql_host, $mysql_user, $mysql_db, $mysql_pass;
			$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
			mysql_select_db($mysql_db, $conn);
			$results = mysql_query("SELECT * FROM garli WHERE portal_job_id = $jobId", $conn);
			if($results != false){
				while($v = mysql_fetch_array($results)){
					$this->unique_patterns = $v['uniquepatterns'];
					$this->num_taxa = $v['numtaxa'];
					$this->datatype = $v['datatype'];
					$this->rate_matrix = $v['ratematrix'];
					$this->statefrequencies = $v['statefrequencies'];
					$this->ratehetmodel = $v['ratehetmodel'];
					$this->numratecats = $v['numratecats'];
					$this->invariantsites = $v['invariantsites'];
				}
			}
		}else{
			$this->log->error("Can't build!, no id!");
		}
	}

	function verify(){
		if(isset($this->runtime)&&isset($this->unique_patterns)&&isset($this->num_taxa)&&isset($this->mem_used)&&isset($this->datatype)&&isset($this->rate_matrix)&&isset($this->statefrequencies)&&isset($this->ratehetmodel)&&isset($this->numratecats)&&isset($this->invariantsites)){
			if($this->runtime > 0 && $this->unique_patterns>0 && $this->num_taxa>0 && $this->mem_used > 0){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	function save(){
		if($this->unique_id != -1){
			$jobId = $this->unique_id;
			$this->saveCore();
			global $mysql_host, $mysql_user, $mysql_db, $mysql_pass;
			$conn = mysql_connect($mysql_host, $mysql_user,$mysql_pass);
			mysql_select_db($mysql_db, $conn);
			/*
			*integer $unique_patterns;
			integer $num_taxa;
			string $datatype;
			string $rate_matrix;
			string $statefrequencies;
			string $ratehetmodel;
			integer $numratecats;
			string $invariantsites;
			*/
			
			$results = mysql_query("UPDATE garli SET uniquepatterns = ".$this->unique_patterns.
				", numtaxa=".$this->num_taxa.
				" WHERE portal_job_id = ".$jobId
				, $conn);

		}else{
			$this->log->error("Can't save, no id!");

		}

	}
}
?>