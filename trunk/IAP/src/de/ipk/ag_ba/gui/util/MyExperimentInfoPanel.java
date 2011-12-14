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
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.toedter.calendar.JDateChooser;

import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.interfaces.RunnableWithExperimentInfo;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataInfoPane;

/**
 * @author klukas
 */
public class MyExperimentInfoPanel extends JPanel {
	private static String CANCEL = "Cancel";
	
	private static final long serialVersionUID = 1L;
	
	JTextField editName;
	JTextField coordinator;
	JTextField groupVisibility;
	JComboBox experimentTypeSelection;
	JDateChooser expStart;
	JDateChooser expEnd;
	JTextField remark;
	JTextField sequence;
	
	private RunnableWithExperimentInfo saveAction;
	
	/**
	 * @param experiment
	 * @return
	 */
	private JComponent getShowDataButton(final ExperimentInterface experiment) {
		JMButton res = new JMButton("Show XML");
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExperimentDataInfoPane.showXMLdata(experiment);
			}
		});
		return res;
	}
	
	private void styles(boolean enabled, JTextField editName, JTextField coordinator, JTextField groupVisibility,
			JComboBox experimentTypeSelection, JDateChooser expStart, JDateChooser expEnd, JTextField sequence, JTextField remark,
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
	}
	
	private JComboBox getExperimentTypes(MongoDB m, String experimentType, boolean editPossible) {
		TreeSet<String> typeset = new TreeSet<String>();
		if (experimentType != null)
			typeset.add(experimentType);
		typeset.add(IAPexperimentTypes.Phytochamber);
		typeset.add(IAPexperimentTypes.BarleyGreenhouse);
		typeset.add(IAPexperimentTypes.MaizeGreenhouse);
		typeset.add("Analysis Results");
		typeset.add("Imported Analysis Results");
		typeset.add("Imported Dataset");
		typeset.add("Test (Delete OK)");
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
		if (jTextField instanceof JTextField)
			((JTextField) jTextField).setEditable(false);
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
	
	public void setExperimentInfo(final MongoDB m,
			final ExperimentHeaderInterface experimentHeader,
			boolean startEnabled, ExperimentInterface optExperiment) {
		
		JComponent correlationInfo = getCorrelationInfo(optExperiment);
		
		boolean hasCorrelationTableData = correlationInfo != null;
		
		if (hasCorrelationTableData) {
			setLayout(new TableLayout(new double[][] { { 0, TableLayout.PREFERRED, 0 },
					{ 0, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 0 } }));
			add(correlationInfo, "1,3");
		} else
			setLayout(new TableLayout(new double[][] { { 0, TableLayout.PREFERRED, 0 }, { 0, TableLayout.PREFERRED, 0 } }));
		
		setOpaque(false);
		
		final boolean editPossible = true;
		// experimentHeader.getExcelfileid() != null
		// && experimentHeader.getExcelfileid().length() > 0 &&
		// experimentHeader.getImportusername() != null
		// && experimentHeader.getImportusername().equals(login);
		
		if (!editPossible)
			startEnabled = true;
		
		FolderPanel fp = new FolderPanel("Experiment " + experimentHeader.getExperimentName(), false, true, false, null);
		Color c = new Color(220, 220, 220);
		fp.setFrameColor(c, Color.BLACK, 4, 8);
		
		editName = new JTextField(experimentHeader.getExperimentName());
		coordinator = new JTextField(experimentHeader.getCoordinator());
		groupVisibility = new JTextField(experimentHeader.getImportusergroup());
		// getGroups(login, pass, experimentHeader.getImportusergroup(),
		// editPossible);
		experimentTypeSelection = getExperimentTypes(m, experimentHeader.getExperimentType(), editPossible);
		expStart = new JDateChooser(experimentHeader.getStartdate() != null ? experimentHeader.getStartdate() : new Date(0l));
		expEnd = new JDateChooser(experimentHeader.getStartdate() != null ? experimentHeader.getImportdate() : new Date(0l));
		remark = new JTextField(experimentHeader.getRemark());
		sequence = new JTextField(experimentHeader.getSequence());
		
		fp.addGuiComponentRow(new JLabel("Name"), editName, false);
		fp.addGuiComponentRow(new JLabel("ID"), disable(new JTextField(experimentHeader.getDatabaseId() + "")), false);
		fp.addGuiComponentRow(new JLabel("Import by"), disable(new JTextField(experimentHeader.getImportusername())),
				false);
		fp.addGuiComponentRow(new JLabel("Origin"), new JLabel(experimentHeader.getOriginDbId() + ""), false);
		fp.addGuiComponentRow(new JLabel("Database"), new JLabel(experimentHeader.getDatabase() + ""), false);
		
		fp.addGuiComponentRow(new JLabel("Coordinator"), coordinator, false);
		fp.addGuiComponentRow(new JLabel("Group"), groupVisibility, false);
		fp.addGuiComponentRow(new JLabel("Experiment-Type"), experimentTypeSelection, false);
		fp.addGuiComponentRow(new JLabel("Start-Time"), expStart, false);
		fp.addGuiComponentRow(new JLabel("End-Time"), expEnd, false);
		fp.addGuiComponentRow(new JLabel("Sequence"), sequence, false);
		fp.addGuiComponentRow(new JLabel("Remark"), remark, false);
		fp.addGuiComponentRow(new JLabel("Connected Files"), new JLabel(niceValue(experimentHeader.getNumberOfFiles(), null)
				+ " (" + niceValue(experimentHeader.getSizekb(), "KB") + ")"), false);
		if (experimentHeader.getStorageTime() != null)
			fp.addGuiComponentRow(new JLabel("Storage Time"), new JLabel(SystemAnalysisExt.getCurrentTime(experimentHeader.getStorageTime().getTime())), false);
		fp.addGuiComponentRow(new JLabel("History"), new JLabel(getVersionString(experimentHeader)), false);
		if (optExperiment != null)
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
				styles(b, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd, sequence, remark, editB,
						saveB, editPossible, true);
				
				if (restore) {
					saveB.setText("Save Changes");
					editName.setText(experimentHeader.getExperimentName());
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
					experimentHeader.setCoordinator(coordinator.getText());
					if (saveAction != null) {
						if (saveAction != null)
							saveAction.run(experimentHeader);
					} else {
						if (editPossibleBBB) {
							if (experimentHeader.getDatabaseId().startsWith("lemnatec:") || experimentHeader.getDatabaseId().startsWith("hsm:")
									|| experimentHeader.getDatabaseId().isEmpty()) {
								saveB.setText("Updated (in memory)");
							} else {
								m.setExperimentInfo(experimentHeader);
								saveB.setText("Updated in Cloud DB");
							}
						} else {
							Experiment exp = new Experiment();
							exp.setHeader(experimentHeader);
							m.saveExperiment(exp, null);
							saveB.setText("Experiment Saved in Cloud DB");
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
				styles(b, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd, sequence, remark, editB,
						saveB, editPossibleBBB, false);
			}
		});
		
		styles(startEnabled, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd, sequence, remark,
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
	
	private JComponent getCorrelationInfo(ExperimentInterface optExperiment) {
		if (optExperiment == null)
			return null;
		else {
			ArrayList<GuiRow> rows = new ArrayList<GuiRow>();
			
			double width = 90, space = 15, border = 5;
			
			rows.add(new GuiRow(new JLabel("<html><br>Average visual property per plant vs. manual measurement<hr>"), null));
			for (MatchInfo mi : match(optExperiment, new String[] { "corr.", ".avg" }, false)) {
				JComponent desc, height, freshWeight, dryWeight;
				desc = new JLabel(mi.getDesc());
				height = new JLabel(mi.getHeight());
				freshWeight = new JLabel(mi.getFreshWeight());
				dryWeight = new JLabel(mi.getDryWeight());
				JComponent right = TableLayout.get4Split(new JLabel(), height, freshWeight, dryWeight, width, space, border);
				rows.add(new GuiRow(desc, right));
			}
			for (MatchInfo mi : match(optExperiment, new String[] { "corr.", "volume." }, false)) {
				JComponent desc, height, freshWeight, dryWeight;
				desc = new JLabel(mi.getDesc());
				height = new JLabel(mi.getHeight());
				freshWeight = new JLabel(mi.getFreshWeight());
				dryWeight = new JLabel(mi.getDryWeight());
				JComponent right = TableLayout.get4Split(new JLabel(), height, freshWeight, dryWeight, width, space, border);
				rows.add(new GuiRow(desc, right));
			}
			rows.add(new GuiRow(new JLabel(""), null));
			rows.add(new GuiRow(new JLabel("<html><br>Visual property for each side view vs. manual measurement<hr>"), null));
			for (MatchInfo mi : match(optExperiment, new String[] { "corr.", ".avg" }, true)) {
				JComponent desc, height, freshWeight, dryWeight;
				desc = new JLabel(mi.getDesc());
				height = new JLabel(mi.getHeight());
				freshWeight = new JLabel(mi.getFreshWeight());
				dryWeight = new JLabel(mi.getDryWeight());
				JComponent right = TableLayout.get4Split(new JLabel(), height, freshWeight, dryWeight, width, space, border);
				rows.add(new GuiRow(desc, right));
			}
			if (rows.size() == 2)
				return null;
			
			FolderPanel fp = new FolderPanel("<html>Correlations (Pearson&#39;s <i>r</i>)", false, true, false, null);
			JComponent right = TableLayout.get4Split(new JLabel(), new JLabel("Height"), new JLabel("Fresh Weight"), new JLabel("Dry Weight"), width, space,
					border);
			fp.addGuiComponentRow(new JLabel("Visual Property"), right, false);
			for (GuiRow row : rows)
				fp.addGuiComponentRow(row, false);
			fp.layoutRows();
			return fp;
		}
	}
	
	private Collection<MatchInfo> match(ExperimentInterface optExperiment, String[] match, boolean inverseSecond) {
		ArrayList<MatchInfo> res = new ArrayList<MatchInfo>();
		for (SubstanceInterface si : optExperiment) {
			System.out.println(si.getName() + " <== match? " + match[0] + " / " + match[1]);
			if (si.getName().startsWith(match[0]) &&
					(
					(!inverseSecond && si.getName().contains(match[1])) ||
					(inverseSecond && !si.getName().contains(match[1]))
					)) {
				MatchInfo mi = new MatchInfo(si.getName());
				boolean matched = false;
				for (ConditionInterface ci : si) {
					if (ci.getConditionName().contains("dry weight")) {
						for (SampleInterface sam : ci) {
							mi.setDryWeight(
									StringManipulationTools.formatNumber(
											sam.getSampleAverage().getValue(), "#.###") + " " + sam.getAverageUnit());
							matched = true;
							break;
						}
					}
					if (ci.getConditionName().contains("fresh weight")) {
						for (SampleInterface sam : ci) {
							mi.setFreshWeight(StringManipulationTools.formatNumber(
									sam.getSampleAverage().getValue(), "#.###") + " " + sam.getAverageUnit());
							matched = true;
							break;
						}
					}
					if (ci.getConditionName().contains("height")) {
						for (SampleInterface sam : ci) {
							mi.setHeight(StringManipulationTools.formatNumber(
									sam.getSampleAverage().getValue(), "#.###") + " " + sam.getAverageUnit());
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
					SystemAnalysisExt.getCurrentTime(experimentHeader.getHistory().firstEntry().getKey())
					+ ")";
		}
	}
	
	private String niceValue(long d, String unit) {
		try {
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
				return d + " " + unit;
			else
				return d + "";
		}
	}
	
	public void setCancelText(String text) {
		CANCEL = text;
	}
	
}
