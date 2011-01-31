package org;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * @author klukas
 */
public class SettingsHelperDefaultIsTrue implements HelperClass {
	
	public boolean isEnabled(String name) {
		return !new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_disabled_" + encode(name)).exists();
	}
	
	public static String encode(String name) {
		return StringManipulationTools.removeHTMLtags(name).replaceAll(" ", "_").replaceAll("/", "_");
	}
	
	public void setEnabled(String name, boolean b) {
		if (!b)
			try {
				new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_disabled_" + encode(name)).createNewFile();
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		else {
			new File(ReleaseInfo.getAppFolderWithFinalSep() + "feature_disabled_" + encode(name)).delete();
		}
	}
	
	public JComponent getBooleanSettingsEditor(String description, final String option, final Runnable enable, final Runnable disable) {
		final JCheckBox result = new JCheckBox(description, isEnabled(option));
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean enabled = isEnabled(option);
				enabled = !enabled;
				setEnabled(option, enabled);
				if (enabled) {
					if (enable != null)
						enable.run();
				} else {
					if (disable != null)
						disable.run();
				}
			}
		});
		Timer t = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean active = isEnabled(option);
				boolean b = active;
				result.setSelected(b);
			}
		});
		t.start();
		return result;
	}
	
}
