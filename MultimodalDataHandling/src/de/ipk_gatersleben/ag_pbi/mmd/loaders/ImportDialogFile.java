/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;
import de.ipk_gatersleben.ag_pbi.datahandling.JComboBoxAutoCompleteAndSelectOnTab;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface;
import de.ipk_gatersleben.ag_pbi.mmd.JSpinnerSelectOnTab;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ImportDialogFile extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public static final double LEFTSIZE = 150, RIGHTSIZE = 300;
	
	public static final String DUPLICATE_TOOLTIP = "<html>Will copy the formular data of the actual file <br>to the other formulars.";
	public static final String DUPLICATE_TEXT = "Apply to all other files";
	
	private TemplateLoaderInterface loader;
	
	private JComboBoxAutoCompleteAndSelectOnTab combonameexp;
	private JComboBoxAutoCompleteAndSelectOnTab comboexpsrc;
	private JComboBoxAutoCompleteAndSelectOnTab combocoordinator;
	private JComboBoxAutoCompleteAndSelectOnTab combostartdate;
	private JComboBoxAutoCompleteAndSelectOnTab comboimportdate;
	private JTextField combocommentar;
	
	private JComboBoxAutoCompleteAndSelectOnTab combospecies;
	private JComboBoxAutoCompleteAndSelectOnTab combogenotype;
	private JComboBoxAutoCompleteAndSelectOnTab combotreatment;
	
	private JSpinnerSelectOnTab spinnertimepoint;
	private JComboBoxAutoCompleteAndSelectOnTab combounit;
	private JComboBoxAutoCompleteAndSelectOnTab combocomponent;
	private JComboBoxAutoCompleteAndSelectOnTab combomeasurementtool;
	
	private ArrayList<ImportDialogFile> idflist;
	private JButton expFormDuplicateBT;
	private JButton condFormDuplicateBT;
	private JButton sampFormDuplicateBT;
	
	private JButton dataDuplicateBT;
	
	public ImportDialogFile() {
		super();
		// empty
	}
	
	public boolean initializePanel(File f, int cnt, ArrayList<ImportDialogFile> idflist, TemplateLoaderMMD templateLoader) {
		loader = templateLoader.createInstance(f);
		
		if (loader == null)
			return false;
		
		this.idflist = idflist;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setBackground(null);
		
		String name = f.getName();
		while (name.length() < 30)
			name += " ";
		add(TableLayout.getSplit(new JLabel("File " + (cnt + 1) + " (" + loader.toString() + "):"), new JLabel("<html>"
				+ name), LEFTSIZE, RIGHTSIZE));
		
		addSeparator();
		createExperimentDialog();
		addSeparator();
		createConditionDialog();
		addSeparator();
		createSampleDialog();
		addSeparator();
		JPanel panel = null;
		try {
			panel = loader.getAttributeDialog(cnt);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			panel = null;
		}
		if (panel != null)
			add(panel);
		else
			return false;
		addDuplicateButton();
		
		return true;
	}
	
	private void addDuplicateButton() {
		dataDuplicateBT = new JButton(ImportDialogFile.DUPLICATE_TEXT);
		dataDuplicateBT.setToolTipText(ImportDialogFile.DUPLICATE_TOOLTIP);
		dataDuplicateBT.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ImportDialogFile idf : idflist)
					idf.getLoader().setFormularData(loader, loader.getFormData());
				MainFrame.getInstance().showMessageDialog(
						"<html>Data-values marked with * were copied to<br>" + "all other formulars of type \""
								+ loader.toString() + "\".");
			}
		});
		add(TableLayout.getSplit(new JLabel("<html><small><hspace> *copied field "), dataDuplicateBT,
				ImportDialogFile.LEFTSIZE, ImportDialogFile.RIGHTSIZE));
	}
	
	private void addSeparator() {
		JPanel sep = new JPanel();
		sep.setOpaque(false);
		add(sep);
		JSeparator sepline = new JSeparator();
		sepline.setOpaque(false);
		add(sepline);
		JPanel sep2 = new JPanel();
		sep2.setOpaque(false);
		add(sep2);
	}
	
	private void createExperimentDialog() {
		expFormDuplicateBT = new JButton(DUPLICATE_TEXT);
		expFormDuplicateBT.setToolTipText(DUPLICATE_TOOLTIP);
		expFormDuplicateBT.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ImportDialogFile idf : idflist) {
					idf.setExperimentName(getExperimentName());
					idf.setExperimentSrc(getExperimentSrc());
					idf.setCoordinator(getCoordinator());
					idf.setStartdate(getStartdate());
					idf.setImportdate(getImportdate());
					idf.setCommentar(getCommentar());
				}
				MainFrame.getInstance().showMessageDialog("<html>Experiment data were copied to all other formulars.");
			}
		});
		add(TableLayout.getSplit(new JLabel("<html><large>Experiment:"), TableLayout.getSplit(null, expFormDuplicateBT,
				100, 200), LEFTSIZE, RIGHTSIZE));
		combonameexp = new JComboBoxAutoCompleteAndSelectOnTab();
		combonameexp.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Experimentname"), combonameexp, LEFTSIZE, RIGHTSIZE));
		comboexpsrc = new JComboBoxAutoCompleteAndSelectOnTab();
		comboexpsrc.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Experiment src"), comboexpsrc, LEFTSIZE, RIGHTSIZE));
		combocoordinator = new JComboBoxAutoCompleteAndSelectOnTab();
		combocoordinator.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Coordinator"), combocoordinator, LEFTSIZE, RIGHTSIZE));
		combostartdate = new JComboBoxAutoCompleteAndSelectOnTab();
		combostartdate.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Startdate (dd-mm-yy)"), combostartdate, LEFTSIZE, RIGHTSIZE));
		comboimportdate = new JComboBoxAutoCompleteAndSelectOnTab();
		comboimportdate.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Enddate (dd-mm-yy)"), comboimportdate, LEFTSIZE, RIGHTSIZE));
		combocommentar = new JTextField("");
		combocommentar.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Additional Comments"), combocommentar, LEFTSIZE, RIGHTSIZE));
	}
	
	private void createConditionDialog() {
		condFormDuplicateBT = new JButton(DUPLICATE_TEXT);
		condFormDuplicateBT.setToolTipText(DUPLICATE_TOOLTIP);
		condFormDuplicateBT.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ImportDialogFile idf : idflist) {
					idf.setSpecies(getSpecies());
					idf.setGenotype(getGenotype());
					idf.setTreatment(getTreatment());
				}
				MainFrame.getInstance().showMessageDialog("<html>Condition data were copied to all other formulars.");
			}
		});
		add(TableLayout.getSplit(new JLabel("<html><large>Condition:"), TableLayout.getSplit(null, condFormDuplicateBT,
				100, 200), LEFTSIZE, RIGHTSIZE));
		combospecies = new JComboBoxAutoCompleteAndSelectOnTab();
		combospecies.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Species"), combospecies, LEFTSIZE, RIGHTSIZE));
		combogenotype = new JComboBoxAutoCompleteAndSelectOnTab();
		combogenotype.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Genotype"), combogenotype, LEFTSIZE, RIGHTSIZE));
		combotreatment = new JComboBoxAutoCompleteAndSelectOnTab();
		combotreatment.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Treatment"), combotreatment, LEFTSIZE, RIGHTSIZE));
	}
	
	private void createSampleDialog() {
		sampFormDuplicateBT = new JButton(DUPLICATE_TEXT);
		sampFormDuplicateBT.setToolTipText(DUPLICATE_TOOLTIP);
		sampFormDuplicateBT.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ImportDialogFile idf : idflist) {
					idf.setTime(getTime());
					idf.setTimeUnit(getTimeUnit());
					idf.setComponent(getComponent());
					idf.setMeasurementtool(getMeasurementtool());
				}
				MainFrame.getInstance().showMessageDialog("<html>Sample data were copied to all other formulars.");
			}
		});
		add(TableLayout.getSplit(new JLabel("<html><large>Sample:"), TableLayout.getSplit(null, sampFormDuplicateBT, 100,
				200), LEFTSIZE, RIGHTSIZE));
		spinnertimepoint = new JSpinnerSelectOnTab(new SpinnerNumberModel(-1, -1, 1000000, 1));
		add(TableLayout.getSplit(new JLabel("Timepoint"), spinnertimepoint, LEFTSIZE, RIGHTSIZE));
		combounit = new JComboBoxAutoCompleteAndSelectOnTab();
		
		combounit.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Timeunit"), combounit, LEFTSIZE, RIGHTSIZE));
		combocomponent = new JComboBoxAutoCompleteAndSelectOnTab();
		combocomponent.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Component"), combocomponent, LEFTSIZE, RIGHTSIZE));
		combomeasurementtool = new JComboBoxAutoCompleteAndSelectOnTab();
		combomeasurementtool.setEditable(true);
		add(TableLayout.getSplit(new JLabel("Measurementtool"), combomeasurementtool, LEFTSIZE, RIGHTSIZE));
	}
	
	private String getCorrectItem(JComboBox combobox) {
		Object o = combobox.getSelectedItem();
		if (o != null && o instanceof String)
			return (String) o;
		else
			return "";
	}
	
	public String getSpecies() {
		String val = getCorrectItem(combospecies);
		if (val == null || val.equals(""))
			val = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return val;
	}
	
	public String getExperimentName() {
		String expname = getCorrectItem(combonameexp);
		if (expname == null || expname.equals(""))
			expname = ExperimentInterface.UNSPECIFIED_EXPERIMENTNAME;
		return expname;
	}
	
	public String getExperimentSrc() {
		String expsrc = getCorrectItem(comboexpsrc);
		if (expsrc == null || expsrc.equals(""))
			expsrc = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return expsrc;
	}
	
	public String getCommentar() {
		return combocommentar.getText();
	}
	
	public String getGenotype() {
		String val = getCorrectItem(combogenotype);
		if (val == null || val.equals(""))
			val = Experiment.UNSPECIFIED_ATTRIBUTE_STRING;
		return val;
	}
	
	public String getTreatment() {
		return getCorrectItem(combotreatment);
	}
	
	public String getCoordinator() {
		String val = getCorrectItem(combocoordinator);
		if (val == null || val.equals(""))
			val = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return val;
	}
	
	public Date getStartdate() {
		try {
			return new SimpleDateFormat("yy-MM-dd").parse(getCorrectItem(combostartdate));
		} catch (Exception e) {
			return new Date();
		}
	}
	
	public Date getImportdate() {
		try {
			return new SimpleDateFormat("yy-MM-dd").parse(getCorrectItem(comboimportdate));
		} catch (Exception e) {
			return new Date();
		}
	}
	
	public int getTime() {
		return (Integer) spinnertimepoint.getValue();
	}
	
	public String getTimeUnit() {
		String timeunit = getCorrectItem(combounit);
		if (timeunit.equalsIgnoreCase(""))
			timeunit = "-1";
		return timeunit;
	}
	
	public String getComponent() {
		return getCorrectItem(combocomponent);
	}
	
	public String getMeasurementtool() {
		return getCorrectItem(combomeasurementtool);
	}
	
	public TemplateLoaderInterface getLoader() {
		return loader;
	}
	
	public File getFile() {
		return loader.getFile();
	}
	
	public String getSubstance() {
		return loader.getSubstance();
	}
	
	public void setSpecies(String val) {
		combospecies.setSelectedItem(val);
	}
	
	public void setExperimentName(String val) {
		combonameexp.setSelectedItem(val);
	}
	
	public void setExperimentSrc(String val) {
		comboexpsrc.setSelectedItem(val);
	}
	
	public void setCommentar(String val) {
		combocommentar.setText(val);
	}
	
	public void setGenotype(String val) {
		combogenotype.setSelectedItem(val);
	}
	
	public void setTreatment(String val) {
		combotreatment.setSelectedItem(val);
	}
	
	public void setCoordinator(String val) {
		combocoordinator.setSelectedItem(val);
	}
	
	public void setStartdate(Date val) {
		combostartdate.setSelectedItem(val);
	}
	
	public void setImportdate(Date val) {
		comboimportdate.setSelectedItem(val);
	}
	
	public void setTime(int val) {
		spinnertimepoint.setValue(val);
	}
	
	public void setTimeUnit(String val) {
		combounit.setSelectedItem(val);
	}
	
	public void setComponent(String val) {
		combocomponent.setSelectedItem(val);
	}
	
	public void setMeasurementtool(String val) {
		combomeasurementtool.setSelectedItem(val);
	}
	
	//
	// public void setVoxelsizeX(double val) {
	// volumevoxelwidth.setValue(val);
	// }
	//
	// public void setVoxelsizeY(double val) {
	// volumevoxelheight.setValue(val);
	// }
	//
	// public void setVoxelsizeZ(double val) {
	// volumevoxeldepth.setValue(val);
	// }
	//
	// public void getDimensionX(int val) {
	// volumevoxelnumberwidth.setValue(val);
	// }
	//
	// public void getDimensionY(int val) {
	// volumevoxelnumberheight.setValue(val);
	// }
	//
	// public void getDimensionZ(int val) {
	// volumevoxelnumberdepth.setValue(val);
	// }
	//
	// public void setColorDepth(String colordepth) {
	// this.colordepth.setSelectedItem(VolumeColorDepth.getDepthFromString(colordepth));
	// }
	
	public void setCopyFormDataEnabled(boolean enabled) {
		expFormDuplicateBT.setEnabled(enabled);
		condFormDuplicateBT.setEnabled(enabled);
		sampFormDuplicateBT.setEnabled(enabled);
		if (dataDuplicateBT != null)
			dataDuplicateBT.setEnabled(enabled);
	}
	
	public void setAnnotation(ExperimentDataAnnotation ed) {
		
		if (ed == null)
			return;
		
		for (String s : ed.getExpname())
			combonameexp.addItem(s);
		combonameexp.addSelectionOnTab();
		for (String s : ed.getExpsrc())
			comboexpsrc.addItem(s);
		comboexpsrc.addSelectionOnTab();
		for (String s : ed.getExpcoord())
			combocoordinator.addItem(s);
		combocoordinator.addSelectionOnTab();
		for (String s : ed.getExpstartdate())
			combostartdate.addItem(s);
		combostartdate.addSelectionOnTab();
		for (String s : ed.getExpimportdate())
			comboimportdate.addItem(s);
		comboimportdate.addSelectionOnTab();
		for (String s : ed.getCondspecies())
			combospecies.addItem(s);
		combospecies.addSelectionOnTab();
		for (String s : ed.getCondgenotype())
			combogenotype.addItem(s);
		combogenotype.addSelectionOnTab();
		for (String s : ed.getCondtreatment())
			combotreatment.addItem(s);
		combotreatment.addSelectionOnTab();
		for (String s : ed.getSamptimepoint())
			spinnertimepoint.setValue(Integer.parseInt(s));
		for (String s : ed.getSamptimeunit())
			combounit.addItem(s);
		combounit.addSelectionOnTab();
		for (String s : ed.getSampcomp())
			combocomponent.addItem(s);
		combocomponent.addSelectionOnTab();
		for (String s : ed.getSampmeas())
			combomeasurementtool.addItem(s);
		combomeasurementtool.addSelectionOnTab();
		
		if (loader != null)
			loader.setAnnotation(ed);
	}
}