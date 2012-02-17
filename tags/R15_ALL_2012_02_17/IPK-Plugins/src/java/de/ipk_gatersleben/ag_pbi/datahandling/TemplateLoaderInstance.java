package de.ipk_gatersleben.ag_pbi.datahandling;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataAnnotation;

public abstract class TemplateLoaderInstance implements TemplateLoaderInterface {
	
	protected static List<NumericMeasurementInterface> toList(NumericMeasurementInterface m) {
		List<NumericMeasurementInterface> list = new ArrayList<NumericMeasurementInterface>();
		list.add(m);
		return list;
	}
	
	protected static Double convertToDouble(Object cellData) {
		if (cellData instanceof String) {
			return Double.parseDouble((String) cellData);
		}
		if (cellData instanceof Double)
			return (Double) cellData;
		return null;
	}
	
	protected File file;
	protected JTextField substancename;
	private final TemplateLoader parent;
	
	public TemplateLoaderInstance(File f, TemplateLoader parent) {
		super();
		this.file = f;
		this.parent = parent;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface#
	 * getAttributeDialog(int)
	 */
	public abstract JPanel getAttributeDialog(int filenumber) throws Exception;
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface#
	 * addMeasurementsToHierarchy
	 * (de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools
	 * .script_helper.Sample, java.lang.String)
	 */
	public abstract List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample,
						String experimentname);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface#getSubstance
	 * ()
	 */
	public String getSubstance() {
		if (substancename == null || substancename.getText().equals(""))
			return "dummy substance";
		else
			return substancename.getText();
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface#setFormularData
	 * (de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface,
	 * java.lang.Object[])
	 */
	public void setFormularData(TemplateLoaderInterface loader, Object[] formularData) {
		// we will only transfer the form data to forms, which are loaded by the
		// same type of loader(== data of same type)
		if (!(loader.getClass().getCanonicalName().equalsIgnoreCase(this.getClass().getCanonicalName())))
			return;
		setFormData(formularData);
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface#getFile()
	 */
	public File getFile() {
		return file;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface#getFormData
	 * ()
	 */
	public abstract Object[] getFormData();
	
	protected abstract void setFormData(Object[] formularData);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInterface#toString()
	 */
	@Override
	public String toString() {
		return parent.toString();
	}
	
	public void setAnnotation(ExperimentDataAnnotation ed) {
		//
	}
	
}
