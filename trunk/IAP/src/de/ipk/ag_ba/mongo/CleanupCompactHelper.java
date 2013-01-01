package de.ipk.ag_ba.mongo;

import java.util.HashMap;
import java.util.Set;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.WriteConcern;

public class CleanupCompactHelper implements RunnableOnDB {
	private final BackgroundTaskStatusProviderSupportingExternalCall status;
	private final StringBuilder res;
	private DB db;
	
	public CleanupCompactHelper(BackgroundTaskStatusProviderSupportingExternalCall status, StringBuilder res) {
		this.status = status;
		this.res = res;
	}
	
	@Override
	public void run() {
		Set<String> col = db.getCollectionNames();
		int n = 0;
		for (String mgfs : col) {
			String msg = "Start compact collection (" + mgfs + ") // " + SystemAnalysis.getCurrentTime();
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			res.append("Start compact collection (" + mgfs + ") // " + SystemAnalysis.getCurrentTime() + "<br>");
			status.setCurrentStatusText1("Start compact of " + mgfs + " at " + SystemAnalysis.getCurrentTime());
			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put("compact", mgfs);// + ".files");
			m.put("force", true);
			BasicDBObject cmd = new BasicDBObject(m);
			WriteConcern wc = db.getWriteConcern();
			db.setWriteConcern(WriteConcern.NONE);
			db.command(cmd);
			db.setWriteConcern(wc);
			SystemAnalysis.sleep(10000);
			
			msg = "Finished compact collection (" + mgfs + ") // " + SystemAnalysis.getCurrentTime();
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			res.append("Finished compact collection (" + mgfs + ") // " + SystemAnalysis.getCurrentTime() + "<br>");
			n++;
			status.setCurrentStatusValueFine(100d * n / col.size());
		}
		String msg = "COMPACT DATABASE FINISHED // "
				+ SystemAnalysis.getCurrentTime();
		System.out.println(msg);
		MongoDB.saveSystemMessage(msg);
		res.append("COMPACT DATABASE FINISHED // "
				+ SystemAnalysis.getCurrentTime() + "<br>");
		status.setCurrentStatusText1("Compact operation finished");
		status.setCurrentStatusValueFine(100d);
	}
	
	@Override
	public void setDB(DB db) {
		this.db = db;
	}
}