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
	private final boolean repairDatabase;
	
	public CleanupCompactHelper(BackgroundTaskStatusProviderSupportingExternalCall status, StringBuilder res) {
		this.status = status;
		this.res = res;
		this.repairDatabase = false;
	}
	
	public CleanupCompactHelper(BackgroundTaskStatusProviderSupportingExternalCall status, StringBuilder res, boolean repairDatabase) {
		this.status = status;
		this.res = res;
		this.repairDatabase = repairDatabase;
	}
	
	@Override
	public void run() {
		if (repairDatabase) {
			status.setCurrentStatusValueFine(-1);
			String msg = "Start repairing database // " + SystemAnalysis.getCurrentTime();
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			res.append(msg);
			res.append(msg + "<br>");
			long startTime = System.currentTimeMillis();
			status.setCurrentStatusText1("Start repairing database at " + SystemAnalysis.getCurrentTime());
			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put("repairDatabase", 1);
			BasicDBObject cmd = new BasicDBObject(m);
			WriteConcern wc = db.getWriteConcern();
			db.setWriteConcern(WriteConcern.NONE);
			db.command(cmd);
			db.setWriteConcern(wc);
			long fullTime = System.currentTimeMillis() - startTime;
			SystemAnalysis.sleep(10000);
			msg = "Finished repairing database (operation took " + SystemAnalysis.getWaitTime(fullTime) + ") // " + SystemAnalysis.getCurrentTime();
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			res.append(msg + "<br>");
			status.setCurrentStatusValueFine(100d);
			status.setCurrentStatusText1("Repair operation finished");
			status.setCurrentStatusValueFine(100d);
		} else {
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
	}
	
	@Override
	public void setDB(DB db) {
		this.db = db;
	}
}