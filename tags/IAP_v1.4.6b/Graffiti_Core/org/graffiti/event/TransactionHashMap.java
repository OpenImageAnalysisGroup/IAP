package org.graffiti.event;

import java.util.HashMap;

import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

public class TransactionHashMap extends HashMap<Object, Object> {
	private static final long serialVersionUID = 1L;
	
	@Override
	public Object put(Object key, Object value) {
		
		if (key == null || value == null)
			return null;
		Object oldValue = get(key);
		
		if (oldValue != null && value != null)
			if (oldValue instanceof Node || oldValue instanceof Edge || oldValue instanceof Graph)
				if (value instanceof Node || value instanceof Edge || value instanceof Graph)
					if (oldValue != value)
						return null;
		
		// in case events are at the same time about edges and nodes
		// store change information about edges and nodes independently
		if (value instanceof Edge && key instanceof Node) {
			key = value;
		}
		
		if (oldValue == null || value instanceof Node || value instanceof Edge || value instanceof Graph) {
			return super.put(key, value);
		} else {
			if (oldValue instanceof Node || oldValue instanceof Edge || oldValue instanceof Graph) {
				return oldValue;
			} else {
				if (oldValue instanceof AttributeEvent && value instanceof AttributeEvent) {
					AttributeEvent previousChange = (AttributeEvent) oldValue;
					AttributeEvent currentChange = (AttributeEvent) value;
					String previousPath = mygetpath(previousChange.getAttribute());
					String currentPath = mygetpath(currentChange.getAttribute());
					PathComparisonResult pcr = PathComparisonResult.compare(previousPath, currentPath);
					if (pcr == PathComparisonResult.PATH_COMPLETELY_DIFFERENT)
						return super.put(key, key);
					else
						if (pcr == PathComparisonResult.EQUAL_PATH)
							return super.put(key, currentChange);
						else {
							super.put(key, key);
							// TODO generate new event with most common attribute path
							// String path = pcr.getCommonPath();
							// AttributeEvent ae = new AttributeEvent(path, ((Attributable) key).getAttribute(path));
							// super.put(key, ae);
						}
					
				} else {
					// TODO here we should handle attribute deletion events (value instanceof attribute)
					if (value instanceof Attribute)
						return super.put(key, value);
				}
			}
		}
		return null;
	}
	
	private String mygetpath(Attribute attribute) {
		if (attribute.getParent() != null) {
			String s = mygetpath(attribute.getParent());
			return s + (s.length() > 0 ? Attribute.SEPARATOR : "") + attribute.getName();
		} else
			return attribute.getName();
	}
}
