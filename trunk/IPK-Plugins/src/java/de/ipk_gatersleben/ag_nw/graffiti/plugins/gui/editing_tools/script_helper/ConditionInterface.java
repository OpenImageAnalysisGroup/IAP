package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

public interface ConditionInterface extends MappingDataEntity, Comparable<ConditionInterface>,
		Set<SampleInterface> {
	
	public void getString(StringBuilder r);
	
	@Override
	public void getXMLAttributeString(StringBuilder r);
	
	@Override
	public void getStringOfChildren(StringBuilder r);
	
	public String getExpAndConditionName();
	
	@Override
	public String toString();
	
	public String getConditionName();
	
	public void getTimes(Set<Integer> times);
	
	public String getExperimentName();
	
	public String getDatabase();
	
	public String getCoordinator();
	
	public Date getExperimentStartDate();
	
	public Date getExperimentStorageDate();
	
	public void setExperimentStorageDate(Date storageTime);
	
	public int getConditionId();
	
	public Collection<MyComparableDataPoint> getMeanMCDPs();
	
	public Collection<MyComparableDataPoint> getMCDPs();
	
	public Collection<Double> getMeanValues();
	
	public Collection<Integer> getMeanTimePoints();
	
	public Collection<String> getMeanTimeUnits();
	
	public double calcAlpha();
	
	public double calcBeta();
	
	public String getSpecies();
	
	public String getGenotype();
	
	public String getTreatment();
	
	public int getSeriesId();
	
	@Override
	public boolean setData(Element condition);
	
	public boolean setData(Element conditionElement, Element experimentChildElement);
	
	public String getName();
	
	@Override
	public void setAttribute(Attribute attr);
	
	@Override
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
	
	public String getExperimentSettings();
	
	public void setExperimentSettings(String settings);
	
	public void setExperimentAnnotation(String annotation);
	
	public String getExperimentAnnotation();
	
	public void setVariety(String variety);
	
	public String getVariety();
	
	public void setExperimentType(String experimenttype);
	
	public void setSequence(String sequence);
	
	public void getExperimentHeader(StringBuilder r, int measurementcount);
	
	public SubstanceInterface getParentSubstance();
	
	public void getXMLAttributeStringForDocument(StringBuilder r);
	
	public void getStringForDocument(StringBuilder r);
	
	@Override
	public int compareTo(ConditionInterface otherSeries);
	
	public void setRowId(int rowId);
	
	public int getRowId();
	
	public void setExperimentImportdate(Date experimentimportdate);
	
	public Date getExperimentImportDate();
	
	public void setExperimentRemark(String experimentRemark);
	
	public void setExperimentDatabaseId(String databaseId);
	
	public String getExperimentDatabaseId();
	
	public String getExperimentRemark();
	
	public void setParent(SubstanceInterface md);
	
	public void setExperimentInfo(ExperimentHeaderInterface header);
	
	public ExperimentHeaderInterface getExperimentHeader();
	
	@Override
	public boolean add(SampleInterface arg0);
	
	@Override
	public boolean addAll(Collection<? extends SampleInterface> arg0);
	
	@Override
	public void clear();
	
	@Override
	public boolean contains(Object arg0);
	
	@Override
	public boolean containsAll(Collection<?> arg0);
	
	@Override
	public boolean isEmpty();
	
	@Override
	public boolean remove(Object arg0);
	
	@Override
	public boolean removeAll(Collection<?> arg0);
	
	@Override
	public boolean retainAll(Collection<?> arg0);
	
	@Override
	public int size();
	
	@Override
	public Object[] toArray();
	
	@Override
	public <T> T[] toArray(T[] arg0);
	
	@Override
	public Iterator<SampleInterface> iterator();
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap);
	
	@Override
	public boolean equals(Object obj);
	
	@Override
	public int hashCode();
	
	public ConditionInterface clone(SubstanceInterface parent);
	
	public ArrayList<SampleInterface> getSortedSamples();
	
	public String getExperimentOriginDbId();
	
	public void setExperimentHeader(ExperimentHeaderInterface header);
	
	public String getExperimentGlobalOutlierInfo();
	
	public String getHTMLdescription();
	
	void setField(ConditionInfo field, String value);
	
	String getField(ConditionInfo field);
}