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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.util.List;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.display.SourceImage;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

public class DICOMImageLoaderInstance extends ImageLoaderInstance {
	
	public DICOMImageLoaderInstance(File f, ImageLoader parent) {
		super(f, parent);
	}
	
	@Override
	public List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname) {
		List<NumericMeasurementInterface> list = super.addMeasurementsToHierarchy(sample, experimentname);
		
		try {
			
			ImageData id = ((ImageData) list.get(0));
			
			SourceImage old = new SourceImage(new DicomInputStream(file));
			BufferedImage oldImage = old.getBufferedImage();
			
			AttributeList al = new AttributeList();
			al.read(file);
			
			BufferedImage image = null;
			
			switch ((int) al.get(new AttributeTag("(0x0028,0x0100)")).getDoubleValues()[0]) {
				case 8:
					image = new BufferedImage(oldImage.getWidth(), oldImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
					byte[] src8 = ((DataBufferByte) oldImage.getRaster().getDataBuffer()).getData();
					System.arraycopy(src8, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData(), 0, src8.length);
					break;
				case 16:
					image = new BufferedImage(oldImage.getWidth(), oldImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
					byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
					int cnt = 0;
					for (short s : ((DataBufferUShort) oldImage.getRaster().getDataBuffer()).getData())
						data[cnt++] = (byte) (s & 0x00FF); // TODO: not the right conversion!!!
					break;
				case 24: // do the same as for 32
				case 32:
					image = new BufferedImage(oldImage.getWidth(), oldImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
					int[] src24 = ((DataBufferInt) oldImage.getRaster().getDataBuffer()).getData();
					System.arraycopy(src24, 0, ((DataBufferInt) image.getRaster().getDataBuffer()).getData(), 0, src24.length);
			}
			
			if (image == null) {
				ErrorMsg.addErrorMessage("\"dcm\" filetype could not be detected");
				return null;
			}
			
			LoadedImage idnew = new LoadedImage(id, image);
			
			id.setParentSample(null);
			
			idnew.setURL(LoadedDataHandler.getURL(id.getURL()));
			
			MainFrame.showMessage("Converting dicom image " + id.getURL().getFileName(), MessageType.INFO);
			
			return toList(idnew);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		
	}
	
}
