// ==============================================================================
//
// AttributeTypesManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeTypesManager.java,v 1.1 2011-01-31 09:04:42 klukas Exp $

package org.graffiti.attributes;

import java.lang.reflect.Constructor;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.AttributeHelper;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.plugin.GenericPlugin;

/**
 * Provides a type manager for attributes. It contains the default attributes
 * from the package <code>org.graffiti.attributes</code>. Additional classes
 * implementing the <code>org.graffiti.attributes.Attribute</code>-interface
 * can be added and then used in an arbitrary <code>Attribute</code> hierarchy
 * associated with this <code>AttributeTypesManager</code>.
 * 
 * @version $Revision: 1.1 $
 */
@SuppressWarnings("unchecked")
public class AttributeTypesManager
					implements PluginManagerListener {
	// ~ Static fields/initializers =============================================
	
	/** The logger for this class */
	private static final Logger logger = Logger.getLogger(AbstractAttribute.class.getName());
	
	// ~ Instance fields ========================================================
	
	/** Maps a fully qualified class name to the appropriate class. */
	private Map attributeTypes;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>AttributeTypesManager</code>. Loads the default <code>Attribute</code> classes from the package
	 * <code>org.graffiti.attributes</code>.
	 */
	public AttributeTypesManager() {
		try {
			logger.setLevel(Level.OFF);
		} catch (AccessControlException ace) {
			// empty
		}
		attributeTypes = new HashMap();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns an instance of the class that is associated with the name of the
	 * attribute.
	 * 
	 * @param attrName
	 *           the name of the attribute type.
	 * @param id
	 *           the id that is assigned to the new attribute.
	 * @return an instance of the class that is associated with the name of the
	 *         attribute.
	 */
	public Object getAttributeInstance(String attrName, String id) {
		assert (attrName != null) && (id != null);
		
		Class c = (Class) (attributeTypes.get(attrName));
		assert c != null : "Attribute type " + attrName + " not registered";
		
		try {
			Class[] argTypes = new Class[] { String.class };
			Constructor constr = c.getDeclaredConstructor(argTypes);
			
			return constr.newInstance(new Object[] { id });
			
			// return ((Class)(attributeTypes.get(attrName))).newInstance();
			// } catch (InstantiationException ie) {
			// throw new RuntimeException
			// ("Class " + attrName + " could not be instantiated: "+ ie);
			// } catch (IllegalAccessException iae) {
			// throw new RuntimeException
			// ("Class " + attrName + " could not be instantiated: "+ iae);
			// } catch (NoSuchMethodException nme) {
			// throw new RuntimeException("No constructor with one String as "
			// + "parameter found: " + nme);
		} catch (Exception e) {
			assert false : "Exception occurred: " + e;
			
			return null;
		}
	}
	
	/**
	 * Sets the map of known <code>Attribute</code> types.
	 * 
	 * @param newAttrTypes
	 *           the new<code>Attribute</code> types map.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void setAttributeTypes(Map newAttrTypes) {
		// this.attributeTypes = attributeTypes;
		assert newAttrTypes != null;
		this.attributeTypes = new HashMap();
		
		Iterator it = newAttrTypes.keySet().iterator();
		
		while (it.hasNext()) {
			String id = null;
			
			try {
				id = (String) it.next();
			} catch (ClassCastException cce) {
				throw new IllegalArgumentException("Map does not contain " +
									"(only) keys of type String");
			}
			
			try {
				addAttributeType((Class) newAttrTypes.get(id));
			} catch (ClassCastException cce) {
				throw new IllegalArgumentException("Map does not contain " +
									"(only) values of type Class");
			}
		}
	}
	
	/**
	 * Returns a map of all known <code>Attribute</code> types.
	 * 
	 * @return a map of all known <code>Attribute</code> types.
	 */
	public Map getAttributeTypes() {
		return attributeTypes;
	}
	
	/**
	 * Adds a given <code>Attribute</code> type class to the list of attribute
	 * types.
	 * 
	 * @param c
	 *           the attribute class to add.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addAttributeType(Class c) {
		// addAttributeType(c.getName(), c);
		boolean implementsAttribute = false;
		
		Class superClass = c;
		
		while (!superClass.getName().equals("java.lang.Object")) {
			Class[] interfaces = superClass.getInterfaces();
			
			for (int i = 0; i < interfaces.length; i++) {
				if (interfaces[i].getName().equals("org.graffiti.attributes.Attribute")) {
					implementsAttribute = true;
					
					break;
				}
			}
			
			superClass = superClass.getSuperclass();
		}
		
		if (implementsAttribute) {
			attributeTypes.put(c.getName(), c);
		} else {
			throw new IllegalArgumentException(
								"Only classes that implement interface " +
													"org.graffiti.attributes.Attribute can be added.");
		}
	}
	
	/**
	 * Called by the plugin manager, iff a plugin has been added.
	 * 
	 * @param plugin
	 *           the added plugin.
	 * @param desc
	 *           the description of the new plugin.
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		Class[] newTypes = plugin.getAttributes();
		
		for (int i = 0; i < newTypes.length; i++) {
			addAttributeType(newTypes[i]);
		}
		
		if (plugin.getAttributeDescriptions() != null)
			for (AttributeDescription ad : plugin.getAttributeDescriptions()) {
				String id = ad.getId();
				String help = ad.getUser_description();
				if (id != null && help != null && id.length() > 0 && help.length() > 0) {
					AttributeHelper.setNiceId(id, help);
				}
				if (id != null && id.length() > 0 && ad.getAttributeClass() != null) {
					if (ad.isNodeAttributeDescription())
						AbstractAttribute.addNodeAttributeType(id, ad.getAttributeClass());
					if (ad.isNodeAttributeDescription())
						AbstractAttribute.addEdgeAttributeType(id, ad.getAttributeClass());
				}
				
				if (id != null && ad.getDeletePath() != null && id.length() > 0 && ad.getDeletePath().length() > 0) {
					AttributeHelper.setDeleteableAttribute(id, ad.getDeletePath());
				}
				
			}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
