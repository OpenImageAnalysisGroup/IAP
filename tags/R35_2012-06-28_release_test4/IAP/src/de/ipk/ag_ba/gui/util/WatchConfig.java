package de.ipk.ag_ba.gui.util;

import java.util.Date;

public class WatchConfig {
	String database, label;
	int h1_st, h2_st, h1_end, h2_end, minute1_st, minute2_st, minute1_end, minute2_end, lastMinutes;
	
	String mails;
	
	WatchConfig(String conf) {
		String[] parts = conf.split(",");
		if (parts.length != 8)
			throw new UnsupportedOperationException("Invalid config entry (" + conf + "), needs 6 defined parts");
		int i = 0;
		
		database = parts[i++].trim();
		label = parts[i++].trim();
		
		h1_st = Integer.parseInt(parts[i].trim().split(":")[0]);
		minute1_st = Integer.parseInt(parts[i++].trim().split(":")[1]);
		
		h1_end = Integer.parseInt(parts[i].trim().split(":")[0]);
		minute1_end = Integer.parseInt(parts[i++].trim().split(":")[1]);
		
		h2_st = Integer.parseInt(parts[i].trim().split(":")[0]);
		minute2_st = Integer.parseInt(parts[i++].trim().split(":")[1]);
		
		h2_end = Integer.parseInt(parts[i].trim().split(":")[0]);
		minute2_end = Integer.parseInt(parts[i++].trim().split(":")[1]);
		
		lastMinutes = Integer.parseInt(parts[i++].trim());
		
		mails = parts[i++];
	}
	
	public int getLastMinutes() {
		return lastMinutes;
	}
	
	public String getExperimentName() {
		return label;
	}
	
	@SuppressWarnings("deprecation")
	private long start(int h_off, int m_off) {
		Date now = new Date();
		now.setHours(h_off);
		now.setMinutes(m_off);
		return now.getTime();
	}
	
	public long getStartTimeForToday1() {
		return start(h1_st, minute1_st);
	}
	
	public long getStartTimeForToday2() {
		if (h2_st == 0 && minute2_st == 0 && h2_end == 0 && minute2_end == 0)
			return getStartTimeForToday1();
		else {
			return start(h2_st, minute2_st);
		}
	}
	
	public long getEndTimeForToday1() {
		return start(h1_end, minute1_end);
	}
	
	public long getEndTimeForToday2() {
		if (h2_st == 0 && minute2_st == 0 && h2_end == 0 && minute2_end == 0)
			return getEndTimeForToday1();
		else {
			return start(h2_end, minute2_end);
		}
	}
	
	public String getMails() {
		return mails;
	}
	
	public String getDatabase() {
		return database;
	}
}