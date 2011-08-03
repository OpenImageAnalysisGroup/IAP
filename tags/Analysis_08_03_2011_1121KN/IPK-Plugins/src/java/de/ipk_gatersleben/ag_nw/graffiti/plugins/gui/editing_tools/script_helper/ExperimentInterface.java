package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.transform.TransformerException;

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
	
	public boolean isEmpty();
	
	public void add(int index, SubstanceInterface element);
	
	public boolean add(SubstanceInterface e);
	
	public boolean addAll(Collection<? extends SubstanceInterface> c);
	
	public boolean addAll(int index, Collection<? extends SubstanceInterface> c);
	
	public boolean contains(Object o);
	
	public boolean containsAll(Collection<?> arg0);
	
	public void ensureCapacity(int minCapacity);
	
	public int indexOf(Object o);
	
	public int lastIndexOf(Object o);
	
	public ListIterator<SubstanceInterface> listIterator();
	
	public ListIterator<SubstanceInterface> listIterator(int index);
	
	public SubstanceInterface remove(int index);
	
	public boolean remove(Object o);
	
	public boolean removeAll(Collection<?> arg0);
	
	public boolean retainAll(Collection<?> arg0);
	
	public SubstanceInterface set(int index, SubstanceInterface element);
	
	public List<SubstanceInterface> subList(int fromIndex, int toIndex);
	
	public Object[] toArray();
	
	public <T> T[] toArray(T[] a);
	
	public String toString();
	
	public String toStringWithErrorThrowing() throws IOException, TransformerException, JDOMException;
	
	public void trimToSize();
	
	public int size();
	
	public SubstanceInterface get(int index);
	
	public Iterator<SubstanceInterface> iterator();
	
	public void clear();
	
	public String getSequence();
	
	public void fillAttributeMap(Map<String, Object> attributeValueMap);
	
	public int getNumberOfMeasurementValues();
	
	public double getMeasurementValuesSum();
	
	public void addAndMerge(ExperimentInterface toBeAdded);
	
	public String toHTMLstring();
	
}