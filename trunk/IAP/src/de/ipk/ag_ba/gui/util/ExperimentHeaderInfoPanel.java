/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 1, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ErrorMsg;
import org.FolderPanel;
import org.GuiRow;
import org.JMButton;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.toedter.calendar.JDateChooser;

import de.ipk.ag_ba.commands.experiment.process.ExperimentAnalysisSettingsIOprovder;
import de.ipk.ag_ba.commands.vfs.ActionDataExportToVfs;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemHandler;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.interfaces.RunnableWithExperimentInfo;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.dc.DCexperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataInfoPane;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class ExperimentHeaderInfoPanel extends JPanel {
	private static String CANCEL = "Cancel";
	
	private static final long serialVersionUID = 1L;
	
	JTextField editName;
	JTextField editDBid;
	JTextField coordinator;
	JTextField groupVisibility;
	JComboBox experimentTypeSelection;
	JDateChooser expStart;
	JDateChooser expEnd;
	JTextField remark;
	JTextField outliers;
	JLabel annotation;
	JButton annotationB;
	JTextField sequence;
	
	private RunnableWithExperimentInfo saveAction;
	
	private ExperimentReference experimentReference;
	
	public ExperimentHeaderInfoPanel() {
		// empty
	}
	
	public ExperimentHeaderInfoPanel(final boolean startEnabled, final ExperimentReference experimentReference) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				BackgroundTaskHelper.executeLaterOnSwingTask(0, new Runnable() {
					@Override
					public void run() {
						if (ExperimentHeaderInfoPanel.this.isVisible())
							setExperimentInfo(
									experimentReference.m,
									experimentReference.getHeader(),
									startEnabled,
									experimentReference.getExperiment());
					}
				});
			}
		};
		this.experimentReference = experimentReference;
		experimentReference.runAsDataBecomesAvailable(r);
	}
	
	/**
	 * @param experiment
	 * @return
	 */
	private JComponent getShowDataButton(final ExperimentInterface experiment) {
		final JMButton res = new JMButton("Generate and show XML");
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				res.setText("Prepare document... please wait");
				BackgroundTaskHelper.executeLaterOnSwingTask(5, new Runnable() {
					@Override
					public void run() {
						ExperimentDataInfoPane.showXMLdata(experiment);
					}
				});
				BackgroundTaskHelper.executeLaterOnSwingTask(3000, new Runnable() {
					@Override
					public void run() {
						res.setText("This may take a few moments!");
					}
				});
				BackgroundTaskHelper.executeLaterOnSwingTask(6000, new Runnable() {
					@Override
					public void run() {
						res.setText("<html>This process continues in the background until completion!");
					}
				});
				BackgroundTaskHelper.executeLaterOnSwingTask(7000, new Runnable() {
					@Override
					public void run() {
						res.setText("<html><u>This process continues in the background until completion!");
					}
				});
				BackgroundTaskHelper.executeLaterOnSwingTask(7500, new Runnable() {
					@Override
					public void run() {
						res.setText("<html>This process continues in the background until completion!");
					}
				});
				BackgroundTaskHelper.executeLaterOnSwingTask(8000, new Runnable() {
					@Override
					public void run() {
						res.setText("<html><u>This process continues in the background until completion!");
					}
				});
				BackgroundTaskHelper.executeLaterOnSwingTask(12000, new Runnable() {
					@Override
					public void run() {
						res.setText("Generate and show XML");
					}
				});
			}
		});
		return res;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private void styles(boolean enabled, JTextField editName, JTextField coordinator, JTextField groupVisibility,
			JComboBox experimentTypeSelection, JDateChooser expStart, JDateChooser expEnd, JTextField sequence, JTextField remark,
			JTextField outliers, JButton annotationB,
			JButton editB, JButton saveB, boolean editPossible, boolean savePossible) {
		
		editB.setEnabled(editPossible);
		if (!editPossible)
			enabled = true;
		
		if (enabled)
			editB.setText("<html>" + CANCEL);
		else
			editB.setText("<html>Edit");
		saveB.setEnabled(savePossible);
		
		editName.setEnabled(enabled);
		coordinator.setEnabled(enabled);
		groupVisibility.setEnabled(enabled);
		experimentTypeSelection.setEnabled(enabled);
		expStart.setEnabled(enabled);
		expEnd.setEnabled(enabled);
		sequence.setEnabled(enabled);
		remark.setEnabled(enabled);
		outliers.setEnabled(enabled);
		annotationB.setEnabled(enabled);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JComboBox getExperimentTypes(String experimentType, boolean editPossible) {
		TreeSet<String> typeset = new TreeSet<String>();
		if (experimentType != null)
			typeset.add(experimentType);
		for (IAPexperimentTypes et : IAPexperimentTypes.values())
			typeset.add(et + "");
		String[] types = typeset.toArray(new String[] {});
		// if (user != null && !user.equalsIgnoreCase("internet") && editPossible) {
		// // try {
		// // if (pass != null)
		// // types = CallDBE2WebService.metaBasisGet(user, pass,
		// // DBTable.EXPTYPE);
		// // } catch (Exception e) {
		// // ErrorMsg.addErrorMessage(e);
		// // }
		// }
		JComboBox res = new JComboBox(types);
		if (experimentType != null)
			res.setSelectedItem(experimentType);
		else
			res.setSelectedIndex(0);
		return res;
	}
	
	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	private JComboBox getGroups(String user, String pass, String userGroup, boolean editPossible) {
		String[] groups = new String[] { userGroup };
		if (user != null && !user.equalsIgnoreCase("internet") && editPossible) {
			// try {
			// if (pass != null)
			// groups = CallDBE2WebService.metaUserGetGroupsOfUser(user, pass);
			// } catch (Exception e) {
			// ErrorMsg.addErrorMessage(e);
			// }
		}
		JComboBox res = new JComboBox(groups);
		res.setSelectedItem(userGroup);
		return res;
	}
	
	private JComponent style(JComponent jTextField) {
		return jTextField;
		// return TableLayout.getSplitVertical(
		// jTextField,
		// new JLabel(),
		// TableLayout.PREFERRED,
		// 6);
	}
	
	private JComponent disable(JComponent jTextField) {
		if (jTextField instanceof JButton) {
			jTextField.setEnabled(false);
		} else {
			if (jTextField instanceof JTextField)
				((JTextField) jTextField).setEditable(false);
			jTextField.setBorder(null);
			jTextField.setBackground(Color.WHITE);
		}
		return style(jTextField);
	}
	
	public String getUserGroupVisibility() {
		return editName.getText();
	}
	
	public void updateSeriesData(Condition experimentInfo) {
		experimentInfo.setExperimentName(editName.getText());
	}
	
	public void setSaveAction(RunnableWithExperimentInfo runnable) {
		saveAction = runnable;
	}
	
	public void setExperimentInfo(
			final MongoDB m,
			final ExperimentHeaderInterface experimentHeader,
			boolean startEnabled, ExperimentInterface optExperiment) {
		
		JComponent correlationInfo = getCorrelationInfo(optExperiment);
		
		boolean hasCorrelationTableData = correlationInfo != null;
		
		if (hasCorrelationTableData) {
			setLayout(new TableLayout(new double[][] { { 0, 950 /* TableLayout.PREFERRED */, 0 },
					{ 0, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 0 } }));
			add(correlationInfo, "1,3");
		} else
			setLayout(new TableLayout(new double[][] { { 0, 750/* TableLayout.PREFERRED */, 0 }, { 0, TableLayout.PREFERRED, 0 } }));
		
		setOpaque(false);
		
		final boolean editPossible = true;
		// experimentHeader.getExcelfileid() != null
		// && experimentHeader.getExcelfileid().length() > 0 &&
		// experimentHeader.getImportusername() != null
		// && experimentHeader.getImportusername().equals(login);
		
		if (!editPossible)
			startEnabled = true;
		
		FolderPanel fp = new FolderPanel("Experiment " + experimentHeader.getExperimentName(), hasCorrelationTableData, true, false, null);
		// Color c = new Color(220, 220, 220);
		// fp.setFrameColor(c, Color.BLACK, 4, 8);
		fp.addCollapseListenerDialogSizeUpdate();
		
		editName = new JTextField(experimentHeader.getExperimentName());
		editDBid = new JTextField(experimentHeader.getDatabaseId() + "");
		coordinator = new JTextField(experimentHeader.getCoordinator());
		groupVisibility = new JTextField(experimentHeader.getImportusergroup());
		// getGroups(login, pass, experimentHeader.getImportusergroup(),
		// editPossible);
		experimentTypeSelection = getExperimentTypes(experimentHeader.getExperimentType(), editPossible);
		expStart = new JDateChooser(experimentHeader.getStartdate() != null ? experimentHeader.getStartdate() : new Date(0l));
		expEnd = new JDateChooser(experimentHeader.getStartdate() != null ? experimentHeader.getImportdate() : new Date(0l));
		remark = new JTextField(experimentHeader.getRemark());
		outliers = new JTextField(experimentHeader.getGlobalOutlierInfo());
		annotation = new JLabel(new DCexperimentHeader(experimentHeader).getHTMLoverview(null));
		annotation.putClientProperty("fulltext", experimentHeader.getAnnotation());
		sequence = new JTextField(experimentHeader.getSequence());
		{
			String to = "<html>The name given to the resource.<br>" +
					"Typically, a Title will be a name by which the resource is formally known.";
			fp.addGuiComponentRow(tooltip(new JLabel("Name/Title"), to), tooltip(editName, to), false);
		}
		fp.addGuiComponentRow(new JLabel("ID"), disable(editDBid), false);
		fp.addGuiComponentRow(new JLabel("Import by"), disable(new JTextField(experimentHeader.getImportusername())),
				false);
		fp.addGuiComponentRow(new JLabel("Origin"), disable(new JTextField(experimentHeader.getOriginDbId() + "")), false);
		fp.addGuiComponentRow(new JLabel("Database"), disable(new JTextField(experimentHeader.getDatabase() + "")), false);
		{
			String to = "<html>An entity primarily responsible for making the content of the resource.";
			fp.addGuiComponentRow(
					tooltip(new JLabel("Coordinator/Creator"), to),
					tooltip(coordinator, to), false);
		}
		fp.addGuiComponentRow(new JLabel("Access Group"), groupVisibility, false);
		fp.addGuiComponentRow(new JLabel("Experiment-Type"), experimentTypeSelection, false);
		fp.addGuiComponentRow(new JLabel("Start-Time"), expStart, false);
		fp.addGuiComponentRow(new JLabel("End-Time"), expEnd, false);
		{
			String ts = "Use ' // ' to split information. Specify stress as follows (examples): 'Stress:4;5;d;drought stress' or (two periods) 'Stress:4$10;5$13;d$n;drought stress$handling'.";
			fp.addGuiComponentRow(tooltip(new JLabel("Sequence/Stress"), ts), tooltip(sequence, ts), false);
		}
		fp.addGuiComponentRow(new JLabel("Remark"), remark, false);
		{
			String to = "<html>" +
					"Use ' // ' to split settings. Specify time values (with >,>=,<,<=,=) or plant IDs or " +
					"plant IDs with time (e.g. 1107BA001/2 -> plant 1107BA001 from day 2 on).";
			fp.addGuiComponentRow(tooltip(new JLabel("Outliers"), to), tooltip(outliers, to), false);
		}
		{
			annotationB = new DCexperimentHeader(experimentHeader).getEditButton(annotation, "Add/Edit");
			String to = "<html>" +
					"Use ' // ' to split annotation values.";
			fp.addGuiComponentRow(tooltip(new JLabel("Annotation"), to),
					TableLayout.getSplit(
							tooltip(annotation, to),
							TableLayout.getSplitVertical(
									annotationB,
									null, TableLayout.PREFERRED, TableLayout.FILL),
							TableLayout.FILL, TableLayout.PREFERRED), false);
		}
		fp.addGuiComponentRow(new JLabel("Connected Files"), disable(new JTextField(
				niceValue(experimentHeader.getNumberOfFiles(), null)
						+ " (" + niceValue(experimentHeader.getSizekb(), "KB") + ")")), false);
		if (optExperiment != null)
			fp.addGuiComponentRow(new JLabel("Numeric Values"), disable(new JTextField(
					niceValue(optExperiment.getNumberOfMeasurementValues(), null))), false);
		if (experimentHeader.getStorageTime() != null)
			fp.addGuiComponentRow(new JLabel("Storage Time"), disable(
					new JTextField(SystemAnalysis.getCurrentTime(experimentHeader.getStorageTime().getTime()))),
					false);
		fp.addGuiComponentRow(new JLabel("History"), disable(new JTextField(getVersionString(experimentHeader))), false);
		try {
			fp.addGuiComponentRow(
					new JLabel("Analysis Settings"),
					disable(new JTextField(
							experimentHeader == null || experimentHeader.getSettings() == null || experimentHeader.getSettings().isEmpty() ? "(not assigned)" :
									""
											+ SystemOptions.getInstance(null,
													new ExperimentAnalysisSettingsIOprovder(experimentHeader, m)).getString("DESCRIPTION",
													"pipeline_name", "(unnamed)", false) + " ("
											+ (StringManipulationTools.count(experimentHeader.getSettings(), "\n") + (experimentHeader.getSettings().isEmpty() ? 0 : 1))
											+ " lines)")), false);
		} catch (Exception err) {
			fp.addGuiComponentRow(new JLabel("Analysis Settings"),
					disable(new JTextField("(improperly defined: error " + err.getMessage() + ")")), false);
		}
		boolean showXMLbutton = false;
		if (optExperiment != null && showXMLbutton)
			fp.addGuiComponentRow(new JLabel("Show XML"), getShowDataButton(optExperiment), false);
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setBval(0, startEnabled);
		
		final JButton editB = new JMButton("Edit");
		final JButton saveB = new JMButton("Save Changes");
		if (!editPossible)
			saveB.setEnabled(false);
		// setText("Create Calendar Entry");
		
		editB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean restore = false;
				
				if (editB.getText().contains(CANCEL))
					restore = true;
				
				boolean b = tso.getBval(0, false);
				b = !b;
				tso.setBval(0, b);
				styles(b, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd,
						sequence, remark, outliers, annotationB, editB,
						saveB, editPossible, true);
				
				saveB.setText("Save Changes");
				if (restore) {
					editName.setText(experimentHeader.getExperimentName());
					editDBid.setText(experimentHeader.getDatabaseId());
					coordinator.setText(experimentHeader.getCoordinator());
					groupVisibility.setText(experimentHeader.getImportusergroup());
					// groupVisibility.setSelectedItem(experimentHeader.getImportusergroup());
					if (experimentHeader.getExperimentType() != null)
						experimentTypeSelection.setSelectedItem(experimentHeader.getExperimentType());
					else
						experimentTypeSelection.setSelectedIndex(0);
					expStart.setDate(experimentHeader.getStartdate());
					expEnd.setDate(experimentHeader.getImportdate());
					sequence.setText(experimentHeader.getSequence());
					remark.setText(experimentHeader.getRemark());
					outliers.setText(experimentHeader.getGlobalOutlierInfo());
					annotation.setText(new DCexperimentHeader(experimentHeader).getHTMLoverview(null));
				}
			}
		});
		
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean editPossibleBBB = editPossible;
				try {
					experimentHeader.setExperimentname(editName.getText());
					// experimentHeader.setImportusergroup((String)
					// groupVisibility.getSelectedItem());
					experimentHeader.setImportusergroup(groupVisibility.getText());
					experimentHeader.setExperimenttype((String) experimentTypeSelection.getSelectedItem());
					experimentHeader.setStartdate(expStart.getDate());
					experimentHeader.setImportdate(expEnd.getDate());
					experimentHeader.setSequence(sequence.getText());
					experimentHeader.setRemark(remark.getText());
					experimentHeader.setGlobalOutlierInfo(outliers.getText());
					experimentHeader.setAnnotation((String) annotation.getClientProperty("fulltext"));
					experimentHeader.setCoordinator(coordinator.getText());
					if (saveAction != null) {
						if (saveAction != null)
							saveAction.run(experimentHeader);
					} else {
						if (editPossibleBBB) {
							if (experimentHeader.getExperimentHeaderHelper() != null) {
								saveB.setText("Save data...");
								boolean ok = false;
								String dbId = experimentHeader != null ? experimentHeader.getDatabaseId() : null;
								if (dbId != null) {
									String id = dbId.contains(":") ? dbId.substring(0, dbId.indexOf(":")) : null;
									if (id != null && !id.isEmpty()) {
										ResourceIOHandler vfs = ResourceIOManager.getHandlerFromPrefix(id);
										if (vfs instanceof VirtualFileSystemHandler) {
											VirtualFileSystemHandler vv = (VirtualFileSystemHandler) vfs;
											if (vv.getVFS() instanceof VirtualFileSystemVFS2) {
												VirtualFileSystemVFS2 vv2 = (VirtualFileSystemVFS2) vv.getVFS();
												if (vv2.isAbleToSaveData()) {
													final ActionDataExportToVfs acc = getCopyAction(experimentReference, vv2);
													acc.setStatusProvider(BackgroundTaskHelper.getStatusHelperFor(saveB));
													BackgroundTaskHelper.issueSimpleTask("Save header changes", "Save changes", new Runnable() {
														@Override
														public void run() {
															try {
																HSMfolderTargetDataManager.clearPathCache();
																acc.setSkipClone(true);
																acc.setSkipUpdateDBid(true);
																acc.performActionCalculateResults(null);
																experimentReference.getHeader().getExperimentHeaderHelper().saveUpdatedProperties(acc.getStatusProvider());
															} catch (Exception e) {
																acc.getStatusProvider().setCurrentStatusText1("Error: " + e.getMessage());
															}
														}
													}, new Runnable() {
														@Override
														public void run() {
															editName.setText(experimentHeader.getExperimentName());
															editDBid.setText(experimentHeader.getDatabaseId());
															coordinator.setText(experimentHeader.getCoordinator());
															groupVisibility.setText(experimentHeader.getImportusergroup());
															// groupVisibility.setSelectedItem(experimentHeader.getImportusergroup());
															if (experimentHeader.getExperimentType() != null)
																experimentTypeSelection.setSelectedItem(experimentHeader.getExperimentType());
															else
																experimentTypeSelection.setSelectedIndex(0);
															expStart.setDate(experimentHeader.getStartdate());
															expEnd.setDate(experimentHeader.getImportdate());
															sequence.setText(experimentHeader.getSequence());
															remark.setText(experimentHeader.getRemark());
															outliers.setText(experimentHeader.getGlobalOutlierInfo());
															annotation.putClientProperty("fulltext", experimentHeader.getAnnotation());
															annotation.setText(new DCexperimentHeader(experimentHeader).getHTMLoverview(null));
															saveB.setText(acc.postResult);
														}
													});
													ok = true;
												}
											}
										}
									}
								}
								if (!ok)
									saveB.setText("Updated (saved)");
								// MyExperimentInfoPanel.this.setExperimentInfo(m, experimentHeader,
								// true, null);
							} else {
								if (m != null)
									m.saveExperimentHeader(experimentHeader);
								if (m != null)
									saveB.setText("Updated (in database)");
								else
									saveB.setText("Updated (in memory)");
							}
						} else {
							Experiment exp = new Experiment();
							exp.setHeader(experimentHeader);
							m.saveExperiment(exp, null);
							saveB.setText("Experiment saved in database");
							editPossibleBBB = false;
						}
					}
					saveB.setEnabled(false);
					editB.setText("Edit");
				} catch (Exception err) {
					editPossibleBBB = true;
					saveB.setEnabled(false);
					saveB.setText("Error");
					ErrorMsg.addErrorMessage(err);
				}
				boolean b = tso.getBval(0, false);
				b = !b;
				tso.setBval(0, b);
				styles(b, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd, sequence, remark,
						outliers, annotationB, editB,
						saveB, editPossibleBBB, false);
			}
		});
		
		styles(startEnabled, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd,
				sequence, remark, outliers, annotationB,
				editB, saveB, editPossible, true);
		
		GuiRow gr = new GuiRow(TableLayout.getSplitVertical(null, TableLayout.get3Split(null, TableLayout.get3Split(
				editB, null, saveB, TableLayout.PREFERRED, 10, TableLayout.PREFERRED), null, TableLayout.FILL,
				TableLayout.PREFERRED, TableLayout.FILL), 5, TableLayout.PREFERRED), null);
		gr.span = true;
		
		// if (editPossible)
		fp.addGuiComponentRow(gr, false);
		
		fp.setRowColSpacing(5, 15);
		
		fp.layoutRows();
		
		add(fp, "1,1");
		validate();
		
		// setBorder(BorderFactory.createEtchedBorder());
		setBorder(BorderFactory.createLoweredBevelBorder());
	}
	
	protected ActionDataExportToVfs getCopyAction(ExperimentReference experiment, VirtualFileSystemVFS2 vv2) {
		return new ActionDataExportToVfs(experiment.m, experiment, vv2, false, null) {
			
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				setSkipClone(true);
				super.performActionCalculateResults(src);
			}
			
			@Override
			public boolean isProvidingActions() {
				return true;
			}
			
			@Override
			public boolean requestTitleUpdates() {
				return false;
			}
			
			@Override
			public String getDefaultTitle() {
				String res = "Save Annotation Changes" + (postResult != null ? "<br>" + postResult : "");
				return res;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				return null;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return currentSet;
			}
			
			@Override
			public String getDefaultImage() {
				return "img/ext/gpl2/Gnome-Emblem-Downloads-64.png";
			}
		};
	}
	
	private JComponent tooltip(JComponent jc, String to) {
		jc.setToolTipText(to);
		return jc;
	}
	
	private JComponent getCorrelationInfo(ExperimentInterface optExperiment) {
		if (optExperiment == null)
			return null;
		else {
			ArrayList<GuiRow> rows1 = new ArrayList<GuiRow>();
			ArrayList<GuiRow> rows2 = new ArrayList<GuiRow>();
			
			double width = 90, space = 5, border = 2;
			// rows.add(new GuiRow(new JLabel("<html><br>Average visual property per plant versus manual measurement<hr>"), null));
			Collection<MatchInfo> sortedMatch = match(optExperiment, new String[] { "corr.", ".avg" }, false);
			Collections.sort((List<MatchInfo>) sortedMatch, new Comparator<MatchInfo>() {
				@Override
				public int compare(MatchInfo o1, MatchInfo o2) {
					Double d1 = o1.getComparisonValue();
					Double d2 = o2.getComparisonValue();
					if (d1 == null)
						d1 = 0d;
					if (d2 == null)
						d2 = 0d;
					return d2.compareTo(d1);
				}
			});
			for (MatchInfo mi : sortedMatch) {
				JComponent desc, height, leafWidth, freshWeight, dryWeight;
				desc = new JLabel(mi.getDesc());
				desc.setToolTipText(mi.getDesc());
				height = new JLabel(mi.getHeight());
				leafWidth = new JLabel(mi.getLeafWidth());
				freshWeight = new JLabel(mi.getFreshWeight());
				dryWeight = new JLabel(mi.getDryWeight());
				JComponent right = TableLayout.get4Split(height, leafWidth, freshWeight, dryWeight, width, space, border);
				rows1.add(new GuiRow(desc, right));
			}
			// rows.add(new GuiRow(new JLabel(""), null));
			// rows.add(new GuiRow(new JLabel("<html><br>Visual property for each side view vs. manual measurement<hr>"), null));
			sortedMatch = match(optExperiment, new String[] { "corr.", ".avg" }, true);
			Collections.sort((List<MatchInfo>) sortedMatch, new Comparator<MatchInfo>() {
				@Override
				public int compare(MatchInfo o1, MatchInfo o2) {
					Double d1 = o1.getComparisonValue();
					Double d2 = o2.getComparisonValue();
					if (d1 == null)
						d1 = 0d;
					if (d2 == null)
						d2 = 0d;
					return d2.compareTo(d1);
				}
			});
			for (MatchInfo mi : sortedMatch) {
				JComponent desc, height, leafWidth, freshWeight, dryWeight;
				desc = new JLabel(mi.getDesc());
				desc.setToolTipText(mi.getDesc());
				height = new JLabel(mi.getHeight());
				leafWidth = new JLabel(mi.getLeafWidth());
				freshWeight = new JLabel(mi.getFreshWeight());
				dryWeight = new JLabel(mi.getDryWeight());
				JComponent right = TableLayout.get4Split(height, leafWidth, freshWeight, dryWeight, width, space, border);
				rows2.add(new GuiRow(desc, right));
			}
			
			FolderPanel fp1 = new FolderPanel("<html>Correlations (Pearson&#39;s <i>r</i>) (AVERAGE PLANT)", false, true, false, null);
			FolderPanel fp2 = new FolderPanel("<html>Correlations (Pearson&#39;s <i>r</i>) (INDIVIDUAL SIDE VIEWS)", true, true, false, null);
			fp1.setMaximumRowCount(10);
			fp1.enableSearch(true);
			fp2.setMaximumRowCount(10);
			fp2.enableSearch(true);
			JComponent right1 = TableLayout.get4Split(new JLabel("Height"), new JLabel("Leaf Width"), new JLabel("Fresh Weight"), new JLabel("Dry Weight"), width,
					space,
					border);
			JComponent right2 = TableLayout.get4Split(new JLabel("Height"), new JLabel("Leaf Width"), new JLabel("Fresh Weight"), new JLabel("Dry Weight"), width,
					space, border);
			fp1.addGuiComponentRow(new JLabel("Visual Property"), right1, false);
			fp2.addGuiComponentRow(new JLabel("Visual Property"), right2, false);
			
			for (GuiRow row : rows1)
				fp1.addGuiComponentRow(row, false);
			fp1.addDefaultTextSearchFilter();
			fp1.addCollapseListenerDialogSizeUpdate();
			fp1.layoutRows();
			
			for (GuiRow row : rows2)
				fp2.addGuiComponentRow(row, false);
			fp2.addDefaultTextSearchFilter();
			fp2.addCollapseListenerDialogSizeUpdate();
			fp2.layoutRows();
			
			if (rows1.size() == 0)
				return null;
			
			return TableLayout.getSplitVertical(fp1, fp2, TableLayout.PREFERRED, TableLayout.PREFERRED);
		}
	}
	
	private ArrayList<MatchInfo> match(ExperimentInterface optExperiment, String[] match, boolean inverseSecond) {
		ArrayList<MatchInfo> res = new ArrayList<MatchInfo>();
		for (SubstanceInterface si : optExperiment) {
			if (si.getName().startsWith(match[0]) &&
					(
					(!inverseSecond && si.getName().endsWith(match[1])) ||
					(inverseSecond && !si.getName().endsWith(match[1]))
					)) {
				MatchInfo mi = new MatchInfo(si.getName());
				boolean matched = false;
				for (ConditionInterface ci : si) {
					System.out.println(ci.getConditionName());
					if (ci.getConditionName().contains("leaf width")) {
						for (SampleInterface sam : ci) {
							mi.setLeafWidth(
									StringManipulationTools.formatNumber(
											sam.getSampleAverage().getValue(), "#.###") + (sam.getAverageUnit() != null ? " " + sam.getAverageUnit() : ""));
							matched = true;
							break;
						}
					}
					if (ci.getConditionName().contains("dry weight")) {
						for (SampleInterface sam : ci) {
							mi.setDryWeight(
									StringManipulationTools.formatNumber(
											sam.getSampleAverage().getValue(), "#.###") + (sam.getAverageUnit() != null ? " " + sam.getAverageUnit() : ""));
							matched = true;
							break;
						}
					}
					if (ci.getConditionName().contains("fresh weight")) {
						for (SampleInterface sam : ci) {
							mi.setComparisonValue(sam.getSampleAverage().getValue());
							mi.setFreshWeight(StringManipulationTools.formatNumber(
									sam.getSampleAverage().getValue(), "#.###") + (sam.getAverageUnit() != null ? " " + sam.getAverageUnit() : ""));
							matched = true;
							break;
						}
					}
					if (ci.getConditionName().contains("height")) {
						for (SampleInterface sam : ci) {
							mi.setHeight(StringManipulationTools.formatNumber(
									sam.getSampleAverage().getValue(), "#.###") + (sam.getAverageUnit() != null ? " " + sam.getAverageUnit() : ""));
							matched = true;
							break;
						}
					}
				}
				if (matched)
					res.add(mi);
			}
		}
		return res;
	}
	
	private String getVersionString(ExperimentHeaderInterface experimentHeader) {
		if (experimentHeader.getHistory() == null || experimentHeader.getHistory().isEmpty())
			return "-";
		else {
			String s = "";
			if (experimentHeader.getHistory().size() > 1)
				s = "s";
			return experimentHeader.getHistory().size() + " older version" + s + " (earliest " +
					SystemAnalysis.getCurrentTime(experimentHeader.getHistory().firstEntry().getKey())
					+ ")";
		}
	}
	
	private String niceValue(long s, String unit) {
		try {
			double d = s;
			if (unit != null && d > 10000000) {
				d = d / 1024 / 1024;
				Locale locale = Locale.US;
				NumberFormat f = NumberFormat.getNumberInstance(locale);
				f.setMaximumFractionDigits(0);
				String string = f.format(d);
				return string + " GB";
			} else
				if (unit != null && d > 10000) {
					d = d / 1024;
					Locale locale = Locale.US;
					NumberFormat f = NumberFormat.getNumberInstance(locale);
					f.setMaximumFractionDigits(0);
					String string = f.format(d);
					return string + " MB";
				} else {
					Locale locale = Locale.US;
					NumberFormat f = NumberFormat.getNumberInstance(locale);
					f.setMaximumFractionDigits(0);
					String string = f.format(d);
					if (unit != null)
						return string + " " + unit;
					else
						return string;
				}
		} catch (Exception e) {
			if (unit != null)
				return s + " " + unit;
			else
				return s + "";
		}
	}
	
	public void setCancelText(String text) {
		CANCEL = text;
	}
	
}
