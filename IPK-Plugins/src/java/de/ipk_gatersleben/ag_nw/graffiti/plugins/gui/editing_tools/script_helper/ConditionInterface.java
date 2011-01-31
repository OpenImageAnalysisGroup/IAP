package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

public interface ConditionInterface extends MappingDataEntity, Comparable<ConditionInterface>, Set<SampleInterface> {
	
	public void getString(StringBuilder r);
	
	public void getXMLAttributeString(StringBuilder r);
	
	public void getStringOfChildren(StringBuilder r);
	
	public String getExpAndConditionName();
	
	public String toString();
	
	public String getConditionName();
	
	public void getTimes(Set<Integer> times);
	
	public String getExperimentName();
	
	public String getDatabase();
	
	public String getCoordinator();
	
	public Date getExperimentStartDate();
	
	public int getConditionId();
	
	public Collection<MyComparableDataPoint> getMeanMCDPs();
	
	public ArrayList<MyComparableDataPoint> getMCDPs();
	
	public ArrayList<Double> getMeanValues();
	
	public ArrayList<Integer> getMeanTimePoints();
	
	public ArrayList<String> getMeanTimeUnits();
	
	public double calcAlpha();
	
	public double calcBeta();
	
	public String getSpecies();
	
	public String getGenotype();
	
	public String getTreatment();
	
	public int getSeriesId();
	
	public boolean setData(Element condition);
	
	public boolean setData(Element conditionElement, Element experimentChildElement);
	
	public String getName();
	
	public void setAttribute(Attribute attr);
	
	public void setDataOfChildElement(Element childElement);
	
	public void setExperimentName(String experimentName);
	
	public void setDatabase(String database);
	
	public void setExperimentCoordinator(String experimentCoordinator);
	
	public String getExperimentCoordinator();
	
	public void setExperimentStartDate(Date experimentStartDate);
	
	public void setSpecies(String species);
	
	public void setGenotype(String genotype);
	
	public void setGrowthconditions(String growthconditions);
	
	public String getGrowthconditions();
	
	public void setTreatment(String treatment);
	
	public String getExperimentType();
	
	public String getSequence();
	
	public void setVariety(String variety);
	
	public String getVariety();
	
	public void setExperimentType(String experimenttype);
	
	public void setSequence(String sequence);
	
	public void getExperimentHeader(StringBuilder r, int measurementcount);
	
	public SubstanceInterface getParentSubstance();
	
	public void getXMLAttributeStringForDocument(StringBuilder r);
	
	public void getStringForDocument(StringBuilder r);
	
	public int compareTo(ConditionInterface otherSeries);
	
	public void setRowId(int rowId);
	
	public int getRowId();
	
	public void setExperimentImportdate(Date experimentimportdate);
	
	public Date getExperimentImportdate();
	
	public void setExperimentRemark(String experimentRemark);
	
	public void setExperimentDatabaseId(String databaseId);
	
	public String getExperimentDatabaseId();
	
	public String getExperimentRemark();
	
	public SampleInterface addAndMerge(SampleInterface samplenew);
	
	public void setParent(SubstanceInterface md);
	
	public void setExperimentInfo(ExperimentHeaderInterface header);
	
	public ExperimentHeaderInterface getExperimentHeader();
	
	public boolean add(SampleInterface arg0);
	
	public boolean addAll(Collection<? extends SampleInterface> arg0);
	
	public void clear();
	
	public boolean contains(Object arg0);
	
	public boolean containsAll(Collection<?> arg0);
	
	public boolean isEmpty();
	
	public boolean remove(Object arg0);
	
	public boolean removeAll(Collection<?> arg0);
	
	public boolean retainAll(Collection<?> arg0);
	
	public int size();
	
	public Object[] toArray();
	
	public <T> T[] toArray(T[] arg0);
	
	public Iterator<SampleInterface> iterator();
	
	public void fillAttributeMap(Map<String, Object> attributeValueMap);
	
	public boolean equals(Object obj);
	
	public int hashCode();
	
	public ConditionInterface clone(SubstanceInterface parent);
	
}