/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.HelperClass;
import org.StringManipulationTools;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_brite.BriteService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.IndexAndString;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class DatabaseBasedLabelReplacementService implements Runnable,
		BackgroundTaskStatusProvider, HelperClass {
	
	int statusVal = -1;
	String status1 = "";
	String status2 = "";
	boolean pleaseStop = false;
	Collection<Node> nodes;
	private final boolean ecNumberToName;
	private final boolean ecNameOrSynonymeToECnumber;
	private final boolean compIDtoName;
	private final boolean compNameToID;
	private final boolean increaseNodeSize;
	private final boolean useShortName;
	private final boolean preserveOldId;
	private final String oldIdIdentifier;
	private final boolean reactionIdToEcName;
	private final boolean reactionNameToId;
	private final boolean useGreekName;
	private final boolean processKeggId2EcAnnotation;
	private final boolean koId2koName;
	private final boolean briteKO2geneName;
	private final boolean briteKO2ecName;
	
	public DatabaseBasedLabelReplacementService(
			Collection<Node> nodes,
			boolean compoundNameToID,
			boolean compoundIDtoName,
			boolean ecNumberToName,
			boolean ecNameOrSynonymeToECnumber,
			boolean reactionNameToEcId,
			boolean reactionNameToNo,
			boolean processKeggId2EcAnnotaion,
			boolean koId2koName,
			boolean briteKO2geneName,
			boolean briteKO2ecName,
			boolean increaseNodeSize,
			boolean useShortName,
			boolean preserveOldId,
			boolean useGreekName, // for compounds
			String oldIdIdentifier) {
		this.nodes = nodes;
		this.ecNumberToName = ecNumberToName;
		this.ecNameOrSynonymeToECnumber = ecNameOrSynonymeToECnumber;
		this.compIDtoName = compoundIDtoName;
		this.compNameToID = compoundNameToID;
		this.reactionIdToEcName = reactionNameToEcId;
		this.reactionNameToId = reactionNameToNo;
		this.briteKO2geneName = briteKO2geneName;
		this.briteKO2ecName = briteKO2ecName;
		this.increaseNodeSize = increaseNodeSize;
		this.useShortName = useShortName;
		this.preserveOldId = preserveOldId;
		this.useGreekName = useGreekName;
		this.oldIdIdentifier = oldIdIdentifier;
		this.processKeggId2EcAnnotation = processKeggId2EcAnnotaion;
		this.koId2koName = koId2koName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		statusVal = 0;
		status1 = "Rename nodes...";
		status2 = "";
		int workLoad = nodes.size();
		int cnt = 0;
		int renameCount = 0;
		
		if (nodes == null || nodes.size() <= 0)
			return;
		
		Graph graph = nodes.iterator().next().getGraph();
		
		graph.getListenerManager().transactionStarted(this);
		
		try {
			
			for (Node n : nodes) {
				double border;
				cnt++;
				statusVal = (int) (100d * cnt / workLoad);
				
				String nodeLabel = AttributeHelper.getLabel(n, null);
				// String substanceName = AttributeHelper.getSubstanceName(n, null);
				String workName = nodeLabel;
				if (workName == null)
					continue;
				String targetName = null;
				EnzymeEntry ee = EnzymeService.getEnzymeInformation(workName, false);
				if (ee != null) {
					if (ecNumberToName) {
						targetName = ee.getDE();
						if (useShortName) {
							for (String syn : ee.getAN()) {
								if (syn.length() < targetName.length())
									targetName = syn;
							}
						}
					}
					if (ecNameOrSynonymeToECnumber)
						targetName = ee.getID();
				} else {
					CompoundEntry ce = CompoundService.getInformation(workName);
					if (ce != null) {
						setCompoundAnnotation(n, ce);
						targetName = processCompoundInfo(n, targetName, ce);
					} else {
						TreeSet<String> names = new TreeSet<String>();
						if (KeggGmlHelper.getKeggId(n) != null) {
							for (String id : KeggGmlHelper.getKeggId(n).split(" ")) {
								id = id.trim();
								if (id.length() <= 0)
									continue;
								ce = CompoundService.getInformation(id);
								if (ce != null) {
									names.add(processCompoundInfo(n, targetName, ce));
								} else
									if (!CompoundService.isDatabaseAvailable() && id.startsWith("cpd:")) {
										String[] res = KeggHelper.get_compounds_by_ko(id.substring("cpd:".length()));
										if (res != null) {
											ce = new CompoundEntry(id, StringManipulationTools.getStringListFromArray(res));
											names.add(processCompoundInfo(n, targetName, ce));
										}
									}
							}
							if (names.size() == 1)
								setCompoundAnnotation(n, ce);
							String myTargetName = "";
							for (String ci : names) {
								if (myTargetName.length() > 0)
									myTargetName = myTargetName + ", " + ci;
								else
									myTargetName = ci;
							}
							if (myTargetName.length() > 0)
								targetName = myTargetName;
						}
					}
				}
				if (targetName == null)
					if (reactionNameToId || reactionIdToEcName) {
						String myTargetName = "";
						for (IndexAndString ias : KeggGmlHelper.getKeggReactions(n)) {
							try {
								String[] enz = KeggHelper.get_enzymes_by_reaction(ias.getValue());
								for (int i = 0; i < enz.length; i++) {
									if (myTargetName.length() > 0)
										myTargetName = myTargetName + ", " + enz[i];
									else
										myTargetName = enz[i];
								}
								if (myTargetName.length() > 0) {
									targetName = myTargetName;
									targetName = StringManipulationTools.stringReplace(targetName, "ec:", "");
								}
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						}
					}
				if (targetName == null)
					if (processKeggId2EcAnnotation) {
						String myTargetName = "";
						HashSet<KoEntry> koEntries = new HashSet<KoEntry>();
						if (KeggGmlHelper.getKeggId(n) != null)
							for (String id : KeggGmlHelper.getKeggId(n).split(" ")) {
								id = id.trim();
								if (id.length() <= 0)
									continue;
								KoEntry koe = KoService.getInformation(id);
								if (koe != null)
									koEntries.add(koe);
								if (koEntries.size() <= 0) {
									// check if id is a gene ID...
									if (id.indexOf(":") > 0) {
										String organismCode = id.substring(0, id.indexOf(":")).toUpperCase();
										String geneId = id.substring(organismCode.length() + 1);
										Collection<KoEntry> koes = KoService.getKoFromGeneId(organismCode, geneId);
										if (koes != null && koes.size() > 0)
											koEntries.addAll(koes);
									} else {
										Collection<KoEntry> koes = KoService.getKoFromGeneIdOrKO(id);
										if (koes != null && koes.size() > 0)
											koEntries.addAll(koes);
									}
								}
							}
						TreeSet<String> ecIds = new TreeSet<String>();
						for (KoEntry ke : koEntries) {
							Collection<String> ecAnnot = ke.getKoDbLinks("EC");
							for (String s : ecAnnot) {
								for (String ss : s.split(" "))
									if (ss.trim().length() > 0)
										ecIds.add(ss.trim());
							}
						}
						for (String ec : ecIds) {
							if (myTargetName.length() > 0)
								myTargetName = myTargetName + ", " + ec;
							else
								myTargetName = ec;
						}
						if (myTargetName.length() > 0) {
							targetName = myTargetName;
							targetName = StringManipulationTools.stringReplace(targetName, "ec:", "");
						}
					}
				if (targetName == null)
					if (koId2koName) {
						String myTargetName = "";
						HashSet<KoEntry> koEntries = new HashSet<KoEntry>();
						if (KeggGmlHelper.getKeggId(n) != null)
							for (String id : KeggGmlHelper.getKeggId(n).split(" ")) {
								id = id.trim();
								if (id.length() <= 0)
									continue;
								Collection<KoEntry> koes = KoService.getKoFromGeneIdOrKO(id);
								if (koes != null)
									for (KoEntry koe : koes)
										koEntries.add(koe);
							}
						TreeSet<String> koNames = new TreeSet<String>();
						for (KoEntry ke : koEntries) {
							HashSet<String> koNamesTemp = new HashSet<String>();
							String koName = ke.getKoName();
							for (String s : koName.split(",")) {
								s = s.trim();
								if (s.length() > 1 && s.startsWith("E") &&
										s.length() - StringManipulationTools.stringReplace(s, ".", "").length() == 3) {
									s = s.substring(1);
								}
								koNamesTemp.add(s);
								if (!useShortName)
									break;
							}
							if (useShortName && koNamesTemp.size() > 1) {
								String ss = null;
								for (String s : koNamesTemp) {
									if (ss == null)
										ss = s;
									if (ss.length() == 0 || (s.length() < ss.length() && s.length() > 0))
										ss = s;
								}
								koNames.add(ss);
							} else
								koNames.addAll(koNamesTemp);
						}
						String lastName = "";
						if (useShortName && koNames.size() > 2) {
							myTargetName = koNames.iterator().next() + "...";
						} else
							for (String ec : koNames) {
								if (lastName.length() > 0 && ec.startsWith(lastName)) {
									myTargetName = myTargetName + "/" + ec.substring(lastName.length());
									continue;
								}
								if (myTargetName.length() > 0)
									myTargetName = myTargetName + ", " + ec;
								else
									myTargetName = ec;
								if (ec.length() > 1)
									lastName = ec.substring(0, ec.length() - 1);
							}
						if (myTargetName.length() > 0) {
							targetName = myTargetName;
							targetName = StringManipulationTools.stringReplace(targetName, "ec:", "");
						}
					}
				
				if (targetName == null)
					if (briteKO2geneName || briteKO2ecName) {
						String title = new NodeHelper(n).getLabel();
						if (title.startsWith("K"))
							System.out.println();
						ArrayList<IndexAndString> keggIDs = KeggGmlHelper.getKeggIds(n);
						String kkid = KeggGmlHelper.getKeggId(n);
						if (kkid != null)
							keggIDs.add(new IndexAndString(-1, kkid));
						for (IndexAndString ias : keggIDs) {
							if (ias.getValue() != null) {
								String[] keggID = ias.getValue().split(" ");
								for (String kid : keggID) {
									if (kid.startsWith("ko:"))
										kid = kid.substring("ko:".length());
									if (kid.startsWith("K")) {
										ArrayList<String> altIDs = BriteService.getKoNamesFromKO(kid);
										if (altIDs != null) {
											if (useShortName && !briteKO2ecName) {
												int minLen = Integer.MAX_VALUE;
												for (String s : altIDs) {
													int len = s.length();
													if (len < minLen) {
														targetName = s;
														minLen = len;
													}
												}
											} else {
												if (briteKO2ecName) {
													for (String s : altIDs) {
														if (s.toUpperCase().startsWith("EC:")) {
															targetName = s.substring("ec:".length());
															break;
														}
													}
												} else
													if (briteKO2geneName && altIDs.size() > 0) {
														targetName = altIDs.iterator().next();
													}
											}
										}
									}
								}
							}
						}
					}
				
				if (targetName != null) {
					if (preserveOldId)
						AttributeHelper.setAttribute(n, "", oldIdIdentifier, AttributeHelper.getLabel(n, null));
					AttributeHelper.setLabel(n, targetName);
					renameCount++;
					status2 = renameCount + " nodes renamed";
				} else
					if (increaseNodeSize) {
						// retrieve targetName for node width check below
						targetName = AttributeHelper.getLabel(n, null);
					}
				
				if (increaseNodeSize && targetName != null) {
					// check if this node has a ellipse shape, then do not resize...
					boolean processSize = true;
					NodeGraphicAttribute na = (NodeGraphicAttribute) n.getAttribute(GraphicAttributeConstants.GRAPHICS);
					if (na != null) {
						if (na.getShape().equals(GraphicAttributeConstants.ELLIPSE_CLASSNAME) ||
								na.getShape().equals(GraphicAttributeConstants.CIRCLE_CLASSNAME))
							processSize = false;
					}
					// / ************************************************************
					if (processSize) {
						border = ((Double) AttributeHelper.getAttributeValue(n,
								"charting", "empty_border_width", new Double(2d), new Double(2d)))
								.doubleValue();
						
						border += AttributeHelper.getFrameThickNess(n);
						
						LabelAttribute la = AttributeHelper.getLabel(-1, n);
						if (la != null) {
							la.wordWrap();
							double newWidth = la.getLastComponentWidth();
							double newHeight = la.getLastComponentHeight();
							Dimension d = AttributeHelper.getSizeD(n);
							d.setSize(d.getWidth(), d.getHeight());
							double xd = border + 2;
							double yd = border;
							if (d.getWidth() - xd < newWidth || d.getHeight() - yd < newHeight)
								AttributeHelper.setSize(n,
										d.getWidth() - xd < newWidth ? newWidth + xd : d.getWidth(),
										d.getHeight() - yd < newHeight ? newHeight + yd : d.getHeight()
										);
						}
					}
				}
				
				if (pleaseStop) {
					status1 = "Command aborted";
					status2 = "Node label changes are incomplete";
					statusVal = 100;
					break;
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		} finally {
			if (graph != null)
				graph.getListenerManager().transactionFinished(this);
		}
		status1 = "Processing complete";
		statusVal = 100;
	}
	
	private String processCompoundInfo(Node n, String targetName,
			CompoundEntry ce) {
		if (compIDtoName) {
			targetName = ce.getNames().iterator().next();
			if (useShortName) {
				for (String syn : ce.getNames()) {
					if (syn.length() < targetName.length())
						targetName = syn;
				}
			}
			if (useGreekName)
				targetName = CompoundEntry.getGreekName(targetName);
		}
		if (compNameToID)
			targetName = ce.getID();
		return targetName;
	}
	
	public static void setCompoundAnnotation(Node n, CompoundEntry ce) {
		String f = ce.getFormula();
		if (f != null && f.length() > 0)
			AttributeHelper.setAttribute(n, "kegg", "formula", f);
		String m = ce.getMass();
		if (m != null && m.length() > 0) {
			try {
				AttributeHelper.setAttribute(n, "kegg", "mass", Double.parseDouble(m));
			} catch (Exception e) {
				
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		return statusVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return statusVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	@Override
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	@Override
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseStop()
	 */
	@Override
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	@Override
	public boolean wantsToStop() {
		return pleaseStop;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	@Override
	public void pleaseContinueRun() {
		// empty
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		statusVal = value;
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
}
