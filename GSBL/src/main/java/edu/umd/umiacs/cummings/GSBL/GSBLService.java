/**
 * @author Adam Bazinet
 */
package edu.umd.umiacs.cummings.GSBL;

import java.lang.Runtime;

import java.rmi.RemoteException;

// Logging.
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// For getting environment variables.
import java.util.Properties;

import java.lang.Runtime;
import java.io.*;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

// For mucking with the argument string.
import java.util.ArrayList;

// Imports for Database entry.
import java.util.Calendar;
import java.util.Vector;
import java.sql.*;

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
	final static protected String workingDirBase = "test/";

	static protected String home = "";

	/**
	 * This will be our service name, e.g., "Ssearch34".
	 */
	protected String name = "not initialized";

	/**
	 * Constructor for a GSBLService. Sets the service's name.
	 * 
	 * @param name - The name of the service.
	 */
	public GSBLService(String name) {
		this.name = name;

		try {
			Properties env = new Properties();
			env.load(Runtime.getRuntime().exec("env").getInputStream());
			home = (String) env.get("HOME");
		} catch (Exception e) {
			log.error("Exception: " + e);
		}

		if (log.isDebugEnabled()) {
			log.debug("Created a new GSBLService with name: " + name + ".");
		}
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
	 * Serializes "argBean" and writes it to our working directory.
	 */
	public synchronized void setArguments(Object argBean, String myWorkingDir) {
		try {
			// Create output stream.
			FileOutputStream fos = new FileOutputStream(myWorkingDir + "argBean");

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
		if (argfile.length() < 1000) {  // Probably didn't work, so retry.
			try {
				// Create output stream.
				FileOutputStream fos = new FileOutputStream(myWorkingDir + "argBean");

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
	 * @return the service name, e.g., "Ssearch34".
	 */
	public String getName() {
		return name;
	}

	/**
	 * Add a job that has been successfully submited to to our database.
	 */
	public synchronized void addToDB(String user, String app, String jobname,
			String job_id, String arguments, String scheduler, String resource,
			String arch_os, String cpus, String replicates, String runtime_estimate,
			String runtime_estimate_recent, String searchreps, String bootstrapreps,
			Object gramID, String myWorkingDir, String hostname) {

		// Changed parameter "workingDir" to "job_id".
		log.debug("job id is: " + job_id);

		// Break apart architecture and operating system.
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// Time that this job was submitted to the grid.
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH) + 1);
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = (yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec);

		Connection connection = null;

		// Open up db.location file and find out who we should be talking to.
		String db = findDB();
//		log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM user WHERE user_name = '"
					+ user + "' LIMIT 1");

			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = ("INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os, cpus, replicates, gram_id, client_working_dir, client_hostname, runtime_estimate, runtime_estimate_recent, searchreps, bootstrapreps) "
						+ "VALUES ('" + dateStr + "'," + id + ",'" + job_id + "','" + app
						+ "','" + jobname + "','" + arguments + "','" + scheduler + "','"
						+ resource + "','" + architecture + "','" + os + "','" + cpus
						+ "','" + replicates + "','" + gramID + "','" + myWorkingDir + "','"
						+ hostname + "','" + runtime_estimate + "','"
						+ runtime_estimate_recent + "','" + searchreps + "','"
						+ bootstrapreps + "')");
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
			String job_id, String arguments, String scheduler, String resource,
			String arch_os, String cpus, String replicates, String runtime_estimate,
			String searchreps, String bootstrapreps) {

		// Changed parameter "workingDir" to "job_id".
		log.debug("job id is: " + job_id);

		// Break apart architecture and operating system.
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// Time that this job was submitted to the grid.
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH) + 1);
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = (yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec);

		Connection connection = null;

		// Open up db.location file and find out who we should be talking to.
		String db = findDB();
//		log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM user WHERE user_name = '"
					+ user + "' LIMIT 1");
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = ("INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os, cpus, replicates, runtime_estimate, searchreps, bootstrapreps) "
						+ "VALUES ('" + dateStr + "'," + id + ",'" + job_id + "','" + app
						+ "','" + jobname + "','" + arguments	+ "','" + scheduler	+ "','"
						+ resource + "','" + architecture + "','" + os + "','" + cpus
						+ "','" + replicates + "','" + runtime_estimate + "','" + searchreps
						+ "','" + bootstrapreps + "')");
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
			String job_id, String arguments, String scheduler,
			String resource, String arch_os, String cpus, String replicates,
			String runtime_estimate) {

		// Changed parameter "workingDir" to "job_id".
		log.debug("job id is: " + job_id);

		// Break apart architecture and operating system.
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// Time that this job was submitted to the grid.
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH) + 1);
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = (yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec);

		Connection connection = null;

		// Open up db.location file and find out who we should be talking to.
		String db = findDB();
//		log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM user WHERE user_name = '"
					+ user + "' LIMIT 1");

			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = ("INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os, cpus, replicates, runtime_estimate) "
						+ "VALUES ('" + dateStr + "'," + id + ",'" + job_id + "','" + app
						+ "','" + jobname + "','" + arguments + "','" + scheduler + "','"
						+ resource + "','" + architecture + "','" + os + "','" + cpus
						+ "','" + replicates + "','" + runtime_estimate + "')");
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
			String job_id, String arguments, String scheduler,
			String resource, String arch_os, String cpus, String replicates) {

		// Changed parameter "workingDir" to "job_id".
		log.debug("job id is: " + job_id);

		// Break apart architecture and operating system.
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// Time that this job was submitted to the grid.
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH) + 1);
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = (yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec);

		Connection connection = null;

		// Open up db.location file and find out who we should be talking to.
		String db = findDB();
//		log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM user WHERE user_name = '"
					+ user + "' LIMIT 1");
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = ("INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os, cpus, replicates) "
						+ "VALUES ('" + dateStr + "'," + id + ",'" + job_id + "','" + app
						+ "','" + jobname + "','" + arguments + "','" + scheduler	+ "','"
						+ resource + "','" + architecture + "','" + os + "','" + cpus
						+ "','" + replicates + "')");
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
			String job_id, String arguments, String scheduler,
			String resource, String arch_os) {

		// Changed parameter "workingDir" to "job_id".
		log.debug("job id is: " + job_id);

		// Break apart architecture and operating system.
		String architecture = arch_os.substring(0, arch_os.lastIndexOf("_"));
		String os = arch_os.substring(arch_os.lastIndexOf("_") + 1);

		// Time that this job was submitted to the grid.
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH) + 1);
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = (yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec);

		Connection connection = null;

		// Open up db.location file and find out who we should be talking to.
		String db = findDB();
//		log.debug("db is: " + db);

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			log.debug("jobname is: " + jobname);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM user WHERE user_name = '"
					+ user + "' LIMIT 1");
			int id = -1;
			while (rs.next()) {
				id = rs.getInt(1);
			}

			if (id != -1) {
				log.debug("About to insert job!");
				String query = ("INSERT INTO job (submitted_time, user_id, unique_id, app, job_name, args, scheduler, resource, arch, os) "
						+ "VALUES ('" + dateStr + "'," + id + ",'" + job_id + "','" + app
						+ "','" + jobname + "','" + arguments + "','" + scheduler + "','"
						+ resource + "','" + architecture + "','" + os + "')");
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

		// Time that this db update was made.
		Calendar cal = Calendar.getInstance();
		int yr = cal.get(Calendar.YEAR);
		int month = (cal.get(Calendar.MONTH) + 1);
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String dateStr = (yr + "-" + month + "-" + day + " " + hour + ":" + min
				+ ":" + sec);

//		log.debug("updating job status: " + status + " at: " + dateStr);

		// First get the unique id from the working directory.
		String[] workingChunks = workingDir.split("/");
		String unique_id = workingChunks[workingChunks.length - 1];

		Connection connection = null;

		// Open up db.location file and find out who we should be talking to.
		String db = findDB();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			Statement stmt = connection.createStatement();

			// 4 is "finished", 5 is "failed", 10 is "files successfully retrieved",
			// and 11 is "files NOT successfully retrieved" (but they're all "end"
			// states, so update the finish_time in addition to the status).
			int ret;
			if (status.equals("4") || status.equals("5") || status.equals("10")
					|| status.equals("11")) {
				ret = stmt.executeUpdate("UPDATE job SET status = " + status
						+ ", finish_time = '" + dateStr
						+ "', update_delay = 0 WHERE unique_id = '" + unique_id + "'");
			} else if (status.equals("2")) {
				// 2 is "running", so update start_time if it is "0000-00-00 00:00:00".
				ResultSet rs = stmt
						.executeQuery("SELECT start_time, update_delay FROM job WHERE unique_id = '"
								+ unique_id + "'");
				if (rs.next()) {  // One row should be returned.
					int update_delay = rs.getInt("update_delay");
					if (update_delay == 0) {
						update_delay = update_interval;
					} else if ((update_delay * 2) <= update_max) {
						update_delay = (update_delay * 2);
					}
					if (rs.getTimestamp("start_time") == null) {
						ret = stmt.executeUpdate("UPDATE job SET status = " + status
								+ ", start_time = '" + dateStr + "', update_delay = "
								+ update_delay + " WHERE unique_id = '" + unique_id + "'");
					} else {
						ret = stmt.executeUpdate("UPDATE job SET status = " + status
								+ ", update_delay = " + update_delay + " WHERE unique_id = '"
								+ unique_id + "'");
					}
				}
			} else if (status.equals("1")) {
				// 1 is "idle", so update "update_delay".
				ResultSet rs = stmt
						.executeQuery("SELECT update_delay FROM job WHERE unique_id = '"
								+ unique_id + "'");
				if (rs.next()) {  // One row should be returned.
					int update_delay = rs.getInt("update_delay");
					if (update_delay == 0) {
						update_delay = update_interval;
					} else if ((update_delay * 2) <= update_max) {
						update_delay = (update_delay * 2);
					}
					ret = stmt.executeUpdate("UPDATE job SET status = " + status
							+ ", update_delay = " + update_delay + " WHERE unique_id = '"
							+ unique_id + "'");
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

		// Open up db.location file and find out who we should be talking to.
		String db = findDB();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			Statement stmt = connection.createStatement();

			String query = ("SELECT unique_id FROM job WHERE app = '" + serviceName
					+ "' and (status = ");

			for (int i = 0; i < status.length; i++) {
				String myStatus = status[i];
				query += myStatus;
				if ((i + 1) < status.length) {
					query += " OR status = ";
				}
			}
			query += (") and update_delay <= " + timecounter);

			ResultSet rs = stmt.executeQuery(query);
			// Rely only on database query, must remove other jobs not finished yet.

			String jobID = "";
			String jobFolder = "";
			File jobFileFolder = null;
			while (rs.next()) {
				jobID = rs.getString(1);
				// Job folder on scheduler.
				jobFolder = (String) getSchedulerName(jobID) + ":" + home + "/" + jobID;
				jobFileFolder = new File(jobFolder);
				// Double check that the job hasn't been cleaned up yet.
				if (jobFileFolder.exists()) {
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

	public static Object getGramID(String uniqueID) {
		Connection connection = null;
		Object gramID = null;
		String db = findDB();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
				.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			Statement stmt = connection.createStatement();

			String query = ("SELECT gram_id FROM job WHERE unique_id = '" + uniqueID
					+ "'");

			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			gramID = rs.getString(1);
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
		return gramID;
	}

	public static Object getHostname(String uniqueID) {
		Object hostname = null;
		Connection connection = null;
		String db = findDB();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			Statement stmt = connection.createStatement();

			String query = ("SELECT client_hostname FROM job WHERE unique_id = '"
					+ uniqueID + "'");

			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			hostname = rs.getString(1);
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
		return hostname;
	}

	public static Object getSchedulerName(String uniqueID) {
		Object scheduler = null;
		Connection connection = null;
		String db = findDB();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			Statement stmt = connection.createStatement();

			String query = ("SELECT scheduler FROM job WHERE unique_id = '" + uniqueID
					+ "'");

			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			scheduler = rs.getString(1);
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
		return scheduler;
	}

	public static Object getWorkingDir(String uniqueID) {
		Object workingDir = null;
		Connection connection = null;
		String db = findDB();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);

			connection = DriverManager.getConnection(db);
			Statement stmt = connection.createStatement();

			String query = ("SELECT client_working_dir FROM job WHERE unique_id = '"
					+ uniqueID + "'");

			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			workingDir = rs.getString(1);
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
		return workingDir;
	}

	/* There is now a copy of this function in GSBLUtils - classes should be
	 * changed to use that function.
	 */
	/**
	 * Read the db.location file located in
	 * "$GSBL_CONFIG_DIR/service_configurations/". This file contains the
	 * location of the database that this service should log its job with.
	 * 
	 * @return a jdbc string for the database to use for logging.
	 */
	private static String findDB() {
		String ret = null;
		try {
			Properties env = new Properties();
			env.load(Runtime.getRuntime().exec("env").getInputStream());
			String globusLocation = (String) env.get("GSBL_CONFIG_DIR");
			BufferedReader br = new BufferedReader(new FileReader(new File(
							globusLocation + "/service_configurations/db.location")));

			String serv = br.readLine();
			String user = br.readLine();
			String pass = br.readLine();
			String db = "GT6_dev";

			ret = "jdbc:mysql://" + serv + "/" + db + "?user=" + user + "&password="
				+ pass + "&zeroDateTimeBehavior=convertToNull";
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
	 *            The argument we're working with.
	 * @param perJobFiles
	 *            Holds all the arguments.
	 * @param index
	 *            Tells the position of arg in perJobFiles.
	 * 
	 * @return the comma-separated string.
	 */
	public String getArgument(String arg, ArrayList<String[]> perJobFiles,
			int index) {
		String argumentString = "";
		String[] files = perJobFiles.get(index);
		for (int i = 0; i < files.length; i++) {
			String aFile = files[i].substring(files[i].lastIndexOf('/') + 1);
			if (i == (files.length - 1)) {
				argumentString += aFile;
			} else {
				argumentString += (aFile + ",");
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
				// Create symlink.
				try {
					Runtime r = Runtime.getRuntime();
					Process proc = r.exec("ln -s " + target + " " + linkname);
					int exitVal = proc.waitFor();
					if (exitVal == 0) {
						log.debug("ln -s " + target + " " + linkname + " was successful!");
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
