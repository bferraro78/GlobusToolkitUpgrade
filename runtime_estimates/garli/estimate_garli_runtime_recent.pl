#!/usr/bin/perl

if(@ARGV != 10) { # we are expecting 10 arguments, as follows
    print "usage: perl estimate_garli_runtime_recent.pl [unique_id] [unique_patterns] [num_taxa] [mem_used] [datatype] [ratematrix] [statefrequencies] [ratehetmodel] [numratecats] [invariantsites]\n";
    exit;
}

$unique_id = $ARGV[0];
$unique_patterns = $ARGV[1];
$num_taxa = $ARGV[2];
$mem_used = $ARGV[3];
$datatype = $ARGV[4];
$ratematrix = $ARGV[5];
$statefrequencies = $ARGV[6];
$ratehetmodel = $ARGV[7];
$numratecats = $ARGV[8];
$invariantsites = $ARGV[9];

# construct the input table
$input_table = $unique_id . "_estimate";
open TABLE, ">$input_table";
print TABLE 
    "NA\t" . # we don't know the runtime... that's what we're looking for
    $unique_patterns . "\t" .
    $num_taxa . "\t" .
    $mem_used . "\t" .
    $datatype . "\t" .
    $ratematrix . "\t" .
    $statefrequencies . "\t" .
    $ratehetmodel . "\t" .
    $numratecats . "\t" .
    $invariantsites . "\t" .
    "\n";
close TABLE;

# fill in the rest of the table
`cat garli_header $input_table output_lines_recent > $input_table\_temp`;
# overwrite the original table
`mv $input_table\_temp $input_table`;

# write out an R file to execute
$R_file = "estimate_" . $unique_id . "_runtime.r";
open ROUT, ">$R_file";
print ROUT "suppressPackageStartupMessages(library(randomForest))\n";
print ROUT "load(\"garli_recent.rf\")\n";
print ROUT "garli_estimate_one <- read.table(\"$input_table\",header=T)\n";
print ROUT "garli.pred <- predict(garli.rf, garli_estimate_one[1,])\n";
print ROUT "cat('ESTIMATE: ',as.integer(garli.pred[1]),'\\n',sep=\"\")\n";
close ROUT;

# execute the R file and capture its output
$output = `R --vanilla --slave -f $R_file`;

if($output =~ /ESTIMATE: ([0-9]*)/) {
    print $1;
    # delete the R file and input table
    `rm $R_file $input_table`;
} else {
    print "-1"; # -1 will be an error code meaning something went wrong
}
