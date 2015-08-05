package edu.umd.grid.bio.structure.shared;

import java.io.*;
import java.util.regex.*;
import java.util.Vector;
    
// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StructureParser
{

    /**
     * Logger.
     */
    static Log log = LogFactory.getLog(StructureParser.class.getName());

    /**
     * The filename of the input file to parse.
     */
    protected String filename;

	/**
	 * The input file.
	 */
	protected String inputFile;

	/**
	 * The output file.
	 */
	protected String outputFile;

	/**
	 * String for storing the current working directory.
	 */
	protected String currentWorkingDir;

    /** 
     * Constructor accepting the name of an input file to parse.
     * @param inputFile the input file to parse
     * @param currentWorkingDir the input file to parse
     */
    public StructureParser(String inputFile, String currentWorkingDir) throws FileNotFoundException, IOException, Exception 
	{
		log.debug("Constructing new StructureParser");

        this.filename = currentWorkingDir + inputFile;
		this.currentWorkingDir = currentWorkingDir;

		inputFile = new String();
		outputFile = new String();

		log.debug("Parsing input file...");
        parseFile();

		log.debug("Finished parsing.  Input file: " + inputFile + ". Output file: " + outputFile);
    }

	public String getOutputFile()
	{
		return outputFile;
	}

	public String getInputFile()
	{
		return inputFile;
	}

    protected void parseFile() throws FileNotFoundException, IOException
	{
        try {
            BufferedReader instream = new BufferedReader(new FileReader(filename));
            String line;

            while ( (line = instream.readLine()) != null) {
				// Command lines start with #define
				Matcher M = Pattern.compile("(?i)^\\s*#define\\s*([A-Z]*)\\s*(\\S*)").matcher(line);
				if (M.find())
				{
					String command = M.group(1).trim();
					String value = M.group(2).trim();

					if(command.equals("INFILE"))
						inputFile = value;

					if(command.equals("OUTFILE"))
						outputFile = value;

				}
            }
        } catch (FileNotFoundException fe) {
            log.error("Input file " + filename + " not found: " + fe);
            throw fe;
        } catch (IOException ie) {
            log.error("IOException reading input file " + filename + ": " + ie);
        }
    }


}
