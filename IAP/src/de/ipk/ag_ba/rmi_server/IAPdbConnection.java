/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Jul 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.sql.BLOB;

import org.ErrorMsg;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.healthmarketscience.rmiio.RemoteInputStream;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;

/**
 * @author klukas
 * 
 */
public class IAPdbConnection implements IAPdb {

	/*
	 * CREATE TABLE experiment2srcData ( experimentID integer not null,
	 * srcExperimentID integer not null, foreign key (experimentID) references
	 * experiment (experimentID), foreign key (srcExperimentID) references
	 * experiment (experimentID))
	 */

	OracleConnection conn;

	/**
	 * Connect to BIMI
	 */
	private synchronized void connectDB(String url, String dbUser, String dbPass) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver"); //$NON-NLS-1$
			conn = (OracleConnection) DriverManager.getConnection(url, dbUser, dbPass);
			conn.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
	}

	public static void setParameters(PreparedStatement ps, Object[] params) throws SQLException {
		if (params != null)
			for (int i = 0; i < params.length; i++) {
				Object par = params[i];
				if (par instanceof String)
					ps.setString(i + 1, (String) par);
				if (par instanceof Integer)
					ps.setInt(i + 1, ((Integer) par).intValue());
				if (par instanceof Date)
					ps.setTimestamp(i + 1, new Timestamp(((Date) par).getTime()));
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rmi_server.IAPdb#getExperiment(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Experiment getExperiment(String login, String pass, String experimentName) throws RemoteException {

		Experiment result = null;

		String sqlCommand = "" + "SELECT excelFile " + "FROM importfile NATURAL JOIN experiment "
				+ "WHERE experimentName=?";

		try {
			if (conn == null || conn.isClosed())
				connectDB(Secret.getDBurl(), Secret.getDBuser(), Secret.getDBpass());

			PreparedStatement ps = conn.prepareStatement(sqlCommand);
			setParameters(ps, new Object[] { experimentName });
			if (!ps.execute())
				throw new RemoteException("Could not execute SQL Query: " + sqlCommand);
			if (ps.getWarnings() == null) {
				OracleResultSet rs = (OracleResultSet) ps.getResultSet();

				if (rs.next()) {
					BLOB b = (BLOB) rs.getBlob("data");
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // new
					// DocumentBuilderFactoryImpl();
					InputStream bis = new BufferedInputStream(b.getBinaryStream());
					try {
						dbf.setNamespaceAware(false);
						DocumentBuilder db = dbf.newDocumentBuilder();
						InputSource is = new InputSource(bis);
						Document doc = db.parse(is);
						result = new Experiment(doc);
					} finally {
						bis.close();
					}
				}
				rs.close();
				ps.close();

				if (result == null)
					throw new RemoteException("Empty result set!");
			} else {
				ps.close();
				throw new RemoteException(ps.getWarnings().toString());
			}
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new RemoteException(e.getMessage(), e);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rmi_server.IAPdb#getExperimentFiles(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Collection<FileInfo> getExperimentFiles(String login, String pass, String experimentName)
			throws RemoteException {
		Collection<FileInfo> result = new ArrayList<FileInfo>();

		String sql = "SELECT imageFileID, fileName, md5, filesize " + "FROM imageFILE " + "NATURAL JOIN imageExperiment "
				+ "NATURAL JOIN experiment " + "NATURAL JOIN account " + "NATURAL JOIN user2group "
				+ "NATURAL JOIN usergroup " + "WHERE experimentName=? AND (userGroupID IN " + "	(SELECT usergroupid "
				+ "	 FROM (user2group " + "		NATURAL JOIN account) " + "	 WHERE dbuser=? AND dbpass=?))";

		try {
			if (conn == null || conn.isClosed())
				connectDB(Secret.getDBurl(), Secret.getDBuser(), Secret.getDBpass());

			PreparedStatement ps = conn.prepareStatement(sql);
			setParameters(ps, new Object[] { experimentName, login, pass });
			if (!ps.execute())
				throw new RemoteException("Could not execute SQL Query: " + sql);
			if (ps.getWarnings() == null) {
				OracleResultSet rs = (OracleResultSet) ps.getResultSet();
				boolean empty = true;
				while (rs.next()) {
					empty = false;
					FileInfo fi = new FileInfo();
					fi.rowID = rs.getInt("imageFileID");
					fi.filename = rs.getString("fileName");
					fi.md5 = rs.getString("md5");
					fi.fileSize = rs.getLong("fileSize");
					result.add(fi);
				}
				rs.close();
				ps.close();

				if (empty)
					throw new RemoteException("Empty result set!");
			} else {
				ps.close();
				throw new RemoteException(ps.getWarnings().toString());
			}
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new RemoteException(e.getMessage(), e);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rmi_server.IAPdb#getExperiments(java.lang.String, java.lang.String)
	 */
	@Override
	public Collection<ExperimentInfo> getExperiments(String login, String pass, boolean fromOwnerTruefromOthersFalse)
			throws RemoteException {

		Collection<ExperimentInfo> result = new ArrayList<ExperimentInfo>();

		String sqlW = "SELECT experimentName " + "FROM experiment " + "NATURAL JOIN account "
				+ "NATURAL JOIN user2group " + "NATURAL JOIN usergroup "
				+ "WHERE dbuser=? AND dbpass=? ORDER BY experimentName";

		String sqlRO = "SELECT experimentName " + "FROM experiment " + "NATURAL JOIN account "
				+ "NATURAL JOIN usergroup " + "WHERE userGroupID IN " + "	(SELECT usergroupid "
				+ "	 FROM user2group NATURAL JOIN account "
				+ "	 WHERE dbuser=? AND dbpass=?) AND dbuser!=? ORDER BY experimentName";

		String sql = fromOwnerTruefromOthersFalse ? sqlW : sqlRO;

		try {
			if (conn == null || conn.isClosed())
				connectDB(Secret.getDBurl(), Secret.getDBuser(), Secret.getDBpass());

			PreparedStatement ps = conn.prepareStatement(sql);
			setParameters(ps, new Object[] { login, pass });
			if (!ps.execute())
				throw new RemoteException("Could not execute SQL Query: " + sql);
			if (ps.getWarnings() == null) {
				OracleResultSet rs = (OracleResultSet) ps.getResultSet();
				boolean empty = true;
				while (rs.next()) {
					empty = false;
					ExperimentInfo ei = new ExperimentInfo();
					ei.experimentName = rs.getString("experimentName");
					ei.coordinator = rs.getString("coordinator");
					ei.importUser = rs.getString("dbuser");
					ei.userGroup = rs.getString("userGroupName");
					ei.experimentID = rs.getInt("experimentid");
					ei.remark = rs.getString("remark");
					ei.dateExperimentStart = rs.getDate("dateExperimentStart");
					ei.dateExperimentImport = rs.getDate("dateImport");
					ei.excelFileMd5 = rs.getInt("excelFileId") + "";
					result.add(ei);
				}
				rs.close();
				ps.close();

				if (empty)
					throw new RemoteException("Empty result set!");
			} else {
				ps.close();
				throw new RemoteException(ps.getWarnings().toString());
			}
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new RemoteException(e.getMessage(), e);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rmi_server.IAPdb#getImageFile(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public RemoteInputStream getImageFile(String login, String pass, String md5, boolean returnPreview)
			throws RemoteException {

		RemoteInputStream result = null;

		try {
			if (conn == null || conn.isClosed())
				connectDB(Secret.getDBurl(), Secret.getDBuser(), Secret.getDBpass());

			result = new DataFileHandling().downloadFile(conn, md5, returnPreview);
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new RemoteException(e.getMessage(), e);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rmi_server.IAPdb#storeExperiment(java.lang.String, java.lang.String,
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.
	 * Experiment)
	 */
	@Override
	public void storeExperiment(String login, String pass, String userGroup, Experiment experiment)
			throws RemoteException {

		try {
			if (conn == null || conn.isClosed())
				connectDB(Secret.getDBurl(), Secret.getDBuser(), Secret.getDBpass());

			int newExperimentID = -1;
			PreparedStatement ps = conn.prepareStatement("SELECT experimentID_seq.NextVal FROM DUAL");
			if (ps.execute()) {
				ResultSet r = ps.getResultSet();
				if (r.next()) {
					newExperimentID = r.getInt(1);
				} else {
					throw new RemoteException("Could not get next experimentID. (SQL Error 1)", null);
				}
				r.close();
			}
			ps.close();
			if (newExperimentID < 0)
				throw new RemoteException("Could not get next experimentID. (SQL Error 2)", null);

			getUserID(login);

			getGroupID(userGroup);

			String sql = "INSERT INTO experiment " + "(experimentID, dateExperimentStart, dateImport, "
					+ " remark, experimentName, coordinator, sequenceName, " + " excelFileID, userID, userGroupID, trash) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
			// setParameters(ps, new Object[] {
			// newExperimentID, experiment.getStartDate(),
			// experiment.getImportDate(),
			// experiment.getRemark(), experiment.getName(),
			// experiment.getCoordinator(), experiment.getSequence(),
			// todoUploadFirstIDhere, userID, groupID, 0 });
			if (ps.executeUpdate() != 1)
				throw new RemoteException("Could not correctly execute SQL Query: " + sql);
			if (ps.getWarnings() != null) {
				throw new RemoteException("Could not correctly execute SQL Query (with warnings): " + sql + ". Warnings: "
						+ ps.getWarnings().getMessage());
			}
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		} finally {
			try {
				conn.commit();
				conn.close();
			} catch (SQLException e) {
				throw new RemoteException(e.getMessage(), e);
			}
		}
	}

	private int getGroupID(String userGroupName) throws Exception {
		String sql = "SELECT userGroupID FROM USERGROUP WHERE userGroupName=?";
		return SQLgetSingleValue(sql, userGroupName);
	}

	private int getUserID(String dbUser) throws Exception {
		String sql = "SELECT userID FROM ACCOUNT WHERE dbUser=?";
		return SQLgetSingleValue(sql, dbUser);
	}

	private int SQLgetSingleValue(String sqlCommand, Object... params) throws Exception {
		try {
			if (conn == null || conn.isClosed())
				connectDB(Secret.getDBurl(), Secret.getDBuser(), Secret.getDBpass());

			PreparedStatement ps;
			ps = conn.prepareStatement(sqlCommand);

			setParameters(ps, params);

			ps.execute();
			if (ps.getWarnings() == null) {
				ResultSet rs = ps.getResultSet();
				if (rs.next()) {
					int res = rs.getInt(1);
					ps.close();
					return res;
				} else {
					ps.close();
					throw new RemoteException("Empty result. SQL command: " + sqlCommand + ". "
							+ ps.getWarnings().getLocalizedMessage());
				}
			} else {
				ps.close();
				throw new RemoteException("Error Executing SQL command: " + sqlCommand + ". "
						+ ps.getWarnings().getLocalizedMessage());
			}
		} catch (SQLException e) {
			throw new RemoteException("Error Executing SQL command: " + sqlCommand, e);
		}
	}

	public static void main(String args[]) {

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			IAPdbConnection obj = new IAPdbConnection();
			IAPdb stub = (IAPdb) UnicastRemoteObject.exportObject(obj);

			LocateRegistry.createRegistry(2010);
			Registry registry = LocateRegistry.getRegistry(2010);
			registry.rebind("IAPdb", stub);
			System.out.println("IAPdb bound in registry");
		} catch (Exception e) {
			System.out.println("IAPdb exception: " + e.getMessage());
			e.printStackTrace();
		} finally {
		}
	}
}
