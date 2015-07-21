/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import java.lang.Runtime;

// for determining host name
import java.net.InetAddress;

import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Properties;

import java.util.ArrayList;
import java.util.Properties;

/**
 * This class manages transferring files between a Grid client and a Grid
 * server.
 */
public class ReliableFileTransferManager {

	/**
	 * The GLOBUS_LOCATION.
	 */
	private static String globusLocation = "";

	private static String tempUploadLocation = "/tmp/temp_uploads/";

	/**
	 * These hold the URLs of the various files to transfer.
	 */
	private ArrayList<String> sourceURL = null;
	private ArrayList<String> destinationURL = null;
	private ArrayList<String> mappingsToAdd = null;

	private static String rftServiceHost = "localhost";

	private String clientHost = "localhost";

	private String clientWorkingDirectory = "";

	private String remoteWorkingDirectory = "";

	private String unique_id = "";

	/**
	 * For RLS queries.
	 */
	private RLSManager rlsmanager = null;

	/**
	 * Constant representing the upload operation.
	 */
	public static final int OP_UPLOAD = 0;

	/**
	 * Constant representing the download operation.
	 */
	public static final int OP_DOWNLOAD = 1;

	/**
	 * Logger.
	 */
	private static Log log = LogFactory
			.getLog(ReliableFileTransferManager.class);

	/**
	 * An array of shared files to transfer.
	 */
	private ArrayList<String> sharedFilesArray = null;

	/**
	 * An array of per job files to transfer.
	 */
	private ArrayList<String[]> perJobFilesArray = null;

	/**
	 * Flag indicating an upload or a download. Must be either OP_DOWNLOAD or
	 * OP_UPLOAD.
	 */
	private int operation;

	/**
	 * Condition variable set to true when the transfer completes.
	 */
	private boolean transfersDone = false;

	/**
	 * Condition variable true on successful completion; false otherwise.
	 */
	private boolean success = false;

	/**
	 * Stores symlinks that need to be created on the Grid server after a
	 * successful upload.
	 */
	private String symlinks_string = "";

	/**
	 * The constructor will set up the file transfer.
	 * 
	 * @param files
	 *            an array of relative file names to transfer
	 * @param operation
	 *            one of OP_UPLOAD or OP_DOWNLOAD
	 * @param cwd
	 *            the client working directory
	 * @param rwd
	 *            the remote working directory
	 * @param factoryHost
	 *            the name of the RFT service host; only used for the upload
	 *            operation
	 * @throws Exception
	 */
	public ReliableFileTransferManager(ArrayList<String> sharedFiles,
			ArrayList<String[]> perJobFiles, int operation, String cwd,
			String rwd, String factoryHost) throws Exception {

		Properties env = new Properties();
		env.load(Runtime.getRuntime().exec("env").getInputStream());
		globusLocation = (String) env.get("GLOBUS_LOCATION");

		// initialize RLS manager
		rlsmanager = new RLSManager();

		if (operation == OP_UPLOAD) {
			rftServiceHost = factoryHost;
		} else {
			// determine the local hostname
			rftServiceHost = InetAddress.getLocalHost().getHostName();
		}

		sharedFilesArray = sharedFiles;
		perJobFilesArray = perJobFiles;

		sourceURL = new ArrayList<String>();
		destinationURL = new ArrayList<String>();
		mappingsToAdd = new ArrayList<String>();

		if (cwd.indexOf(" ") == -1) {
			clientWorkingDirectory = cwd;
		} else {
			throw new Exception("Current working directory \"" + cwd
					+ "\" is not allowed to contain spaces!");
		}

		remoteWorkingDirectory = rwd;
		String[] workingChunks = remoteWorkingDirectory.split("/");
		unique_id = workingChunks[workingChunks.length - 1];

		// check for a legal operation
		if (operation != OP_UPLOAD && operation != OP_DOWNLOAD) {
			log.error("Invalid operation " + operation);
			throw new Exception("Invalid operation " + operation); 
		}
		this.operation = operation;

		// determine the client hostname
		if (operation == OP_UPLOAD) {
			// probably only works on linux
			clientHost = (String) env.get("HOSTNAME");

			// change remoteWorkingDirectory to be the workingDirBase
			remoteWorkingDirectory = "";
			for (int i = 0; i < workingChunks.length - 1; i++) {
				remoteWorkingDirectory += workingChunks[i] + "/";
			}
		} else {
			// we must read the client host from where it is saved in the
			// working directory on the server
			try {
				BufferedReader br = new BufferedReader(new FileReader(rwd
						+ "chn.txt"));
				clientHost = br.readLine();
				br.close();
			} catch (Exception e) {
				log.error("Exception: " + e);
			}
		}

		if (sharedFilesArray == null && perJobFilesArray == null) {
			// null means we want to transfer back the entire output directory
			log.debug("sharedFilesArray is null!");
			log.debug("perJobFilesArray is null!");

			if (operation == OP_DOWNLOAD) {
				sourceURL.add("gsiftp://" + rftServiceHost
						+ remoteWorkingDirectory + unique_id + ".output/");
				destinationURL
						.add("gsiftp://" + clientHost + clientWorkingDirectory
								+ "/" + unique_id + ".output/");

				// temporary, until we can push this functionality onto the
				// remote resource
				String outputFolder = remoteWorkingDirectory + unique_id
						+ ".output/";
				File tarfile = new File(remoteWorkingDirectory + unique_id
						+ ".output/" + unique_id + ".tar.gz");
				if (!tarfile.exists()) {
					String[] command = new String[] {
							"/bin/sh",
							"-c",
							"cd " + outputFolder + " && tar cvfz " + unique_id
									+ ".tar.gz job* && rm -rf job* && cd -" };
					GSBLUtils.executeCommand(command);
				}

				// chown -R gt4admin:biogrid and chmod -R g+w and chmod -R a+r
				// the output directory
				GSBLUtils.executeCommand("chown -R gt4admin:biogrid "
						+ remoteWorkingDirectory + unique_id + ".output/");
				GSBLUtils.executeCommand("chmod -R g+w "
						+ remoteWorkingDirectory + unique_id + ".output/");
				GSBLUtils.executeCommand("chmod -R a+r "
						+ remoteWorkingDirectory + unique_id + ".output/");
			}
		} else {
			// we may be uploading shared files only, per job files only, or
			// both!
			// if (perJobFilesArray != null) {
			// for (String[] fileArray : perJobFilesArray) {
			// log.debug("fileArray.length: " + fileArray.length);
			// }
			// }

			if (operation == OP_UPLOAD) {
				int i = 0, j = 0, k = 0;

				if (sharedFilesArray != null) {
					log.debug("sharedFilesArray.size: "
							+ sharedFilesArray.size());
					for (j = i; j < sharedFilesArray.size(); j++) {
						String file = sharedFilesArray.get(j);

						// make sure the file name contains no spaces!
						if (file.indexOf(" ") != -1) {
							throw new Exception("Filename " + file
									+ " contains spaces!  This is not allowed.");
						}

						// make sure the file exists
						File f = new File(file);
						if (!f.exists()) {
							throw new Exception("File " + file
									+ " does not exist!");
						}

						// make sure the file is readable
						String[] command = new String[] { "chmod", "-R", "a+r",
								file };
						GSBLUtils.executeCommand(command);

						String filename = f.getName();
						String md5sum_of_file = checkCache(file);
						String md5sum_prefix = md5sum_of_file.substring(0, 3);
						// update this filename to be of the form
						// md5sum_prefix/md5sum/filename
						sharedFilesArray.set(j, md5sum_prefix + "/"
								+ md5sum_of_file + "/" + filename);
					}
				}
				i = j;
				log.debug("i is: " + i);
				log.debug("perJobFilesArray.size is: "
						+ perJobFilesArray.size());
				if (perJobFilesArray != null) {
					log.debug("in per job files");
					for (k = 0; k < perJobFilesArray.size(); k++) {
						log.debug("k is: " + k);
						//log.debug("fileArray.length: " + fileArray.length);
						String[] files = perJobFilesArray.get(k);
						for (int a = 0; a < files.length; a++) {
							String file = files[a];

							// make sure the file name contains no spaces!
							if (file.indexOf(" ") != -1) {
								throw new Exception(
										"Filename "
												+ file
												+ " contains spaces!  This is not allowed.");
							}

							// make sure the file exists
							File f = new File(file);
							if (!f.exists()) {
								throw new Exception("File " + file
										+ " does not exist!");
							}

							// make sure the file is readable
							String[] command = new String[] { "chmod", "-R",
									"a+r", file };
							GSBLUtils.executeCommand(command);

							String filename = f.getName();
							String md5sum_of_file = checkCache(file);
							String md5sum_prefix = md5sum_of_file.substring(0,
									3);
							// update this filename to be of the form
							// md5sum_prefix/md5sum/filename
							files[a] = md5sum_prefix + "/" + md5sum_of_file
									+ "/" + filename;
						}
						perJobFilesArray.set(k, files);
					}
				}
			} else { // must be a download - let's assume downloads use only the
						// shared files array, for now
				try {
					for (int i = 0; i < sharedFilesArray.size(); i++) {
						String file = sharedFilesArray.get(i);
						log.debug("file is: " + file);
						String filename = new File(file).getName();
						log.debug("filename is: " + filename);
						if (file.charAt(file.length() - 1) == '/') { // accomodate
																		// directories
																		// properly
							sourceURL.add("gsiftp://" + rftServiceHost
									+ remoteWorkingDirectory + filename + "/");
							destinationURL.add("gsiftp://" + clientHost
									+ clientWorkingDirectory + "/" + filename
									+ "/");
						} else {
							sourceURL.add("gsiftp://" + rftServiceHost
									+ remoteWorkingDirectory + filename);
							destinationURL.add("gsiftp://" + clientHost
									+ clientWorkingDirectory + "/" + filename);
						}

						// chown -R gt4admin:biogrid and chmod -R g+w and chmod
						// -R a+r each output file'
						GSBLUtils.executeCommand("chown -R gt4admin:biogrid "
								+ remoteWorkingDirectory + filename);
						GSBLUtils.executeCommand("chmod -R g+w "
								+ remoteWorkingDirectory + filename);
						GSBLUtils.executeCommand("chmod -R a+r "
								+ remoteWorkingDirectory + filename);
					}
				} catch (Exception e) {
					log.error("Exception: " + e);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Checks RLS for the existence of this file in the cache on the Grid
	 * server, and acts appropriately.
	 * 
	 * @return md5sum of file
	 */
	private String checkCache(String file) {
		String filename = new File(file).getName();

		// bit of a "hack" to deal with enumerated input files
		String choppedFilename = filename;
		if (filename.lastIndexOf("_") != -1
				&& (filename.substring(filename.lastIndexOf("_") + 1))
						.matches("\\d*")) {
			choppedFilename = filename.substring(0, filename.lastIndexOf("_"));
		}

		String full_path_to_file = "";
		if (file.charAt(0) == '/') {
			full_path_to_file = file;
		} else {
			full_path_to_file = clientWorkingDirectory + "/" + file;
		}

		String md5sum_of_file = RLSManager.getMD5Sum(full_path_to_file);
		String size_of_file = RLSManager.getFileSize(full_path_to_file);
		String md5sum_prefix = md5sum_of_file.substring(0, 3);

		// add file staging directive for files if they do not already exist in
		// the cache!
		ArrayList<String> pfns = rlsmanager.getPFNs(md5sum_of_file);
		// search through results to see if file exists on grid server
		boolean foundMatchingHost = false;
		boolean foundMatchingFile = false;
		for (String pfn : pfns) {
			if (pfn.indexOf(rftServiceHost) != -1
					&& pfn.indexOf("${GLOBUS_SCRATCH_DIR}") == -1) {
				// scratch dir should not exist,
				// which avoids confusion with remote resources
				// we have a matching host, so this unique file should exist on
				// the grid server
				foundMatchingHost = true;
				if (pfn.indexOf(choppedFilename) != -1) {
					// we have a matching file name, so we should not need to
					// create a new symlink
					foundMatchingFile = true;
				}
			}
		}

		// create string to use as location of file on grid server
		String fileLocation = "gsiftp://" + rftServiceHost
				+ remoteWorkingDirectory + "cache/" + md5sum_prefix + "/"
				+ md5sum_of_file + "/" + choppedFilename;

		if (foundMatchingHost == false) {
			// we have to upload the file, register its new location with
			// RLS, and create a symlink
			// create an empty directory in our temporary cache whose name is
			// the md5sum of the file
			GSBLUtils.executeCommand("mkdir -p " + tempUploadLocation
					+ unique_id + "/" + md5sum_prefix + "/" + md5sum_of_file);

			// adding a hook to enable upload of absolute file paths!
			if (file.charAt(0) == '/') {
				GSBLUtils.executeCommand("cp " + file + " "
						+ tempUploadLocation + unique_id + "/" + md5sum_prefix
						+ "/" + md5sum_of_file + "/" + md5sum_of_file);
			} else {
				GSBLUtils.executeCommand("cp " + clientWorkingDirectory + "/"
						+ file + " " + tempUploadLocation + unique_id + "/"
						+ md5sum_prefix + "/" + md5sum_of_file + "/"
						+ md5sum_of_file);
			}

			// set sourceURL[0] and destinationURL[0] equal to our temporary
			// cache (which we will upload all at once)
			if (sourceURL.isEmpty()) {
				sourceURL.add("gsiftp://" + clientHost + tempUploadLocation
						+ unique_id + "/");
			}
			if (destinationURL.isEmpty()) {
				destinationURL.add("gsiftp://" + rftServiceHost
						+ remoteWorkingDirectory + "cache/");
			}

			if (pfns.size() == 0) { // we need to create a new LFN -> PFN
									// mapping in RLS
				mappingsToAdd.add(md5sum_of_file + "," + fileLocation + ","
						+ "true," + size_of_file);
			} else { // we need to add a new LFN -> PFN mapping in RLS
				mappingsToAdd.add(md5sum_of_file + "," + fileLocation + ","
						+ "false," + size_of_file);
			}

			symlinks_string += remoteWorkingDirectory + "cache/"
					+ md5sum_prefix + "/" + md5sum_of_file + "/"
					+ md5sum_of_file + ":" + remoteWorkingDirectory + "cache/"
					+ md5sum_prefix + "/" + md5sum_of_file + "/"
					+ choppedFilename + ",";

		} else if (foundMatchingFile == false) {
			// we need to register the file with RLS and create a symlink

			mappingsToAdd.add(md5sum_of_file + "," + fileLocation + ","
					+ "false," + size_of_file);

			symlinks_string += remoteWorkingDirectory + "cache/"
					+ md5sum_prefix + "/" + md5sum_of_file + "/"
					+ md5sum_of_file + ":" + remoteWorkingDirectory + "cache/"
					+ md5sum_prefix + "/" + md5sum_of_file + "/"
					+ choppedFilename + ",";

		} else {
			// we still need to update the requested time and the in_use flag
			RLSManager.updateRequestedDate(fileLocation);
			// RLSManager.updateInUse(fileLocation, "1"); // 1 == "true"
		}

		return md5sum_of_file;
	}

	/**
	 * Initiate the file transfer.
	 */
	public synchronized void beginTransfer() throws Exception {
		success = false;
		// if there are no files to transfer, pretend we were successful
		if (sourceURL.size() == 0) {
			transfersDone = true;
			success = true;
		} else {
			if (operation == OP_UPLOAD) {
				transferFiles("./");
				if (success == true) {
					// add mappings
					for (String mapping : mappingsToAdd) {
						String[] chunks = mapping.split(",");
						String md5sum = chunks[0];
						String file = chunks[1];
						String is_app = chunks[2];
						String size = chunks[3];

						if (is_app.equals("true")) {
							rlsmanager.addMapping(md5sum, file, true, size);
						} else {
							rlsmanager.addMapping(md5sum, file, false, size);
						}
					}

					// clean up temporary cache directory
					GSBLUtils.executeCommand("rm -rf " + tempUploadLocation
							+ unique_id);
				} else {
					log.error("RFT transfer was unsuccessful!");
				}
			} else {
				// invoke the rft command line client to send stuff back to the
				// client
				// write an rft command file to the working directory
				transferFiles(remoteWorkingDirectory);
			}
		}
	}

	public void transferFiles(String writeToDirectory) {
		// ensure unique transfers file name
		String transfersFile = "transfers_" + unique_id + ".txt";

		try {
			String document = "";
			for (int i = 0; i < sourceURL.size(); i++) {
				document += sourceURL.get(i) + " " + destinationURL.get(i)
						+ "\n";
			}

			PrintWriter pr = new PrintWriter(new FileWriter(writeToDirectory
					+ transfersFile));
			pr.println(document);
			pr.close();
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		// make the call to rft
		try {
			// get the database host from the db.location file
			BufferedReader br = new BufferedReader(new FileReader(new File(
					globusLocation + "/service_configurations/rft.location")));

			String db_host = br.readLine();
			br.close();

			File successFile = new File(writeToDirectory + transfersFile
					+ ".success");
			File finishedFile = new File(writeToDirectory + transfersFile
					+ ".finished");

			int counter = 1;
			while (success == false && counter <= 3) {
				log.debug("RFT: attempt # " + counter);
				counter++;

				String[] rftCommand = new String[] {
						"/bin/sh",
						"-c",
						"globus-crft -c -s -m -d -vb -r 5 -f "
								+ writeToDirectory + transfersFile
								+ " -e https://" + db_host
								+ ":8443/wsrf/services > " + writeToDirectory
								+ transfersFile + ".success 2>&1 && touch "
								+ writeToDirectory + transfersFile
								+ ".finished" };

				Process proc = GSBLUtils.executeCommand(rftCommand, false);

				int elapsedTime = 0;
				while (!finishedFile.exists()) {
					try {
						Thread.sleep(3000);
					} catch (Exception e) {
						log.error("Exception while sleeping during rft transfer: "
								+ e);
					}
					elapsedTime += 3;
					if (elapsedTime > 86400) { // allow 24 hours for file
												// transfer
						log.debug("rft transfer is hung, destroying RFT process... ");
						proc.destroy();
						break;
					}
				}

				if (successFile.exists()) {
					br = new BufferedReader(new FileReader(successFile));
					String line = null;
					while ((line = br.readLine()) != null) {
						log.debug("RFT (stdout): " + line);
						// if(line.indexOf("All Transfers are completed") != -1)
						// { // GT 4.0.x
						// if(line.indexOf("/0/0/0/0") != -1) { // GT 4.1.x --
						// is this check good enough?
						if (line.indexOf("Status: Done") != -1) { // GT 4.2.x
							success = true;
						}
					}
					br.close();
				}
			}

			this.transfersDone = true;

			// delete the transfers file
			// File deleteTransfer = new File(writeToDirectory + transfersFile);
			// deleteTransfer.delete();

			if (success == true) {

				// delete the transfers file
				File deleteTransfer = new File(writeToDirectory + transfersFile);
				deleteTransfer.delete();

				// delete the success file
				successFile.delete();

				// delete the finished file
				finishedFile.delete();
			}
		} catch (Throwable t) {
			StringWriter tStack = new StringWriter();
			t.printStackTrace(new PrintWriter(tStack));
			log.error(tStack);
		}
	}

	/**
	 * Wait for an ongoing transfer to complete. Functions blocks until the
	 * transfer has been completed, and polls every ten seconds.
	 */
	public boolean waitComplete() {
		log.debug("Waiting for file transfer to complete");
		synchronized (this) {
			while (!transfersDone) {
				try {
					wait(10000);
				} catch (Exception e) {
					log.error("Exception: " + e.getMessage());
				}
			}
		}
		return success;
	}

	public String getSymlinksString() {
		return symlinks_string;
	}

	public ArrayList<String> getSharedFilesArray() {
		return sharedFilesArray;
	}

	public ArrayList<String[]> getPerJobFilesArray() {
		return perJobFilesArray;
	}
}
