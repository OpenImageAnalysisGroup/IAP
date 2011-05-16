package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

/** This plugin opens a multi-page TIFF file as a virtual stack. It
	implements the File/Import/TIFF Virtual Stack command. */
public class FileInfoVirtualStack extends VirtualStack implements PlugIn {
	FileInfoXYZ[] info;
	int nImages;
	
	/* Default constructor. */
	public FileInfoVirtualStack() {}

	/* Constructs a FileInfoVirtualStack from a FileInfo object. */
	public FileInfoVirtualStack(FileInfoXYZ fi) {
		info = new FileInfoXYZ[1];
		info[0] = fi;
		open(true);
	}

	/* Constructs a FileInfoVirtualStack from a FileInfo 
		object and displays it if 'show' is true. */
	public FileInfoVirtualStack(FileInfoXYZ fi, boolean show) {
		info = new FileInfoXYZ[1];
		info[0] = fi;
		open(show);
	}

	public void run(String arg) {
		OpenDialog  od = new OpenDialog("Open TIFF", arg);
		String name = od.getFileName();
		if (name==null) return;
		if (name.endsWith(".zip")) {
			IJ.error("Virtual Stack", "ZIP compressed stacks not supported");
			return;
		}
		String  dir = od.getDirectory();
		TiffDecoder td = new TiffDecoder(dir, name);
		if (IJ.debugMode) td.enableDebugging();
		IJ.showStatus("Decoding TIFF header...");
		try {info = td.getTiffInfo();}
		catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null||msg.equals("")) msg = ""+e;
			IJ.error("TiffDecoder", msg);
			return;
		}
		if (info==null || info.length==0) {
			IJ.error("Virtual Stack", "This does not appear to be a TIFF stack");
			return;
		}
		if (IJ.debugMode)
			IJ.log(info[0].debugInfo);
		open(true);
	}
	
	void open(boolean show) {
		FileInfoXYZ fi = info[0];
		int n = fi.nImages;
		if (info.length==1 && n>1) {
			info = new FileInfoXYZ[n];
			long size = fi.width*fi.height*fi.getBytesPerPixel();
			for (int i=0; i<n; i++) {
				info[i] = (FileInfoXYZ)fi.clone();
				info[i].nImages = 1;
				info[i].longOffset = fi.getOffset() + i*(size + fi.gapBetweenImages);
			}
		}
		nImages = info.length;
		FileOpener fo = new FileOpener(info[0] );
		ImagePlus imp = fo.open(false);
		Properties props = fo.decodeDescriptionString(fi);
		ImagePlus imp2 = new ImagePlus(fi.fileName, this);
		imp2.setFileInfo(fi);
		if (imp!=null && props!=null) {
			imp2.setCalibration(imp.getCalibration());
			imp2.setOverlay(imp.getOverlay());
			if (fi.info!=null)
				imp2.setProperty("Info", fi.info);
			int channels = getInt(props,"channels");
			int slices = getInt(props,"slices");
			int frames = getInt(props,"frames");
			if (channels*slices*frames==nImages) {
				imp2.setDimensions(channels, slices, frames);
				if (getBoolean(props, "hyperstack"))
					imp2.setOpenAsHyperStack(true);
			}
			if (channels>1 && fi.description!=null) {
				int mode = CompositeImage.COMPOSITE;
				if (fi.description.indexOf("mode=color")!=-1)
					mode = CompositeImage.COLOR;
				else if (fi.description.indexOf("mode=gray")!=-1)
					mode = CompositeImage.GRAYSCALE;
				imp2 = new CompositeImage(imp2, mode);
			}
		}
		if (show) imp2.show();
	}

	int getInt(Properties props, String key) {
		Double n = getNumber(props, key);
		return n!=null?(int)n.doubleValue():1;
	}

	Double getNumber(Properties props, String key) {
		String s = props.getProperty(key);
		if (s!=null) {
			try {
				return Double.valueOf(s);
			} catch (NumberFormatException e) {}
		}	
		return null;
	}

	boolean getBoolean(Properties props, String key) {
		String s = props.getProperty(key);
		return s!=null&&s.equals("true")?true:false;
	}

	/** Deletes the specified image, were 1<=n<=nImages. */
	public void deleteSlice(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (nImages<1) return;
		for (int i=n; i<nImages; i++)
			info[i-1] = info[i];
		info[nImages-1] = null;
		nImages--;
	}
	
	/** Returns an ImageProcessor for the specified image,
		were 1<=n<=nImages. Returns null if the stack is empty.
	*/
	public ImageProcessor getProcessor(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (IJ.debugMode) IJ.log("FileInfoVirtualStack: "+n+", "+info[n-1].getOffset());
		//if (n>1) IJ.log("  "+(info[n-1].getOffset()-info[n-2].getOffset()));
		info[n-1].nImages = 1; // why is this needed?
		FileOpener fo = new FileOpener(info[n-1]);
		ImagePlus imp = fo.open(false);
		if (imp!=null)
			return imp.getProcessor();
		else
			return null;
	 }
 
	 /** Returns the number of images in this stack. */
	public int getSize() {
		return nImages;
	}

	/** Returns the label of the Nth image. */
	public String getSliceLabel(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (info[0].sliceLabels==null || info[0].sliceLabels.length!=nImages)
			return null;
		else
			return info[0].sliceLabels[n-1];
	}

	public int getWidth() {
		return info[0].width;
	}
	
	public int getHeight() {
		return info[0].height;
	}
    
}
