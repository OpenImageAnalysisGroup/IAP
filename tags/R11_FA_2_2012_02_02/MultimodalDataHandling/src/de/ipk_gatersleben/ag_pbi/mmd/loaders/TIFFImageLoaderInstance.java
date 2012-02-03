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

import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.io.TiffDecoder;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.List;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

public class TIFFImageLoaderInstance extends ImageLoaderInstance {
	
	public TIFFImageLoaderInstance(File f, ImageLoader parent) {
		super(f, parent);
	}
	
	@Override
	public List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname) {
		List<NumericMeasurementInterface> list = super.addMeasurementsToHierarchy(sample, experimentname);
		
		try {
			
			ImageData id = ((ImageData) list.get(0));
			
			// taken from ImageJ v1.44
			TiffDecoder dec = new TiffDecoder(id.getURL().getInputStream(), id.getURL().getFileName());
			FileInfo[] infos = dec.getTiffInfo();
			ImageReader ir = new ImageReader(infos[0]);
			
			Object result = ir.readPixels(id.getURL().getInputStream());
			
			BufferedImage image = null;
			
			if (infos[0].getBytesPerPixel() == 1) {
				image = new BufferedImage(infos[0].width, infos[0].height, BufferedImage.TYPE_BYTE_GRAY);
				byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				System.arraycopy(result, 0, data, 0, ((byte[]) result).length);
			} else
				if (infos[0].getBytesPerPixel() == 2) {
					image = new BufferedImage(infos[0].width, infos[0].height, BufferedImage.TYPE_BYTE_GRAY);
					byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
					for (int i = 0; i < ((short[]) result).length; i++)
						data[i] = (byte) ((((short[]) result)[i] & 0x00FF));
				} else
					if (infos[0].getBytesPerPixel() == 3) {
						image = new BufferedImage(infos[0].width, infos[0].height, BufferedImage.TYPE_INT_RGB);
						int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
						System.arraycopy(result, 0, data, 0, ((int[]) result).length);
					} else
						if (infos[0].getBytesPerPixel() == 4) {
							image = new BufferedImage(infos[0].width, infos[0].height, BufferedImage.TYPE_INT_ARGB);
							int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
							System.arraycopy(result, 0, data, 0, ((int[]) result).length);
						}
			
			if (image == null) {
				ErrorMsg.addErrorMessage("\"tif\" filetype could not be detected");
				return null;
			}
			
			LoadedImage idnew = new LoadedImage(id, image);
			
			id.setParentSample(null);
			
			idnew.setURL(LoadedDataHandler.getURL(id.getURL()));
			
			MainFrame.showMessage("Converting tif image " + id.getURL().getFileName(), MessageType.INFO);
			
			return toList(idnew);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		
	}
	
}
