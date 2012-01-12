package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.managers.pluginmgr.PluginDescription;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

public class Addon {
	
	public static URL[] getURLfromJarfile(File jarfiles) {
		try {
			return new URL[] { jarfiles.toURI().toURL() };
		} catch (MalformedURLException e) {
			ErrorMsg.addErrorMessage(e);
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static URL getXMLURL(ClassLoader loader, File jarFile) {
		return loader.getResource(jarFile.getName().replaceAll(".jar", ".xml"));
	}
	
	private File jarfile;
	private URL xmlURL;
	private PluginDescription description;
	private Boolean isactive;
	private ImageIcon icon;
	
	public Addon(File file, URL xmlURL, PluginDescription pd, boolean active, ImageIcon icon) {
		this.jarfile = file;
		this.xmlURL = xmlURL;
		this.description = pd;
		this.isactive = active;
		this.icon = icon;
	}
	
	public URL getXMLURL() {
		return xmlURL;
	}
	
	public PluginDescription getDescription() {
		return description;
	}
	
	public Boolean isActive() {
		return isactive;
	}
	
	void setIsActive(boolean active) {
		isactive = active;
	}
	
	public String getName() {
		return jarfile.getName().toLowerCase().replaceAll(".jar", "");
	}
	
	public File getJarFile() {
		return jarfile;
	}
	
	public ImageIcon getIcon() {
		return icon;
	}
	
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	
	public boolean isTestedWithRunningVersion() {
		// 1.8,2.1, //// 1.8,
		return (description.getCompatibleVersion() + ",").contains(DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE + ",");
	}
	
}
