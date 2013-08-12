package de.ipk.ag_ba.postgresql;

// JDBC imports
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import oracle.jdbc.driver.OracleConnection;

public class DatabaseDataExchange {
	
	OracleConnection conn;
	
	/**
	 * Connect to BIMI
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public synchronized void connectDB() throws ClassNotFoundException, SQLException {
		String url;
		// url = "@bimi.ipk-gatersleben.de"; //$NON-NLS-1$
		url = "@oradb.ipk-gatersleben.de"; //$NON-NLS-1$
		// url="@194.94.136.133";
		url = "jdbc:oracle:thin:" + url + ":1521:genophen"; //$NON-NLS-1$ //$NON-NLS-2$
		Class.forName("oracle.jdbc.driver.OracleDriver"); //$NON-NLS-1$
		conn = (OracleConnection) DriverManager.getConnection(url,
					"klukas", "imoagnw03");
		conn.setAutoCommit(false);
	}
	
	public synchronized String[] getUserGroups(String user, String pass) throws Exception {
		boolean result = false;
		
		if (!isConnected())
			connectDB();
		
		PreparedStatement ps;
		ps = conn.prepareStatement("select pdw_secure.check_password(?,?) from dual");
		ps.setString(1, user);
		ps.setString(2, pass);
		if (ps.execute()) {
			ResultSet r = ps.getResultSet();
			if (r.next())
				result = r.getInt(1) == 1;
			r.close();
		}
		if (ps.getWarnings() != null) {
			ps.close();
			throw new Exception(ps.getWarnings().getMessage());
		} else {
			ps.close();
		}
		
		if (result == false)
			return new String[] {};
		
		ArrayList<String> groups = new ArrayList<String>();
		
		ps = conn.prepareStatement("select * from table(secure_util.getallgroups)");// select * from table(secure_util.getallgroupsforcurruser)");
		if (ps.execute()) {
			ResultSet r = ps.getResultSet();
			while (r.next())
				groups.add(r.getString(1));
			r.close();
		}
		if (ps.getWarnings() != null) {
			ps.close();
			throw new Exception(ps.getWarnings().getMessage());
		} else {
			ps.close();
		}
		
		disconnectDB();
		
		return groups.toArray(new String[] {});
	}
	
	public synchronized boolean isValidDomainUser(String user, String pass) throws Exception {
		boolean result = false;
		
		if (!isConnected())
			connectDB();
		
		PreparedStatement ps;
		ps = conn.prepareStatement("select pdw_secure.check_password(?,?) from dual");
		ps.setString(1, user);
		ps.setString(2, pass);
		if (ps.execute()) {
			ResultSet r = ps.getResultSet();
			if (r.next())
				result = r.getInt(1) == 1;
			r.close();
		}
		if (ps.getWarnings() != null) {
			ps.close();
			throw new Exception(ps.getWarnings().getMessage());
		} else {
			ps.close();
		}
		
		disconnectDB();
		
		return result;
	}
	
	/**
	 * Disconnect from the BIMI database.
	 * 
	 * @throws Exception
	 */
	public synchronized void disconnectDB() throws Exception {
		try {
			if (conn == null || conn.isClosed())
				return;
			conn.close();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public synchronized void closeDB() throws SQLException {
		conn.commit();
		conn.close();
	}
	
	/**
	 * Check if a connection to the database has been established.
	 * 
	 * @return True, if a connection is open; False, if not.
	 * @throws Exception
	 */
	public synchronized boolean isConnected() throws Exception {
		try {
			if (conn == null)
				return false;
			return !conn.isClosed();
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public synchronized void closeResultSet(ResultSet rset) throws SQLException {
		rset.close();
	}
}
