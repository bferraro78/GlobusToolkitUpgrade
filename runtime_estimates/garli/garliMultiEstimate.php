<?php
require_once('estimate.php');
require_once('garliEstimate.php');
require_once('settings.php');

class garliMultiEstimate extends garliEstimate{

	private $multiModel = false; //Boolean. Is this one model to many partitions, or one model to one partition?
	public $unique_patterns = array();
	public $num_taxa = array();
	public $datatype = array();
	public $rate_matrix = array();
	public $statefrequencies = array();
	public $ratehetmodel = array();
	public $numratecats = array();
	public $invariantsites = array();
	public $estimateLines = array();
	private $catches = array();

	function __construct(){
		parent::__construct();
		$this->log->loud = false;
	}

	function __destruct(){
		parent::__destruct();
	}

	/*This is (essentially) an override. It only takes a conf.
	 *It will generate a partial screen log by running Garli
	 *In validate mode. It will then parse as needed for a 
	 *multi model/multi partition job.
	 *
	 *This assumes the datafile is correctly specified in the conf.
	 *
	 */
	function parse($path){
		//Handle the conf
		$conf = fopen($path.'garli.conf', 'r');
		$this->log->notify($path);
		if($conf){
			while(!feof($conf)){
				$line = fgets($conf);
				//Since every line is var=value we can use a helper function
				$this->consumeConfLine($line); //defined in parent
			}
			fclose($conf);
			//This line fills in our class with the bits from consumeConfLine()
			$this->parseCatches(); 
		}

		//Check for the screen.log, if it doesn't exist, fire up Garli in validation mode
		if(!file_exists('garli.screen.log')){
					exec('Garli-2.0 -V '.$path.'garli.conf');
		}

		$screenLog = fopen('garli.screen.log', 'r');

		if($screenLog){
			//these lines are not so standard, so, we'll need to do this manually
			while(!feof($screenLog)){
				$line = fgets($screenLog);
				if(preg_match('/unique patterns in/', $line)){
					$line = explode(' ', $line);
					$this->unique_patterns[] = $line[2];
				}else if(preg_match('/will actually use approx/', $line)){
					$line = explode(' ', $line);
					$this->mem_used[] = $line[5];
				}else if(preg_match('/sequences./', $line)){
					$line = explode(' ', $line);
					$this->num_taxa[] = $line[2];
				}
			}
			fclose($screenLog);
		}else{
			$this->log->error('No screen log!');
			die(0);
		}
		
	}

	//This will catch the lines we care about and put them inside a nice array for us
	function consumeConfLine($line){
		$extractList = array('datatype', 'ratematrix', 'statefrequencies', 'ratehetmodel', 'numratecats', 'invariantsites');
		foreach($extractList as $catch){
			if(preg_match("/$catch/", $line)){
				$lineExploded = explode(' ', $line);
				$this->catches[$catch][] = trim($lineExploded[2]);
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

	//Construct each line needed to get an estimate
	//Assume that if $this->multiModel, then $parts=count($this->datatype)
	function constructEstimateLines(){
		if(count($this->datatype)>1){
			$this->multiModel = true;
		}
		$parts = count($this->num_taxa);
		if($this->multiModel){
			//put an upper bound on subsets so we don't fire off a billion estimates
			if($parts>10){
				$this->log->notify("Scaling subsets to 10, actual subsets=$parts");
				$parts=10;
			}

			for($i=0;$i<$parts;$i++){
				//Construct the first half of the line based on the subset
				$line = "subset$i ".$this->unique_patterns[$i]." ".$this->num_taxa[$i]." ".$this->mem_used[0]." ";
				
				$line .= $this->datatype[0]." ".$this->rate_matrix[0]." ".$this->statefrequencies[0]." ".$this->ratehetmodel[0]." ".$this->numratecats[0]." ".$this->invariantsites[0];
				
				$this->estimateLines[] = $line;
			}
		}else{
			$unique_patterns = 0;
			for($i=0;$i<$parts;$i++){
				$unique_patterns += $this->unique_patterns[$i];
			}
			$line = "subset$i ".$unique_patterns." ".$this->num_taxa[0]." ".$this->mem_used[0]." ";
			$line .= $this->datatype[0]." ".$this->rate_matrix[0]." ".$this->statefrequencies[0]." ".$this->ratehetmodel[0]." ".$this->numratecats[0]." ".$this->invariantsites[0];
			$this->estimateLines[] = $line;
		}
		$this->log->debug(print_r($this->estimateLines, true));
	}

}
?>