# Copyright 1999-2006 University of Chicago
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Globus::GRAM::JobManager::condor package
#
# CVS Information:
# $Source$
# $Date$
# $Revision$
# $Author$

use Globus::GRAM::Error;
use Globus::GRAM::JobState;
use Globus::GRAM::JobManager;
use Globus::GRAM::StdioMerger;
use Globus::Core::Paths;
use Globus::Core::Config;
use IPC::Open3;

#ALB -- For DB update
use Config;
use DBI;
use lib '/usr/local/lib64/perl5/x86_64-linux-thread-multi';

# Load OSG group accounting module, if possible
eval { require Globus::GRAM::JobManager::condor_accounting_groups };
our $accounting_groups_callout = ! $@; # Don't make callout if we couldn't load module


package Globus::GRAM::JobManager::condor;

@ISA = qw(Globus::GRAM::JobManager);

my ($condor_submit, $condor_rm, $condor_config, $isNFSLite);

BEGIN
{
    my $config = new Globus::Core::Config(
            '${sysconfdir}/globus/globus-condor.conf');

    $condor_submit = $config->get_attribute("condor_submit") || "no";
    $condor_rm = $config->get_attribute("condor_rm") || "no";
    $condor_arch = $config->get_attribute("condor_arch") || undef;
    $condor_os = $config->get_attribute("condor_os") || undef;
    $condor_config = $config->get_attribute("condor_config") || "";
    $condor_check_vanilla_files = $config->get_attribute(
            "check_vanilla_files") || "no";
    $condor_mpi_script = $config->get_attribute("condor_mpi_script") || "no";

    if ($condor_config ne '')
    {
        $ENV{CONDOR_CONFIG} = $condor_config;
    }

    $isNFSLite = 0;
}

sub new
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    my $log_dir;
    my $description = $self->{JobDescription};
    my $stdout = $description->stdout();
    my $stderr = $description->stderr();
    my $globus_condor_conf = "$Globus::Core::Paths::sysconfdir/globus-condor.conf";

    if (-r $globus_condor_conf)
    {   
        local(*FH);
        
        if (open(FH, "<$globus_condor_conf"))
        {   
            while(<FH>) {
                chomp;
                if (m/^isNFSLite=([0-9]*)$/) {
                    $isNFSLite = int($1);
                    last;
                }
            }
            close(FH);
        }
    }

    if (! exists($self->{condor_logfile}))
    {
        if(! exists($ENV{GLOBUS_SPOOL_DIR}))
        {
            $log_dir = $self->job_dir(); 
        }
        else
        {
            $log_dir = $ENV{GLOBUS_SPOOL_DIR};
        }
        $self->{condor_logfile} = "$log_dir/condor." . $description->uniq_id();
    }
    if(! -e $self->{condor_logfile}) 
    {
        if ( open(CONDOR_LOG_FILE, '>>' . $self->{condor_logfile}) ) 
        {
            close(CONDOR_LOG_FILE);
        }
    }

    if($description->jobtype() eq 'multiple' && $description->count > 1)
    {
        $self->{STDIO_MERGER} =
            new Globus::GRAM::StdioMerger($self->job_dir(), $stdout, $stderr);
    }
    else
    {
        $self->{STDIO_MERGER} = 0;
    }

    # ALB -- write out job description
    $desc_file = "/tmp/condorjmdesc";
    $description->save($desc_file);

    bless $self, $class;
    return $self;
}

sub submit
{
    my $self = shift;
    my $description = $self->{JobDescription};
    my @environment;
    my $environment_string;
    my $script_filename;
    my @requirements;
    my @tmpr;
    my $rank = '';
    my @arguments;
    my $argument_string;
    my @response_text;
    my @submit_attrs;
    my $submit_attrs_string;
    my $multi_output = 0;
    my $pid;
    my $status;
    my ($condor_submit_out, $condor_submit_err);
    my $rc;
    my $scratch_isset = 0; # Flag if the SCRATCH_DIRECTORY environment variable is set indicating likely GRAM job

    # Reject jobs that want streaming, if so configured
    if ( $description->streamingrequested() &&
	 $description->streamingdisabled() ) {

	$self->log("Streaming is not allowed.");
	return Globus::GRAM::Error::OPENING_STDOUT;
    }

    if($description->jobtype() eq 'single' ||
       $description->jobtype() eq 'multiple')
    {
	$universe = 'vanilla';

        if ($description->jobtype() eq 'multiple'
                && ($description->count() > 1)) {
            $multi_output = 1;
        }
    }
    elsif($description->jobtype() eq 'condor')
    {
	# ALB -- adding support for standard universe job batches
	if($description->count() > 1) {
            $multi_output = 1;
	}
	$universe = 'standard'
    }
    elsif($description->jobtype() eq 'mpi' && $condor_mpi_script ne 'no')
    {
        $universe = 'parallel';
    }
    else
    {
	return Globus::GRAM::Error::JOBTYPE_NOT_SUPPORTED();
    }

    # Validate some RSL parameters
    if(!defined($description->directory()))
    {
        return Globus::GRAM::Error::RSL_DIRECTORY;
    }
    elsif( $description->stdin() eq '')
    {
	return Globus::GRAM::Error::RSL_STDIN;
    }
    elsif(ref($description->count()) ||
       $description->count() != int($description->count()))
    {
	return Globus::GRAM::Error::INVALID_COUNT();
    }
    elsif( $description->executable eq '')
    {
	return Globus::GRAM::Error::RSL_EXECUTABLE();
    }

    @environment = $description->environment();

    foreach my $tuple ($description->environment())
    {
        if(!ref($tuple) || scalar(@$tuple) != 2)
        {
            return Globus::GRAM::Error::RSL_ENVIRONMENT();
        }
    }

    # NFS lite start
    if ($isNFSLite && !$isManagedFork) {

        my $osg_grid = '';
        my $use_osg_grid = 1;
    
        map {
            if ($_->[0] eq "OSG_GRID") {
                $osg_grid =  $_->[1]; 
            } elsif ($_->[0] eq "OSG_DONT_USE_OSG_GRID_FOR_GL") {
                $use_osg_grid = 0;
            } elsif ($_->[0] eq "LOGNAME") {
                $logname =  $_->[1];
            } elsif ($_->[0] eq "SCRATCH_DIRECTORY") {
                $scratch_isset = 1;
                $scratch_directory =  $_->[1]; 
                $_->[1] = '$_CONDOR_SCRATCH_DIR';
            } elsif ($_->[0] eq "X509_USER_PROXY") {
                $_->[0] = "CHANGED_X509"; 
            }
        } @environment;
    
        if ($scratch_isset) {
            # Remote_InitialDir apparently suppresses the setting of the SCRATCH_DIRECTORY env variable
            push(@environment,["MY_INITIAL_DIR",'$_CONDOR_SCRATCH_DIR']);
        }
        elsif ( $description->directory() =~ m/.+$logname/xms ) {
            # If the directory ends with the logname it might be a globus-job-run job
            # so take control of the initial_dir
            push(@environment,["MY_INITIAL_DIR",'$_CONDOR_SCRATCH_DIR'] );
        }
        else {
            # assume that remote_initialdir is set and the submitter knows what they are
            # doing.
            push(@environment,["MY_INITIAL_DIR",$description->directory()] );
        }
    }       
    # NFS Lite End

    $environment_string = join(';',
                               map {$_->[0] . "=" . $_->[1]} @environment);

    # ALB -- capture scratch directory from environment string
    my $scratch_dir = "";
    if($environment_string =~ /.*SCRATCH_DIRECTORY=(.*?);.*/) {
	$scratch_dir = $1;
    }

    @arguments = $description->arguments();
    foreach (@arguments)
    {
	if(ref($_))
	{
	    return Globus::GRAM::Error::RSL_ARGUMENTS();
	}
    }
    if ($description->directory() =~ m|^[^/]|)
    {
        my $home = (getpwuid($<))[7];

        $description->add('directory', "$home/".$description->directory());
    }
    if ($description->executable() =~ m|^[^/]|)
    {
        $description->add('executable',
                $description->directory() . '/' . $description->executable());
    }
    if ($universe eq 'parallel')
    {
        unshift(@arguments, $description->executable);
        $description->add('executable', $condor_mpi_script);
    }
    if($#arguments >= 0)
    {
	# JTK -- Quotes surrounding argument file names messes up Condor.
	$argument_string = join(' ', @arguments);
#	$argument_string = '"' . join(' ',
#				map
#				{
#				    $_ =~ s/'/''/g;
#				    $_ =~ s/"/""/g;
#				    $_ = "'$_'";
#				}
#				@arguments) . '"';
    }
    else
    {
	$argument_string = '';
    }

    @submit_attrs = $description->condorsubmit();
    if(defined($submit_attrs[0]))
    {
	foreach $tuple (@submit_attrs)
	{
	    if(!ref($tuple) || scalar(@$tuple) != 2)
	    {
		return Globus::GRAM::Error::RSL_SCHEDULER_SPECIFIC();
	    }
	}
	$submit_attrs_string = join("\n",
				map {$_->[0] . "=" . $_->[1]} @submit_attrs);
    }
    else
    {
	$submit_attrs_string = '';
    }

    my $group;
    if ($accounting_groups_callout) {
       $group = Globus::GRAM::JobManager::condor_accounting_groups::obtain_condor_group(\@environment, $self);
    }

    # ALB -- moved this block down so it happens after symlink creation!
    # In the standard universe, we can validate stdin and directory
    # because they will sent to the execution host  by condor transparently.
    if($universe eq 'standard' || $condor_check_vanilla_files eq 'yes')
    {
	if(! -d $description->directory())
	{
            return Globus::GRAM::Error::BAD_DIRECTORY();
	}
	elsif(! -r $description->stdin())
	{
            return Globus::GRAM::Error::STDIN_NOT_FOUND();
	}
	elsif(! -f $description->executable())
	{
            return Globus::GRAM::Error::EXECUTABLE_NOT_FOUND();
	}
	elsif(! -x $description->executable())
	{
            return Globus::GRAM::Error::EXECUTABLE_PERMISSIONS();
	}
    }

    # Create script for condor submission
    $script_filename = $self->job_dir() . '/scheduler_condor_submit_script';

    local(*SCRIPT_FILE);

    $rc = open(SCRIPT_FILE, ">$script_filename") ;

    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "open: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }

    $rc = print SCRIPT_FILE "#\n# description file for condor submission\n#\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "Universe = $universe\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "Notification = Never\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "Executable = " . $description->executable . "\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    # ALB -- modifying requirements block
    my $operating_system = $description->operating_system();
    if (defined($operating_system)) {
	$self->log("Adding \"operating_system = $operating_system\"\n");
	if($operating_system eq "WIN") {
            my $r = "(OpSys == \"WINNT51\" || OpSys == \"WINNT52\")";
	    push(@requirements, $r);
	} else {
            my $r = "OpSys == \"" . $operating_system . "\"";
	    push(@requirements, $r);
	}

	my $architecture = $description->architecture();
	if (defined($architecture)) {
            $self->log("Adding \"architecture = $architecture\"\n");
	    if($architecture eq "INTEL" && $operating_system eq "LINUX") {
		my $r = "(Arch == \"INTEL\" || Arch == \"X86_64\")";
		push(@requirements, $r);
            }
	}
    } else {
	@tmpr = $description->condor_os;
	if (scalar(@tmpr) > 0)
	{
            my $r = "(" .
		join(" || ",
                    map {"OpSys == \"$_\""} @tmpr) .
		")";
            push(@requirements, $r);
	}
	elsif (defined($condor_os))
	{
            my $r = "(" . join(" || ",
		map { "OpSys == \"$_\"" } split(/\s+/, $condor_os)) . ")";
            push(@requirements, $r);
	}
	@tmpr = $description->condor_arch();
	if (scalar(@tmpr) > 0)
	{
            my $r = "(" .
		join(" || ", map {"Arch == \"$_\""} @tmpr) .
                    ")";
            push(@requirements, $r);
	}
	elsif (defined($condor_arch))
	{
            my $r = "(" . join(" || ",
		map { "Arch == \"$_\"" } split(/\s+/, $condor_arch)) . ")";
            push(@requirements, $r);
	}
    }
    if($description->min_memory() ne '')
    {
        push(@requirements, " Memory >= " . $description->min_memory());
    }

    # ALB -- adding minimum speed requirement
    my $r = "KFlops >= 600000 && (FileSystemDomain == \"wren.umiacs.umd.edu\")"; #|| FileSystemDomain == \"lccd.umiacs.umd.edu\")";

    #ALB -- excluding 2.4 kernels
    $r .= " && CheckpointPlatform != \"LINUX INTEL 2.4.x normal\" && CheckpointPlatform != \"LINUX INTEL 2.4.x normal N/A\"";
    push(@requirements, $r);

    if (scalar(@requirements) > 0)
    {
        $rc = print SCRIPT_FILE "Requirements = ", join(" && ", @requirements) ."\n";
    }
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }

    if ($accounting_groups_callout && $group) {
        $name = getpwuid($>);
        $rc = print SCRIPT_FILE "+AccountingGroup = \"$group.$name\"\n" if $group;
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }

    if($rank ne '')
    {
	$rc = print SCRIPT_FILE "$rank\n";
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }

    if ($ENV{X509_USER_PROXY} ne "") {
        $rc = print SCRIPT_FILE "X509UserProxy = $ENV{X509_USER_PROXY}\n";
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }
    $rc = print SCRIPT_FILE "Environment = $environment_string\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "Arguments = $argument_string\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "InitialDir = " . $description->directory() . "\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "Input = " . $description->stdin() . "\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "Log = " . $self->{condor_logfile} . "\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "log_xml = True\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    # NFS Lite mode
    if ($isNFSLite && !$isManagedFork) {
        print SCRIPT_FILE "should_transfer_files = YES\n";
        print SCRIPT_FILE "when_to_transfer_output = ON_EXIT\n";
        print SCRIPT_FILE "transfer_output = true\n";
        # GRAM Files to transfer to the worker node
        # Only do this if we are dealing with a GRAM job that has set up a scratch area
        # otherwise we assume it is a globus-job-run or the users is using remote_initialdir
	if ( $scratch_isset ) {
            my $sdir; my @flist;
            opendir($sdir,$scratch_directory);
            my @sfiles = grep { !/^\./} readdir($sdir);
            close $sdir;

            print SCRIPT_FILE "transfer_input_files = ";
	  SFILE:
	    foreach $f ( @sfiles ) {
		$f =~ s{\/\/}{\/}g;
		$f = $scratch_directory . "/" . $f;
		next SFILE if $f eq $description->executable();
		next SFILE if $f eq $description->stdin();
		next SFILE if $f eq $description->stdout();
                push (@flist,"$f");
            }
            print SCRIPT_FILE join(",",@flist);
            print SCRIPT_FILE "\n";
        }
    }
    # End NFS Lite Mode

    $rc = print SCRIPT_FILE "#Extra attributes specified by client\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "$submit_attrs_string\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }
    $rc = print SCRIPT_FILE "X509UserProxy = $ENV{X509_USER_PROXY}\n";
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "print: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }       
    # default max_wall_time can be specified in globus-gram-job-manager.rvf
    if($description->max_wall_time() ne '')
    {
        my $max_wall_time = $description->max_wall_time() ;
        $rc = print SCRIPT_FILE "PeriodicRemove= (JobStatus == 2) && ( (time() - EnteredCurrentStatus) > (" . $max_wall_time . " * 60))\n";
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }

# ALB -- do the following only for the vanilla universe
if($universe ne 'standard') {

    my $shouldtransferfiles = $description->shouldtransferfiles();
    if (defined($shouldtransferfiles))
    {
        $self->log("Adding \"should_transfer_files = $shouldtransferfiles\"\n");
        $rc = print SCRIPT_FILE "should_transfer_files = $shouldtransferfiles\n";
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }
    my @xcount = $description->xcount();
    if(@xcount)
    {   
        $self->log("xcount = " . scalar(@xcount));
        foreach my $this_xcount (@xcount)
        {   
            $self->log("xcount = " . $this_xcount);
            $rc = print SCRIPT_FILE "request_cpus=$this_xcount \n";
            if (!$rc)
                {
                    return $self->respond_with_failure_extension(
                    "print: $script_filename: $!",
                    Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
                }
        }   
    }

    if($description->min_memory() ne '')
    {
        my $memory_request = $description->min_memory();
        $rc = print SCRIPT_FILE "request_memory=" . $memory_request . "\n";
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }

    my $WhenToTransferOutput = $description->whentotransferoutput();
    if (defined($WhenToTransferOutput))
    {
        $self->log("Adding \"WhenToTransferOutput = $WhenToTransferOutput\"\n");
        $rc = print SCRIPT_FILE "WhenToTransferOutput = $WhenToTransferOutput\n";
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }

    my $transfer_input_files = $description->transferinputfiles();

    if(!$multi_output) {
	if (defined($transfer_input_files))
	{
            # replace occurrences of ${GSBL_CONFIG_DIR}
	    $transfer_input_files =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;

            $self->log("Adding explicitly \"transfer_input_files = "
                    ."$transfer_input_files\"\n");
            $rc = print SCRIPT_FILE "transfer_input_files = $transfer_input_files\n";
            if (!$rc)
            {
		return $self->respond_with_failure_extension(
                    "print: $script_filename: $!",
                    Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
            }
	}
	else
	{
            my @transfer_input_files = $description->transferinputfiles();
            if (defined($transfer_input_files[0]))
            {
		my $file_list_string = "";
		foreach my $file (@transfer_input_files)
		{
                    $file_list_string .= "$file, ";
		}
		$file_list_string =~ s/, $//;
		$self->log("Adding \"transfer_input_files = $file_list_string\"\n");
		$rc = print SCRIPT_FILE "transfer_input_files = $file_list_string\n";

		if (!$rc)
		{
                    return $self->respond_with_failure_extension(
			"print: $script_filename: $!",
			Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
		}
            }
        }
    }

    my $transfer_output_files = $description->transferoutputfiles();
    if (defined($transfer_output_files))
    {
        $self->log("Adding explicitly \"transfer_output_files = "
                  ."$transfer_output_files\"\n");
        $rc = print SCRIPT_FILE "transfer_output_files = $transfer_output_files\n";
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }
    else
    {
        my @transfer_output_files = $description->transferoutputfiles();
        if (defined($transfer_output_files[0]))
        {
            my $file_list_string = "";
            foreach my $file (@transfer_output_files)
            {
                $file_list_string .= "$file, ";
            }
            $file_list_string =~ s/, $//;
            $self->log("Adding \"transfer_output_files = "
                      ."$file_list_string\"\n");
            $rc = print SCRIPT_FILE "transfer_output_files = $file_list_string\n";
            if (!$rc)
            {
                return $self->respond_with_failure_extension(
                    "print: $script_filename: $!",
                    Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
            }
        }
    }

    # ALB -- adding extra handling for other Condor options we specify
    my $stream_output = $description->stream_output();
    if (defined($stream_output))
    {
	$self->log("Adding \"stream_output = $stream_output\"\n");
	$rc = print SCRIPT_FILE "stream_output = $stream_output\n";
	if (!$rc)
	{
            return $self->respond_with_failure_extension(
		"print: $script_filename: $!",
		Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
	}
    }
    my $stream_error = $description->stream_error();
    if (defined($stream_error))
    {
	$self->log("Adding \"stream_error = $stream_error\"\n");
	$rc = print SCRIPT_FILE "stream_error = $stream_error\n";
	if (!$rc)
	{
            return $self->respond_with_failure_extension(
		"print: $script_filename: $!",
		Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
	}
    }
}

    my $leave_in_queue = $description->leave_in_queue();
    if (defined($leave_in_queue))
    {
	$self->log("Adding \"leave_in_queue = $leave_in_queue\"\n");
	$rc = print SCRIPT_FILE "leave_in_queue = $leave_in_queue\n";
	if (!$rc)
	{
            return $self->respond_with_failure_extension(
		"print: $script_filename: $!",
		Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
	}
    }
    my $transfer_executable = $description->transfer_executable();
    if (defined($transfer_executable))
    {
	$self->log("Adding \"transfer_executable = $transfer_executable\"\n");
	$rc = print SCRIPT_FILE "transfer_executable = $transfer_executable\n";
	if (!$rc)
	{
            return $self->respond_with_failure_extension(
		"print: $script_filename: $!",
		Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
	}
    }

    if ($universe eq 'parallel')
    {
        $rc = print SCRIPT_FILE "Output = " . $description->stdout() . "\n" .
                                "Error = " . $description->stderr() . "\n" .
                                "machine_count = " . $description->count() . "\n" .
                                "queue\n";
        if (!$rc)
        {
            return $self->respond_with_failure_extension(
                "print: $script_filename: $!",
                Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
        }
    }
    else
    {
        for (my $i = 0; $i < $description->count(); $i++) {
            if ($multi_output)
            {
		# ALB -- keep output segregated for job batches
		$rc = print SCRIPT_FILE "InitialDir = " . $description->directory() . "/job$i\n";
		if (!$rc)
		{
                    return $self->respond_with_failure_extension(
                        "print: $script_filename: $!",
                        Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
		}
		$rc = print SCRIPT_FILE "Output = " . $description->directory() . "/job$i/stdout\n";
		if (!$rc)
		{
                    return $self->respond_with_failure_extension(
			"print: $script_filename: $!",
			Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
		}
		$rc = print SCRIPT_FILE "Error = " . $description->directory() . "/job$i/stderr\n";
		if (!$rc)
		{
                    return $self->respond_with_failure_extension(
			"print: $script_filename: $!",
			Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
		}

		my @custom_arguments = ();
		my $arg_counter = 0;
		foreach (@arguments) {
                    if($_ =~ /,/) { # if this argument contains a comma, we need to get the right arg
			my @many_args = split(/,/,$arguments[$arg_counter]);
			$custom_arguments[$arg_counter] = $many_args[$i];
                    } else {
			$custom_arguments[$arg_counter] = $arguments[$arg_counter];
                    }
		    $arg_counter++;
		}

		if($#arguments >= 0)
		{
                    $argument_string = join(' ',
                                            map
					    {
						$_ =~ s/"/\\\"/g; #"
						$_;
                                            }
					    @custom_arguments);
		}
		else
		{
                    $argument_string = '';
		}

		# ALB -- adding in a new command line for each job
		$rc = print SCRIPT_FILE "Arguments = $argument_string\n";
		
		if (!$rc)
		{
                    return $self->respond_with_failure_extension(
			"print: $script_filename: $!",
			Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
		}

#		if($universe ne 'standard') {
#
#                    # ALB -- construct a transferinputfiles directive from shared files and per-job files
#                    my $temp_input_files_string = "";
#
#                    # shared files exist in the cache
#                    my $shared_input_files_string = $description->transferinputfiles();
#                    if(!defined($shared_input_files_string)) {
#			$shared_input_files_string = "";
#                    }
#
#		    # per-job files also exist in the cache
#		    my $per_job_input_files_string = $description->transferperjobfiles();
#		    if (defined($per_job_input_files_string)) {
#			# groups of per-job files are separated with a comma; individual files within a group are separated with a colon
#			my @groups = split(/,/,$per_job_input_files_string);
#
#			my $this_job_input_files_string = "";
#			for(my $j = 0; $j < @groups; $j++) {
#                            my @files = split(/:/,$groups[$j]);
#			    $this_job_input_files_string .= ",$files[$i]";
#			}
#
#			$temp_input_files_string = $shared_input_files_string . $this_job_input_files_string;
#
#                    } else {
#			$temp_input_files_string = $shared_input_files_string;
#                    }
#
#		    # replace occurrences of ${GSBL_CONFIG_DIR}
#		    $temp_input_files_string =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;
#
#		    $self->log("Adding explicitly \"transfer_input_files = "
#				."$temp_input_files_string\"\n");
#                    $rc = print SCRIPT_FILE "transfer_input_files = $temp_input_files_string\n";
#                    if(!$rc)
#		    {
#			return $self->respond_with_failure_extension(
#                            "print: $script_filename: $!",
#			    Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
#                    }
#
#		} else { # ALB -- adding support for standard universe job batches
#
#                    # ALB - construct a file_remaps directive from shared files and per-job files
#		    my $temp_file_remaps_string = "";
#
#		    # shared files exist in the cache
#		    my $shared_file_remaps_string = $description->transferinputfiles();
#		    if(defined($shared_file_remaps_string)) {
#			# split the shared files string looking for the comma
#			my @files = split(/,/,$shared_file_remaps_string);
#			foreach my $file (@files) {
#                            # get just filename (not any path information)
#			    my $just_filename = $file;
#			    if($file =~ /.*\/(.*)/) {
#				$just_filename = $1;
#                            }
#			    $temp_file_remaps_string .= "$just_filename=$file; ";
#			}
#                    }
#
#		    # per-job files also exist in the cache
#		    my $per_job_file_remaps_string = $description->transferperjobfiles();
#		    if (defined($per_job_file_remaps_string)) {
#			# groups of per-job files are separated with a comma; individual files within a group are separated with a colon
#			my @groups = split(/,/,$per_job_file_remaps_string);
#			for(my $j = 0; $j < @groups; $j++) {
#                            my @files = split(/:/,$groups[$j]);
#			    my $file = $files[$i];
#			    # get just filename (not any path information)
#			    my $just_filename = $file;
#			    if($file =~ /.*\/(.*)/) {
#				$just_filename = $1;
#                            }
#			    $temp_file_remaps_string .= "$just_filename=$file; ";
#			}
#                    }
#
#		    if($temp_file_remaps_string ne "") {
#			# get rid of trailing semi-colon
#			$temp_file_remaps_string =~ s/; $//;
#
#			# replace occurrences of ${GSBL_CONFIG_DIR}
#			$temp_file_remaps_string =~ s/\$\{GSBL_CONFIG_DIR\}/$scratch_dir/g;
#
#			$self->log("Adding \"file_remaps = $temp_file_remaps_string\"\n");
#			$rc = print SCRIPT_FILE "file_remaps = \"$temp_file_remaps_string\"\n";
#			if (!$rc)
#			{
#                            return $self->respond_with_failure_extension(
#				"print: $script_filename $!",
#				Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
#			}
#                    }
#		}
            }
            else
            {
                $rc = print SCRIPT_FILE
                        "Output = " . $description->stdout() .  "\n" .
                        "Error = " . $description->stderr() . "\n";
                if (!$rc)
                {
                    return $self->respond_with_failure_extension(
                        "print: $script_filename: $!",
                        Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
                }
            }
            $rc = print SCRIPT_FILE "queue 1\n";
            if (!$rc)
            {
                return $self->respond_with_failure_extension(
                    "print: $script_filename: $!",
                    Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
            }
        }
    }

    $rc = close(SCRIPT_FILE);
    if (!$rc)
    {
        return $self->respond_with_failure_extension(
            "close: $script_filename: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }

    # ALB -- write out condor submit file
    `cp $script_filename /tmp/condor_sf`;

    $self->log("About to submit condor job");
    local(*SUBMIT_IN);
    local(*SUBMIT_OUT);
    local(*SUBMIT_ERR);
    $pid = IPC::Open3::open3(
            \*SUBMIT_IN, \*SUBMIT_OUT, \*SUBMIT_ERR,
            $condor_submit, $script_filename);
    if (!$pid)
    {
        return $self->respond_with_failure_extension(
            "open3: $condor_submit: $!",
            Globus::GRAM::Error::TEMP_SCRIPT_FILE_FAILED());
    }

    close(SUBMIT_IN);

    local $/;

    $condor_submit_out = <SUBMIT_OUT>;
    $condor_submit_err = <SUBMIT_ERR>;

    close(SUBMIT_OUT);
    close(SUBMIT_ERR);

    waitpid($pid, 0);
    $status = $?>>8;

    $self->log("condor_submit status: $status");
    $self->log("condor_submit output: $condor_submit_out");
    $self->log("condor_submit error: $condor_submit_err");

    if ($status == 0)
    {
        $response_line = (grep(/submitted to cluster/,
                split(/\n/, $condor_submit_out)))[0];

        $job_id = (split(/\./, (split(/\s+/, $response_line))[5]))[0];

	if($job_id ne '')
	{
            # ALB -- call update_db
	    my $workingdir = $description->directory();
	    # get unique id from working_dir
	    $workingdir =~ /\/([0-9]+\.[0-9]+)/;
	    my $unique_id = $1;
	    &update_db($unique_id,$job_id);

	    $status = Globus::GRAM::JobState::PENDING;

            $job_id = join(',', map { sprintf("%03d.%03d.%03d",
                    $job_id, $_, 0) } (0..($description->count()-1)));
	    return {JOB_STATE => Globus::GRAM::JobState::PENDING,
		    JOB_ID    => $job_id};
	}
    }
    elsif ($condor_submit_err ne '')
    {
        $self->log("Writing extended error information to stderr");
        local(*ERR);
        open(ERR, '>' . $description->stderr());
        print ERR $condor_submit_err;
        close(ERR);

        $condor_submit_err =~ s/\n/\\n/g;

        return $self->respond_with_failure_extension(
                "condor_submit: $condor_submit_err",
                Globus::GRAM::Error::JOB_EXECUTION_FAILED());
    }
    return Globus::GRAM::Error::JOB_EXECUTION_FAILED;
}

sub poll
{
    my $self = shift;
    my $description = $self->{JobDescription};
    my $state;
    my $job_id = $description->job_id();
    my @job_ids = split(/,/, $description->job_id());
    my ($cluster, $rest) = split(/\./, $job_ids[0], 2);
    my $num_done;
    my $num_run;
    my $num_evict;
    my $num_abort;
    my $record = {};
    local(*CONDOR_LOG_FILE);

    $self->log("polling job " . $description->jobid());

    if ( open(CONDOR_LOG_FILE, '<' . $self->{condor_logfile}) )
    {
        while (<CONDOR_LOG_FILE>)
        {
            if (/<c>/) {
                if (defined($record)) {
                    if ($record->{Cluster} == $cluster)
                    {
                        # record Matches our job id
                        if ($record->{EventTypeNumber} == 1)
                        {
                            # execute event
                            $num_run++;
                        } elsif ($record->{EventTypeNumber} == 4) {
                            $num_evict++;
                        } elsif ($record->{EventTypeNumber} == 5) {
                            $num_done++;
                        } elsif ($record->{EventTypeNumber} == 9) {
                            $num_abort++;
                        }
                    }
                }
                $record = {};
            } elsif (/<a n="([^"]+)">/) { #"/) {
                my $attr = $1;

                if (/<s>([^<]+)<\/s>/) {
                    $record->{$attr} = $1;
                } elsif (/<i>([^<]+)<\/i>/) {
                    $record->{$attr} = int($1);
                } elsif (/<b v="([tf])"\/>/) {
                    $record->{$attr} = ($1 eq 't');
                } elsif (/<r>([^<]+)<\/r>/) {
                    $record->{$attr} = $1;
                }
            } elsif (/<\/c>/) {
            }
        }

        if (defined($record)) {
            if ($record->{Cluster} == $cluster)
            {
                # record Matches our job id
                if ($record->{EventTypeNumber} == 1)
                {
                    # execute event
                    $num_run++;
                } elsif ($record->{EventTypeNumber} == 4) {
                    $num_evict++;
                } elsif ($record->{EventTypeNumber} == 5) {
                    $num_done++;
                } elsif ($record->{EventTypeNumber} == 9) {
                    $num_abort++;
                }
            }
        } 
        @status = grep(/^[0-9]* \(0*${job_id}/, <CONDOR_LOG_FILE>);
        close(CONDOR_LOG_FILE);
    }
    else
    {
        $self->nfssync( $description->stdout(), 0 )
            if $description->stdout() ne '';
        $self->nfssync( $description->stderr(), 0 )
            if $description->stderr() ne '';
        return { JOB_STATE => Globus::GRAM::JobState::DONE };
    }

    if($num_abort > 0)
    {
        $state = Globus::GRAM::JobState::FAILED;
    }
    elsif($num_done == $description->count())
    {
        $self->nfssync( $description->stdout(), 0 )
            if $description->stdout() ne '';
        $self->nfssync( $description->stderr(), 0 )
            if $description->stderr() ne '';

        $state = Globus::GRAM::JobState::DONE;
    }
    elsif($num_run == 0)
    {
        $state = Globus::GRAM::JobState::PENDING;
    }
    elsif($num_run > $num_evict)
    {
        $state = Globus::GRAM::JobState::ACTIVE;
    }
    else
    {
        $state = Globus::GRAM::JobState::SUSPENDED;
    }

    if($self->{STDIO_MERGER}) {
        $self->{STDIO_MERGER}->poll($state == Globus::GRAM::JobState::DONE);
    }

    return { JOB_STATE => $state };
}

sub cancel
{
    my $self = shift;
    my $description = $self->{JobDescription};
    my $job_id = $description->jobid();
    my $count = 0;

    $job_id =~ s/,/ /g;
    $job_id =~ s/(\d+\.\d+)\.\d+/$1/g;

    $self->log("cancel job " . $description->jobid());
    # we do not need to be too efficient here
    $self->log(`$condor_rm $job_id 2>&1`);

    if($? == 0)
    {
	return { JOB_STATE => Globus::GRAM::JobState::FAILED };
    }
    else
    {
	return Globus::GRAM::Error::JOB_CANCEL_FAILED();
    }
}

sub respond_with_failure_extension
{
    my $self = shift;
    my $msg = shift;
    my $rc = shift;

    $self->respond({GT3_FAILURE_MESSAGE => $msg });
    return $rc;
}

sub update_db {
    my $id = $_[0];
    my $cluster = $_[1];
    my $db = "DBI:mysql:GT6_dev:latticedb.umiacs.umd.edu";
    my $dbh = DBI->connect($db, "gt4admin", "Daim7juz", {RaiseError=>1});
    my $query = qq{ UPDATE job SET cluster_id="$cluster" WHERE unique_id="$id" };
    $dbh->do( $query );
    $dbh->disconnect();
}

1;
