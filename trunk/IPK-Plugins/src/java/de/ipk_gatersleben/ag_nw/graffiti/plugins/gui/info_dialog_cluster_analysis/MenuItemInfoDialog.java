/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_cluster_analysis;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.gui.GraffitiContainer;
import org.graffiti.plugin.gui.GraffitiMenu;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * Shows the info dialog for PatternGraffiti
 * 
 * @author Christian Klukas
 */
public class MenuItemInfoDialog
					extends GraffitiMenu
					implements GraffitiContainer {
	// to avoid collisions let ID be package name + menu + name of the menu
	
	private static final long serialVersionUID = 1L;
	
	public String errorMessagesMenuTitle = "Error Messages";
	
	/**
	 * DOCUMENT ME!
	 */
	public static final String ID =
						"de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.MenuItemInfoDialog";
	
	/**
	 * Create Menu.
	 */
	public MenuItemInfoDialog() {
		super();
		setName("ipk.help");
		setText("Help");
		setMnemonic('H');
		setEnabled(true);
		final JMenuItem info = new JMenuItem("About " + DBEgravistoHelper.CLUSTER_ANALYSIS_NAME);
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/dbe_logo_16x16.png"));
		info.setIcon(icon);
		
		info.setMnemonic('I');
		info.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				Runtime r = Runtime.getRuntime();
				r.gc();
				ClassLoader cl = this.getClass().getClassLoader();
				String path = this.getClass().getPackage().getName().replace('.', '/');
				ImageIcon icon = new ImageIcon(cl.getResource(path + "/pattern_graffiti_logo.png"));
				int divisor = 1024;
				JOptionPane.showOptionDialog(
									GravistoService.getInstance().getMainFrame(),
									"<html>" +
														"<h2>" + DBEgravistoHelper.CLUSTER_ANALYSIS_VERSION + "</h2>" +
														"<p>" + DBEgravistoHelper.CLUSTER_ANALYSIS_NAME + " (c) 2003-2007 IPK-Gatersleben" +
														"<br>" + "- based on Gravisto (c) 2001-2004 University of Passau -" +
														"<p><p>http://nwg.bic-gh.de<p>http://www.gravisto.org<p>"
														+ "<br>- developed by Christian Klukas from the Network Analysis Group<p><br>"
														+ "<small><small><small><font color=\"gray\">System-Info: " +
														SystemAnalysis.getNumberOfCPUs() + " CPU" + (SystemAnalysis.getNumberOfCPUs() > 1 ? "s, " : ", ") +
														// Java_1_5_compatibility.getJavaVersion()+
											// ",<br>"+
											"used/free/max memory: " +
														((r.totalMemory() / divisor / divisor) - (r.freeMemory() / divisor / divisor)) + "" +
														"/" + (r.freeMemory() / divisor / divisor) + "/"
														+ (r.maxMemory() / divisor / divisor) + " MB"
														+ "</font></small></small></small>",
									// PatternGraffitiHelper.getPluginStatusText()
						"Info about " + DBEgravistoHelper.CLUSTER_ANALYSIS_NAME,
									JOptionPane.DEFAULT_OPTION,
									JOptionPane.INFORMATION_MESSAGE,
									icon, null, null);
			}
		});
		
		JMenuItem helpText = new JMenuItem("Tutorial");
		helpText.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				JFrame w = new JFrame("Help / Tutorial");
				double border = 0;
				double[][] size =
				{ { border, TableLayoutConstants.FILL, border }, // Columns
						{ border, TableLayoutConstants.FILL, border }
				}; // Rows
				w.setLayout(new TableLayout(size));
				w.add(
									getWebPane("http://pgrc-16.ipk-gatersleben.de/~klukas/expdat/gravisto/IPK-Cluster%20Analysis%20-%20Help.htm"),
									"1,1");
				w.setSize(640, 480);
				w.setVisible(true);
			}
		});
		
		JMenuItem jMenuItemReleaseInfo = new JMenuItem("Release Info");
		jMenuItemReleaseInfo.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				boolean ext = true;
				if (ext) {
					AttributeHelper.showInBrowser("http://vanted.ipk-gatersleben.de/index.php?file=doc139.html");
					return;
				}
				JFrame w = new JFrame("Release Information");
				double border = 0;
				double[][] size =
				{ { border, TableLayoutConstants.FILL, border }, // Columns
						{ border, TableLayoutConstants.FILL, border }
				}; // Rows
				w.setLayout(new TableLayout(size));
				// http://vanted/index.php?file=doc139.html
				// http://pgrc-16.ipk-gatersleben.de/~klukas/expdat/gravisto/release.html
				w.add(
									getWebPane("http://vanted.ipk-gatersleben.de/index.php?file=doc139.html"),
									"1,1");
				w.setSize(640, 480);
				w.setVisible(true);
			}
		});
		
		final JMenuItem error = new JMenuItem(errorMessagesMenuTitle);
		error.setMnemonic('E');
		error.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] errorMsgs = ErrorMsg.getErrorMessages();
				String err;
				if (errorMsgs.length > 0) {
					if (errorMsgs.length == 1)
						err = "<html><body><h2>" + errorMsgs.length + " problem detected</h2><hr>";
					else
						err = "<html><body><h2>" + errorMsgs.length + " problems detected</h2><hr>";
					for (int i = 0; i < errorMsgs.length; i++) {
						if (errorMsgs[i] != null)
							err = err + errorMsgs[i] + "<p>";
					}
					err += "</body></html>";
				} else {
					err = "<html><body><h2>No error messages available.</h2></body></html>";
				}
				ErrorMsg.clearErrorMessages();
				// GraffitiSingleton.getInstance().getMainFrame().showMessageDialog(err);
				JEditorPane errMsg = new JEditorPane("text/html", err);
				errMsg.setEditable(false);
				errMsg.setAutoscrolls(false);
				
				final JScrollPane scp = new JScrollPane(errMsg);
				scp.setMaximumSize(new Dimension(800, 600));
				scp.setPreferredSize(new Dimension(800, 600));
				Timer t = new Timer(10, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						scp.getVerticalScrollBar().setValue(scp.getVerticalScrollBar().getMinimum());
						scp.getHorizontalScrollBar().setValue(scp.getHorizontalScrollBar().getMinimum());
					}
				});
				t.setRepeats(false);
				t.start();
				JOptionPane.showMessageDialog(GravistoService.getInstance().getMainFrame(),
									scp, "Error Messages", JOptionPane.ERROR_MESSAGE);
				
			}
		});
		
		insert(error, 1);
		insert(helpText, 2);
		// insert(jMenuItemReleaseInfo, 3);
		insert(info, 2);
		
		// mark Help Menu and Error menu item in case error messages are available
		Timer t = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] errorMsg = ErrorMsg.getErrorMessages();
				if (errorMsg != null && errorMsg.length > 0) {
					// error.setFont(new Font(error.getFont().getName(), Font.BOLD, error.getFont().getSize()));
					// setFont(new Font(error.getFont().getName(), Font.BOLD, error.getFont().getSize()));
					error.setForeground(new Color(170, 0, 0));
					setForeground(new Color(170, 0, 0));
					error.setText(errorMessagesMenuTitle + " (" + errorMsg.length + ")");
					error.setEnabled(true);
				} else {
					// error.setFont(info.getFont());
					// setFont(info.getFont());
					error.setForeground(info.getForeground());
					setForeground(info.getForeground());
					error.setText(errorMessagesMenuTitle);
					error.setEnabled(false);
				}
			}
		});
		t.start();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getId() {
		return ID;
	}
	
	/**
	 * @return A pane with info about the program
	 */
	private Component getWebPane(String urlText) {
		// String html;
		JEditorPane editorPane = null;
		try {
			String url = urlText;
			editorPane = new JEditorPane(
								new URL(url));
		} catch (Exception e) {
			editorPane = new JEditorPane(
								"",
								"" +
													"Could not show Help-Window!"
													+ "Error-Message: " + e.getLocalizedMessage() + "");
			ErrorMsg.addErrorMessage(e);
		}
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(createHyperLinkListener(editorPane));
		return new JScrollPane(editorPane);
	}
	
	/**
	 * @param editorPane
	 * @return
	 */
	public HyperlinkListener createHyperLinkListener(final JEditorPane editorPane) {
		return new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (e instanceof HTMLFrameHyperlinkEvent) {
						((HTMLDocument) editorPane.getDocument()).processHTMLFrameHyperlinkEvent(
											(HTMLFrameHyperlinkEvent) e);
					} else {
						// System.out.println(e.getURL());
						if (e.getURL().toString().indexOf("#close") > 0) { //$NON-NLS-1$
							closeHelp();
						} else {
							try {
								editorPane.setPage(e.getURL());
							} catch (IOException ioe) {
								ErrorMsg.addErrorMessage(
													"IO Exception in HTML hyperlink event:" +
																		ioe.getLocalizedMessage());
							}
						}
					}
				}
			}
			
			private void closeHelp() {
				// TODO
			}
		};
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
