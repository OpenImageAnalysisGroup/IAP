package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.TreeAttributSearcher;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * Helps to set and get Attributes on Nodes and Edges. It can work with single
 * attributes, list of attributes and lists of list-attributes.
 * 
 * @author ricardo
 * 
 */
public class HelperClass
{
	/**
	 * returns a set of attributes the attribute looked befor like:
	 * comment_1,comment_2,...
	 * 
	 * @param attr
	 * @param elem
	 * @return
	 */
	protected static ArrayList<Attribute> getAttributeOfSetOfString(String attr, Graph elem)
	{
		ArrayList<Attribute> set = new ArrayList<Attribute>();

		attr = attr.replace("iii", "[0-9]*");
		Pattern pattern = Pattern.compile(attr);

		HelperClass.returnAttributeByPath(elem, pattern, set);
		return set;
	}

	/**
	 * returns a set of attributes the attribute looked befor like:
	 * comment_1,comment_2,...
	 * 
	 * @param attr
	 * @param elem
	 * @return
	 */
	protected static ArrayList<Attribute> getAttributeOfSetOfString(String attr, GraphElement elem)
	{
		ArrayList<Attribute> set = new ArrayList<Attribute>();

		attr = attr.replace("iii", "[0-9]*");
		Pattern pattern = Pattern.compile(attr);

		HelperClass.returnAttributeByPath(elem, pattern, set);
		return set;
	}

	/**
	 * returns a set of Attributes they looked before like:
	 * 
	 * Xref_1.comment_1 Xref_1.comment_2 ...
	 * 
	 * @param attr
	 * @param elem
	 * @param firstNumber
	 * @return
	 */
	protected static ArrayList<Attribute> getAttributeOfSetWithTwoInnerReplacements(String attr, Graph elem, int firstNumber)
	{
		ArrayList<Attribute> set = new ArrayList<Attribute>();

		attr = attr.replace("iii", String.valueOf(firstNumber));
		attr = attr.replace("jjj", "[0-9]*");
		Pattern pattern = Pattern.compile(attr);

		HelperClass.returnAttributeByPath(elem, pattern, set);
		return set;
	}

	/**
	 * returns a set of Attributes they looked before like:
	 * 
	 * Xref_1.comment_1 Xref_1.comment_2 ...
	 * 
	 * @param attr
	 * @param elem
	 * @param firstNumber
	 * @return
	 */
	protected static ArrayList<Attribute> getAttributeOfSetWithTwoInnerReplacements(String attr, GraphElement elem, int firstNumber)
	{
		ArrayList<Attribute> set = new ArrayList<Attribute>();

		attr = attr.replace("iii", String.valueOf(firstNumber));
		attr = attr.replace("jjj", "[0-9]*");
		Pattern pattern = Pattern.compile(attr);

		HelperClass.returnAttributeByPath(elem, pattern, set);
		return set;
	}

	/**
	 * returns one specific attribute that looks like BioPax.RDFId
	 * 
	 * @param elem
	 * @param attr
	 * @return
	 */
	protected static String getAttributeSecure(GraphElement elem, String attr)
	{
		String value = "";
		if (AttributeHelper.hasAttribute(elem, attr))
		{
			value = elem.getAttribute(attr).getValue().toString();
		}
		return value;
	}

	/**
	 * made for an attribute you are sure of to be existing only once the
	 * attriute exists in the form Xref_1.db
	 * 
	 * @param attr
	 * @param elem
	 * @param first
	 * @return
	 */
	protected static Attribute getAttributeWithOneSpecificInnerReplacement(String attr, Graph elem, int first)
	{
		ArrayList<Attribute> set = new ArrayList<Attribute>();

		attr = attr.replace("iii", String.valueOf(first));
		Pattern pattern = Pattern.compile(attr);

		HelperClass.returnAttributeByPath(elem, pattern, set);
		if (!set.isEmpty())
			return set.get(0);
		else
			return null;
	}

	/**
	 * made for an attribute you are sure of to be existing only once the
	 * attriute exists in the form Xref_1.db
	 * 
	 * @param attr
	 * @param elem
	 * @param first
	 * @return
	 */
	protected static Attribute getAttributeWithOneSpecificInnerReplacement(String attr, GraphElement elem, int first)
	{
		ArrayList<Attribute> set = new ArrayList<Attribute>();

		attr = attr.replace("iii", String.valueOf(first));
		Pattern pattern = Pattern.compile(attr);

		HelperClass.returnAttributeByPath(elem, pattern, set);
		if (!set.isEmpty())
			return set.get(0);
		else
			return null;
	}

	/**
	 * uses AttributeHelper to see whether there is an attribute or not.
	 * 
	 * @param elem
	 * @param string
	 * @return
	 */
	protected static boolean hasAttribute(GraphElement elem, String string)
	{
		return AttributeHelper.hasAttribute(elem, string);
	}

	/**
	 * written by Hendrik Mehlhorn it is looking for all attributes that match a
	 * specific pattern and putting the result into the set passed into the
	 * function
	 * 
	 * @param elem
	 * @param pattern
	 * @param set
	 */
	public static void returnAttributeByPath(Graph elem, Pattern pattern, ArrayList<Attribute> set)
	{
		HashSet<SearchType> search = new HashSet<SearchType>();
		search.add(SearchType.searchString);
		search.add(SearchType.searchBoolean);
		search.add(SearchType.searchDouble);
		search.add(SearchType.searchInteger);

		Map<String[], Attribute> map = new HashMap<String[], Attribute>();
		map = TreeAttributSearcher.getMapPathToAttributable(search, elem);

		for (String[] key : map.keySet())
		{
			String path = map.get(key).getPath();
			Matcher matcher = pattern.matcher(path);

			if (matcher.matches())
			{
				set.add(map.get(key));
			}

		}
	}

	/**
	 * finds Attributes in GraphElements
	 * 
	 * @param elem
	 *            GraphElement to be read
	 * @param pattern
	 *            Attribute that is looked for
	 * @param set
	 *            gets filled if Attribute was found
	 */
	public static void returnAttributeByPath(GraphElement elem, Pattern pattern, ArrayList<Attribute> set)
	{
		HashSet<SearchType> search = new HashSet<SearchType>();
		search.add(SearchType.searchString);
		search.add(SearchType.searchBoolean);
		search.add(SearchType.searchDouble);
		search.add(SearchType.searchInteger);

		Map<String[], Attribute> map = new HashMap<String[], Attribute>();
		map = TreeAttributSearcher.getMapPathToAttributable(search, elem);

		for (String[] key : map.keySet())
		{
			String path = map.get(key).getPath();
			Matcher matcher = pattern.matcher(path);

			if (matcher.matches())
			{
				set.add(map.get(key));
			}

		}
	}

	/**
	 * sets a set filled with doubles as given attributes to a node or edge
	 * 
	 * @param elem
	 * @param attr
	 * @param attributeSet
	 */
	protected static void setAttributeOfSetOfDouble(GraphElement elem, String attr, Set<Double> attributeSet)
	{
		int i = 1;
		for (Double s : attributeSet)
		{
			String attr_replaced = attr.replace("iii", String.valueOf(i));

			int firstIndex = attr_replaced.indexOf(".");
			String leaf = attr_replaced.substring(firstIndex);
			String pathToLeaf = attr_replaced.substring(0, firstIndex);

			AttributeHelper.setAttribute(elem, pathToLeaf, leaf, String.valueOf(s));
			i++;
		}
	}

	/**
	 * sets a set filled with floats as given attributes to a node or edge
	 * 
	 * @param elem
	 * @param attr
	 * @param attributeSet
	 */
	protected static void setAttributeOfSetOfFloat(GraphElement elem, String attr, Set<Float> attributeSet)
	{
		int i = 1;
		for (Float s : attributeSet)
		{
			String attr_replaced = attr.replace("iii", String.valueOf(i));

			int firstIndex = attr_replaced.indexOf(".");
			String leaf = attr_replaced.substring(firstIndex);
			String pathToLeaf = attr_replaced.substring(0, firstIndex);

			AttributeHelper.setAttribute(elem, pathToLeaf, leaf, String.valueOf(s));
			i++;
		}
	}

	/**
	 * sets a set filled with strings as given attributes to a node or edge
	 * 
	 * @param elem
	 * @param attr
	 * @param attributeSet
	 */
	protected static void setAttributeOfSetOfString(GraphElement elem, String attr, Set<String> attributeSet)
	{
		int i = 1;
		for (String s : attributeSet)
		{
			String attr_replaced = attr.replace("iii", String.valueOf(i));

			int firstIndex = attr_replaced.indexOf(".");
			String leaf = attr_replaced.substring(firstIndex);
			String pathToLeaf = attr_replaced.substring(0, firstIndex);

			AttributeHelper.setAttribute(elem, pathToLeaf, leaf, s);
			i++;
		}
	}

	/**
	 * sets one specific attribute, that looks like on a node .BioPax.RDFId = 1
	 * 
	 * @param elem
	 *            node or edge
	 * @param attr
	 *            .BioPax.RDFId
	 * @param value
	 *            1
	 */
	protected static void setAttributeSecure(GraphElement elem, String attr, String value)
	{
		if (value != null)
		{
			int firstIndex = attr.indexOf(".");
			String leaf = attr.substring(firstIndex);
			String pathToLeaf = attr.substring(0, firstIndex);

			AttributeHelper.setAttribute(elem, pathToLeaf, leaf, value);
		}
	}

	/**
	 * sets attributes with one inner replacement, use while need maintain an
	 * index for example BioPax.Comment_i --> BioPax.Comment_14
	 * 
	 * @param elem
	 * @param attr
	 * @param number
	 * @param value
	 */
	protected static void setAttributeWithOneInnerReplacement(Attributable elem, String attr, int number, String value)
	{
		if (value != null)
		{
			String attr_replaced = attr.replace("iii", String.valueOf(number));
			int firstIndex = attr_replaced.indexOf(".");
			String leaf = attr_replaced.substring(firstIndex);
			String pathToLeaf = attr_replaced.substring(0, firstIndex);

			AttributeHelper.setAttribute(elem, pathToLeaf, leaf, value);
		}
	}

	/**
	 * works for iterating over the second level of an attribute
	 * 
	 * BioPax.CellularLocationRDFId_iii.Term_jjj -->
	 * BioPax.CellularLocationRDFId_14.Term_1
	 * 
	 * @param elem
	 * @param attr
	 * @param i
	 * @param j
	 * @param value
	 */
	protected static void setAttributeWithTwoInnerReplacements(Attributable elem, String attr, int i, int j, String value)
	{
		if (value != null)
		{
			String attr_replaced = attr.replace("iii", String.valueOf(i));
			attr_replaced = attr_replaced.replace("jjj", String.valueOf(j));
			int firstIndex = attr_replaced.indexOf(".");
			String leaf = attr_replaced.substring(firstIndex);
			String pathToLeaf = attr_replaced.substring(0, firstIndex);

			AttributeHelper.setAttribute(elem, pathToLeaf, leaf, value);
		}
	}
}
