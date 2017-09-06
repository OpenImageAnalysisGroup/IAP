package de.ipk.ag_ba.server.databases;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.commands.vfs.ActionDataExportToVfs;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;

public class DatabaseTargetNull implements DatabaseTarget {
	
	public boolean saveImagesToSingleFolder;
	public boolean printImagesToConsole;
	public String targetFolder;
	public boolean ignoreTimeInOutputName;
	
	public DatabaseTargetNull() {
		// empty
	}
	
	@Override
	public LoadedImage saveImage(String[] optFileNameMainAndLabelPrefix, LoadedImage limg, boolean keepRemoteLabelURLs_safe_space, boolean skipLabelProcessing) throws Exception {
		if (saveImagesToSingleFolder) {
			LoadedImage imgd = limg;
			
			if (saveImagesToSingleFolder) {
				if (printImagesToConsole) {
					InputStream is = imgd.getInputStream();
					byte[] bytes = new byte[64];
					int numBytes;
					while ((numBytes = is.read(bytes)) != -1) {
						System.out.write(bytes, 0, numBytes);
					}
					is.close();
				} else {
					String desiredFileName = ActionDataExportToVfs.determineBinaryFileName(imgd.getParentSample().getSampleFineTimeOrRowId(), imgd.getSubstanceName(), imgd, imgd, true);
					desiredFileName = desiredFileName.substring(0, desiredFileName.length() - imgd.getURL().getFileNameExtension().length()) + "." + IAPservice.getTargetFileExtension(false, imgd.getURL().getFileNameExtension(), true);
					
					if (desiredFileName.startsWith("_"))
						desiredFileName = desiredFileName.substring("_".length());
					
					desiredFileName = desiredFileName.trim();
					
					Files.copy(imgd.getInputStream(), Paths.get(targetFolder, desiredFileName), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void saveVolume(LoadedVolume volume, Sample3D s3d, MongoDB m, InputStream threeDvolumePreviewIcon, BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		// empty
	}
	
	@Override
	public String getPrefix() {
		return "null";
	}
	
}
