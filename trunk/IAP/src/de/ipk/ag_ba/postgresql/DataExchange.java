package de.ipk.ag_ba.postgresql;

// JDBC imports
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.jdbc.driver.OracleConnection;

public class DataExchange {
	
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
	
	public synchronized boolean isValidDomainUser(String user, String pass) throws Exception {
		boolean result = false;
		String groupName = "";
		
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
		
		ps = conn.prepareStatement("select pdw_secure.secure_util.getCurrGroupName() from dual");
		if (ps.execute()) {
			ResultSet r = ps.getResultSet();
			if (r.next())
				groupName = r.getString(1);
			r.close();
		}
		if (ps.getWarnings() != null) {
			ps.close();
			throw new Exception(ps.getWarnings().getMessage());
		} else {
			ps.close();
		}
		
		ps = conn.prepareStatement("select * from table(secure_util.getAllGroupsforCurrUser()");
		if (ps.execute()) {
			ResultSet r = ps.getResultSet();
			if (r.next())
				groupName = r.getString(1);
			r.close();
		}
		if (ps.getWarnings() != null) {
			ps.close();
			throw new Exception(ps.getWarnings().getMessage());
		} else {
			ps.close();
		}
		
		closeDB();
		
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
