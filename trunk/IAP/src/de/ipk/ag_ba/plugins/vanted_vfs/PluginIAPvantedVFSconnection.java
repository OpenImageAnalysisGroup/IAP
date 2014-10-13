package de.ipk.ag_ba.plugins.vanted_vfs;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPvantedVFSconnection extends AbstractIAPplugin {
	
	public PluginIAPvantedVFSconnection() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP-VANTED VFS features plugin is beeing loaded");
	}
	
	@Override
	public Algorithm[] getAlgorithms() {
		return new Algorithm[] {
				new OpenFromVFSalgorithm(),
				new SaveInVFSalgorithm()
		};
	}
}