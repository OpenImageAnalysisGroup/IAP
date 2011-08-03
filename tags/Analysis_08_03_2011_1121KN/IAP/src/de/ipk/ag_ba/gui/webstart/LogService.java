package de.ipk.ag_ba.gui.webstart;

import java.util.Stack;

import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;

public class LogService {
	
	private static boolean ba13reachable = IAPservice.isMongoReachable();
	
	public String getLatestNews(int n, String pre, String preLine, String lineBreak, String follow) {
		StringBuilder res = new StringBuilder();
		if (!ba13reachable) {
			System.out.println("INFO: MAIN CLOUD DATABASE NOT REACHABLE: COULD RETRIEVE LATEST NEWS");
			return res.toString();
		}
		try {
			Stack<String> news = new Stack<String>();
			for (String item : MongoDB.getDefaultCloud().getNews(5)) {
				news.push(preLine + item);
			}
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
