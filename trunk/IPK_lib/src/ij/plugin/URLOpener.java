package ij.plugin;
import java.awt.*;
import java.io.*;
import java.net.URL;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.plugin.frame.*;

/** Opens TIFFs, ZIP compressed TIFFs, DICOMs, GIFs and JPEGs using a URL. 
	TIFF file names must end in ".tif", ZIP file names must end 
	in ".zip" and DICOM file names must end in ".dcm". 
	Opens a Web page in the default browser if the URL ends with "/".
*/
public class URLOpener implements PlugIn {

	private static String url = IJ.URL+"/images/clown.gif";

	/** If 'urlOrName' is a URL, opens the image at that URL. If it is
		a file name, opens the image with that name from the 'images.location'
		URL in IJ_Props.txt. If it is blank, prompts for an image
		URL and open the specified image. */
	public void run(String urlOrName) {
		if (!urlOrName.equals("")) {
			if (urlOrName.endsWith("StartupMacros.txt"))
				openTextFile(urlOrName, true);
			else {
				String url = urlOrName.indexOf("://")>0?urlOrName:Prefs.getImagesURL()+urlOrName;
				ImagePlus imp = new ImagePlus(url);
				if (Recorder.record)
					Recorder.recordCall("imp = IJ.openImage(\""+url+"\");");
				if (imp.getType()==ImagePlus.COLOR_RGB)
					Opener.convertGrayJpegTo8Bits(imp);
				WindowManager.checkForDuplicateName = true;
				FileInfoXYZ fi = imp.getOriginalFileInfo();
				if (fi!=null && fi.fileType==FileInfoXYZ.RGB48)
					imp = new CompositeImage(imp, CompositeImage.COMPOSITE);
				else if (imp.getNChannels()>1 && fi!=null && fi.description!=null && fi.description.indexOf("mode=")!=-1) {
					int mode = CompositeImage.COLOR;
					if (fi.description.indexOf("mode=composite")!=-1)
						mode = CompositeImage.COMPOSITE;
					else if (fi.description.indexOf("mode=gray")!=-1)
						mode = CompositeImage.GRAYSCALE;
					imp = new CompositeImage(imp, mode);
				}
				if (fi!=null && (fi.url==null || fi.url.length()==0)) {
					fi.url = url;
					imp.setFileInfo(fi);
				}
				imp.show();
			}
			return;
		}
		
		GenericDialog gd = new GenericDialog("Enter a URL");
		gd.addMessage("Enter URL of an image, macro or web page");
		gd.addStringField("URL:", url, 45);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		url = gd.getNextString();
		url = url.trim();
		if (url.indexOf("://")==-1)
			url = "http://" + url;
		if (url.endsWith("/"))
			IJ.runPlugIn("ij.plugin.BrowserLauncher", url.substring(0, url.length()-1));
		else if (url.endsWith(".html") || url.endsWith(".htm") ||  url.indexOf(".html#")>0 || noExtension(url))
			IJ.runPlugIn("ij.plugin.BrowserLauncher", url);
		else if (url.endsWith(".txt")||url.endsWith(".ijm")||url.endsWith(".js")||url.endsWith(".java"))
			openTextFile(url, false);
		else if (url.endsWith(".jar")||url.endsWith(".class"))
			IJ.open(url);
		else {
			IJ.showStatus("Opening: " + url);
			ImagePlus imp = new ImagePlus(url);
			WindowManager.checkForDuplicateName = true;
			FileInfoXYZ fi = imp.getOriginalFileInfo();
			if (fi!=null && fi.fileType==FileInfoXYZ.RGB48)
				imp = new CompositeImage(imp, CompositeImage.COMPOSITE);
			imp.show();
			IJ.showStatus("");
		}
		IJ.register(URLOpener.class);  // keeps this class from being GC'd
	}
	
	boolean noExtension(String url) {
		int lastSlash = url.lastIndexOf("/");
		if (lastSlash==-1) lastSlash = 0;
		int lastDot = url.lastIndexOf(".");
		if (lastDot==-1 || lastDot<lastSlash || (url.length()-lastDot)>6)
			return true;  // no extension
		else
			return false;
	}
	
	void openTextFile(String urlString, boolean install) {
		StringBuffer sb = null;
		try {
			URL url = new URL(urlString);
			InputStream in = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			sb = new StringBuffer() ;
			String line;
			while ((line=br.readLine()) != null)
				sb.append (line + "\n");
			in.close ();
		} catch (IOException e) {
			if  (!(install&&urlString.endsWith("StartupMacros.txt")))
				IJ.error("URL Opener", ""+e);
			sb = null;
		}
		if (sb!=null) {
			if (install)
				(new MacroInstaller()).install(new String(sb));
			else {
				int index = urlString.lastIndexOf("/");
				if (index!=-1 && index<=urlString.length()-1)
					urlString = urlString.substring(index+1);
				(new Editor()).create(urlString, new String(sb));
			}
		}
	}
 
}
