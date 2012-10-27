package org;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * @author klukas
 */
public class SettingsHelperDefaultIsFalse extends SettingsHelperDefaultIsTrue {
	
	boolean oldStyle = SettingsHelperDefaultIsTrue.oldStyle;
	
	private static HashSet<String> falseSettings = new HashSet<String>();
	
	@Override
	public synchronized boolean isEnabled(String name) {
		falseSettings.add(name);
		if (oldStyle)
			return new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_enabled_" + encode(name)).exists();
		else
			return SystemOptions.getInstance().getBoolean("VANTED", name, false);
	}
	
	@Override
	public synchronized void setEnabled(String name, boolean b) {
		falseSettings.add(name);
		if (oldStyle) {
			if (b)
				try {
					new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_enabled_" + encode(name)).createNewFile();
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
				}
			else {
				new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_enabled_" + encode(name)).delete();
			}
		} else {
			SystemOptions.getInstance().setBoolean("VANTED", name, b);
		}
	}
	
	public synchronized static void resetAllKnown() {
		for (String n : falseSettings)
			new SettingsHelperDefaultIsFalse().setEnabled(n, false);
	}
}
