package de.ipk.ag_ba.gui.webstart;

import java.util.Stack;

import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

public class LogService {
	
	private static boolean ba13reachable = IAPservice.isMongoReachable();
	
	public String getLatestNews(int n, String pre, final String preLine, String lineBreak, String follow) {
		StringBuilder res = new StringBuilder();
		if (!ba13reachable) {
			System.out.println("INFO: MAIN CLOUD DATABASE NOT REACHABLE: COULD RETRIEVE LATEST NEWS");
			return res.toString();
		}
		try {
			final Stack<String> news = new Stack<String>();
			Runnable r = new Runnable() {
				@Override
				public void run() {
					for (String item : MongoDB.getDefaultCloud().getNews(5)) {
						news.push(preLine + item);
					}
				}
			};
			Thread t = new Thread(r, "Read MonogDB news");
			t.start();
			long start = System.currentTimeMillis();
			do {
				Thread.sleep(10);
				long current = System.currentTimeMillis();
				if (current - start > 1000) {
					news.add(SystemAnalysisExt.getCurrentTime()
							+ ": Could not access latest news (time-out). <b>&quot;Data Processing&quot; function may not work correctly at the moment.</b> (system message)");
					t.interrupt();
					break;
				}
			} while (t.isAlive());
			while (!news.empty()) {
				String item = news.pop();
				res.append(item);
			}
		} catch (Exception e) {
			System.out.println("ERROR: COULD RETRIEVE LATEST NEWS: " + e.getMessage());
		}
		if (res != null && res.length() > 0)
			return pre + res.toString() + follow;
		else
			return res.toString();
	}
	
	public void addNews(MongoDB m, String user, String text) throws Exception {
		m.addNewsItem(text, user);
	}
}
