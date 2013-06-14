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

import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.ErrorMsg;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.display.SourceImage;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeColorDepth;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.VolumeHeader.CheckSize;

public class DICOMVolumeLoaderInstance extends VolumeLoaderInstance {
	
	private final FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".dcm");
		}
	};
	
	public DICOMVolumeLoaderInstance(File f, VolumeLoader parent) {
		super(f, parent);
	}
	
	@Override
	protected VolumeHeader getHeaderInformation() throws Exception {
		SourceImage i = new SourceImage(file.getAbsolutePath());
		
		String[] dcmfiles = file.getParentFile().list(filter);
		
		AttributeList al = new AttributeList();
		al.read(file);
		Attribute pixelspacing = al.get(new AttributeTag("(0x0028,0x0030)"));
		Attribute slicethickness = al.get(new AttributeTag("(0x0018,0x0050)"));
		
		VolumeHeader hdr = new VolumeHeader();
		hdr.voxelnbrx = i.getWidth();
		hdr.voxelnbry = i.getHeight();
		hdr.voxelnbrz = dcmfiles.length;
		
		hdr.voxeldimx = pixelspacing.getDoubleValues()[0];
		hdr.voxeldimy = pixelspacing.getDoubleValues()[1];
		hdr.voxeldimz = slicethickness.getDoubleValues()[0];
		
		hdr.checkFileSize = CheckSize.APPROXIMATE;
		
		switch ((int) al.get(new AttributeTag("(0x0028,0x0100)")).getDoubleValues()[0]) {
			case 8:
				hdr.fileType = VolumeColorDepth.BIT8;
				break;
			case 16:
				hdr.fileType = VolumeColorDepth.BIT16;
				break;
			case 24:
				hdr.fileType = VolumeColorDepth.RGB;
				break;
			case 32:
				hdr.fileType = VolumeColorDepth.RGBA;
				break;
		}
		
		return hdr;
	}
	
	@Override
	public List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname) {
		List<NumericMeasurementInterface> list = super.addMeasurementsToHierarchy(sample, experimentname);
		
		try {
			VolumeData vd = (VolumeData) list.get(0);
			
			String[] dcmfiles = file.getParentFile().list(filter);
			Arrays.sort(dcmfiles, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
			int z = 0;
			LoadedVolume vdnew;
			if (VolumeColorDepth.getDepthFromString(vd.getColorDepth()) == VolumeColorDepth.BIT8) {
				byte[][][] volume = new byte[vd.getDimensionX()][vd.getDimensionY()][vd.getDimensionZ()];
				
				for (String dcmFile : dcmfiles) {
					byte[] image = ((DataBufferByte) (new SourceImage(file.getParent() + "/" + dcmFile).getBufferedImage()).getRaster().getDataBuffer()).getData();
					
					for (int x = 0; x < vd.getDimensionX(); x++)
						for (int y = 0; y < vd.getDimensionY(); y++)
							volume[x][y][z] = image[x + y * vd.getDimensionX()];
					z++;
				}
				vdnew = new LoadedVolume(vd, new ByteShortIntArray(volume));
				
			} else
				if (VolumeColorDepth.getDepthFromString(vd.getColorDepth()) == VolumeColorDepth.BIT16) {
					short[][][] volume = new short[vd.getDimensionX()][vd.getDimensionY()][vd.getDimensionZ()];
					
					for (String dcmFile : dcmfiles) {
						short[] image = ((DataBufferUShort) (new SourceImage(file.getParent() + "/" + dcmFile).getBufferedImage()).getRaster().getDataBuffer())
											.getData();
						for (int x = 0; x < vd.getDimensionX(); x++)
							for (int y = 0; y < vd.getDimensionY(); y++)
								volume[x][y][z] = image[x + y * vd.getDimensionX()];
						z++;
					}
					vdnew = new LoadedVolume(vd, new ByteShortIntArray(volume));
				} else {
					int[][][] volume = new int[vd.getDimensionX()][vd.getDimensionY()][vd.getDimensionZ()];
					
					for (String dcmFile : dcmfiles) {
						int[] image = ((DataBufferInt) (new SourceImage(file.getParent() + "/" + dcmFile).getBufferedImage()).getRaster().getDataBuffer()).getData();
						for (int x = 0; x < vd.getDimensionX(); x++)
							for (int y = 0; y < vd.getDimensionY(); y++)
								volume[x][y][z] = image[x + y * vd.getDimensionX()];
						z++;
					}
					vdnew = new LoadedVolume(vd, new ByteShortIntArray(volume));
				}
			
			vd.setParentSample(null);
			
			vdnew.setURL(LoadedDataHandler.getURL(vd.getURL()));
			vdnew.setLabelURL(null); // DICOM volumes have no labelfield :-)
			return toList(vdnew);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
}
