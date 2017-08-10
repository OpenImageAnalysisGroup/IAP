package de.ipk.ag_ba.server.databases;

import java.io.InputStream;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;

public class DatabaseTargetNull implements DatabaseTarget {
	
	@Override
	public LoadedImage saveImage(String[] optFileNameMainAndLabelPrefix, LoadedImage limg, boolean keepRemoteLabelURLs_safe_space, boolean skipLabelProcessing) throws Exception {
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
