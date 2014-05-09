/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.ErrorMsg;

public class PasswordDialog {
	public String login, password, lastClearPass;
	
	private static Class<?> pdc = null;
	private static Object pd = null;
	
	public static boolean DBE_global_login_valid() {
		return false;
		/*
		 * try {
		 * if (pdc==null || pd==null) {
		 * pdc = Class.forName("de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.PasswordDialog");
		 * pd = pdc.newInstance();
		 * }
		 * String passAndLogin = pd.toString();
		 * String[] pal = passAndLogin.split("§", 2);
		 * return (!passAndLogin.equals("null§null")) && pal[0].length()>0 && pal[1].length()>0;
		 * } catch(ClassNotFoundException cnfe) {
		 * ErrorMsg.addErrorMessage(cnfe);
		 * } catch (SecurityException e) {
		 * ErrorMsg.addErrorMessage(e);
		 * } catch (InstantiationException e) {
		 * ErrorMsg.addErrorMessage(e);
		 * } catch (IllegalAccessException e) {
		 * ErrorMsg.addErrorMessage(e);
		 * }
		 * return false;
		 */
	}
	
	public PasswordDialog() {
		this(true);
	}
	
	public PasswordDialog(boolean askForLogin) {
		if (login == null || login.length() == 0) {
			pdc = null;
			pd = null;
		}
		
		setDBE_global_login_data();
	}
	
	private void setDBE_global_login_data() {
		try {
			if (pdc == null || pd == null) {
				pdc = Class.forName("de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.PasswordDialog");
				pd = pdc.newInstance();
			}
			
			try {
				Method thisMethod = pdc.getDeclaredMethod("getIDandPassword", (Class[]) null);
				thisMethod.invoke(pd, (Object[]) null);
			} catch (NoSuchMethodException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (IllegalArgumentException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (InvocationTargetException e) {
				ErrorMsg.addErrorMessage(e);
			}
			
			String passAndLogin = pd.toString();
			String[] pal = passAndLogin.split("§", 2);
			login = pal[0];
			password = pal[1];
		} catch (ClassNotFoundException cnfe) {
			ErrorMsg.addErrorMessage(cnfe);
		} catch (SecurityException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (InstantiationException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (IllegalAccessException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	// modal dialog to get user ID and password
	static String[] ConnectOptionNames = { "Login", "Cancel" };
	static String ConnectTitle = "Input Login Information";
	
	public JPanel getLoginDataPanel() {
		JPanel connectionPanel;
		// Create the labels and text fields.
		JLabel userNameLabel = new JLabel("Login   :   ", SwingConstants.RIGHT);
		JTextField userNameField = new JTextField("");
		JLabel passwordLabel = new JLabel("Password:   ", SwingConstants.RIGHT);
		JTextField passwordField = new JPasswordField("");
		connectionPanel = new JPanel(false);
		connectionPanel.setLayout(new BoxLayout(connectionPanel,
							BoxLayout.X_AXIS));
		JPanel namePanel = new JPanel(false);
		namePanel.setLayout(new GridLayout(0, 1));
		namePanel.add(userNameLabel);
		namePanel.add(passwordLabel);
		JPanel fieldPanel = new JPanel(false);
		fieldPanel.setLayout(new GridLayout(0, 1));
		fieldPanel.add(userNameField);
		fieldPanel.add(passwordField);
		fieldPanel.add(new JLabel("<html><small>For public access,<br>leave login and password blank!"));
		connectionPanel.add(namePanel);
		connectionPanel.add(fieldPanel);
		
		connectionPanel.putClientProperty("login", userNameField);
		connectionPanel.putClientProperty("pass", passwordField);
		
		return connectionPanel;
	}
	
	/**
	 * @return
	 */
	public static String tryGetDBEISloginUrlParameters() {
		try {
			if (pdc == null || pd == null) {
				pdc = Class.forName("de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.PasswordDialog");
				pd = pdc.newInstance();
			}
			String passAndLogin = pd.toString();
			String[] pal = passAndLogin.split("§", 2);
			String l = pal[0];
			String p = pal[1];
			if ((!p.equals("null")) && (!l.equals("null")) && (l.length() > 0) && (p.length() > 0))
				return "?username=" + l + "&passvalue=" + p + "&reduced=true";
		} catch (ClassNotFoundException cnfe) {
			ErrorMsg.addErrorMessage(cnfe);
		} catch (SecurityException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (InstantiationException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (IllegalAccessException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return "";
	}
}