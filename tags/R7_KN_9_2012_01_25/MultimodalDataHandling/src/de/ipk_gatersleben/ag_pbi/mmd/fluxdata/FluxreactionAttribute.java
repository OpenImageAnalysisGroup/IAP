package de.ipk_gatersleben.ag_pbi.mmd.fluxdata;

import org.AttributeHelper;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Node;

public class FluxreactionAttribute extends StringAttribute {
	
	public static final String name = "fluxreactionsize";
	public static final String path = "flux";
	
	public FluxreactionAttribute() {
		super();
	}
	
	public FluxreactionAttribute(String id) {
		super(id);
	}
	
	public FluxreactionAttribute(String id, String value) {
		super(id, value);
	}
	
	public static void setNiceReaction(Node rnd, double size) {
		AttributeHelper.setAttribute(rnd, path, name, new FluxreactionAttribute(name, size + ""));
	}
	
	public static void removeNicereaction(Node rnd) {
		if (AttributeHelper.hasAttribute(rnd, path + SEPARATOR + name))
			AttributeHelper.deleteAttribute(rnd, path, name);
	}
	
}
