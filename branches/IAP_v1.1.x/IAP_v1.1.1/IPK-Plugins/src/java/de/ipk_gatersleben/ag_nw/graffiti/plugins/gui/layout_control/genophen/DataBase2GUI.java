/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 10.03.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.genophen;

/**
 * @author klukas
 *         To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DataBase2GUI {

	// public static void processDBEnodeData(final String experimentName, final List nodes,
	// final ActionEvent arg0) {
	// Thread r = new Thread(new Runnable() {
	// public void run() {
	// JButton startButton=(JButton) arg0.getSource();
	// String oldText=startButton.getText();
	// startButton.setEnabled(false);
	// startButton.setText("Processing experimental data...");
	// try {
	// DataBase2GUI.threadProcessDBEnodeData(experimentName, nodes, startButton);
	// } catch (Exception e) {
	// // empty
	// }
	// startButton.setText(oldText);
	// startButton.setEnabled(true);
	// }
	// });
	// r.setPriority(Thread.MIN_PRIORITY);
	// r.start();
	// }
	//
	// static void threadProcessDBEnodeData(String experimentName, List nodes, JButton callButton) {
	// for (int i = 0; i < nodes.size(); i++) {
	//
	// PluginStatusInfo.setCurrentMessage("Process experimental data for node "+i);
	// PluginStatusInfo.setCurrentProgress(i*100/nodes.size());
	// callButton.setText("Processing experimental data: "+i*100/nodes.size()+"%");
	//
	// Node currentNode = (Node)nodes.get(i);
	//
	// String name = AttributeHelper.getLabel(currentNode, "");
	//
	// MainFrame.showMessage("Processing Data for Node \""+name+"\". Complete: "+i*100/nodes.size()+"%",
	// MessageType.PERMANENT_INFO);
	//
	// String substanceName =
	// AttributeHelper.getLabel(currentNode, "");
	//
	// // src = sum replicate count for that substance
	// int src = DataExchange.getDataCount(experimentName, substanceName);
	// AttributeHelper.setAttribute(currentNode,"dbe","entryCount",new Integer(src));
	//
	// if (src<=0) continue;
	//
	// String compoundID =
	// DataExchange.getCompoundIDfromSubstrateName(
	// substanceName);
	//
	// int minPlantID =
	// DataExchange.getMinPlantID(experimentName);
	// int maxPlantID =
	// DataExchange.getMaxPlantID(experimentName);
	// /* System.err.println(
	// "Node: " + new Integer(i + 1).toString()); */
	// processPlantDataForAllMeasurements(currentNode, compoundID, minPlantID, maxPlantID);
	// if (minPlantID!=Integer.MAX_VALUE && maxPlantID!=Integer.MAX_VALUE && src>0)
	// for (int plantID = minPlantID;
	// plantID <= maxPlantID;
	// plantID++) {
	// processPlantData(experimentName, currentNode, substanceName, plantID);
	// }
	// }
	// MainFrame.showMessage("DBE Data - Processing complete",
	// MessageType.INFO);
	// }
	
	// private static void processPlantDataForAllMeasurements(Node currentNode, String compoundID, int minPlantID, int maxPlantID) {
	// try {
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "formula",
	// DataExchange.getSubstrateFormula(compoundID));
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "compoundID",
	// compoundID);
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "minID",
	// new Integer(minPlantID));
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "maxID",
	// new Integer(maxPlantID));
	// } catch (Exception e) {
	// ErrorMsg.addErrorMessage("Could not set data attributes to graph node.<br>"+e.getLocalizedMessage());
	// }
	// }
	//
	// private static void processPlantData(String experimentName, Node currentNode, String substanceName, int plantID) {
	// try {
	// String curID = new Integer(plantID).toString();
	//
	// int i = DataExchange.getReplCount(
	// experimentName,
	// plantID,
	// substanceName);
	// if (i==Integer.MAX_VALUE) return;
	// System.out.println("Found something: "+substanceName+"/"+plantID);
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "replCount"+curID,
	// new Integer(i));
	//
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "avgMeasurement"+curID,
	// new Double(
	// DataExchange.getAverageMeasurement(
	// experimentName,
	// plantID,
	// substanceName)));
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "stdDev"+curID,
	// new Double(
	// DataExchange.getStdDev(
	// experimentName,
	// plantID,
	// substanceName)));
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "variance"+curID,
	// new Double(
	// DataExchange.getVariance(
	// experimentName,
	// plantID,
	// substanceName)));
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "min"+curID,
	// new Double(
	// DataExchange.getMin(
	// experimentName,
	// plantID,
	// substanceName)));
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "max"+curID,
	// new Double(
	// DataExchange.getMax(
	// experimentName,
	// plantID,
	// substanceName)));
	// AttributeHelper.setAttribute(
	// currentNode,
	// "dbe",
	// "measUnit"+curID,
	// DataExchange.getMeasUnit(
	// experimentName,
	// plantID,
	// substanceName));
	// } catch (Exception e) {
	// System.err.println(e.getLocalizedMessage());
	// }
	// }
	
}
