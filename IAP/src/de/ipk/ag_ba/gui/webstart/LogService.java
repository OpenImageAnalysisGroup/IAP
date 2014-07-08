package de.ipk.ag_ba.gui.webstart;

import java.util.Stack;

import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.mongo.MongoDB;

public class LogService {
	
	private static boolean isDefaultCloudReachable() {
		return MongoDB.getDefaultCloud() == null ? false : MongoDB.getDefaultCloud().isDbHostReachable();
	}
	
	public String getLatestNews(final int n, String pre, final String preSystemLine, final String preLine, String lineBreak, String follow) {
		if (n < 1)
			return "";
		StringBuilder res = new StringBuilder();
		final Stack<String> news = new Stack<String>();
		if (!isDefaultCloudReachable()) {
			news.add(preLine
					+ SystemAnalysis.getCurrentTime()
					+ ": Data Processing database is not reachable at network level (time-out). <b>&quot;Data Processing&quot; function may not work correctly at the moment.</b> (system message)");
		} else {
			try {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						MongoDB dc = MongoDB.getDefaultCloud();
						try {
							if (dc != null)
								for (String item : dc.getNews(n)) {
									news.push(preLine + item);// StringManipulationTools.removeHTMLtags(item));
								}
						} catch (Exception e2) {
							// error
						}
					}
				};
				Thread t = new Thread(r, "Read MonogDB news");
				t.start();
				long start = System.currentTimeMillis();
				do {
					Thread.sleep(10);
					long current = System.currentTimeMillis();
					if (current - start > IAPoptions.getInstance().getInteger("NEWS", "read_time_out_ms", 2000)) {
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
		StringBuilder resSystem = new StringBuilder(preSystemLine);
		while (!news.empty()) {
			String item = news.pop();
			if (item != null && !item.contains("system-msg"))
				res.append(item);
			else
				resSystem.append(StringManipulationTools.stringReplace(item, "(system-msg)", "").trim());
		}
		if (res != null && res.length() + resSystem.length() > 0) {
			return pre + res.toString() + follow + (resSystem.length() == 0 ? "" : resSystem.toString() + follow);
		} else
			return res.toString();
	}
	
	public void addNews(MongoDB m, String user, String text) throws Exception {
		m.addNewsItem(text, user);
	}
}
