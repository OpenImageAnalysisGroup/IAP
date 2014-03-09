package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBObject;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.RunnableOnDB;

public class ActionMongoDatabaseServerStatus extends AbstractNavigationAction {
	
	private MongoDB m;
	private Object cmd;
	private String cmdDesc;
	
	public ActionMongoDatabaseServerStatus(String tooltip) {
		super(tooltip);
	}
	
	public ActionMongoDatabaseServerStatus(String tooltip, MongoDB m, Object cmd, String cmdDesc) {
		super(tooltip);
		this.m = m;
		this.cmd = cmd;
		this.cmdDesc = cmdDesc;
	}
	
	private final ArrayList<String> serverStatus = new ArrayList<String>();
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		serverStatus.clear();
		
		final StringBuilder res = new StringBuilder();
		res.append("<table border='0'>");
		res.append("<tr><th colspan='2'>" + cmdDesc + "</th>");
		m.processDB(new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				CommandResult cr;
				if (cmd instanceof String)
					cr = db.command((String) cmd);
				else
					cr = db.command((DBObject) cmd);
				for (String k : cr.keySet()) {
					System.out.println("K=" + k);
					Object o = cr.get(k);
					if (o instanceof BasicDBObject) {
						StringBuilder rr = new StringBuilder();
						rr.append("<table border='0'>");
						rr.append("<tr><th colspan='2'>" + k + "</th>");
						rr.append("<tr><td colspan='2'>" + getString(o) + "</td></tr>");
						rr.append("</table>");
						serverStatus.add(rr.toString());
					} else
						res.append("<tr><td>" + k + "</td><td>" + getString(o) + "</td></tr>");
					// "<tr><th colspan='2'>Server Status</th>"
				}
			}
			
			private String getString(Object o) {
				if (o instanceof BasicDBObject)
					return getStringFromObject((BasicDBObject) o);
				else
					return o.toString();
			}
			
			private String getStringFromObject(BasicDBObject o) {
				StringBuilder r = new StringBuilder();
				r.append("<table border='0'>");
				for (String k : o.keySet()) {
					r.append("<tr><td>" + k + "</td><td>" + getString(o.get(k)) + "</td></tr>");
				}
				r.append("</table>");
				return r.toString();
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		
		res.append("</table>");
		
		serverStatus.add(0, res.toString());
	}
	
	@Override
	public String getDefaultTitle() {
		return cmdDesc;
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getToolbox();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(serverStatus);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}	
}
