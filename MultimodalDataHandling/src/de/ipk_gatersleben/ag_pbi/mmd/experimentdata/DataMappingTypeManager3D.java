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
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.DataMappingTypeManagerInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleAverage;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public class DataMappingTypeManager3D implements DataMappingTypeManagerInterface {
	
	public static void replaceVantedMappingTypeManager() {
		Experiment.setTypeManager(new DataMappingTypeManager3D());
	}
	
	private DataMappingTypeManager3D() {
		super();
	}
	
	@Override
	public NumericMeasurementInterface getNewMeasurement(SampleInterface sampleData) {
		return new NumericMeasurement3D(sampleData);
	}
	
	@Override
	public SampleInterface getNewSample(ConditionInterface seriesData) {
		return new Sample3D(seriesData);
	}
	
	@Override
	public ConditionInterface getNewCondition(SubstanceInterface md) {
		return new Condition3D(md);
	}
	
	@Override
	public SubstanceInterface getNewSubstance() {
		return new Substance3D();
	}
	
	@Override
	public SampleAverage getNewSampleAverage(SampleInterface sample) {
		return new SampleAverage3D(sample);
	}
	
	@Override
	public NumericMeasurementInterface getNewMeasurementOfType(String type, SampleInterface sample) {
		if (type != null && type.equals(NumericMeasurement.typeName))
			return getNewMeasurement(sample);
		else
			if (type != null && type.equals(ImageData.typeName))
				return new ImageData(sample);
			else
				if (type != null && type.equals(VolumeData.typeName))
					return new VolumeData(sample);
				else
					if (type != null && type.equals(NetworkData.typeName))
						return new NetworkData(sample);
					else
						return null;
	}
	
	@Override
	public boolean isKnownMeasurementType(String type) {
		return type.equals(NumericMeasurement.typeName)
				|| type.equals(ImageData.typeName)
				|| type.equals(VolumeData.typeName)
				|| type.equals(NetworkData.typeName);
	}
	
}
