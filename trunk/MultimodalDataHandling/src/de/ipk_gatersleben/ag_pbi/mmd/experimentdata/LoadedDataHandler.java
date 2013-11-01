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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.ObjectRef;
import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
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
		ObjectRef useLabelURLref = new ObjectRef("false", false);
		LoadedData result = getEntry(url, useLabelURLref);
		
		if (result != null) {
			if (!(Boolean) useLabelURLref.getObject())
				return result.getInputStream();
			else
				return result.getInputStreamLabelField();
		}
		return null;
	}
	
	private synchronized LoadedData getEntry(IOurl url, ObjectRef useLabelURLref) {
		synchronized (known) {
			LoadedData result = null;
			ArrayList<WeakReference<LoadedData>> del = null;
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
							useLabelURLref.setObject(true);
							break;
						}
				}
			}
			if (del != null)
				known.removeAll(del);
			return result;
		}
	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	public synchronized static void registerObject(LoadedData loadedData) {
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
	
	@Override
	public Long getStreamLength(IOurl url) throws IOException {
		ObjectRef useLabelURLref = new ObjectRef("false", false);
		LoadedData result = getEntry(url, useLabelURLref);
		
		if (result != null) {
			InputStream is = null;
			if (!(Boolean) useLabelURLref.getObject())
				is = result.getInputStream();
			else
				is = result.getInputStreamLabelField();
			if (is != null) {
				if (is instanceof MyByteArrayInputStream)
					return (long) ((MyByteArrayInputStream) is).getCount();
				else
					return (long) is.available();
			}
		}
		
		return null;
	}
	
}
