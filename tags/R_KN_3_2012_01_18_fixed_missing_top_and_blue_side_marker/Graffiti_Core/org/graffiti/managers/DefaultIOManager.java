// ==============================================================================
//
// DefaultIOManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultIOManager.java,v 1.4 2011-05-13 09:07:16 klukas Exp $

package org.graffiti.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;

import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.SystemAnalysis;
import org.graffiti.core.GenericFileFilter;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

/**
 * Handles the editor's IO serializers.
 * 
 * @version $Revision: 1.4 $
 */
public class DefaultIOManager implements IOManager {
	
	private class GravistoFileOpenFilter extends GenericFileFilter {
		
		/**
		 * @param extension
		 */
		public GravistoFileOpenFilter(String extension) {
			super(extension);
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File file) {
			if (file.isDirectory())
				return true;
			if (file.getName().lastIndexOf(".") > 0) {
				String fileExt = file.getName().substring(file.getName().lastIndexOf("."));
				if (fileExt.equalsIgnoreCase(".GZ")) {
					String fileName = file.getName();
					fileName = fileName.substring(0, fileName.length() - ".gz".length());
					fileExt = fileName.substring(fileName.lastIndexOf("."));
				}
				for (Iterator<InputSerializer> itr = inputSerializer.iterator(); itr.hasNext();) {
					InputSerializer is = itr.next();
					String[] ext = is.getExtensions();
					for (int i = 0; i < ext.length; i++)
						if (ext[i].equalsIgnoreCase(fileExt))
							return true;
				}
			}
			return false;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			// return "Graph Files (" + getSupported("*", "; ") + ")";
			return "Supported Graph Files";
		}
	}
	
	// ~ Static fields/initializers =============================================
	
	// ~ Instance fields ========================================================
	
	/** The set of input serializers. */
	private final List<InputSerializer> inputSerializer;
	
	/** The set of output serializers. */
	private final List<OutputSerializer> outputSerializer;
	
	/** The file chooser used to open and save graphs. */
	private JFileChooser fc;
	
	/** The list of listeners. */
	private final List<IOManagerListener> listeners;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new io manager.
	 */
	public DefaultIOManager() {
		inputSerializer = new ArrayList<InputSerializer>();
		outputSerializer = new ArrayList<OutputSerializer>();
		listeners = new LinkedList<IOManagerListener>();
		try {
			if (!SystemAnalysis.isHeadless())
				fc = new JFileChooser();
		} catch (AccessControlException ace) {
			// ErrorMsg.addErrorMessage(ace);
		}
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see
	 * org.graffiti.managers.IOManager#addInputSerializer(org.graffiti.plugin
	 * .io.InputSerializer)
	 */
	public void addInputSerializer(InputSerializer is) {
		// String[] inExtensions = is.getExtensions();
		
		// for (int j = 0; j < inExtensions.length; j++) {
		inputSerializer.add(is);
		// }
		fireInputSerializerAdded(is);
	}
	
	/*
	 * @see
	 * org.graffiti.managers.IOManager#addListener(org.graffiti.managers.IOManager
	 * .IOManagerListener)
	 */
	public void addListener(IOManagerListener ioManagerListener) {
		listeners.add(ioManagerListener);
	}
	
	/*
	 * @see
	 * org.graffiti.managers.IOManager#addOutputSerializer(org.graffiti.plugin
	 * .io.OutputSerializer)
	 */
	public void addOutputSerializer(OutputSerializer os) {
		String[] outExtensions = os.getExtensions();
		
		for (int j = 0; j < outExtensions.length; j++)
			outputSerializer.add(os);
		
		fireOutputSerializerAdded(os);
	}
	
	/*
	 * @see
	 * org.graffiti.managers.IOManager#createInputSerializer(java.lang.String)
	 */
	public InputSerializer createInputSerializer(InputStream in, String extSearch) throws FileNotFoundException {
		ArrayList<InputSerializer> ins = new ArrayList<InputSerializer>();
		for (InputSerializer is : inputSerializer) {
			String[] ext = is.getExtensions();
			extsearch: for (int i = 0; i < ext.length; i++)
				if (ext[i].equalsIgnoreCase(extSearch)) {
					// System.out.println("Possible reader: "+is.getClass().getCanonicalName());
					ins.add(is);
					break extsearch;
				}
		}
		if (in == null)
			return ins.iterator().next();
		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		try {
			ResourceIOManager.copyContent(in, out, 5000);
			for (InputSerializer is : ins) {
				try {
					InputStream inps = new MyByteArrayInputStream(out.getBuff(), out.size());
					if (is.validFor(inps)) {
						// System.out.println(ins.size() + " input serializers for file extension " + extSearch + ". Selected "
						// + is.getClass().getCanonicalName());
						return is;
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		} catch (IOException e1) {
			ErrorMsg.addErrorMessage(e1);
		}
		return null;
	}
	
	public Set<String> getGraphFileExtensions() {
		Set<String> knownExt = new TreeSet<String>();
		for (Iterator<InputSerializer> itr = inputSerializer.iterator(); itr.hasNext();) {
			InputSerializer is = itr.next();
			String[] ext = is.getExtensions();
			for (int i = 0; i < ext.length; i++) {
				knownExt.add(ext[i]);
			}
		}
		return knownExt;
	}
	
	/*
	 * @see org.graffiti.managers.IOManager#createOpenFileChooser()
	 */
	public JFileChooser createOpenFileChooser() {
		fc.resetChoosableFileFilters();
		Set<String> knownExt = new TreeSet<String>();
		for (Iterator<InputSerializer> itr = inputSerializer.iterator(); itr.hasNext();) {
			InputSerializer is = itr.next();
			String[] ext = is.getExtensions();
			String[] desc = is.getFileTypeDescriptions();
			if (ext.length != desc.length) {
				ErrorMsg.addErrorMessage("Error: File-Type descriptions do not match extensions - Class: " + is.toString());
				continue;
			}
			for (int i = 0; i < ext.length; i++) {
				// if (knownExt.contains(ext[i]))
				// ErrorMsg.addErrorMessage("Internal Error: Duplicate Input File Type Extension - "
				// + ext[i] + " Class: " + is.toString());
				knownExt.add(ext[i]);
				// System.out.println("Output: " + ext[i] + " Class: " +
				// is.toString());
				GravistoFileFilter gff = new GravistoFileFilter(ext[i], desc[i]);
				fc.addChoosableFileFilter(gff);
			}
		}
		
		fc.addChoosableFileFilter(new GravistoFileOpenFilter(null));
		
		return fc;
	}
	
	/*
	 * @see
	 * org.graffiti.managers.IOManager#createOutputSerializer(java.lang.String)
	 */
	public OutputSerializer createOutputSerializer(String extSearch) {
		for (Iterator<OutputSerializer> itr = outputSerializer.iterator(); itr.hasNext();) {
			OutputSerializer os = itr.next();
			String[] ext = os.getExtensions();
			for (int i = 0; i < ext.length; i++)
				if (ext[i].equalsIgnoreCase(extSearch))
					return os;
		}
		return null;
	}
	
	/*
	 * @see org.graffiti.managers.IOManager#createSaveFileChooser()
	 */
	public JFileChooser createSaveFileChooser() {
		String defaultExt = ".gml";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			defaultExt = ".xml";
		GravistoFileFilter defaultFileFilter = null;
		fc.resetChoosableFileFilters();
		fc.setAcceptAllFileFilterUsed(false);
		Set<String> knownExt = new TreeSet<String>();
		for (Iterator<OutputSerializer> itr = outputSerializer.iterator(); itr.hasNext();) {
			OutputSerializer os = itr.next();
			String[] ext = os.getExtensions();
			String[] desc = os.getFileTypeDescriptions();
			if (ext.length != desc.length) {
				ErrorMsg.addErrorMessage("Error: File-Type descriptions do not match extensions - Class: " + os.toString());
				continue;
			}
			for (int i = 0; i < ext.length; i++) {
				if (knownExt.contains(ext[i]))
					ErrorMsg.addErrorMessage("Error: Duplicate Output File Type Extension - " + ext[i] + " Class: "
										+ os.toString());
				knownExt.add(ext[i]);
				GravistoFileFilter gff = new GravistoFileFilter(ext[i], desc[i]);
				
				if (defaultFileFilter == null && gff.getExtension().equalsIgnoreCase(defaultExt)) {
					defaultFileFilter = gff;
				} else {
					fc.addChoosableFileFilter(gff);
				}
			}
		}
		if (defaultFileFilter != null)
			fc.addChoosableFileFilter(defaultFileFilter);
		return fc;
	}
	
	/*
	 * @see org.graffiti.managers.IOManager#hasInputSerializer()
	 */
	public boolean hasInputSerializer() {
		return !inputSerializer.isEmpty();
	}
	
	/*
	 * @see org.graffiti.managers.IOManager#hasOutputSerializer()
	 */
	public boolean hasOutputSerializer() {
		return !outputSerializer.isEmpty();
	}
	
	/*
	 * @see
	 * org.graffiti.managers.pluginmgr.PluginManagerListener#pluginAdded(org.
	 * graffiti.plugin.GenericPlugin,
	 * org.graffiti.managers.pluginmgr.PluginDescription)
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		// register add input serializers
		InputSerializer[] is = plugin.getInputSerializers();
		
		for (int i = 0; i < is.length; i++) {
			addInputSerializer(is[i]);
		}
		
		// register all output serializers
		OutputSerializer[] os = plugin.getOutputSerializers();
		
		for (int i = 0; i < os.length; i++) {
			addOutputSerializer(os[i]);
		}
	}
	
	/*
	 * @see
	 * org.graffiti.managers.IOManager#removeListener(org.graffiti.managers.IOManager
	 * .IOManagerListener)
	 */
	public boolean removeListener(IOManagerListener l) {
		return listeners.remove(l);
	}
	
	/**
	 * Returns a string representation of the io manager. Useful for debugging.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "inputSerializer: " + inputSerializer + " outputSerializer: " + outputSerializer;
	}
	
	/**
	 * Informs every registered io manager listener about the addition of the
	 * given input serializer.
	 * 
	 * @param is
	 *           the input serializer, which was added.
	 */
	private void fireInputSerializerAdded(InputSerializer is) {
		for (Iterator<IOManagerListener> i = listeners.iterator(); i.hasNext();) {
			IOManager.IOManagerListener l = i.next();
			
			l.inputSerializerAdded(is);
		}
	}
	
	/**
	 * Informs every output serializer about the addition of the given output
	 * serializer.
	 * 
	 * @param os
	 *           the output serializer, which was added.
	 */
	private void fireOutputSerializerAdded(OutputSerializer os) {
		for (Iterator<IOManagerListener> i = listeners.iterator(); i.hasNext();) {
			IOManager.IOManagerListener l = i.next();
			
			l.outputSerializerAdded(os);
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
