/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.genophen;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.graffiti.editor.GravistoService;

public class HTMLErrorMsg {
	public static void printErrorMessageAndQuit(Connection conn, String heading, String text, String errmsg) {
		System.out.println("<table class=\"warning\"><tr><th class=\"warning\">" + heading + "</th</tr>");
		System.out.println("<tr><td>" + text + "</td></tr>");
		if (errmsg != null && errmsg.length() > 0) {
			System.out.println("<tr><td><hr>Java-Error Message:<br><code>" + errmsg + "</code><hr></td></tr>");
		}
		System.out.println("</table>");
		
		try {
			if (conn != null && !conn.isClosed()) {
				conn.rollback();
				conn.close();
			}
		} catch (SQLException e) {
			System.out.println("<table class=\"warning\"><tr><th class=\"warning\">Could not revert to a save database condition</th</tr>");
			System.out
								.println("<tr><td>The rollback command was not successfull. Please do not use this Webservice for now and inform the database adminstrators.<br>The last import could not succed successfully.</td></tr>");
			System.out.println("<tr><td>Java-Error Message:<br><code>" + e.getLocalizedMessage() + "</code></td></tr>");
		}
		if (GravistoService.getInstance().getMainFrame() != null) {
			int res = JOptionPane
								.showConfirmDialog(
													GravistoService.getInstance().getMainFrame(),
													"<html><h1>"
																		+ heading
																		+ "</h1><b>While executing a SQL command a problem was detected (see error message below).<p><p>Do you want to close the application?<p></b><p>Eror Message: "
																		+ text + "<p><br><code><p>SQL Error: " + errmsg, "SQL Error / Close appliaction?",
													JOptionPane.YES_NO_OPTION + JOptionPane.ERROR_MESSAGE);
			if (res == JOptionPane.YES_OPTION)
				System.exit(1);
		} else
			System.exit(1);
	}
	
	public static void printSuccess(String msg) {
		System.out.println("<table class=\"ok\"><tr><th class=\"ok\">Import successfull: " + msg + "</th</tr>");
		System.out.println("<tr><td>No errors where detected while importing the Excel File and its data.</td</tr>");
	}
}