package de.ipk.ag_ba.gui.util;

import java.io.File;

import de.ipk.ag_ba.gui.picture_gui.DataExchangeHelperForExperiments;
import de.ipk.ag_ba.gui.picture_gui.DataSetFileButton;
import de.ipk.ag_ba.mongo.DatabaseStorageResultWithURL;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;

public class ExperimentIoHelper {
	
	private MongoDB m;
	
	public ExperimentIoHelper(MongoDB m) {
		this.m = m;
	}
	
	public DatabaseStorageResultWithURL insertHashedFile(File file,
			File createTempPreviewImage, int isJavaImage,
			DataSetFileButton imageButton,
			MappingDataEntity targetEntity) {
		return DataExchangeHelperForExperiments.insertHashedFile(m, file,
				createTempPreviewImage, isJavaImage,
				imageButton, targetEntity);
		
	}
	
}
