package de.ipk.ag_ba.gui.webstart;

import java.util.Stack;

import org.SystemAnalysis;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;

public class LogService {
	
	private static boolean ba13reachable = IAPservice.isMongoReachable();
	
	public String getLatestNews(int n, String pre, final String preLine, String lineBreak, String follow) {
		StringBuilder res = new StringBuilder();
		final Stack<String> news = new Stack<String>();
		if (!ba13reachable) {
			news.add(preLine
					+ SystemAnalysis.getCurrentTime()
					+ ": Data Processing database is not reachable at network level (time-out). <b>&quot;Data Processing&quot; function may not work correctly at the moment.</b> (system message)");
		} else {
			try {
				Runnable r = new Runnable() {
					@Override
					public void run() {
							MongoDB dc = MongoDB.getDefaultCloud();
							if (dc != null)
								for (String item : dc.getNews(5)) {
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
					if (current - start > 2000) {
						news.add(preLine
								+ SystemAnalysis.getCurrentTime()
								+ ": Could not access latest news (time-out). <b>&quot;Data Processing&quot; function may not work correctly at the moment.</b> (system message)");
						t.interrupt();
						break;
					}
				} while (t.isAlive());
			} catch (Exception e) {
				news.add(preLine
						+ SystemAnalysis.getCurrentTime()
						+ ": Could not access latest news (" + e.getMessage()
						+ "). <b>&quot;Data Processing&quot; function may not work correctly at the moment.</b> (system message)");
			}
		}
		while (!news.empty()) {
			String item = news.pop();
			res.append(item);
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
