# File: boinc.pm (or boinc.in)
# Original Author: Daniel Myers
# Contributors: Andrew J Younge (ajy4490@umiacs.umd.edu),
#								Adam Bazinet (adam.bazinet@umiacs.umd.edu)
# Description:
#		Main Perl module that is responsible for the interface between Globus
#		GT6 and BOINC. It translates a generic Globus job description to the correct
#		corresponding BOINC calls in order to create work and submit jobs.

use Globus::GRAM::Error;
use Globus::GRAM::JobState;
use Globus::GRAM::JobManager;
use Globus::GRAM::StdioMerger;
use Globus::Core::Paths;

use Config;
use Carp;
use DBI;
use XML::Parser;
use strict;

package Globus::GRAM::JobManager::boinc;
use vars qw(@ISA);
use Storable;

@ISA = qw(Globus::GRAM::JobManager);

my ($find_app, $create_work, $cancel, $poll, $cache_pgm, $boinc_download_dir,
		$boinc_upload_dir, $wu_ru_path);
my ($project_dir, $globus_dir);

# Predefined variables.
BEGIN {
	$project_dir = '/fs/mikedata/arginine/work/boinc_projects';  # Changed from boinc5 --> boinc_projects BRF
	$globus_dir = '/opt/gsbl-config';  # Changed from /fs/mikedata/arginine/work/globus-4.2.1 JTK
}

$find_app = "$project_dir/bin/find_app";	
$create_work = "$project_dir/bin/create_work";
$cancel = "$project_dir/bin/cancel_job";	

# Poll is deprecated.
$poll = "/$project_dir/bin/get_assim_state";  # Changed from get_state --> get_assim_state JTK

$boinc_download_dir = "$project_dir/download/";
$boinc_upload_dir = "$project_dir/upload/";
$wu_ru_path = "$project_dir/templates";
my $ru_max_nbytes = "1000000000";

# Other.
$cache_pgm = "$Globus::Core::Paths::bindir/globus-gass-cache";

my $stagein_save_prefix = "$project_dir/stagein.";

my $logfile = "/tmp/boinc_jm_log.txt";

# This will be inferred from the working directory in the submit function.
my $scratch_dir = "";

my ($wu_filename, $ru_filename);

my ($db_host, $db_user, $db_pass, $db_name, $temp_tag);
my ($grid_host, $grid_user, $grid_pass, $grid_db);

# Constructor. Calls super constructor.
sub new {
	my $proto = shift;
	my $class = ref($proto) || $proto;
	my $self = $class->SUPER::new(@_);

	return $self;
}

################################################################################
# No longer used.
#sub get_stage_dir {
#	my $self = shift;
#	my $description = $self->{JobDescription};
#	return $description->directory(); 
#}

################################################################################
# This function submits a workunit to BOINC.
sub submit {
	my $self = shift;

	open LOG, ">$logfile";

	get_grid_db_args();
	get_boinc_db_args();

	my $description = $self->{JobDescription};
	$description->save("/tmp/boinc_jm_desc");


	my $unique_id = $description->uniqid();  # Changed from directory --> uniqid JTK
	if ($unique_id =~ /(.*\/)/) {
		$scratch_dir = $1;
		if ($scratch_dir =~ /(.*\/).*\..*/) {
			$scratch_dir = $1;
		}
		print LOG "scratch dir is: $scratch_dir\n";
	}

	# ALB -- create symlinks, if necessary.
	my $symlinks_string = $description->symlinks();
	if (defined($symlinks_string)) {
		open SYMLINK, ">/tmp/symlink_log";
		# Symlink pairs are separated by commas.
		my @symlinks = split(/,/, $symlinks_string);

		for (my $j = 0; $j < @symlinks; $j++) {
			my @symlink_pair = split(/:/, $symlinks[$j]);
			my $target = $symlink_pair[0];
			print SYMLINK "target: $target\n";
			my $linkname = $symlink_pair[1];
			print SYMLINK "linkname: $linkname\n";
			# Substitute for ${GSBL_CONFIG_DIR}, if necessary.
			$target =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;
			$linkname =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;
			print SYMLINK "target after: $target\n";
			print SYMLINK "linkname after: $linkname\n";
			# Now, actually create the symlink.
			# Redirect stderr to stdout to avoid job failure if symlink already
			# exists.
			`ln -s $target $linkname 2>&1`;
		}
		close SYMLINK;
	}
	
	$unique_id =~ s/.*\///;
	print LOG "Unique id: $unique_id\n";

	$unique_id =~ s/.output//;
	print LOG "New unique id: $unique_id\n";

	my $num_batch = get_num_batches($description);
	print LOG "num_batch: $num_batch\n";

	$wu_filename = ($wu_ru_path . "/wu." . $unique_id);
	$ru_filename = ($wu_ru_path . "/ru." . $unique_id);

	# Validate the RSL file and determine the BOINC application to use.
	my $boinc_app = check_rsl_file($description);

	if ($boinc_app == Globus::GRAM::Error::JOBTYPE_NOT_SUPPORTED()) {
		return Globus::GRAM::Error::JOBTYPE_NOT_SUPPORTED();
	} elsif ($boinc_app == Globus::GRAM::Error::RSL_EXECUTABLE()) {
		return Globus::GRAM::Error::RSL_EXECUTABLE();
	} elsif ($boinc_app == Globus::GRAM::Error::EXECUTABLE_NOT_FOUND) {
		return Globus::GRAM::Error::EXECUTABLE_NOT_FOUND;
	} elsif ($boinc_app == Globus::GRAM::Error::RSL_STDIN) {
		return Globus::GRAM::Error::RSL_STDIN;
	} elsif ($boinc_app == Globus::GRAM::Error::STDIN_NOT_FOUND) {
		return Globus::GRAM::Error::STDIN_NOT_FOUND;
	} elsif ($boinc_app == Globus::GRAM::Error::RSL_DIRECTORY) {
		return Globus::GRAM::Error::RSL_DIRECTORY;
	} elsif ($boinc_app == Globus::GRAM::Error::BAD_DIRECTORY()) {
		return Globus::GRAM::Error::BAD_DIRECTORY();
	}

	print LOG "Determined BOINC app to be: $boinc_app\n";

	my ($mem_bound, $wall_bound, $cpu_bound, $cpu_est, $disk_bound) =
			computeBounds($description);

	# CNS settings.
	if ($boinc_app eq "cns") {
		$cpu_est = 1.53E13;  # About 4 hours.
		$mem_bound = ((256 * 1024) * 1024);  # 256 Mb.
	}

	# HMMPfam settings.
	if ($boinc_app eq "hmmpfam") {
		$cpu_est = 6.15E12;  # 2 hours or so?
		$cpu_bound = (6.15E12 * 10);  # 20 hours or so?
		$wall_bound = (86400 * 2);  # 2 days.
	}

	# GARLI settings.
	if ($boinc_app eq "garli") {
		if (defined($description -> runtime_estimate())) {

			# This code block is likely obsolete and needs to be revisited.
			my $runtime_est = $description -> runtime_estimate();
			$cpu_est = ($runtime_est / 916);
			$cpu_est *= 1.59E12;
			$cpu_bound = (5.7E14 * 10);
			my $multiple = ($runtime_est * 3);
			my $exponential = (($runtime_est ** 1.30) / 50);
			# Give exponential growth to leniency of deadline.
			$wall_bound = ($multiple + $exponential);
			if ($wall_bound < 3600) {
				$wall_bound = 3600;  # No job is given less than one hour.
			}

#			$wall_bound = 604800;  # One week.
			$wall_bound = 172800;  # Two days.

			print LOG "multiple $multiple\n";
			print LOG "exponential $exponential\n";
			print LOG "wall $wall_bound\n";
		} else {
			# For codon model jobs.
#			$cpu_est = 5.7E14;  # 66 hours on 2400 million fpops/sec machine.
#			$cpu_bound = (5.7E14 * 10);
#			$wall_bound = (86400 * 21);  # 21 days.

			# For "in between" jobs.
			# 2 hours; corresponds to optimal main workunit length.
	    $cpu_est = (6.15E12 * 2);
#			$cpu_est = 6E14;
			$cpu_bound = (5.7E14 * 10);  # Just some very large value.
			$wall_bound = (86400 * 11);  # 11 days.

#			$cpu_est = 2E14;  # ~22 hours on 2400 million fpops/sec machine.
#			$cpu_bound = (5.7E14 * 10);  # No point in lowering this.
#			$wall_bound = (86400 * 5);  # 5 days.

			# Really long jobs!
#			$cpu_est = 11.4E14;  # ~132 hours on 2400 million fpops/sec machine.
#			$cpu_bound = (5.7E14 * 10);  # No point in lowering this.
#			$wall_bound = (86400 * 21);  # 21 days.

			# Shorter jobs.
#			$cpu_est = (6.15E12 * 1);  # 1 hour.
#			$cpu_bound = (5.7E14 * 10);  # No point in lowering this.
#			$wall_bound = (86400 * 4);  # 4 days.
		}
	}

	# MARXAN settings.
	if ($boinc_app eq "marxan") {
		$cpu_est = (6.15E12 * 2);  # 4 hours.
		$cpu_bound = (6.15E12 * 20);  # 40 hours.
		$wall_bound = (86400 * 2);  # 2 days.
	}

	# Quote/escape arguments.
	my $args = $self->process_arguments($description);

	# Returns array refs.
	my $perjob_files = get_perjob_files($description, $num_batch);
	my $stagein_files = get_stagein_files($description);
	my $stageout_files = get_stageout_files($description);


	# Assume now that we will use workphasedivision by default, unless multiple
	# searchreps or bootstrapreps.
	# Assume that the GARLI configuration file contains the workphasedivision,
	# writecheckpoints, and stoptime directives.
	# Set the stoptime here.
	# Assume this is not a heterogeneous batch.
	my $workphasedivision = 0;
	if ($boinc_app eq "garli") {
		$workphasedivision = 1;
		my $searchreps = 1;
		my $bootstrapreps = 1;

		foreach my $inputfile (@$stagein_files) {
			if ($inputfile =~ /garli\.conf/) {
				# Substitute for ${GSBL_CONFIG_DIR}, if necessary.
				$inputfile =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;
				print LOG "garli.conf: $inputfile\n";
				open GARLICONF, "$inputfile";
				while (<GARLICONF>) {
					chomp;
					my $line = $_;
					if ($line =~ /searchreps\s*=\s*([0-9]*)/) {
						$searchreps = $1;
						chomp($searchreps);
					}
					if ($line =~ /bootstrapreps\s*=\s*([0-9]*)/) {
						$bootstrapreps = $1;
						chomp($bootstrapreps);
					}
				}
				close GARLICONF;

				print LOG "searchreps: $searchreps\n";
				print LOG "bootstrapreps: $bootstrapreps\n";

				# Do not use workphase division.
				if (($searchreps > 1) || ($bootstrapreps > 1)) {
					`perl -pi -e 's/workphasedivision\\s*=\\s*1/workphasedivision = 0/g;' $inputfile`;
					$workphasedivision = 0;
				} else {
					# Set workphasedivision, writecheckpoints, and stoptime appropriately.
					`perl -pi -e 's/workphasedivision\\s*=\\s*0/workphasedivision = 1/g;' $inputfile`;
					`perl -pi -e 's/writecheckpoints\\s*=\\s*0/writecheckpoints = 1/g;' $inputfile`;
					`perl -pi -e 's/stoptime\\s*=\\s*[0-9]*/stoptime = 7200/g;' $inputfile`;

					# We need to set restart = 1 in the garli.conf file; we have to make a
					# copy in the Globus scratch dir.
					my $restartconf = ($inputfile . "_restart");
					`cp $inputfile $restartconf`;
					`perl -pi -e 's/restart = 0/restart = 1/g;' $restartconf`;
				}
			}
		}
	}

	my ($wu_names, $perjob_boinc_files);
	# Write BOINC workunit file(s).
	if (!is_hetero_batch($description)) {
		my $wu_filename = "";

		# Catch if args is empty.
		if ($args eq "") {
			print LOG "args is empty!\n";
			$wu_filename = $self->write_wu_template($description, "", $stagein_files,
					$workphasedivision);
		} else {
			$wu_filename = $self->write_wu_template($description, @$args[0],
					$stagein_files, $workphasedivision);
		}

		my @temp = ($wu_filename);
		$wu_names = \@temp;
		print LOG "Wrote WU template to $wu_filename; in /tmp/wu\n";
		`cp $wu_filename /tmp/wu`;
	} else {
		# Write multiple per-job templates.
		$wu_names = $self->write_wu_templates($description, $args, $stagein_files,
				$perjob_files, 0);

		# Copy per-job files into the download dir.
		my $index = (@$stagein_files + 1);
		$perjob_boinc_files = copy_perjob_files_to_boinc($description,
				$perjob_files, $unique_id, $index, $num_batch);
		if ($perjob_boinc_files == Globus::GRAM::Error::STAGE_IN_FAILED()) {
			return Globus::GRAM::Error::STAGE_IN_FAILED();
		}
	}

	# Write result unit template.
	my $ru_filename = write_ru_template($description, $stageout_files,
			$ru_filename, 0);
	print LOG "Wrote RU template to $ru_filename; contents are in /tmp/ru\n";
#	`cp $wu_names[0] /tmp/ru`;

	# Adding the ability to specify a custom workunit name (for use with phased
	# GARLI analyses).
	my $workunit_name = $unique_id;

	if ($boinc_app eq "garli") {
		if ($workphasedivision == 1) {
			# Write a workunit template that includes checkpoint files as input files.
			$wu_filename = ($wu_ru_path . "/wu." . $unique_id
					. "_with_checkpoint_files");
			# Assume the output file prefix is always "garli" for these types of jobs.
			my @stagein_with_checkpoint = @$stagein_files;
			push(@stagein_with_checkpoint, "garli.adap.check");
			push(@stagein_with_checkpoint, "garli.pop.check");
			push(@stagein_with_checkpoint, "garli.swaps.check");
			# Also include the GARLI screen log from the previous workunit.
			push(@stagein_with_checkpoint, "garli.screen.log");
		
			if ($args eq "") { # Makes sure $args[0] is not empty -- BRF
			    print LOG"garli args is EMPTY\n";
			    $wu_filename = $self->write_wu_template($description, "",
					\@stagein_with_checkpoint, 0);
			} else {
			    print LOG"garli args is NOT EMPTY\n";
			    $wu_filename = $self->write_wu_template($description, @$args[0],
								\@stagein_with_checkpoint, 0);
			}

			print LOG "Wrote WU template to $wu_filename\n";
			# Write an additional workunit template with <no_delete/> tags, for use
			# with _main workunits.
			$wu_filename = ($wu_ru_path . "/wu." . $unique_id
					. "_with_checkpoint_files_no_delete");
			if ($args eq "") { # Makes sure $args[0] is not empty -- BRF 
			    print LOG "garli args (no delete) is EMPTY\n";
			    $wu_filename = $self->write_wu_template($description, "",
					\@stagein_with_checkpoint, 1);
			} else {
			    print LOG "garli args (no delete) is NOT EMPTY\n";
			    $wu_filename = $self->write_wu_template($description, @$args[0],
								    \@stagein_with_checkpoint, 1);
			}
			print LOG "Wrote WU template to $wu_filename\n";

			# Write a result unit template that expects checkpoint files as output
			# files instead of original output files.
			$ru_filename = ($wu_ru_path . "/ru." . $unique_id . "_checkpoint_files");
			# Assume the output file prefix is always "garli" for these types of jobs.
			my @stageout_checkpoint = ();
			push(@stageout_checkpoint, "garli.adap.check");
			push(@stageout_checkpoint, "garli.pop.check");
			push(@stageout_checkpoint, "garli.swaps.check");
			# Including the GARLI screen log for debugging purposes.
			push(@stageout_checkpoint, "garli.screen.log");
			my $ru_filename = write_ru_template($description, \@stageout_checkpoint,
					$ru_filename, 1);
			print LOG "Wrote RU template to $ru_filename\n";

			# Change the workunit name to include "_initial".
			$workunit_name .= "_initial";

			# Change the wall bound on the _initial workunit to two days.
			$wall_bound = 172800;
		}
	}

	# Copy files needed by the BOINC client to the BOINC download directory.
	my $boinc_files = copy_files_to_boinc($description, $stagein_files,
			$unique_id, $workphasedivision);
	if ($boinc_files == Globus::GRAM::Error::STAGE_IN_FAILED()) {
		return Globus::GRAM::Error::STAGE_IN_FAILED();
	}

	# FOR HETEROGENEOUS BATCH SUPPORT:
	# At this point, we should have available wu_names which contain an array of
	# the different workunit names (ex: wu_12345678.1234567890.2) and
	# perjob_boinc_files which holds an array of arrays that have the different
	# filenames for each boinc-translated per-job name
	# (ex: 12345678.1234567890.2_4).

	# Create the BOINC workunit.
	create_work($wu_names, $ru_filename, $boinc_app, $workunit_name, $wall_bound,
			$cpu_bound, $cpu_est, $mem_bound, $disk_bound, $boinc_files, $num_batch,
			$perjob_boinc_files);

	# Write the assimilator callback script.
	$self->write_assimilator_script($description, $scratch_dir, $unique_id,
			$workunit_name, $stagein_files, $stageout_files, $wu_ru_path, $num_batch,
			$wall_bound, $cpu_bound, $cpu_est, $mem_bound, $disk_bound);
	if ($workphasedivision == 1) {
		# We need to write two additional assimilator callback scripts.
		$self->write_assimilator_script($description, $scratch_dir, $unique_id,
				($unique_id . "_main"), $stagein_files, $stageout_files, $wu_ru_path,
				$num_batch, $wall_bound, $cpu_bound, $cpu_est, $mem_bound, $disk_bound);
		$self->write_assimilator_script($description, $scratch_dir, $unique_id,
				($unique_id . "_final"), $stagein_files, $stageout_files, $wu_ru_path,
				$num_batch, $wall_bound, $cpu_bound, $cpu_est, $mem_bound, $disk_bound);
	}

	# Job went in OK, so return the ID of the workunit.
	$description->add('jobid', $unique_id);

	# Update the grid database.
	add_wu_name_db($description);

	close LOG;

	return {JOB_STATE => Globus::GRAM::JobState::PENDING,
			JOB_ID => $unique_id};
}

################################################################################
####################### BEGIN submit support functions. ########################
################################################################################

# Test to see if the job is a heterogeneous batch.
sub is_hetero_batch {
	my $description = shift;

	if (defined($description->transfer_perjob_files())) {
		return 1;
	} else {
		return 0;
	}
}

# Add BOINC information to the grid database.
sub add_wu_name_db {
	my $description = shift;

#	my $hash = ($description->filestageout())[0]->[1];

#	$hash =~ /(hash-\d*-\d*)/;
#	$hash = $1;

	my $db = "DBI:mysql:database=$grid_db:host=$grid_host";
	my $dbh;
	my $sth;

	$dbh = DBI->connect($db, $grid_user, $grid_pass, {RaiseError => 1});

	my $wu_id = $description->uniqid;
	$wu_id =~ s/.*\///;
	$wu_id =~ s/.output//;

	my $query = qq{UPDATE job SET wu_id="$wu_id" where unique_id=$wu_id };
	open(Q, ">/tmp/tmpQuery");
	print Q "$query\n";
	close(Q);
	$dbh->do($query);
	$query = qq{UPDATE job SET resource="BOINC" WHERE unique_id=$wu_id };
	$dbh->do($query);
	$dbh->disconnect();
}

# Validate the RSL description and determine the BOINC application to execute.
sub check_rsl_file {
	my $description = shift;

	if (defined($description->jobtype())) {
		my $jobtype = $description->jobtype();
		unless (($jobtype eq "single")
				|| (($jobtype eq "multiple") && ($description->count >= 1))) {
			return Globus::GRAM::Error::JOBTYPE_NOT_SUPPORTED();
		}
	}

	# Check that we have a valid executable (not null and is listed as a BOINC
	# application).
	my $executable = $description->executable;
	unless ($executable) {
		return Globus::GRAM::Error::RSL_EXECUTABLE();
	}
	my $boinc_app;
	chomp ($boinc_app = `$find_app $executable 2>/dev/null`);
	unless ($boinc_app) {
		return Globus::GRAM::Error::EXECUTABLE_NOT_FOUND;
	}

	# Check that we have a valid STDIN file.
	unless ($description->stdin()) {
		return Globus::GRAM::Error::RSL_STDIN;
	}
	unless (-r $description->stdin()) {
		return Globus::GRAM::Error::STDIN_NOT_FOUND;
	}

	# Check that we have a valid scratch directory to work in.
	unless ($description->directory) {
		return Globus::GRAM::Error::RSL_DIRECTORY;
	}

	my $dir = $description->directory();
	if ($dir =~ m/.output/) {
		$dir .= "/../";
	}
	chdir $dir or return Globus::GRAM::Error::BAD_DIRECTORY();

	return $boinc_app;
}

################################################################################
# This function returns a string representing the environment variables.
sub get_environment {
	my ($self, $description) = @_;
	my $env = "";
	foreach my $pair($description->environment) {
		if ($env ne "") {
			$env .= "&";
		}
		$env .= ($pair->[0] . "=" . $pair->[1]);
	}
	return $env;
}

################################################################################
# Write heterogeneous batch workunit template files.
sub write_wu_templates {
	my ($self, $description, $args, $stagein_files, $perjob_files, $nodelete) =
			@_;
	my @wu_filenames;
	my $env = $self->get_environment($description);
	my $i = 1;
	foreach my $per_files (@$perjob_files) {	
		open (WU_OUT, ">$wu_filename.$i")
				|| return Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED();
		print WU_OUT "<input_template>\n";
		# File info blocks.
		print WU_OUT "<file_info>\n<number>0</number>\n";
		if ($nodelete == 1) {
			print WU_OUT "<no_delete/>\n";
		}
		print WU_OUT "</file_info>\n";
		print WU_OUT make_wu_file_info_blocks($stagein_files, $per_files,
				$nodelete);

		my $arg = "";
		# Catch the case where args is empty.
		if ($args ne "") {
			$arg = @$args[$i - 1];
		}
		print WU_OUT "<workunit>\n<command_line>$arg</command_line>\n";
#		print WU_OUT "<env_vars>$env</env_vars>\n";

		# File ref blocks, starting with boinc_stdin.
		print WU_OUT "<file_ref>\n";
		print WU_OUT "<file_number>0</file_number>\n";
		print WU_OUT "<open_name>boinc_stdin</open_name>\n";
		print WU_OUT "</file_ref>\n";

		print WU_OUT $self->make_wu_file_ref_blocks($stagein_files, $per_files);

		print WU_OUT "</workunit>\n";
		print WU_OUT "</input_template>\n";
		close WU_OUT;

		# Add the wu filename to an array (used later in create_work).
		push(@wu_filenames, "$wu_filename.$i");
		$i++;
	}
	return \@wu_filenames;
}

# This function writes a workunit template for BOINC.
sub write_wu_template {
	my ($self, $description, $args, $stagein_files, $nodelete) = @_;

#	my $uniqid = $description->uniqid;
	my $env = $self->get_environment($description);

	open (WU_OUT, ">$wu_filename")
			|| return Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED();
	print WU_OUT "<input_template>\n";
	# File info blocks.
	print WU_OUT "<file_info>\n<number>0</number>\n";
	if ($nodelete == 1) {
		print WU_OUT "<no_delete/>\n";
	}
	print WU_OUT "</file_info>\n";  # boinc_stdin
	print WU_OUT make_wu_file_info_blocks($stagein_files, 0, $nodelete);

	print WU_OUT "<workunit>\n<command_line>$args</command_line>\n";
#	print WU_OUT "<env_vars>$env</env_vars>\n";

	# File ref blocks, starting with boinc_stdin.
	print WU_OUT "<file_ref>\n";
	print WU_OUT "<file_number>0</file_number>\n";
	print WU_OUT "<open_name>boinc_stdin</open_name>\n";
	print WU_OUT "</file_ref>\n";

	print WU_OUT $self->make_wu_file_ref_blocks($stagein_files, 0);

	print WU_OUT "</workunit>\n";
	print WU_OUT "</input_template>\n";
	close WU_OUT;
	return $wu_filename;
}

##############################################################################
# Write <file_info> blocks for work units.
sub make_wu_file_info_blocks {
	my $files = shift;
	my $perjob_files = shift;
	my $nodelete = shift;
	my $blocks;
	my $i = 1;	# File 0 is boinc_stdin.

	foreach my $file(@$files) {
		$blocks .= "<file_info>\n<number>$i</number>\n";
		if ($nodelete == 1) {
			$blocks .= "<no_delete/>\n";
		}
		$blocks .= "</file_info>\n";
		$i++;
	}
	if ($perjob_files != 0) {
		foreach my $file(@$perjob_files) {
			$blocks .= "<file_info>\n<number>$i</number>\n";
			if ($nodelete == 1) {
				$blocks .= "<no_delete/>\n";
			}
			$blocks .= "</file_info>\n";
			$i++;
		}
	}
	return $blocks;
}

################################################################################
# Write <file_ref> blocks for work units.
sub make_wu_file_ref_blocks {
	my ($self, $files, $perjob_files) = @_;
	my $i = 1;  # File 0 is boinc_stdin.
	my $blocks;

	foreach my $file(@$files) {
		# We just want the file's basename -- no path information. If it's in the
		# staging directory, it will have path info. So, we strip that off.
		my $basename = $file;
		$basename =~ /([^\/]+)$/;
		$basename = $1;

		$blocks .= "<file_ref>\n<file_number>$i</file_number>\n";
		$blocks .= "<open_name>$basename</open_name>\n<copy_file/>\n</file_ref>\n";
		$i++;
	}

	if ($perjob_files != 0) {
		my $index = $i;
		foreach my $file(@$perjob_files) {
			my $basename = $file;
			$basename =~ /([^\/]+)$/;
			$basename = $1;

			$blocks .= "<file_ref>\n<file_number>$i</file_number>\n";
			$blocks .=
					"<open_name>$basename</open_name>\n<copy_file/>\n</file_ref>\n";
			$i++;
		}
	}
	return $blocks;
}

################################################################################
# This function writes result unit templates for BOINC.
# Note the two hard-coded file_info and file_ref blocks: these are for
# boinc_stdout and boinc_stderr, which represent the stdout and stderr of the
# program being executed.
sub write_ru_template {

	my ($description, $stageout_files, $ru_filename, $nodelete) = @_;
#	my $uniqid = $description->uniqid;

	# Now, the result unit template.

	open (RU_OUT, ">$ru_filename") ||
			return Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED();
	print RU_OUT (<<EOF);
<output_template>
<file_info>
    <name><OUTFILE_0/></name>
    <generated_locally/>
    <upload_when_present/>
    <max_nbytes>$ru_max_nbytes</max_nbytes>
    <url><UPLOAD_URL/></url>
</file_info>
<file_info>
    <name><OUTFILE_1/></name>
    <generated_locally/>
    <upload_when_present/>
    <max_nbytes>$ru_max_nbytes</max_nbytes>
    <url><UPLOAD_URL/></url>
</file_info>
EOF
	print RU_OUT make_ru_file_info_blocks($stageout_files, $nodelete);
	print RU_OUT (<<EOF);
<result>
    <file_ref>
        <file_name><OUTFILE_0/></file_name>
        <open_name>boinc_stdout</open_name>
    </file_ref>
    <file_ref>
        <file_name><OUTFILE_1/></file_name>
        <open_name>boinc_stderr</open_name>
    </file_ref>
EOF
	print RU_OUT make_ru_file_ref_blocks($stageout_files);
	print RU_OUT "</result>\n";
	print RU_OUT "</output_template>\n";
	close RU_OUT;

#	print LOG "Method: ", ($description->filestageout())[0]->[0], "\n";
	return $ru_filename;
}

################################################################################
# This function returns a reference to an array containing the (local) names of
# files that need to be staged out by Globus. 
sub get_ru_stageout_files {
	my $description = shift;
	my @files;
	foreach my $pair($description->filestageout()) {
		next if $pair->[0] =~ /x-gass/;
		my $file = $pair->[0];
		next if ($file =~ /\.globus/);
		$file =~ /([^\/]+)$/;	 # Match the basename.
		$file = $1;
		push @files, $file;  # [0] is the local file; [1] remote.
	}
	return \@files;
}

################################################################################
# This function generates the <file_info> blocks for result unit template files.
sub make_ru_file_info_blocks {
	my $files = shift;
	my $nodelete = shift;
	my $blocks;
	my $i = 2;  # Files 0 and 1 are stdout and stderr, respectively.

	foreach my $file(@$files) {
		$blocks .= "<file_info>\n<name><OUTFILE_$i/></name>\n";
		$blocks .= "<generated_locally/>\n<upload_when_present/>\n";
		$blocks .= "<max_nbytes>$ru_max_nbytes</max_nbytes>\n";
		$blocks .= "<url><UPLOAD_URL/></url>\n";
		if ($nodelete == 1) {
			$blocks .= "<no_delete/>\n";
		}
		$blocks .= "</file_info>\n";
		$i++;
	}
	return $blocks;
}

################################################################################
# This function generates the <file_ref> blocks for result unit template files.
sub make_ru_file_ref_blocks {
	my $files = shift;
	my $i = 2;	# Files 0 and 1 are stdout and stderr, respectively.
	my $blocks;

	foreach my $file(@$files) {
		my $basename = $file;
		$basename =~ /([^\/]+)$/;
		$basename = $1;

		next if (($basename =~ /stdout/) || ($basename =~ /stderr/));

		$blocks .= "<file_ref>\n<file_name><OUTFILE_$i/></file_name>\n";
		$blocks .= "<open_name>$basename</open_name>\n<copy_file/>\n";
		$blocks .= "</file_ref>\n";
		$i++;
	}
	return $blocks;
}

################################################################################
# Make a call to the boinc create_work program to enter a workunit into the
# BOINC database. This function depends on the work and result unit template
# files already being written (wu_filename, ru_filename).
# Memory and disk bound variables are in bytes.
sub create_work {
	my ($wu_filenames, $ru_filename, $boinc_app, $wu_name, $wall_bound,
			$cpu_bound, $cpu_est, $mem_bound, $disk_bound, $boinc_files, $batch,
			$perjob_boinc_files) = @_;

	print LOG "The boinc files are: $boinc_files\n";

	my $file_str = join(" ", @$boinc_files);

	$ru_filename =~ /\/([^\/]*)$/;
	my $relative_ru_filename = $1;

	# With the templates written, we can go ahead and call create_work to generate
	# a BOINC work unit.
	if ($batch == 1) {
		@$wu_filenames[0] =~ /\/([^\/]*)$/;
		my $relative_wu_filename = $1;

		my $cmd = ("cd $project_dir && $create_work -appname $boinc_app "
				. "-wu_name $wu_name -wu_template templates/$relative_wu_filename "
				. "-result_template templates/$relative_ru_filename "
				. "-rsc_memory_bound $mem_bound -rsc_fpops_est $cpu_est "
				. "-rsc_fpops_bound $cpu_bound -rsc_disk_bound $disk_bound "
				. "-delay_bound $wall_bound -target_nresults 2 -min_quorum 1 "
				. "--max_error_results 10 --max_total_results 30 "
				. "--max_success_results 18 $file_str 2>/tmp/creatework.err");
		system($cmd);
		print LOG "$cmd\n";
		`echo $ru_filename >> /tmp/creatework.err`;
		unless (($? >> 8) == 0) {
			# Non-zero exit code.
			return Globus::GRAM::Error::INVALID_SCRIPT_REPLY();
		}
	} else {
		my @b_arry = split(/\./, $wu_name);
		my $b_name = $b_arry[0];
#		print LOG "Changed $b_arry[0] $b_name\n";

		# Homogeneous batches will have only one value, heterogeneous batches will
		# have more. Use this to tell the difference.
		if (@$wu_filenames > 1) {
			my $i = 0;
			my $b = 1;
			foreach my $wu (@$wu_filenames) {
				$wu =~ /\/([^\/]*)$/;
				my $relative_wu_filename = $1;

				my @temp_files = @{@$perjob_boinc_files[$i]};
				my $s = scalar(@{@$perjob_boinc_files[$i]});
				print LOG "size: $s\n";
				my $per_files = join(" ", @temp_files);
				my $cmd = ("cd $project_dir && $create_work -appname $boinc_app "
						. "-wu_name $wu_name.$b "
						. "-wu_template templates/$relative_wu_filename "
						. "-result_template templates/$relative_ru_filename "
						. "-rsc_memory_bound $mem_bound -rsc_fpops_est $cpu_est "
						. "-rsc_fpops_bound $cpu_bound -rsc_disk_bound $disk_bound "
						. "-delay_bound $wall_bound -target_nresults 2 -min_quorum 1"
						.	"--max_error_results 10 --max_total_results 30 "
						. "--max_success_results 18 -batch $b_name "
						. "$file_str $per_files 2>/tmp/creatework.err");
				system($cmd);
				print LOG "$cmd\n";
				`echo $ru_filename >> /tmp/creatework.err`;
				unless (($? >> 8) == 0) {
					# Non-zero exit code.
					return Globus::GRAM::Error::INVALID_SCRIPT_REPLY();
				}
				$b++;
				$i++;
			}
		} else {  # Oversubmit homogeneous batches by a configurable amount.
			my $oversubmit = 0.8;  # 125%.
			my $oversubreps = sprintf("%.0f", ($batch / $oversubmit));
			while ($oversubreps > 0) {
				@$wu_filenames[0] =~ /\/([^\/]*)$/;
				my $relative_wu_filename = $1;

				my $cmd = ("cd $project_dir && $create_work -appname $boinc_app "
						. "-wu_name $wu_name.$oversubreps "
						. "-wu_template templates/$relative_wu_filename "
						. "-result_template templates/$relative_ru_filename "
						. "-rsc_memory_bound $mem_bound -rsc_fpops_est $cpu_est "
						. "-rsc_fpops_bound $cpu_bound -rsc_disk_bound $disk_bound "
						. "-delay_bound $wall_bound -target_nresults 2 -min_quorum 1 "
						. "--max_error_results 10 --max_total_results 30 "
						. "--max_success_results 18 -batch $b_name "
						. "$file_str 2>/tmp/creatework.err");
				system($cmd);
				print LOG "$cmd\n";
				`echo $ru_filename >> /tmp/creatework.err`;
				unless (($? >> 8) == 0) {
					# Non-zero exit code.
					return Globus::GRAM::Error::INVALID_SCRIPT_REPLY();
				}
				$oversubreps--;
			}
		}
	}
}

################################################################################
# This function creates a Perl script which is called by the assimilator
# component of BOINC (once a sufficient number of result units have been
# returned). It copies files from BOINC locations to Globus locations.
sub write_assimilator_script {
	my ($self, $description, $scratch_dir, $job_id, $workunit_name,
			$stagein_files, $stageout_files, $wu_ru_path, $batch, $wall_bound,
			$cpu_bound, $cpu_est, $mem_bound, $disk_bound) = @_;
	my $stdout_dest = $description->stdout;
	my $stderr_dest = $description->stderr;
	my $script_name = "$project_dir/templates/boinc_script.$workunit_name";
#	my $stageout_dir = $self->get_stage_dir();
	my $stageout_dir = $description->directory();
	my $dir = "$description->directory/$job_id.output";
	my $garliconf = "";
	foreach my $stageinfile (@$stagein_files) {
		if ($stageinfile =~ /garli\.conf/) {
			# Substitute for ${GSBL_CONFIG_DIR}, if necessary.
			$stageinfile =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;
			$garliconf = $stageinfile;
			last;
		}
	}

	# This file is for keeping this script persistent for batch runs.
	# At the end of the last run, this script will unlink this file as well as the
	# script itself.
	open BATCH_COUNT, ">$project_dir/templates/$job_id.batch" || dir $!;
	print BATCH_COUNT "0";
	close BATCH_COUNT;

	if ($workunit_name =~ /initial/) {  # For initial optimization jobs.

		open (SCRIPT, ">$script_name") || die $!;
		print SCRIPT (<<EOF);
#!/usr/bin/perl
use strict;
use DBI;

my \$workunit_name = "$workunit_name";

# First arg is batch ID # (0 = single job).
my \$bid = \$ARGV[0];

if (\$bid ne "0") {
	\$workunit_name .= ("." . \$bid);
}

#my \$cred = \$ARGV[1];
# Args 3-n are the paths to the output files.
#my \$boincstdout = \$ARGV[3];
#my \$boincstderr = \$ARGV[4];
#my \$otherdir = "$stageout_dir";
my \$project_dir = "$project_dir";

my \$db = \"DBI:mysql:database=$db_name:host=$grid_host\";
my \$dbh;
my \$sth;

my \$job_id = \"$job_id\";

\$dbh = DBI->connect(\$db, \"$db_user\", \"$db_pass\", {RaiseError => 1});

# Check to see if there are two or more valid results for this workunit; if so,
# avoid creating any more workunits.
#\$sth = \$dbh->prepare("SELECT id FROM workunit WHERE name = '\$workunit_name'");
#\$sth->execute();
#my \@row = \$sth->fetchrow_array;
#my \$workunitid = \$row[0];
#\$sth = \$dbh->prepare("SELECT count(*) FROM result WHERE workunitid = '\$workunitid' and validate_state = 1");
#\$sth->execute();
#\@row = \$sth->fetchrow_array;
#my \$numvalidresults = \$row[0];
#if (\$numvalidresults >= 2) {
#	\$sth->finish();
#	exit;
#}

# Determine the canonical result number.
\$sth = \$dbh->prepare("SELECT canonical_resultid FROM workunit WHERE name = '\$workunit_name'");
\$sth->execute();
my \@row = \$sth->fetchrow_array;
my \$canonical_resultid = \$row[0];

\$sth = \$dbh->prepare("SELECT name FROM result WHERE id = '\$canonical_resultid'");
\$sth->execute();
\@row = \$sth->fetchrow_array;
\$sth->finish();
my \$canonical_resultname = \$row[0];

my \$canonical_resultnumber = "";
if (\$canonical_resultname =~ /.*_([0-9]*)/) {
	\$canonical_resultnumber = \$1;
}
# Debugging.
`echo "canonical_resultid: \$canonical_resultid  canonical_resultname: \$canonical_resultname  canonical_resultnumber: \$canonical_resultnumber" > /tmp/canonical_result_info`;

# Stage in the checkpoint files and garli.screen.log from the _initial WU these
# should be the last four output files (args 3 through n are output files).
my \$arglength = \@ARGV;
my \@output_files_staged_in = ();
# Files n-4 through n-1 are checkpoint files; file n is the garli.screen.log.
my \$i = (\$arglength - 7);
my \$endpos = (\$i + 4);
my \$output_file = "";
for (\$i; \$i < \$endpos; \$i++) {
	\$output_file = (\$workunit_name . "_" . \$canonical_resultnumber . "_" . \$i);
	push(\@output_files_staged_in, \$output_file);
}

# Determine how many input files there are.
my \$wutemplate = (\$project_dir . "/templates/wu." . \$job_id);
my \$numinputfiles = `grep -c "<number>" \$wutemplate`;
chomp(\$numinputfiles);

# Add four additional input files.
my \$j = \$numinputfiles;
foreach my \$output_file_staged_in (\@output_files_staged_in) {
	# Determine location in upload directory of output file to be staged in.
	my \$upload_hier_location =
			`cd \$project_dir && ./bin/upload_dir_hier_path \$output_file_staged_in`;
	chomp(\$upload_hier_location);

	my \$additional_input_file = "$job_id";
	if (\$bid eq "0") {
		\$additional_input_file .= ("_" . \$j);
	} else {
		\$additional_input_file .= ("." . \$bid . "_" . \$j);
	}

	# Determine location in download directory in which to place output file.
	my \$download_hier_location =
			`cd \$project_dir && ./bin/dir_hier_path \$additional_input_file`;
	chomp(\$download_hier_location);

	# Now, copy the file from the upload directory to the download directory.
	my \$retval = `cp \$upload_hier_location \$download_hier_location`;
	unless ((\$? >> 8) == 0) {
		# Copy failed -- save error message.
		`echo "attempted cp \$upload_hier_location \$download_hier_location" > /tmp/copy_failed`;
		`echo "error: \$?" >> /tmp/copy_failed`;
		`echo "retval: \$retval" >> /tmp/copy_failed`;
	}
	\$j++;
}

# Create input files string for the create_work call.
my \$input_files_str = "";
for (my \$k = 0; \$k < \$numinputfiles; \$k++) {
	\$input_files_str .= ("$job_id" . "_" . \$k . " ");
}
for (my \$k = \$numinputfiles; \$k <= (\$numinputfiles + 3); \$k++) {
	\$input_files_str .= "$job_id";
	if (\$bid eq "0") {
		\$input_files_str .= ("_" . \$k . " ");
	} else {
		\$input_files_str .= ("." . \$bid . "_" . \$k . " ");
	}
}

# Assume garli.conf is input file _1; replace it with conf containing restart=1.
my \$confsearchstr = ("$job_id" . "_1");
my \$confreplacestr = ("$job_id" . "_restart");
\$input_files_str =~ s/\$confsearchstr/\$confreplacestr/g;

# Determine the hr_class of the _initial workunit.
\$sth = \$dbh->prepare("SELECT hr_class FROM workunit WHERE name = '\$workunit_name'");
\$sth->execute();
\@row = \$sth->fetchrow_array;
my \$hr_class = \$row[0];
\$sth->finish();
\$dbh->disconnect();

# Now we submit the _main_1 workunit, which must have the same HR class as the
# _initial workunit override the wall_bound; set it to 12 hours for now.
my \$cmd = ("cd \$project_dir && ./bin/create_work -appname garli "
		. "-wu_name $job_id" . "_main_1");
if (\$bid ne "0") {
	\$cmd .= ("." . \$bid . " ");
} else {
	\$cmd .= " ";
}

my \@b_arry = split(/\\./, "$job_id");
my \$b_name = \$b_arry[0];

\$cmd .= ("-wu_template templates/wu.$job_id"
	. "_with_checkpoint_files_no_delete -result_template templates/ru.$job_id"
	. "_checkpoint_files -rsc_memory_bound $mem_bound -rsc_fpops_est $cpu_est "
	. "-rsc_fpops_bound $cpu_bound -rsc_disk_bound $disk_bound "
	. "-delay_bound 43200 -hr_class \$hr_class -target_nresults 2 -min_quorum 1 "
	. "--max_error_results 10 --max_total_results 30 --max_success_results 18 "
	. "-batch \$b_name \$input_files_str 2>>/tmp/creatework_main_1.err");
system(\$cmd);
`echo "attempted \$cmd" >> /tmp/creatework_main_1.err`;
unless (($? >> 8) == 0) {
	# Non-zero exit code.
	# Create_work failed -- save error message.
	`echo "error: \$?" >> /tmp/creatework_main_1.err`;
}

EOF

		close SCRIPT;
		`chmod a+x $script_name`;

	} elsif ($workunit_name =~ /main/) {  # For _main_n workunits.

		open (SCRIPT, ">$script_name") || die $!;
		print SCRIPT (<<EOF);
#!/usr/bin/perl
use strict;
use DBI;

# First arg is batch ID # (0 = single job).
my \$bid = \$ARGV[0];
#my \$cred = \$ARGV[1];
# Args 3-n are the paths to the output files.
#my \$boincstdout = \$ARGV[3];
#my \$boincstderr = \$ARGV[4];
#my \$otherdir = "$stageout_dir";

# The main workunit number (e.g., the "1" in "_main_1", the "2" in "_main_2",
# etc.) is a script argument.
my \$mainwunum = \$ARGV[2];
my \$workunit_name = ("$workunit_name" . "_" . "\$mainwunum");
if (\$bid ne "0") {
	\$workunit_name .= ("." . \$bid);
}

my \$project_dir = "$project_dir";

my \$db = \"DBI:mysql:database=$db_name:host=$grid_host\";
my \$dbh;
my \$sth;

my \$job_id = \"$job_id\";

\$dbh = DBI->connect( \$db, \"$db_user\", \"$db_pass\", {RaiseError => 1} );

# Check to see if there are two or more valid results for this workunit; if so,
# avoid creating any more workunits.
#\$sth = \$dbh->prepare("SELECT id FROM workunit WHERE name = '\$workunit_name'");
#\$sth->execute();
#my \@row = \$sth->fetchrow_array;
#my \$workunitid = \$row[0];
#\$sth = \$dbh->prepare("SELECT count(*) FROM result WHERE workunitid = '\$workunitid' and validate_state = 1");
#\$sth->execute();
#\@row = \$sth->fetchrow_array;
#my \$numvalidresults = \$row[0];
#if (\$numvalidresults >= 2) {
#	\$sth->finish();
#	exit;
#}

# Determine the canonical result number.
\$sth = \$dbh->prepare("SELECT canonical_resultid FROM workunit WHERE name = '\$workunit_name'");
\$sth->execute();
my \@row = \$sth->fetchrow_array;
my \$canonical_resultid = \$row[0];

\$sth = \$dbh->prepare("SELECT name FROM result WHERE id = '\$canonical_resultid'");
\$sth->execute();
\@row = \$sth->fetchrow_array;
\$sth->finish();
my \$canonical_resultname = \$row[0];

my \$canonical_resultnumber = "";
if (\$canonical_resultname =~ /.*_([0-9]*)/) {
	\$canonical_resultnumber = \$1;
}
# Debugging.
`echo "canonical_resultid: \$canonical_resultid  canonical_resultname: \$canonical_resultname  canonical_resultnumber: \$canonical_resultnumber" > /tmp/canonical_result_info`;

# Stage in the checkpoint files and garli.screen.log from the _initial WU these
# should be the last four output files (args 3 through n are output files).
my \$arglength = \@ARGV;
my \@output_files_staged_in = ();
# Files n-4 through n-1 are checkpoint files; file n is the garli.screen.log.
my \$i = (\$arglength - 7);
my \$endpos = (\$i + 4);
my \$output_file = "";
for (\$i; \$i < \$endpos; \$i++) {
	\$output_file = (\$workunit_name . "_" . \$canonical_resultnumber . "_"
		. \$i);
	push(\@output_files_staged_in, \$output_file);
}

# Determine how many input files there are.
my \$wutemplate = (\$project_dir . "/templates/wu." . \$job_id);
my \$numinputfiles = `grep -c "<number>" \$wutemplate`;
chomp(\$numinputfiles);

# Add four additional input files.
# We will overwrite the previous four additional input files.
my \$j = \$numinputfiles;
my \$garli_screen_log_path = "";
foreach my \$output_file_staged_in (\@output_files_staged_in) {
	# Determine location in upload directory of output file to be staged in.
	my \$upload_hier_location =
			`cd \$project_dir && ./bin/upload_dir_hier_path \$output_file_staged_in`;
	chomp(\$upload_hier_location);

	my \$additional_input_file = "$job_id";
	if (\$bid eq "0") {
		\$additional_input_file .= ("_" . \$j);
	} else {
		\$additional_input_file .= ("." . \$bid . "_" . \$j);
	}

	# Determine location in download directory in which to place output file.
	my \$download_hier_location =
			`cd \$project_dir && ./bin/dir_hier_path \$additional_input_file`;
	chomp(\$download_hier_location);

	# Now, copy the file from the upload directory to the download directory.
	my \$retval = `cp \$upload_hier_location \$download_hier_location`;
	unless ((\$? >> 8) == 0) {
		# Copy failed -- save error message.
		`echo "attempted cp \$upload_hier_location \$download_hier_location" > /tmp/copy_failed`;
		`echo "error: \$?" >> /tmp/copy_failed`;
		`echo "retval: \$retval" >> /tmp/copy_failed`;
	}

	# Save the path to the garli.screen.log specifically.
	if (\$j == (\$numinputfiles + 3)) {
		\$garli_screen_log_path = \$download_hier_location;
	}
	\$j++;
}

my \$next_main_wu_num = (\$mainwunum + 1);
my \$next_workunit_name = ("$job_id" . "_main_" . "\$next_main_wu_num");
if (\$bid ne "0") {
	\$next_workunit_name .= ("." . \$bid);
}
my \$next_workunit_wu_template = ("templates/wu.$job_id" . "_with_checkpoint_files_no_delete");
my \$next_workunit_ru_template = ("templates/ru.$job_id" . "_checkpoint_files");

# Check to see if we're ready for final optimization.
my \$grepoutput1 = `grep -c "Terminating run before final optimization" \$garli_screen_log_path`;
chomp(\$grepoutput1);
# Also, check to see if there was a glitch and somehow a main workunit completed
# a run.
my \$grepoutput2 = `grep -c "The checkpoint loaded indicates that this run already completed" \$garli_screen_log_path`;
chomp(\$grepoutput2);

if ((\$grepoutput1 >= 1) || (\$grepoutput2 >= 1)) {
	# Submit the _final workunit.
	# /* We need to set stoptime to a large number in the garli.conf file; we can
	# simply modify the conf in the Globus scratch dir, because copies in the
	# BOINC download dir symlink here. */
#	`perl -pi -e 's/stoptime = [0-9]*/stoptime = 5000000/g;' $garliconf`;
	\$next_workunit_name = ("$job_id" . "_final");
	if (\$bid ne "0") {
		\$next_workunit_name .= ("." . \$bid);
	}
	\$next_workunit_wu_template = ("templates/wu.$job_id"
			. "_with_checkpoint_files");
	\$next_workunit_ru_template = "templates/ru.$job_id";
}

# Create input files string for the create_work call.
my \$input_files_str = "";
for (my \$k = 0; \$k < \$numinputfiles; \$k++) {
	\$input_files_str .= ("$job_id" . "_" . \$k . " ");
}
for (my \$k = \$numinputfiles; \$k <= (\$numinputfiles + 3); \$k++) {
	\$input_files_str .= "$job_id";
	if (\$bid eq "0") {
		\$input_files_str .= ("_" . \$k . " ");
	} else {
		\$input_files_str .= ("." . \$bid . "_" . \$k . " ");
	}
}

# Assume garli.conf is input file _1; replace it with conf containing restart=1.
my \$confsearchstr = ("$job_id" . "_1");
my \$confreplacestr = ("$job_id" . "_restart");
\$input_files_str =~ s/\$confsearchstr/\$confreplacestr/g;

# Determine the hr_class of the _main workunit.
\$sth = \$dbh->prepare("SELECT hr_class FROM workunit WHERE name = '\$workunit_name'");
\$sth->execute();
\@row = \$sth->fetchrow_array;
my \$hr_class = \$row[0];
\$sth->finish();
\$dbh->disconnect();

my \@b_arry = split(/\\./, "$job_id");
my \$b_name = \$b_arry[0];

my \$delay_bound = 172800;  # Two days for a _final workunit.
if (\$next_workunit_name =~ /main/) {
	\$delay_bound = 43200;  # 12 hours for a _main workunit.
}

# Now submit either the next _main workunit or the _final workunit, which must
# have the same HR class as the current _main workunit.
my \$cmd = "cd \$project_dir && ./bin/create_work -appname garli "
	. "-wu_name \$next_workunit_name -wu_template \$next_workunit_wu_template "
	. "-result_template \$next_workunit_ru_template -rsc_memory_bound $mem_bound "
	. "-rsc_fpops_est $cpu_est -rsc_fpops_bound $cpu_bound "
	. "-rsc_disk_bound $disk_bound -delay_bound \$delay_bound "
	. "-hr_class \$hr_class -target_nresults 2 -min_quorum 1 "
	. "--max_error_results 10 --max_total_results 30 --max_success_results 18 "
	. "-batch \$b_name "
	. "\$input_files_str 2>>/tmp/creatework_nextmain_or_final.err";
system(\$cmd);
`echo "attempted \$cmd" >> /tmp/creatework_nextmain_or_final.err`;
unless (($? >> 8) == 0) {
	# Non-zero exit code.
	# Create_work failed -- save error message.
	`echo "error: \$?" >> /tmp/creatework_nextmain_or_final.err`;
}

EOF

		close SCRIPT;
		`chmod a+x $script_name`;

	} else {  # For regular workunits and workunits with name containing "_final".

		open (SCRIPT, ">$script_name") || die $!;

		# boinc_prefix will be config.upload_dir/###/canonical_result.name
		# boinc_prefix_0 is stdout, _1 is stderr.

		# This script fragment copies the standard output and standard error files
		# from the BOINC locations to the place where Globus expects to find them.
		print SCRIPT (<<EOF);
#!/usr/bin/perl
use strict;
use DBI;
	
my \$workunit_name = "$workunit_name";
# First arg is batch id # (if 0, single job).
my \$bid = \$ARGV[0];
my \$cred = \$ARGV[1];
# Args 3-n are the paths to the output files.
my \$boincstdout = \$ARGV[3];
my \$boincstderr = \$ARGV[4];
my \$otherdir = "$stageout_dir";
my \$project_dir = "$project_dir";
my \$job_id = \"$job_id\";
my \$batch = $batch;
my \$line;

my \$db = \"DBI:mysql:database=$grid_db:host=$grid_host\";
my \$dbh;
my \$sth;

# Remove all initial and main output files.
if (\$bid > 0) {
	my \$findstringinitial = (\"*\" . \$job_id . \"_initial.\" . \$bid . \"*\");
	my \$findstringmain = (\"*\" . \$job_id . \"_main_*.\" . \$bid . \"*\");
	`find /fs/mikedata/threonine/work/boinc5/upload -name \"\$findstringinitial\" -exec rm -f \\{\\} \\\\;`;
	`find /fs/mikedata/threonine/work/boinc5/upload -name \"\$findstringmain\" -exec rm -f \\{\\} \\\\;`;
} else {
	`find /fs/mikedata/threonine/work/boinc5/upload -name \"*\$job_id*_initial*\" -exec rm -f \\{\\} \\\\;`;
	`find /fs/mikedata/threonine/work/boinc5/upload -name \"*\$job_id*_main_*\" -exec rm -f \\{\\} \\\\;`;
}

# Update credit totals in the grid database.
\$dbh = DBI->connect(\$db, \"$grid_user\", \"$grid_pass\", {RaiseError => 1});
\$dbh->do(\"UPDATE user SET boinc_cred=boinc_cred+\\"\$cred\\" WHERE user_name=\\"gt4admin\\" \");
\$dbh->do(\"UPDATE job SET boinc_cred=\\"\$cred\\" WHERE wu_id=\\"\$job_id\\"\");
\$dbh->disconnect();

if (\$batch > 1) {
	open RCOUNT, "<\$project_dir/templates/$job_id.batch" or die "Cannot open counter to read";
	\$line = readline(RCOUNT);
	close RCOUNT;
}

# If we have already reached the required number of completed workunits (batch
# size), abort because we do not want to copy any files or do anything else.
if (\$line >= \$batch) {
	exit;
}

if (\$bid > 0) {
	my \$tempotherdir;
	\$tempotherdir = (\$otherdir . "/job" . (\$bid - 1) . "/");
	# If this an "extra" replicate that is the result of an oversubmission,
	# otherdir will not exist or, perhaps the specified directory has already been
	# used (already has files in it).
	if (!(-e \$tempotherdir) || (-e "\$tempotherdir/stdout")) {
		# We need to find an empty directory for these files (there *should* be
		# one).
		my \$jobdir;
		my \$foundemptydir = 0;
		for (\$jobdir = 0; \$jobdir <= (\$batch - 1); \$jobdir++) {
			\$tempotherdir = (\$otherdir . "/job" . \$jobdir . "/");
			if (!-e "\$tempotherdir/stdout") {
				\$otherdir = \$tempotherdir;
				\$foundemptydir = 1;
				last;
			}
		}
		if (\$foundemptydir == 0) {
			# We found no empty directory; this shouldn't happen.
			\$otherdir = \$tempotherdir;  # Copy into the last folder.
			my \$debugemptydir = (\$job_id . "." . \$bid);
			`touch /tmp/\$debugemptydir`;
		}
	} else {
	\$otherdir = \$tempotherdir;
}

if (-e \$boincstdout) {
	`cp \$boincstdout '\$otherdir/stdout'`;
} else {
	`touch '\$otherdir/stdout'`;
}
if (-e \$boincstderr) {
	`cp \$boincstderr '\$otherdir/stderr'`;
} else {
	`touch '\$otherdir/stderr'`;
}
# Set up other output files to go to the right directory.
} else {
	# Copy std out and err.
	# Support for blank stdout.
	if (-e \$boincstdout) {	
		`cp \$boincstdout '$stdout_dest'`;
	} else {
		`touch '$stdout_dest'`;
	}
	# Support for blank stderr.
	if (-e \$boincstderr) {
		`cp \$boincstderr '$stderr_dest'`;
	} else {
		`touch '$stderr_dest'`;
	}
}
EOF

		# This loop writes lines to the script that cause it to copy those files
		# that need to be staged out from the BOINC upload directory to the
		# locations where the Globus stage out code will expect to find them.
		# _0 and _1 are boinc_stdout and boinc_stderr, respectively, and we skip
		# over these.
		my $i = 5;
		foreach my $file (@$stageout_files) {
			print SCRIPT "`cp '\$ARGV[$i]' '\$otherdir/$file'`;\n";
			$i++;
		}

		print SCRIPT (<<EOF);

if (\$batch == 1) {
	# Temporarily commenting this for debugging purposes.
#	unlink("\$project_dir/templates/\$workunit_name.batch");

	# Right now the validator is using the workunit and result unit templates, so
	# we can't delete them!
#	unlink(\"$wu_ru_path/wu.$job_id\");
#	unlink(\"$wu_ru_path/ru.$job_id\");
#	unlink(\$0);
} else {
	\$line++;
	# If we are at the batch number, we know it's time to unlink.
	if (\$line == \$batch) {
		my \@spl_id = split(/\\./, \$job_id);
		my \$batch_id = \$spl_id[0];
		\$dbh = DBI->connect(\"DBI:mysql:database=$db_name:host=$db_host\", \"$db_user\", \"$db_pass\", {RaiseError => 1});
		my \$row1 = \$dbh->do(\"UPDATE result SET batch = 0 WHERE batch = \\"\$batch_id\\" \");
		my \$row2 = \$dbh->do(\"UPDATE workunit SET batch = 0 WHERE batch = \\"\$batch_id\\" \");
#		print "ID: \$batch_id , row1: \$row1, row2: \$row2";

		# Temporarily commenting this for debugging purposes.
#		unlink("\$project_dir/templates/\$workunit_name.batch");

#		unlink( \"$wu_ru_path/wu.$job_id\" );
#		unlink( \"$wu_ru_path/ru.$job_id\" );

		# Temporarily commenting this for debugging purposes.
#		unlink(\$0);

#		exit(0);
	}
	if (\$line <= \$batch) {  # Write the new number to the file.
		open WCOUNT, ">\$project_dir/templates/$job_id.batch" or die "Cannot open counter to write!";
		print WCOUNT "\$line";
		close WCOUNT;
	}
}
EOF

		close SCRIPT;
		`chmod a+x $script_name`;
	}
}

################################################################################
# Escape the command's command-line arguments and produce an argument string.

sub process_arguments {
	my ($self, $description) = @_;
	my @args = $description->arguments();
	my %files;
	my $args;

	my @changing_args;
	return ('') if (scalar @args == 0);

	my $size;	
	foreach my $arg(@args) {
		my @arg_list = split(/,/, $arg);
		if (scalar(@arg_list) > 1) {
			$size = scalar(@arg_list);
			push(@changing_args, \@arg_list);
		}
	}

	my @arglist;
	if ($size == 0) {
		$size = 1;
	}

	for (my $i = 0; $i < $size; $i++) {
		my $count = 0;
		my $args;
		foreach (@args) {
			$_ =~ s/\\/\\\\/g;
			$_ =~ s/\$/\\\$/g;
			$_ =~ s/"/\\\"/g;
			$_ =~ s/`/\\\`/g;

			if ($_ =~ m/,/) {
				$args .= ('"' . @{$changing_args[$count]}[$i] . '" ');
				$count++;
			} else {
				$args .= ('"' . $_ . '" ');
			}
		}
		push(@arglist, $args);
	}
	return \@arglist;
}

################################################################################
# This function takes in a reference to an array of files and a scalar
# representing the ID of the workunit with which they will be associated. It
# copies the files into the BOINC download directory and gives them names
# $uniqid_0 ... $uniqid_n.
# Returns a reference to an array whose elements are the names of the files
# created in BOINC's download directory (filenames only, no path information).

sub copy_files_to_boinc {
	my ($description, $files, $uniqid, $workphasedivision) = @_;
	my $i = 1;	# File 0 is boinc_stdin.
	my @boinc_files;

	print LOG "type 1: ${uniqid}\n";

	# Stage the standard input file.
	my $stdin = $description->stdin();
	if ($stdin ne "/dev/null") {
		my $hier_location = `cd $project_dir && ./bin/dir_hier_path ${uniqid}_0`;
		print LOG "linking stdin=$stdin to BOINC file $hier_location\n";
#		`cp $stdin $hier_location`;
		`ln -s $stdin $hier_location 2>&1`;
	} else {
		my $hier_location = `cd $project_dir && ./bin/dir_hier_path ${uniqid}_0`;
		print LOG "Creating fake stdin for BOINC at $hier_location\n";
		`touch $hier_location`;
	}
	push @boinc_files, "${uniqid}_0";
	foreach my $file(@$files) {
		my $hier_location = `cd $project_dir && ./bin/dir_hier_path ${uniqid}_$i`;
#		my $ret = `cp $file $hier_location`;

		# Substitute for ${GSBL_CONFIG_DIR}, if necessary.
		$file =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;

		my $ret = `ln -s $file $hier_location 2>&1`;
		print LOG "ln -s $file $hier_location\n";
		# stderr is being redirected; not sure if this will fire now.
		unless (($? >> 8) == 0) {
			# Link failed -- delete any links we've already made and error out.
			print LOG "Error: $?: $ret\n";
			foreach my $boinc_file(@boinc_files) {
				my $hier_path = `cd $project_dir && ./bin/dir_hier_path $boinc_file`;
				unlink("$hier_path");
			}
			return Globus::GRAM::Error::STAGE_IN_FAILED();
		}

		push @boinc_files, "${uniqid}_$i";
		$i++;

		# Special check for garli.conf.
		if ($workphasedivision == 1) {
			if ($file =~ /garli\.conf/) {
				my $restartconf = ($file . "_restart");
				$hier_location =
						`cd $project_dir && ./bin/dir_hier_path ${uniqid}_restart`;
				$ret = `ln -s $restartconf $hier_location 2>&1`;
				print LOG "ln -s $restartconf $hier_location\n";
				# stderr is being redirected; not sure if this will fire now.
				unless (($? >> 8) == 0) {
					# Link failed -- delete any links we've already made and error out.
					print LOG "Error: $?: $ret\n";
					foreach my $boinc_file(@boinc_files) {
						my $hier_path =
								`cd $project_dir && ./bin/dir_hier_path $boinc_file`;
						unlink("$hier_path");
					}
					return Globus::GRAM::Error::STAGE_IN_FAILED();
				}
			}
		}
	}
	return \@boinc_files;
}

# Copy in per-job files into the BOINC download directory (only useful for
# heterogeneous batches.)
sub copy_perjob_files_to_boinc {
	my ($description, $files, $unique_id, $i, $batch) = @_;
	my @perjob_boinc_files;

	my $pwd = `pwd`;
	print LOG "PWD: $pwd\n";
	my $num = 0;
	foreach my $job (@$files) {
		my @file_array;
		my $index = $i;
		foreach my $file (@$job) {
			my $b_num = $num+1;
			# Link the per-job file into the boinc download directory.
			# Add the .batch number in order to keep them unique.
			my $boinc_file = "$unique_id.${b_num}_$index";
			my $hier_location = `cd $project_dir && ./bin/dir_hier_path $boinc_file`;
#			my $ret = `cp ${unique_id}.output/job${num}/$file $hier_location`;

			# Substitute for ${GSBL_CONFIG_DIR}, if necessary.
			$file =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;

			my $ret = `ln -s $file $hier_location 2>&1`;
			print LOG "ln -s $file $hier_location\n";
			# stderr is being redirected; not sure if this will fire now.
			unless (($? >> 8) == 0) {
				# Link failed -- delete any links we've already made and error out.
				print LOG "Error: $?: $ret\n";
				foreach my $boinc_file(@file_array) {
					my $hier_path = `cd $project_dir && ./bin/dir_hier_path $boinc_file`;
					unlink("$hier_path");
				}
				return Globus::GRAM::Error::STAGE_IN_FAILED();
			}
			push(@file_array, $boinc_file);
			$index++;
		}
		$num++;
		# This is building another array of arrays. The idea is that the create_work
		# call will use this.
		push(@perjob_boinc_files, \@file_array);
	}
	return \@perjob_boinc_files;
}

# Set job requirements (memory, cputime, walltime, etc), if any, or give
# defaults.
sub computeBounds {
	my $description = shift || die "No description passed to computeBounds";

	# Compute the memory bound, in bytes.
	my $mem_bound;
	if (defined $description->maxMemory) {
		# Convert Mb to bytes.
		$mem_bound = (($description->maxMemory * 1024) * 1024);
	} else {
		# ALB 2/15/04 lowered memory constraint slightly to allow more machines to
		# do work.
		$mem_bound = ((126 * 1024) * 1024);  # 126 Mb.
	}

	# Compute walltime bound (delay bound), coverting from minutes to seconds.
	my $wall_bound;
	if (defined $description->maxWallTime) {
		$wall_bound = ($description->maxWallTime * 60);
	} elsif (defined $description->maxTime) {
		$wall_bound = ($description->maxTime * 60);
	} else {
		$wall_bound = (86400 * 2);  # 2 days default.
#		$wall_bound = (86400 * 7);  # 7 days.
	}

	# Compute CPU bounds in number of floating point operations on the BOINC
	# reference computer (capable of 10^9 ops/sec).
	my $cpu_bound;
	if (defined $description->maxCpuTime) {
		$cpu_bound = (($description->maxCpuTime * 60) * 1E9);
	} else {
		$cpu_bound = ((86400 * 7) * 1E9);
	}
	my $cpu_est;
	if (defined $description->estCpuTime) {
		$cpu_est = (($description->estCpuTime * 60) * 1E9);
	} else {
#		$cpu_est = ($cpu_bound / 2);
		# SJM Changed estimated CPU ops, to a lower number which allows for slower
		# clients.
		$cpu_est = (1.53E13 * 8);
#		$cpu_est = 1.53E5;
		# DSM TESTING DISABLE.
#		$cpu_est = 1;
	}

	# Compute disk bound, in bytes.
	my $disk_bound;
	if (defined $description->maxDisk) {
		$disk_bound = (($description->maxDisk * 1024) * 1024);
	} else {
		$disk_bound = ((100 * 1024) * 1024);
	}
	return ($mem_bound, $wall_bound, $cpu_bound, $cpu_est, $disk_bound);
}

# Records files to be staged in.
# Not sure what this function does. This may be deprecated and no longer used.
sub stage_in {
	my $self = shift;
	my $description = $self->{JobDescription};
	my @files = $description->file_;
	my @files_shared = $description->file_stage_in_shared;
	push @files, @files_shared;

	my $file = $stagein_save_prefix.$description->uniqid;

	close STDERR;
	open(STDERR, ">/tmp/stderr");

	if (!(-f $file) && (scalar @files > 0)) {
		Storable::store(\@files, $file);
	}
	return $self->SUPER::stage_in(@_);
}

# Build an array of arrays that holds all per-job files.
sub get_perjob_files {
	my ($description, $batch) = @_;

	if (!defined($description->transfer_perjob_files()) || ($batch == 1)) {
		# This function isn't useful, so return.
		return;
	}
	my $per_string = $description->transfer_perjob_files();

	my @perjobs = split(/,/, $per_string);

	my @temp_perjobs;
	my $i;
	for ($i = 0; $i < scalar(@perjobs); $i++) {
		my @splitgroup = split(/:/, $perjobs[$i]);
		push(@temp_perjobs, \@splitgroup);
	}
	my $size = scalar(@{$temp_perjobs[0]});
	print LOG "BATCH: $batch, SIZE: $size\n";
	if ($batch != $size) {
		die "ERROR: incorrect number of jobs specfied!";
	}
	my @per_job_array;
	# Loop for the number of groups we have to make.
	for ($i = 0; $i < $size; $i++) {
		my @job_array;
		# Loop for each input set.
		foreach my $set (@temp_perjobs) {
			push(@job_array, ${$set}[$i]);
		}
		push(@per_job_array, \@job_array);
	}
	# We should now have an array of arrays, so let's return it.
	return \@per_job_array;
}

# Retrieves list of files to be staged in.
sub get_stagein_files {
	my $description = shift || die "No description passed to get_stagein_files";

	my $file = $description->boincsubmit(); 


	print LOG "input files: $file\n";

	my @stagein = split(",", $file);
	foreach my $pair(@stagein) {
		print LOG "pair: $pair\n";
	}
	return \@stagein;
}

# Retrieves list of files to be staged out.
sub get_stageout_files {
	my $description = shift || die "No description passed to get_stageout_files";

	my $file = $description->transfer_output_files();
	print LOG "output files: $file\n";
	my @stageout = split(",", $file);
	foreach my $pair(@stageout) {
		print LOG "pair: $pair\n";
	}
	return \@stageout;
}

# Returns the number of batch jobs to run. If this is a single job, 0 is
# returned.
sub get_num_batches {
	my $description = shift or die "No description passed to get_num_batches";

	if (defined($description->count())) {
		return $description->count();
	} else {
		return 1;
	}
}

# Get the grid database login information in order to use the database.
sub get_grid_db_args {
	my $grid_db_info_file = "$globus_dir/service_configurations/db.location";
	open GRID, "<$grid_db_info_file" || die "Unable to open db.location\n";
	$grid_host = readline(GRID);
	chomp($grid_host);
	$grid_user = readline(GRID);
	chomp($grid_user);
	$grid_pass = readline(GRID);
	chomp($grid_pass);
	$grid_db = readline(GRID);
	chomp($grid_db);

#	print LOG ("host: $grid_host user: $grid_user pass: $grid_pass "
#			. "name: $grid_db\n");

	close GRID;
}

# Get the BOINC database login information.
sub get_boinc_db_args {
	my $config_file = "$project_dir/config.xml";
	my $parser = new XML::Parser(ErrorContext => 2);
	$parser->setHandlers(Start => \&handle_start_xml, End => \&handle_end_xml,
			Char => \&handle_char_xml);

	$parser->parsefile($config_file);
	print LOG "BOINC database:  Host: $db_host User: $db_user Pass: $db_pass DB: $db_name\n";
}

# Perl XML start handler.
sub handle_start_xml {
	my ($expat, $tag, %attrs) = @_;
#	print LOG "Recieved tag: $tag\n";
	if (($tag eq 'db_host') || ($tag eq 'db_user') || ($tag eq 'db_passwd')
			|| ($tag eq 'db_name')) {
		$temp_tag = $tag;
#		print LOG "Got tag: $temp_tag\n";
	}
}

# Perl XML handler: handle setting the correct variables.
sub handle_char_xml {
	my ($expat, $str) = @_;
	if ($temp_tag eq 'db_host') {
		$db_host .= $str;
	} elsif ($temp_tag eq 'db_user') {
		$db_user .= $str;
	} elsif ($temp_tag eq 'db_passwd') {
		$db_pass .= $str;
	} elsif ($temp_tag eq 'db_name') {
		$db_name .= $str;
	}
}

# Perl XML handler: clean up.
sub handle_end_xml {
	my ($expat, $tag) = @_;

	if ($tag eq 'db_host') {
		$db_host = trim($db_host);
#		print LOG "Set host to $db_host\n";
	} elsif ($tag eq 'db_user') {
		$db_user = trim($db_user);
#		print LOG "Set user to $db_user\n";
	} elsif ($tag eq 'db_passwd') {
		$db_pass = trim($db_pass);
#		print LOG "Set pass to $db_pass\n";
	} elsif ($tag eq 'db_name') {
		$db_name = trim($db_name);
#		print LOG "Set name to $db_name\n";
	}	
	$temp_tag = "";
}

# Trim the string. Used by the XML end handler.
sub trim {
	my $string = shift;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}

################################################################################
######################## END submit support functions. #########################
################################################################################

# Cancel a submitted job.
sub cancel {
	my $self = shift;
	my $description = $self->{JobDescription};
	$description->save("/tmp/boinc_jm_cancel_desc");
	my $unique_id = $description->jobid();
#	my $unique_id = $description->directory();
	$unique_id =~ s/.*\///;
	if ($unique_id =~ m/output/) {
		$unique_id =~ s/.output//;
	}
	open LOG, ">>$logfile";
	print LOG "unique_id: $unique_id\n";
	$self->log("cancel job $unique_id");
#	system("$cancel $unique_id");
	my $ret = `$cancel $unique_id 2>&1`;
	print LOG "$ret\n";
	if (($? >> 8) != 0) {
		print LOG "Cancel job failed!\n";
		return Globus::GRAM::Error::JOB_CANCEL_FAILED();
	}
	close LOG;
	return {JOB_STATE => Globus::GRAM::JobState::FAILED}	
}

################################################################################
# Check the status of a submitted job.
# NOTE: This is not used since GT4. Instead, the BOINC SEG module controls the
# job status. Leaving it here anyway for legacy support.
sub poll {
	my $self = shift;
	my $description = $self->{JobDescription};
	my $job_id = $description->jobid();
	my $pollval;

	chomp ($pollval = `$poll $job_id`);
	unless (($? >> 8) == 0) {
		`echo "was polling $job_id" > /tmp/poll.err`;
		# Non-zero exit code.
		return Globus::GRAM::Error::INVALID_SCRIPT_REPLY();
	}

	if ($pollval =~ /1/) {
		`echo "\n[$job_id]PENDING" >/tmp/poll.err`;
		return {JOB_STATE => Globus::GRAM::JobState::PENDING};
	} elsif ($pollval =~ /2/) {
		`echo "\n[$job_id]ACTIVE" >/tmp/poll.err`;
		return {JOB_STATE => Globus::GRAM::JobState::ACTIVE};
	} elsif ($pollval =~ /3/) {
		`echo "\nD[$job_id]ONE" >/tmp/poll.err`;
		return {JOB_STATE => Globus::GRAM::JobState::DONE};
	} elsif ($pollval =~ /4/) {
		`echo "\nF[$job_id]AILED" >/tmp/poll.err`;
		return {JOB_STATE => Globus::GRAM::JobState::FAILED,
				ERROR => Globus::GRAM::Error::SYSTEM_CANCELLED()->value};
	} else {
		`echo "\nUNKNOWN REPONSE" >/tmp/poll.err`;
		$self->log("BOINC poll returned an unknown response. Telling JM to ignore this poll.");
		return {};
	}
}

1;
