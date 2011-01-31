package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.plugin.XMLHelper;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

public class Substance implements SubstanceInterface {
	
	private String rowId, name, funcat, info, formula, substancegroup, cluster_id, spot, new_blast, new_blast_e_val,
						new_blast_score, affy_hit, score, secure;
	
	HashMap<Integer, String> synonyms = null; // new HashMap<Integer, String>()
	
	private final Collection<ConditionInterface> conditions = new ArrayList<ConditionInterface>();
	
	public Substance() {
		// empty
	}
	
	/**
	 * Whole XML dataset: <experimentdata> <experiment experimentid="-1">
	 * <experimentname>Gerstenentwicklung-Frühjahr 2003</experimentname>
	 * <remark/> <coordinator>Hardy Rolletschek</coordinator> <excelfileid/>
	 * <importusername/> <importdate>Fri Apr 08 12:32:35 CEST 2005</importdate>
	 * <startdate>Tue Apr 01 00:00:00 CEST 2003</startdate>
	 * <measurements>1057</measurements> <imagefiles>0</imagefiles>
	 * <sizekb>0</sizekb> </experiment> <measurements> <substance id="column 19"
	 * name="Arg"> <line genotype="wild type" growthconditions="" id="1"
	 * name="Hordeum vulgare" treatment="Tagproben" variety=""> <sample id="1"
	 * measurementtool="HPLC" time="4" unit="day"> <average
	 * max="0.1720235400633771" min="0.07136485280999108" replicates="2"
	 * stddev="0.07117644034220516"
	 * unit="µmol / g FW">0.1216941964366841</average> <data replicates="3"
	 * unit="µmol / g FW">0.1720235400633771</data> <data replicates="3"
	 * unit="µmol / g FW">0.07136485280999108</data> </sample> ...
	 */
	static Experiment getData(Element e) {
		ArrayList<SubstanceInterface> result = new ArrayList<SubstanceInterface>();
		boolean debug = false;
		try {
			if (debug)
				new XMLOutputter().output(e.getDocument(), System.out);
			// only difference to setMappedData, is, that we need to receive the
			// experimentname
			// from the experiment entity instead of the line entity.
			
			List<?> childrenList = e.getChildren();
			Element experimentChildElement = null;
			for (Object o : childrenList) {
				if (o instanceof Element) {
					Element childElement = (Element) o;
					if (childElement.getName().equals("experiment")) {
						experimentChildElement = childElement;
					} else
						if (childElement.getName().equals("measurements")) {
							
							List<?> measurementChildrenList = childElement.getChildren();
							for (Object o2 : measurementChildrenList) {
								if (o2 instanceof Element) {
									Element substanceElement = (Element) o2;
									SubstanceInterface m = Experiment.getTypeManager().getNewSubstance();
									
									if (m.setMappedData(substanceElement, experimentChildElement))
										result.add(m);
								}
							}
						}
				}
			}
		} catch (IOException e1) {
			ErrorMsg.addErrorMessage(e1);
		}
		
		return new Experiment(result);
	}
	
	/**
	 * Dataset, transformed for data mapping: <substance id="" name=""> <line
	 * experimentname="" genotype="WT" growthconditions="" id="1"
	 * name="Unknown 1" treatment="" variety=""> <sample id="" measurementtool=""
	 * time="26" unit="day"> <average max="" min="" replicates="1" stddev="0.0"
	 * unit="units">48.5313748658488</average> <data replicates=""
	 * unit="">48.5313748658488</data> </sample> ...
	 */
	public boolean setMappedData(Element e, Element experimentChildElement) {
		// setMappedData
		
		boolean debug = false;
		try {
			if (debug)
				new XMLOutputter().output(e.getDocument(), System.out);
			
			List<?> attributeList = e.getAttributes();
			setRowId("");
			setName("");
			setInfo("");
			setFuncat("");
			setFormula("");
			clearSynonyms();
			for (Object o : attributeList) {
				if (o instanceof Attribute) {
					Attribute a = (Attribute) o;
					if (!a.getName().equalsIgnoreCase("new_blast"))
						setAttribute(a);
				}
			}
			
			List<?> childrenList = e.getChildren();
			for (Object o : childrenList) {
				if (o instanceof Element) {
					Element childElement = (Element) o;
					setDataOfChildElement(childElement, experimentChildElement);
				}
			}
			return true;
		} catch (IOException e1) {
			ErrorMsg.addErrorMessage(e1);
			return false;
		}
	}
	
	public static void addAndMerge(ExperimentInterface result, SubstanceInterface tobeMerged) {
		SubstanceInterface save = null;
		for (SubstanceInterface m : result)
			if (tobeMerged.equals(m)) {
				save = m;
				break;
			}
		
		if (save == null)
			result.add(tobeMerged);
		else
			for (ConditionInterface s : tobeMerged) {
				s.setParent(save);
				save.addAndMergeData(s);
			}
	}
	
	public ConditionInterface addAndMergeData(ConditionInterface seriesnew) {
		ConditionInterface save = null;
		for (ConditionInterface s : this)
			if (s.equals(seriesnew)) {
				save = s;
				break;
			}
		
		if (save == null) {
			add(seriesnew);
			return seriesnew;
		} else {
			for (SampleInterface s : seriesnew) {
				s.setParent(save);
				save.addAndMerge(s);
			}
			return save;
		}
	}
	
	public String getXMLstring() {
		return toString();
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		getString(s);
		return s.toString();
	}
	
	private static final String[] attributeNames = new String[] { "id", "name", "funcat", "info", "formula",
						"substancegroup", "cluster_id", "spot", "new_blast", "new_blast_e_val", "new_blast_score", "affy_hit",
						"score", "secure" };
	
	private void getString(StringBuilder s) {
		s.append("<substance");
		getXMLAttributeString(s);
		s.append(">");
		getStringOfChildren(s);
		s.append("</substance>");
	}
	
	public void getXMLAttributeString(StringBuilder s) {
		getSynonymsString(s);
		getAttributeString(s, attributeNames, getAttributeValues());
	}
	
	private Object[] getAttributeValues() {
		return new Object[] { getRowId(), getName(), getFuncat(), getInfo(), getFormula(), getSubstancegroup(),
							getClusterId(), getSpot(), getNewBlast(), getNewBlastEval(), getNewBlastScore(), getAffyHit(), getScore(),
							getSecure() };
	}
	
	public void getStringOfChildren(StringBuilder s) {
		for (ConditionInterface c : conditions)
			c.getString(s);
	}
	
	public static void getAttributeString(StringBuilder s, String[] attributeNames, Object[] attributeValues) {
		getAttributeString(s, attributeNames, attributeValues, false);
	}
	
	public static boolean includeEmptyAttributes = false;
	
	/**
	 * @param asElements
	 *           indicates, that the attribute shall be written as single element, eg<br>
	 *           <code>&lt;attrname>attrvalue&lt;/attrname&gt;</code> instead <code>attrname='attrvalue'</code>
	 */
	public static void getAttributeString(StringBuilder s, String[] attributeNames, Object[] attributeValues, boolean asElements) {
		int idx = 0;
		for (String attribute : attributeNames) {
			Object v = attributeValues[idx];
			if (v != null && v instanceof Date)
				v = AttributeHelper.getDateString((Date) v);
			
			if (v != null && (!(v.equals("") && asElements)) && !v.equals("null")) { //
				if (includeEmptyAttributes || !v.equals(""))
					if (asElements)
						s.append("<" + attribute + ">" + (v instanceof String ? escapeBadCharacters((String) v) : v) + "</" + attribute + ">");
					else
						s.append(" " + attribute + "='" + (v instanceof String ? escapeBadCharacters((String) v) : v) + "'");
			} else
				if (asElements)
					s.append("<" + attribute + "/>");
			idx++;
		}
	}
	
	private static String escapeBadCharacters(String value) {
		String s = StringManipulationTools.UnicodeToHtml(value, getBad());// .replaceAll("&#", "~");
		return s;
	}
	
	private static HashSet<Character> badChars = null;
	
	private static HashSet<Character> getBad() {
		if (badChars != null)
			return badChars;
		else {
			badChars = new HashSet<Character>();
			badChars.add('<');
			badChars.add('>');
			badChars.add('\'');
			badChars.add('"');
			badChars.add('&');
			return badChars;
		}
	}
	
	private void getSynonymsString(StringBuilder res) {
		if (synonyms == null || synonyms.size() == 0)
			return;
		else {
			for (Entry<Integer, String> e : synonyms.entrySet()) {
				res.append(" name" + e.getKey().toString() + "='" + escapeBadCharacters(e.getValue()) + "'");
			}
		}
	}
	
	public Collection<MyComparableDataPoint> getDataPoints(boolean returnAvgValues) {
		ArrayList<MyComparableDataPoint> result = new ArrayList<MyComparableDataPoint>();
		for (ConditionInterface c : conditions)
			if (returnAvgValues)
				result.addAll(c.getMeanMCDPs());
			else
				result.addAll(c.getMCDPs());
		return result;
	}
	
	// public Collection<Condition> getConditions() {
	// return conditions;
	// }
	//
	// public void addCondition(Condition condition) {
	// conditions.add(condition);
	// }
	
	// public void addConditions(Collection<Condition> conditions2) {
	// for (Condition c : conditions2)
	// conditions.add(c);
	// }
	
	/**
	 * @return Null or eventually empty list of synonyms.
	 */
	public Collection<String> getSynonyms() {
		if (synonyms == null)
			synonyms = new HashMap<Integer, String>();
		return synonyms.values();
	}
	
	public HashMap<Integer, String> getSynonymMap() {
		return synonyms;
	}
	
	public int getNumberOfDifferentTimePoints() {
		HashSet<Integer> times = new HashSet<Integer>();
		for (ConditionInterface c : conditions)
			c.getTimes(times);
		return times.size();
	}
	
	public int clearSynonyms() {
		if (synonyms == null)
			return 0;
		int result = synonyms.size();
		synonyms.clear();
		return result;
	}
	
	public void setSynonyme(int idx, String value) {
		if (synonyms == null)
			synonyms = new HashMap<Integer, String>();
		synonyms.put(idx, value);
	}
	
	@Override
	public void setSynonyme(HashMap<Integer, String> hashMap) {
		synonyms = hashMap;
	}
	
	public String getSynonyme(int idx) {
		return synonyms.get(idx);
	}
	
	public String getFuncat() {
		return funcat;
	}
	
	public String getInfo() {
		return info;
	}
	
	public int getMaximumSynonymeIndex(int returnIfNoSynonymes) {
		if (synonyms == null || synonyms.size() == 0)
			return returnIfNoSynonymes;
		else {
			return synonyms.size();
			// int max = Integer.MIN_VALUE;
			// for (Integer i : synonyms.keySet()) {
			// if (i>max)
			// max = i;
			// }
			// return max;
		}
	}
	
	public double getAverage() {
		double sum = 0;
		double cnt = 0;
		for (ConditionInterface c : conditions)
			for (SampleInterface s : c) {
				sum += s.getSampleAverage().getValue();
				cnt++;
			}
		return sum / cnt;
	}
	
	public void setAttribute(Attribute attr) {
		if (attr == null)
			return;
		if (attr.getValue() == null)
			return;
		attr.setValue(StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#")));
		if (attr.getName().equals("id"))
			setRowId(attr.getValue());
		else
			if (attr.getName().equals("name"))
				setName(attr.getValue());
			else
				if (attr.getName().equals("info"))
					setInfo(attr.getValue());
				else
					if (attr.getName().equals("substancegroup"))
						setSubstancegroup(attr.getValue());
					else
						if (attr.getName().equals("funcat"))
							setFuncat(attr.getValue());
						else
							if (attr.getName().equals("new_blast"))
								; // setNewBlast(attr.getValue());
							else
								if (attr.getName().equals("spot"))
									setSpot(attr.getValue());
								else
									if (attr.getName().equals("cluster_id"))
										setClusterId(attr.getValue());
									else
										if (attr.getName().equals("new_blast_e_val"))
											setNewBlastEval(attr.getValue());
										else
											if (attr.getName().equals("new_blast_score"))
												setNewBlastScore(attr.getValue());
											else
												if (attr.getName().equals("affy_hit"))
													setAffyHit(attr.getValue());
												else
													if (attr.getName().equals("score"))
														setScore(attr.getValue());
													else
														if (attr.getName().equals("secure"))
															setSecure(attr.getValue());
														else
															if (attr.getName().equals("formula"))
																setFormula(attr.getValue());
															else
																if (attr.getName().startsWith("name")) {
																	String index = attr.getName().substring("name".length());
																	try {
																		int idx = Integer.parseInt(index);
																		if (synonyms == null)
																			synonyms = new HashMap<Integer, String>();
																		synonyms.put(idx, attr.getValue());
																	} catch (Exception err) {
																		ErrorMsg.addErrorMessage(err);
																	}
																} else
																	System.err.println("Internal Error: Unknown Substance Attribute: " + attr.getName());
	}
	
	public boolean setData(Element experimentElement) {
		return setMappedData(experimentElement, null);
	}
	
	public void setDataOfChildElement(Element childElement) {
		setDataOfChildElement(childElement, null);
	}
	
	public void setDataOfChildElement(Element childElement, Element experimentChildElement) {
		if (childElement.getName().equals("line")) {
			ConditionInterface c = Experiment.getTypeManager().getNewCondition(this);
			if (c.setData(childElement, experimentChildElement))
				conditions.add(c);
		}
	}
	
	public void getSubstanceString(StringBuilder r) {
		r.append("<substance");
		getXMLAttributeString(r);
		r.append(">");
	}
	
	public int compareTo(SubstanceInterface o) {
		return getName().compareTo(o.getName());
	}
	
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	
	public String getRowId() {
		return rowId;
	}
	
	public void setName(String name) {
		assert name != null;
		assert name.length() > 0;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setFuncat(String funcat) {
		this.funcat = funcat;
	}
	
	public void setInfo(String info) {
		this.info = info;
	}
	
	public void setFormula(String formula) {
		this.formula = formula;
	}
	
	public String getFormula() {
		return formula;
	}
	
	public void setSubstancegroup(String substancegroup) {
		this.substancegroup = substancegroup;
	}
	
	public String getSubstancegroup() {
		return substancegroup;
	}
	
	public void setClusterId(String cluster_id) {
		this.cluster_id = cluster_id;
	}
	
	public String getClusterId() {
		return cluster_id;
	}
	
	public void setSpot(String spot) {
		this.spot = spot;
	}
	
	public String getSpot() {
		return spot;
	}
	
	public void setNewBlast(String new_blast) {
		// if (new_blast!=null && new_blast.indexOf("~")>0)
		// new_blast = ErrorMsg.stringReplace(new_blast, "~", "_");
		// this.new_blast = new_blast;
	}
	
	public String getNewBlast() {
		return new_blast;
	}
	
	public void setNewBlastEval(String new_blast_e_val) {
		this.new_blast_e_val = new_blast_e_val;
	}
	
	public String getNewBlastEval() {
		return new_blast_e_val;
	}
	
	public void setNewBlastScore(String new_blast_score) {
		this.new_blast_score = new_blast_score;
	}
	
	public String getNewBlastScore() {
		return new_blast_score;
	}
	
	public void setAffyHit(String affy_hit) {
		this.affy_hit = affy_hit;
	}
	
	public String getAffyHit() {
		return affy_hit;
	}
	
	public void setScore(String score) {
		this.score = score;
	}
	
	public String getScore() {
		return score;
	}
	
	public void setSecure(String secure) {
		this.secure = secure;
	}
	
	public String getSecure() {
		return secure;
	}
	
	public static void validate(Document doc) throws Exception {
		URL xsd = Substance.class.getClassLoader().getResource(
							Substance.class.getPackage().getName().replace('.', '/') + "/" + "vanted.xsd");
		XMLHelper.validate(doc, xsd);
	}
	
	public int getDataPointCount(boolean returnAvgValues) {
		int res = 0;
		for (ConditionInterface c : conditions)
			if (returnAvgValues)
				res += c.size();
			else
				for (SampleInterface sd : c)
					res += sd.size();
		return res;
	}
	
	public double getSum() {
		double sum = 0;
		for (ConditionInterface c : conditions)
			for (SampleInterface s : c) {
				for (NumericMeasurementInterface md : s) {
					sum += md.getValue();
				}
			}
		return sum;
	}
	
	public Collection<ConditionInterface> getConditions(Collection<String> validConditons) {
		if (validConditons == null)
			return conditions;
		else {
			Collection<ConditionInterface> result = new ArrayList<ConditionInterface>();
			HashSet<String> valid = new HashSet<String>();
			valid.addAll(validConditons);
			for (ConditionInterface s : conditions) {
				if (valid.contains(s.getExpAndConditionName()))
					result.add(s);
			}
			return result;
		}
	}
	
	/*
	 * Delegate Methods
	 */
	public boolean add(ConditionInterface e) {
		return conditions.add(e);
	}
	
	public Iterator<ConditionInterface> iterator() {
		return conditions.iterator();
	}
	
	public boolean addAll(Collection<? extends ConditionInterface> c) {
		return conditions.addAll(c);
	}
	
	public void clear() {
		conditions.clear();
	}
	
	public boolean contains(Object o) {
		return conditions.contains(o);
	}
	
	public boolean containsAll(Collection<?> c) {
		return conditions.containsAll(c);
	}
	
	public boolean isEmpty() {
		return conditions.isEmpty();
	}
	
	public boolean remove(Object o) {
		return conditions.remove(o);
	}
	
	public boolean removeAll(Collection<?> c) {
		return conditions.removeAll(c);
	}
	
	public boolean retainAll(Collection<?> c) {
		return conditions.retainAll(c);
	}
	
	public int size() {
		return conditions.size();
	}
	
	public Object[] toArray() {
		return conditions.toArray();
	}
	
	public <T> T[] toArray(T[] a) {
		return conditions.toArray(a);
	}
	
	public Substance(Map<?, ?> map) {
		this();
		for (Object o : map.keySet()) {
			if (o instanceof String) {
				String key = (String) o;
				Object v = map.get(key);
				if (v instanceof String) {
					String s = (String) v;
					// s = StringManipulationTools.UnicodeToHtml(s);
					setAttribute(new Attribute(key, s));
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		if (synonyms == null || synonyms.size() == 0) {
			// empty
		} else {
			for (Entry<Integer, String> e : synonyms.entrySet()) {
				attributeValueMap.put("name" + e.getKey().toString(), e.getValue());
			}
		}
		
		int idx = 0;
		Object[] attributeValues = getAttributeValues();
		for (String attribute : attributeNames) {
			Object value = attributeValues[idx];
			if (value != null && !value.equals("null")) { // !value.equals("") &&
				attributeValueMap.put(attribute, value);
			}
			idx++;
		}
	}
	
	@Override
	public SubstanceInterface clone() {
		SubstanceInterface s = Experiment.getTypeManager().getNewSubstance();
		s.setRowId(getRowId());
		s.setName(getName());
		s.setFuncat(getFuncat());
		s.setInfo(getInfo());
		s.setFormula(getFormula());
		s.setSubstancegroup(getSubstancegroup());
		s.setClusterId(getClusterId());
		s.setSpot(getSpot());
		s.setNewBlast(getNewBlast());
		s.setNewBlastEval(getNewBlastEval());
		s.setNewBlastScore(getNewBlastScore());
		s.setAffyHit(getAffyHit());
		s.setScore(getScore());
		s.setSecure(getSecure());
		s.setSynonyme(getSynonymMap() == null ? null : new HashMap<Integer, String>(getSynonymMap()));
		return s;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Substance))
			return false;
		
		String s1 = rowId + ";" + name + ";" + funcat + ";" + info + ";" + formula + ";" + substancegroup + ";" + cluster_id + ";" + spot + ";" +
						new_blast + ";" + new_blast_e_val + ";" + new_blast_score + ";" + affy_hit + ";" + score + ";" + secure;
		Substance s = (Substance) obj;
		String s2 = s.rowId + ";" + s.name + ";" + s.funcat + ";" + s.info + ";" + s.formula + ";" + s.substancegroup + ";" + s.cluster_id + ";" + s.spot + ";" +
						s.new_blast + ";" + s.new_blast_e_val + ";" + s.new_blast_score + ";" + s.affy_hit + ";" + s.score + ";" + s.secure;
		return s1.equals(s2);
	}
	
	@Override
	public int hashCode() {
		String s1 = rowId + ";" + name + ";" + funcat + ";" + info + ";" + formula + ";" + substancegroup + ";" + cluster_id + ";" + spot + ";" +
				new_blast + ";" + new_blast_e_val + ";" + new_blast_score + ";" + affy_hit + ";" + score + ";" + secure;
		return s1.hashCode();
	}
	
}
