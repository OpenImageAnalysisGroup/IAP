// ==============================================================================
//
// PluginDescription.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PluginDescription.java,v 1.1 2011-01-31 09:04:52 klukas Exp $

package org.graffiti.managers.pluginmgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains a meta data of a plugin.
 * 
 * @version $Revision: 1.1 $
 */
public class PluginDescription {
	// ~ Static fields/initializers =============================================
	
	// ~ Instance fields ========================================================
	
	/** The list of <code>Dependency</code> objects of this plugin. */
	private List<PluginDependency> dependencies;
	
	/** The author of the plugin. */
	private String author;
	
	/**
	 * An URL or short description about the location of this plugin in the
	 * internet.
	 */
	private String available;
	
	/** A short (american english) description of this plugin. */
	private String description;
	
	/**
	 * The name of the plugin class, which implements the <code>GenericPlugin</code> interface.
	 * 
	 * @see org.graffiti.plugin.GenericPlugin
	 */
	private String main;
	
	/** The name of the plugin. */
	private String name;
	
	/** The version of this plugin. */
	private String version;
	
	private String compatibleVersion;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs an empty plugin description,
	 */
	public PluginDescription() {
		dependencies = new LinkedList<PluginDependency>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the author.
	 * 
	 * @param author
	 *           The author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/**
	 * Returns the author.
	 * 
	 * @return String
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * Sets the available.
	 * 
	 * @param available
	 *           The available to set
	 */
	public void setAvailable(String available) {
		this.available = available;
	}
	
	/**
	 * Returns the available.
	 * 
	 * @return String
	 */
	public String getAvailable() {
		return available;
	}
	
	/**
	 * Sets the dependencies.
	 * 
	 * @param dependencies
	 *           The dependencies to set
	 */
	public void setDependencies(List<PluginDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	/**
	 * Returns the dependencies.
	 * 
	 * @return List
	 */
	public List<PluginDependency> getDependencies() {
		return dependencies;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description
	 *           The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the description.
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the main.
	 * 
	 * @param main
	 *           The main to set
	 */
	public void setMain(String main) {
		this.main = main;
	}
	
	/**
	 * Returns the main.
	 * 
	 * @return String
	 */
	public String getMain() {
		return main;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name
	 *           The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns an iterator over the plugin dependency list.
	 * 
	 * @return an iterator over the plugin dependency list.
	 */
	public Iterator<PluginDependency> getPluginDependenciesIterator() {
		return dependencies.iterator();
	}
	
	/**
	 * Sets the version.
	 * 
	 * @param version
	 *           The version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * Returns the version.
	 * 
	 * @return Version
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * Sets the version for compatibility.
	 * 
	 * @param version
	 *           The version to set
	 */
	public void setCompatibleVersion(String v) {
		this.compatibleVersion = v;
	}
	
	/**
	 * Returns the version for compatibility.
	 * 
	 * @return Version
	 */
	public String getCompatibleVersion() {
		return compatibleVersion;
	}
	
	/**
	 * Adds the given dependency to the list of dependencies.
	 * 
	 * @param dep
	 *           the dependency to add to the list of dependent plugins.
	 */
	public void addDependency(PluginDependency dep) {
		dependencies.add(dep);
	}
	
	/**
	 * Adds the given plugin dependency to the list of dependencies.
	 * 
	 * @param dep
	 *           the dependency to add to the list.
	 */
	public void addPluginDependency(PluginDependency dep) {
		dependencies.add(dep);
	}
	
	/**
	 * Returns a human readable string representation of this object.
	 * 
	 * @return a human readable string representation of this object.
	 */
	@Override
	public String toString() {
		return "[name = " + name + ", version = " + version + ", " +
							" main = " + main + ", number of dependencies = " +
							dependencies.size() + "]";
	}
	
	private Collection<PluginEntry> childPlugins = new ArrayList<PluginEntry>();
	
	private boolean isAddon;
	
	public boolean isAddon() {
		return isAddon;
	}
	
	public void setAddon(boolean isAddon) {
		this.isAddon = isAddon;
	}
	
	public void addChild(PluginEntry plugin) {
		synchronized (childPlugins) {
			childPlugins.add(plugin);
		}
	}
	
	public Collection<PluginEntry> getChildPlugins() {
		synchronized (childPlugins) {
			return childPlugins;
		}
	}
	
	private boolean isOptional = false;
	
	private boolean isOptionalDefaultTrue = true;
	
	public void setIsOptional(String optional) {
		if (optional != null && optional.equalsIgnoreCase("true"))
			isOptional = true;
		else
			isOptional = false;
	}
	
	public boolean isOptional() {
		return isOptional;
	}
	
	public void setIsOptionalDefaultTrue(String optionalDefaultValue) {
		if (optionalDefaultValue != null && optionalDefaultValue.equalsIgnoreCase("false"))
			isOptionalDefaultTrue = false;
		else
			isOptionalDefaultTrue = true;
	}
	
	public boolean isOptionalDefaultTrue() {
		return isOptionalDefaultTrue;
	}
	
	private boolean isPriorityPlugin = false;
	
	private String rssFeedURL;
	
	private String rssFeedName;
	
	public boolean isPriorityPlugin() {
		return isPriorityPlugin;
	}
	
	public void setIsPriorityPlugin(String priority) {
		if (priority != null && priority.equalsIgnoreCase("true"))
			isPriorityPlugin = true;
		else
			isPriorityPlugin = false;
	}
	
	public boolean hasRSSfeedDefined() {
		return rssFeedURL != null && rssFeedName != null;
	}
	
	/**
	 * @return Null, if no feed is defined.
	 */
	public RSSfeedDefinition getFeed() {
		if (hasRSSfeedDefined())
			return new RSSfeedDefinition(rssFeedURL, rssFeedName);
		else
			return null;
	}
	
	public void setRSSfeedName(String name) {
		this.rssFeedName = name;
	}
	
	public void setRSSfeedURL(String url) {
		this.rssFeedURL = url;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
