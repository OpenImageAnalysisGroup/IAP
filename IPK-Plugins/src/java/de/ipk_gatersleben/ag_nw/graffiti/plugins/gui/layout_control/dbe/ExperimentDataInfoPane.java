/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.11.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.xml.transform.TransformerException;

import net.iharder.dnd.FileDrop;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FolderPanel;
import org.GuiRow;
import org.JMButton;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemInfo;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.XMLHelper;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;
import org.jdom.JDOMException;
import org.w3c.dom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.XPathHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExperimentDataInfoPane extends JComponent implements SessionListener {
	
	private static final long serialVersionUID = 1L;
	
	// private Document mydoc;
	
	private ExperimentInterface md;
	
	private FolderPanel fpExperimentInfo;
	private FolderPanel fpTimeAndPlants;
	
	private JList jcbFilterTime;
	private JList jcbFilterPlant;
	private String[] times;
	
	private final JTabbedPane parentPane;
	
	private JComponent currentGui;
	
	private JLabel alternativeIDCount;
	
	private JLabel substances;
	
	// /**
	// * Use getDocumentData, if possible.
	// *
	// * @return
	// */
	// @Deprecated
	// private Document getDocument() {
	// return Experiment.getDocuments(md).iterator().next();
	// }
	
	public ExperimentInterface getDocumentData() {
		return md;
	}
	
	public void installDragNDropForAnnotationFiles(final JButton target, final JButton addIdentifiers) {
		target.setToolTipText("Drag & Drop supported");
		final String title = target.getText();
		FileDrop.Listener fdl = new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				if (files != null && files.length > 0) {
					ArrayList<File> mfiles = new ArrayList<File>();
					for (File f : files)
						mfiles.add(f);
					processAnnotationFileLoading(md, addIdentifiers, mfiles);
					// setDocument(doc);
				}
			}
		};
		
		Runnable dragdetected = new Runnable() {
			@Override
			public void run() {
				MainFrame.showMessage(
						"<html><b>Drag &amp; Drop action detected:</b> release mouse button to load annotation data",
						MessageType.PERMANENT_INFO);
				target.setText("<html><br>Drop file to load annotation<br><br>");
			}
		};
		
		Runnable dragenddetected = new Runnable() {
			@Override
			public void run() {
				// MainFrame.showMessage("Drag & Drop action canceled",
				// MessageType.INFO);
				target.setText(title);
			}
		};
		new FileDrop(target, fdl, dragdetected, dragenddetected);
	}
	
	public void installDragNDropForArrayFiles(final JButton target, final JButton addIdentifiers) {
		target.setToolTipText("Drag & Drop supported");
		final String title = target.getText();
		FileDrop.Listener fdl = new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				if (files != null && files.length > 0) {
					ArrayList<File> mfiles = new ArrayList<File>();
					for (File f : files)
						mfiles.add(f);
					processAffyAnnotationFileLoading(md, addIdentifiers, mfiles);
				}
			}
		};
		
		Runnable dragdetected = new Runnable() {
			@Override
			public void run() {
				MainFrame.showMessage("<html><b>Drag &amp; Drop action detected:</b> "
						+ "release mouse button to load array annotation data", MessageType.PERMANENT_INFO);
				target.setText("<html><br>Drop file to load array annotation<br><br>");
			}
		};
		
		Runnable dragenddetected = new Runnable() {
			@Override
			public void run() {
				// MainFrame.showMessage("Drag & Drop action canceled",
				// MessageType.INFO);
				target.setText(title);
			}
		};
		new FileDrop(target, fdl, dragdetected, dragenddetected);
	}
	
	private static ArrayList<AnnotationProvider> annotationProviders = new ArrayList<AnnotationProvider>();
	
	public static void addAnnotationProvider(AnnotationProvider provider) {
		annotationProviders.add(provider);
	}
	
	public ExperimentDataInfoPane(final TableData td, ExperimentInterface doc, final JTabbedPane parentPane,
			final List<ExperimentDataInfoPane> shownExpPanes, final JComponent gui) {
		
		MainFrame.getInstance().getSessionManager().addSessionListener(this);
		
		this.parentPane = parentPane;
		
		// this.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN,
		// SingleFiledLayout.FULL, 5));
		
		double[][] size = new double[][] {
				{ TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
						TableLayout.PREFERRED, 5, TableLayout.FILL } };
		
		this.setLayout(new TableLayout(size));
		
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		
		this.md = doc;
		
		final ExperimentDataInfoPane thisPane = this;
		
		JButton closeTab = new JMButton("<html><small>Close");
		if (SystemInfo.isMac()) {
			closeTab.setText(StringManipulationTools.removeHTMLtags(closeTab.getText()));
			closeTab.putClientProperty("JComponent.sizeVariant", "mini");
		}
		closeTab.setOpaque(false);
		closeTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ExperimentDataInfoPane.this.md = null;
				shownExpPanes.remove(thisPane);
				parentPane.remove(thisPane);
				MainFrame.getInstance().getSessionManager().removeSessionListener(thisPane);
				MainFrame.getInstance().updateActions();
			}
		});
		
		JButton saveXMLdoc = new JMButton("<html><small><center>Export to<br>Filesystem");
		if (SystemInfo.isMac()) {
			saveXMLdoc.putClientProperty("JComponent.sizeVariant", "mini");
		}
		saveXMLdoc.setOpaque(false);
		saveXMLdoc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File xmlFile = OpenFileDialogService.getSaveFile(new String[] { "xlsx", "bin", "xml" }, "Experiment-Data (*.xlsx, *.bin, *.xml)");
				if (xmlFile != null) {
					
					if (xmlFile.getName().toLowerCase().endsWith(".xlsx")) {
						try {
							ExperimentDataFileWriter.writeExcel(xmlFile, md);
							AttributeHelper.showInFileBrowser(xmlFile.getParent(), xmlFile.getName());
						} catch (Exception e1) {
							ErrorMsg.addErrorMessage(e1);
						}
						
					} else
						if (xmlFile.getName().toLowerCase().endsWith(".bin") || xmlFile.getName().toLowerCase().endsWith(".xml")) {
							// XMLOutputter out = new XMLOutputter();
							// OutputStream stream;
							try {
								TextFile.write(xmlFile.toString(), md.toString());
								AttributeHelper.showInFileBrowser(xmlFile.getParent(), xmlFile.getName());
								// stream = new FileOutputStream(xmlFile);
								// org.jdom.Document dd = JDOM2DOM.getJDOMfromDOM(getDocument());
								// out.output(dd, stream);
								// stream.close();
								AttributeHelper.showInFileBrowser(xmlFile.getParent(), xmlFile.getName());
							} catch (Exception e1) {
								ErrorMsg.addErrorMessage(e1);
							}
						} else
							MainFrame
									.showMessageDialog("Invalid file type (file name extension). Please save as *.bin (native), *.xml or *.xlsx", "File Format Error");
				}
			}
		});
		
		JButton showTableData = new JMButton("<html><small><center>Show<br>Input-Form");
		if (SystemInfo.isMac()) {
			showTableData.putClientProperty("JComponent.sizeVariant", "mini");
		}
		
		showTableData.setOpaque(false);
		showTableData.setEnabled(td != null);
		showTableData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				td.showDataDialog();
			}
		});
		
		if (doc == null) {
			this.add(TableLayout.get3Split(closeTab, new JLabel(), showTableData, TableLayout.PREFERRED, TableLayout.FILL,
					TableLayout.PREFERRED), "0,0");
			this
					.add(
							new JLabel(
									"<html>Error: Experiment-Data could not be retrieved from the network or could not be converted from the file source.<br>"
											+ "<br>In case the data has been loaded from the network, you might try loading again in a few minutes. "
											+ "Sometimes database or general network problems prevent the loading of data.<br><br>"
											+ "In case the data has been loaded from file, please check the format of the file.<br>"
											+ "Sometimes cells are in a wrong format (text instead of numbers) and sometimes mandatory"
											+ " fields are not filled."
											+ "<br>"
											+ "You should check the Error-Log with<br>the menu command <i>Help/Error Messages</i> "
											+ "to get more details on the cause of the error.<br><br>"
											+ "Also the <i>Show Input-Form</i> command at the top of this tab pane might help to identify problems."),
							"0,1");
			this.currentGui = null;
		} else {
			fpExperimentInfo = new FolderPanel("Experiment Info", true, true, false, null);
			fpExperimentInfo.layoutRows();
			
			fpTimeAndPlants = new FolderPanel("Specify Mapping-Data", false, true, false, null);
			fpTimeAndPlants.layoutRows();
			
			final JButton replaceIDs = new JMButton("Set Main ID");
			
			replaceIDs.setOpaque(false);
			replaceIDs
					.setToolTipText("Allows to specify the most important ID (name) for a substance,<br>which is used as new node's label text .");
			replaceIDs.addActionListener(replaceSubstanceIdsCommand());
			
			final JButton addIdentifiers = new JMButton("Add Alternative IDs");
			
			addIdentifiers.setOpaque(false);
			replaceIDs.setToolTipText("Allows to specify other names for substances, e.g. \"1438_at\" -> \"EC:2.7.10.1\"");
			addIdentifiers.addActionListener(getAddAlternativeIdentifiersIdsCommand(addIdentifiers));
			
			// final JButton bridgeDB = new JMButton("BRIDGE DB");
			//
			// bridgeDB.setOpaque(false);
			// bridgeDB.addActionListener(getAddAlternativeIdentifiersIdsCommandBRIDGEDB(addIdentifiers));
			
			final JButton addAffyIdentifiers = new JMButton("Load Array-Annotation");
			addAffyIdentifiers.setToolTipText("<html>" + "Load Affymetrix or Agilent Annotation File<br>"
					+ "Processed are Entrez Gene IDs.");
			
			addAffyIdentifiers.setOpaque(false);
			addAffyIdentifiers.addActionListener(getAddAffyIdentifiersCommand(addIdentifiers));
			
			JButton removeIdentifiers = new JMButton("Remove IDs");
			
			removeIdentifiers.setOpaque(false);
			removeIdentifiers.setToolTipText("Allows to remove alternative names for substances");
			removeIdentifiers.addActionListener(getRemoveAnnotationCommand(addIdentifiers));
			
			FolderPanel fpDataAnnotation = new FolderPanel("Identifier Annotation", true, true, false, null);
			
			int cnt = 0;
			for (SubstanceInterface s : doc)
				cnt += s.getSynonyms().size();
			
			alternativeIDCount = new JLabelHTMLlink("Alternative Identifiers: " + cnt, "Click to see all identifiers", new Runnable() {
				@Override
				public void run() {
					TableData td = new TableData();
					int row = 0;
					HashMap<Integer, String> header = new HashMap<Integer, String>();
					for (SubstanceInterface s : ExperimentDataInfoPane.this.md) {
						Collection<String> syns = s.getSynonyms();
						header.put(0, "Main ID");
						td.addCellData(0, row, s.getName());
						int col = 1;
						for (String syn : syns) {
							header.put(col, "Alt. ID " + col);
							td.addCellData(col++, row, syn);
						}
						if (syns.size() <= 0) { // just one substancename/id
							header.put(1, "Alt. ID 1");
							td.addCellData(1, row, s.getName());
						}
						row++;
					}
					td.showDataDialog(header);
				}
			});
			
			fpDataAnnotation.addComp(alternativeIDCount);
			fpDataAnnotation.addComp(TableLayout.get3Split(TableLayout.get3SplitVertical(addIdentifiers,
					addAffyIdentifiers, TableLayout.getMultiSplitVertical(getAnnotationProviderButtons(doc), 0),
					TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED), new JLabel(""),
					TableLayout.getSplitVertical(replaceIDs, removeIdentifiers, TableLayout.PREFERRED, TableLayout.PREFERRED),
					TableLayout.FILL, 5, TableLayout.PREFERRED), 5);
			fpDataAnnotation.layoutRows();
			
			substances = new JLabelHTMLlink("Substances", "Click to see all substance names", new Runnable() {
				@Override
				public void run() {
					TableData td = new TableData();
					int row = 0;
					HashMap<Integer, String> header = new HashMap<Integer, String>();
					for (SubstanceInterface s : ExperimentDataInfoPane.this.md) {
						td.addCellData(0, row++, s.getName());
					}
					td.showDataDialog(header);
				}
			});
			
			installDragNDropForAnnotationFiles(addIdentifiers, addIdentifiers);
			
			installDragNDropForArrayFiles(addAffyIdentifiers, addAffyIdentifiers);
			
			FolderPanel fp5 = new FolderPanel("Analysis Pipeline", true, true, false, null);
			JButton hierarchyWizard = new JMButton("Hierarchy Wizard");
			
			fp5.addComp(hierarchyWizard, 5);
			fp5.layoutRows();
			
			this.add(TableLayout.get3Split(TableLayout.getSplit(new JLabel(), closeTab, TableLayout.FILL,
					TableLayout.PREFERRED), saveXMLdoc, showTableData, TableLayout.FILL, TableLayout.PREFERRED,
					TableLayout.PREFERRED), "0,0");
			
			this.add(TableLayout.get3SplitVertical(null, getActionCommandGUI(), null, 4, TableLayout.PREFERRED, 4), "0,1");
			
			this.add(fpExperimentInfo, "0,2");
			this.add(fpTimeAndPlants, "0,4");
			this.add(fpDataAnnotation, "0,6");
			if (gui != null) {
				this.add(gui, "0,8");
				this.currentGui = gui;
			} else
				this.currentGui = null;
		}
		this.revalidate();
		if (doc != null) {
			updateGUIforUpdatedExperimentData(null, null, null);
		}
	}
	
	private Collection<JComponent> getAnnotationProviderButtons(final ExperimentInterface md) {
		Collection<JComponent> result = new ArrayList<JComponent>();
		for (final AnnotationProvider p : annotationProviders)
			result.add(p.getButton(md, this));
		return result;
	}
	
	private final Collection<MappingButton> dataMappingCommandButtons = new ArrayList<MappingButton>();
	
	protected JComboBox keepremovesubstancesbox;
	
	private JComponent getActionCommandGUI() {
		if (dataMappingCommandButtons.size() > 0)
			ErrorMsg.addErrorMessage("Internal Error: getActionCommandGUI should be called only once!");
		ArrayList<JComponent> buttonCommands = new ArrayList<JComponent>();
		
		for (ExperimentDataProcessor p : ExperimentDataProcessingManager.getExperimentDataProcessors()) {
			if (p instanceof PutIntoSidePanel || !(p instanceof AbstractExperimentDataProcessor))
				continue;
			MappingButton b = new MappingButton((AbstractExperimentDataProcessor) p);
			dataMappingCommandButtons.add(b);
			buttonCommands.add(b);
			final AbstractExperimentDataProcessor pp = (AbstractExperimentDataProcessor) p;
			b.addActionListener(new ActionListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void actionPerformed(ActionEvent e) {
					ExperimentInterface experimentData = getDocumentData();
					
					final boolean applyPlantFilter = jcbFilterPlant.getSelectedValues().length != jcbFilterPlant.getModel()
							.getSize();
					final boolean applyTimeFilter = jcbFilterTime.getSelectedValues().length != jcbFilterTime.getModel()
							.getSize();
					Object[] sel = jcbFilterPlant.getSelectedValues();
					final Collection<String> validNames = new ArrayList<String>();
					for (Object o : sel) {
						String s = (String) o;
						if (s.startsWith("id="))
							s = s.substring(s.indexOf("id=") + "id=".length());
						validNames.add(s);
					}
					final Collection<String> validTimes = new ArrayList<String>();
					sel = jcbFilterTime.getSelectedValues();
					for (Object o : sel)
						validTimes.add((String) o);
					
					if (applyPlantFilter || applyTimeFilter)
						experimentData = experimentData.filter(validNames, validTimes);
					
					pp.setActionEvent(e);
					pp.setExperimentData(experimentData);
					View v;
					try {
						v = MainFrame.getInstance().getActiveEditorSession().getActiveView();
					} catch (Exception err) {
						v = null;
					}
					if (pp.activeForView(v))
						GravistoService.getInstance().runAlgorithm(pp, e);
				}
			});
		}
		updateButtonStatus();
		return TableLayout.getMultiSplitVertical(buttonCommands, 5);
	}
	
	private void updateButtonStatus() {
		Session s;
		try {
			s = MainFrame.getInstance().getActiveSession();
		} catch (Exception e) {
			s = null;
		}
		sessionChanged(s);
	}
	
	private ActionListener getRemoveAnnotationCommand(final JButton addIdentifiers) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object[] res = MyInputHelper.getInput("<html>"
						+ "By leaving the search text empty, all alternative identifiers besides the main ID will<br>"
						+ "be removed. If you specify search text, alternative identifiers which contain the given<br>"
						+ "search text will be removed.", "Alternative Identifiers Removal", new Object[] { "Search Text",
						"", "Remove Alternative IDs which do NOT contain search text", false,
						"Remove Substances with no alternative IDs", false });
				if (res == null) {
					MainFrame.showMessageDialog("Alternative identifiers remain unchanged.", "Information");
					return;
				}
				String search = (String) res[0];
				boolean all = search.length() <= 0;
				boolean invertSearch = (Boolean) res[1];
				boolean removeEmpty = (Boolean) res[2];
				ArrayList<SubstanceInterface> removeTheseSubstances = new ArrayList<SubstanceInterface>();
				int workCnt = 0;
				for (SubstanceInterface xmlSubstanceNode : md) {
					Set<Integer> delSynIdx = new LinkedHashSet<Integer>();
					if (all) {
						workCnt += xmlSubstanceNode.getSynonyms().size();
						xmlSubstanceNode.clearSynonyms();
					} else {
						for (Entry<Integer, String> e : xmlSubstanceNode.getSynonymMap().entrySet())
							if (invertSearch) {
								if (e.getValue() != null && !e.getValue().contains(search))
									delSynIdx.add(e.getKey());
							} else {
								if (e.getValue() != null && e.getValue().contains(search))
									delSynIdx.add(e.getKey());
							}
						for (Integer idx : delSynIdx) {
							xmlSubstanceNode.getSynonymMap().remove(idx);
							workCnt++;
						}
					}
					if (removeEmpty && xmlSubstanceNode.getSynonyms().size() <= 0)
						removeTheseSubstances.add(xmlSubstanceNode);
				}
				for (SubstanceInterface dels : removeTheseSubstances)
					md.remove(dels);
				
				MainFrame.showMessageDialog("<html>" + "Alternative substance identifiers (" + workCnt
						+ ") have been removed.<br>" + removeTheseSubstances.size()
						+ " substance datasets have been removed. ", "Information");
				if (removeTheseSubstances.size() > 0)
					updateGUIforUpdatedExperimentData(null, null, null);
				// addIdentifiers.setText("Additional Identifiers (0)");
				refreshAnnotationCounter();
			}
		};
	}
	
	public void refreshAnnotationCounter() {
		refreshAnnotationCounter(null);
	}
	
	public void refreshAnnotationCounter(String txt) {
		int cnt = 0;
		for (SubstanceInterface s : md)
			cnt += s.getSynonyms().size();
		
		((JLabelHTMLlink) alternativeIDCount).setLabelText("Alternative Identifiers: " + cnt + (txt != null ? txt : ""));
		alternativeIDCount.validate();
		alternativeIDCount.repaint();
	}
	
	private ActionListener getAddAlternativeIdentifiersIdsCommand(final JButton addIdentifiers) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Collection<File> excelFiles = OpenExcelFileDialogService.getExcelOrAnnotationFiles();
				processAnnotationFileLoading(md, addIdentifiers, excelFiles);
			}
		};
	}
	
	// public static Runnable bridgeDBrunner;
	//
	// private ActionListener getAddAlternativeIdentifiersIdsCommandBRIDGEDB(final JButton addIdentifiers) {
	// return new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// bridgeDBrunner.run();
	// }
	// };
	// }
	
	private ActionListener getAddAffyIdentifiersCommand(final JButton addIdentifiers) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Collection<File> excelFiles = OpenExcelFileDialogService.getAffyOrAgilAnnotationFiles();
				processAffyAnnotationFileLoading(md, addIdentifiers, excelFiles);
			}
		};
	}
	
	private ActionListener replaceSubstanceIdsCommand() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int maxID = 0;
				HashMap<Integer, String> exampleValues = new HashMap<Integer, String>();
				for (SubstanceInterface xmlSubstanceNode : md) {
					HashMap<Integer, String> temp = xmlSubstanceNode.getSynonymMap();
					if (temp != null) {
						exampleValues.putAll(temp);
						for (Integer idx : temp.keySet())
							if (idx > maxID)
								maxID = idx;
					}
				}
				if (maxID == 0) {
					MainFrame.showMessageDialog("No alternative identifiers available!", "No data available");
				} else {
					ArrayList<String> selvals = new ArrayList<String>();
					for (int i = 0; i <= maxID; i++) {
						String s = "" + i;
						String example = exampleValues.get(new Integer(i));
						if (example != null)
							s += " (e.g. " + example + ")";
						selvals.add(s);
					}
					Object result = JOptionPane.showInputDialog(MainFrame.getInstance(),
							"<html>Select the alternative identifier index which will be used to replace the current<br>"
									+ "substance name of the measurements (0 is the default value):", "Select Identifier",
							JOptionPane.QUESTION_MESSAGE, null, selvals.toArray(), null);
					if (result == null) {
						MainFrame.showMessageDialog("No value selected, substance IDs remain unchanged.", "Information");
					} else {
						int workCnt = 0;
						String number = (String) result;
						if (number.contains(" ")) {
							number = number.substring(0, number.indexOf(" "));
							number = number.trim();
						}
						int idx = Integer.parseInt(number);
						for (SubstanceInterface xmlSubstanceNode : md) {
							if (idx < xmlSubstanceNode.getSynonyms().size()) {
								xmlSubstanceNode.setName(xmlSubstanceNode.getSynonyme(idx));
								workCnt++;
							}
						}
						MainFrame.showMessageDialog("Replaced " + workCnt + " substance names with alternative identifiers!",
								"Information");
					}
				}
			}
		};
	}
	
	// @Deprecated
	// protected void setDocument(Document doc) {
	// md = Experiment.getExperimentFromDOM(doc);
	// }
	
	public String getExperimentName() {
		try {
			String name = md.getName();
			return name != null ? name : "null";
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return "null";
		}
	}
	
	public synchronized void updateGUIforUpdatedExperimentData(String experimentName, final ExperimentInterface mdNew,
			JComponent gui) {
		if (mdNew != null)
			md = mdNew;
		if (experimentName != null) {
			try {
				int idx = parentPane.indexOfComponent(this);
				if (idx >= 0)
					parentPane.setTitleAt(idx, "<html>&nbsp;&nbsp;" + experimentName);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		if (gui != null) {
			try {
				if (currentGui != null)
					this.remove(currentGui);
				this.add(gui, "0,8");
				currentGui = gui;
				validate();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		
		for (JMButton b : dataMappingCommandButtons)
			b.setEnabled(false);
		
		fpExperimentInfo.clearGuiComponentList();
		fpExperimentInfo.addComp(new JLabel("<html>Analyze Dataset..."));
		fpExperimentInfo.layoutRows();
		fpTimeAndPlants.clearGuiComponentList();
		fpTimeAndPlants.layoutRows();
		revalidate();
		final ArrayList<JComponent> components = new ArrayList<JComponent>();
		final ArrayList<JComponent> timecomponents = new ArrayList<JComponent>();
		
		final ArrayList<Boolean> onePlant = new ArrayList<Boolean>();
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Analyze Dataset", "XPATH query");
		BackgroundTaskHelper.issueSimpleTask("Post-Processing", "XPATH query", new Runnable() {
			
			@Override
			public void run() {
				ExperimentInterface doc = md;
				status.setCurrentStatusText2("Get Experiment-Name...");
				components.add(new JLabel("<html>Experiment-Name: " + doc.getName()));
				updateView(components, timecomponents, onePlant);
				status.setCurrentStatusText2("Get Remark...");
				if (doc.getRemark() != null && doc.getRemark().length() > 0)
					components.add(new JLabel("<html>Remark: " + doc.getRemark()));
				updateView(components, timecomponents, onePlant);
				status.setCurrentStatusText2("Get Coordinator...");
				if (doc.getCoordinator() != null && doc.getCoordinator().length() > 0)
					components.add(new JLabel("<html>Coordinator: " + doc.getCoordinator()));
				updateView(components, timecomponents, onePlant);
				status.setCurrentStatusText2("Get Number of Measurement Values...");
				components.add(new JLabel("<html>Measurement values: " + md.getNumberOfMeasurementValues()));
				updateView(components, timecomponents, onePlant);
				status.setCurrentStatusText2("Get Sum of Measurement Values...");
				components.add(new JLabel("<html>Measurement values-sum: " + md.getMeasurementValuesSum()));
				updateView(components, timecomponents, onePlant);
				status.setCurrentStatusText2("Get Import Time...");
				if (doc.getImportDate() != null)
					components.add(new JLabel("<html>Import time: " + AttributeHelper.getDateString(doc.getImportDate())));
				updateView(components, timecomponents, onePlant);
				status.setCurrentStatusText2("Get Start Date...");
				if (doc.getStartDate() != null)
					components.add(new JLabel("<html>Experiment started: " + AttributeHelper.getDateString(doc.getStartDate())));
				
				updateView(components, timecomponents, onePlant);
				status.setCurrentStatusText2("Get Sample Time Values...");
				times = Experiment.getTimes(md);
				jcbFilterTime = new JList(times == null ? new String[] { "Error" } : times);
				jcbFilterTime.setSelectionInterval(0, times.length - 1);
				jcbFilterTime.setVisibleRowCount(times.length > 5 ? 5 : times.length);
				timecomponents.add(TableLayout.getSplitVertical(new JLabel("Time Points"), new JScrollPane(jcbFilterTime),
						TableLayout.PREFERRED, jcbFilterTime.getPreferredScrollableViewportSize().getHeight() + 5));
				updateView(components, timecomponents, onePlant);
				status.setCurrentStatusText2("Analyze Conditions...");
				String[] plants = Experiment.getConditionsAsString(md);
				
				onePlant.add(plants != null && plants.length == 1);
				
				jcbFilterPlant = new JList(plants == null ? new String[] { "Error" } : plants);
				jcbFilterPlant.setSelectionInterval(0, plants.length - 1);
				jcbFilterPlant.setVisibleRowCount(plants.length > 5 ? 5 : plants.length);
				timecomponents.add(TableLayout.getSplitVertical(new JLabel("Genotypes/Conditions"), new JScrollPane(
						jcbFilterPlant), TableLayout.PREFERRED, jcbFilterPlant.getPreferredScrollableViewportSize()
						.getHeight() + 5));
				
				status.setCurrentStatusText2("Looking for substance names...");
				timecomponents.add(TableLayout.getSplitVertical(substances, getFilterSubstancesBT(),
						TableLayout.PREFERRED, TableLayout.PREFERRED));
				refreshSubstanceCount();
				
				status.setCurrentStatusText2("Looking for missing replicates...");
				if (Experiment.isReplicateDataMissing(md))
					timecomponents.add(TableLayout.getSplitVertical(new JLabel("Replicate data not available"),
							getStdDevProcessingButton(), TableLayout.PREFERRED, TableLayout.PREFERRED));
				
				status.setCurrentStatusText2("Looking for technical and biological replicates...");
				if (Experiment.isBiologicalAndTechnicalReplicateDataAvailable(md))
					timecomponents.add(TableLayout.getSplitVertical(new JLabel("Biological Replicate data available"),
							getReplicateProcessingButton(), TableLayout.PREFERRED, TableLayout.PREFERRED));
				
				status.setCurrentStatusText2("Looking for annotations...");
				refreshAnnotationCounter();
				
				status.setCurrentStatusText2("Processing finished");
				
			}
		}, new Runnable() {
			@Override
			public void run() {
				updateView(components, timecomponents, onePlant);
				
				validateXML();
				
				updateButtonStatus();
			}
			
		}, status);
	}
	
	protected JComponent getFilterSubstancesBT() {
		JButton bt = new JButton("Remove substances from experiment");
		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				keepremovesubstancesbox = new JComboBox(new Object[] { "Keep Substances", "Remove Substances" });
				Object[] list = new Object[] {
						"", keepremovesubstancesbox,
						"", new JPanel(),
						"", getFilterByGraphButton(),
						"", getFilterByFileButton(),
				};
				MyInputHelper.getInput("[Close]", "Remove Substances", list);
			}
		});
		return bt;
	}
	
	protected JComponent getFilterByGraphButton() {
		JButton bt = new JButton("which occur in Graph Labels");
		bt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				EditorSession es = MainFrame.getInstance().getActiveEditorSession();
				if (es == null || es.getGraph() == null)
					MainFrame.showMessageDialog("No graph open to be able to get graph labels", "error");
				else {
					HashSet<String> acceptedSubstancenames = new HashSet<String>();
					for (Node nd : GraphHelper.getSelectedOrAllNodes())
						for (String lbl : AttributeHelper.getLabels(nd, true))
							acceptedSubstancenames.addAll(AttributeHelper.getFuzzyLabels(lbl));
					filterSubstances(acceptedSubstancenames);
				}
			}
		});
		return bt;
	}
	
	protected void filterSubstances(HashSet<String> acceptedSubstancenames) {
		int size = md.size();
		
		ArrayList<SubstanceInterface> keepSubstances = new ArrayList<SubstanceInterface>();
		ArrayList<SubstanceInterface> removeSubstances = new ArrayList<SubstanceInterface>();
		boolean invert = keepremovesubstancesbox.getSelectedItem().equals("Remove Substances");
		for (SubstanceInterface sub : md) {
			if ((!invert && acceptedSubstancenames.contains(sub.getName())) || (invert && !acceptedSubstancenames.contains(sub.getName())))
				keepSubstances.add(sub);
			else
				removeSubstances.add(sub);
		}
		
		// the problem is here that we cant call removeAll(removeSubstances), because for each substance we go through the whole arraylist and ask for equality,
		// which is implemented as putting some strings together. this is so fucking slow that we here refill the list of substances without any equals (we are
		// just interested in the substance names at this stage)
		
		md.clear();
		for (SubstanceInterface sub : keepSubstances)
			md.add(sub);
		refreshSubstanceCount();
		
		if (size - md.size() <= 0)
			MainFrame.showMessageDialog("No substance were filtered out", "Filtering Complete");
		else {
			boolean tooLarge = removeSubstances.size() > 1000;
			String msg = "<html>Filtered out " + removeSubstances.size() + "/" + size + " substances" + (tooLarge ? " showing only first 1000)" : "") + ":<br>";
			
			for (int i = 0; (i < 1000 || i >= removeSubstances.size()); i++)
				msg += "<br>" + removeSubstances.get(i).getName();
			if (tooLarge)
				msg += "<br>...";
			
			MainFrame.showMessageDialogWithScrollBars(msg, "Filtering Complete");
		}
		
	}
	
	protected JComponent getFilterByFileButton() {
		JButton bt = new JButton("which occur in Filelist");
		
		new FileDrop(bt, new LineBorder(new Color(0, 0, 200, 100), 5), false, new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				ArrayList<File> goodFiles = new ArrayList<File>();
				for (File f : files)
					for (String ext : OpenExcelFileDialogService.EXCEL_OR_BINARY_EXTENSION)
						if (f.getName().toLowerCase().endsWith(ext.toLowerCase()))
							goodFiles.add(f);
				for (File f : goodFiles)
					filterSubstances(getFilterListFromFile(f));
			}
			
		});
		bt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Collection<File> files = OpenExcelFileDialogService.getExcelOrAnnotationFiles();
				if (files != null && files.size() > 0)
					for (File f : files)
						if (f != null)
							filterSubstances(getFilterListFromFile(f));
			}
		});
		return bt;
	}
	
	protected HashSet<String> getFilterListFromFile(File f) {
		HashSet<String> acceptedSubstances = new HashSet<String>();
		TableData td = ExperimentDataFileReader.getExcelTableData(f);
		for (int i = 1; i <= td.getMaximumRow(); i++) {
			String cell = td.getCellData(1, i, "") + "";
			if (cell != null && cell.length() > 0)
				acceptedSubstances.add(cell);
		}
		return acceptedSubstances;
	}
	
	protected void validateXML() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (fpExperimentInfo.isVisible()) {
					final String result = "Show XML";
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JLabel lbl = new JLabel("<html><font color='gray'>" + result);
							lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							lbl.addMouseListener(new MouseListener() {
								@Override
								public void mouseReleased(MouseEvent e) {
								}
								
								@Override
								public void mousePressed(MouseEvent e) {
								}
								
								@Override
								public void mouseExited(MouseEvent e) {
								}
								
								@Override
								public void mouseEntered(MouseEvent e) {
								}
								
								@Override
								public void mouseClicked(MouseEvent e) {
									showXMLdata(md);
								}
							});
							fpExperimentInfo.addGuiComponentRow(new GuiRow(null, lbl), true);
						}
					});
				}
			}
		});
		t.setName("VANTED XML Validation");
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	private synchronized void updateView(ArrayList<JComponent> components, ArrayList<JComponent> timecomponents,
			ArrayList<Boolean> onePlant) {
		final ArrayList<JComponent> fComponents = new ArrayList<JComponent>(components);
		final ArrayList<JComponent> fTimecomponents = new ArrayList<JComponent>(timecomponents);
		// final ArrayList<Boolean> fOnePlant = new ArrayList<Boolean>(onePlant);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ArrayList<JComponent> cc = new ArrayList<JComponent>(fComponents);
				fpExperimentInfo.clearGuiComponentList();
				for (JComponent jc : cc)
					fpExperimentInfo.addComp(jc);
				fpExperimentInfo.layoutRows();
				
				fpTimeAndPlants.clearGuiComponentList();
				for (JComponent jc : fTimecomponents)
					fpTimeAndPlants.addComp(jc);
				fpTimeAndPlants.layoutRows();
				
				validate();
			}
		});
	}
	
	private JComponent getStdDevProcessingButton() {
		JButton result = new JMButton("Process standard deviation data");
		
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.showMessageDialog("<html>"
						+ "While it is recommended to add all replicate data into the data-sets,<br>"
						+ "in some situations this is not possible. In case there are pre-computed<br>"
						+ "standard deviation values available, these may be added directly in the<br>"
						+ "dataset as measurement values. To differentiate between &quot;normal&quot;<br>"
						+ "measurement values and standard deviation data, these values need to be<br>"
						+ "specially named: These values need to be added with a substance-name<br>"
						+ "ending with &quot;-STDDEV&quot;. This means for example, if you have<br>"
						+ "measuremen values, identified by their substance name &quot;Water&quot;,<br>"
						+ "you should add the corresponding standard deviation data to the dataset<br>"
						+ "identified by the name &quot;Water-STDDEV&quot;.<br><br>"
						+ "This command recognizes samples named ending with the tag<br>"
						+ "&quot;-STDDEV&quot; and adds the standard deviation data to the<br>"
						+ "corresponding measurement values. The corresponding measurement values<br>"
						+ "are identified by their substance name, replicate ID and time point.<br>"
						+ "The standard deviation substance samples, which are meant not to be <br>"
						+ "substance samples, as they are just a annotation to the &quot;real&quot;<br>"
						+ "measurement values, are removed.<br><br>" + "The processing will continue as you proceed.",
						"Information");
				Document doc = Experiment.getDocuments(md).iterator().next();
				int transformed = XPathHelper.processAvailableStdDevSubstanceData(doc, "-STDDEV");
				if (transformed > 0) {
					md = new Experiment(doc);
					MainFrame.showMessage(" Standard deviation set on " + transformed + " samples", MessageType.INFO);
				} else
					MainFrame.showMessageDialog("<html>All sample data remained unchanged.<br>"
							+ "No corresponding data found (check help on this feature)", "Error");
			}
		});
		return result;
	}
	
	private JComponent getReplicateProcessingButton() {
		JButton result = new JMButton("Merge Biological Replicates");
		result.setToolTipText("<html>Biological replicates are determined by checking, if there are equal Genotypes/Conditions specified.<br>" +
				"If you choose to merge this data, the technical replicates will be merged into one value and the values<br>" +
				"of all equal Genotypes/Conditions treated as replicates, therefor removing double Genotypes/Conditions.");
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
						"Merging Biological Replicates", "Please wait...");
				BackgroundTaskHelper.issueSimpleTask("Merging Biological Replicates", "Please wait...", new Runnable() {
					@Override
					public void run() {
						md.mergeBiologicalReplicates(status);
					}
				}, new Runnable() {
					@Override
					public void run() {
						TabDBE.addOrUpdateExperimentPane(new ProjectEntity(md.getName(), md, null));
					}
				}, status);
			}
		});
		return result;
	}
	
	private void processAnnotationFileLoading(final ExperimentInterface md, final JButton addIdentifiers,
			final Collection<File> excelFiles) {
		if (excelFiles != null && excelFiles.size() > 0) {
			boolean splitU = false;
			String splitCharU = null;
			boolean tagRemoveU = false;
			String startU = null;
			String endU = null;
			
			boolean skipFirstRowU = false;
			
			ArrayList<String> modes = new ArrayList<String>();
			final String mode1 = "1:1 match / Match current Main ID with reference IDs (col 1)";
			final String mode2 = "n:1 match / Match main and alternative IDs with reference IDs (col 1)";
			final String mode3 = "m:n match / Match main and alternative IDs with IDs from all columns";
			modes.add(mode1);
			modes.add(mode2);
			modes.add(mode3);
			
			String mode;
			
			Object[] res = MyInputHelper.getInput(StringManipulationTools.getWordWrap(new String[] {
					"<html>",
					"The alternative identifier table needs to contain the "
							+ "reference IDs in column A, alternative identifiers in " + "the following colum(s).",
					"<br>",
					"Multiple alternative ID for one main ID may be " + "specified as several columns or in several rows.",
					"<br><br>",
					"In case the corresponding setting is enabled, the "
							+ "cells content for the definition of alternative "
							+ "identifiers may be split. In this case the column " + "indices are not retained.",
					"<br>",
					"Additionally, parts of the identifier text may be "
							+ "automatically removed. With the default settings "
							+ "a identifier value &quot;A (remark), B (remark)&quot; will be "
							+ "transformed and split: cell 1: &quot;A&quot;, cell 2: &quot;B&quot;. "
							+ "In case of splitting or removal of text content, white "
							+ "space at the start or end is removed.", "<br><br>" }, 80), "Split Cells", new Object[] {
					"Mode of Operation", modes, "Enable Cell Splitting", false, "Split-Char", ",", "Remove Tags", false,
					"Start", "(", "End", ")", "Skip First Row", false });
			
			if (res == null) {
				MainFrame.showMessage("Processing aborted!", MessageType.INFO);
				return;
			} else {
				int idx = 0;
				mode = (String) res[idx++];
				splitU = (Boolean) res[idx++];
				splitCharU = (String) res[idx++];
				if (splitCharU == null || splitCharU.length() <= 0)
					splitU = false;
				tagRemoveU = (Boolean) res[idx++];
				startU = (String) res[idx++];
				endU = (String) res[idx++];
				if (startU == null)
					startU = "";
				if (endU == null)
					endU = "";
				if (startU.length() <= 0 && endU.length() <= 0)
					tagRemoveU = false;
				skipFirstRowU = (Boolean) res[idx++];
				idx++; // affy hint
			}
			final String modeOfOperation = mode;
			final boolean split = splitU;
			final String splitChar = splitCharU;
			final boolean tagRemove = tagRemoveU;
			final String start = startU;
			final String end = endU;
			final boolean skipFirstRow = skipFirstRowU;
			
			final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
					"Process Data", "Please wait...");
			BackgroundTaskHelper.issueSimpleTask("Additional Identifiers", "Process Data", new Runnable() {
				@Override
				public void run() {
					ArrayList<StringBuilder> statusMessages = new ArrayList<StringBuilder>();
					int i = 0;
					int max = excelFiles.size();
					int result = 0;
					for (File excelFile : excelFiles) {
						status.setCurrentStatusValueFine(100d * i / max);
						i++;
						status.setCurrentStatusText1("Read Table " + i + "/" + max + " (" + excelFile.getName() + ")...");
						TableData myData = ExperimentDataFileReader.getExcelTableData(excelFile);
						if (split)
							myData.splitCells(splitChar);
						HashSet<Integer> ignoreColumns = new HashSet<Integer>();
						// myData.showDataDialog();
						if (tagRemove)
							myData.processCellContentRemoveStringTags(start, end);
						// myData.showDataDialog();
						if (status.wantsToStop()) {
							// addIdentifiers.setText("Additional Identifiers ("+result+") (processing aborted)");
							refreshAnnotationCounter(" (processing aborted)");
							MainFrame.showMessageDialog("<html>"
									+ "Processing incomplete. Additional identifiers have not be added, previous<br>"
									+ "attached have been removed.", "Information");
							return;
						}
						status.setCurrentStatusValueFine(60); // 50 --> 60%
						if (tagRemove || split)
							myData.showDataDialog();
						status.setCurrentStatusText1("Process Table Data " + i + "/" + max + " (" + excelFile.getName()
								+ ")...");
						StringBuilder statusMessage = new StringBuilder();
						statusMessage.append("Processed Table Data " + i + "/" + max + " (" + excelFile.getName() + "):<br>");
						statusMessages.add(statusMessage);
						boolean matchAllExisting = false;
						boolean matchAllNew = false;
						if (modeOfOperation.equals(mode1)) {
							matchAllExisting = false;
							matchAllNew = false;
						}
						if (modeOfOperation.equals(mode2)) {
							matchAllExisting = true;
							matchAllNew = false;
						}
						if (modeOfOperation.equals(mode3)) {
							matchAllExisting = true;
							matchAllNew = true;
						}
						result += AlternativeIdentifierTableData.processAdditionaldentifiers(matchAllExisting, matchAllNew,
								myData, md, status, 60, 100, statusMessage, skipFirstRow, ignoreColumns);
						// addIdentifiers.setText("Additional Identifiers ("+result+") (still processing)");
						refreshAnnotationCounter(result + " (still processing)");
					}
					status.setCurrentStatusValueFine(100d);
					StringBuilder statusMsg = new StringBuilder();
					for (StringBuilder s : statusMessages)
						statusMsg.append("<li>" + s.toString());
					// addIdentifiers.setText("Additional Identifiers ("+result+")");
					refreshAnnotationCounter(result + "");
					MainFrame.showMessageDialogWithScrollBars("<html>"
							+ "Additional identifiers from the mapping table have been added to the dataset.<br>"
							+ "Existing identifiers (if available) have not been removed. Status-Message(s):<br>" + "<ul>"
							+ statusMsg + "</ul>", "Information");
				}
			}, null, status);
		}
	}
	
	private void processAffyAnnotationFileLoading(final ExperimentInterface md, final JButton addIdentifiers,
			final Collection<File> excelFiles) {
		if (excelFiles != null && excelFiles.size() > 0) {
			// boolean processAffyGOu = false;
			// boolean processAffyEntrezU = false;
			//
			// ArrayList<String> modes = new ArrayList<String>();
			// // final String mode1
			// ="1:1 match / Match current Main ID with reference IDs (col 1)";
			// // final String mode2 =
			// "n:1 match / Match main and alternative IDs with reference IDs (col 1)";
			// // modes.add(mode1);
			// // modes.add(mode2);
			// //
			// // String mode;
			//
			// Object[] res = MyInputHelper.getInput(
			// ErrorMsg.getWordWrap(new String[] {
			// "<html>",
			// "Affymetrix CSV annotation file processing:"}, 80) ,
			// "Split Cells",
			// new Object[] {
			// "Mode of Operation", modes,
			// // "Process GO Annotation", true,
			// "<html>" +
			// "Process Entrez Gene C.<br>" +
			// "<small>" +
			// "(if access is enabled, KEGG<br>" +
			// "SOAP API is used to determine<br>" +
			// "organism code)", true
			// });
			//
			// if (res==null) {
			// MainFrame.showMessage("Processing aborted!", MessageType.INFO);
			// return;
			// } else {
			// int idx = 0;
			// mode = (String)res[idx++];
			// processAffyGOu = false; // (Boolean)res[idx++];
			// processAffyEntrezU = (Boolean)res[idx++];
			// }
			// final String modeOfOperation = mode;
			final boolean split = true;
			final String splitChar = "///";
			final boolean tagRemove = false;
			final String start = "";
			final String end = "";
			final boolean skipFirstRow = true;
			// final boolean processAffyGO = processAffyGOu;
			// final boolean processAffyEntrez = processAffyEntrezU;
			
			final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
					"Process Data", "Please wait...");
			BackgroundTaskHelper.issueSimpleTask("Additional Identifiers", "Process Data", new Runnable() {
				@Override
				public void run() {
					ArrayList<StringBuilder> statusMessages = new ArrayList<StringBuilder>();
					int i = 0;
					int max = excelFiles.size();
					int result = 0;
					for (File excelFile : excelFiles) {
						status.setCurrentStatusValueFine(100d * i / max);
						i++;
						status.setCurrentStatusText1("Read Table " + i + "/" + max + " (" + excelFile.getName() + ")...");
						TableData myData;
						if (excelFile.getName().endsWith(".xls") || excelFile.getName().endsWith(".xlsx"))
							myData = ExperimentDataFileReader.getExcelTableData(excelFile, TableData
									.getRelevantAffymetrixAnnotationColumnHeaders());
						else
							myData = ExperimentDataFileReader.getCSVdata(excelFile, TableData
									.getRelevantAffymetrixAnnotationColumnHeaders(), status);
						HashSet<Integer> ignoreColumns = new HashSet<Integer>();
						boolean processAffyEntrez = true;
						boolean processAffyGO = false;
						if (processAffyEntrez || processAffyGO)
							ignoreColumns = myData.processAffymetrixAnnotationColumns(processAffyGO, processAffyEntrez);
						if (split)
							myData.splitCells(splitChar);
						if (tagRemove)
							myData.processCellContentRemoveStringTags(start, end);
						if (status.wantsToStop()) {
							// addIdentifiers.setText("Additional Identifiers ("+result+") (processing aborted)");
							refreshAnnotationCounter(" (processing aborted)");
							MainFrame.showMessageDialog("<html>"
									+ "Processing incomplete. Additional identifiers have not be added, previous<br>"
									+ "attached have been removed.", "Information");
							return;
						}
						status.setCurrentStatusValueFine(60); // 50 --> 60%
						if (tagRemove || split)
							myData.showDataDialog();
						status.setCurrentStatusText1("Process Table Data " + i + "/" + max + " (" + excelFile.getName()
								+ ")...");
						StringBuilder statusMessage = new StringBuilder();
						statusMessage.append("Processed Table Data " + i + "/" + max + " (" + excelFile.getName() + "):<br>");
						statusMessages.add(statusMessage);
						boolean matchAllExisting = false;
						boolean matchAllNew = false;
						// if (modeOfOperation.equals(mode1)) {
						matchAllExisting = false;
						matchAllNew = false;
						// }
						// if (modeOfOperation.equals(mode2)) {
						// matchAllExisting = true;
						// matchAllNew = false;
						// }
						result += AlternativeIdentifierTableData.processAdditionaldentifiers(matchAllExisting, matchAllNew,
								myData, md, status, 60, 100, statusMessage, skipFirstRow, ignoreColumns);
						// addIdentifiers.setText("Additional Identifiers ("+result+") (still processing)");
						refreshAnnotationCounter(result + " (still processing)");
					}
					status.setCurrentStatusValueFine(100d);
					StringBuilder statusMsg = new StringBuilder();
					for (StringBuilder s : statusMessages)
						statusMsg.append("<li>" + s.toString());
					// addIdentifiers.setText("Additional Identifiers ("+result+")");
					refreshAnnotationCounter(result + "");
					MainFrame.showMessageDialogWithScrollBars("<html>"
							+ "Additional identifiers from the mapping table have been added to the dataset.<br>"
							+ "Existing identifiers (if available) have not been removed. Status-Message(s):<br>" + "<ul>"
							+ statusMsg + "</ul>", "Information");
				}
			}, null, status);
		}
	}
	
	@Override
	public void sessionChanged(Session s) {
		View v = s != null ? s.getActiveView() : null;
		for (MappingButton b : dataMappingCommandButtons) {
			boolean active = b.getExperimentProcessor().activeForView(v);
			b.setEnabled(active);
			b.setText(b.getExperimentProcessor().getName());
		}
	}
	
	@Override
	public void sessionDataChanged(Session s) {
		sessionChanged(s);
	}
	
	private void refreshSubstanceCount() {
		((JLabelHTMLlink) substances).setLabelText(md.size() + " Substances");
		refreshAnnotationCounter();
	}
	
	public static void showXMLdata(final ExperimentInterface md) {
		
		if (((Experiment) md).getAllMeasurements().size() > 50000) {
			Object[] res = MyInputHelper.getInput("<html>[Show anyway;Cancel]The experiment contains more than 50000 measurement values.<br>" +
					"It is not adviced to show such large datasets as xml.",
					"Warning",
					new Object[] {});
			if (res == null)
				return;
		}
		
		BackgroundTaskStatusProviderSupportingExternalCallImpl status =
				new BackgroundTaskStatusProviderSupportingExternalCallImpl("Generating XML", "of experiment \"" + md.getName() + "\"");
		BackgroundTaskHelper.issueSimpleTask("Generating XML", "Generating XML of \"" + md.getName(), new Runnable() {
			
			@Override
			public void run() {
				boolean ok = false;
				Document document = Experiment.getDocuments(md).iterator().next();
				String validationResult = "";
				try {
					Substance.validate(document);
					ok = true;
				} catch (Exception err) {
					validationResult = err.getMessage();
				}
				String xml = "";
				try {
					if (!ok)
						xml += validationResult + "\n\n";
					xml += document != null ? XMLHelper.getOuterXmlPretty(document) : "(XML == null)";
				} catch (IOException e1) {
					xml = "(" + e1.getMessage() + ")";
				} catch (TransformerException e1) {
					xml = "(" + e1.getMessage() + ")";
				} catch (JDOMException e1) {
					xml = "(" + e1.getMessage() + ")";
				}
				final String xmlf = xml;
				final boolean okf = ok;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JEditorPane jep = new JEditorPane("text/plain", xmlf);
						MainFrame.showMessageDialog((okf ? "XML XSD validation successful!" : "XML invalid"), new JScrollPane(jep));
					}
				});
			}
		}, null, status);
	}
}