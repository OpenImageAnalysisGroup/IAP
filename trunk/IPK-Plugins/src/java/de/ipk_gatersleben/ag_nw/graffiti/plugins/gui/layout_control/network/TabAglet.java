/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 * (c) 2012 Image Analysis Group
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ErrorMsg;
import org.SettingsHelperDefaultIsFalse;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.session.Session;

import de.ipk_gatersleben.ag_nw.graffiti.services.network.BroadCastService;

public class TabAglet
		extends InspectorTab implements Runnable {
	
	public static String newline = System.getProperty("line.separator");
	
	private static TabAglet instance;
	
	private static final long serialVersionUID = 1L;
	
	public static final String ENABLE_BROADCAST_SETTING = "udp_broadcast";
	
	private Timer networkBroadCast;
	private final BroadCastService broadCastService = new BroadCastService(9900, 9910, 512);
	private BroadCastTask broadCastTask;
	
	private final JComponent runService =
			new SettingsHelperDefaultIsFalse().getBooleanSettingsEditor(
					"Allow Network Broadcast (udp-port " +
							broadCastService.getStartPort() + "-" + broadCastService.getEndPort() + ")",
					ENABLE_BROADCAST_SETTING, null, null);
	
	private final JLabel myStatusLabel = new JLabel();
	public String status = "not initialized";
	private final JTextArea myDataIn = new JTextArea();
	private final JTextField inputField = new JTextField();
	private final JButton sendButton = new JButton("Send");
	
	private javax.swing.Timer updateNetStatus;
	
	private void initComponents() {
		double border = 2;
		double[][] size =
		{
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, 30, border }
		}; // Rows
		this.setLayout(new TableLayout(size));
		
		this.add(runService, "1,1");
		this.add(myStatusLabel, "1,2");
		this.add(myDataIn, "1,3");
		this.add(
				TableLayout.
						getSplit(
								inputField, sendButton,
								TableLayoutConstants.FILL, 80), "1,4");
		sendButton.setMnemonic('S');
		sendButton.setEnabled(false);
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = inputField.getText();
				inputField.setText("");
				inputField.requestFocusInWindow();
				try {
					broadCastTask.addMessageToBeSent(msg);
				} catch (Exception e1) {
					ErrorMsg.addErrorMessage(e1);
				}
				updateNetworkStatus();
			}
		});
		
		myDataIn.setBackground(new Color(230, 230, 255));
		
		updateNetStatus = new javax.swing.Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateNetworkStatus();
			}
		});
		updateNetStatus.start();
		
		this.revalidate();
		inputField.addActionListener((e) -> {
			sendButton.doClick();
		});
	}
	
	/**
	 * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabAglet() {
		super();
		this.title = "Chat";
		TabAglet.instance = this;
		initComponents();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.event.AttributeListener#postAttributeAdded(org.graffiti.event.AttributeEvent)
	 */
	public void postAttributeAdded(AttributeEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.event.AttributeListener#postAttributeChanged(org.graffiti.event.AttributeEvent)
	 */
	public void postAttributeChanged(AttributeEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.event.AttributeListener#postAttributeRemoved(org.graffiti.event.AttributeEvent)
	 */
	public void postAttributeRemoved(AttributeEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.event.AttributeListener#preAttributeAdded(org.graffiti.event.AttributeEvent)
	 */
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.event.AttributeListener#preAttributeChanged(org.graffiti.event.AttributeEvent)
	 */
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.event.AttributeListener#preAttributeRemoved(org.graffiti.event.AttributeEvent)
	 */
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.event.TransactionListener#transactionFinished(org.graffiti.event.TransactionEvent)
	 */
	public void transactionFinished(TransactionEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.event.TransactionListener#transactionStarted(org.graffiti.event.TransactionEvent)
	 */
	public void transactionStarted(TransactionEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (broadCastTask == null)
			return;
		ArrayList<?> inMessages = broadCastTask.getInMessages();
		for (Iterator<?> it = inMessages.iterator(); it.hasNext();) {
			String curText = myDataIn.getText();
			String msg;
			try {
				msg = new String((byte[]) it.next(), "UTF-8");
				myDataIn.setText(msg + newline + curText);
				MainFrame.showMessage("<html><b>Incoming Broadcast Chat Message:</b> " + msg, MessageType.PERMANENT_INFO);
			} catch (UnsupportedEncodingException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		updateNetworkStatus();
	}
	
	private void updateNetworkStatus() {
		boolean on = new SettingsHelperDefaultIsFalse().isEnabled(ENABLE_BROADCAST_SETTING);
		if (on && !enabled)
			enableTimers();
		else
			if (!on && enabled)
				disableTimers();
		
		if (broadCastTask == null) {
			myStatusLabel.setText("<html><small>Network functions are disabled");
			status = "disabled";
		} else {
			List<InetAddress> hosts = broadCastTask.getActiveHosts();
			String hostList = "";
			for (Iterator<InetAddress> it = hosts.iterator(); it.hasNext();) {
				String name = it.next().toString();
				if (name.startsWith("/"))
					name = name.substring(1);
				hostList += ", " + name;
			}
			if (hostList.length() < 2)
				hostList = "no hosts found";
			else
				hostList = hostList.substring(2);
			String netW;
			if (on)
				netW = "Broadcast enabled";
			else
				netW = "Broadcast disabled";
			status = hostList;
			myStatusLabel.setText("<html><small>" + netW + " (in/out/other in, listener-port): "
					+ broadCastService.getInCount() + "/"
					+ broadCastService.getOutCount() + "/"
					+ broadCastService.getOtherInCount() +
					", " + broadCastService.getBindPort() +
					"<br>Active Hosts (" + hosts.size() + "): " + hostList);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	public void sessionChanged(Session s) {
		//
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
		//
		
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
	
	private boolean enabled = false;
	
	public synchronized void enableTimers() {
		if (!enabled) {
			enabled = true;
			networkBroadCast = new Timer("Aglet Network Broadcast");
			broadCastTask = new BroadCastTask(broadCastService, this);
			networkBroadCast.schedule(broadCastTask, 0, BroadCastTask.timeDefaultBeatTime);
			if (sendButton != null)
				sendButton.setEnabled(true);
		}
	}
	
	public synchronized void disableTimers() {
		if (enabled) {
			enabled = false;
			if (broadCastTask != null)
				broadCastTask.cancel();
			if (networkBroadCast != null)
				networkBroadCast.cancel();
			if (sendButton != null)
				sendButton.setEnabled(false);
		}
	}
	
	public static TabAglet getInstance() {
		return instance;
	}
	
	public BroadCastTask getBroadCastTask() {
		return broadCastTask;
	}
}
