/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import java.lang.Runtime;
import java.lang.Integer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//for determining host name
import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Arrays;

import java.io.*;

/**
 * Interface with a Globus RLS server using various Globus command line tools.
 */
public class RLSManager {

	/**
	 * Local host name.
	 */
	private static String rlsServiceHost = "localhost";

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(RLSManager.class.getName());

	/**
	 * The GLOBUS_LOCATION.
	 */
	private static String globusLocation = "";

	/**
	 * RLI host, through which we'll put all our queries.
	 */
	private static String rliHost = "";

	/**
	 * Location of RLS client binary.
	 */
	private static String rlsClient = globusLocation + "/bin/globus-rls-cli";

	/**
	 * Default constructor.
	 */
	public RLSManager() {

		// determine the globus location
		Properties env = new Properties();
		try {
			env.load(Runtime.getRuntime().exec("env").getInputStream());

			globusLocation = (String) env.get("GLOBUS_LOCATION");
			rlsClient = globusLocation + "/bin/globus-rls-cli";
	
			// determine the local hostname
				rlsServiceHost = InetAddress.getLocalHost().getHostName();
	
	
			// determine the RLI host by reading from config file
			BufferedReader br = new BufferedReader(new FileReader(new File(
					globusLocation + "/service_configurations/rls.location")));
			rliHost = br.readLine();
			br.close();
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
	}

	/**
	 * Given a full path to a file, return that file's MD5Sum.
	 */
	public static String getMD5Sum(String file) {
		String md5sum = "invalid";

		try {
			Runtime r = Runtime.getRuntime();

			String[] command = { "/usr/bin/md5sum", file };
			log.debug("command is: " + Arrays.toString(command));
			Process proc = r.exec(command);
			String line = null;
			String md5sum_output = null;
			InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				md5sum_output = line;
			}
			br.close();

			// make sure output is valid
			if (md5sum_output != null) {
				if (md5sum_output.matches("[a-f0-9]{32}.*")) {
					md5sum = md5sum_output.substring(0, 32);
				}
			} else {
				log.debug("md5sum_output is null!");
			}
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		log.debug("MD5Sum is: " + md5sum);
		return md5sum;
	}

	/**
	 * Given a full path to a file, return that file's size in Kb.
	 */
	public static String getFileSize(String file) {
		String size = "invalid";

		try {
			Runtime r = Runtime.getRuntime();

			String[] command = { "/bin/sh", "-c",
					"/bin/ls -lH " + file + " | awk '{print $5}'" };
			log.debug("command is: " + Arrays.toString(command));
			Process proc = r.exec(command);
			String line = null;
			String size_output = null;
			InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				size_output = line;
			}
			br.close();

			// make sure output is valid
			if (size_output != null) {
				if (size_output.matches("[0-9]*")) {
					size = size_output;
				} else {
					log.debug("size output doesn't match!  size output is: "
							+ size_output);
				}
			} else {
				log.debug("size_output is null!");
			}
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		log.debug("Size is: " + size);
		return size;
	}

	/**
	 * Return the current date/time in seconds since the epoch.
	 */
	public static String getCurrentDate() {
		String date = "invalid";

		try {
			Runtime r = Runtime.getRuntime();
			String command = "/bin/date +%s";
			log.debug("command is: " + command);
			Process proc = r.exec(command);
			String line = null;
			String date_output = null;
			InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				date_output = line;
			}
			br.close();

			// make sure output is valid
			if (date_output != null) {
				if (date_output.matches("[0-9]*")) {
					date = date_output;
				}
			} else {
				log.debug("date_output is null!");
			}
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		log.debug("Date is: " + date);
		return date;
	}

	/**
	 * Update the requested date for a PFN.
	 */
	public static void updateRequestedDate(String PFN) {
		String current_date = RLSManager.getCurrentDate();
		String command = rlsClient + " attribute modify " + PFN
				+ " requested pfn string " + current_date + " rlsn://"
				+ rliHost + ":39281";
		GSBLUtils.executeCommand(command);
	}

	/**
	 * Update the in_use flag for a PFN. NOT CURRENTLY USED.
	 */
	public static void updateInUse(String PFN, String in_use) {
		if (in_use.equals("0") || in_use.equals("1")) {
			String command = rlsClient + " attribute modify " + PFN
					+ " in_use pfn int " + in_use + " rlsn://" + rliHost
					+ ":39281";
			GSBLUtils.executeCommand(command);
		}
	}

	/**
	 * Query the RLI to find out which LRC, if any, the LFN is registered with.
	 * For now, we will assume that only one LRC will be returned, though in the
	 * future this can be modified. Then, query the LRC and return a collection
	 * of PFNs. If an LRC is returned, the assumption is that there is at least
	 * one PFN to consider.
	 */
	public ArrayList<String> getPFNs(String LFN) {
		ArrayList<String> pfns = new ArrayList<String>();

		try {
			Runtime r = Runtime.getRuntime();
			String command = rlsClient + " query rli lfn " + LFN + " rlsn://"
					+ rliHost + ":39281";
			log.debug("command is: " + command);
			Process proc = r.exec(command);
			String line = null;
			String rli_output = null;
			String rli_error = null;
			InputStream stdout = proc.getInputStream();
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			InputStreamReader esr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			BufferedReader ebr = new BufferedReader(esr);
			while ((line = br.readLine()) != null) {
				log.debug("stdout is: " + line);
				rli_output = line;
			}
			while ((line = ebr.readLine()) != null && !line.equals("")) {
				log.debug("stderr is: " + line);
				rli_error = line;
			}
			br.close();
			ebr.close();

			// again, at most only one LRC is returned
			if (rli_error != null && rli_error.matches(".*doesn't exist.*")) {
				// there are no PFNs for this LFN
				log.debug("there are no PFNs for: " + LFN);
			} else {
				if (rli_output != null) {
					if (rli_output.indexOf("rlsn:") == -1) {
						log.debug("problem!  rli_output doesn't contain 'rlsn:'");
					} else {
						String LRC = rli_output.substring(rli_output
								.indexOf("rlsn:"));
						log.debug("LRC is: " + LRC);
						// query the LRC for a list of PFNs
						command = rlsClient + " query lrc lfn " + LFN + " "
								+ LRC;
						log.debug("command is: " + command);
						proc = r.exec(command);
						line = null;
						String lrc_output = null;
						stdout = proc.getInputStream();
						isr = new InputStreamReader(stdout);
						br = new BufferedReader(isr);
						while ((line = br.readLine()) != null) {
							if (line.indexOf("PFN doesn't exist") != -1) {
								log.debug("LFN exists without PFN!");
								break;
							}
							if (line.indexOf("gsiftp:") == -1) {
								log.debug("problem!  lrc_output doesn't contain 'gsiftp:'");
							} else {
								lrc_output = line.substring(line
										.indexOf("gsiftp:"));
								pfns.add(lrc_output);
							}
						}
						br.close();
					}
				} else {
					log.debug("rli_output is null!");
				}
			}

		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		return pfns;

	}

	/**
	 * Either create or add an LFN -> PFN mapping in an LRC catalog.
	 * 
	 * returns true if operation was successful
	 */
	public synchronized boolean addMapping(String LFN, String PFN,
			boolean create, String size) {
		boolean successful = false;
		try {
			Runtime r = Runtime.getRuntime();
			String command = "";
			if (create == true) {
				command = rlsClient + " create " + LFN + " " + PFN + " rlsn://"
						+ rliHost + ":39281";
			} else {
				command = rlsClient + " add " + LFN + " " + PFN + " rlsn://"
						+ rliHost + ":39281";
			}
			log.debug("command is: " + command);
			Process proc = r.exec(command);
			String line = null;
			String lrc_output = null;
			String lrc_error = null;
			InputStream stdout = proc.getInputStream();
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			InputStreamReader esr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			BufferedReader ebr = new BufferedReader(esr);
			while ((line = br.readLine()) != null) {
				log.debug("stdout is: " + line);
				lrc_output = line;
			}
			while ((line = ebr.readLine()) != null && !line.equals("")) {
				log.debug("stderr is: " + line);
				lrc_error = line;
			}
			br.close();
			ebr.close();

			// if stderr is non-empty, assume there was a problem
			if (lrc_error != null && !lrc_error.equals("")) {
				log.debug("ERROR: " + lrc_error);
				if (lrc_error.indexOf("LFN already exists") != -1
						&& create == true) { // odds are the database hadn't
												// updated yet; let's try an add
												// instead
					command = rlsClient + " add " + LFN + " " + PFN
							+ " rlsn://" + rliHost + ":39281";
					log.debug("command is: " + command);
					proc = r.exec(command);
					line = null;
					lrc_output = null;
					lrc_error = null;
					stdout = proc.getInputStream();
					stderr = proc.getErrorStream();
					isr = new InputStreamReader(stdout);
					esr = new InputStreamReader(stderr);
					br = new BufferedReader(isr);
					ebr = new BufferedReader(esr);
					while ((line = br.readLine()) != null) {
						log.debug("stdout is: " + line);
						lrc_output = line;
					}
					while ((line = ebr.readLine()) != null && !line.equals("")) {
						log.debug("stderr is: " + line);
						lrc_error = line;
					}
					br.close();
					ebr.close();

					// if stderr is non-empty, assume there was a problem
					if (lrc_error != null && !lrc_error.equals("")) {
						log.debug("ERROR (in add after attempted create): "
								+ lrc_error);
					} else {
						successful = true;
					}
				}
			} else {
				successful = true;
			}

			// add attributes
			command = rlsClient + " attribute add " + PFN + " size pfn string "
					+ size + " rlsn://" + rliHost + ":39281";
			GSBLUtils.executeCommand(command);
			String current_date = RLSManager.getCurrentDate();
			command = rlsClient + " attribute add " + PFN
					+ " requested pfn string " + current_date + " rlsn://"
					+ rliHost + ":39281";
			GSBLUtils.executeCommand(command);
			// in_use flag is not currently used
			// command = rlsClient + " attribute add " + PFN +
			// " in_use pfn int 1 rlsn://" + rliHost + ":39281";
			// GSBLUtils.executeCommand(command);

		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		return successful;

	}

}
