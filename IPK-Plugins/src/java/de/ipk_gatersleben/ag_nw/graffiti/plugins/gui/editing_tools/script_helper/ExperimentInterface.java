package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.jdom.JDOMException;

public interface ExperimentInterface extends List<SubstanceInterface>, Cloneable, MappingDataEntity {
	
	public static final String UNSPECIFIED_ATTRIBUTE_STRING = "unspecified";
	public static final String UNSPECIFIED_EXPERIMENTNAME = "Untitled";
	public static final String UNSPECIFIED_SUBSTANCE = "dummy substance";
	
	public void addAll(ExperimentInterface m);
	
	public ExperimentInterface filter(Collection<String> validNames, Collection<String> validTimes);
	
	public String getName();
	
	public String getRemark();
	
	public String getCoordinator();
	
	public Date getImportDate();
	
	public Date getStartDate();
	
	public ExperimentInterface clone();
	
	public Collection<ExperimentInterface> split();
	
	public void setHeader(ExperimentHeaderInterface header);
	
	public ExperimentHeaderInterface getHeader();
	
	public Collection<ExperimentHeaderInterface> getHeaders();
	
	@Override
	public boolean isEmpty();
	
	@Override
	public void add(int index, SubstanceInterface element);
	
	@Override
	public boolean add(SubstanceInterface e);
	
	@Override
	public boolean addAll(Collection<? extends SubstanceInterface> c);
	
	@Override
	public boolean addAll(int index, Collection<? extends SubstanceInterface> c);
	
	@Override
	public boolean contains(Object o);
	
	@Override
	public boolean containsAll(Collection<?> arg0);
	
	public void ensureCapacity(int minCapacity);
	
	@Override
	public int indexOf(Object o);
	
	@Override
	public int lastIndexOf(Object o);
	
	@Override
	public ListIterator<SubstanceInterface> listIterator();
	
	@Override
	public ListIterator<SubstanceInterface> listIterator(int index);
	
	@Override
	public SubstanceInterface remove(int index);
	
	@Override
	public boolean remove(Object o);
	
	@Override
	public boolean removeAll(Collection<?> arg0);
	
	@Override
	public boolean retainAll(Collection<?> arg0);
	
	@Override
	public SubstanceInterface set(int index, SubstanceInterface element);
	
	@Override
	public List<SubstanceInterface> subList(int fromIndex, int toIndex);
	
	@Override
	public Object[] toArray();
	
	@Override
	public <T> T[] toArray(T[] a);
	
	@Override
	public String toString();
	
	public String toStringWithErrorThrowing() throws IOException, TransformerException, JDOMException;
	
	public void trimToSize();
	
	@Override
	public int size();
	
	@Override
	public SubstanceInterface get(int index);
	
	@Override
	public Iterator<SubstanceInterface> iterator();
	
	@Override
	public void clear();
	
	public String getSequence();
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap);
	
	public int getNumberOfMeasurementValues();
	
	public double getMeasurementValuesSum();
	
	public void addAndMerge(ExperimentInterface toBeAdded);
	
	public String toHTMLstring();
	
	public void numberConditions();
	
	public void saveToFile(String string) throws Exception;
	
	public TreeSet<String> getTreatmentList();
	
	public ExperimentCalculationService calc();
	
	public void mergeBiologicalReplicates(BackgroundTaskStatusProviderSupportingExternalCall status);
}