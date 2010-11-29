/*******************************************************************************
 * 
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.rmi_server.databases;

import java.io.InputStream;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;

/**
 * @author klukas
 * 
 */
public interface DatabaseTarget {

	LoadedImage saveImage(LoadedImage limg, String login, String pass) throws Exception;

	void saveVolume(final LoadedVolume volume, Sample3D s3d, String login, String pass, DBTable sample,
						InputStream threeDvolumePreviewIcon,
						String md5, BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception;
}