/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.database;

import java.net.InetAddress;
import java.sql.*;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import ch.epfl.DerbyServerStarter;
import ch.epfl.qosdisc.operators.*;

/**
 * Singleton for handling the persistent database connection.
 * 
 * @author Sebastian Gerlach
 */
/**
 * @author sgerlach
 *
 */
public class Connection {

	/**
	 * Singleton instance.
	 */
	private static Connection theConnection = null;
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = Logger.getLogger(Connection.class);
		
    /**
     * The driver to use for contacting the database.
     */
    public static String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    /**
     * The protocol to use for contacting the database.
     */
    public static String protocol = "jdbc:derby:";
    
    /**
     * The user name to use for contacting the database.
     */
    public static String username = "qosdisc";
        
    /**
     * The password to use for contacting the database.
     */
    public static String password = "qosdisc";
    
    /**
     * The name of the database file. If no full path is specified and the
     * embedded driver is used, the current path is used.
     */
    public static String name = "qosdisc.db";
    
    /**
     * The properties. Typically a copy of those in TestFrame.
     */
    private Properties properties;
    
    /**
     * The connection to the database.
     */
    private java.sql.Connection conn = null;
    
    /**
     * A statement for executing queries.
     */
    private Statement statement = null;
    

	/**
	 * Open the database connection.
	 * 
	 * @throws Exception
	 */
	public static void open(Properties props) throws Exception {

		// Instantiate Connection singleton if required.
		if (theConnection != null) {
			return;
		} 

		//list properties
//		for (Object prop : props.keySet()){
//			String str = (String)prop;
//			log.debug(prop + " - " + props.getProperty(str));
//		}
		
		// Create the singleton.
		Connection conn = new Connection(props);
		
		// Call the singleton.
		conn.innerOpen();
		
		// Only store the connection if no exceptions were thrown.
		theConnection = conn;
	}
	
	/**
	 * Close the database connection.
	 * 
	 * @throws Exception
	 */
	public static void close() throws Exception {
		
		// Check that singleton exists.
		if (theConnection == null) {
			log.error("The connection is already closed.");
			throw new Exception("The connection is already closed.");
		} 
		
		// Destroy singleton.
		theConnection.innerClose();
		theConnection = null;
	}
	
	/**
	 * Check whether the database connection is available.
	 * 
	 * @return true if the database can currently be used.
	 */
	public static boolean available() {
		
		return theConnection != null;
	}
	
	/**
	 * Execute a query returning a result set.
	 * 
	 * @param query The query to execute.
	 * @return The result set.
	 * @throws SQLException
	 */
	public static synchronized ResultSet executeQuery(String query) throws SQLException {
		
		// Pass it on to the statement.
		return theConnection.statement.executeQuery(query);
	}
	
	/**
	 * Execute a SQL statement returning nothing.
	 * 
	 * @param query The statement to execute. 
	 * @return true if successful.
	 * @throws SQLException
	 */
	public static synchronized boolean execute(String query) throws SQLException {
				
		// Pass it on to the statement.
		boolean rv = theConnection.statement.execute(query);
		theConnection.conn.commit();
		return rv;
	}
	
	/**
	 * Execute all statements contained in a file.
	 * 
	 * @param filename The name of the file to execute.
	 * @return true if successful.
	 * @throws Exception
	 */
	public static boolean executeFile(String filename) throws Exception {
		
		// Open the file.
		BufferedReader r = new BufferedReader(new FileReader(PropertySet.getPath()+File.separator+filename));
		
		// Get lines from file.
		String line;
		while((line = r.readLine()) != null) {

			// The query may be on multiple lines, collect lines until a semicolon is found.
			String query = "";
			do {

				query = query + line.trim();
				if(query.length()>0 && query.charAt(query.length()-1)==';')
					break;
			} while((line = r.readLine()) != null);
			
			// If we have reached the end of the file, leave now.
			if(query.length() == 0)
				break;
			
			// Remove semicolon
			if(query.charAt(query.length()-1)==';')
				query = query.substring(0,query.length()-1);
			
			// Execute the query.
			log.debug("Executing '"+query+"'");
			try {
				execute(query);
			} catch(SQLException ex) {
				log.warn(ex.getMessage());
			}
		}
		
		// Done, commit and return.
		r.close();
		theConnection.conn.commit();
		return true;
	}
	
	/**
	 * Create a prepared statement (for accessing CLOBs and BLOBs)
	 * 
	 * @param query The statement.
	 * @return The prepared statement.
	 * @throws SQLException
	 */
	public static PreparedStatement prepareStatement(String query) throws SQLException {        
		
		// Create the prepared statement.
		return theConnection.conn.prepareStatement(query);
	}
		
	/**
	 * Private singleton constructor.
	 */
	private Connection(Properties props) {
		
		// Store properties.
		properties = props;

		// Create dummy properties if no properties specified.
		if(properties == null)
			properties = new Properties();

		// Start server if required.
		if(properties.getProperty("startserver","true").equals("true")) {
	        try {
	    		DerbyServerStarter.start();
	    	} catch(Exception ex) { 
	    		ex.printStackTrace();
	    	}
		}

	}
	
	/**
	 * Open connection to database.
	 * 
	 * @throws Exception
	 */
	private void innerOpen() throws Exception {
		
		// Create database driver.
		String localDriver = properties.getProperty("db.driver",driver);
        Class.forName(localDriver).newInstance();
        log.debug("Loaded driver "+localDriver);

        // Setup connection properties.
        Properties connProps = new Properties();
        connProps.put("user", properties.getProperty("db.user",username));
        connProps.put("password", properties.getProperty("db.password",password));
        
        // Create database name.
    	String localName = properties.getProperty("db.name",name);
    	if(localName.indexOf('/')==-1 && localName.indexOf('\\')==-1 && 
    			localDriver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
    		String path = PropertySet.getPath();
    		localName = path + localName;
    	}

    	// Create connection.
    	String localProtocol = properties.getProperty("db.protocol",protocol);
    	String createStr = properties.getProperty("db.create");
    	boolean create = (createStr != null && createStr.toLowerCase().equals("true")) ? true: false;
    			 
    	if(localDriver.equals("org.apache.derby.jdbc.EmbeddedDriver") || create)
    		localName += ";create=true";
   	
    	log.debug("Connecting to "+localProtocol+localName);
        conn = DriverManager.getConnection(localProtocol + localName, connProps);
        conn.setAutoCommit(false);
        
        // Create statement.
        statement = conn.createStatement();
	}
	
	/**
	 * Close connection to database.
	 * 
	 * @throws Exception
	 */
	private void innerClose() throws Exception {
		
		// Close the statement.
		statement.close();
		
		// Close the connection.
		conn.commit();
		conn.close();
		
		// If this is embedded Derby, shut down now.
		if(properties.getProperty("db.driver",driver).equals("org.apache.derby.jdbc.EmbeddedDriver")) {
			
			// We expect an exception here.
			boolean gotException = false;
	        try {
	            DriverManager.getConnection("jdbc:derby:;shutdown=true");
	        } catch (SQLException se) {
	        	gotException = true;
	        }
	
	        // Check whether we got the exception.
	        if (!gotException ) {
	            log.error("Database did not shut down normally.");
	            throw new Exception("Database did not shut down normally.");
	        } else {
	            log.info("Database shut down normally.");
	        }
		}
	}
}
