// ==============================================================================
//
// GenericPluginAdapter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GenericPluginAdapter.java,v 1.1 2011-01-31 09:05:03 klukas Exp $

package org.graffiti.plugin;

import javax.swing.ImageIcon;

import org.graffiti.attributes.AttributeDescription;
import org.graffiti.core.ImageBundle;
import org.graffiti.core.StringBundle;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.actions.URLattributeAction;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.extension.Extension;
import org.graffiti.plugin.io.GraphPostProcessor;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;

/**
 * An adapter class for the generic plugin interface.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class GenericPluginAdapter
					implements GenericPlugin {
	// ~ Static fields/initializers =============================================
	
	/**
	 * The default plugin icon for plugin implementations, which do not
	 * overwrite the <code>getIcon</code> method in this class.
	 */
	private static final ImageIcon DEFAULT_ICON = ImageBundle.getInstance().getIcon("icon.plugin.default");
	
	// ~ Instance fields ========================================================
	
	/** The <code>ImageBundle</code> of the plugin adapter. */
	protected ImageBundle iBundle = ImageBundle.getInstance();
	
	/** The preferences for this plugin. */
	protected GravistoPreferences prefs;
	
	/** The <code>StringBundle</code> of the plugin adapter. */
	protected StringBundle sBundle = StringBundle.getInstance();
	
	/** The algorithms the plugin provides. */
	protected Algorithm[] algorithms;
	
	/** The algorithms plugin descriptions the plugin provides. */
	protected AttributeDescription[] attributeDescriptions;
	
	/** The extensions the plugin provides. */
	protected Extension[] extensions;
	
	/** The attribute types the plugin provides. */
	@SuppressWarnings("unchecked")
	protected Class[] attributes;
	
	/** The plugin's dependencies. */
	protected String[] dependencies;
	
	/** The input serializers of this plugin. */
	protected InputSerializer[] inputSerializers;
	
	/** The graph post-processors, which modify newly loaded or created graphs */
	protected GraphPostProcessor[] graphPostProcessors;
	
	/** The output serializers of this plugin. */
	protected OutputSerializer[] outputSerializers;
	
	/** The views the plugin provides (class names of the views). */
	protected String[] views;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GenericPluginAdapter</code>.
	 */
	protected GenericPluginAdapter() {
		this.algorithms = new Algorithm[0];
		this.attributes = new Class[0];
		this.dependencies = new String[0];
		this.views = new String[0];
		this.inputSerializers = new InputSerializer[0];
		this.outputSerializers = new OutputSerializer[0];
		this.graphPostProcessors = new GraphPostProcessor[0];
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the array of <code>org.graffiti.algorithm.Algorithm</code>s the
	 * plugin contains.
	 * 
	 * @return the array of <code>org.graffiti.algorithm.Algorithm</code>s the
	 *         plugin contains.
	 */
	public Algorithm[] getAlgorithms() {
		return this.algorithms;
	}
	
	public AttributeDescription[] getAttributeDescriptions() {
		return this.attributeDescriptions;
	}
	
	/**
	 * Returns the attribute types provided by this plugin.
	 * 
	 * @return the attribute types provided by this plugin.
	 */
	@SuppressWarnings("unchecked")
	public Class[] getAttributes() {
		return this.attributes;
	}
	
	/**
	 * Returns the array containing the names of the plugin classes the current
	 * plugin depends on.
	 * 
	 * @return the array containing the names of the plugin classes the current
	 *         plugin depends on.
	 */
	public String[] getDependencies() {
		return this.dependencies;
	}
	
	/**
	 * Returns an Array of Extensions the plugin provides.
	 */
	public Extension[] getExtensions() {
		return this.extensions;
	}
	
	private boolean isAddon = false;
	
	public void setIsAddon(boolean v) {
		this.isAddon = v;
	}
	
	/**
	 * Returns the default icon for a plugin, which does not overwrite this
	 * method.
	 * 
	 * @return the default plugin icon for a plugin implementation, which does
	 *         not overwrite this method.
	 */
	public ImageIcon getIcon() {
		if (isAddon)
			return getAddonIcon();
		else
			return DEFAULT_ICON;
	}
	
	public static ImageIcon getAddonIcon() {
		ClassLoader cl = GenericPlugin.class.getClassLoader();
		String path = GenericPlugin.class.getPackage().getName().replace('.', '/');
		ImageIcon i = new ImageIcon(cl.getResource(path + "/addon-icon.png"));
		return i;
	}
	
	/**
	 * Returns the input serializers the plugin provides.
	 * 
	 * @return the input serializers the plugin provides.
	 */
	public InputSerializer[] getInputSerializers() {
		return this.inputSerializers;
	}
	
	/**
	 * Returns the output serializers the plugin provides.
	 * 
	 * @return the output serializers the plugin provides.
	 */
	public OutputSerializer[] getOutputSerializers() {
		return outputSerializers;
	}
	
	public GraphPostProcessor[] getGraphPostProcessors() {
		return graphPostProcessors;
	}
	
	/**
	 * States whether this class wants to be registered as a <code>SelectionListener</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isSelectionListener() {
		return false;
	}
	
	/**
	 * States whether this class wants to be registered as a <code>SessionListener</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isSessionListener() {
		return false;
	}
	
	/**
	 * @see org.graffiti.plugin.GenericPlugin#isViewListener()
	 */
	public boolean isViewListener() {
		return false;
	}
	
	/**
	 * Returns the array of <code>org.graffiti.plugin.view.View</code>s the
	 * plugin contains.
	 * 
	 * @return the array of <code>org.graffiti.plugin.view.View</code>s the
	 *         plugin contains.
	 */
	public String[] getViews() {
		return this.views;
	}
	
	/**
	 * Runs configuration routines for the plugin, e.g. load preferences etc.
	 * 
	 * @param p
	 *           DOCUMENT ME!
	 */
	public void configure(GravistoPreferences p) {
		prefs = p;
	}
	
	/**
	 * The routines to perform before the editor will exit.
	 */
	public void doBeforeExit() {
	}
	
	/**
	 * Interrupts the running plugin.
	 */
	public void interrupt() {
	}
	
	/**
	 * States whether this class needs up-to-date information about the current
	 * editcomponents. If this method returns <code>true</code>, it must
	 * implement interface <code>NeedEditComponents</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean needsEditComponents() {
		return false;
	}
	
	/**
	 * Stops a running plugin. Performs exit routines.
	 */
	public void stop() {
	}
	
	public String getDefaultView() {
		return null;
	}
	
	public URLattributeAction[] getURLattributeActions() {
		return null;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
