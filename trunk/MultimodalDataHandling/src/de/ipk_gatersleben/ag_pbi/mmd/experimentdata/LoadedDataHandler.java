/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
/*
 * Created on Sep 21, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;

/**
 * @author klukas
 */
public class LoadedDataHandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "loaded";
	
	private static LoadedDataHandler instance = new LoadedDataHandler();
	
	private LoadedDataHandler() {
		
	}
	
	public static LoadedDataHandler getInstance() {
		return instance;
	}
	
	private static ArrayList<WeakReference<LoadedData>> known = new ArrayList<WeakReference<LoadedData>>();
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
						throws Exception {
		return null;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		synchronized (known) {
			ArrayList<WeakReference<LoadedData>> del = null;
			LoadedData result = null;
			boolean useLabelURL = false;
			for (WeakReference<LoadedData> wr : known) {
				LoadedData li = wr.get();
				if (li == null) {
					if (del == null)
						del = new ArrayList<WeakReference<LoadedData>>();
					del.add(wr);
				} else {
					if (li.getURL() == url || li.getURL().toString().equals(url.toString())) {
						result = li;
						break;
					}
					if (li.getLabelURL() != null)
						if (li.getLabelURL() == url || li.getLabelURL().toString().equals(url.toString())) {
							result = li;
							useLabelURL = true;
							break;
						}
				}
			}
			if (del != null)
				known.removeAll(del);
			
			if (result != null) {
				if (!useLabelURL)
					return result.getInputStream();
				else
					return result.getInputStreamLabelField();
			}
		}
		return null;
	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	public static void registerObject(LoadedData loadedData) {
		synchronized (known) {
			known.add(new WeakReference<LoadedData>(loadedData));
			ArrayList<WeakReference<LoadedData>> del = null;
			for (WeakReference<LoadedData> wr : known) {
				LoadedData li = wr.get();
				if (li == null) {
					if (del == null)
						del = new ArrayList<WeakReference<LoadedData>>();
					del.add(wr);
				}
			}
			if (del != null)
				known.removeAll(del);
		}
	}
	
	public static IOurl getURL(IOurl remoteImageUrl) {
		return new IOurl(PREFIX, remoteImageUrl.getFileName());
	}
	
}
