package GSBL::Client;

use 5.008;
use strict;
use warnings;

require Exporter;
use AutoLoader qw(AUTOLOAD);
use strict;
use Getopt::Mixed "nextOption";

our @ISA = qw(Exporter);

# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.

# This allows declaration	use GSBL::Client ':all';
# If you do not need this, moving things directly into @EXPORT or @EXPORT_OK
# will save memory.
our %EXPORT_TAGS = ( 'all' => [ qw(
	process_options
	print_usage	
) ] );

our @EXPORT_OK = ( @{ $EXPORT_TAGS{'all'} } );

our @EXPORT = qw(
	
);

our $VERSION = '0.01';

# Global variables used by the output format for usage messages.
my ($name, $takes, $description);

=head1 NAME

GSBL::Client - Perl module for writing grid clients

=head1 SYNOPSIS

  use GSBL::Client ":all";
  my %options = (O => { TYPE => "s", 
		KEY => "outputFile", 
		TAKES => "output_file",
		OPTIONAL_FLAG => 1,
		OPTIONAL_VALUE => 0,
		DESC => "the file to which to write output"},
	); 
  my $properties = process_options(\%options);
  if (@ARGV != 2) { # Assume program takes 2 arguments
      print_usage(\%option_desc, "program_name");
  }

=head1 ABSTRACT

GSBL::Client is a Perl module for writing grid clients.
It provides functions for processing command-line arguments and 
writing Java properties files read by the Java grid clients.

=head1 DESCRIPTION

GSBL::Client is a Perl module for writing grid clients. Currently,
it focuses on argument processing. Arguments are described in a hash
whose keys are argument names (either single characters for short 
(e.g., -x) options or strings for long (e.g., --foo) options) and whose
values are hash references describing the options. The hash references
may contain the following key/value pairs:

 TYPE => the type of the value taken by the argument. Should be i for 
    integer, f for float, s for string, and either "none" or "" for 
    no value.

 KEY => the key to be written in the properties file for this argument.

 TAKES => short description of what the argument accepts; will be used 
    when printing a usage message. E.g., for the example hash above, 
    the TAKES parameter will result in an argument description of
    -O output_file	the file to which to write output	

 OPTIONAL_FLAG => boolean value indicating whether or not the argument
	is optional.  If it is not specified, then the argument is assumed
	optional.

 OPTIONAL_VALUE => boolean value indicating whether or not the argument's 
    value is optional. If it is not specified, then the value is 
    assumed mandatory.  This only applies if TYPE is not set 
    to "none" or "".

 DESC => Help text describing the option; used when printing usage 
    messages.  It will be automatically line-wrapped, but try to 
    keep it concise.

=head1 EXPORT

None by default. 

=head1 METHODS

=over 4

=item process_options(options_desc)

This function process command-line arguments using the options described
in the hash reference options_desc. It returns a Java properties file
as a string.

=cut

sub process_options {
    my $options = shift;
	my $progname = shift;
                                                                                
    my $opt_str = "help emit-wsdl ";
    my $type;
    my $return = "";
                                                                                
    foreach my $opt(sort keys %$options) {
		# Skip processing non-flag arguments.
		next if $opt =~ /arg[0-9]+/;

        $opt_str .= "$opt";
        if ( ($type = $$options{$opt}{'TYPE'}) ne "") {
            if ($type ne "none") {
                if ($$options{$opt}{'OPTIONAL_VALUE'}) {
                    $opt_str .= ":$type";
                } else {
                    $opt_str .= "=$type";
                }
            }
        }
        $opt_str .= " ";
    }

	my %options_seen;

    Getopt::Mixed::init($opt_str);
    my ($option, $value, $pretty);
    while (($option, $value, $pretty) = nextOption()) {
        if ($option eq "help") {
            print_usage($options, $progname);
            exit 0;
        }
        if (!defined $$options{$option}{'TYPE'} ||
                $$options{$option}{'TYPE'} eq "" ||
                $$options{$option}{'TYPE'} eq "none") {
            $value = "true";
        }
		my $type = map_option_type_to_java_type($$options{$option}{'TYPE'});
        $return .= $$options{$option}{'KEY'} . " \@-- " . $value . " \@-- " .
				$type . "\n";
		$options_seen{$option} = $value;
    }
    Getopt::Mixed::cleanup();

	#  Make sure all mandatory options were called
	foreach my $opt(sort keys %$options) {
		# Skip processing non-flag arguments.
		next if $opt =~ /arg[0-9]+/;
		next if ($$options{$opt}{'OPTIONAL_FLAG'});
		unless (defined $options_seen{$opt}) {
			print STDERR "Error: no value for required argument $opt.\n";
			print_usage($options, $progname);
			exit 1;
		}
	}

	# Now that we've processed the option flags, we can deal with the 
	# command-line arguments.

	my $i = 0;
    while (defined $$options{"arg$i"}) {
        if ($$options{"arg$i"}{'OPTIONAL_FLAG'} || $$options{"arg$i"}{'OPTIONAL_FLAG'}) {
			if (defined $ARGV[$i]) {
				$return .= $$options{"arg$i"}{'KEY'} . " = " . $ARGV[$i] . "\n";
			} 
		} else {
			unless (defined $ARGV[$i]) {
				print STDERR "Error: no value for required argument " .
							$$options{"arg$i"}{'TAKES'}, ".\n";
				print_usage($options, $progname);
				exit 1;
			}
			my $type = map_option_type_to_java_type($$options{"arg$i"}{'TYPE'});
        	$return .= $$options{"arg$i"}{'KEY'} . " \@-- " . 
							$ARGV[$i] . " \@-- " .  $type . "\n";
		}
        $i++;
    }
                                                                                
    return $return;
}

=item map_option_type_to_java_type($type)

Maps the contents of the TYPE value to a Java type.

=cut

sub map_option_type_to_java_type {
	my $type = shift;
	if (!defined $type || $type eq "" || $type eq "none") {
		return "java.lang.Boolean";
	}
	if ($type eq "s") {
		return "java.lang.String";
	}
	if ($type eq "i") {
		return "java.lang.Integer";
	}
	if ($type eq "f") {
		return "java.lang.Double";
	}
	die "Unrecognized type '$type'";
}

=item print_usage(options, program_name)

This function prints a usage message for the program. Options is 
a hash reference describing the options accepted by the program,
and program_name is the name of the program.

=cut

sub print_usage {
    my ($options, $progname) = @_;

	print STDERR "\n   Usage: $progname ";

	# Print mandatory options
    foreach my $opt(sort keys %$options) {

		# Skip printing non-flag arguments.
		next if $opt =~ /arg[0-9]+/;
   
		# Skip printing optional arguments 
        next if ($$options{$opt}{'OPTIONAL_FLAG'});
                                                                            
        # Print the option name
        my $name_1 = length($opt) > 1 ? "--$opt" : "-$opt";
                                                                                
        # Add to it text representing the value taken by the argument,
        # if any.
        my $takes_1;
        if ( defined $$options{$opt}{'TAKES'} && 
				($takes_1 = $$options{$opt}{'TAKES'}) ne "") {
            if ($$options{$opt}{'OPTIONAL_VALUE'}) {
                $name_1 .= " [$takes_1]";
            } else {
                $name_1 .= " $takes_1";
            }
        }

		print STDERR "$name_1 ";

    }

	print STDERR "[OPTIONS]";
	my $i = 0;	
	while (defined $$options{"arg$i"}) {
		if ($$options{"arg$i"}{'OPTIONAL_FLAG'} || $$options{"arg$i"}{'OPTIONAL_VALUE'}) {
			print STDERR " [", $$options{"arg$i"}{'TAKES'}, "]";
		} else {
			print STDERR " ", $$options{"arg$i"}{'TAKES'};
		}
		$i++;
	}
	print STDERR "\n\n";
                                                                                
                                                                                
    open(OPTION, ">&STDERR") || die
            "Cannot duplicate STDERR for printing options: $!";
                                                                                
    foreach my $opt(sort keys %$options) {

		# Skip printing non-flag arguments.
		next if $opt =~ /arg[0-9]+/;
                                                                                
        # Create a string with the name of the argument and any synonyms.
        $name = length($opt) > 1 ? "--$opt" : "-$opt";
        if ($$options{$opt}{'SYN'}) {
            foreach my $syn_opt($$options{$opt}{'SYN'}) {
                $name .= ", ";
                $name .= length($syn_opt) > 1 ? "--$syn_opt" : "-$syn_opt";
            }
        }
                                                                                
        # Add to it text representing the value taken by the argument,
        # if any.
        if ( defined $$options{$opt}{'TYPE'} && 
	     ($takes = $$options{$opt}{'TYPE'}) ne "") {

	    if($takes eq "i") {
		$takes = "integer";
	    } elsif($takes eq "f") {
		$takes = "float";
	    } elsif($takes eq "s") {
		$takes = "string";
	    } elsif($takes eq "none") {
		$takes = "boolean";
	    }

            if ($$options{$opt}{'OPTIONAL_VALUE'}) {
                #$name .= " [$takes]";
		$takes = "[$takes]";
            } else {
                #$name .= " $takes";
            }
        }
                                                                                
        $description = $$options{$opt}{'DESC'} || "";
                                                                                
        # Write the argument.
        write(OPTION);
    }
                                                                                
    close(OPTION);
    print STDERR "\n";
}

=item get_wsdl_type(type)

This function maps the options hash TYPE field to a WSDL type.

=cut

sub get_wsdl_type {
	my $type = shift;
	if (!defined $type || $type eq "" || $type eq "none") {
		return "xsd:boolean";
	} 
	if ($type eq "i") {
		return "xsd:int";
	}
	if ($type eq "f") {
		return "xsd:double";
	}
	if ($type eq "s") {
		return "xsd:string";
	}
	die "Unknown type '$type'";
}

###########################
format OPTION =
@>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   ^<<<<<<<<   ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
$name,                            $takes,     $description
~~                                            ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                                              $description
.
###########################




# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__

=head1 SEE ALSO

=head1 AUTHOR

Daniel Sumers Myers, E<lt>dmyers@umiacs.umd.eduE<gt>

=head1 COPYRIGHT AND LICENSE

Copyright 2004 by Daniel Sumers Myers

This library is free software distributed under the GPL.

=cut

