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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.FolderPanel;
import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInstance;
import de.ipk_gatersleben.ag_pbi.mmd.JSpinnerSelectOnTab;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

public class GradientLoaderInstance extends TemplateLoaderInstance {
	
	private ArrayList<OmicsValue> values;
	
	public GradientLoaderInstance(File f, GradientLoader parent) {
		super(f, parent);
	}
	
	@Override
	public List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname) {
		return getValues(sample);
	}
	
	@Override
	public JPanel getAttributeDialog(int filenumber) throws Exception {
		JPanel pan = new JPanel();
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
		substancename = new JTextField();
		pan.add(TableLayout.getSplit(new JLabel("Substance"), substancename, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		pan.add(new JPanel());
		FolderPanel omics = new FolderPanel("Gradient values");
		omics.enableSearch(false);
		omics.setShowCondenseButton(false);
		omics.setCondensedState(false);
		omics.setMaximumRowCount(1, true);
		omics.setOpaque(false);
		pan.add(omics);
		
		values = new ArrayList<OmicsValue>();
		
		int unitnbr = 2;
		int posunitnbr = 1;
		TableData td = ExperimentDataFileReader.getExcelTableData(file);
		if ((td.getCellData(1, 1, "") + "").startsWith("Value")) {
			unitnbr = 1;
			posunitnbr = 2;
		}
		String unitstring = td.getCellData(unitnbr, 1, "") + "", posunitstring = td.getCellData(posunitnbr, 1, "") + "";
		
		unitstring = StringManipulationTools.stringReplace(unitstring, "Value", "");
		unitstring = StringManipulationTools.stringReplace(unitstring, "(", "");
		unitstring = StringManipulationTools.stringReplace(unitstring, ")", "").trim();
		posunitstring = StringManipulationTools.stringReplace(posunitstring, "Position", "");
		posunitstring = StringManipulationTools.stringReplace(posunitstring, "(", "");
		posunitstring = StringManipulationTools.stringReplace(posunitstring, ")", "").trim();
		
		for (int row = 2; row < td.getMaximumRow(); row++) {
			values.add(new OmicsValue(td, row, unitstring, posunitstring));
			omics.addGuiComponentRow(null, values.get(values.size() - 1), true);
		}
		return pan;
	}
	
	private class OmicsValue extends JPanel {
		private static final long serialVersionUID = 1L;
		private final JSpinnerSelectOnTab replicate;
		private final JSpinnerSelectOnTab omicsvalue;
		private final JTextField unit;
		private final JSpinnerSelectOnTab position;
		private final JTextField positionunit;
		
		private OmicsValue(TableData td, int row, String unitstring, String posunitstring) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setOpaque(false);
			replicate = new JSpinnerSelectOnTab(new SpinnerNumberModel(0, 0, 100000, 1));
			add(TableLayout.getSplit(new JLabel("Replicate ID"), replicate, 150, TableLayout.FILL));
			replicate.setOpaque(false);
			double val = convertToDouble(td.getCellData(2, row, -1d));
			omicsvalue = new JSpinnerSelectOnTab(new SpinnerNumberModel(val, 0d, 100000d, 0.01d));
			add(TableLayout.getSplit(new JLabel("Value"), omicsvalue, 150, TableLayout.FILL));
			omicsvalue.setOpaque(false);
			unit = new JTextField(unitstring);
			add(TableLayout.getSplit(new JLabel("Unit"), unit, 150, TableLayout.FILL));
			unit.setOpaque(false);
			double pos = convertToDouble(td.getCellData(1, row, -1d));
			position = new JSpinnerSelectOnTab(new SpinnerNumberModel(pos, 0d, 100000d, 0.01d));
			position.setOpaque(false);
			add(TableLayout.getSplit(new JLabel("Position"), position, 150, TableLayout.FILL));
			positionunit = new JTextField(posunitstring);
			add(TableLayout.getSplit(new JLabel("Position Unit"), positionunit, 150, TableLayout.FILL));
			positionunit.setOpaque(false);
		}
		
	}
	
	public List<NumericMeasurementInterface> getValues(SampleInterface sample) {
		ArrayList<NumericMeasurementInterface> list = new ArrayList<NumericMeasurementInterface>();
		for (OmicsValue ov : values) {
			NumericMeasurement3D meas = new NumericMeasurement3D(sample);
			meas.setReplicateID((Integer) ov.replicate.getValue());
			meas.setUnit(ov.unit.getText());
			meas.setPosition((Double) ov.position.getValue());
			meas.setPositionUnit(ov.positionunit.getText());
			meas.setValue((Double) ov.omicsvalue.getValue());
			list.add(meas);
		}
		return list;
	}
	
	@Override
	public Object[] getFormData() {
		return null;
	}
	
	@Override
	protected void setFormData(Object[] formularData) {
		// no formulars to transfer
	}
	
}
