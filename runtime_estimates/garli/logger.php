<?php
require_once('settings.php');

class logger{

	public $logLocation;
	public $fhandle = 0;
	public $newline;

	function setLocation($location){
		$this->logLocation = $location;
	}
	
	function init(){

		if(!CLI){
			$this->newline = '<br />';
		}else{
			$this->newline = "\n";
		}
		global $log_location;
		$this->logLocation = $log_location.date('mdy');
		if(!file_exists($this->logLocation)){
			touch($this->logLocation);
		}
		$this->fhandle = fopen($this->logLocation,'a');
		if(!($this->fhandle)){
			print("Could not initiate log session.");
		}
	}

	function error($msg){
		if($this->fhandle){
			fwrite($this->fhandle, time().":ERROR:".$msg."\n");
		}else{
			print("Could not write to log file! Did you init()?".$this->newline);
		}
		print(time().":ERROR:".$msg.$this->newline);
	}

	function notify($msg){
		if($this->fhandle){
			fwrite($this->fhandle, time().":".$msg."\n");
		}else{
			print("Could not write to log file! Did you init()?".$this->newline);
		}
		print(time().":".$msg.$this->newline);
	}

	function warn($msg){
		if($this->fhandle){
			fwrite($this->fhandle, time().":WARN:".$msg."\n");
		}else{
			print("Could not write to log file! Did you init()?".$this->newline);
		}
		print(time().":WARN:".$msg.$this->newline);
	}

	function debug($msg){
		if(DBG){
			if($this->fhandle){
				fwrite($this->fhandle, time().":DBG:".$msg."\n");
			}else{
				print("Could not write to log file! Did you init()?".$this->newline);
			}
			if(!CLI){
				print('<pre>'.time().":DBG:".$msg.'</pre>'.$this->newline);
			}else{
				print(time().":DBG:".$msg.$this->newline);
			}
		}
	}

	function close(){
		if($this->fhandle)
		fclose($this->fhandle);
	}

}

?>