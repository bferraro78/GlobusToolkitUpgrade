/**
 * @author Adam Bazinet
 */

package edu.umd.umiacs.cummings.GSBL;

import java.lang.Runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Properties;
import java.util.ArrayList;

public class GSBLUtils {

	/**
	 * Logger.
	 */
	private static Log log = LogFactory.getLog(GSBLUtils.class);

	public static void executeCommand(String command) {
		try {
			Runtime r = Runtime.getRuntime();
			Process proc = r.exec(command);
			int exitVal = proc.waitFor();
			if (exitVal == 0) {
				log.debug(command + " was successful!");
			} else {
				log.debug(command + " was NOT successful!");
			}
		} catch (Throwable t) {
			StringWriter tStack = new StringWriter();
			t.printStackTrace(new PrintWriter(tStack));
			log.error(tStack);
		}
	}

	public static void executeCommand(String[] command) {
		try {
			Runtime r = Runtime.getRuntime();
			Process proc = r.exec(command);
			int exitVal = proc.waitFor();
			if (exitVal == 0) {
				log.debug(java.util.Arrays.toString(command)
						+ " was successful!");
			} else {
				log.debug(java.util.Arrays.toString(command)
						+ " was NOT successful!");
			}
		} catch (Throwable t) {
			StringWriter tStack = new StringWriter();
			t.printStackTrace(new PrintWriter(tStack));
			log.error(tStack);
		}
	}

	public static Process executeCommand(String[] command, boolean waitfor) {
		Process proc = null;
		try {
			Runtime r = Runtime.getRuntime();
			proc = r.exec(command);
			if (waitfor) {
				int exitVal = proc.waitFor();
				if (exitVal == 0) {
					log.debug(java.util.Arrays.toString(command)
							+ " was successful!");
				} else {
					log.debug(java.util.Arrays.toString(command)
							+ " was NOT successful!");
				}
			}
		} catch (Throwable t) {
			StringWriter tStack = new StringWriter();
			t.printStackTrace(new PrintWriter(tStack));
			log.error(tStack);
		}
		return proc;
	}

	public static void executeCommand(String command, String workingDirectory) {

		File workingDir = new File(workingDirectory);

		try {
			Runtime r = Runtime.getRuntime();
			Process proc = r.exec(command, null, workingDir);
			int exitVal = proc.waitFor();
			if (exitVal == 0) {
				log.debug(command + " was successful!");
			} else {
				log.debug(command + " was NOT successful!");
			}
		} catch (Throwable t) {
			StringWriter tStack = new StringWriter();
			t.printStackTrace(new PrintWriter(tStack));
			log.error(tStack);
		}
	}

	public static String executeCommandReturnOneLine(String command,
			String workingDirectory, boolean waitfor) {
		File workingDir = new File(workingDirectory);
		Process proc = null;
		String output = "";
		try {
			Runtime r = Runtime.getRuntime();
			proc = r.exec(command, null, workingDir);
			if (waitfor) {
				int exitVal = proc.waitFor();
				if (exitVal == 0) {
					log.debug(command + " was successful!");
				} else {
					log.debug(command + " was NOT successful!");
				}
			}
			String line = "";
			InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				output = line;
			}
			br.close();
		} catch (Throwable t) {
			StringWriter tStack = new StringWriter();
			t.printStackTrace(new PrintWriter(tStack));
			log.error(tStack);
		}
		return output;
	}

	public static String getConfigElement(String configElement) {
		String configValue = "null";
		try {
			Properties env = new Properties();
			env.load(Runtime.getRuntime().exec("env").getInputStream());
			String globusLocation = ""; //(String) env.get("GLOBUS_LOCATION");
			File configFile = new File(globusLocation
					+ "/service_configurations/" + configElement);
			if (configFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(
						configFile));
				configValue = br.readLine();
				br.close();
			}
		} catch (Exception e) {
			log.error("Error getting config element " + configElement + ": "
					+ e);
		}
		return configValue;
	}

	public static void updateDrupalStatus(String drupalUpdateURL,
			String unique_id, String status, String replicates) {
		try {
			if (drupalUpdateURL == null) {
				drupalUpdateURL = GSBLUtils
						.getConfigElement("drupal_update_url");
			}
			String query = drupalUpdateURL + "?job_id=" + unique_id
					+ "&status=" + status + "&replicates=" + replicates;
			log.debug("Drupal update query is: " + query);
			URL drupalUpdate = new URL(query);
			URLConnection dc = drupalUpdate.openConnection();

			// set the connection timeout to 5 seconds and the read timeout to
			// 10 seconds
			// dc.setConnectTimeout(5000);
			// dc.setReadTimeout(10000);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					dc.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null)
				log.debug("update status response: " + inputLine);

			in.close();

		} catch (Exception e) {
			log.error("Error while updating Drupal status: " + e);
			// retry after delay
			/*
			 * try { Thread.sleep(60000); } catch (Exception ex) {
			 * log.error("Error while sleeping: " + ex); }
			 * updateDrupalStatus(drupalUpdateURL, unique_id, status,
			 * replicates);
			 */
		}
	}

	// note: constraint must be varchar
	public static String selectDBStringField(String table, String field,
			String constraint, String constraint_val) {
		String result = null;
		Connection connection = null;
		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);
			connection = DriverManager.getConnection(GSBLUtils.findDB());
			Statement stmt = connection.createStatement();
			String query = "SELECT " + field + " FROM " + table + " WHERE "
					+ constraint + " = '" + constraint_val + "' LIMIT 1";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				result = rs.getString(1);
			}

			if (result == null) {
				log.error("The database query returned no results: " + query);
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
		return result;
	}

	/*
	 * Fetch all DISTINCT values for a given column in a table
	 */

	public static ArrayList<String> returnDistinctGarliValues(String field) {
		Connection connection = null;
		ArrayList<String> results = new ArrayList();

		try {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver")
					.newInstance();
			DriverManager.registerDriver(driver);
			connection = DriverManager.getConnection(GSBLUtils.findDB());
			Statement stmt = connection.createStatement();
			/*
			 * SELECT DISTINCT column FROM ( SELECT column FROM table LEFT JOIN
			 * ( timeTable ) USING ( synchronized_field ) WHERE (time_field <
			 * DATE_SUB(NOW(), INTERVAL numDays DAY )) ) AS table
			 */
			String query = "SELECT DISTINCT "
					+ field
					+ " FROM garli LEFT JOIN ( portal_job ) ON ( garli.portal_job_id = portal_job.id ) WHERE created < DATE_SUB( NOW(), INTERVAL 7 DAY) AND profiled = 1";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				results.add(rs.getString(1));
			}

			if (results == null || results.size() == 0) {
				log.error("The database query returned no results: " + query);
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
		return results;
	}

	/**
	 * Read the db.location file located in
	 * $GLOBUS_LOCATION/service_configurations/ this file contains the location
	 * of the database that this service should log its job with.
	 * 
	 * @return a jdbc string for the database to use for logging.
	 */
	public static String findDB() {
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
}
