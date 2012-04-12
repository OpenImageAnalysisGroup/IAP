package de.ipk.ag_ba.experimentdata;

import java.util.Collection;

public interface Revision {
	public long getSaveTime();
	
	public void setSaveTime(long time);
	
	public long getReplaces();
	
	public void setReplaces(long time);
	
	public String getEditor();
	
	public void setEditor(String editor);
	
	public String getEditComment();
	
	public void setEditComment(String comment);
	
	public Collection<Revision> getRevisionSet();
}
