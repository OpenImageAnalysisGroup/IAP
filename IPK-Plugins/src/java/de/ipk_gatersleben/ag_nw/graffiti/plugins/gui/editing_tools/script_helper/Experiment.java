package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.XMLHelper;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataInfoPane;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.JDOM2DOM;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.TimeAndTimeUnit;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.XPathHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class Experiment implements ExperimentInterface {
	
	LinkedList<SubstanceInterface> md;
	private ExperimentHeaderInterface header;
	static DataMappingTypeManagerInterface typemanager = new DataMappingTypeManager();
	
	public Experiment() {
		md = new LinkedList<SubstanceInterface>();
		header = new ExperimentHeader();
	}
	
	public Experiment(SubstanceInterface data) {
		this();
		add(data);
	}
	
	public Experiment(List<SubstanceInterface> data) {
		this();
		addAll(data);
	}
	
	public Experiment(ExperimentInterface data) {
		this();
		for (SubstanceInterface m : data)
			add(m);
	}
	
	public Experiment(Document doc) {
		this();
		addAll(getExperimentFromDOM(doc, null));
	}
	
	@Override
	public void addAll(ExperimentInterface m) {
		md.addAll(m);
		if (isEmpty())
			setHeader(m.getHeader());
	}
	
	private HashMap<String, String> getNiceHTMLfieldNameMapping() {
		return ExperimentHeader.getNiceHTMLfieldNameMapping();
	}
	
	@Override
	public Experiment filter(Collection<String> validNames,
			Collection<String> validTimes) {
		HashSet<String> vn = new HashSet<String>(validNames);
		HashSet<String> vt = new HashSet<String>(validTimes);
		Experiment filtered = clone();
		ArrayList<ConditionInterface> delSeries = new ArrayList<ConditionInterface>();
		ArrayList<SampleInterface> delSamples = new ArrayList<SampleInterface>();
		for (SubstanceInterface m : filtered) {
			for (ConditionInterface s : m) {
				if (!vn.contains(s.getConditionName()))
					delSeries.add(s);
				else
					for (SampleInterface ss : s) {
						TimeAndTimeUnit tt = new TimeAndTimeUnit(""
								+ ss.getTime(), ss.getTimeUnit());
						if (!vt.contains(tt.toString()))
							delSamples.add(ss);
					}
			}
		}
		for (ConditionInterface sd : delSeries)
			sd.getParentSubstance().remove(sd);
		for (SampleInterface sd : delSamples)
			sd.getParentCondition().remove(sd);
		return filtered;
	}
	
	@Override
	public String getName() {
		if (isEmpty())
			return header.getExperimentName();
		else
			return findHeader(header, this).getExperimentName();
	}
	
	@Override
	public String getRemark() {
		if (isEmpty())
			return header.getRemark();
		else
			return findHeader(header, this).getRemark();
	}
	
	@Override
	public String getCoordinator() {
		if (isEmpty())
			return header.getCoordinator();
		else
			return findHeader(header, this).getCoordinator();
	}
	
	@Override
	public Date getImportDate() {
		if (isEmpty())
			return header.getImportdate();
		else
			return findHeader(header, this).getImportdate();
	}
	
	@Override
	public Date getStartDate() {
		if (isEmpty())
			return header.getStartdate();
		else
			return findHeader(header, this).getStartdate();
	}
	
	public Collection<ExperimentInterface> splitOldStyle() {
		HashMap<String, ExperimentInterface> result = new HashMap<String, ExperimentInterface>();
		
		ArrayList<Document> dl = Experiment.getDocuments(this);
		for (Document d : dl) {
			ExperimentInterface e = new Experiment(d);
			String experimentName = e.getName();
			result.put(experimentName, e);
		}
		return result.values();
	}
	
	@Override
	public Collection<ExperimentInterface> split() {
		HashMap<String, ExperimentInterface> result = new HashMap<String, ExperimentInterface>();
		
		HashMap<String, ArrayList<ConditionInterface>> expname2cons = new HashMap<String, ArrayList<ConditionInterface>>();
		
		// first: group all conditions with the same experiment header
		for (SubstanceInterface s : this)
			for (ConditionInterface c : s) {
				if (!expname2cons.containsKey(c.getExperimentName()))
					expname2cons.put(c.getExperimentName(),
							new ArrayList<ConditionInterface>());
				expname2cons.get(c.getExperimentName()).add(c);
			}
		
		// second: clone all conditions (including their substance and all
		// samples recursively
		for (String expn : expname2cons.keySet()) {
			ExperimentInterface e = new Experiment();
			
			for (ConditionInterface con : expname2cons.get(expn)) {
				SubstanceInterface sub = con.getParentSubstance().clone();
				ConditionInterface c = con.clone(sub);
				sub.add(c);
				for (SampleInterface sam : con) {
					SampleInterface samnew = sam.clone(con);
					c.add(samnew);
					for (NumericMeasurementInterface m : sam) {
						NumericMeasurementInterface mnew = m.clone(samnew);
						samnew.add(mnew);
					}
				}
				Substance.addAndMerge(e, sub, false);
			}
			result.put(expn, e);
		}
		if (result.size() <= 0 && isEmpty())
			result.put("doesnt matter", this.clone());
		return result.values();
	}
	
	public List<NumericMeasurementInterface> getAllMeasurements() {
		List<NumericMeasurementInterface> list = new ArrayList<NumericMeasurementInterface>();
		for (SubstanceInterface sub : md)
			for (ConditionInterface series : sub)
				for (SampleInterface sample : series)
					for (NumericMeasurementInterface meas : sample)
						list.add(meas);
		
		return list;
	}
	
	@Override
	public void setHeader(ExperimentHeaderInterface header) {
		this.header = header;
		if (!isEmpty())
			for (SubstanceInterface m : this)
				for (ConditionInterface s : m)
					s.setExperimentInfo(header);
	}
	
	@Override
	public ExperimentHeaderInterface getHeader() {
		if (isEmpty())
			return header;
		else {
			if (header != null)
				return header;
			for (SubstanceInterface m : this)
				for (ConditionInterface s : m)
					return s.getExperimentHeader();
			ErrorMsg.addErrorMessage("Internal error, isEmpty false, but no seriesData!");
			return null;
		}
	}
	
	@Override
	public Collection<ExperimentHeaderInterface> getHeaders() {
		if (isEmpty()) {
			ArrayList<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
			result.add(getHeader());
			return result;
		} else {
			ArrayList<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
			if (header != null)
				result.add(header);
			for (SubstanceInterface m : this)
				for (ConditionInterface s : m)
					result.add(s.getExperimentHeader());
			return result;
		}
	}
	
	public static Document getEmptyDocument(ExperimentHeaderInterface header) {
		StringBuilder r = new StringBuilder();
		r.append("</measurements>");
		r.append("</experimentdata>");
		StringBuilder r2 = new StringBuilder();
		r2.append("<experimentdata>");
		
		r2.append(header.toString());
		r2.append("<measurements>");
		
		r2.append(r);
		
		return XMLHelper.getDocumentFromXMLstring(r2.toString());
	}
	
	public static ArrayList<Document> getDocuments(
			ExperimentInterface mappingDataList,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			boolean mergeExperimentsReturnOnlyOne) {
		ArrayList<Document> docList = new ArrayList<Document>();
		for (String s : getStrings(mappingDataList, status,
				mergeExperimentsReturnOnlyOne))
			docList.add(XMLHelper.getDocumentFromXMLstring(s));
		return docList;
	}
	
	public static ArrayList<String> getStrings(
			ExperimentInterface mappingDataList,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			boolean mergeExperimentsReturnOnlyOne) {
		final ArrayList<StringBuilder> resSB = new ArrayList<StringBuilder>();
		AppenderFactory af = new AppenderFactory() {
			@Override
			public Appender getNewAppender() {
				StringBuilder sb = new StringBuilder();
				resSB.add(sb);
				Appender ap = new Appender(sb);
				return ap;
			}
		};
		try {
			getStrings(af, mappingDataList, status, mergeExperimentsReturnOnlyOne);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ArrayList<String> res = new ArrayList<String>();
		while (!resSB.isEmpty()) {
			StringBuilder sb = resSB.get(0);
			resSB.remove(0);
			res.add(sb.toString());
		}
		return res;
	}
	
	public static void getStrings(
			AppenderFactory appenderFactory,
			ExperimentInterface mappingDataList,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			boolean mergeExperimentsReturnOnlyOne) throws Exception {
		
		HashMap<String, LinkedHashMap<SubstanceInterface, LinkedHashMap<String, ConditionInterface>>> experimentName2substanceName2Conditions = new HashMap<String, LinkedHashMap<SubstanceInterface, LinkedHashMap<String, ConditionInterface>>>();
		
		String experimentNameForAll = null;
		
		if (status != null)
			status.setCurrentStatusText2("Extracting metadata from elements");
		if (mappingDataList != null)
			for (SubstanceInterface substance : mappingDataList) {
				for (ConditionInterface condition : substance) {
					String expName = condition.getExperimentName();
					if (mergeExperimentsReturnOnlyOne
							&& experimentNameForAll == null)
						experimentNameForAll = expName;
					if (mergeExperimentsReturnOnlyOne)
						expName = experimentNameForAll;
					
					if (!experimentName2substanceName2Conditions
							.containsKey(expName))
						experimentName2substanceName2Conditions
								.put(expName,
										new LinkedHashMap<SubstanceInterface, LinkedHashMap<String, ConditionInterface>>());
					
					if (!experimentName2substanceName2Conditions.get(expName)
							.containsKey(substance))
						experimentName2substanceName2Conditions
								.get(expName)
								.put(substance, new LinkedHashMap<String, ConditionInterface>());
					
					if (!experimentName2substanceName2Conditions
							.get(expName)
							.get(substance)
							.containsKey(
									((Condition) condition)
											.getConditionName(false))) {
						experimentName2substanceName2Conditions
								.get(expName)
								.get(substance)
								.put(((Condition) condition)
										.getConditionName(false),
										condition);
					} else {
						experimentName2substanceName2Conditions
								.get(expName)
								.get(substance)
								.get(((Condition) condition)
										.getConditionName(false))
								.addAll(condition);
					}
				}
			}
		
		if (status != null)
			status.setCurrentStatusText2("Creating experiment header");
		if (experimentName2substanceName2Conditions.isEmpty()) {
			ExperimentHeaderInterface eh = findHeader(
					mappingDataList != null ? mappingDataList.getHeader()
							: null, mappingDataList);
			
			StringBuilder r = new StringBuilder();
			int measurementcount = 0;
			r.append("<experimentdata>");
			eh.toString(r, measurementcount);
			r.append("<measurements>");
			r.append("</measurements>");
			r.append("</experimentdata>");
			
			appenderFactory.getNewAppender().append(r.toString());
			
			if (status != null)
				status.setCurrentStatusText2("Finished processing empty dataset");
		} else {
			for (String expName : experimentName2substanceName2Conditions
					.keySet()) {
				
				Appender r = appenderFactory.getNewAppender();
				
				r.append("<experimentdata>");
				
				int measurementcount = 0;
				LinkedHashMap<SubstanceInterface, LinkedHashMap<String, ConditionInterface>> substances2conditions =
						experimentName2substanceName2Conditions.get(expName);
				ConditionInterface c1 = substances2conditions.values()
						.iterator().next().values().iterator().next();
				
				for (SubstanceInterface substance : substances2conditions.keySet()) {
					for (ConditionInterface sd : substances2conditions.get(substance).values()) {
						for (SampleInterface sample : sd)
							measurementcount += sample.size();
					}
				}
				
				ExperimentHeaderInterface eh = findHeader(
						c1.getExperimentHeader(), mappingDataList);
				{
					StringBuilder rr = new StringBuilder();
					eh.toString(rr, measurementcount);
					r.append(rr.toString());
				}
				r.append("<measurements>");
				
				for (SubstanceInterface substance : substances2conditions.keySet()) {
					if (status != null)
						status.setCurrentStatusText2("Process " + substance.getName());
					StringBuilder rr = new StringBuilder();
					substance.getSubstanceString(rr);
					for (ConditionInterface sd : substances2conditions.get(substance).values()) {
						sd.getStringForDocument(rr);
					}
					rr.append("</substance>");
					r.append(rr.toString());
				}
				
				r.append("</measurements>");
				r.append("</experimentdata>");
			}
			if (status != null)
				status.setCurrentStatusText2("Created String representation");
		}
	}
	
	private static ExperimentHeaderInterface findHeader(
			ExperimentHeaderInterface suggestion,
			ExperimentInterface mappingDataList) {
		if (mappingDataList.isEmpty()) {
			return new ExperimentHeader(suggestion);
		} else {
			for (SubstanceInterface s : mappingDataList)
				for (ConditionInterface c : s)
					if (c.getExperimentHeader() != null)
						return c.getExperimentHeader();
		}
		return null;
	}
	
	/**
	 * <experimentdata> <experiment experimentid="-1">
	 * <experimentname>Gluthation</experimentname> <remark>SXD plants</remark>
	 * <coordinator>Mohammad Hajirezaei</coordinator> <excelfileid/>
	 * <importusername/> <importdate>Tue Mar 10 11:39:36 CET 2009</importdate>
	 * <startdate>Sun Jan 18 00:00:00 CET 2004</startdate>
	 * <measurements>92</measurements> <imagefiles>0</imagefiles>
	 * <sizekb>0</sizekb> </experiment> <measurements> <substance id="column 6"
	 * name="Cys"> ...
	 * 
	 * @param mappingDataList
	 * @param status
	 */
	public static ArrayList<Document> getDocuments(
			ExperimentInterface mappingDataList) {
		return getDocuments(mappingDataList, null, false);
	}
	
	public static String getString(ExperimentInterface mappingDataList,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		ArrayList<String> res = getStrings(mappingDataList, optStatus, true);
		if (res.size() > 1)
			ErrorMsg.addErrorMessage("Internal error: request for merged string representation returned more than one string!");
		return res.iterator().next();
	}
	
	public static void setTypeManager(DataMappingTypeManagerInterface m) {
		Experiment.typemanager = m;
	}
	
	public static DataMappingTypeManagerInterface getTypeManager() {
		return Experiment.typemanager;
	}
	
	public static Experiment getExperimentFromDOM(org.w3c.dom.Document doc, BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		return getExperimentFromJDOM(JDOM2DOM.getJDOMfromDOM(doc), optStatus);
	}
	
	// public static List<Substance> getExperimentFromDocuments(List<Document>
	// documents) {
	//
	// List<org.jdom.Document> list = new ArrayList<org.jdom.Document>();
	// for(Document doc : documents)
	// list.add(JDOM2DOM.getJDOMfromDOM(doc));
	//
	// return getExperiment(list);
	// }
	
	public static ExperimentInterface getExperiment(
			List<org.jdom.Document> documents) {
		List<Experiment> results = new ArrayList<Experiment>();
		for (org.jdom.Document doc : documents)
			results.add(Substance.getData(doc.getRootElement(), null));
		
		if (results.size() == 0)
			return new Experiment();
		if (results.size() == 1) {
			return results.get(0);
		} else {
			Experiment mainDataset = results.get(0);
			for (int i = 1; i < results.size(); i++) {
				ExperimentInterface toBeAdded = results.get(i);
				mainDataset.addAndMerge(toBeAdded);
			}
			return mainDataset;
		}
	}
	
	public static Experiment getExperimentFromJDOM(org.jdom.Document doc, BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		Experiment e = Substance.getData(doc.getRootElement(), optStatus);
		if (e.isEmpty()) {
			Element temp = doc.getRootElement().getChild("experiment")
					.getChild("experimentname");
			if (temp != null)
				e.getHeader().setExperimentname(temp.getValue());
		}
		return e;
	}
	
	/*
	 * Delegate methods
	 */
	
	@Override
	public synchronized boolean isEmpty() {
		for (SubstanceInterface s : this)
			for (ConditionInterface c : s)
				return c == null;
		
		return true;
	}
	
	@Override
	public void add(int index, SubstanceInterface element) {
		md.add(index, element);
	}
	
	@Override
	public boolean add(SubstanceInterface e) {
		return md.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends SubstanceInterface> c) {
		return md.addAll(c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends SubstanceInterface> c) {
		return md.addAll(index, c);
	}
	
	@Override
	public boolean contains(Object o) {
		return md.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> arg0) {
		return md.containsAll(arg0);
	}
	
	@Override
	public int indexOf(Object o) {
		return md.indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return md.lastIndexOf(o);
	}
	
	@Override
	public ListIterator<SubstanceInterface> listIterator() {
		return md.listIterator();
	}
	
	@Override
	public ListIterator<SubstanceInterface> listIterator(int index) {
		return md.listIterator(index);
	}
	
	@Override
	public SubstanceInterface remove(int index) {
		return md.remove(index);
	}
	
	@Override
	public boolean remove(Object o) {
		boolean success = md.remove(o);
		if (size() == 0 && o instanceof Substance) {
			for (ConditionInterface c : (SubstanceInterface) o) {
				setHeader(c.getExperimentHeader());
				break;
			}
		}
		return success;
	}
	
	@Override
	public boolean removeAll(Collection<?> arg0) {
		return md.removeAll(arg0);
	}
	
	@Override
	public boolean retainAll(Collection<?> arg0) {
		return md.retainAll(arg0);
	}
	
	@Override
	public SubstanceInterface set(int index, SubstanceInterface element) {
		return md.set(index, element);
	}
	
	@Override
	public List<SubstanceInterface> subList(int fromIndex, int toIndex) {
		return md.subList(fromIndex, toIndex);
	}
	
	@Override
	public Object[] toArray() {
		return md.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return md.toArray(a);
	}
	
	@Override
	public String toString() {
		try {
			return toStringWithErrorThrowing();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return "<html>Could not retrieve complete document!<br>"
					+ md.toString();
		}
	}
	
	@Override
	public String toStringWithErrorThrowing() throws IOException,
			TransformerException, JDOMException {
		return getString(this, null);
	}
	
	@Override
	public int size() {
		return md.size();
	}
	
	@Override
	public SubstanceInterface get(int index) {
		return md.get(index);
	}
	
	@Override
	public Iterator<SubstanceInterface> iterator() {
		return md.iterator();
	}
	
	@Override
	public void clear() {
		md.clear();
	}
	
	@Override
	public String getSequence() {
		if (isEmpty())
			return header.getSequence();
		else
			return md.iterator().next().iterator().next().getSequence();
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		getHeader().fillAttributeMap(attributeValueMap,
				Experiment.getNumberOfMeasurementValues(this));
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#getStringOfChildren(java.lang.StringBuilder)
	 */
	@Override
	public void getStringOfChildren(StringBuilder r) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#getXMLAttributeString(java.lang.StringBuilder)
	 */
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#setAttribute(org.jdom.Attribute)
	 */
	@Override
	public void setAttribute(MyAttribute attr) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#setData(org.jdom.Element)
	 */
	@Override
	public boolean setData(Element xmlElement) {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#setDataOfChildElement(org.jdom.Element)
	 */
	@Override
	public void setDataOfChildElement(Element childElement) {
		// empty
	}
	
	public void writeToFile(File targetDir, boolean asDocument) {
		if (asDocument) {
			int cnt = 1;
			for (Document doc : getDocuments(this)) {
				String name = "Omics-Data " + (cnt++);
				Node temp = doc.getElementsByTagName("experimentname").item(0).getFirstChild();
				if (temp != null)
					name = temp.getNodeValue();
				XMLHelper.writeXMLDataToFile(doc, targetDir.getAbsolutePath() + "/" + name + ".xml");
			}
		} else
			try {
				TextFile.write(targetDir.getAbsolutePath() + ReleaseInfo.getFileSeparator() + getName() + ".xml", toString());
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
	}
	
	public void writeToFile(File targetDir) {
		
		int cnt = 1;
		for (Document doc : getDocuments(this)) {
			String name = "Omics-Data " + (cnt++);
			Node temp = doc.getElementsByTagName("experimentname").item(0)
					.getFirstChild();
			if (temp != null)
				name = temp.getNodeValue();
			XMLHelper.writeXMLDataToFile(doc, targetDir.getAbsolutePath() + "/"
					+ name + ".xml");
		}
	}
	
	public Experiment cloneOldStyle() {
		Experiment clone = null;
		ArrayList<Document> dl = Experiment.getDocuments(this);
		for (Document d : dl) {
			if (clone == null)
				clone = new Experiment(d);
			else
				clone.addAll(new Experiment(d));
		}
		for (ExperimentHeaderInterface eh : clone.getHeaders()) {
			eh.setImportusergroup(getHeader().getImportusergroup());
			eh.setImportusername(getHeader().getImportusername());
		}
		return clone;
	}
	
	@Override
	public Experiment clone() {
		Experiment clone = new Experiment();
		clone.header = header.clone();
		for (SubstanceInterface sub : md) {
			SubstanceInterface newsub = sub.clone();
			clone.add(newsub);
			for (ConditionInterface c : sub) {
				ConditionInterface newc = c.clone(newsub);
				newsub.add(newc);
				for (SampleInterface s : c) {
					SampleInterface news = s.clone(newc);
					newc.add(news);
					for (NumericMeasurementInterface m : s)
						news.add(m.clone(news));
				}
			}
		}
		return clone;
	}
	
	/**
	 * Use experiment.addAndMerge instead.
	 */
	@Deprecated
	public static void addAndMerge(ExperimentInterface result,
			ExperimentInterface toBeAdded) {
		result.addAndMerge(toBeAdded);
	}
	
	@Override
	public void addAndMerge(ExperimentInterface toBeAdded) {
		if (isEmpty() && toBeAdded.isEmpty())
			header = toBeAdded.getHeader().clone();
		else
			for (SubstanceInterface tobeMerged : toBeAdded)
				Substance.addAndMerge(this, tobeMerged, false);
	}
	
	public static String[] getTimes(ExperimentInterface experimentData) {
		LinkedHashSet<String> times = new LinkedHashSet<String>();
		if (experimentData != null)
			for (SubstanceInterface md : experimentData) {
				for (ConditionInterface sd : md) {
					for (SampleInterface s : sd) {
						String t = s.getSampleTime();
						if (!t.equals("-1 -1"))
							times.add(t);
					}
				}
			}
		if (times.size() > 0)
			return times.toArray(new String[] {});
		else
			return new String[] { XPathHelper.noGivenTimeStringConstant };
	}
	
	public static String[] getConditionsAsString(
			ExperimentInterface experimentData) {
		TreeSet<String> plants = new TreeSet<String>();
		if (experimentData != null)
			for (SubstanceInterface md : experimentData) {
				for (ConditionInterface sd : md) {
					plants.add(sd.getConditionName());
				}
			}
		if (plants.size() > 0)
			return plants.toArray(new String[] {});
		else
			return new String[] { XPathHelper.noGivenTimeStringConstant };
	}
	
	public static boolean isReplicateDataMissing(ExperimentInterface md) {
		boolean oneWithMoreThanOne = false;
		all: for (SubstanceInterface m : md)
			for (ConditionInterface c : m)
				for (SampleInterface s : c) {
					if (s.size() > 0) {
						oneWithMoreThanOne = true;
						break all;
					}
				}
		return !oneWithMoreThanOne;
	}
	
	/**
	 * Use experiment.getNumberOfMeasurementValues() instead.
	 */
	@Deprecated
	public static int getNumberOfMeasurementValues(ExperimentInterface md) {
		int res = 0;
		for (SubstanceInterface m : md) {
			res += m.getDataPointCount(false);
		}
		return res;
	}
	
	public static boolean isBiologicalAndTechnicalReplicateDataAvailable(ExperimentInterface md) {
		for (SubstanceInterface m : md) {
			ArrayList<String> condnames = new ArrayList<String>();
			for (ConditionInterface c : m) {
				String cname = c.getSpecies() + c.getGenotype() + c.getVariety() + c.getGrowthconditions() + c.getTreatment();
				if (condnames.contains(cname))
					return true;
				else
					condnames.add(cname);
			}
		}
		
		return false;
	}
	
	@Override
	public int getNumberOfMeasurementValues() {
		int res = 0;
		for (SubstanceInterface m : new ArrayList<SubstanceInterface>(this)) {
			res += m.getDataPointCount(false);
		}
		return res;
	}
	
	/**
	 * Use experiment.getMeasurementValuesSum() instead.
	 */
	@Deprecated
	public static double getMeasurementValuesSum(ExperimentInterface md) {
		double res = 0;
		for (SubstanceInterface m : md) {
			res += m.getSum();
		}
		return res;
	}
	
	@Override
	public double getMeasurementValuesSum() {
		double res = 0;
		for (SubstanceInterface m : this) {
			res += m.getSum();
		}
		return res;
	}
	
	@Override
	public String toHTMLstring() {
		StringBuilder sb = new StringBuilder();
		String tableRowBreak = "<tr><td>&nbsp;</td><td>&nbsp;</td></tr>";
		sb.append("<table class='experimentInfo'>"
				+ "<tr><th>Experiment</th><th>"
				+ StringManipulationTools.removeHTMLtags(getName())
				+ "</th></tr>" + tableRowBreak);
		
		Map<String, Object> attributeValueMap = new HashMap<String, Object>();
		header.fillAttributeMap(attributeValueMap,
				getNumberOfMeasurementValues());
		TreeMap<String, String> niceProperties = new TreeMap<String, String>();
		HashMap<String, String> field2niceName = getNiceHTMLfieldNameMapping();
		for (String key : attributeValueMap.keySet()) {
			Object o = attributeValueMap.get(key);
			if (o == null)
				continue;
			String value = StringManipulationTools.removeHTMLtags(o.toString());
			String keyName = field2niceName.get(key);
			if (keyName == null)
				continue;
			if (value == null || value.equals("null"))
				continue;// value = "[unknown]";
			if (value.contains("//") && value.contains(":")) {
				// create sub-table to show values (e.g. Aussaat:25.05.2012 // Keimung:13.03.2012 // ...)
				StringBuilder t = new StringBuilder();
				t.append("<table border=0>");
				for (String s : value.split("//")) {
					s = s.trim();
					if (s.startsWith("zoom-top")) {
						t.append("<tr><th>Top Zoom Adjustments (Zoom, X, Y)</th><th>VIS</th><th>FLUO</th><th>NIR</th><th>IR</th></tr>");
						try {
							String[] sARR = s.split(":", 2);
							String s1 = sARR[0];
							sARR = s.split(";");
							sARR[0] = sARR[0].substring("zoom-top:".length());
							String sVIS = sARR[0];
							String sFLU = sARR[1];
							String sNIR = sARR[2];
							String sIR = sARR[3];
							sVIS = sVIS.replace(":", "<br>");
							sFLU = sFLU.replace(":", "<br>");
							sNIR = sNIR.replace(":", "<br>");
							sIR = sIR.replace(":", "<br>");
							t.append("<tr><td>" + s1 + "</td><td>" + sVIS + "</td><td>" + sFLU + "</td><td>" + sNIR + "</td><td>" + sIR + "</td></tr>");
						} catch (Exception e) {
							t.append("<tr><td>Could not parse correctly: " + s + "</td></tr>");
						}
					} else {
						for (String ss : s.split(":", 2)) {
							t.append("<td>");
							t.append(ss);
							t.append("</td>");
						}
						t.append("</tr>");
					}
				}
				t.append("</table>");
				value = t.toString();
			}
			niceProperties.put(keyName, value);
		}
		for (String n : niceProperties.keySet()) {
			sb.append("<tr><td>" + n + "</td><td>" + niceProperties.get(n)
					+ "</td></tr>");
			if (n.indexOf(" BR -->") > 0)
				sb.append(tableRowBreak);
		}
		sb.append("</table>");
		return sb.toString();
	}
	
	@Override
	public void numberConditions() {
		TreeSet<String> conditions = new TreeSet<String>();
		for (SubstanceInterface si : this) {
			for (ConditionInterface ci : si) {
				Condition c = (Condition) ci;
				conditions.add(c.getConditionName(false, false));
			}
		}
		HashMap<String, Integer> condition2id = new HashMap<String, Integer>();
		int idx = 1;
		for (String cid : conditions) {
			condition2id.put(cid, idx);
			idx++;
		}
		for (SubstanceInterface si : this) {
			for (ConditionInterface ci : si) {
				Condition c = (Condition) ci;
				String cid = c.getConditionName(false, false);
				c.setRowId(condition2id.get(cid));
			}
		}
	}
	
	public void sortSubstances() {
		Collections.sort(md, new Comparator<SubstanceInterface>() {
			@Override
			public int compare(SubstanceInterface o1, SubstanceInterface o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	
	public void sortConditions() {
		for (SubstanceInterface s : md) {
			s.sortConditions();
		}
	}
	
	public static ExperimentInterface loadFromFile(String fileName)
			throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
		InputStream in = new FileInputStream(fileName);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document w3Doc = builder.parse(in);
		in.close();
		Experiment md = Experiment.getExperimentFromDOM(w3Doc, null);
		return md;
	}
	
	public static ExperimentInterface loadFromIOurl(IOurl url, BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws Exception {
		long inputStramLength = ResourceIOManager.getHandlerFromPrefix(url.getPrefix()).getStreamLength(url);
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Transfer Binary Data (" + inputStramLength / 1024
					/ 1024 + " MB)...");
		
		long start = System.currentTimeMillis();
		
		InputStream is = url.getInputStream();
		if (is == null)
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: No input stream for URL " + url);
		if (optStatus != null)
			if (is == null)
				optStatus.setCurrentStatusText2("Error: No Data");
			else {
				if (is instanceof MyByteArrayInputStream) {
					long end = System.currentTimeMillis();
					long transfered = ((MyByteArrayInputStream) is).available();
					optStatus.setCurrentStatusText1("Binary Data Transferred (" + transfered / 1024 / 1024 + " MB , "
							+ SystemAnalysis.getDataTransferSpeedString(transfered, start, end) + ")");
				}
			}
		Experiment md = loadFromXmlBinInputStream(is, inputStramLength, optStatus);
		return md;
	}
	
	public static Experiment loadFromXmlBinInputStream(InputStream is, long inputStreamLength, BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws Exception {
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Process XML structure...");
		
		boolean useDOM = false;
		
		if (useDOM) {
			SAXBuilder sb = new SAXBuilder();
			org.jdom.Document doc = sb.build(is);
			if (optStatus != null) {
				optStatus.setCurrentStatusText1("Generate experiment structure...");
				optStatus.setCurrentStatusText2("");
			}
			return Experiment.getExperimentFromJDOM(doc, optStatus);
		} else
			return Experiment.getSAXhandler(is, inputStreamLength).getExperiment(optStatus);
	}
	
	private static ExperimentSaxHandler getSAXhandler(InputStream is, long inputStreamLength) {
		return new ExperimentSaxHandler(is, inputStreamLength);
	}
	
	@Override
	public void saveToFile(String fileName) throws Exception {
		
		final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName));
		
		AppenderFactory af = new AppenderFactory() {
			@Override
			public Appender getNewAppender() {
				return new Appender(outputStream);
			}
		};
		getStrings(af, this, null, true);
		outputStream.close();
		// old: TextFile.write(fileName, toString());
		// very old:
		// stream = new FileOutputStream(xmlFile);
		// org.jdom.Document dd = JDOM2DOM.getJDOMfromDOM(getDocument());
		// out.output(dd, stream);
		// stream.close();
		
	}
	
	@Override
	public TreeSet<String> getTreatmentList() {
		TreeSet<String> result = new TreeSet<String>();
		for (SubstanceInterface si : this)
			for (ConditionInterface ci : si)
				result.add(ci.getTreatment() != null ? ci.getTreatment() : "");
		return result;
	}
	
	public void showXMLtoUser() {
		ExperimentDataInfoPane.showXMLdata(this);
	}
	
	@Override
	public ExperimentCalculationService calc() {
		return new ExperimentCalculationService(this);
	}
	
	@Override
	public String getFiles() {
		return header != null ? header.getFiles() : null;
	}
	
	@Override
	public void setFiles(String files) {
		header.setFiles(files);
	}
	
	@Override
	public void mergeBiologicalReplicates(BackgroundTaskStatusProviderSupportingExternalCall status) {
		int cnt = 0, all = size();
		for (SubstanceInterface m : this) {
			if (status != null) {
				status.setCurrentStatusText1("Processing substance " + m.getName());
				status.setCurrentStatusValue(100 * (cnt++) / all);
			}
			HashMap<String, ArrayList<ConditionInterface>> condnames = new HashMap<String, ArrayList<ConditionInterface>>();
			for (ConditionInterface c : m) {
				String cname = c.getSpecies() + c.getGenotype() + c.getVariety() + c.getGrowthconditions() + c.getTreatment();
				if (!condnames.containsKey(cname))
					condnames.put(cname, new ArrayList<ConditionInterface>());
				condnames.get(cname).add(c);
				for (SampleInterface sam : c)
					if (sam.size() > 0) {
						double value = sam.getSampleAverage().getValue();
						// remove all technical replicates and replace by mean value
						NumericMeasurementInterface val = sam.iterator().next().clone(sam);
						val.setValue(value);
						sam.clear();
						sam.add(val);
					}
			}
			
			for (Map.Entry<String, ArrayList<ConditionInterface>> entry : condnames.entrySet())
				if (entry.getValue().size() > 1) {
					Iterator<ConditionInterface> it = entry.getValue().iterator();
					ConditionInterface firstcondition = it.next();
					
					while (it.hasNext()) {
						ConditionInterface tobemerged = it.next();
						SampleInterface savesamfirst = null;
						
						// temporary list of samples, that couldn't be found in the "master" condition
						// used to later add to "firstcondition"
						ArrayList<SampleInterface> tobelatermerged = new ArrayList<SampleInterface>();
						for (SampleInterface sammerged : tobemerged) {
							for (SampleInterface samfirst : firstcondition)
								if (isSampleEqualForMerge(samfirst, sammerged)) {
									savesamfirst = samfirst;
									break;
								}
							
							if (savesamfirst != null) {
								if (sammerged.iterator().hasNext()) {
									NumericMeasurementInterface val = sammerged.iterator().next();
									val.setParentSample(savesamfirst);
									savesamfirst.add(val);
								}
							} else {
								savesamfirst = sammerged;
								sammerged.setParent(firstcondition);
								tobelatermerged.add(sammerged);
							}
						}
						
						firstcondition.addAll(tobelatermerged);
						
						if (tobemerged.getParentSubstance() != null)
							tobemerged.getParentSubstance().remove(tobemerged);
						tobemerged.setParent(null);
					}
					
					for (SampleInterface sam : firstcondition)
						sam.recalculateSampleAverage(false);
					
				}
		}
		if (status != null) {
			status.setCurrentStatusText1("Processing finished!");
			status.setCurrentStatusText2("All biological replicates were merged");
		}
		
	}
	
	private boolean isSampleEqualForMerge(SampleInterface sample1, SampleInterface sample2) {
		if (sample1 == null || sample2 == null)
			return false;
		String s1 = sample1.getMeasurementtool() + ";" + sample1.getTime() + ";" + sample1.getTimeUnit() + ";" + sample1.getTtestInfo().name();
		String s2 = sample2.getMeasurementtool() + ";" + sample2.getTime() + ";" + sample2.getTimeUnit() + ";" + sample2.getTtestInfo().name();
		return s1.equals(s2);
		
	}
	
	public static void write(ExperimentInterface ei, BackgroundTaskStatusProviderSupportingExternalCall optStatus, final OutputStream outputStream)
			throws Exception {
		AppenderFactory af = new AppenderFactory() {
			@Override
			public Appender getNewAppender() {
				Appender ap = new Appender(outputStream);
				return ap;
			}
		};
		getStrings(af, ei, optStatus, true);
		outputStream.close();
	}
	
	public static ExperimentInterface copyAndExtractSubtanceInclusiveData(Collection<SubstanceInterface> toBeExtracted) {
		Experiment experiment = new Experiment();
		if (toBeExtracted.isEmpty())
			return experiment;
		if (!toBeExtracted.iterator().next().isEmpty())
			experiment.setHeader(toBeExtracted.iterator().next().iterator().next().getExperimentHeader().clone());
		for (SubstanceInterface sub : toBeExtracted) {
			SubstanceInterface su = sub.clone();
			for (ConditionInterface c : sub) {
				ConditionInterface cu = c.clone(su);
				for (SampleInterface si : c) {
					SampleInterface sic = si.clone(cu);
					for (NumericMeasurementInterface nmi : si) {
						NumericMeasurementInterface nmic = nmi.clone(sic);
						sic.add(nmic);
					}
					cu.add(sic);
				}
				su.add(cu);
			}
			experiment.add(su);
		}
		return experiment;
	}
	
	public static ExperimentInterface copyAndExtractSubtanceInclusiveData(SubstanceInterface toBeExtracted) {
		ArrayList<SubstanceInterface> sl = new ArrayList<SubstanceInterface>();
		sl.add(toBeExtracted);
		return copyAndExtractSubtanceInclusiveData(sl);
	}
	
	public ExperimentStatistics getExperimentStatistics() {
		return new ExperimentStatistics(this);
	}
}
