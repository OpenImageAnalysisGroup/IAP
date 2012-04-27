package de.ipk.ag_ba.experimentdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;

public class NetWorkDataWithRevisionSupport extends NetworkData implements Revision {
	private long saveTime;
	private long replaces;
	private String editor;
	private String comment;
	
	public NetWorkDataWithRevisionSupport(SampleInterface parent) {
		super(parent);
	}
	
	public NetWorkDataWithRevisionSupport(SampleInterface parent, NetWorkDataWithRevisionSupport other) {
		super(parent, other);
		setSaveTime(other.getSaveTime());
		setReplaces(other.getReplaces());
		setEditor(other.getEditor());
		setEditComment(other.getEditComment());
	}
	
	public NetWorkDataWithRevisionSupport(SampleInterface sample, Map<String, Object> map) {
		super(sample, map);
		if (map.containsKey("savetime"))
			setSaveTime((Long) map.get("savetime"));
		if (map.containsKey("replaces"))
			setReplaces((Long) map.get("replaces"));
		if (map.containsKey("editor"))
			setEditor((String) map.get("editor"));
		if (map.containsKey("comment"))
			setEditComment((String) map.get("comment"));
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributes) {
		super.fillAttributeMap(attributes);
		attributes.put("savetime", saveTime);
		attributes.put("replaces", replaces);
		attributes.put("editor", editor);
		attributes.put("comment", comment);
	}
	
	@Override
	protected NetWorkDataWithRevisionSupport clone() throws CloneNotSupportedException {
		return new NetWorkDataWithRevisionSupport(getParentSample(), this);
	}
	
	@Override
	public long getSaveTime() {
		return saveTime;
	}
	
	@Override
	public void setSaveTime(long time) {
		this.saveTime = time;
	}
	
	@Override
	public long getReplaces() {
		return replaces;
	}
	
	@Override
	public void setReplaces(long time) {
		this.replaces = time;
	}
	
	@Override
	public String getEditor() {
		return editor;
	}
	
	@Override
	public void setEditor(String editor) {
		this.editor = editor;
	}
	
	@Override
	public String getEditComment() {
		return comment;
	}
	
	@Override
	public void setEditComment(String comment) {
		this.comment = comment;
	}
	
	@Override
	public Collection<Revision> getRevisionSet() {
		Collection<Revision> result = new ArrayList<Revision>();
		Sample3D s3d = (Sample3D) getParentSample();
		for (NumericMeasurementInterface n : s3d.getMeasurements(MeasurementNodeType.NETWORK)) {
			if (n instanceof NetWorkDataWithRevisionSupport)
				result.add((Revision) n);
		}
		
		return result;
	}
}
