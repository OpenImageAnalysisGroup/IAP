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

import ij.io.FileInfoXYZ;
import ij.io.ImageReader;
import ij.io.TiffDecoderExtended;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeColorDepth;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.VolumeHeader.CheckSize;

public class TIFFVolumeLoaderInstance extends VolumeLoaderInstance {
	
	public TIFFVolumeLoaderInstance(File f, VolumeLoader parent) {
		super(f, parent);
	}
	
	@Override
	protected VolumeHeader getHeaderInformation() throws Exception {
		
		TiffDecoderExtended dec = new TiffDecoderExtended(file.getParent(), file.getName());
		FileInfoXYZ[] infos = dec.getTiffInfo();
		
		VolumeHeader hdr = new VolumeHeader();
		hdr.voxelnbrx = infos[0].width;
		hdr.voxelnbry = infos[0].height;
		hdr.voxelnbrz = infos[0].nImages;
		
		hdr.voxeldimx = infos[0].pixelWidth;
		hdr.voxeldimy = infos[0].pixelHeight;
		hdr.voxeldimz = infos[0].pixelDepth;
		
		hdr.checkFileSize = CheckSize.NONE;
		
		switch (infos[0].getBytesPerPixel()) {
			case 1:
				hdr.fileType = VolumeColorDepth.BIT8;
				break;
			case 2:
				hdr.fileType = VolumeColorDepth.BIT16;
				break;
			case 3:
				hdr.fileType = VolumeColorDepth.RGB;
				break;
			case 4:
				hdr.fileType = VolumeColorDepth.RGBA;
				break;
		}
		
		return hdr;
	}
	
	@Override
	public List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname) {
		List<NumericMeasurementInterface> list = super.addMeasurementsToHierarchy(sample, experimentname);
		
		try {
			
			VolumeData vd = ((VolumeData) list.get(0));
			
			// taken from ImageJ v1.44
			TiffDecoderExtended dec = new TiffDecoderExtended(vd.getURL().getInputStream(), vd.getURL().getFileName());
			FileInfoXYZ[] infos = dec.getTiffInfo();
			
			int bpp = infos[0].getBytesPerPixel();
			
			InputStream is = vd.getURL().getInputStream();
			
			ImageReader ir = new ImageReader(infos[0]);
			
			LoadedVolume vdnew = null;
			
			if (bpp == 1) {
				
				byte[][][] volume = new byte[vd.getDimensionX()][vd.getDimensionY()][vd.getDimensionZ()];
				long offset = 0;
				int x = 0, y = 0, z = 0;
				
				while (offset < vd.getDimensionX() * vd.getDimensionY() * vd.getDimensionZ()) {
					
					byte[] bytes = (byte[]) ir.readPixels(is);
					
					if (bytes == null)
						throw new EOFException();
					
					for (byte b : bytes) {
						x++;
						if (x >= vd.getDimensionX()) {
							x = 0;
							y++;
							if (y >= vd.getDimensionY()) {
								x = 0;
								y = 0;
								z++;
							}
						}
						if (z < vd.getDimensionZ())
							volume[x][y][z] = b;
					}
					offset += bytes.length;
				}
				vdnew = new LoadedVolume(vd, new ByteShortIntArray(volume));
			} else
				if (bpp == 2) {
					
					short[][][] volume = new short[vd.getDimensionX()][vd.getDimensionY()][vd.getDimensionZ()];
					long offset = 0;
					int x = 0, y = 0, z = 0;
					
					while (offset < vd.getDimensionX() * vd.getDimensionY() * vd.getDimensionZ()) {
						
						short[] shorts = (short[]) ir.readPixels(is);
						
						if (shorts == null)
							throw new EOFException();
						
						for (short s : shorts) {
							x++;
							if (x >= vd.getDimensionX()) {
								x = 0;
								y++;
								if (y >= vd.getDimensionY()) {
									x = 0;
									y = 0;
									z++;
								}
							}
							if (z < vd.getDimensionZ())
								volume[x][y][z] = s;
							
						}
						offset += shorts.length;
					}
					vdnew = new LoadedVolume(vd, new ByteShortIntArray(volume));
				} else
					if (bpp == 3 || bpp == 4) {
						
						int[][][] volume = new int[vd.getDimensionX()][vd.getDimensionY()][vd.getDimensionZ()];
						long offset = 0;
						int x = 0, y = 0, z = 0;
						
						while (offset < vd.getDimensionX() * vd.getDimensionY() * vd.getDimensionZ()) {
							
							int[] ints = (int[]) ir.readPixels(is);
							
							if (ints == null)
								throw new EOFException();
							
							for (int i : ints) {
								x++;
								if (x >= vd.getDimensionX()) {
									x = 0;
									y++;
									if (y >= vd.getDimensionY()) {
										x = 0;
										y = 0;
										z++;
									}
								}
								if (z < vd.getDimensionZ())
									volume[x][y][z] = i;
								
							}
							offset += ints.length;
						}
						vdnew = new LoadedVolume(vd, new ByteShortIntArray(volume));
					} else
						throw new IOException("TIFF Volume is neither 1, 2, 3 nor 4 bytes per voxel!");
			
			vd.setParentSample(null);
			
			vdnew.setURL(LoadedDataHandler.getURL(vd.getURL()));
			// vdnew.setLabelURL(null); //TIFF volumes have no labelfield :-)
			return toList(vdnew);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		
	}
}
