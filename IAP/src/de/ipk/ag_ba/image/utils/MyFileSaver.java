package de.ipk.ag_ba.image.utils;

import ij.ImagePlus;
import ij.VirtualStack;
import ij.io.FileInfoXYZ;
import ij.io.FileSaver;
import ij.io.RoiEncoder;
import ij.io.TiffEncoder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.ErrorMsg;

public class MyFileSaver extends FileSaver {
	
	public MyFileSaver(ImagePlus imp) {
		super(imp);
	}
	
	public boolean saveAsTiffStack(OutputStream os) {
		if (fi.nImages == 1) {
			ErrorMsg.addErrorMessage("This is not a stack");
			return false;
		}
		if (imp.getStack().isVirtual())
			fi.virtualStackXYZ = (VirtualStack) imp.getStack();
		Object info = imp.getProperty("Info");
		if (info != null && (info instanceof String))
			fi.info = (String) info;
		fi.description = getDescriptionString();
		fi.sliceLabels = imp.getStack().getSliceLabels();
		fi.roi = RoiEncoder.saveAsByteArray(imp.getRoi());
		fi.overlay = getOverlay(imp);
		if (imp.isComposite())
			saveDisplayRangesAndLuts(imp, fi);
		try {
			TiffEncoder file = new TiffEncoder(fi);
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(os));
			file.write(out);
			out.close();
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
			return false;
		}
		updateImp(fi, FileInfoXYZ.TIFF);
		return true;
	}
}
