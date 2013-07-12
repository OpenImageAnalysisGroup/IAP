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

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInstance;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;

public class NetworkLoaderInstance extends TemplateLoaderInstance {
	
	private JTextField networkname;
	private JTextField networkgroup;
	private JTextField networksource;
	
	public NetworkLoaderInstance(File f, NetworkLoader parent) {
		super(f, parent);
	}
	
	@Override
	public JPanel getAttributeDialog(int filenumber) throws Exception {
		JPanel pan = new JPanel();
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
		String filename = file.getName(), group = null, name = null;
		if (filename.indexOf(".") != filename.lastIndexOf(".")) {
			name = filename.substring(0, filename.lastIndexOf(".")); // remove file
			// extension
			group = name.substring(0, name.lastIndexOf("."));
			name = name.substring(name.lastIndexOf(".") + ".".length());
		}
		
		networkname = new JTextField(name != null ? name : "");
		pan.add(TableLayout.getSplit(new JLabel("Name"), networkname, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		networkgroup = new JTextField(group != null ? group : "");
		pan.add(TableLayout.getSplit(new JLabel("Group*"), networkgroup, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		networksource = new JTextField();
		pan.add(TableLayout.getSplit(new JLabel("Source"), networksource, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		return pan;
	}
	
	@Override
	public List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname) {
		NetworkData nd = new NetworkData(sample);
		nd.setURL(FileSystemHandler.getURL(file));
		File labelfield = new File(file.getAbsolutePath() + ".labelfield");
		if (labelfield.canRead())
			nd.setLabelURL(FileSystemHandler.getURL(labelfield));
		
		nd.setName(getNetworkName());
		nd.setGroup(getNetworkGroup());
		nd.setSource(getNetworkSource());
		
		return toList(nd);
	}
	
	private String getNetworkName() {
		String val = networkname.getText();
		if (val.equals(""))
			val = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return val;
	}
	
	private String getNetworkGroup() {
		String val = networkgroup.getText();
		if (val.equals(""))
			val = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return val;
	}
	
	private String getNetworkSource() {
		String val = networksource.getText();
		if (val.equals(""))
			val = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return val;
	}
	
	public void setNetworkName(String val) {
		networkname.setText(val);
	}
	
	public void setNetworkGroup(String val) {
		networkgroup.setText(val);
	}
	
	public void setNetworkSource(String val) {
		networksource.setText(val);
	}
	
	@Override
	public Object[] getFormData() {
		return new Object[] { getNetworkGroup() };
	}
	
	@Override
	protected void setFormData(Object[] formularData) {
		setNetworkGroup((String) formularData[0]);
	}
	
}
