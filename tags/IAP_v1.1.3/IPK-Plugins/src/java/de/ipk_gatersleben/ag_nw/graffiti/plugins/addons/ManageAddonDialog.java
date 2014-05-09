package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;

import net.iharder.dnd.FileDrop;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FolderPanel;
import org.GuiRow;
import org.MarkComponent;
import org.ReleaseInfo;
import org.SearchFilter;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginEntry;

import com.lowagie.text.Font;

import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.plugin_info.PluginInfoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.NewsHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.RSSFeedManager;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class ManageAddonDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static int iconcolumn = 0;
	final static int namecolumn = 1;
	final static int checkcolumn = 2;
	
	// private KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
	// private KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
	
	private JTextArea textVersion;
	private JButton buttonclose;
	private JButton buttonopenplugindir;
	// private JLabel labelwarning;
	private JButton buttoninstall;
	
	private MarkComponent findUpdatesMarker;
	
	private JButton buttondownload;
	private JTextArea textDescription;
	private JTable tableaddons;
	private JLabelHTMLlink textContact;
	// private AddonManagerPlugin manager;
	private JLabel msgPanel;
	// private String msg;
	protected PluginDescription currentDPE;
	
	public ManageAddonDialog(JFrame frame, String msg) {
		super(frame);
		frame.setEnabled(false);
		// this.manager = addonManager;
		JButton jb = initGUI();
		setTopText(msg);
		
		setVisible(true);
		installDragNDrop(jb);
	}
	
	private void installDragNDrop(final JButton result) {
		final String oldText = result.getText();
		FileDrop.Listener fdl = new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				if (files != null && files.length > 0)
					for (File f : files)
						if (!f.getName().toLowerCase().endsWith(".jar")) {
							result.setText("<html>File is not a valid Add-on!");
							Timer t = new Timer(5000, new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									result.setText(oldText);
								}
							});
							t.start();
						}
							else
								process(f);
				}
		};
		
		Runnable dragdetected = new Runnable() {
			public void run() {
				result.setText("<html><br><b>Drop file to install Add-on<br><br>");
			}
		};
		
		Runnable dragenddetected = new Runnable() {
			public void run() {
				if (!result.getText().contains("!"))
					result.setText(oldText);
			}
		};
		// result
		new FileDrop(null, this, null, true, fdl, dragdetected, dragenddetected);
	}
	
	@Override
	public void setVisible(boolean visible) {
		AddonManagerPlugin.getInstance().dialog = null;
		if (!visible)
			getParent().setEnabled(true);
		super.setVisible(visible);
	}
	
	public void close() {
		setVisible(false);
		getParent().setEnabled(true);
		dispose();
	}
	
	private JButton initGUI() {
		try {
			
			addKeys();
			
			TableLayout thisLayout = new TableLayout(new double[][] {
								// spalten
					{ 5.0, TableLayoutConstants.FILL, 80, 10.0, 85.0, 80, 0.0, 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 5.0 },
								// zeilen
					{ 0.0, TableLayout.PREFERRED, 5.0, 0.0, 45.0, 5.0, 45.0, 5.0, 45.0, 0.0, TableLayoutConstants.FILL, 5.0, 45.0, 0.0, 0, 0, 5.0, 30.0,
													5.0 } });
			getContentPane().setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(700, 400));
			this.setMinimumSize(new java.awt.Dimension(700, 400));
			this.setTitle("Add-on Manager");
			
			// setModal(true);
			// setAlwaysOnTop(true);
			setLocationRelativeTo(MainFrame.getInstance());
			
			{
				textContact = new JLabelHTMLlink("", "", null, true) {
					private static final long serialVersionUID = 1L;
					
					public String getToolTipText() {
						if (getText().length() > 0)
							return super.getToolTipText();
						else
							return null;
					}
					
					public Cursor getCursor() {
						if (getText().length() > 0)
							return super.getCursor();
						else
							return Cursor.getDefaultCursor();
					}
				};
				
				textContact.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
				textContact.setOpaque(true);
				JComponent jc = TableLayout.getSplit(textContact, null, TableLayout.FILL, 0);
				jc.setBorder(BorderFactory.createTitledBorder("Availability"));
				getContentPane().add(jc, "8, 4, 9, 4");
			}
			{
				textDescription = new JTextArea() {
					private static final long serialVersionUID = 1L;
					
					public String getToolTipText() {
						if (getText().length() > 0)
							return super.getToolTipText();
						else
							return null;
					}
					
					public Cursor getCursor() {
						if (getText().length() > 0)
							return super.getCursor();
						else
							return Cursor.getDefaultCursor();
					}
				};
				textDescription.setFont(new JLabel().getFont().deriveFont(Font.NORMAL));
				textDescription.setEditable(false);
				textDescription.setLineWrap(true);
				textDescription.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
				textDescription.setWrapStyleWord(true);
				textDescription.setBackground(null);
				textDescription.setOpaque(false);
				Cursor c = new Cursor(Cursor.HAND_CURSOR);
				textDescription.setCursor(c);
				textDescription.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
						// AttributeHelper.showInBrowser(urlLink);
						if (currentDPE != null) {
							boolean found = false;
							for (PluginEntry pe : MainFrame.getInstance().getPluginManager().getPluginEntries()) {
								if (pe.getDescription().getName().equals(currentDPE.getName())) {
									MainFrame.showMessageDialog(
														"<html>" + PluginInfoHelper.getPluginDescriptionTable(pe),
														"Add-on features (" + currentDPE.getName() + ")");
									found = true;
									break;
								}
							}
							if (!found) {
								MainFrame.showMessageDialog(
													"<html>Add-on is not loaded. Plugin-features can't be determined.",
													"Information");
							}
						}
					}
					
					public void mousePressed(MouseEvent e) {
					}
					
					public void mouseReleased(MouseEvent e) {
					}
					
					Color oldColor;
					boolean oldOpaque;
					
					public void mouseEntered(MouseEvent e) {
						if (textDescription.getText().length() <= 0)
							return;
						oldOpaque = textDescription.isOpaque();
						textDescription.setOpaque(true);
						oldColor = textDescription.getBackground();
						textDescription.setBackground(new Color(240, 240, 255));
					}
					
					public void mouseExited(MouseEvent e) {
						textDescription.setOpaque(oldOpaque);
						textDescription.setBackground(oldColor);
					}
				});
				textDescription.setToolTipText("Show list of add-on features");
				JComponent jc = TableLayout.getSplit(textDescription, null, TableLayout.FILL, 0);
				jc.setOpaque(false);
				jc.setBorder(BorderFactory.createTitledBorder("Description"));
				getContentPane().add(jc, "8, 6, 9, 11");
			}
			{
				textVersion = new JTextArea();
				getContentPane().add(textVersion, "8, 12, 9, 15");
				textVersion.setBackground(this.getBackground());
				textVersion.setEditable(false);
				textVersion.setBorder(BorderFactory.createTitledBorder("Compatibility"));
				textVersion.setOpaque(false);
			}
			{
				tableaddons = new JTable(new AddonTableModel());
				tableaddons.setOpaque(false);
				tableaddons.setGridColor(new Color(230, 230, 230));
				tableaddons.setRowHeight(35);
				tableaddons.getTableHeader().setReorderingAllowed(false);
				tableaddons.getColumn("Active").setMaxWidth(50);
				tableaddons.getColumn("Active").setMinWidth(50);
				tableaddons.getColumn("").setMaxWidth(35);
				tableaddons.getColumn("").setMinWidth(35);
				tableaddons.getModel().addTableModelListener(new TableModelListener() {
					public void tableChanged(TableModelEvent e) {
						if (tableaddons.getSelectedColumn() == checkcolumn) {
							BackgroundTaskHelper.executeLaterOnSwingTask(10, new Runnable() {
								public void run() {
									if (!AddonManagerPlugin.getInstance().getAddon(tableaddons.getSelectedRow()).isActive()) {
										if (AddonManagerPlugin.getInstance().activateAddon(tableaddons.getSelectedRow()))
											setTopText("<html><b>Add-on \"" + AddonManagerPlugin.getInstance().getAddon(tableaddons.getSelectedRow()).getName()
																+ "\" is active");
										else
											setTopText("<html><b>Add-on \"" + AddonManagerPlugin.getInstance().getAddon(tableaddons.getSelectedRow()).getName()
																+ "\" is not activated");
									} else {
										AddonManagerPlugin.getInstance().deactivateAddon(tableaddons.getSelectedRow());
										setTopText("<html><b>Deactivation of Add-on \""
															+ AddonManagerPlugin.getInstance().getAddon(tableaddons.getSelectedRow()).getName() + "\" needs restart");
									}
								}
							});
							// tableaddons.repaint();
						}
					}
				});
				tableaddons.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				// tableaddons.setR AutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
				tableaddons.setPreferredSize(new java.awt.Dimension(228, 190));
				tableaddons.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting() == false) {
							// textAuthor.setText(addons.getDescription(tableaddons.getSelectedRow()).getAuthor());
							Addon a = AddonManagerPlugin.getInstance().getAddon(tableaddons.getSelectedRow());
							textContact.setText("<html><code>" + a.getDescription().getAvailable());
							textContact.setUrl(a.getDescription().getAvailable());
							currentDPE = a.getDescription();
							textDescription.setText(
												pretifyAddonDesc(
												AddonManagerPlugin.getInstance().getAddon(tableaddons.getSelectedRow()).getDescription().getDescription()
												)
												);
							String vvv = a.getDescription().getCompatibleVersion();
							if (vvv == null || vvv.equalsIgnoreCase("null"))
								vvv = "";
							String mmm;
							
							if (vvv == null || vvv.length() == 0)
								textVersion.setText(" No compatiblity information specified");
							else {
								if (vvv.contains(","))
									mmm = "s ";
								else
									mmm = " ";
								
								textVersion.setText(" " + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT + " Version" + mmm + vvv);
							}
							
						}
					}
					
					private String pretifyAddonDesc(String d) {
						if (d == null || d.trim().length() <= 0)
							return "- no plugin description defined -";
						d = StringManipulationTools.stringReplace(d, "\n", "");
						d = StringManipulationTools.stringReplace(d, "\t", "");
						return d;
					}
				});
				if (tableaddons.getRowCount() > 0)
					tableaddons.getSelectionModel().setSelectionInterval(0, 0);
				
				tableaddons.addMouseListener(new MouseListener() {
					public void mouseReleased(MouseEvent e) {
						Point p = e.getPoint();
						int rowNumber = tableaddons.rowAtPoint(p);
						tableaddons.getSelectionModel().setSelectionInterval(rowNumber, rowNumber);
						
						if (tableaddons.getSelectedRow() > -1 && (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3)) {
							final Addon addon = AddonManagerPlugin.getInstance().getAddon(tableaddons.getSelectedRow());
							final boolean wasActive = addon.isActive();
							JPopupMenu menu = new JPopupMenu();
							JMenuItem item = new JMenuItem("Uninstall " + addon.getDescription().getName());
							item.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									try {
										AddonManagerPlugin.getInstance().removeAddon(addon.getJarFile());
										
										String msg = "<html><b>\"" + addon.getDescription().getName() + "\" has been uninstalled." +
															(wasActive ? "<br>Deactivation requires restart of the program." : "");
										if (AttributeHelper.windowsRunning())
											msg = "<html><b>\""
																+ addon.getDescription().getName()
																+ "\" has been marked for removal."
																+
																(wasActive ? "<br>Deactivation and complete uninstallation requires restart of the program."
																					: "Completion of the deinstallation requires restart of the program.");
										
										rebuild(msg, false);
									} catch (Exception e) {
										ErrorMsg.addErrorMessage(e);
									}
								}
							});
							menu.add(item);
							menu.show(tableaddons, e.getX(), e.getY());
						}
					}
					
					public void mousePressed(MouseEvent e) {
					}
					
					public void mouseExited(MouseEvent e) {
					}
					
					public void mouseEntered(MouseEvent e) {
					}
					
					public void mouseClicked(MouseEvent e) {
					}
					
				});
				
				JComponent ttt = TableLayout.getSplitVertical(
									tableaddons.getTableHeader(),
									tableaddons,
									TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
				ttt.setBorder(BorderFactory.createEtchedBorder());
				getContentPane().add(
									ttt
									, "1, 4, 6, 15");
			}
			{
				buttoninstall = new JButton();
				buttoninstall.setText("Install Add-on");
				buttoninstall.setOpaque(false);
				buttoninstall.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.setAcceptAllFileFilterUsed(false);
						fc.setFileFilter(new FileFilter() {
							@Override
							public boolean accept(File pathname) {
								return pathname.getName().toLowerCase().endsWith(".jar") || pathname.isDirectory();
							}
							
							@Override
							public String getDescription() {
								return "Add-on Files (*.jar)";
							}
						});
						fc.setApproveButtonText("Install");
						fc.setDialogTitle("Choose Add-on to be installed");
						int returnVal = fc.showDialog(getParent(), null);// OpenDialog(getParent());
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							try {
								AddonManagerPlugin.getInstance().installAddon(fc.getCurrentDirectory().toString(), fc.getSelectedFile().getName());
							} catch (FileNotFoundException e1) {
								ErrorMsg.addErrorMessage(e1);
							} catch (IOException e1) {
								ErrorMsg.addErrorMessage(e1);
							}
						}
					}
				});
			}
			{
				buttondownload = new JButton();
				
				findUpdatesMarker = new MarkComponent(buttondownload, false, TableLayout.PREFERRED, false, TableLayout.FILL);
				
				buttondownload.setText("Find Add-ons/Updates");
				buttondownload.setOpaque(false);
				buttondownload.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						close();
						
						final RSSFeedManager rfm = RSSFeedManager.getInstance();
						rfm.loadRegisteredFeeds();
						rfm.setWordWrap(60);
						NewsHelper.refreshNews(rfm, new Runnable() {
							public void run() {
								ArrayList<Object> res = new ArrayList<Object>();
								boolean found = false;
								for (JComponent jc : rfm.getNewsComponents()) {
									if (jc instanceof FolderPanel) {
										FolderPanel fp = (FolderPanel) jc;
										fp.setCondensedState(false);
										fp.addSearchFilter(getSearchFilter());
										fp.setMaximumRowCount(3, true);
										fp.setShowCondenseButton(false);
										fp.layoutRows();
										fp.addCollapseListenerDialogSizeUpdate();
										fp.setTitle(fp.getTitle().substring(0, fp.getTitle().indexOf(" (")) + " - " + fp.getFixedSearchFilterMatchCount() + " messages");
										if (fp.getFixedSearchFilterMatchCount() > 0) {
											found = true;
											res.add("");
											res.add(jc);
										}
									}
								}
								if (!found) {
									res.add("");
									res.add(new JLabel(
														"<html>" +
																			"Currently, there is no new or additional " +
																			"Add-on available for direct download."));
								}
								MyInputHelper.getInput("[OK]", "Direct Add-on Download", res.toArray());
							}
							
							private SearchFilter getSearchFilter() {
								return new SearchFilter() {
									public boolean accept(GuiRow gr, String searchText) {
										if (gr.left == null || gr.right == null || searchText == null)
											return true;
										JButton downloadButton = (JButton) ErrorMsg.findChildComponent(gr.right, JButton.class);
										if (downloadButton != null) {
											String addOnProperty = (String) downloadButton.getClientProperty("addon-version");
											String oldVersion = "";
											if (addOnProperty != null && addOnProperty.length() > 0 && addOnProperty.contains("/v")) {
												// "vanted3d/v0.4"
												String addOnName = addOnProperty.substring(0, addOnProperty.indexOf("/v"));
												String addOnVersion = addOnProperty.substring(addOnProperty.indexOf("/v") + "/v".length());
												for (Addon a : AddonManagerPlugin.getInstance().getAddons()) {
													if (a.getDescription() != null) {
														String n = a.getName();
														String v = a.getDescription().getVersion();
														if (addOnName.equalsIgnoreCase(n)) {
															if (v.compareToIgnoreCase(addOnVersion) >= 0)
																return false;
															else
																oldVersion = v;
														}
													}
												}
												if (oldVersion != null && oldVersion.length() > 0)
													downloadButton.setText("<html>Update Add-on from v" + oldVersion + " to v" + addOnVersion + "");
												return FolderPanel.getDefaultSearchFilter(null).accept(gr, searchText);
											}
										}
										return false;
									}
								};
							}
						}, null);
					}
				});
			}
			{
				buttonclose = new JButton();
				buttonclose.setText("Close");
				buttonclose.setOpaque(false);
				buttonclose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						close();
					}
				});
			}
			{
				buttonopenplugindir = new JButton();
				buttonopenplugindir.setText("Open Add-on Folder");
				buttonopenplugindir.setOpaque(false);
				buttonopenplugindir.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AttributeHelper.showInBrowser(ReleaseInfo.getAppSubdirFolder("addons"));
					}
				});
			}
			buttonclose.setText("OK");
			getContentPane().add(
								getButtonPanel(
													buttonclose,
													buttoninstall,
													buttonopenplugindir,
													findUpdatesMarker), "1,17,9,17");
		} catch (Exception e) {
			e.printStackTrace();
		}
		validate();
		pack();
		return buttoninstall;
	}
	
	private void addKeys() {
		// GravistoService.addActionOnKeystroke(this,new ActionListener() {
		// public void actionPerformed(ActionEvent arg0) {
		// close();
		// }
		// },escape);
		//
		// GravistoService.addActionOnKeystroke(this,new ActionListener() {
		// public void actionPerformed(ActionEvent arg0) {
		// buttonclose.doClick();
		// }
		// },enter);
	}
	
	private Component getButtonPanel(JComponent a,
						JComponent b, JComponent c, JComponent d) {
		return TableLayout.get4Split(a, b, c, d, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 10, 0);
	}
	
	public boolean process(File f) {
		if (f.getAbsolutePath().toLowerCase().endsWith(".jar")) {
			try {
				AddonManagerPlugin.getInstance().installAddon(f.getParent(), f.getName());
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return false;
			}
			return true;
		}
		return false;
	}
	
	public void setTopText(final String msg) {
		if (msgPanel != null)
			remove(msgPanel);
		msgPanel = new JLabel(msg != null ? "<html><center>" + msg : "");
		if (msg != null) {
			msgPanel.setOpaque(true);
			msgPanel.setHorizontalAlignment(JLabel.CENTER);
			msgPanel.setBackground(new Color(255, 250, 105));
		}
		add(msgPanel, "0,1,10,1");
		validate();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				validate();
				pack();
				validate();
				msgPanel.setText(msg != null ? "<html><center><br>" + msg + "<br>&nbsp;" : "");
				validate();
				pack();
			}
		});
		repaint();
	}
	
	public void highlightFindUpdatesButton() {
		findUpdatesMarker.setRequestFocus(true);
		findUpdatesMarker.setMark(true);
	}
	
	private void rebuild(String msg, boolean highlightfind) {
		setVisible(false);
		AddonManagerPlugin p = AddonManagerPlugin.getInstance();
		p.showManageAddonDialog(msg, highlightfind);
	}
	
}
