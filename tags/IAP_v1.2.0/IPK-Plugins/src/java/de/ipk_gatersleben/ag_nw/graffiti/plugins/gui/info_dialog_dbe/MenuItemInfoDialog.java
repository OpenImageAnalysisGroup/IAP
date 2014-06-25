/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.Java_1_5_compatibility;
import org.Release;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemInfo;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.actions.ClipboardService;
import org.graffiti.plugin.gui.GraffitiContainer;
import org.graffiti.plugin.gui.GraffitiMenu;

import apple.dts.samplecode.osxadapter.OSXAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.plugin_info.PluginInfoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.WorkflowHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.PatchedHTMLEditorKit;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

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
	
	private static String extractText = "[Save license text to file and open with system editor]";
	
	public String errorMessagesMenuTitle = "Error Messages";
	
	/**
	 * DOCUMENT ME!
	 */
	public static final String ID =
			"de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.MenuItemInfoDialog";
	
	private int lastErrorCount = 0;
	
	final JMenuItem info;
	
	/**
	 * Create Menu.
	 */
	public MenuItemInfoDialog() {
		super();
		setName("ipk.help");
		setText("Help");
		setMnemonic('H');
		setEnabled(true);
		
		info = new JMenuItem("About " + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT);
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/dbe_logo_16x16.png"));
		info.setIcon(icon);
		KeyStroke f1 = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		info.setAccelerator(f1);
		info.setMnemonic('I');
		info.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Runtime r = Runtime.getRuntime();
				r.gc();
				ClassLoader cl = this.getClass().getClassLoader();
				String path = this.getClass().getPackage().getName().replace('.', '/');
				ImageIcon icon = new ImageIcon(cl.getResource(path + "/pattern_graffiti_logo.png"));
				int divisor = 1024;
				
				String otherParties, otherParties2, copyR;
				if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH)) {
					// otherParties = "The node overlap removal algorithm is developed by Dr. Tim Dwyer.";
				} else {
					// otherParties = "The node overlap removal algorithm is provided by Dr. Tim Dwyer.";
				}
				otherParties = "";
				if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
					otherParties2 =
							"This application does not contain/distribute information from the KEGG website, <br>" +
									"but it contains functionality for the online-access to the KEGG (Web)Services.<br>" +
									"We would like to thank the developers of KEGG, Professor Minoru Kanehisa and<br>" +
									"his associates, for their great work on this comprehensive information resource.<br><br>";
				} else {
					otherParties2 = "";
				}
				
				if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
					copyR = "&copy; 2003-2014 Leibniz Institute of Plant Genetics and Crop Plant Research (IPK)";
				else
					copyR = "(c) 2003-2014 IPK-Gatersleben";
				
				int num = 0;
				num = MainFrame.getInstance().getPluginManager().getPluginEntries().size();
				
				int rrr = JOptionPane
						.showOptionDialog(
								GravistoService.getInstance().getMainFrame(),
								"<html>" +
										"<h2>"
										+ DBEgravistoHelper.DBE_GRAVISTO_VERSION
										+ " <small>"
										+ DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE_SUBVERSION
										+ "</small></h2>"
										+
										""
										+ DBEgravistoHelper.DBE_GRAVISTO_NAME.replaceAll("<br>", "")
										+
										"<br><br><b>Copyrights</b><br><br>"
										+ copyR
										+
										"<br>"
										+ "- based on Gravisto (c) 2001-2004 University of Passau -<br>"
										+
										"Graph Visualization Toolkit (Gravisto) - http://gravisto.fim.uni-passau.de/"
										+
										"<p><p>To view copyright information about the used libraries and web services click"
										+
										"<br>the \"Foreign Copyrights\" button."
										+
										"<p><p>"
										+
										(DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME.length() > 0 ? DBEgravistoHelper.DBE_GRAVISTO_NAME
												.replaceAll("<br>",
														"")
												+
												"<br>is part of "
												+
												DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME
												+ " (c) 2010-2011 Group Image Analysis,<br>IPK-Gatersleben - " +
												"design and implementation by Christian Klukas, head of group<br><br>"
												: "")
										+
										"<b>Development</b>"
										+ "<br><br>"
										+ "IAP-Data-Navigator is one of the core components of the Integrated Analysis<br>" +
										"Platform (IAP). IAP is developed since May 2010 by the research group image<br>" +
										"analysis, lead by C. Klukas.<br><br>"
										+
										"VANTED has been designed and implemented mainly by Christian Klukas in the<br>"
										+
										"frame of his PhD thesis. The PhD work was performed under the supervision<br>"
										+
										"of Falk Schreiber (lead of group plant bioinformatics).<br><br>"
										+
										"Parts of the application have been implementated by other members<br>"
										+
										"of the IPK working group plant bioinformatics.<br>"
										+
										"<br>Initial Gravisto graph editor library development at University of Passau.<br>"
										+
										otherParties +
										(otherParties2.length() > 0 ?
												"<br><br><b>Information</b><br><br>" : "") +
										otherParties2 +
										"<small><small><small><br><font color=\"gray\"><br>System-Info: " +
										Java_1_5_compatibility.getJavaVersion() + ", " +
										SystemAnalysis.getNumberOfCPUs() + " CPU" + (SystemAnalysis.getNumberOfCPUs() > 1 ? "s, " : ", ")
										+ "<br>used/free/max memory: " +
										((r.totalMemory() / divisor / divisor) - (r.freeMemory() / divisor / divisor)) + "" +
										"/" + (r.freeMemory() / divisor / divisor) + "/"
										+ (r.maxMemory() / divisor / divisor) + " MB"
										+ "</font></small></small></small>",
								// PatternGraffitiHelper.getPluginStatusText()
								"Info about " + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.INFORMATION_MESSAGE,
								icon, new String[] { "OK", "Foreign Copyrights", "Loaded Plugins (" + num + ")" }, null);
				if (rrr == JOptionPane.CANCEL_OPTION)
				{
					MainFrame.showMessageDialogWithScrollBars2(
							PluginInfoHelper.pretifyPluginList(MainFrame.getInstance().getPluginManager().getPluginEntries()),
							"Loaded Plugins (" + num + ")");
					actionPerformed(arg0);
				} else
					if (rrr == JOptionPane.NO_OPTION)
					{
						boolean finished = false;
						while (!finished) {
							final JEditorPane jep;
							jep = new JEditorPane();
							jep.setEditorKitForContentType("text/html", new PatchedHTMLEditorKit());
							jep.setContentType("text/html");
							jep.setText("<h3><font face=\"Sans,Tohama,Arial\">" + DBEgravistoHelper.DBE_GRAVISTO_VERSION + " is created<br>" +
									"with the help of the following libraries:</font></h3><font face=\"Sans,Tohama,Arial\">" +
									"<ul>" +
									getLibsText() +
									"</ul>Click <a href=\"http://save\">here</a> to save license text(s).");
							jep.setEditable(false);
							jep.setBackground(new JPanel().getBackground());
							jep.addHyperlinkListener(new HyperlinkListener() {
								@Override
								public void hyperlinkUpdate(HyperlinkEvent e) {
									if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
										jep.setCursor(new Cursor(Cursor.HAND_CURSOR));
									if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
										jep.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
									if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
										String url = e.getURL().toString();
										if (url.equals("http://save"))
											saveFiles();
										else
											AttributeHelper.showInBrowser(url);
									}
									// System.out.println(e.getDescription()+": "+e.getURL().toString());
								}
							});
							final JScrollPane sp = new JScrollPane(jep);
							sp.setPreferredSize(new Dimension(800, 600));
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									sp.getVerticalScrollBar().setValue(0);
								}
							});
							GUIhelper.showMessageDialog(
									sp,
									"Info about " + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT,
									new String[] { "OK" });
							finished = true;
						}
						actionPerformed(arg0);
					}
				/*
				 * MyInfoDialog id = new MyInfoDialog(MainFrame.getInstance());
				 * id.setLocationRelativeTo(MainFrame.getInstance());
				 * id.setVisible(true);
				 */
			}
		});
		
		if (SystemInfo.isMac() && !ReleaseInfo.isRunningAsApplet()) {
			try {
				OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("showAbout", (Class[]) null));
				OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("doQuit", (Class[]) null));
				OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("showPreferences", (Class[]) null));
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		
		// JMenuItem jMenuItemJavaHelp = new JMenuItem("Help Contents");
		//
		// if (ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp)) {
		// try {
		// String helpHS = "main.hs";
		// HelpSet hs;
		// URL hsURL = HelpSet.findHelpSet(cl, helpHS);
		// if (hsURL != null) {
		// hs = new HelpSet(this.getClass().getClassLoader(), hsURL);
		// HelpBroker hb = hs.createHelpBroker();
		// hb.setCurrentID("main");
		// jMenuItemJavaHelp.addActionListener(new CSH.DisplayHelpFromSource(hb));
		// } else {
		// jMenuItemJavaHelp.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent arg0) {
		// MainFrame.showMessageDialog(
		// "Help Function not fully accessible.", "Missing Help Topic"
		// );
		// }
		// });
		// }
		// } catch (Exception ee) {
		// ErrorMsg.addErrorMessage(ee);
		// return;
		// }
		// }
		JMenuItem jMenuItemJavaHelpPDF = new JMenuItem("Help Contents (PDF)");
		jMenuItemJavaHelpPDF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File temp;
				try {
					temp = File.createTempFile("documentation", ".pdf");
					temp.deleteOnExit();
					
					ClassLoader cl = this.getClass().getClassLoader();
					
					this.getClass().getPackage().getName().replace('.', '/');
					try {
						FileOutputStream out = new FileOutputStream(temp);
						InputStream inpS = cl.getResourceAsStream("doc.pdf");
						InputStream in = inpS;
						int b;
						while ((b = inpS.read()) != -1) {
							out.write(b);
						}
						in.close();
						out.close();
						MainFrame.showMessage("PDF document created as temporary file (" + temp.getAbsolutePath() + "), open file...", MessageType.INFO);
						/*
						 * MainFrame.showMessageDialog("<html><h3>The application documentation was saved into a temporary PDF file.</h3>" +
						 * "An important note for Windows users: Please close the PDF viewer before closing this application,<br>" +
						 * "otherwise the temporary file will be not be deleted upon closing of the application!",
						 * "Open PDF Documentation");
						 */
						AttributeHelper.showInBrowser(temp.getAbsolutePath());
					} catch (FileNotFoundException err) {
						MainFrame.showMessageDialog("Error: " + err.getLocalizedMessage(), "Could not save file");
						ErrorMsg.addErrorMessage(err);
					} catch (IOException err) {
						MainFrame.showMessageDialog("Error: " + err.getLocalizedMessage(), "Could not save file");
						ErrorMsg.addErrorMessage(err);
					}
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			}
		});
		
		JMenuItem helpText = new JMenuItem("Video Tutorials");
		helpText.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String url = "http://iap.ipk-gatersleben.de";
				// "http://pgrc-16.ipk-gatersleben.de/~klukas/expdat/gravisto/demo/demos.html";
				AttributeHelper.showInBrowser(url);
				// JFrame w = new JFrame("Help / Tutorial");
				// double border = 0;
				// double[][] size =
				// { { border, TableLayoutConstants.FILL, border }, // Columns
				// { border, TableLayoutConstants.FILL, border }
				// }; // Rows
				// w.setLayout(new TableLayout(size));
				// w.add(
				// getWebPane("http://pgrc-16.ipk-gatersleben.de/~klukas/expdat/gravisto/dbe-gravisto.html"),
				// "1,1");
				// w.setSize(640,480);
				// w.setVisible(true);
			}
		});
		
		JMenuItem jMenuItemReleaseInfo = new JMenuItem("Release Info");
		jMenuItemReleaseInfo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean ext = true;
				if (ext) {
					// AttributeHelper.showInBrowser("http://vanted.ipk-gatersleben.de/index.php?file=doc139.html");
					AttributeHelper.showInBrowser("http://iap.ipk-gatersleben.de");
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
						// getWebPane("http://vanted.ipk-gatersleben.de/index.php?file=doc139.html"),
						getWebPane("http://iap.ipk-gatersleben.de"),
						"1,1");
				w.setSize(640, 480);
				w.setVisible(true);
			}
		});
		
		JMenuItem feedback = new JMenuItem("Send E-Mail Feedback");
		feedback.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AttributeHelper.showInBrowser("mailto:klukas@ipk-gatersleben.de?subject=" + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT + "%20feedback");
			}
		});
		
		JMenuItem cite = new JMenuItem("How to cite?");
		cite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ReleaseInfo.getRunningReleaseStatus() == Release.RELEASE_PUBLIC) {
					MainFrame
							.showMessageDialogWithScrollBars(
									"Please cite the following paper, if you have used "
											+ DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT
											+ " for your research:<br><br>"
											+
											"Bj&ouml;rn H. Junker, Christian Klukas and Falk Schreiber (2006): <b>VANTED: A system for advanced data analysis and visualization in the context of biological networks.</b> BMC Bioinformatics, 7:109<br><br>"
											+
											"LaTeX/Bibtex:<hr><code>"
											+
											"@article{junker2006vsa,<br>"
											+
											"&nbsp;&nbsp;&nbsp;title={{VANTED: a system for advanced data analysis and visualization in the context of biological networks}},<br>"
											+
											"&nbsp;&nbsp;&nbsp;author={Junker, B.H. and Klukas, C. and Schreiber, F.},<br>"
											+
											"&nbsp;&nbsp;&nbsp;journal={BMC Bioinformatics},<br>"
											+
											"&nbsp;&nbsp;&nbsp;volume={7},<br>"
											+
											"&nbsp;&nbsp;&nbsp;number={1},<br>"
											+
											"&nbsp;&nbsp;&nbsp;pages={109},<br>"
											+
											"&nbsp;&nbsp;&nbsp;year={2006}<br>"
											+
											"}<br>"
											+
											"</code><hr><br><br>An overview on the system is available in this commentary:<br><br>"
											+
											"Christian Klukas, Björn H. Junker and Falk Schreiber (2006): <b>The VANTED software system for transcriptomics, proteomics and metabolomics analysis.</b> J. Pestic. Sci. Vol. 31<br><br>"
											+
											"LaTex/Bibtex:<hr><code>"
											+
											"@article{klukas2006vss,<br>"
											+
											"&nbsp;&nbsp;&nbsp;title={{The VANTED software system for transcriptomics, proteomics and metabolomics analysis}},<br>"
											+
											"&nbsp;&nbsp;&nbsp;author={Klukas, C. and Junker, B.H. and Schreiber, F.},<br>" +
											"&nbsp;&nbsp;&nbsp;journal={Journal of Pesticide Science},<br>" +
											"&nbsp;&nbsp;&nbsp;volume={31},<br>" +
											"&nbsp;&nbsp;&nbsp;number={3},<br>" +
											"&nbsp;&nbsp;&nbsp;pages={289--292},<br>" +
											"&nbsp;&nbsp;&nbsp;year={2006},<br>" +
											"&nbsp;&nbsp;&nbsp;publisher={J-STAGE}<br>" +
											"}<br>" +
											"</code><hr>",
									"Literature");
				} else
					if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
						MainFrame
								.showMessageDialogWithScrollBars(
										"Please cite the following paper, if you have used "
												+ DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT
												+ " for your research:<br><br>"
												+
												"Christian Klukas and Falk Schreiber: <b>Dynamic exploration and editing of KEGG pathway diagrams.</b> Bioinformatics 2007 23: 344-350.<br><br>"
												+
												"LaTeX/Bibtex:<hr><code>" +
												"@article{klukas2007dea,<br>" +
												"&nbsp;&nbsp;&nbsp;title={{Dynamic exploration and editing of KEGG pathway diagrams}},<br>" +
												"&nbsp;&nbsp;&nbsp;author={Klukas, C. and Schreiber, F.},<br>" +
												"&nbsp;&nbsp;&nbsp;journal={Bioinformatics},<br>" +
												"&nbsp;&nbsp;&nbsp;volume={23},<br>" +
												"&nbsp;&nbsp;&nbsp;number={3},<br>" +
												"&nbsp;&nbsp;&nbsp;pages={344},<br>" +
												"&nbsp;&nbsp;&nbsp;year={2007},<br>" +
												"&nbsp;&nbsp;&nbsp;publisher={Oxford Univ Press}<br>" +
												"}<br>" +
												"</code>",
										"Literature");
					}
			}
		});
		
		JMenuItem database = new JMenuItem("Database Status");
		database.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DatabaseFileStatusService.showStatusDialog();
			}
		});
		
		JMenuItem preferences = new JMenuItem("Preferences");
		preferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPreferences();
			}
		});
		
		final JMenuItem error = new JMenuItem(errorMessagesMenuTitle);
		error.setMnemonic('E');
		error.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] errorMsgs = ErrorMsg.getErrorMessages();
				String[] errorMsgsShort = ErrorMsg.getErrorMessagesShort();
				StringBuilder err = new StringBuilder("");
				StringBuilder errorLogShort = new StringBuilder("");
				if (errorMsgs.length > 0) {
					if (errorMsgs.length == 1)
						err.append("<html><body><h2>" + errorMsgs.length + " problem detected</h2>");
					else
						err.append("<html><body><h2>" + errorMsgs.length + " problems detected</h2>");
					err
							.append("<h2>Click &quot;Yes&quot; to send a short error log by mail to help fixing bugs. Click &quot;No&quot; to just close this dialog.</h2><hr>");
					for (int i = 0; i < errorMsgs.length; i++) {
						if (errorMsgs[i] != null) {
							err.append(errorMsgs[i] + "<p>");
						}
					}
					for (int i = 0; i < errorMsgsShort.length; i++) {
						if (errorMsgsShort[i] != null) {
							errorLogShort.append(errorMsgsShort[i] + " //// ");
						}
					}
					err.append("</body></html>");
				} else {
					err.append("<html><body><h2>No error messages available.</h2></body></html>");
				}
				ErrorMsg.clearErrorMessages();
				// GraffitiSingleton.getInstance().getMainFrame().showMessageDialog(err);
				JEditorPane errMsg = new JEditorPane("text/html", err.toString());
				errMsg.setEditable(false);
				errMsg.setAutoscrolls(false);
				
				final JScrollPane scp = new JScrollPane(errMsg);
				scp.setMaximumSize(new Dimension(800, 600));
				scp.setPreferredSize(new Dimension(800, 600));
				Timer t = new Timer(10, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						scp.getVerticalScrollBar().setValue(scp.getVerticalScrollBar().getMinimum());
						scp.getHorizontalScrollBar().setValue(scp.getHorizontalScrollBar().getMinimum());
					}
				});
				t.setRepeats(false);
				t.start();
				if (errorMsgs.length > 0) {
					if (JOptionPane.showConfirmDialog(GravistoService.getInstance().getMainFrame(),
							scp, "Send E-Mail to help fixing these bugs?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
						ClipboardService.writeToClipboardAsText(errorLogShort.toString());
						MainFrame.showMessageDialog("<html>" +
								"As you press OK your default email application should be automatically opened.<br>" +
								"If the subject or email body is not automatically filled with the error log information,<br>" +
								"please choose the menu command Edit/Paste to fill the email body with the relevant information<br>" +
								"(the error log information has been copied to the clipboard).<br><br>" +
								"<br>" +
								"By sending this error log information you help improving the system, eventually<br>" +
								"the bugs you just experienced will be fixed very soon!<br>" +
								"If you have general suggestions for improvement, use the Send feedback command<br>" +
								"from the Help menu!",
								"Information");
						AttributeHelper.showInBrowser("mailto:klukas@ipk-gatersleben.de?subject=" + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT +
								"%20errorlog&body=" + errorLogShort.toString());
					}
				} else {
					MainFrame.showMessageDialog("No error messages logged!", "Information");
				}
				
			}
		});
		
		int pos = 1;
		insert(error, pos++);
		// if (ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
		// insert(jMenuItemJavaHelp, pos++);
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
			insert(jMenuItemJavaHelpPDF, pos++);
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.URL_HELPTEXT))
			insert(helpText, pos++);
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.URL_RELEASEINFO))
			insert(jMenuItemReleaseInfo, pos++);
		insert(database, pos++);
		if (!SystemInfo.isMac())
			insert(preferences, pos++);
		insert(feedback, pos++);
		
		// if (ReleaseInfo.getRunningReleaseStatus()==Release.KGML_EDITOR ||
		// ReleaseInfo.getRunningReleaseStatus()==Release.RELEASE_PUBLIC)
		// insert(cite, pos++);
		
		insert(info, pos++);
		
		// mark Help Menu and Error menu item in case error messages are available
		Timer t = new Timer(200, new ActionListener() {
			@Override
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
					if (!error.getText().equals(errorMessagesMenuTitle))
						error.setText(errorMessagesMenuTitle);
					error.setEnabled(false);
				}
				if (errorMsg != null) {
					if (errorMsg.length > lastErrorCount && !MainFrame.getInstance().isTaskPanelVisible("Error Watch")) {
						// BackgroundTaskHelper.isTaskWithGivenReferenceRunning()
						BackgroundTaskHelper.issueSimpleTask("Error Watch",
								ErrorMsg.getErrorMessages()[errorMsg.length - 1],
								"<html><small>Click 'Help/Error Messages' for more details.",
								new Runnable() {
									@Override
									public void run() {
									}
								}, new Runnable() {
									@Override
									public void run() {
									}
								}, false);
					}
					// if (!MainFrame.getInstance().isTaskPanelVisible("Error Watch"))
					lastErrorCount = errorMsg.length;
				}
			}
		});
		t.start();
	}
	
	public void showAbout() {
		if (info != null)
			info.doClick();
	}
	
	/**
	 * Needed for Mac compatibility (used by reflection code)
	 */
	public void doQuit() {
		MainFrame.getInstance().closeGravisto();
	}
	
	public void showPreferences() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			WorkflowHelper.showPreferencesFolder();
		} else {
			MainFrame.getInstance().showAndHighlightSidePanelTab("Help", false);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
					}
					MainFrame.getInstance().showAndHighlightSidePanelTab("Settings", false);
				}
			});
			t.start();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void saveFiles() {
		String targetFileName = FileHelper.getFileName("txt", "license text file");
		if (targetFileName == null || targetFileName.length() <= 0) {
			MainFrame.showMessageDialog("License text not saved!", "Operation aborted");
			return;
		}
		ClassLoader cl = this.getClass().getClassLoader();
		
		String path = this.getClass().getPackage().getName().replace('.', '/');
		try {
			File tgt = new File(targetFileName);
			FileOutputStream out = new FileOutputStream(tgt);
			long sz = 0;
			for (String fileName : getLibLicenseFileNames()) {
				InputStream inpS = cl.getResourceAsStream(path + "/license/" + fileName);
				InputStream in = inpS;
				
				int b;
				while ((b = inpS.read()) != -1) {
					out.write(b);
					sz++;
				}
				sz = sz / 1024;
				in.close();
			}
			out.close();
			MainFrame.showMessage("License text saved as " + targetFileName + " (" + sz + " kb)", MessageType.INFO);
			AttributeHelper.showInBrowser(tgt.toURL().toString());
		} catch (FileNotFoundException err) {
			MainFrame.showMessageDialog("Error: " + err.getLocalizedMessage(), "Could not save file");
			ErrorMsg.addErrorMessage(err);
		} catch (IOException err) {
			MainFrame.showMessageDialog("Error: " + err.getLocalizedMessage(), "Could not save file");
			ErrorMsg.addErrorMessage(err);
		}
	}
	
	private String[] getLibLicenseFileNames() {
		return new String[] {
				"apache.txt", "cpl.txt", "cupparser.txt",
				"lesser.txt", "secondstring.txt", "spl.txt" };
	}
	
	public static String getLibsText() {
		return getLibText(
				"SecondString",
				"a open-source Java-based package of approximate string-matching techniques",
				"University of Illinois/NCSA Open Source License",
				new String[] { "http://opensource.org/licenses/UoI-NCSA.php" }) +
				getLibText(
						"JRuby",
						"a pure Java implementation of the Ruby interpreter",
						"JRuby is free software released under a tri CPL/GPL/LGPL license.",
						new String[] {
								"http://opensource.org/licenses/cpl1.0.php",
								"http://opensource.org/licenses/gpl-license.php",
								"http://opensource.org/licenses/lgpl-license.php"
						}
				) +
				getLibText(
						"Apache SOAP; Axis",
						"A implementation of the SOAP submission to W3C; Axis is essentially a SOAP engine",
						"Apache SOAP packages are made available under the Apache Software License",
						new String[] {
						"http://ws.apache.org/LICENSE.txt"
						}
				) +
				getLibText(
						"JavaMail",
						"a platform-independent and protocol-independent framework to build mail and messaging applications",
						"Copyright 1994-2005 Sun Microsystems, Inc.",
						new String[] {
						}
				) +
				getLibText(
						"JavaHelp",
						"a full-featured, platform-independent, extensible help system",
						"Copyright 2003 Sun Microsystems, Inc.",
						new String[] {
						}
				) +
				getLibText(
						"JCommon",
						"a collection of useful classes used by JFreeChart, JFreeReport and other projects",
						" JCommon is licensed under the terms of the GNU Lesser General Public Licence (LGPL).",
						new String[] {
						"http://www.jfree.org/lgpl.php"
						}
				) +
				getKEGGlibText() +
				getLibText(
						"Jakarta Commons",
						"The Commons is a Jakarta subproject focused on all aspects of reusable Java components.",
						"Apache License Version 2.0",
						new String[] {
						"http://jakarta.apache.org/commons/license.html"
						}
				) +
				getLibText(
						"CUP Parser Generator",
						"LALR Parser Generator in Java - Version 11",
						"CUP Parser Generator Copyright Notice, License, and Disclaimer" +
								"Copyright 1996-1999 by Scott Hudson, Frank Flannery, C. Scott Ananian" +
								"Permission to use, copy, modify, and distribute this software and its " +
								"documentation for any purpose and without fee is hereby granted, " +
								"provided that the above copyright notice appear in all copies and " +
								"that both the copyright notice and this permission notice and warranty " +
								"disclaimer appear in supporting documentation, and that the names of the " +
								"authors or their employers not be used in advertising or publicity " +
								"pertaining to distribution of the software without specific, written " +
								"prior permission. " +
								"The authors and their employers disclaim all warranties with regard to " +
								"this software, including all implied warranties of merchantability and " +
								"fitness. In no event shall the authors or their employers be liable " +
								"for any special, indirect or consequential damages or any damages whatsoever " +
								"resulting from loss of use, data or profits, whether in an action of " +
								"contract, negligence or other tortious action, arising out of or in " +
								"connection with the use or performance of this software.",
						new String[] {
						"http://www2.cs.tum.edu/projects/cup/licence.html"
						}
				) +
				getLibText("BeanShell",
						"BeanShell is a small, free, embeddable Java source interpreter with object scripting language features, written in Java.",
						"BeanShell is dual licensed under both the SPL and LGPL. You may use and develop BeanShell under either license.",
						new String[] {
								"http://www.netbeans.org/about/legal/spl.html",
								"http://www.gnu.org/copyleft/lesser.html"
						}) +
				getLibText("Enzyme Nomenclature Database (SIB)",
						"we believe  that the  ENZYME  database  can be useful to anybody working " +
								"with enzymes",
						"This database is copyright from the Swiss Institute of Bioinformatics." +
								"There are  no restrictions  on  its use by any institutions as long as " +
								"its content is in no way modified.",
						new String[] {
						"http://www.expasy.org/cgi-bin/lists?enzyme.get"
						}) +
				getLibText("JFreeChart",
						"a free chart library for the Java(tm) platform",
						"This library is free software; you can redistribute it and/or modify it under the terms " +
								"of the GNU Lesser General Public License as published by the Free Software Foundation; " +
								"either version 2.1 of the License, or (at your option) any later version.",
						new String[] {
						"http://www.gnu.org/copyleft/lesser.html"
						});
	}
	
	public static String getKEGGlibText() {
		return getLibText(
				"KEGG libraries and SOAP access",
				"KEGG - Kyoto Encyclopedia of Genes and Genomes (Kanehisa Laboratory of Kyoto University Bioinformatics Center)",
				"(at the listed web-site possible updates to this license text might be available and should be considered)" +
						"<hr>" +
						"Non-academic users and Academic users intending to use KEGG for commercial purposes are " +
						"requested to obtain a license agreement through KEGG's exclusive licensing agent, Pathway " +
						"Solutions, for installation of KEGG at their sites, for distribution or reselling of KEGG " +
						"data, for software development or any other commercial activities that make use of KEGG, " +
						"or as end users of any third-party application that requires downloading of KEGG data or " +
						"access to KEGG data via the KEGG API.",
				new String[] {
				"http://www.genome.jp/kegg/legal.html"
				});
		/*
		 * return getLibText(
		 * "KEGG libraries and SOAP access",
		 * "KEGG - Kyoto Encyclopedia of Genes and Genomes (Kanehisa Laboratory of Kyoto University Bioinformatics Center)",
		 * "(the following license text was current during development of the system, " +
		 * "at the listed web-site possible updates might be available and should also be considered)" +
		 * "<hr>" +
		 * "Academic users may freely use the KEGG web site at http://www.genome.jp/kegg/. " +
		 * "Non-academic users may also use the KEGG web site as end users, " +
		 * "but any form of distribution requires a license agreement (see below)." +
		 * "Non-academic users are requested to obtain a license agreement through " +
		 * "the licensor, Pathway Solutions Inc., for installation of KEGG at their " +
		 * "sites, for distribution or reselling of KEGG data, for software development " +
		 * "and any other commercial activities that make use of KEGG, and also as end " +
		 * "users of the third-party programs that access the KEGG ftp site.",
		 * new String[] {
		 * "http://www.genome.jp/kegg/kegg5.html"
		 * }
		 * );
		 */
		/*
		 * return getLibText(
		 * "KEGG libraries and SOAP access",
		 * "KEGG - Kyoto Encyclopedia of Genes and Genomes (Kanehisa Laboratory of Kyoto University Bioinformatics Center)",
		 * "<b>Disclaimer</b><br>"+
		 * "KEGG is an original database product developed by Minoru Kanehisa and his " +
		 * "associates with financial supports from the Japanese Government. Although best " +
		 * "efforts are always applied, neither Minoru Kanehisa nor his associates warrant " +
		 * "or assume any legal " +
		 * "responsibility for accuracy or usefulness of any information in KEGG.<br><br>"+
		 * "<b>KEGG web site</b><br>"+
		 * "Academic users may freely use the KEGG web site at http://www.genome.jp/kegg/. " +
		 * "Non-academic users may also use the KEGG web site as end users, but any form "+
		 * "of distribution requires a license agreement (see below).<br><br>"+
		 * "<b>GenomeNet ftp site</b><br>"+
		 * "Academic users may freely download the KEGG data as provided at the GenomeNet " +
		 * "ftp site at ftp://ftp.genome.jp/pub/kegg/.<br><br>"+
		 * "<b>License agreement</b><br>"+
		 * "Non-academic users are requested to obtain a license agreement through the " +
		 * "licensor, Pathway Solutions, for installation of KEGG at their sites, for " +
		 * "distribution or reselling of KEGG data, for software development and any " +
		 * "other commercial activities that make use of KEGG, and also as end users " +
		 * "of the third-party programs that require downloading of KEGG data or access " +
		 * "to KEGG API.<br><br>"+
		 * "Academic users are requested to contact Pathway Solutions for local implementation" +
		 * " of KEGG, for distribution of KEGG data, and for any other activities that may " +
		 * "require a license agreement.<br><br>" +
		 * "<b>Last updated: July 1, 2006</b><hr>" +
		 * "<b>Please also visit this website, to see, whether this license text, which was " +
		 * "current during development of this software, is still valid:</b>",
		 * new String[] {
		 * "http://www.genome.jp/kegg/kegg5.html"
		 * }
		 * );
		 */
	}
	
	private static ArrayList<String> knownUrls = new ArrayList<String>();
	
	protected static String getLibText(String lib, String desc, String licenseDesc, String[] licenseTextURLS) {
		String res = "<li><b>" + lib + "</b><br>- " + StringManipulationTools.getWordWrap(desc, 60) + "<p><small>"
				+ StringManipulationTools.getWordWrap(licenseDesc, 80) + "</small>";
		for (String url : licenseTextURLS) {
			if (!knownUrls.contains(url))
				knownUrls.add(url);
			// res += "<br><small><font color=\"blue\">"+url+"</font></small>";
			res += "<br><small><a href=\"" + url + "\">" + url + "</a></small>";
		}
		String res2 = "**" + lib + "**\n\n" + desc + "\n\n**" + licenseDesc + "**";
		for (String url : licenseTextURLS) {
			if (!knownUrls.contains(url))
				knownUrls.add(url);
			res2 += "\n\n[[" + url + "|" + url + "]]\n\n";
		}
		// System.out.println(res2);
		return res;
	}
	
	protected ArrayList<String> getLibLicenseUrls() {
		ArrayList<String> res = new ArrayList<String>();
		res.add(extractText);
		res.addAll(knownUrls);
		return res;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
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
		} catch (UnknownHostException e) {
			editorPane = new JEditorPane(
					"",
					"Web-Content (" + urlText + ") could not be located, check your internet connection!");
		} catch (Exception e) {
			editorPane = new JEditorPane(
					"",
					"Could not show Help-Window! "
							+ "Error: " + e.toString());
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
			@Override
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
				
			}
		};
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
