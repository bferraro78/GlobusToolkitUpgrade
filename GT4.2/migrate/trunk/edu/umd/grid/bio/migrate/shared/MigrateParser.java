package edu.umd.grid.bio.migrate.shared;

import java.io.*;
import java.util.regex.*;
import java.util.Vector;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * Class to determine which files be staged in/out for a MIGRATE job. Parses enough
 * of the MIGRATE command file language to determine which files will be required for
 * the job and which files will be produced by the job.
 */

public class MigrateParser {
	/**
     * Logger.
     */
    static Log log = LogFactory.getLog(MigrateParser.class.getName());

	/**
	 * The filename of the parameter file to parse.
  	 */
	protected String parmfileName;
    protected String path;

	/**
	 * The contents of the parameter file.
	 */
	StringBuffer parmfile = null;

	protected boolean useCatfile = false; 	// Will always be named "catfile"
	protected boolean useWeightfile = false;
	protected boolean useDistfile = false;
	protected boolean useUsertree = false;
	protected boolean useGeofile = false;
	protected boolean useTreefile = false;
	protected boolean useSeedfile = false;
	protected boolean useMathfile = false;	// Should be true if plot =~ /Both/
	protected boolean useSumfile = false;
	protected boolean useLogfile = false;

	/* These are file names that can be changed. */
	protected String mathfile = "mathfile";	// Can only be used if plot =~ /Both/
	protected String sumfile = "sumfile";
	protected String logfile = null;
	protected String seedfile = null;	// Used is random-seed = Noauto
	protected String outfile = "outfile";
	protected String infile = "infile";

	/* These are file names that cannot be changed. */
	protected static String catfile = "catfile";
	protected static String weightfile = "weightfile";
	protected static String distfile = "distfile";
	protected static String usertree = "intree";
	protected static String geofile = "geofile";
	protected static String treefile = "treefile";
	
	
	/** 
	 * Constructor accepting the name of a parameter file to parse.
	 * @param parmfileName the parameter file to parse
	 */
	public MigrateParser(String parmfileName) throws Exception {
		this.parmfileName = parmfileName;
		parse();
	}
    
    /**
     * Construct a new MigrateParser using this param file, located at path.
     * @param path  path to the param file.
     * @param filename name of the param file.
     */
    public MigrateParser( String path, String filename ) throws Exception {
        this.parmfileName = path + filename;
        this.path = path;
        log.debug( "Beging to Parse file" );
        parse();
        log.debug( "Done Parsing file" );
    }

    /**
     * Ensure that the menu file is set to no, if it is set to yes then change it.
     * Added by SJM 9/23/04
     */
    public void menuNo() {

        log.debug( "Entering menuNO()" );

        try {
            BufferedReader in = new BufferedReader( new FileReader( parmfileName ));
            PrintWriter out = new PrintWriter( new FileOutputStream( new File( path + "tmp" )));
    
            String line;
            boolean found = false;
            //make the substitution if necessary.
            while( (line = in.readLine()) != null ) {
                if( line.equalsIgnoreCase( "menu=YES" )) {
                    line = "menu=NO";
                    log.debug( ">>>> Changing menu to no <<<<<" );
                    found = true;
                }
                out.print( line + "\r\n" );
            }

            //put in logic to enter a menu=no line if no menu line was present.

            out.close();
            in.close();

            File f = new File( path + "tmp" );
            //File pf = new File( paramfilename );
            //pf.delete();
            boolean renme = f.renameTo( new File( parmfileName ));
            if( !renme ) {
                log.error( "Paramfile unchanged!!!" );
            }
            
        } catch ( FileNotFoundException e ) {
            log.error( "Parmfile " + parmfileName + " not found: " + e );
        } catch ( IOException e ) {
            log.error( "IOException reading parmfile " + parmfileName + ": " + e );
        }
    }

	/**
	 * Parse the parameter file.
	 */
	protected void parse() throws Exception {
		parmfile = new StringBuffer();
		try {
			BufferedReader instream = new BufferedReader(new FileReader(parmfileName));
			String line;
			Pattern commentLine = Pattern.compile("(?i)^\\s*#");

			while ( (line = instream.readLine()) != null) {
				if (!commentLine.matcher(line).find()) {
					parmfile.append(line + "\n");
				}
			}
		} catch (FileNotFoundException fe) {
			log.error("Parmfile " + parmfileName + " not found: " + fe);
			throw fe;
		} catch (IOException ie) {
			log.error("IOException reading parmfile " + parmfileName + ": " + ie);
		}

		if (paramTrue("categories")) {
			useCatfile = true;
			System.err.println("Using catfile");
		}

		if (paramTrue("weights")) {
			useWeightfile = true;
			System.err.println("Using weightfile");
		}

		if (paramTrue("distfile")) {
			useDistfile = true;
			System.err.println("Using distfile");
		}

		if (paramTrue("usertree")) {
			useUsertree = true;
			System.err.println("Using usertree");
		}

		if (paramTrue("geofile")) {
			useGeofile = true;
			System.err.println("Using geofile");
		}

		if (Pattern.compile("(?i)print-tree\\s*=\\s*(all|last|best)").matcher(parmfile).find()) {
			useTreefile = true;
			System.err.println("Using treefile");
		}

		// We require that Migrate input files give a random seed for BOINC purposes.
		if (!Pattern.compile("(?i)random-seed\\s*=\\s*own:").matcher(parmfile).find()) {
			log.error("Please supply a random number seed for Migrate runs");
			throw new Exception("Migrate parmfile without a random number seed");
		}
		/*
		if (!Pattern.compile("(?i)random-seed\\s*=\\s*auto").matcher(parmfile).find() &&
				!Pattern.compile("(?i)random-seed\\s*=\\s*own:").matcher(parmfile).find()) {
			useSeedfile = true;
			seedfile = getFilename("random-seed");
			System.err.println("Using seedfile: " + seedfile);
		}	
*/

		if (Pattern.compile("(?i)plot\\s*=\\s*yes:both").matcher(parmfile).find()) {
			useMathfile = true;
			System.err.println("Using mathfile");
		}

		String filename;
		if ( (filename = getFilename("infile")) != null) {
			infile = filename;
			System.err.println("Setting infile to: " + infile);
		}

		if ( (filename = getFilename("outfile")) != null) {
			outfile = filename;
			System.err.println("Setting outfile to: " + outfile);
		}

		if ( (filename = getFilename("mathfile")) != null) {
			mathfile = filename;
			System.err.println("Setting mathfile to: " + filename);
		}

		if (paramTrue("sumfile")) {
			useSumfile = true;
			System.err.println("Using sumfile");
			if ( (filename = getFilename("sumfile")) != null) {
				sumfile = filename;
				System.err.println("Setting sumfile to: " + filename);
			}
		}

		if (!Pattern.compile("(?i)logfile\\s*=\\s*none").matcher(parmfile).find() && 
				Pattern.compile("(?i)logfile\\s*=").matcher(parmfile).find()) {
			useLogfile = true;
			logfile = getFilename("logfile");
			System.err.println("Using logfile: " + logfile);
		}

	}

	/**
	 * Return a vector containing the names of the files needed as input to the job.
	 */
	public String [] getInputFiles(boolean noPaths) {
		Vector files = new Vector();

		// Mandatory input file
		files.add(getInfile());

		// Mandatory parameter file
		files.add(parmfileName);
		
		if (useCatfile()) {
			files.add(getCatfile());
		}

		if (useGeofile()) {
			files.add(getGeofile());
		}

		if (useSeedfile()) {
			files.add(getSeedfile());
		}

		if (useDistfile()) {
			files.add(getDistfile());
		}

		if (useWeightfile()) {
			files.add(getWeightfile());
		}

		if (useUsertree()) {
			files.add(getUsertree());
		}

		String [] toret = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			toret[i] = (String) files.elementAt(i);
			if (noPaths) {
				toret[i] = new File(toret[i]).getName();
			}
		}

		return toret;
	}

	/**
	 * Return a vector containing the names of the files produced by the job.
	 */
	public String[] getOutputFiles(boolean noPaths) {
		Vector files = new Vector();

		// Required output file
		files.add(getOutfile());
		
		if (useTreefile()) {
			files.add(getTreefile());
		}

		if (useMathfile()) {
			files.add(getMathfile());
		}

		if (useSumfile()) {
			files.add(getSumfile());
		}

		if (useLogfile()) {
			files.add(getLogfile());
		}

		String [] toret = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			toret[i] = (String) files.elementAt(i);
			if (noPaths) {
				toret[i] = new File(toret[i]).getName();
			}
		}

		return toret;
	}		
		

	protected boolean paramTrue(String parameter) {
		String regexp = "(?i)" + parameter + "\\s*=\\s*yes";
		return Pattern.compile(regexp).matcher(parmfile).find();
	}

	protected String getFilename(String parameter) {
		Matcher M = Pattern.compile("(?i)(?m)^[^#]*" + parameter + "\\s*=(Yes:)*\\s*(.*)$").matcher(parmfile);
		if (M.find()) {
			String filename = M.group(2).trim();
			if (filename.equalsIgnoreCase("yes")) {
				return null;
			} else {
				return filename;
			}
		} else {
			return null;
		}
	}		

	public boolean useCatfile() { return useCatfile; }
	public boolean useWeightfile() { return useWeightfile; }
	public boolean useDistfile() { return useDistfile; }
	public boolean useUsertree() { return useUsertree; }
	public boolean useGeofile() { return useGeofile; }
	public boolean useTreefile() { return useTreefile; }
	public boolean useSeedfile() { return useSeedfile; }
	public boolean useMathfile() { return useMathfile; }
	public boolean useSumfile() { return useSumfile; }
	public boolean useLogfile() { return useLogfile; }

	public String getInfile() { return infile; }
	public String getOutfile() { return outfile; }
	public String getMathfile() { return mathfile; }
	public String getSumfile() { return sumfile; }
	public String getLogfile() { return logfile; }
	public String getSeedfile() { return seedfile; }

	public String getCatfile() { return catfile; }
	public String getWeightfile() { return weightfile; }
	public String getDistfile() { return distfile; }
	public String getUsertree() { return usertree; }
	public String getGeofile() { return geofile; }
	public String getTreefile() { return treefile; }
	
	public static void main(String args[]) throws Exception {
		MigrateParser MP = new MigrateParser(args[0]);
		System.out.println(MP.getInputFiles(false));
		System.out.println(MP.getOutputFiles(false));
		
	}
}
			
		
