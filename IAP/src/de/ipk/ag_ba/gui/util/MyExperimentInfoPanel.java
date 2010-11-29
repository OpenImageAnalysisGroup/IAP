/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Jun 1, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;

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
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.toedter.calendar.JDateChooser;

import de.ipk.ag_ba.gui.interfaces.RunnableWithExperimentInfo;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataInfoPane;

/**
 * @author klukas
 * 
 */
public class MyExperimentInfoPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	JTextField editName;
	JTextField coordinator;
	JTextField groupVisibility;
	JComboBox experimentTypeSelection;
	JDateChooser expStart;
	JDateChooser expEnd;
	JTextField remark;

	private RunnableWithExperimentInfo saveAction;

	// public void setExperimentInfo(final String login, final String pass, final
	// ExperimentInfo ei, boolean startEnabled,
	// ExperimentInterface experiment) {
	// setLayout(new TableLayout(new double[][] { { 0, 400, 0 }, { 0,
	// TableLayout.PREFERRED, 0 } }));
	//
	// setOpaque(false);
	//
	// final boolean editPossible = ei.excelFileMd5 != null &&
	// ei.excelFileMd5.length() > 0 && ei.importUser != null
	// && ei.importUser.equals(login);
	//
	// if (!editPossible)
	// startEnabled = true;
	//
	// FolderPanel fp = new FolderPanel("Experiment " + ei.experimentName +
	// " (Stored in MongoDB)", false, false, false,
	// null);
	// Color c = new Color(220, 220, 220);
	// fp.setFrameColor(c, Color.BLACK, 4, 8);
	//
	// editName = new JTextField(ei.experimentName);
	// coordinator = new JTextField(ei.coordinator);
	// groupVisibility = getGroups(login, pass, ei.userGroup, editPossible);
	// experimentTypeSelection = getExperimentTypes(login, pass,
	// ei.experimentType, editPossible);
	// expStart = new JDateChooser(ei.dateExperimentStart);
	// expEnd = new JDateChooser(ei.dateExperimentImport);
	// remark = new JTextField(ei.remark);
	//
	// fp.addGuiComponentRow(new JLabel("Name"), editName, false);
	// fp.addGuiComponentRow(new JLabel("ID"), disable(new
	// JTextField(ei.excelFileMd5 + "")), false);
	// fp.addGuiComponentRow(new JLabel("Import by"), disable(new
	// JTextField(ei.importUser)), false);
	// fp.addGuiComponentRow(new JLabel("Coordinator"), coordinator, false);
	// fp.addGuiComponentRow(new JLabel("Visible for"), groupVisibility, false);
	// fp.addGuiComponentRow(new JLabel("Experiment-Type"),
	// experimentTypeSelection, false);
	// fp.addGuiComponentRow(new JLabel("Start-Time"), expStart, false);
	// fp.addGuiComponentRow(new JLabel("End-Time"), expEnd, false);
	// fp.addGuiComponentRow(new JLabel("Remark"), remark, false);
	// fp.addGuiComponentRow(new JLabel("Connected Files"), new
	// JLabel(ei.fileCount + " (" + (ei.byteSize / 1024)
	// + " KB)"), false);
	// fp.addGuiComponentRow(new JLabel("Show XML"),
	// getShowDataButton(experiment), false);
	//
	// final ThreadSafeOptions tso = new ThreadSafeOptions();
	// tso.setBval(0, startEnabled);
	//
	// final JButton editB = new JMButton("<html><small>Edit");
	// final JButton saveB = new JMButton("<html><small>Save Changes");
	//
	// editB.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// boolean restore = false;
	//
	// if (editB.getText().contains("Cancel"))
	// restore = true;
	//
	// boolean b = tso.getBval(0, false);
	// b = !b;
	// tso.setBval(0, b);
	// styles(b, editName, coordinator, groupVisibility, experimentTypeSelection,
	// expStart, expEnd, remark, editB,
	// saveB, editPossible);
	//
	// if (restore) {
	// saveB.setText("<html><small>Save Changes");
	// editName.setText(ei.experimentName);
	// groupVisibility.setSelectedItem(ei.userGroup);
	// if (ei.experimentType != null)
	// experimentTypeSelection.setSelectedItem(ei.experimentType);
	// else
	// experimentTypeSelection.setSelectedIndex(0);
	// expStart.setDate(ei.dateExperimentStart);
	// expEnd.setDate(ei.dateExperimentImport);
	// remark.setText(ei.remark);
	// }
	// }
	// });
	//
	// saveB.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// try {
	// ExperimentInfo newProperties = new ExperimentInfo(editName.getText(),
	// ei.experimentID, ei.importUser,
	// (String) groupVisibility.getSelectedItem(), (String)
	// experimentTypeSelection.getSelectedItem(),
	// expStart.getDate(), expEnd.getDate(), remark.getText(),
	// coordinator.getText(), ei.excelFileMd5,
	// ei.fileCount, ei.byteSize);
	// if (saveAction != null)
	// saveAction.run(newProperties);
	// else {
	// CallDBE2WebService.setChangeExperimentMetaData(login, pass,
	// ei.experimentName, newProperties);
	// AIPgui.removeNavigationInfo(3, -1);
	// }
	// saveB.setText("Saved");
	// } catch (Exception err) {
	// saveB.setText("Error");
	// ErrorMsg.addErrorMessage(err);
	// }
	// boolean b = tso.getBval(0, false);
	// b = !b;
	// tso.setBval(0, b);
	// styles(b, editName, coordinator, groupVisibility, experimentTypeSelection,
	// expStart, expEnd, remark, editB,
	// saveB, editPossible);
	// }
	// });
	//
	// styles(startEnabled, editName, coordinator, groupVisibility,
	// experimentTypeSelection, expStart, expEnd, remark,
	// editB, saveB, editPossible);
	//
	// GuiRow gr = new GuiRow(TableLayout.getSplitVertical(null,
	// TableLayout.get3Split(null, TableLayout.get3Split(
	// editB, null, saveB, TableLayout.PREFERRED, 10, TableLayout.PREFERRED),
	// null, TableLayout.FILL,
	// TableLayout.PREFERRED, TableLayout.FILL), 5, TableLayout.PREFERRED),
	// null);
	// gr.span = true;
	//
	// if (editPossible)
	// fp.addGuiComponentRow(gr, false);
	//
	// fp.setRowColSpacing(5, 15);
	//
	// fp.layoutRows();
	//
	// add(fp, "1,1");
	// validate();
	//
	// // setBorder(BorderFactory.createEtchedBorder());
	// setBorder(BorderFactory.createLoweredBevelBorder());
	// }

	/**
	 * @param experiment
	 * @return
	 */
	private JComponent getShowDataButton(final ExperimentInterface experiment) {
		JMButton res = new JMButton("Show XML");
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExperimentDataInfoPane.showXMLdata(null, experiment);
			}
		});
		return res;
	}

	private void styles(boolean enabled, JTextField editName, JTextField coordinator, JTextField groupVisibility,
			JComboBox experimentTypeSelection, JDateChooser expStart, JDateChooser expEnd, JTextField remark,
			JButton editB, JButton saveB, boolean editPossible, boolean savePossible) {

		editB.setEnabled(editPossible);
		if (!editPossible)
			enabled = true;

		if (enabled)
			editB.setText("<html>Cancel");
		else
			editB.setText("<html>Edit");
		saveB.setEnabled(savePossible);

		editName.setEnabled(enabled);
		coordinator.setEnabled(enabled);
		groupVisibility.setEnabled(enabled);
		experimentTypeSelection.setEnabled(enabled);
		expStart.setEnabled(enabled);
		expEnd.setEnabled(enabled);
		remark.setEnabled(enabled);
	}

	private JComboBox getExperimentTypes(String user, String pass, String experimentType, boolean editPossible) {
		String[] types = new String[] { experimentType };
		if (user != null && !user.equalsIgnoreCase("internet") && editPossible) {
			// try {
			// if (pass != null)
			// types = CallDBE2WebService.metaBasisGet(user, pass,
			// DBTable.EXPTYPE);
			// } catch (Exception e) {
			// ErrorMsg.addErrorMessage(e);
			// }
		}
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

	public void setExperimentInfo(final String login, final String pass,
			final ExperimentHeaderInterface experimentHeader, boolean startEnabled, ExperimentInterface experiment) {
		setLayout(new TableLayout(new double[][] { { 0, 400, 0 }, { 0, TableLayout.PREFERRED, 0 } }));

		setOpaque(false);

		final boolean editPossible = true;
		// experimentHeader.getExcelfileid() != null
		// && experimentHeader.getExcelfileid().length() > 0 &&
		// experimentHeader.getImportusername() != null
		// && experimentHeader.getImportusername().equals(login);

		if (!editPossible)
			startEnabled = true;

		FolderPanel fp = new FolderPanel("Experiment " + experimentHeader.getExperimentname(), false, false, false, null);
		Color c = new Color(220, 220, 220);
		fp.setFrameColor(c, Color.BLACK, 4, 8);

		editName = new JTextField(experimentHeader.getExperimentname());
		coordinator = new JTextField(experimentHeader.getCoordinator());
		groupVisibility = new JTextField(experimentHeader.getImportusergroup());
		// getGroups(login, pass, experimentHeader.getImportusergroup(),
		// editPossible);
		experimentTypeSelection = getExperimentTypes(login, pass, experimentHeader.getExperimentType(), editPossible);
		expStart = new JDateChooser(experimentHeader.getStartdate());
		expEnd = new JDateChooser(experimentHeader.getImportdate());
		remark = new JTextField(experimentHeader.getRemark());

		fp.addGuiComponentRow(new JLabel("Name"), editName, false);
		fp.addGuiComponentRow(new JLabel("ID"), disable(new JTextField(experimentHeader.getExcelfileid() + "")), false);
		fp.addGuiComponentRow(new JLabel("Import by"), disable(new JTextField(experimentHeader.getImportusername())),
				false);
		fp.addGuiComponentRow(new JLabel("Coordinator"), coordinator, false);
		fp.addGuiComponentRow(new JLabel("Visible for"), groupVisibility, false);
		fp.addGuiComponentRow(new JLabel("Experiment-Type"), experimentTypeSelection, false);
		fp.addGuiComponentRow(new JLabel("Start-Time"), expStart, false);
		fp.addGuiComponentRow(new JLabel("End-Time"), expEnd, false);
		fp.addGuiComponentRow(new JLabel("Remark"), remark, false);
		fp.addGuiComponentRow(new JLabel("Connected Files"), new JLabel(niceValue(""
				+ experimentHeader.getNumberOfFiles(), null)
				+ " (" + niceValue(experimentHeader.getSizekb(), "KB") + ")"), false);
		fp.addGuiComponentRow(new JLabel("Show XML"), getShowDataButton(experiment), false);

		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setBval(0, startEnabled);

		final JButton editB = new JMButton("Edit");
		final JButton saveB = new JMButton("Save Changes");
		if (!editPossible)
			saveB.setEnabled(false);
		// setText("Create Calendar Entry");

		editB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean restore = false;

				if (editB.getText().contains("Cancel"))
					restore = true;

				boolean b = tso.getBval(0, false);
				b = !b;
				tso.setBval(0, b);
				styles(b, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd, remark, editB,
						saveB, editPossible, true);

				if (restore) {
					saveB.setText("Save Changes");
					editName.setText(experimentHeader.getExperimentname());
					groupVisibility.setText(experimentHeader.getImportusergroup());
					// groupVisibility.setSelectedItem(experimentHeader.getImportusergroup());
					if (experimentHeader.getExperimentType() != null)
						experimentTypeSelection.setSelectedItem(experimentHeader.getExperimentType());
					else
						experimentTypeSelection.setSelectedIndex(0);
					expStart.setDate(experimentHeader.getStartdate());
					expEnd.setDate(experimentHeader.getImportdate());
					remark.setText(experimentHeader.getRemark());
				}
			}
		});

		saveB.addActionListener(new ActionListener() {
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
					experimentHeader.setRemark(remark.getText());
					experimentHeader.setCoordinator(coordinator.getText());
					if (saveAction != null) {
						if (saveAction != null)
							saveAction.run(experimentHeader);
					} else {
						if (editPossibleBBB) {
							if (experimentHeader.getExcelfileid().startsWith("lemnatec:")) {
								saveB.setText("Updated (in memory)");
							} else {
								new MongoDB().setExperimentInfo(experimentHeader);
								saveB.setText("Updated in Cloud DB");
							}
						} else {
							Experiment exp = new Experiment();
							exp.setHeader(experimentHeader);
							new MongoDB().saveExperiment("dbe3", null, login, pass, exp, null);
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
				styles(b, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd, remark, editB,
						saveB, editPossibleBBB, false);
			}
		});

		styles(startEnabled, editName, coordinator, groupVisibility, experimentTypeSelection, expStart, expEnd, remark,
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

	private String niceValue(String sizekb, String unit) {
		try {
			Double d = Double.valueOf(sizekb);
			if (unit != null && d > 10000) {
				d = d / 1024;
				Locale locale = Locale.US;
				NumberFormat f = NumberFormat.getNumberInstance(locale);
				f.setMaximumFractionDigits(0);
				String string = f.format(d);
				if (unit != null)
					return string + " MB";
				else
					return string;
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
				return sizekb + " " + unit;
			else
				return sizekb;
		}
	}

}
