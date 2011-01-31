package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.HelperClass;
import org.jdom.Attribute;
import org.jdom.Element;

import unit_test_support.TestValueRequired;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

@TestValueRequired("name")
public interface SubstanceInterface extends MappingDataEntity, Comparable<SubstanceInterface>,
					Collection<ConditionInterface>, HelperClass {
	
	/**
	 * Dataset, transformed for data mapping: <substance id="" name=""> <line
	 * experimentname="" genotype="WT" growthconditions="" id="1"
	 * name="Unknown 1" treatment="" variety=""> <sample id="" measurementtool=""
	 * time="26" unit="day"> <average max="" min="" replicates="1" stddev="0.0"
	 * unit="units">48.5313748658488</average> <data replicates=""
	 * unit="">48.5313748658488</data> </sample> ...
	 */
	public abstract boolean setMappedData(Element e, Element experimentChildElement);
	
	public abstract ConditionInterface addAndMergeData(ConditionInterface seriesnew);
	
	public abstract String getXMLstring();
	
	public abstract String toString();
	
	public abstract void getXMLAttributeString(StringBuilder s);
	
	public abstract void getStringOfChildren(StringBuilder s);
	
	public abstract Collection<MyComparableDataPoint> getDataPoints(boolean returnAvgValues);
	
	/**
	 * @return Null or eventually empty list of synonyms.
	 */
	public abstract Collection<String> getSynonyms();
	
	public abstract HashMap<Integer, String> getSynonymMap();
	
	public abstract int getNumberOfDifferentTimePoints();
	
	public abstract int clearSynonyms();
	
	public abstract void setSynonyme(int idx, String value);
	
	public abstract String getSynonyme(int idx);
	
	public abstract String getFuncat();
	
	public abstract String getInfo();
	
	public abstract int getMaximumSynonymeIndex(int returnIfNoSynonymes);
	
	public abstract double getAverage();
	
	public abstract void setAttribute(Attribute attr);
	
	public abstract boolean setData(Element experimentElement);
	
	public abstract void setDataOfChildElement(Element childElement);
	
	public abstract void setDataOfChildElement(Element childElement, Element experimentChildElement);
	
	public abstract void getSubstanceString(StringBuilder r);
	
	public abstract int compareTo(SubstanceInterface o);
	
	public abstract void setRowId(String rowId);
	
	public abstract String getRowId();
	
	public abstract void setName(String name);
	
	public abstract String getName();
	
	public abstract void setFuncat(String funcat);
	
	public abstract void setInfo(String info);
	
	public abstract void setFormula(String formula);
	
	public abstract String getFormula();
	
	public abstract void setSubstancegroup(String substancegroup);
	
	public abstract String getSubstancegroup();
	
	public abstract void setClusterId(String cluster_id);
	
	public abstract String getClusterId();
	
	public abstract void setSpot(String spot);
	
	public abstract String getSpot();
	
	public abstract void setNewBlast(String new_blast);
	
	public abstract String getNewBlast();
	
	public abstract void setNewBlastEval(String new_blast_e_val);
	
	public abstract String getNewBlastEval();
	
	public abstract void setNewBlastScore(String new_blast_score);
	
	public abstract String getNewBlastScore();
	
	public abstract void setAffyHit(String affy_hit);
	
	public abstract String getAffyHit();
	
	public abstract void setScore(String score);
	
	public abstract String getScore();
	
	public abstract void setSecure(String secure);
	
	public abstract String getSecure();
	
	public abstract int getDataPointCount(boolean returnAvgValues);
	
	public abstract double getSum();
	
	public abstract Collection<ConditionInterface> getConditions(Collection<String> validConditons);
	
	/*
	 * Delegate Methods
	 */
	public abstract boolean add(ConditionInterface e);
	
	public abstract Iterator<ConditionInterface> iterator();
	
	public abstract boolean addAll(Collection<? extends ConditionInterface> c);
	
	public abstract void clear();
	
	public abstract boolean contains(Object o);
	
	public abstract boolean containsAll(Collection<?> c);
	
	public abstract boolean isEmpty();
	
	public abstract boolean remove(Object o);
	
	public abstract boolean removeAll(Collection<?> c);
	
	public abstract boolean retainAll(Collection<?> c);
	
	public abstract int size();
	
	public abstract Object[] toArray();
	
	public abstract <T> T[] toArray(T[] a);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	public abstract void fillAttributeMap(Map<String, Object> attributeValueMap);
	
	public abstract void setSynonyme(HashMap<Integer, String> hashMap);
	
	public SubstanceInterface clone();
	
}