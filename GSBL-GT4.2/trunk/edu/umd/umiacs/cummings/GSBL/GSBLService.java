/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import java.lang.Runtime;

import java.rmi.RemoteException;

// Logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// For getting environment variables
import java.util.Properties;

import java.lang.Runtime;
import java.io.*;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

// For mucking with the argument string
import java.util.ArrayList;

// Imports for Database entry
import java.util.Calendar;
import java.util.Vector;
import java.sql.*;

import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.axis.message.addressing.EndpointReferenceType;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.xml.sax.InputSource;

/**
 * Grid service base class.
 */
public class GSBLService {

	/**
	 * Logger.
	 */
	static Log log = LogFactory.getLog(GSBLService.class.getName());

	/**
	 * A writable directory under which our temporary directories will be
	 * created.
	 */
	final static protected String workingDirBase = "/export/grid_files/";

	/**
	 * This will be our service name, e.g., "Ssearch34".
	 */
	protected String name = "not initialized";

	/**
	 * Constructor for a GSBLService. Sets the service's name.
	 * 
	 * String name The name of the service.
	 */
	public GSBLService(String name) {
		this.name = name;

		if (log.isDebugEnabled()) {
			log.debug("Created a new GSBLService with name: " + name + ".");
		}
	}

	/**
	 * Should be invoked via RPC by a GSBLClient to create the working directory
	 * on the server. Also store the client working directory and the client
	 * hostname in separate files.
	 * 
	 * @param info
	 *            a string of the following form: resourceID + "@--" + cwd +
	 *            "@--" + hostname
	 * @return true if method completes successfully
	 */
	
	public synchronized boolean createWorkingDir(String info) {
		// break apart info
		String[] chunks = info.split("@--");
		String unique_id = chunks[0];
		String cwd = chunks[1];
		String hostname = chunks[2];
		int reps = Integer.parseInt(chunks[3]);

		String myWorkingDir = cwd + "/"; //workingDirBase + unique_id + "/";

		try {

			// construct the full working directory
			/* No longer need to create /export/grid_files/jobID because cwd=(/export/work/drupal/user_files/admin/job#/) already exists */
			
			/* File myFile = new File(myWorkingDir);
			 * if (!myFile.exists()) {
			 *	myFile.mkdir();
			 * }
			 * log.debug("GSBLService using working dir of '" + myWorkingDir
			 *		+ "'.");
			 */

			if (reps > 1) {
				// if reps > 1, create an 'output' folder in our working
				// directory and fill it with sub-job folders
				File outputDir = new File(myWorkingDir + unique_id + ".output/");
				try {
					outputDir.mkdir();
					File tempJobDir = null;
					for (int i = 0; i < reps; i++) {
						tempJobDir = new File(myWorkingDir + unique_id
								+ ".output/job" + i + "/");
						tempJobDir.mkdir();
					}
				} catch (Exception e) {
					log.error("Exception: " + e);
				}
			}

			// write the cwd to a file in our working directory
			/* New system does not require cwd file b/c input files and submission 
			* with take place in working directory 
			*/
			/*
			 * String cwdFilename = myWorkingDir + "cwd.txt";
			 * FileWriter fileWriter = new FileWriter(cwdFilename);
			 * BufferedWriter bfWriter = new BufferedWriter(fileWriter);
			 * bfWriter.write(cwd);
			 * bfWriter.close();
			 * log.debug("client working directory written to file: "
			 *		+ cwdFilename);
			 */

			// write the client's hostname to a file in our working directory
			String hostFilename = myWorkingDir + "chn.txt";
			fileWriter = new FileWriter(hostFilename);
			bfWriter = new BufferedWriter(fileWriter);
			bfWriter.write(hostname);
			bfWriter.close();
			log.debug("client hostname written to file: " + hostFilename);

		} catch (Exception e) {
			log.error("Unable to create GSBLService temporary directory: "
					+ myWorkingDir, e);
		}
		return true;
	}

	/**
	 * Called as a cleanup method after a successful run to delete the working
	 * directory and all of its contents.
	 * 
	 * @param myWorkingDir
	 *            passed as an argument in case myWorkingDir hasn't yet been
	 *            initialized in this class
	 * @return true if the directory was successfully deleted
	 */
	public synchronized boolean deleteWorkingDir(String myWorkingDir) {
		try {
			if (deleteDir(new File(myWorkingDir))) {
				if (log.isDebugEnabled()) {
					log.debug("Deleted GSBLService working directory: "
							+ myWorkingDir);
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error("Problem deleting working directory: " + e);
			return false;
		}
	}

	// Code from http://joust.kano.net/weblog/archives/000071.html
	/**
	 * Method to recursively delete a directory tree.
	 * 
	 * @param dir
	 *            a File object representing the directory to delete.
	 * @return true if the deletion was successful, else false.
	 */
	public static boolean deleteDir(File dir) {

		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return false;
		}

		// to see if this directory is actually a symbolic link to a directory,
		// we want to get its canonical path - that is, we follow the link to
		// the file it's actually linked to
		File candir;
		try {
			candir = dir.getCanonicalFile();
		} catch (IOException e) {
			return false;
		}

		// a symbolic link has a different canonical path than its actual path,
		// unless it's a link to itself
		if (!candir.equals(dir.getAbsoluteFile())) {
			// this file is a symbolic link, and there's no reason for us to
			// follow it, because then we might be deleting something outside of
			// the directory we were told to delete
			return false;
		}

		// now we go through all of the files and subdirectories in the
		// directory and delete them one by one
		File[] files = candir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];

				// in case this directory is actually a symbolic link, or it's
				// empty, we want to try to delete the link before we try
				// anything
				boolean deleted = file.delete();
				if (!deleted) {
					// deleting the file failed, so maybe it's a non-empty
					// directory
					if (file.isDirectory()) {
						deleteDir(file);
					}

					// otherwise, there's nothing else we can do
				}
			}
		}

		// now that we tried to clear the directory out, we can try to delete it
		// again
		return dir.delete();
	}

	/**
	 * Reads the EPR of the associated GramJob from the working directory. File
	 * must be named "jobEPR.txt".
	 * 
	 * @param workingDir
	 *            passed as an argument in case myWorkingDir hasn't yet been
	 *            initialized in this class
	 * @return the EPR for the job associated with this Grid service instance
	 */
	public synchronized EndpointReferenceType getJobEPR(String workingDir) {
		String eprString = workingDir + "jobEPR.txt";
		File eprFile = new File(eprString);
		int seconds = 0;
		while (!eprFile.exists() && seconds < 60) { // give it some time to be
													// written to disk
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				log.error("Exception: " + e);
			}
			seconds += 2;
		}

		EndpointReferenceType jobEPR = null;
		try {
			FileInputStream fis = new FileInputStream(workingDir + "jobEPR.txt");
			jobEPR = (EndpointReferenceType) ObjectDeserializer.deserialize(
					new InputSource(fis), EndpointReferenceType.class);
			fis.close();
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
		return jobEPR;
	}

	/**
	 * Returns our argBean, read from disk.
	 */
	public synchronized Object getArguments(String workingDir) {
		Object myBean = null;
		try {
			// Create input stream.
			FileInputStream fis = new FileInputStream(workingDir + "argBean");

			// Create XML decoder.
			XMLDecoder xdec = new XMLDecoder(fis);

			// Read object.
			myBean = xdec.readObject();

			// Clean up.
			xdec.close();
			fis.close();
		} catch (Exception e) {
			System.err.println("working dir is: " + workingDir);
			log.error("Exception: " + e);
		}

		return myBean;
	}

	/**
	 * Serializes argBean and writes it to our working directory.
	 */
	public synchronized void setArguments(Object argBean, String myWorkingDir) {
		try {
			// Create output stream.
			FileOutputStream fos = new FileOutputStream(myWorkingDir
					+ "argBean");

			// Create XML encoder.
			XMLEncoder xenc = new XMLEncoder(fos);

			// Write object.
			xenc.writeObject(argBean);
			xenc.flush();

			// Clean up.
			xenc.close();
			fos.close();
		} catch (Exception e) {
			System.err.println("argBean is: " + argBean);
			log.error("Exception: " + e);
		}

		File argfile = new File(myWorkingDir + "argBean");
		if (argfile.length() < 1000) { // probably didn't work, so retry
			try {
				// Create output stream.
				FileOutputStream fos = new FileOutputStream(myWorkingDir
						+ "argBean");

				// Create XML encoder.
				XMLEncoder xenc = new XMLEncoder(fos);

				// Write object.
				xenc.writeObject(argBean);
				xenc.flush();

				// Clean up.
				xenc.close();
				fos.close();
			} catch (Exception e) {
				System.err.println("argBean is: " + argBean);
				log.error("Exception: " + e);
			}
		}
	}

	/**
	 * Returns the working directory base.
	 */
	public String getWorkingDirBase() {
		return workingDirBase;
	}

	/**
	 * Get the name of this service.
	 * 
	 * @return the service name, e.g., "Ssearch34"
	 */
	public String getName() {
		return name;
	}

	/**
	 * Add a job that has been successfully submited to to our database.
	 */
	public synchronized void addToDB(String user, String app, String jobname,
			String workingDir, String arguments, String scheduler,
			String resource, String arch_os, String cpus, String replicates,
			String runtime_estimate, String runtime_estimate_recent,
			String searchreps, String bootstrapreps) {

		// get job id from working dir
		String job_id = workingDir.substring(0, workingDir.lastIndexOf("/"));
		job_id = job_id.substring(job_id.lastIndexOf("/") + 1);

		log.debug("job id is: " + job_id);

		// break apart architecture and operating system
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// time that this job was submitted to the grid
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH)) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec;

		Connection connection = null;

		// open up db.location file and find out who we should be talking to
		String db = findDB();
		// log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT id FROM user WHERE user_name = '"
							+ user + "' LIMIT 1");
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = "INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os, cpus, replicates, runtime_estimate, runtime_estimate_recent, searchreps, bootstrapreps) "
						+ "VALUES ('"
						+ dateStr + "',"
						+ id + ",'"
						+ job_id + "','"
						+ app + "','"
						+ jobname + "','"
						+ arguments + "','"
						+ scheduler + "','"
						+ resource + "','"
						+ architecture + "','"
						+ os + "','"
						+ cpus + "','"
						+ replicates + "','"
						+ runtime_estimate + "','"
						+ runtime_estimate_recent + "','"
						+ searchreps + "','"
						+ bootstrapreps + "')";
				log.debug("Query string is: " + query);
				int ret = stmt.executeUpdate(query);
				log.debug("Inserted job!");
			} else {
				log.error("Unknown user, job not added to database!");
			}

		} catch (Exception e) {
			log.error("Exception: " + e);
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * Add a job that has been successfully submited to to our database.
	 */
	public synchronized void addToDB(String user, String app, String jobname,
			String workingDir, String arguments, String scheduler,
			String resource, String arch_os, String cpus, String replicates,
			String runtime_estimate, String searchreps, String bootstrapreps) {

		// get job id from working dir
		String job_id = workingDir.substring(0, workingDir.lastIndexOf("/"));
		job_id = job_id.substring(job_id.lastIndexOf("/") + 1);

		log.debug("job id is: " + job_id);

		// break apart architecture and operating system
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// time that this job was submitted to the grid
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH)) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec;

		Connection connection = null;

		// open up db.location file and find out who we should be talking to
		String db = findDB();
		// log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT id FROM user WHERE user_name = '"
							+ user + "' LIMIT 1");
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = "INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os, cpus, replicates, runtime_estimate, searchreps, bootstrapreps) "
						+ "VALUES ('"
						+ dateStr + "',"
						+ id + ",'"
						+ job_id + "','"
						+ app + "','"
						+ jobname + "','"
						+ arguments	+ "','"
						+ scheduler	+ "','"
						+ resource + "','"
						+ architecture + "','"
						+ os + "','"
						+ cpus + "','"
						+ replicates + "','"
						+ runtime_estimate + "','"
						+ searchreps + "','"
						+ bootstrapreps + "')";
				log.debug("Query string is: " + query);
				int ret = stmt.executeUpdate(query);
				log.debug("Inserted job!");
			} else {
				log.error("Unknown user, job not added to database!");
			}

		} catch (Exception e) {
			log.error("Exception: " + e);
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * Add a job that has been successfully submited to to our database.
	 */
	public synchronized void addToDB(String user, String app, String jobname,
			String workingDir, String arguments, String scheduler,
			String resource, String arch_os, String cpus, String replicates,
			String runtime_estimate) {

		// get job id from working dir
		String job_id = workingDir.substring(0, workingDir.lastIndexOf("/"));
		job_id = job_id.substring(job_id.lastIndexOf("/") + 1);

		log.debug("job id is: " + job_id);

		// break apart architecture and operating system
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// time that this job was submitted to the grid
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH)) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec;

		Connection connection = null;

		// open up db.location file and find out who we should be talking to
		String db = findDB();
		// log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT id FROM user WHERE user_name = '"
							+ user + "' LIMIT 1");
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = "INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os, cpus, replicates, runtime_estimate) "
						+ "VALUES ('"
						+ dateStr + "',"
						+ id + ",'"
						+ job_id + "','"
						+ app + "','"
						+ jobname + "','"
						+ arguments + "','"
						+ scheduler + "','"
						+ resource + "','"
						+ architecture + "','"
						+ os + "','"
						+ cpus + "','"
						+ replicates + "','"
						+ runtime_estimate + "')";
				log.debug("Query string is: " + query);
				int ret = stmt.executeUpdate(query);
				log.debug("Inserted job!");
			} else {
				log.error("Unknown user, job not added to database!");
			}

		} catch (Exception e) {
			log.error("Exception: " + e);
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * Add a job that has been successfully submited to to our database.
	 */
	public synchronized void addToDB(String user, String app, String jobname,
			String workingDir, String arguments, String scheduler,
			String resource, String arch_os, String cpus, String replicates) {

		// get job id from working dir
		String job_id = workingDir.substring(0, workingDir.lastIndexOf("/"));
		job_id = job_id.substring(job_id.lastIndexOf("/") + 1);

		log.debug("job id is: " + job_id);

		// break apart architecture and operating system
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// time that this job was submitted to the grid
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH)) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec;

		Connection connection = null;

		// open up db.location file and find out who we should be talking to
		String db = findDB();
		// log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT id FROM user WHERE user_name = '"
							+ user + "' LIMIT 1");
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = "INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os, cpus, replicates) "
						+ "VALUES ('"
						+ dateStr + "',"
						+ id + ",'"
						+ job_id + "','"
						+ app + "','"
						+ jobname + "','"
						+ arguments + "','"
						+ scheduler	+ "','"
						+ resource + "','"
						+ architecture + "','"
						+ os + "','"
						+ cpus + "','"
						+ replicates + "')";
				log.debug("Query string is: " + query);
				int ret = stmt.executeUpdate(query);
				log.debug("Inserted job!");
			} else {
				log.error("Unknown user, job not added to database!");
			}

		} catch (Exception e) {
			log.error("Exception: " + e);
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * Add a job that has been successfully submited to to our database.
	 */
	public synchronized void addToDB(String user, String app, String jobname,
			String workingDir, String arguments, String scheduler,
			String resource, String arch_os) {

		// get job id from working dir
		String job_id = workingDir.substring(0, workingDir.lastIndexOf("/"));
		job_id = job_id.substring(job_id.lastIndexOf("/") + 1);

		log.debug("job id is: " + job_id);

		// break apart architecture and operating system
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// time that this job was submitted to the grid
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH)) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec;

		Connection connection = null;

		// open up db.location file and find out who we should be talking to
		String db = findDB();
		// log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT id FROM user WHERE user_name = '"
							+ user + "' LIMIT 1");
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = "INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os) "
						+ "VALUES ('"
						+ dateStr + "',"
						+ id + ",'"
						+ job_id + "','"
						+ app + "','"
						+ jobname + "','"
						+ arguments + "','"
						+ scheduler + "','"
						+ resource + "','"
						+ architecture + "','"
						+ os + "')";
				log.debug("Query string is: " + query);
				int ret = stmt.executeUpdate(query);
				log.debug("Inserted job!");
			} else {
				log.error("Unknown user, job not added to database!");
			}

		} catch (Exception e) {
			log.error("Exception: " + e);
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * Update the status of a job. If a job reaches a finished state, update the
	 * finish time as well.
	 */
	public synchronized static void updateDBStatus(String status,
			String workingDir, int update_interval, int update_max) {

		// time that this db update was made
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH)) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec;

		// log.debug("updating job status: " + status + " at: " + dateStr);

		// first get the unique id from the working directory
		String[] workingChunks = workingDir.split("/");
		String unique_id = workingChunks[workingChunks.length - 1];

		Connection connection = null;

		// open up db.location file and find out who we should be talking to
		String db = findDB();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			Statement stmt = connection.createStatement();

			// 4 is "finished", 5 is "failed", 10 is
			// "files successfully retrieved", and 11 is
			// "files NOT successfully retrieved"
			// (but they're all "end" states, so update the finish_time in
			// addition to the status)
			int ret;
			if (status.equals("4") || status.equals("5") || status.equals("10")
					|| status.equals("11")) {
				ret = stmt.executeUpdate("UPDATE job SET status = " + status
						+ ", finish_time = '" + dateStr
						+ "', update_delay = 0 WHERE unique_id = '" + unique_id
						+ "'");
			} else if (status.equals("2")) { // 2 is "running", so update
												// start_time if it is
												// 0000-00-00 00:00:00
				ResultSet rs = stmt
						.executeQuery("SELECT start_time, update_delay FROM job WHERE unique_id = '"
								+ unique_id + "'");
				if (rs.next()) { // one row should be returned
					int update_delay = rs.getInt("update_delay");
					if (update_delay == 0) {
						update_delay = update_interval;
					} else if (update_delay * 2 <= update_max) {
						update_delay = update_delay * 2;
					}
					if (rs.getTimestamp("start_time") == null) {
						ret = stmt.executeUpdate("UPDATE job SET status = "
								+ status + ", start_time = '" + dateStr
								+ "', update_delay = " + update_delay
								+ " WHERE unique_id = '" + unique_id + "'");
					} else {
						// log.debug("timestamp is: " +
						// (rs.getTimestamp("start_time")).toString());
						ret = stmt.executeUpdate("UPDATE job SET status = "
								+ status + ", update_delay = " + update_delay
								+ " WHERE unique_id = '" + unique_id + "'");
					}
				}
			} else if (status.equals("1")) { // 1 is "idle", so update
												// update_delay
				ResultSet rs = stmt
						.executeQuery("SELECT update_delay FROM job WHERE unique_id = '"
								+ unique_id + "'");
				if (rs.next()) { // one row should be returned
					int update_delay = rs.getInt("update_delay");
					if (update_delay == 0) {
						update_delay = update_interval;
					} else if (update_delay * 2 <= update_max) {
						update_delay = update_delay * 2;
					}
					ret = stmt.executeUpdate("UPDATE job SET status = "
							+ status + ", update_delay = " + update_delay
							+ " WHERE unique_id = '" + unique_id + "'");
				}
			} else {
				ret = stmt.executeUpdate("UPDATE job SET status = " + status
						+ " WHERE unique_id = '" + unique_id + "'");
			}
		} catch (Exception e) {
			log.error("Exception: " + e);
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * Returns a list of jobs matching the status given. The objects in the
	 * array will be Strings.
	 */
	public static Object[] getJobList(String serviceName, String[] status,
			int timecounter) {

		Vector jobIDs = new Vector();
		Connection connection = null;

		// open up db.location file and find out who we should be talking to
		String db = findDB();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			Statement stmt = connection.createStatement();

			String query = "SELECT unique_id FROM job WHERE app = '"
					+ serviceName + "' and (status = ";

			for (int i = 0; i < status.length; i++) {
				String myStatus = status[i];
				query += myStatus;
				if (i + 1 < status.length) {
					query += " OR status = ";
				}
			}
			query = query + ") and update_delay <= " + timecounter;

			ResultSet rs = stmt.executeQuery(query);
			String jobID = "";
			String jobFolder = "";
			File jobFileFolder = null;
			while (rs.next()) {
				jobID = rs.getString(1);
				jobFolder = workingDirBase + jobID;
				jobFileFolder = new File(jobFolder);
				if (jobFileFolder.exists()) { // double check that the job
												// hasn't been cleaned up yet
					jobIDs.add(jobID);
				}
			}

		} catch (Exception e) {
			log.error("Exception: " + e);
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

		return jobIDs.toArray();
	}

	// there is now a copy of this function in GSBLUtils - classes should be
	// changed to use that function
	/**
	 * Read the db.location file located in
	 * $GLOBUS_LOCATION/service_configurations/ this file contains the location
	 * of the database that this service should log its job with.
	 * 
	 * @return a jdbc string for the database to use for logging.
	 */
	private static String findDB() {
		String ret = null;
		try {
			Properties env = new Properties();
			env.load(Runtime.getRuntime().exec("env").getInputStream());
			String globusLocation = (String) env.get("GLOBUS_LOCATION");
			BufferedReader br = new BufferedReader(new FileReader(new File(
					globusLocation + "/service_configurations/db.location")));

			String serv = br.readLine();
			String user = br.readLine();
			String pass = br.readLine();
			String db = br.readLine();

			ret = "jdbc:mysql://" + serv + "/" + db + "?user=" + user
					+ "&password=" + pass
					+ "&zeroDateTimeBehavior=convertToNull";
			br.close();

		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return ret;
	}

	/**
	 * Return a bunch of arguments concatenated together as a string.
	 * 
	 * @param arg
	 *            the argument we're working with
	 * @param perJobFiles
	 *            holds all the arguments
	 * @param index
	 *            tells the position of arg in perJobFiles
	 * 
	 * @return the comma-separated string
	 */
	public String getArgument(String arg, ArrayList<String[]> perJobFiles,
			int index) {
		String argumentString = "";
		String[] files = perJobFiles.get(index);
		for (int i = 0; i < files.length; i++) {
			String aFile = files[i].substring(files[i].lastIndexOf('/') + 1);
			if (i == files.length - 1) {
				argumentString += aFile;
			} else {
				argumentString += aFile + ",";
			}
		}
		return argumentString;
	}

	public void makeSymlinks(String symlinks) {
		String[] pairs = symlinks.split(",");

		for (int i = 0; i < pairs.length; i++) {
			String pair = pairs[i];
			String[] chunks = pair.split(":");
			if (chunks.length == 2) {
				String target = chunks[0];
				String linkname = chunks[1];
				// create symlink
				try {
					Runtime r = Runtime.getRuntime();
					Process proc = r.exec("ln -s " + target + " " + linkname);
					int exitVal = proc.waitFor();
					if (exitVal == 0) {
						log.debug("ln -s " + target + " " + linkname
								+ " was successful!");
					} else {
						log.error("ln -s " + target + " " + linkname
								+ " was NOT successful!");
					}
				} catch (Exception e) {
					log.error("Exception: " + e);
				}
			} else {
				log.error("ERROR: symlinks chunks length was not == 2");
			}
		}
	}
}
