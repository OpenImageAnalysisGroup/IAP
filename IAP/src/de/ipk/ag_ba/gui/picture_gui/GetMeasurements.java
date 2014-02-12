package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;

public class GetMeasurements implements Runnable {
	boolean readOnly;
	private final ArrayList<SampleInterface> samples;
	private final MongoTreeNode sampNode;
	private final ExperimentReference experiment;
	private final ActionListener dataChangedListener;
	private final SimpleDateFormat sdf;
	
	public GetMeasurements(MongoTreeNode sampNode,
			ExperimentReference experiment, ArrayList<SampleInterface> samples,
			boolean readOnly, ActionListener dataChangedListener, SimpleDateFormat sdf) {
		this.readOnly = readOnly;
		this.samples = samples;
		this.sampNode = sampNode;
		this.experiment = experiment;
		this.dataChangedListener = dataChangedListener;
		this.sdf = sdf;
	}
	
	@Override
	public void run() {
		int p = 0;
		ArrayList<DBEtreeNodeModelHelper> children = new ArrayList<DBEtreeNodeModelHelper>();
		for (SampleInterface sample : samples) {
			for (Measurement meas : sample) {
				MongoTreeNode measNode = new MongoTreeNode(sampNode,
						dataChangedListener, experiment, meas,
						meas.toString(), readOnly);
				measNode.setIsLeaf(true);
				measNode.setIndex(p++);
				
				if (meas instanceof NumericMeasurementInterface) {
					NumericMeasurementInterface nm = (NumericMeasurementInterface) meas;
					Map<String, Object> attributes = new HashMap<String, Object>();
					nm.fillAttributeMap(attributes);
					
					Map<String, Object> attributesOfSA = new HashMap<String, Object>();
					nm.getParentSample().getSampleAverage().fillAttributeMap(attributesOfSA);
					
					for (String k : attributesOfSA.keySet()) {
						if (!attributes.containsKey(k))
							attributes.put(k, "<td/>");
					}
					
					StringBuilder s = new StringBuilder();
					s.append("<html><table border='1'><th>Property</th><th>Value</th><td>Sample Average</td></tr>");
					if (sample.getSampleFineTimeOrRowId() != null)
						s.append("<tr><td>Sample Time</td><td>" + sdf.format(new Date(sample.getSampleFineTimeOrRowId())) + "</td></tr>");
					for (String id : attributes.keySet()) {
						String idC = id;
						if (idC != null && idC.equals("replicates"))
							idC = "replicate ID";
						if (attributes.get(id) != null || attributesOfSA.get(id) != null) {
							Object v = attributes.get(id);
							if (v == null)
								v = "</td>";
							else
								if (!(v + "").startsWith("<td/>"))
									v = "<td>" + v + "</td>";
							s.append("<tr><td>" + idC + "</td>" + v + "<td>" + attributesOfSA.get(id) + "</td></tr>");
						}
					}
					s.append("</table></html>");
					measNode.setTooltipInfo(s.toString());
				} else {
					if (sample.getSampleFineTimeOrRowId() != null)
						measNode.setTooltipInfo(sdf.format(new Date(sample.getSampleFineTimeOrRowId())));
				}
				
				children.add(measNode);
			}
		}
		sampNode.setChildren(children.toArray(new DBEtreeNodeModelHelper[0]));
	}
}
