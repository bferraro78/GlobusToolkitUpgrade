#!/usr/bin/perl

$total = 0;
$counter = 0;
open TIME, "umiacs_timings";
while(<TIME>) {
    chomp;
    $line = $_;
    print "line is: $line\n";
    if($line =~ /.* ([0-9]*) minutes and ([0-9]*) seconds/) {
	$minutes = $1;
	print "minutes is: $minutes\n";
	$total += $minutes * 60;
	$seconds = $2;
	print "seconds is: $seconds\n";
	$total += $seconds;
	$counter++;
    }
}
close TIME;

$average = $total / $counter;

print "total seconds: $total\n";
print "average seconds: $average\n";
