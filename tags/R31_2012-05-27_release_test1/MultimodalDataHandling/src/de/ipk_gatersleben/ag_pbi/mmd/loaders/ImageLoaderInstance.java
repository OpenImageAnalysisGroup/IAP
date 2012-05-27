/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
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

import java.io.File;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInstance;
import de.ipk_gatersleben.ag_pbi.mmd.JSpinnerSelectOnTab;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class ImageLoaderInstance extends TemplateLoaderInstance {
	
	private JSpinnerSelectOnTab replicate;
	private JTextField unit;
	private JSpinnerSelectOnTab position;
	private JTextField positionunit;
	private JSpinnerSelectOnTab imagepixelwidth;
	private JSpinnerSelectOnTab imagepixelheight;
	private JSpinnerSelectOnTab imagethickness;
	private boolean preventBinaryFileCopy;
	
	public ImageLoaderInstance(File f, ImageLoader parent) {
		super(f, parent);
	}
	
	@Override
	public void setAnnotation(ExperimentDataAnnotation ed) {
		if (ed == null)
			return;
		
		if (ed.getSubstances() != null && ed.getSubstances().size() > 0)
			substancename.setText(ed.getSubstances().iterator().next());
		if (ed.getReplicateIDs() != null && ed.getReplicateIDs().size() > 0)
			replicate.setValue(ed.getReplicateIDs().iterator().next());
		if (ed.getPositions() != null && ed.getPositions().size() > 0)
			position.setValue(ed.getPositions().iterator().next());
		if (ed.getPositionUnits() != null && ed.getPositions().size() > 0)
			positionunit.setText(ed.getPositionUnits().iterator().next());
	}
	
	@Override
	public List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname) {
		ImageData id = new ImageData(sample);
		id.setURL(FileSystemHandler.getURL(file));
		File labelfield = new File(file.getAbsolutePath() + ".labelfield");
		if (labelfield.canRead())
			id.setLabelURL(FileSystemHandler.getURL(labelfield));
		id.setReplicateID(getReplicate());
		id.setUnit(getUnit());
		id.setPosition(getPosition());
		id.setPositionUnit(getPositionUnit());
		id.setPixelsizeX(getPixelsizeX());
		id.setPixelsizeY(getPixelsizeY());
		id.setThickness(getImageThickness());
		
		return toList(id);
	}
	
	@Override
	public JPanel getAttributeDialog(int filenumber) throws Exception {
		JPanel pan = new JPanel();
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
		substancename = new JTextField();
		pan.add(TableLayout.getSplit(new JLabel("Substance*"), substancename, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		replicate = new JSpinnerSelectOnTab(new SpinnerNumberModel(filenumber, 0, 100000, 1));
		pan.add(TableLayout.getSplit(new JLabel("Replicate ID"), replicate, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		unit = new JTextField();
		pan.add(TableLayout.getSplit(new JLabel("Unit*"), unit, ImportDialogFile.LEFTSIZE, ImportDialogFile.RIGHTSIZE));
		position = new JSpinnerSelectOnTab(new SpinnerNumberModel(0d, 0d, 100000d, 0.01d));
		pan.add(TableLayout.getSplit(new JLabel("Position"), position, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		positionunit = new JTextField();
		pan.add(TableLayout.getSplit(new JLabel("Position Unit*"), positionunit, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		imagepixelwidth = new JSpinnerSelectOnTab(new SpinnerNumberModel(5d, 0d, 100d, 0.01d));
		pan.add(TableLayout.getSplit(new JLabel("Pixel Width*"), imagepixelwidth, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		imagepixelheight = new JSpinnerSelectOnTab(new SpinnerNumberModel(5d, 0d, 100d, 0.01d));
		pan.add(TableLayout.getSplit(new JLabel("Pixel Height*"), imagepixelheight, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		imagethickness = new JSpinnerSelectOnTab(new SpinnerNumberModel(0d, 0d, 100d, 0.01d));
		pan.add(TableLayout.getSplit(new JLabel("Thickness*"), imagethickness, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		return pan;
	}
	
	public int getReplicate() {
		return (Integer) replicate.getValue();
	}
	
	public String getUnit() {
		return unit.getText();
	}
	
	public double getPosition() {
		return (Double) position.getValue();
	}
	
	public String getPositionUnit() {
		return positionunit.getText();
	}
	
	public double getPixelsizeX() {
		return (Double) imagepixelwidth.getValue();
	}
	
	public double getPixelsizeY() {
		return (Double) imagepixelheight.getValue();
	}
	
	public double getImageThickness() {
		return (Double) imagethickness.getValue();
	}
	
	public void setReplicate(int val) {
		replicate.setValue(val);
	}
	
	public void setSubstance(String val) {
		substancename.setText(val);
	}
	
	public void setUnit(String val) {
		unit.setText(val);
	}
	
	public void setPosition(int val) {
		position.setValue(val);
	}
	
	public void setPositionUnit(String val) {
		positionunit.setText(val);
	}
	
	public void setPixelsizeX(double val) {
		imagepixelwidth.setValue(val);
	}
	
	public void setPixelsizeY(double val) {
		imagepixelheight.setValue(val);
	}
	
	public void setImageThickness(double val) {
		imagethickness.setValue(val);
	}
	
	@Override
	public Object[] getFormData() {
		return new Object[] { getSubstance(), getUnit(), getPositionUnit(), getPixelsizeX(), getPixelsizeY(),
							getImageThickness() };
	}
	
	@Override
	protected void setFormData(Object[] formularData) {
		int idx = 0;
		setSubstance((String) formularData[idx++]);
		setUnit((String) formularData[idx++]);
		setPositionUnit((String) formularData[idx++]);
		setPixelsizeX((Double) formularData[idx++]);
		setPixelsizeY((Double) formularData[idx++]);
		setImageThickness((Double) formularData[idx++]);
	}
	
	public void setPreventBinaryFileCopy(boolean preventBinaryFileCopy) {
		this.preventBinaryFileCopy = preventBinaryFileCopy;
	}
	
	public boolean isPreventBinaryFileCopy() {
		return preventBinaryFileCopy;
	}
}
