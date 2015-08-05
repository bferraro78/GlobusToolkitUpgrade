package edu.umd.grid.bio.paup.shared;

import java.io.*;
import java.util.regex.*;
import java.util.Vector;

// For logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

/** 
 * Class to determine which files be staged in/out for a PAUP job. Parses enough
 * of the PAUP command file language to determine which files will be required for
 * the job and which files will be produced by the job.
 */

public class PAUPParser {
	/**
     * Logger.
     */
    static Log log = LogFactory.getLog(PAUPParser.class.getName());

	/**
	 * The filename of the parameter file to parse.
  	 */
	protected String paramfileName;

	/* 
	 * Where to look for files.
	 */
	String myWorkingDir;

	/**
	 * The files needed for input.
	 */
	Vector inputFiles;

	/**
	 * The files produced as output.
	 */
	Vector outputFiles;
	


	/** 
	 * Constructor accepting the name of a parameter file to parse.
	 * @param paramfileName the parameter file to parse
	 */
	public PAUPParser(String paramfileName, String workingDir) throws Exception {
		this.paramfileName = paramfileName;
		this.inputFiles = new Vector();
		this.outputFiles = new Vector();
		myWorkingDir = workingDir;

		this.inputFiles.add(paramfileName);

		parseCommands(readFile());
	}

	/** 
  	 * Parse the command file from the string in memory.
	 */

	protected void parseCommands(StringBuffer paramfile) throws Exception {
		Pattern command = Pattern.compile("([^;]+)");
		Matcher M = command.matcher(paramfile);

		// Skip to PAUP* block
		while(M.find() && !M.group(1).trim().equalsIgnoreCase("begin paup")) {
			;
		}

		// Parse PAUP* commands
		while (M.find() && !M.group(1).trim().equalsIgnoreCase("end")) {
			handleCommand(M.group(1).trim());
		}
		System.err.println("Files in parameter file: " + paramfileName);
		System.err.println(inputFiles);
		System.err.println(outputFiles);
	}

	protected void handleCommand(String cmdString) throws Exception {
		String [] tokens = cmdString.split("\\s+");

		String filename = null;

		if (commandNameIs(tokens[0], "log")) {
			addFile(tokens, "file", "output");
		}

		if (commandNameIs(tokens[0], "savetrees")) {
			addFile(tokens, "file", "output");
		}

		if (commandNameIs(tokens[0], "nj")) {
			addFile(tokens, "treefile", "output");
		}

		if (commandNameIs(tokens[0], "agree")) {
			addFile(tokens, "treefile", "output");
		}

		if (commandNameIs(tokens[0], "alltrees")) {
			addFile(tokens, "fdfile", "output");
			addFile(tokens, "scorefile", "output");
		}

		if (commandNameIs(tokens[0], "bandb")) {
			addFile(tokens, "fdfile", "output");
		}

		if (commandNameIs(tokens[0], "bootstrap")) {
			addFile(tokens, "treefile", "output");
		}

		if (commandNameIs(tokens[0], "contree")) {
			addFile(tokens, "treefile", "output");
		}

		if (commandNameIs(tokens[0], "dscores")) {
			addFile(tokens, "scorefile", "output");
		}

		if (commandNameIs(tokens[0], "edit")) {
			throw new Exception("Prohibited command 'edit' encountered while parsing PAUP file");
		}

		if (commandNameIs(tokens[0], "execute")) {
			if (tokens.length < 2) {
				throw new Exception("Missing filename argument to execute command");
			}
//			inputFiles.add(tokens[1]);
			/* We don't add the filename to execute to our input files list because we'll
			 * get it when we merge in the new PAUPParser we create for that file. 
			 */
			this.merge(new PAUPParser(tokens[1], myWorkingDir));
		}

		if (commandNameIs(tokens[0], "export")) {
			addFile(tokens, "file", "output");
		}

		if (commandNameIs(tokens[0], "gettrees")) {
			addFile(tokens, "file", "input");
		}

		if (commandNameIs(tokens[0], "jackknife")) {
			addFile(tokens, "treefile", "output");
		}

		if (commandNameIs(tokens[0], "loadconstr")) {
			addFile(tokens, "file", "output");
		}

		if (commandNameIs(tokens[0], "lscores")) {
			addFile(tokens, "scorefile", "output");
		}

		if (commandNameIs(tokens[0], "matrixrep")) {
			addFile(tokens, "file", "output");
		}

		if (commandNameIs(tokens[0], "pscores")) {
			addFile(tokens, "scorefile", "output");
		}

		if (commandNameIs(tokens[0], "puzzle")) {
			addFile(tokens, "treefile", "output");
		}

		if (commandNameIs(tokens[0], "saveassum")) {
			addFile(tokens, "file", "output");
		}

		if (commandNameIs(tokens[0], "tonexus")) {
			addFile(tokens, "fromfile", "input");
			addFile(tokens, "tofile", "output");
		}

		if (commandNameIs(tokens[0], "treedist")) {
			addFile(tokens, "file", "output");
		}

		if (commandNameIs(tokens[0], "upgma")) {
			addFile(tokens, "treefile", "output");
		}
	}
		

	/**
	 * Read the parameter file. Strips out comments.
	 */
	protected StringBuffer readFile() throws FileNotFoundException, IOException {
		StringBuffer paramfile = new StringBuffer();
		try {
			BufferedReader instream = new BufferedReader(new FileReader(myWorkingDir + paramfileName));
			String line;

			// This pattern matches a [ followed by zero or more non-] characters
			// followed by a ].
			Pattern comment = Pattern.compile("(\\[[^\\]]*\\])");

			// Discard first line if it's just #NEXUS (as should be the case)
			line = instream.readLine();
			if (!line.trim().equals("#NEXUS")) {
				paramfile.append(line.trim());
			}
			

			while ( (line = instream.readLine()) != null) {

				Matcher M = comment.matcher(line);

				// Replace all comments ([...]) with the empty string.
				String nonComment = M.replaceAll("");

				if (!nonComment.equals("")) {
					paramfile.append(nonComment);
				}
			}
		} catch (FileNotFoundException fe) {
			log.error("Paramfile " + paramfileName + " not found: " + fe);
			throw fe;
		} catch (IOException ie) {
			log.error("IOException reading paramfile " + paramfileName + ": " + ie);
		}

		return paramfile;
	}

	/** 
	 * Parse the parameter file 
	 */

	/**
	 * Return a vector containing the names of the files needed as input to the job.
	 */
	public String [] getInputFiles(boolean wantPaths) {
		String [] toret = new String[inputFiles.size()];
		for (int i = 0; i < inputFiles.size(); i++) {
			toret[i] = (String) inputFiles.elementAt(i);
			if (!wantPaths) {
				toret[i] = new File(toret[i]).getName();
			}
		}
		return toret;
	}

	/**
	 * Return a vector containing the names of the files produced by the job.
	 */
	public String [] getOutputFiles(boolean wantPaths) {
		String [] toret = new String[outputFiles.size()];
		for (int i = 0; i < outputFiles.size(); i++) {
			toret[i] = (String) outputFiles.elementAt(i);
			if (!wantPaths) {
				toret[i] = new File(toret[i]).getName();
			}
		}
		return toret;
	}		

	/**
	 * Merge the input and output files of another PAUPParser into our lists.
	 * FIXME: worry about duplicate filenames
	 * FIXME: worry about output files being inputs for other commands--not needing
	 * 		to be staged.
	 */
	public void merge(PAUPParser P) {
		this.outputFiles.addAll(Arrays.asList(P.getOutputFiles(true)));
		this.inputFiles.addAll(Arrays.asList(P.getInputFiles(true)));
	}
		
	/**
	 * Add a new input or output file.
	 * @param tokens the tokens comprising the command
	 * @param argument the name of the argument specifying the file
	 * @param type "input" or "output" file
	 */
	protected void addFile(String [] tokens, String argument, String type) throws Exception {
		Pattern cmdPat = Pattern.compile("(?i)" + argument + "\\s*=\\s*(.*)");

		// We start at i = 1 because tokens[0] is the command name
		for (int i = 1; i < tokens.length; i++) {
			Matcher M = cmdPat.matcher(tokens[i]);
			if (M.find()) {
				// Guard against path-qualified filenames
				String filename = M.group(1);
				if (!filename.equals(new File(filename).getName())) {
					throw new Exception("Use of path-qualified filename: " + filename);
				}

				if (type.equals("output")) {
					if (outputFiles.contains(filename)) {
						throw new Exception("Tried to add already-seen output file: " +
												filename);
					}
					outputFiles.add(M.group(1));
				} else if (type.equals("input")) {
					if (inputFiles.contains(filename)) {
						throw new Exception("Tried to add already-seen input file: " +
												filename);
					}
					inputFiles.add(M.group(1));
				} else {
					throw new Exception("Unknown type value: " + type);
				}
			}
		}
	}

	public boolean commandNameIs(String candidate, String command) {
		return candidate.equalsIgnoreCase(command);
	}
	
	public static void main(String args[]) throws Exception {
		PAUPParser PP = new PAUPParser(args[0], "");
		
	}
}
			
