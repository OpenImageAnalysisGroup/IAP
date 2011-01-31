/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.ErrorMsg;
import org.HelperClass;
import org.StringManipulationTools;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeExistsException;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.FieldAlreadySetException;
import org.graffiti.attributes.NoCollectionAttributeException;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.LabelAttribute;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute.XMLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.DataSetRow;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.DataSetTable;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.RemoveMappingDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class NodeHelper implements Node, HelperClass {
	
	private final Node n;
	private final boolean lastNode;
	
	public NodeHelper(Node n, boolean isLastNode) {
		this.n = n;
		this.lastNode = isLastNode;
	}
	
	/**
	 * Enumerate alternative identifiers, assigned to the mapped xml substance
	 * data.
	 * 
	 * @return A list of alternative names, assigned to a node and its mapping
	 *         data.
	 */
	public ArrayList<String> getAlternativeIDs() {
		ArrayList<String> result = new ArrayList<String>();
		for (SubstanceInterface md : getDataMappings()) {
			result.addAll(md.getSynonyms());
		}
		return result;
	}
	
	/**
	 * Enumerate alternative identifiers, assigned to the mapped xml substance
	 * data. Only the specified index value is processed.
	 * 
	 * @return A list of alternative names, assigned to a node and its mapping
	 *         data.
	 */
	public ArrayList<String> getAlternativeIDsWithIdx(int index) {
		ArrayList<String> result = new ArrayList<String>();
		for (SubstanceInterface md : getDataMappings()) {
			String altId = md.getSynonyme(index);
			result.add(altId);
		}
		return result;
	}
	
	private ArrayList<MemSample> memSamples = null;
	private ArrayList<MemPlant> memPlants = null;
	
	public boolean memSample(double value, int replicate, int plantID, String unit, String optTimeUnit,
						Integer optTimeValueForComparision) {
		if (memSamples == null)
			memSamples = new ArrayList<MemSample>();
		memSamples.add(new MemSample(value, replicate, plantID, unit, optTimeUnit, optTimeValueForComparision));
		return true;
	}
	
	public int memGetPlantID(String species, String genotype, String optVariety, String optGrowthConditions,
						String optTreatment) {
		if (memPlants == null)
			memPlants = new ArrayList<MemPlant>();
		int idx = 0;
		for (MemPlant mp : memPlants) {
			idx++;
			if (mp.getSpecies().equals(species) && mp.getGenotype().equals(genotype))
				return idx;
		}
		MemPlant mp = new MemPlant(species, genotype, optVariety, optGrowthConditions, optTreatment);
		memPlants.add(mp);
		return memPlants.size();
	}
	
	public boolean memAddDataMapping(String substanceName, String measurementUnit, String experimentStart,
						String experimentName, String coordinator, String optRemark, String optSequence) {
		if (memPlants == null || memPlants.size() <= 0) {
			ErrorMsg.addErrorMessage("No plants defined (use memGetPlantID), can not create and add mapping data!");
			return false;
		}
		if (memSamples == null || memSamples.size() <= 0) {
			ErrorMsg.addErrorMessage("No samples defined (use memSample), can not create and add mapping data!");
			return false;
		}
		ExperimentInterface d = getMappingDataDocument(substanceName, measurementUnit, experimentStart, experimentName,
							coordinator, optRemark, optSequence);
		if (d != null) {
			for (SubstanceInterface m : d)
				Experiment2GraphHelper.addMappingData2Node(m, getGraphNode());
		}
		memPlants.clear();
		memSamples.clear();
		return d != null;
	}
	
	public void addDataMapping(Collection<DataSetRow> datasetRows, String substanceName) {
		// memGetPlantID(species, genotype, optVariety, optGrowthConditions,
		// optTreatment)
		String unit = "";
		String experimentName = "";
		for (DataSetRow dsr : datasetRows) {
			if (dsr.substanceName == null || !dsr.substanceName.equals(substanceName))
				continue;
			int plantID = memGetPlantID(dsr.species, dsr.genotype, "", "", dsr.treatment);
			unit = dsr.unit;
			experimentName = dsr.experimentName;
			Integer timeValue = null;
			try {
				timeValue = Integer.parseInt(dsr.timeS);
			} catch (Exception e) {
				timeValue = new Integer(-1);
			}
			String timeUnit = dsr.timeUnit;
			if (timeUnit == null || timeUnit.length() <= 0 || timeUnit.equals("NA"))
				timeUnit = "-1";
			int replicateID = -1;
			try {
				replicateID = dsr.replicateID;
			} catch (Exception e) {
			}
			memSample(dsr.value.doubleValue(), replicateID, plantID, dsr.unit, timeUnit, timeValue);
		}
		memAddDataMapping(substanceName, unit, "", experimentName, "Coordinator", "", "");
	}
	
	public ExperimentInterface getMappingDataDocument(String substanceName, String measurementUnit,
						String experimentStart, String experimentName, String coordinator, String optRemark, String optSequence) {
		ExperimentInterface d = ExperimentConstructor.processData(substanceName, measurementUnit, memSamples, memPlants,
							experimentStart, experimentName, coordinator, optRemark, optSequence);
		return d;
	}
	
	public NodeHelper(Node workNode) {
		this(workNode, false);
	}
	
	public boolean writeDatasetTable(String fileName, boolean useAverage) {
		TextFile tf = new TextFile();
		tf.add(getDatasetTable().toString());
		
		try {
			tf.write(fileName);
			return true;
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
			return false;
		}
	}
	
	private String checkStringFormat(String text) {
		if (text == null || text.equals("NA"))
			return "NA";
		else
			return "\"" + text + "\"";
	}
	
	private String checkFormat(String text) {
		text = StringManipulationTools.stringReplace(text, "\"", "\\\"");
		return text;
	}
	
	private String getRowLabel(int i, int minWidth) {
		String result = "" + i;
		while (result.length() < minWidth)
			result = "0" + result;
		return result;
	}
	
	public Node getGraphNode() {
		return n;
	}
	
	public void setFillColor(Color c) {
		AttributeHelper.setFillColor(n, c);
	}
	
	public Color getFillColor() {
		return AttributeHelper.getFillColor(n);
	}
	
	public boolean isLastNode() {
		return lastNode;
	}
	
	public Color getBorderColor() {
		return AttributeHelper.getOutlineColor(n);
	}
	
	public void setBorderColor(Color c) {
		AttributeHelper.setOutlineColor(n, c);
	}
	
	public void setSize(double width, double height) {
		AttributeHelper.setSize(n, width, height);
	}
	
	public double getWidth() {
		return AttributeHelper.getSize(n).x;
	}
	
	public double getHeight() {
		return AttributeHelper.getSize(n).y;
	}
	
	public String getClusterID(String ifNoCluster) {
		return NodeTools.getClusterID(n, ifNoCluster);
	}
	
	public void setClusterID(String clusterID) {
		NodeTools.setClusterID(n, clusterID);
	}
	
	public void setPosition(double x, double y) {
		AttributeHelper.setPosition(n, x, y);
	}
	
	public double getX() {
		return AttributeHelper.getPositionX(n);
	}
	
	public double getY() {
		return AttributeHelper.getPositionY(n);
	}
	
	public String getURL() {
		String u = AttributeHelper.getReferenceURL(n);
		return u;
	}
	
	public void setURL(String url) {
		AttributeHelper.setReferenceURL(n, url);
	}
	
	public String getPathwayReference() {
		String u = AttributeHelper.getPathwayReference(n);
		return u;
	}
	
	public void setPathwayReference(String ref) {
		AttributeHelper.setPathwayReference(n, ref);
	}
	
	public void setAttributeValue(String path, String name, Object value) {
		AttributeHelper.setAttribute(n, path, name, value);
	}
	
	public Object getAttributeValue(String path, String name, Object returnIfNull, Object returnType) {
		return AttributeHelper.getAttributeValue(n, path, name, returnIfNull, returnType);
	}
	
	public String getTooltip() {
		return AttributeHelper.getToolTipText(n);
	}
	
	public void setTooltip(String tooltip) {
		AttributeHelper.setToolTipText(n, tooltip);
	}
	
	public void setLabel(String label) {
		AttributeHelper.setLabel(n, label);
	}
	
	public String getLabel() {
		return AttributeHelper.getLabel(n, "");
	}
	
	public double getBorderWidth() {
		return AttributeHelper.getFrameThickNess(n);
	}
	
	public void setBorderWidth(double w) {
		AttributeHelper.setBorderWidth(n, w);
	}
	
	public void setRounding(double r) {
		AttributeHelper.setRoundedEdges(n, r);
	}
	
	public double getRounding() {
		return AttributeHelper.getRoundedEdges(n);
	}
	
	public ExperimentInterface getDataMappings() {
		try {
			CollectionAttribute ca = (CollectionAttribute) n.getAttribute(Experiment2GraphHelper.mapFolder);
			XMLAttribute xa = (XMLAttribute) ca.getAttribute(Experiment2GraphHelper.mapVarName);
			return xa.getMappedData();
		} catch (AttributeNotFoundException e) {
			// no mapping data
			return new Experiment();
		}
	}
	
	public ArrayList<SubstanceInterface> getMappings() {
		ArrayList<SubstanceInterface> result = new ArrayList<SubstanceInterface>();
		try {
			CollectionAttribute ca = (CollectionAttribute) n.getAttribute(Experiment2GraphHelper.mapFolder);
			XMLAttribute xa = (XMLAttribute) ca.getAttribute(Experiment2GraphHelper.mapVarName);
			for (SubstanceInterface xmldata : xa.getMappedData()) {
				result.add(xmldata);
			}
		} catch (AttributeNotFoundException e) {
			// no mapping data
		}
		return result;
	}
	
	public Set<String> getMappedSeriesNames() {
		HashSet<String> seriesnames = new HashSet<String>();
		for (ConditionInterface sd : getMappedSeriesData())
			seriesnames.add(sd.getConditionName());
		
		return seriesnames;
	}
	
	public ArrayList<ConditionInterface> getMappedSeriesData() {
		ArrayList<ConditionInterface> result = new ArrayList<ConditionInterface>();
		for (SubstanceInterface md : getDataMappings()) {
			result.addAll(md);
		}
		return result;
	}
	
	// public List<org.w3c.dom.Node> getMappedSeriesDataDomNodes() {
	// List<org.w3c.dom.Node> result = new ArrayList<org.w3c.dom.Node>();
	// ArrayList<MappingData> mdl = getDataMappings();
	//
	// for (MappingData md : mdl) {
	// org.w3c.dom.Node n = md.getXMLnode().getFirstChild();
	// while(n!=null) {
	// result.add(n);
	// n = n.getNextSibling();
	// }
	// }
	//
	// return result;
	// }
	
	public ArrayList<NumericMeasurementInterface> getMappedSampleData() {
		ArrayList<NumericMeasurementInterface> result = new ArrayList<NumericMeasurementInterface>();
		for (SubstanceInterface md : getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (SampleInterface s : sd)
					result.addAll(s);
			}
		}
		return result;
	}
	
	public ArrayList<SampleAverageInterface> getMappedAverageSampleData() {
		ArrayList<SampleAverageInterface> result = new ArrayList<SampleAverageInterface>();
		for (SubstanceInterface md : getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (SampleInterface s : sd)
					result.add(s.getSampleAverage());
			}
		}
		return result;
	}
	
	public ArrayList<NumericMeasurementInterface> getMappedSampleDataForTimePoint(int timeValue) {
		ArrayList<NumericMeasurementInterface> result = new ArrayList<NumericMeasurementInterface>();
		for (NumericMeasurementInterface sd : getMappedSampleData()) {
			if (sd.getParentSample().getTime() == timeValue)
				result.add(sd);
		}
		return result;
	}
	
	public ArrayList<Double> getMappedMeanValuesForTimePoint(int timeValue) {
		ArrayList<Double> result = new ArrayList<Double>();
		for (ConditionInterface sd : getMappedSeriesData()) {
			ArrayList<Double> mv = sd.getMeanValues();
			ArrayList<Integer> mvtimes = sd.getMeanTimePoints();
			for (int i = 0; i < mvtimes.size(); i++) {
				int time = mvtimes.get(i);
				if (time == timeValue)
					result.add(mv.get(i));
			}
		}
		return result;
	}
	
	public Set<Integer> getMappedTimePointsCoveredByAllLines() {
		List<HashSet<Integer>> timePoints = new ArrayList<HashSet<Integer>>();
		for (ConditionInterface sd : getMappedSeriesData()) {
			HashSet<Integer> tp = new HashSet<Integer>();
			sd.getTimes(tp);
			timePoints.add(tp);
		}
		HashSet<Integer> timePointsCoveredInAllSeries = new HashSet<Integer>();
		Set<Integer> allPossibleTimePoints = getMappedUniqueTimePoints();
		for (Integer checkTime : allPossibleTimePoints) {
			boolean inAll = true;
			for (HashSet<Integer> checkSeries : timePoints) {
				if (!checkSeries.contains(checkTime)) {
					inAll = false;
					break;
				}
			}
			if (inAll)
				timePointsCoveredInAllSeries.add(checkTime);
		}
		return timePointsCoveredInAllSeries;
	}
	
	public Set<Integer> getMappedUniqueTimePoints() {
		HashSet<Integer> result = new HashSet<Integer>();
		for (SubstanceInterface md : getDataMappings()) {
			for (ConditionInterface sd : md) {
				sd.getTimes(result);
			}
		}
		return result;
	}
	
	public void setChartType(GraffitiCharts chartType0123456) {
		// if (chartType0123456==0)
		NodeTools.setNodeComponentType(n, chartType0123456.getName());
		// else
		// if (chartType0123456==1)
		// NodeTools.setNodeComponentType(n,
		// XMLAttribute.nodeTypeChart2D_type1_line);
		// else
		// if (chartType0123456==2)
		// NodeTools.setNodeComponentType(n,
		// XMLAttribute.nodeTypeChart2D_type2_bar);
		// else
		// if (chartType0123456==3)
		// NodeTools.setNodeComponentType(n,
		// XMLAttribute.nodeTypeChart2D_type3_bar_flat);
		// else
		// if (chartType0123456==4)
		// NodeTools.setNodeComponentType(n,
		// XMLAttribute.nodeTypeChart2D_type4_pie);
		// else
		// if (chartType0123456==5)
		// NodeTools.setNodeComponentType(n,
		// XMLAttribute.nodeTypeChart2D_type5_pie3d);
		// else
		// if (chartType0123456==6)
		// NodeTools.setNodeComponentType(n,
		// XMLAttribute.nodeTypeChart2D_type6_heatmap);
		// else
		// if (chartType0123456==-1)
		// NodeTools.setNodeComponentType(n, XMLAttribute.nodeTypeChart_auto);
		// else
		// ErrorMsg.addErrorMessage("Internal Error: Invalid diagram style id: Valid is only type -1...6!");
	}
	
	public double getAverage() {
		double sum = 0;
		ExperimentInterface mdl = getDataMappings();
		for (SubstanceInterface md : mdl)
			sum += md.getAverage();
		return sum / mdl.size();
	}
	
	public Collection<Edge> getAllInEdges() {
		return n.getAllInEdges();
	}
	
	public Collection<Node> getAllInNeighbors() {
		return n.getAllInNeighbors();
	}
	
	public Collection<Edge> getAllOutEdges() {
		return n.getAllOutEdges();
	}
	
	public Collection<Node> getAllOutNeighbors() {
		return getAllInNeighbors();
	}
	
	public Collection<Edge> getDirectedInEdges() {
		return n.getDirectedInEdges();
	}
	
	public Iterator<Edge> getDirectedInEdgesIterator() {
		return n.getDirectedInEdgesIterator();
	}
	
	public Collection<Edge> getDirectedOutEdges() {
		return n.getDirectedOutEdges();
	}
	
	public Iterator<Edge> getDirectedOutEdgesIterator() {
		return n.getDirectedOutEdgesIterator();
	}
	
	public Collection<Edge> getEdges() {
		return n.getEdges();
	}
	
	public Iterator<Edge> getEdgesIterator() {
		return n.getEdgesIterator();
	}
	
	public int getInDegree() {
		return n.getInDegree();
	}
	
	public Set<Node> getInNeighbors() {
		return n.getInNeighbors();
	}
	
	public Iterator<Node> getInNeighborsIterator() {
		return n.getInNeighborsIterator();
	}
	
	public Set<Node> getNeighbors() {
		return n.getNeighbors();
	}
	
	public Iterator<Node> getNeighborsIterator() {
		return n.getNeighborsIterator();
	}
	
	public int getOutDegree() {
		return n.getOutDegree();
	}
	
	public Set<Node> getOutNeighbors() {
		return n.getOutNeighbors();
	}
	
	public Iterator<Node> getOutNeighborsIterator() {
		return n.getOutNeighborsIterator();
	}
	
	public Iterator<Node> getUndirectedNeighborsIterator() {
		return n.getUndirectedNeighborsIterator();
	}
	
	public Collection<Edge> getUndirectedEdges() {
		return n.getUndirectedEdges();
	}
	
	public Iterator<Edge> getUndirectedEdgesIterator() {
		return n.getUndirectedEdgesIterator();
	}
	
	public Collection<Node> getUndirectedNeighbors() {
		return n.getUndirectedNeighbors();
	}
	
	public void setGraph(Graph graph) {
		n.setGraph(graph);
	}
	
	public Graph getGraph() {
		return n.getGraph();
	}
	
	public void setID(long id) {
		n.setID(id);
	}
	
	public long getID() {
		return n.getID();
	}
	
	public int getViewID() {
		return n.getViewID();
	}
	
	public void setViewID(int id) {
		n.setViewID(id);
	}
	
	public Attribute getAttribute(String path) throws AttributeNotFoundException {
		return n.getAttribute(path);
	}
	
	public CollectionAttribute getAttributes() {
		return n.getAttributes();
	}
	
	public synchronized void setBoolean(String path, boolean value) {
		n.setBoolean(path, value);
	}
	
	public boolean getBoolean(String path) throws AttributeNotFoundException {
		return n.getBoolean(path);
	}
	
	public synchronized void setByte(String path, byte value) {
		n.setByte(path, value);
	}
	
	public byte getByte(String path) throws AttributeNotFoundException {
		return n.getByte(path);
	}
	
	public synchronized void setDouble(String path, double value) {
		n.setDouble(path, value);
	}
	
	public double getDouble(String path) throws AttributeNotFoundException {
		return n.getDouble(path);
	}
	
	public synchronized void setFloat(String path, float value) {
		n.setFloat(path, value);
	}
	
	public float getFloat(String path) throws AttributeNotFoundException {
		return n.getFloat(path);
	}
	
	public synchronized void setInteger(String path, int value) {
		n.setInteger(path, value);
	}
	
	public int getInteger(String path) throws AttributeNotFoundException {
		return n.getInteger(path);
	}
	
	public ListenerManager getListenerManager() {
		return n.getListenerManager();
	}
	
	public synchronized void setLong(String path, long value) {
		n.setLong(path, value);
	}
	
	public long getLong(String path) throws AttributeNotFoundException {
		return n.getLong(path);
	}
	
	public synchronized void setShort(String path, short value) {
		n.setShort(path, value);
	}
	
	public short getShort(String path) throws AttributeNotFoundException {
		return n.getShort(path);
	}
	
	public synchronized void setString(String path, String value) {
		n.setString(path, value);
	}
	
	public String getString(String path) throws AttributeNotFoundException {
		return n.getString(path);
	}
	
	public synchronized void addAttribute(Attribute attr, String path) throws AttributeExistsException,
						NoCollectionAttributeException, FieldAlreadySetException {
		n.addAttribute(attr, path);
	}
	
	public synchronized void addBoolean(String path, String id, boolean value) throws NoCollectionAttributeException,
						AttributeExistsException, FieldAlreadySetException {
		n.addBoolean(path, id, value);
	}
	
	public synchronized void addByte(String path, String id, byte value) throws NoCollectionAttributeException,
						AttributeExistsException, FieldAlreadySetException {
		n.addByte(path, id, value);
	}
	
	public synchronized void addDouble(String path, String id, double value) throws NoCollectionAttributeException,
						AttributeExistsException, FieldAlreadySetException {
		n.addDouble(path, id, value);
	}
	
	public synchronized void addFloat(String path, String id, float value) throws NoCollectionAttributeException,
						AttributeExistsException, FieldAlreadySetException {
		n.addFloat(path, id, value);
	}
	
	public synchronized void addInteger(String path, String id, int value) throws NoCollectionAttributeException,
						AttributeExistsException, FieldAlreadySetException {
		n.addInteger(path, id, value);
	}
	
	public synchronized void addLong(String path, String id, long value) throws NoCollectionAttributeException,
						AttributeExistsException, FieldAlreadySetException {
		n.addLong(path, id, value);
	}
	
	public synchronized void addShort(String path, String id, short value) throws NoCollectionAttributeException,
						AttributeExistsException, FieldAlreadySetException {
		n.addShort(path, id, value);
	}
	
	public synchronized void addString(String path, String id, String value) throws NoCollectionAttributeException,
						AttributeExistsException, FieldAlreadySetException {
		n.addString(path, id, value);
	}
	
	public synchronized void changeBoolean(String path, boolean value) throws AttributeNotFoundException {
		n.changeBoolean(path, value);
	}
	
	public synchronized void changeByte(String path, byte value) throws AttributeNotFoundException {
		n.changeByte(path, value);
	}
	
	public synchronized void changeDouble(String path, double value) throws AttributeNotFoundException {
		n.changeDouble(path, value);
	}
	
	public synchronized void changeFloat(String path, float value) throws AttributeNotFoundException {
		n.changeFloat(path, value);
	}
	
	public synchronized void changeInteger(String path, int value) throws AttributeNotFoundException {
		n.changeInteger(path, value);
	}
	
	public synchronized void changeLong(String path, long value) throws AttributeNotFoundException {
		n.changeLong(path, value);
	}
	
	public synchronized void changeShort(String path, short value) throws AttributeNotFoundException {
		n.changeShort(path, value);
	}
	
	public synchronized void changeString(String path, String value) throws AttributeNotFoundException {
		n.changeString(path, value);
	}
	
	public synchronized Attribute removeAttribute(String path) throws AttributeNotFoundException {
		return n.removeAttribute(path);
	}
	
	public synchronized void removeDataMapping() {
		RemoveMappingDataAlgorithm.removeMappingDataFrom(n);
	}
	
	public synchronized void addDataMapping(SubstanceInterface mappingData) {
		Experiment2GraphHelper.addMappingData2Node(mappingData, n);
	}
	
	public void mergeMultipleMappings() {
		ExperimentInterface mappingList = Experiment2GraphHelper.getMappedDataListFromGraphElement(n);
		if (mappingList != null) {
			SubstanceInterface mapping1 = mappingList.iterator().next();
			for (SubstanceInterface m : mappingList) {
				if (m == mapping1)
					continue;
				mapping1.addAll(m);
			}
			removeDataMapping();
			addDataMapping(mapping1);
			AttributeHelper.deleteAttribute(n, "charting", "diagramtitle*");
		}
	}
	
	public int getDegree() {
		return n.getDegree();
	}
	
	public static List<NodeHelper> getNodeHelperList(Collection<Node> nodes) {
		List<NodeHelper> result = new ArrayList<NodeHelper>();
		for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
			Node n = it.next();
			result.add(new NodeHelper(n, !it.hasNext()));
		}
		return result;
	}
	
	public void setPosition(Point2D position) {
		setPosition(position.getX(), position.getY());
	}
	
	public Point2D getPosition() {
		return new Point2D.Double(getX(), getY());
	}
	
	/**
	 * Shortcut method to modify the range axis minimum and maximum value.
	 * Besides setting the minimum and maximum value, this method enables the use
	 * of the custom values. You may disable the use of the custom range with the
	 * method <code>setChartSettingUseCustomRange</code>.
	 * 
	 * @param minValue
	 * @param maxValue
	 */
	public void setChartRange(double minValue, double maxValue) {
		setAttributeValue("charting", "minRange", new Double(minValue));
		setAttributeValue("charting", "maxRange", new Double(maxValue));
		setAttributeValue("charting", "useCustomRange", new Boolean(true));
	}
	
	/**
	 * Shortcut method to enable or disable the display of a custom range. (see
	 * also <code>setChartRange</code>) Implementation:
	 * setAttributeValue("charting", "useCustomRange", new Boolean(set));
	 */
	public void setChartSettingUseCustomRange(boolean set) {
		setAttributeValue("charting", "useCustomRange", new Boolean(set));
	}
	
	public double getMappedMinSampleAvgValue() {
		double min = Double.NaN;
		for (SampleAverageInterface sd : getMappedAverageSampleData()) {
			double thisMin = sd.getValue();
			if (thisMin < min || Double.isNaN(min))
				if (!Double.isNaN(thisMin))
					min = thisMin;
		}
		return min;
	}
	
	public double getMappedMaxSampleAvgValue() {
		double max = Double.NaN;
		for (SampleAverageInterface sd : getMappedAverageSampleData()) {
			double thisMax = sd.getValue();
			if (thisMax > max || Double.isNaN(max))
				if (!Double.isNaN(thisMax))
					max = thisMax;
		}
		return max;
	}
	
	public DataSetTable getDatasetTable() {
		String substanceName = getLabel();
		DataSetTable dst = new DataSetTable();
		int idx = 0;
		int row = 1;
		for (SubstanceInterface md : getDataMappings()) {
			idx++;
			String mapping = "" + idx;
			for (ConditionInterface sd : md) {
				String experimentName = sd.getExperimentName();
				String species = sd.getSpecies();
				String genotype = sd.getGenotype();
				String treatment = sd.getTreatment();
				int seriesId = sd.getSeriesId();
				for (SampleInterface spd : sd) {
					int time = spd.getTime();
					String timeS = time + "";
					if (time == -1)
						timeS = "NA";
					String timeUnit = spd.getTimeUnit();
					if (timeUnit.equalsIgnoreCase("-1"))
						timeUnit = "NA";
					// String measUnit = spd.getUnit();
					for (NumericMeasurementInterface m : spd) {
						dst.addRow(getRowLabel(row++, 5), checkFormat(experimentName), checkFormat(substanceName), mapping,
											checkFormat(species), checkFormat(genotype), checkFormat(treatment), seriesId, timeS,
											checkStringFormat(timeUnit), m.getReplicateID(), m.getValue(), checkFormat(m.getUnit()));
					}
				}
			}
		}
		return dst;
	}
	
	public HashSet<Node> getAllOutChildNodes() {
		HashSet<Node> result = new HashSet<Node>();
		enumerateChildNodes(this.getGraphNode(), result);
		return result;
	}
	
	private static void enumerateChildNodes(Node n, Collection<Node> result) {
		if (result.contains(n))
			return;
		result.add(n);
		for (Node nn : n.getAllOutNeighbors())
			enumerateChildNodes(nn, result);
	}
	
	public boolean hasDataMapping() {
		try {
			Attribute a = n.getAttribute(Experiment2GraphHelper.mapFolder + Attribute.SEPARATOR
								+ Experiment2GraphHelper.mapVarName);
			return a != null;
		} catch (AttributeNotFoundException anfe) {
			return false;
		}
	}
	
	public TreeMap<DataMappingId, Stack<Double>> getIdsAndValues(Integer overrideReplicateId) {
		TreeMap<DataMappingId, Stack<Double>> result = new TreeMap<DataMappingId, Stack<Double>>();
		for (NumericMeasurementInterface sd : getMappedSampleData()) {
			DataMappingId sid = sd.getParentSample().getFullId();
			
			DataMappingId fullId;
			if (overrideReplicateId != null)
				fullId = sid.getFullDataMappingIdForReplicate(overrideReplicateId);
			else
				fullId = sid.getFullDataMappingIdForReplicate(sd.getReplicateID());
			Double value = sd.getValue();
			if (!result.containsKey(fullId))
				result.put(fullId, new Stack<Double>());
			result.get(fullId).add(value);
		}
		return result;
	}
	
	public TreeMap<DataMappingId, Stack<Double>> getIdsAndAverageValues() {
		TreeMap<DataMappingId, Stack<Double>> result = new TreeMap<DataMappingId, Stack<Double>>();
		for (SampleAverageInterface sd : getMappedAverageSampleData()) {
			DataMappingId sid = sd.getParentSample().getFullId();
			
			DataMappingId fullId;
			fullId = sid.getFullDataMappingIdForReplicate(-1);
			Double value = sd.getValue();
			if (!result.containsKey(fullId))
				result.put(fullId, new Stack<Double>());
			result.get(fullId).add(value);
		}
		return result;
	}
	
	public void setLabelFontSize(int size, boolean wordWrap) {
		LabelAttribute la = AttributeHelper.getLabel(-1, n);
		if (la != null) {
			la.setFontSize(size);
			if (wordWrap)
				la.wordWrap();
		}
	}
	
	/**
	 * Set Label Position relative to the node.
	 * 
	 * @param index
	 *           Use "-1" to set position of main label, use values 0 to 99 to
	 *           set annotation label positions.
	 * @param align
	 *           The node label alignment setting.
	 */
	public void setLabelAlignment(int index, AlignmentSetting align) {
		AttributeHelper.setLabelAlignment(index, n, align);
	}
	
	public void labelWordWrap() {
		LabelAttribute la = AttributeHelper.getLabel(-1, n);
		if (la != null) {
			la.wordWrap();
		}
	}
	
	public int removeAdditionalDataMappingIDs() {
		int workCnt = 0;
		for (SubstanceInterface md : getDataMappings()) {
			workCnt += md.clearSynonyms();
		}
		return workCnt;
	}
	
	public String getShape() {
		return AttributeHelper.getShape(this);
	}
	
	public String getLabel(boolean htmlEncoded) {
		if (!htmlEncoded)
			return getLabel();
		else
			return StringManipulationTools.UnicodeToHtml(getLabel());
	}
	
	public int compareTo(GraphElement o) {
		return getGraphNode().compareTo(o);
	}
	
	/**
	 * @return List of leaf-nodes, reachable from this node.
	 */
	public ArrayList<NodeHelper> getReachableLeafNodes() {
		ArrayList<Node> src = new ArrayList<Node>();
		src.add(getGraphNode());
		ArrayList<NodeHelper> result = new ArrayList<NodeHelper>();
		for (Node n : GraphHelper.getLeafNodes(src)) {
			result.add(new NodeHelper(n));
		}
		return result;
	}
}
