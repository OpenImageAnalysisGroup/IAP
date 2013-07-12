package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.ByteAttribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.FloatAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.graphics.LineModeAttribute;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * enumerates the attributes of the given node / attributable in a path -->
 * attribute map
 * 
 * @param validSearchTypes
 * @param attr
 * @return
 */
public class TreeAttributSearcher
{
	public static Map<String[], Attribute> getMapPathToAttributable(HashSet<SearchType> validSearchTypes, GraphElement attr)
	{
		Map<String[], Attribute> mapPathToAttribute = new HashMap<String[], Attribute>();

		// get CollectionAttribute
		CollectionAttribute ca = attr.getAttributes();

		// for recursion
		Stack<CollectionAttribute> stack = new Stack<CollectionAttribute>();
		stack.push(ca);

		// attr to path
		Map<CollectionAttribute, List<String>> mapColAttrToPathList = new HashMap<CollectionAttribute, List<String>>();
		mapColAttrToPathList.put(ca, new ArrayList<String>());

		while (!stack.empty())
		{
			// get current attr and add its path
			CollectionAttribute colAttr = stack.pop();
			List<String> pathList = mapColAttrToPathList.remove(colAttr);

			String pathElement = colAttr.getId();

			if (pathElement != null && pathElement.length() > 0)
				pathList.add(pathElement);

			for (Attribute subAttr : colAttr.getCollection().values())
			{
				// for all child-attributes - clone path list
				@SuppressWarnings("unchecked")
				List<String> listClone = (List<String>) ((ArrayList<String>) pathList).clone();

				if (subAttr instanceof CollectionAttribute)
				{
					// recursion!
					CollectionAttribute subColAttr = (CollectionAttribute) subAttr;

					stack.push(subColAttr);
					mapColAttrToPathList.put(subColAttr, listClone);
				} else
				{
					// leaf!
					SearchType st = null;
					if (subAttr instanceof StringAttribute)
						st = SearchType.searchString;
					if (subAttr instanceof ByteAttribute)
						st = SearchType.searchInteger;
					if (subAttr instanceof IntegerAttribute)
						st = SearchType.searchInteger;
					if (subAttr instanceof BooleanAttribute)
						st = SearchType.searchBoolean;
					if (subAttr instanceof DoubleAttribute)
						st = SearchType.searchDouble;
					if (subAttr instanceof FloatAttribute)
						st = SearchType.searchDouble;
					if (subAttr instanceof ColorAttribute)
						st = SearchType.searchColor;
					if (subAttr instanceof LineModeAttribute)
						st = SearchType.searchString;

					if (validSearchTypes != null && !(validSearchTypes.contains(st)))
						// attribute type not wanted!
						continue;

					if (subAttr != null && subAttr.getId() != null)
					{
						// valid!
						String leafName = subAttr.getId();
						listClone.add(leafName);

						mapPathToAttribute.put(listClone.toArray(new String[listClone.size()]), subAttr);
					} else
						// valid!
						ErrorMsg.addErrorMessage("Unexpected attribute: " + st + ", " + subAttr + ", " + subAttr.getClass() + ", " + subAttr.getId());
				}
			}
		}

		return mapPathToAttribute;
	}

	public static Map<String[], Attribute> getMapPathToAttributable(HashSet<SearchType> validSearchTypes, Graph attr)
	{
		Map<String[], Attribute> mapPathToAttribute = new HashMap<String[], Attribute>();

		// get CollectionAttribute
		CollectionAttribute ca = attr.getAttributes();

		// for recursion
		Stack<CollectionAttribute> stack = new Stack<CollectionAttribute>();
		stack.push(ca);

		// attr to path
		Map<CollectionAttribute, List<String>> mapColAttrToPathList = new HashMap<CollectionAttribute, List<String>>();
		mapColAttrToPathList.put(ca, new ArrayList<String>());

		while (!stack.empty())
		{
			// get current attr and add its path
			CollectionAttribute colAttr = stack.pop();
			List<String> pathList = mapColAttrToPathList.remove(colAttr);

			String pathElement = colAttr.getId();

			if (pathElement != null && pathElement.length() > 0)
				pathList.add(pathElement);

			for (Attribute subAttr : colAttr.getCollection().values())
			{
				// for all child-attributes - clone path list
				@SuppressWarnings("unchecked")
				List<String> listClone = (List<String>) ((ArrayList<String>) pathList).clone();

				if (subAttr instanceof CollectionAttribute)
				{
					// recursion!
					CollectionAttribute subColAttr = (CollectionAttribute) subAttr;

					stack.push(subColAttr);
					mapColAttrToPathList.put(subColAttr, listClone);
				} else
				{
					// leaf!
					SearchType st = null;
					if (subAttr instanceof StringAttribute)
						st = SearchType.searchString;
					if (subAttr instanceof ByteAttribute)
						st = SearchType.searchInteger;
					if (subAttr instanceof IntegerAttribute)
						st = SearchType.searchInteger;
					if (subAttr instanceof BooleanAttribute)
						st = SearchType.searchBoolean;
					if (subAttr instanceof DoubleAttribute)
						st = SearchType.searchDouble;
					if (subAttr instanceof FloatAttribute)
						st = SearchType.searchDouble;
					if (subAttr instanceof ColorAttribute)
						st = SearchType.searchColor;
					if (subAttr instanceof LineModeAttribute)
						st = SearchType.searchString;

					if (validSearchTypes != null && !(validSearchTypes.contains(st)))
						// attribute type not wanted!
						continue;

					if (subAttr != null && subAttr.getId() != null)
					{
						// valid!
						String leafName = subAttr.getId();
						listClone.add(leafName);

						mapPathToAttribute.put(listClone.toArray(new String[listClone.size()]), subAttr);
					} else
						// valid!
						ErrorMsg.addErrorMessage("Unexpected attribute: " + st + ", " + subAttr + ", " + subAttr.getClass() + ", " + subAttr.getId());
				}
			}
		}

		return mapPathToAttribute;
	}
}