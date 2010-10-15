/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Jul 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server;

import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.sql.BFILE;
import oracle.sql.BLOB;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.RemoteStreamMonitor;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

/**
 * @author klukas
 * 
 */
public class DataFileHandling {

	public synchronized RemoteInputStream downloadFile(OracleConnection conn, String md5, boolean returnPreview)
			throws RemoteException {
		String sqlCommand = "SELECT length(data) as len, data, databfile, filesize " + "FROM imageFile " + "WHERE md5=?";
		if (returnPreview)
			sqlCommand = "SELECT length(previewImage) as len, previewImage " + "FROM imageFile " + "WHERE md5=?";
		RemoteInputStream result = null;
		try {
			if (conn == null || conn.isClosed())
				return null;
			PreparedStatement ps = conn.prepareStatement(sqlCommand);
			IAPdbConnection.setParameters(ps, new Object[] { md5 });
			if (!ps.execute())
				throw new RemoteException("Could not execute SQL Query:<p>" + sqlCommand);
			if (ps.getWarnings() == null) {
				OracleResultSet rs = (OracleResultSet) ps.getResultSet();
				ps.close();

				if (rs.next()) {
					int maxBytes = rs.getInt("len");
					result = readBlob(rs, maxBytes);
				}
				rs.close();
			} else {
				ps.close();
				throw new RemoteException(ps.getWarnings().toString());
			}
		} catch (SQLException e) {
			throw new RemoteException("SQL Error", e);
		}
		return result;
	}

	private RemoteInputStream readBlob(OracleResultSet rs, int maxBytes) throws SQLException {
		final BLOB b = (BLOB) rs.getBlob("data");
		return new SimpleRemoteInputStream(b.getBinaryStream(), new RemoteStreamMonitor<RemoteInputStreamServer>() {
			@Override
			public void localBytesSkipped(RemoteInputStreamServer stream, long numBytes) {
			}

			@Override
			public void localBytesMoved(RemoteInputStreamServer stream, int numBytes) {
			}

			@Override
			public void failure(RemoteInputStreamServer stream, Exception e) {
				try {
					b.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void closed(RemoteInputStreamServer stream, boolean clean) {
				try {
					b.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void bytesSkipped(RemoteInputStreamServer stream, long numBytes, boolean isReattempt) {
			}

			@Override
			public void bytesMoved(RemoteInputStreamServer stream, int numBytes, boolean isReattempt) {
			}
		});
	}

	private RemoteInputStream readBFile(OracleResultSet rs, int maxBytes) throws SQLException {
		final BFILE bfile = rs.getBFILE("databfile");
		bfile.openFile();
		return new SimpleRemoteInputStream(bfile.getBinaryStream(), new RemoteStreamMonitor<RemoteInputStreamServer>() {
			@Override
			public void localBytesSkipped(RemoteInputStreamServer stream, long numBytes) {
			}

			@Override
			public void localBytesMoved(RemoteInputStreamServer stream, int numBytes) {
			}

			@Override
			public void failure(RemoteInputStreamServer stream, Exception e) {
				try {
					bfile.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void closed(RemoteInputStreamServer stream, boolean clean) {
				try {
					bfile.closeFile();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void bytesSkipped(RemoteInputStreamServer stream, long numBytes, boolean isReattempt) {
			}

			@Override
			public void bytesMoved(RemoteInputStreamServer stream, int numBytes, boolean isReattempt) {
			}
		});
	}
}
