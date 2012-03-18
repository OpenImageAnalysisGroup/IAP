/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.07.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.ErrorMsg;
import org.HelperClass;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xpath.internal.objects.XBoolean;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
@SuppressWarnings("unchecked")
public class XPathHelper implements HelperClass {
	
	public static String noGivenTimeStringConstant = "- not specified -";
	
	public static int getSampleCountForSubstanceNode(Node n) {
		XPath xpath;
		try {
			xpath = new DOMXPath("count(line/sample)");
			return xpath.numberValueOf(n).intValue();
		} catch (JaxenException e) {
			ErrorMsg.addErrorMessage(e);
			return 0;
		}
	}
	
	public static int getLineCountForSubstanceNode(Node n) {
		XPath xpath;
		try {
			xpath = new DOMXPath("count(line[count(sample)>0])");
			return xpath.numberValueOf(n).intValue();
		} catch (JaxenException e) {
			ErrorMsg.addErrorMessage(e);
			return 0;
		}
	}
	
	public static int getTimeCountForSubstanceNode(Node nn) {
		try {
			ArrayList resultList = new ArrayList();
			HashSet<String> memTest = new HashSet<String>();
			XPath xpath = new DOMXPath("//substance/line/sample"); // /substance/line/sample
			XPath xpathTimeValue = new DOMXPath("@time");
			XPath xpathTimeUnit = new DOMXPath("@unit");
			ArrayList results = (ArrayList) xpath.selectNodes(nn);
			for (Iterator it = results.iterator(); it.hasNext();) {
				Node n = (Node) it.next();
				String time = xpathTimeValue.stringValueOf(n);
				String unit = xpathTimeUnit.stringValueOf(n);
				TimeAndTimeUnit tatu = new TimeAndTimeUnit(time, unit);
				if (!tatu.toString().equals(" "))
					if (!memTest.contains(tatu.toString())) {
						resultList.add(tatu);
						memTest.add(tatu.toString());
					}
			}
			return resultList.size();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return 0;
		}
	}
	
	/**
	 * Get the possible time values that are stored in an experiment.
	 * 
	 * @param experimentData
	 * @return
	 */
	public static String[] getTimes(Document experimentData) {
		ArrayList resultList = new ArrayList();
		HashSet<String> memTest = new HashSet<String>();
		// resultList.add(filterAll);
		try {
			XPath xpath = new DOMXPath("/experimentdata/measurements/substance/line/sample"); // /substance/line/sample
			XPath xpathTimeValue = new DOMXPath("@time");
			XPath xpathTimeUnit = new DOMXPath("@unit");
			ArrayList results = (ArrayList) xpath.selectNodes(experimentData);
			for (Iterator it = results.iterator(); it.hasNext();) {
				Node n = (Node) it.next();
				String time = xpathTimeValue.stringValueOf(n);
				String unit = xpathTimeUnit.stringValueOf(n);
				TimeAndTimeUnit tatu = new TimeAndTimeUnit(time, unit);
				if (!tatu.toString().equals(" "))
					if (!memTest.contains(tatu.toString())) {
						resultList.add(tatu);
						memTest.add(tatu.toString());
					}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return null;
		}
		Collections.sort(resultList, new Comparator() {
			public int compare(Object o1, Object o2) {
				if ((o1 instanceof String) || (o2 instanceof String)) {
					return o1.toString().compareTo(o2.toString());
				} else {
					TimeAndTimeUnit tatu1 = (TimeAndTimeUnit) o1;
					TimeAndTimeUnit tatu2 = (TimeAndTimeUnit) o2;
					if (tatu1.getTimeUnit().equals(tatu2.getTimeUnit()))
						return tatu1.getTime().compareTo(tatu2.getTime());
					else
						return tatu1.getTimeUnit().compareTo(tatu2.getTimeUnit());
				}
			}
		});
		ArrayList<String> string_resultList = new ArrayList<String>();
		HashSet<String> string_resultHS = new HashSet<String>();
		for (Object o : resultList) {
			if (!string_resultHS.contains(o.toString())) {
				string_resultList.add(o.toString());
				string_resultHS.add(o.toString());
			}
		}
		String[] res = (String[]) string_resultList.toArray(new String[] {});
		return res;
	}
	
	/**
	 * Get a list of analyzed plants for the experiment.
	 * 
	 * @param experimentData
	 * @return
	 */
	public static String[] getPlants(Document experimentData) {
		ArrayList<String> resultList = new ArrayList<String>();
		// resultList.add(filterAll);
		final HashMap<String, String> lineName2lineID = new HashMap<String, String>();
		try {
			XPath xpath = new DOMXPath("/experimentdata/measurements/substance/line"); // /substance/line/sample
			XPath xpathID = new DOMXPath("@id");
			XPath xpathName = new DOMXPath("@name");
			XPath xpathGenoType = new DOMXPath("@genotype");
			XPath xpathTreatment = new DOMXPath("@treatment");
			ArrayList results = (ArrayList) xpath.selectNodes(experimentData);
			for (Iterator it = results.iterator(); it.hasNext();) {
				Node n = (Node) it.next();
				String lineID = xpathID.stringValueOf(n);
				String lineName = xpathName.stringValueOf(n);
				String genotype = xpathGenoType.stringValueOf(n);
				String treatment = xpathTreatment.stringValueOf(n);
				String line = lineName + getGenotypeAndTreatment(genotype, treatment) + " id=" + lineID;
				if (!resultList.contains(line)) {
					resultList.add(line);
					while (lineID.length() < 20)
						lineID = "0" + lineID;
					lineName2lineID.put(line, lineID);
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return null;
		}
		String[] res = (String[]) resultList.toArray(new String[] {});
		Arrays.sort(res, new Comparator<String>() {
			public int compare(String arg0, String arg1) {
				String id1 = lineName2lineID.get(arg0);
				String id2 = lineName2lineID.get(arg1);
				if (id1 == null || id2 == null)
					return 0;
				else
					return id1.compareTo(id2);
			}
		});
		return res;
	}
	
	/**
	 * @param genotype
	 * @param treatment
	 * @return
	 */
	private static String getGenotypeAndTreatment(String genotype,
						String treatment) {
		StringBuilder res = new StringBuilder();
		if (genotype != null && genotype.length() > 0)
			res.append(genotype);
		if (treatment != null && treatment.length() > 0) {
			if (res.length() > 0)
				res.append("/");
			res.append(treatment);
		}
		if (res.length() > 0)
			return " (" + res.toString() + ")";
		else
			return "";
	}
	
	public static ArrayList<Node> getPlantNodes(Document experimentData) {
		try {
			XPath xpath = new DOMXPath("/experimentdata/measurements/substance/line"); // /substance/line/sample
			ArrayList results = (ArrayList) xpath.selectNodes(experimentData);
			return results;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	public static ArrayList<Node> getSubstanceNodes(Document document) {
		try {
			XPath xpathAllSubstances;
			xpathAllSubstances = new DOMXPath("/experimentdata/measurements/substance");
			ArrayList resultAllSubstances = (ArrayList) xpathAllSubstances.selectNodes(document);
			if (resultAllSubstances.size() <= 0) {
				xpathAllSubstances = new DOMXPath("substance");
				resultAllSubstances = (ArrayList) xpathAllSubstances.selectNodes(document);
			}
			return resultAllSubstances;
		} catch (JaxenException e) {
			ErrorMsg.addErrorMessage(e);
			return new ArrayList();
		}
	}
	
	public static Node getChildNode(Node node, String childNodeName) {
		XPath workXpath;
		try {
			workXpath = new DOMXPath(childNodeName);
			ArrayList resultNodeList = (ArrayList) workXpath.selectNodes(node);
			return (Node) resultNodeList.get(0);
		} catch (JaxenException e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			return null;
		}
	}
	
	public static String getSumOfValues(Document mydoc) {
		String res = "";
		try {
			NodeList nl = mydoc.getElementsByTagName("data");
			double sum = 0;
			int errCnt = 0;
			for (int i = 0; i < nl.getLength(); i++) {
				Node matchSample = nl.item(i);
				try {
					String val = matchSample.getFirstChild().getNodeValue();
					try {
						double vd = Double.parseDouble(val);
						sum += vd;
					} catch (Exception nfe) {
						errCnt++;
					}
				} catch (Exception ee) {
					ErrorMsg.addErrorMessage(ee);
					return "- Error 1 -";
				} catch (AbstractMethodError e) {
					ErrorMsg.addErrorMessage(e.toString());
					return "- Error 2 -";
				}
			}
			res = new Double(sum).toString();
			if (errCnt > 0) {
				res = res + " (" + errCnt + " errors)";
			}
			return res;
		} catch (Exception err) {
			ErrorMsg.addErrorMessage(err);
			return "- Error 0 -";
		}
	}
	
	public static void setTtestInfoSampleIsReference(org.w3c.dom.Node sampleNode) {
		Attr attr = sampleNode.getOwnerDocument().createAttribute("t-test");
		attr.setNodeValue("reference");
		Element e = (Element) sampleNode;
		e.setAttributeNode(attr);
	}
	
	public static Double[] getDataList(Node sampleNode) {
		if (sampleNode == null)
			return new Double[0];
		List<Double> measureValues = new ArrayList<Double>();
		Node n = sampleNode.getFirstChild();
		while (n != null) {
			if (n.getNodeName().equalsIgnoreCase("data")) {
				String ms = n.getFirstChild().getNodeValue();
				double mesVal = Double.parseDouble(ms);
				measureValues.add(new Double(mesVal));
			}
			n = n.getNextSibling();
		}
		return measureValues.toArray(new Double[0]);
	}
	
	public static int getSampleTimeValueForComparison(Node sampleNode) {
		String time;
		if (sampleNode.getAttributes() == null
							|| sampleNode.getAttributes().getNamedItem("time") == null)
			time = "-1";
		else
			time = sampleNode.getAttributes().getNamedItem("time").getFirstChild()
								.getNodeValue();
		int result;
		try {
			result = Integer.parseInt(time);
		} catch (NumberFormatException nfe) {
			ErrorMsg.addErrorMessage("Sample time \"" + time + "\" not in integer format!");
			result = -1;
		}
		return result;
	}
	
	/**
	 * @param sampleNode
	 * @return The time and time unit from a sample. e.g. "day 1" or "week 2".
	 *         Returns "" if no time information is avalilable.
	 */
	public static String getSampleTime(Node sampleNode) {
		String time;
		if (sampleNode.getAttributes() == null
							|| sampleNode.getAttributes().getNamedItem("time") == null)
			time = "-1";
		else
			time = sampleNode.getAttributes().getNamedItem("time").getFirstChild()
								.getNodeValue();
		String timeunit;
		if (sampleNode.getAttributes() == null
							|| sampleNode.getAttributes().getNamedItem("unit") == null)
			timeunit = "-1";
		else
			timeunit = sampleNode.getAttributes().getNamedItem("unit")
								.getFirstChild().getNodeValue();
		
		String timeUnitAndTime = timeunit + " " + time; // +" ("+unit+")";
		if (time.equals("-1") && timeunit.equals("-1"))
			timeUnitAndTime = ""; // "("+unit+")";
			
		return timeUnitAndTime;
	}
	
	/**
	 * @param sampleNode
	 * @return The time and time unit from a sample. e.g. "day 1" or "week 2".
	 *         Returns "" if no time information is avalilable.
	 */
	public static String getSampleTimeUnit(Node sampleNode) {
		String timeunit;
		if (sampleNode.getAttributes() == null
							|| sampleNode.getAttributes().getNamedItem("unit") == null)
			timeunit = null;
		else
			timeunit = sampleNode.getAttributes().getNamedItem("unit")
								.getFirstChild().getNodeValue();
		
		return timeunit;
	}
	
	/**
	 * @param sampleNode
	 */
	public static void tTestSetSampleAsReference(Node sampleNode) {
		Attr attr = sampleNode.getOwnerDocument().createAttribute("ttest");
		attr.setNodeValue("reference");
		Element e = (Element) sampleNode;
		e.setAttributeNode(attr);
	}
	
	public static void tTestSetSampleSignificane(Node sampleNode,
						boolean different) {
		Attr attr = sampleNode.getOwnerDocument().createAttribute("ttest");
		if (different)
			attr.setNodeValue("H1");
		else
			attr.setNodeValue("H0");
		// System.out.println(attr.getNodeValue());
		Element e = (Element) sampleNode;
		e.setAttributeNode(attr);
		e.removeAttribute("ttest-level");
	}
	
	public static void setTTestSetSampleSignificane(Node sampleNode,
						boolean different, double level) {
		Attr attr = sampleNode.getOwnerDocument().createAttribute("ttest");
		Attr attrLevel = sampleNode.getOwnerDocument().createAttribute("ttest-level");
		if (different)
			attr.setNodeValue("H1");
		else
			attr.setNodeValue("H0");
		// System.out.println(attr.getNodeValue());
		attrLevel.setNodeValue(level + "");
		Element e = (Element) sampleNode;
		e.setAttributeNode(attr);
		e.setAttributeNode(attrLevel);
	}
	
	/**
	 * @param samplenode
	 * @return
	 */
	public static boolean tTestIsReference(Node sampleNode) {
		if (sampleNode.getAttributes() == null
							|| sampleNode.getAttributes().getNamedItem("ttest") == null)
			return true;
		else
			return sampleNode.getAttributes().getNamedItem("ttest")
								.getFirstChild().getNodeValue().equals("reference");
	}
	
	/**
	 * @param linenode
	 * @return
	 */
	public static String getSeriesNameForLine(org.w3c.dom.Node linenode) {
		try {
			String linename = linenode.getAttributes().getNamedItem("name").getFirstChild().getNodeValue();
			String linegenotype = linenode.getAttributes().getNamedItem("genotype").getFirstChild().getNodeValue();
			String linetreatment;
			try {
				linetreatment = linenode.getAttributes().getNamedItem("treatment").getFirstChild().getNodeValue();
			} catch (NullPointerException npe) {
				linetreatment = null;
			}
			return getSeriesNameFromSpeciesGenotypeAndTreatment(linename, linegenotype, linetreatment);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return "Error: Series Name Unknown";
		}
	}
	
	public static String getSeriesNameFromSpeciesGenotypeAndTreatment(String linename, String linegenotype, String linetreatment) {
		String serie;
		// DO NOT CHANGE THE NAMING SYSTEM
		// IT IS PROCESSED BY CLASS SeriesData and at least XML Helper !
		if (linetreatment != null && linetreatment.length() > 0 && !linetreatment.equalsIgnoreCase("null")
							&& !checkForSameGenoTypeAndTreatment(linegenotype, linetreatment))
			serie = linename + (linegenotype.length() > 0 ? "/" + linegenotype : "") + " (" + linetreatment + ")";
		else
			serie = linename + (linegenotype.length() > 0 ? "/" + linegenotype : "");
		return serie;
	}
	
	private static boolean checkForSameGenoTypeAndTreatment(String linegenotype, String linetreatment) {
		if (linegenotype == null || linetreatment == null)
			return false;
		if (linegenotype.equalsIgnoreCase(linetreatment))
			return true;
		else
			return false;
	}
	
	/**
	 * @param samplenode
	 * @return
	 */
	public static boolean ttestIsH1(Node sampleNode) {
		if (sampleNode.getAttributes() == null
							|| sampleNode.getAttributes().getNamedItem("ttest") == null)
			return true;
		else
			return sampleNode.getAttributes().getNamedItem("ttest")
								.getFirstChild().getNodeValue().equals("H1");
	}
	
	public static String getExpAndSeriesName(String expName, String seriesName) {
		return expName + ": " + seriesName;
	}
	
	// public static String getExpAndSeriesNameFromSample(
	// org.w3c.dom.Node sampleNode) {
	// org.w3c.dom.Node lineNode = sampleNode.getParentNode();
	// if (lineNode.getNodeName().equals("line")) {
	// return getExpAndSeriesNameFromLine(lineNode);
	// } else
	// return null;
	// }
	
	// public static String getExpAndSeriesNameFromLine(org.w3c.dom.Node lineNode) {
	// String expName = getExperimentNameFromLineNode(lineNode);
	// return getExpAndSeriesName(expName, getSeriesNameForLine(lineNode));
	// }
	
	// public static Node getFirstMatchingSampleFromTimeAndLine(List samplesInNode,
	// String compareTime, String seriesNameFromSample) {
	// Node result = null;
	// for (Iterator it = samplesInNode.iterator(); it.hasNext();) {
	// Node n = (Node) it.next();
	// if (n.getNodeName().equalsIgnoreCase("sample")) {
	// String time = getSampleTime(n);
	// if (time.equals(compareTime)
	// && seriesNameFromSample
	// .equals(getExpAndSeriesNameFromSample(n))) {
	// result = n;
	// break;
	// }
	// } else
	// ErrorMsg.addErrorMessage("Expected Sample Node, but got different data set: "+ n.getNodeName());
	// }
	// return result;
	// }
	
	public static String getExperimentNameFromLineNode(Node linenode) {
		try {
			Node t1 = linenode.getAttributes().getNamedItem("experimentname");
			if (t1 != null)
				return t1.getNodeValue();
			else
				return linenode.getOwnerDocument().getElementsByTagName(
									"experimentname").item(0).getFirstChild().getNodeValue();
		} catch (NullPointerException npe) {
			return "";
		}
	}
	
	public static String getCoordinatorFromLineNode(Node linenode) {
		Node t1 = linenode.getAttributes().getNamedItem("coordinator");
		if (t1 != null)
			return t1.getNodeValue();
		else
			return linenode.getOwnerDocument().getElementsByTagName(
								"coordinator").item(0).getFirstChild().getNodeValue();
	}
	
	public static String getStartDateFromLineNode(Node linenode) {
		String date = "";
		Node t1 = linenode.getAttributes().getNamedItem("startdate");
		if (t1 != null)
			date = t1.getNodeValue();
		else
			date = linenode.getOwnerDocument().getElementsByTagName(
								"startdate").item(0).getFirstChild().getNodeValue();
		return date;
	}
	
	// public static String getFuncatDescFromLineNode(Node linenode) {
	// Node t1 = linenode.getAttributes().getNamedItem("funcat");
	// if (t1 != null)
	// return t1.getNodeValue();
	// else
	// return null;
	// }
	
	public static String getSeriesIDforLine(Node linenode) {
		try {
			String lineid = linenode.getAttributes().getNamedItem("id").getFirstChild().getNodeValue();
			return lineid;
		} catch (Exception eee) {
			return null;
		}
	}
	
	/**
	 * (1) search substance names which end with <code>substanceStdDevEndID</code> (2) for each substance get all experiment/genotype/line/time combinations
	 * enclosed
	 * extract average sample values
	 * (3) use average sample values as sample stddev for corresponding substance/sample data
	 * whose substance name is the same as the one mentioned above, but without the trailing <code>substanceStdDevEndID</code> id.
	 * 
	 * @param mydoc
	 * @param substanceStdDevEndID
	 * @return Number of processed "std-dev substances"
	 */
	public static int processAvailableStdDevSubstanceData(Document mydoc, String substanceStdDevEndID) {
		substanceStdDevEndID = substanceStdDevEndID.toUpperCase();
		int transformed = 0;
		String xpath = "//substance";
		HashMap<String, Node> substName2substNode = new HashMap<String, Node>();
		ArrayList<String> toBeDeleted = new ArrayList<String>();
		try {
			NodeIterator substanceNodes = XPathAPI.selectNodeIterator(mydoc, xpath);
			Node substance;
			while ((substance = substanceNodes.nextNode()) != null) {
				String substanceName = substance.getAttributes().getNamedItem("name").getNodeValue();
				if (substName2substNode.containsKey(substanceName.toUpperCase())) {
					ErrorMsg.addErrorMessage("Substance named " + substanceName + " seems to be more than one time defined in the dataset!");
				} else
					substName2substNode.put(substanceName.toUpperCase(), substance);
			}
			for (String substName : substName2substNode.keySet()) {
				if (substName.endsWith(substanceStdDevEndID)) {
					String correspondingSubstanceName = substName.substring(0, substName.length() - substanceStdDevEndID.length());
					if (substName2substNode.containsKey(correspondingSubstanceName)) {
						int transformedForThisSubstance = 0;
						
						HashMap<String, Node> samplePath2avgNode_stdDevData = getSamplePathAndAvgValues(substName2substNode.get(substName));
						HashMap<String, Node> samplePath2avgNode_actualDataWithoutStdDev = getSamplePathAndAvgValues(substName2substNode
											.get(correspondingSubstanceName));
						// samplePath = //id of plant/name of plant/genotype of plant/timepoint and timeunit
						for (String samplePath : samplePath2avgNode_stdDevData.keySet()) {
							if (samplePath2avgNode_actualDataWithoutStdDev.containsKey(samplePath)) {
								Node stdDevAvgData = samplePath2avgNode_stdDevData.get(samplePath);
								Node actualDataWithoutStdDev = samplePath2avgNode_actualDataWithoutStdDev.get(samplePath);
								// avg value --> std dev value
								String avgValue = stdDevAvgData.getFirstChild().getNodeValue();
								actualDataWithoutStdDev.getAttributes().getNamedItem("stddev").setNodeValue(avgValue);
								transformed++;
								transformedForThisSubstance++;
							}
						}
						
						if (transformedForThisSubstance > 0)
							toBeDeleted.add(substName);
					}
				}
			}
			for (String deleteSubstance : toBeDeleted) {
				Node n = substName2substNode.get(deleteSubstance);
				n.getParentNode().removeChild(n);
			}
		} catch (TransformerException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return transformed;
	}
	
	private static HashMap<String, Node> getSamplePathAndAvgValues(Node substanceNode) throws TransformerException {
		HashMap<String, Node> result = new HashMap<String, Node>();
		String xpath = "line/sample";
		NodeIterator sampleNodes = XPathAPI.selectNodeIterator(substanceNode, xpath);
		Node sample;
		while ((sample = sampleNodes.nextNode()) != null) {
			String sampleTime = sample.getAttributes().getNamedItem("time").getNodeValue();
			String sampleTimeUnit = sample.getAttributes().getNamedItem("unit").getNodeValue();
			String lineSpeciesID = sample.getParentNode().getAttributes().getNamedItem("id").getNodeValue();
			String lineSpeciesName = sample.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
			String lineSpeciesGenotype = sample.getParentNode().getAttributes().getNamedItem("genotype").getNodeValue();
			// search average child node
			Node avgOrData = sample.getFirstChild();
			while (avgOrData != null) {
				if (avgOrData.getNodeName().equalsIgnoreCase("average")) {
					String path = "//" + lineSpeciesID + "/" + lineSpeciesName + "/" + lineSpeciesGenotype + "/" + sampleTime + " " + sampleTimeUnit;
					path = path.toUpperCase();
					if (result.containsKey(path))
						ErrorMsg.addErrorMessage("Duplicate measurement data found for species/genotype/sample time combination: " + path);
					else
						result.put(path, avgOrData);
				}
				avgOrData = avgOrData.getNextSibling();
			}
		}
		return result;
	}
	
	public static boolean isReplicateDataMissing(Document mydoc) {
		//
		String xpath = "count(//substance/line/sample/average[@replicates > 1])<=0";
		Object result;
		try {
			result = XPathAPI.eval(mydoc, xpath);
		} catch (TransformerException e) {
			ErrorMsg.addErrorMessage(e);
			return false;
		}
		return ((XBoolean) result).bool();
	}
	
	// public static ArrayList<Node> getSampleNodes(Document doc) {
	// try {
	// XPath xpath=new DOMXPath("/experimentdata/measurements/substance/line/sample");
	// ArrayList results = (ArrayList) xpath.selectNodes(doc);
	// return results;
	// } catch (Exception e) {
	// ErrorMsg.addErrorMessage("XPATH ERROR: "+e.getLocalizedMessage());
	// return null;
	// }
	// }
	
	public static ArrayList<Node> getSampleNodes(Document doc, Integer plantID, String substanceID) {
		try {
			XPath xpath = new DOMXPath("/experimentdata/measurements/substance[@id=" + substanceID + "]/line[@id=" + plantID + "]/sample");
			ArrayList results = (ArrayList) xpath.selectNodes(doc);
			return results;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	public static ArrayList<Node> getDataNodes(Document doc, Integer sampleID) {
		try {
			XPath xpath = new DOMXPath("/experimentdata/measurements/substance/line/sample[@id=" + sampleID + "]/data");
			ArrayList results = (ArrayList) xpath.selectNodes(doc);
			return results;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	public static ArrayList<Node> getDataVolumeNodes(Document doc, Integer sampleID) {
		try {
			XPath xpath = new DOMXPath("/experimentdata/measurements/substance/line/sample[@id=" + sampleID + "]/volume");
			ArrayList results = (ArrayList) xpath.selectNodes(doc);
			return results;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	public static ArrayList<Node> getDataNetworkNodes(Document doc, Integer sampleID) {
		try {
			XPath xpath = new DOMXPath("/experimentdata/measurements/substance/line/sample[@id=" + sampleID + "]/network");
			ArrayList results = (ArrayList) xpath.selectNodes(doc);
			return results;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	public static ArrayList<Node> getDataImageNodes(Document doc, Integer sampleID) {
		try {
			XPath xpath = new DOMXPath("/experimentdata/measurements/substance/line/sample[@id=" + sampleID + "]/image");
			ArrayList results = (ArrayList) xpath.selectNodes(doc);
			return results;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("XPATH ERROR: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	/**
	 * Returns the highest index number of a alternative identifier. Processes all attributes and looks for
	 * attribute names that start with "name".
	 * 
	 * @param xmlSubstanceNode
	 * @return The highest index of a alternative identifier attribute. Returns -1 in case no alternative identifier is
	 *         currently assigned.
	 */
	public static int getMaximumAlternativeIDidx(Node xmlSubstanceNode) {
		int maxId = -1;
		int maxID = xmlSubstanceNode.getAttributes().getLength();
		for (int i = 0; i < maxID; i++) {
			Attr oAlternative = (Attr) xmlSubstanceNode.getAttributes().getNamedItem("name" + i);
			if (oAlternative != null) {
				maxId = i;
			}
		}
		return maxId;
	}
	
	public static int getAlternativeIdCount(Node xmlSubstanceNode, boolean includeEmpty) {
		int idCnt = 0;
		int maxID = xmlSubstanceNode.getAttributes().getLength();
		for (int i = 1; i < maxID; i++) {
			Attr oAlternative = (Attr) xmlSubstanceNode.getAttributes().getNamedItem("name" + i);
			if (oAlternative != null) {
				String value = oAlternative.getNodeValue();
				if (includeEmpty || (value != null && value.length() > 0))
					idCnt++;
			}
		}
		return idCnt;
	}
	
	public static ArrayList<String> getAlternativeIDs(Node xmlSubstanceNode) {
		ArrayList<String> result = new ArrayList<String>();
		int maxID = xmlSubstanceNode.getAttributes().getLength();
		for (int i = 0; i < maxID; i++) {
			Attr oAlternative = (Attr) xmlSubstanceNode.getAttributes().getNamedItem("name" + i);
			if (oAlternative != null) {
				result.add(oAlternative.getNodeValue());
			}
		}
		return result;
	}
	
}
