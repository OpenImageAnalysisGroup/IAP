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
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

import java.io.InputStream;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;

/**
 * @author klukas
 */
public class LoadedVolume extends VolumeData implements LoadedData {
	
	protected ByteShortIntArray volume;
	protected ByteShortIntArray volumeLabelField;
	
	public LoadedVolume(Sample parent, ByteShortIntArray volume) {
		super(parent);
		LoadedDataHandler.registerObject(this);
		this.volume = volume;
		this.volumeLabelField = null;
	}
	
	public LoadedVolume(Sample parent, ByteShortIntArray volume, ByteShortIntArray volumeLabelField) {
		super(parent);
		LoadedDataHandler.registerObject(this);
		this.volume = volume;
		this.volumeLabelField = volumeLabelField;
	}
	
	public LoadedVolume(VolumeData md) {
		super(md != null ? md.getParentSample() : null, md);
	}
	
	public LoadedVolume(VolumeData vd, ByteShortIntArray volume) {
		super(vd.getParentSample(), vd);
		LoadedDataHandler.registerObject(this);
		this.volume = volume;
	}
	
	public LoadedVolume(VolumeData vd, ByteShortIntArray volume, ByteShortIntArray volumeLabelField) {
		super(vd.getParentSample(), vd);
		LoadedDataHandler.registerObject(this);
		this.volume = volume;
		this.volumeLabelField = volumeLabelField;
	}
	
	public ByteShortIntArray getLoadedVolume() {
		return volume;
	}
	
	public ByteShortIntArray getLoadedVolumeLabelField() {
		return volumeLabelField;
	}
	
	public void setVolume(ByteShortIntArray volume) {
		this.volume = volume;
	}
	
	public void setVolumeLabelField(ByteShortIntArray volumeLabelField) {
		this.volumeLabelField = volumeLabelField;
	}
	
	@Override
	public InputStream getInputStream() {
		return volume.getInputStream();
	}
	
	@Override
	public InputStream getInputStreamLabelField() {
		if (volumeLabelField != null)
			return volumeLabelField.getInputStream();
		else
			return null;
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		return new LoadedVolume((VolumeData) super.clone(parent), getLoadedVolume(), getLoadedVolumeLabelField());
	}
	
}
