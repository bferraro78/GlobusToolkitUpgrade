<?php
require_once('estimate.php');
require_once('settings.php');

class runtimeScaling{
	private static $runtimes;

	static function getResources(){
		$return = array();
		$conn = mysql_connect('cysteine.umiacs.umd.edu', 'gt4admin','Daim7juz');
		mysql_select_db('grid_production', $conn);
		$results = mysql_query('SELECT * FROM resource', $conn);
		while($v = mysql_fetch_array($results)){
		$return[$v['ip']] = $v['sf'];	
		}
		runtimeScaling::$runtimes = $return;
	}

	static function scaleRuntime(estimate $est){
		$run = $est->getRuntime();
		$run = $run * runtimeScaling::$runtimes[$est->getResourceId()];
		$est->setRuntime((float)$run);
	}
	static function forceScaleRuntime(estimate $est){
		$run = $est->getRuntime();
		$id = $est->resourceIdForceResolve();
		$run = $run * runtimeScaling::$runtimes[$id];
		if($run==0){
			$est->log->error("Could not properly scale for resource: ".$id);
		}
		$est->setRuntime((float)$run);
	}

}

?>