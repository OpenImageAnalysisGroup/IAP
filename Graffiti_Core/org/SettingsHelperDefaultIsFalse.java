package org;

import java.io.File;
import java.io.IOException;

/**
 * @author klukas
 */
public class SettingsHelperDefaultIsFalse extends SettingsHelperDefaultIsTrue {
	
	public boolean isEnabled(String name) {
		return new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_enabled_" + encode(name)).exists();
	}
	
	public void setEnabled(String name, boolean b) {
		if (b)
			try {
				new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_enabled_" + encode(name)).createNewFile();
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		else {
			new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_enabled_" + encode(name)).delete();
		}
	}
}
