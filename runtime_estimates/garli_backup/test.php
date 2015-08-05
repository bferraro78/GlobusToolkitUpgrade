<?php
require_once('garliMultiEstimate.php');
define(DBG, true);
$ge = new garliMultiEstimate();
$ge->parse('garli.conf');
$ge->constructEstimateLines();
?>