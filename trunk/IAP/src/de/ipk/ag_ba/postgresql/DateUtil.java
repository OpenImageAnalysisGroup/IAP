package de.ipk.ag_ba.postgresql;

/**
 * public domain code (http://forums.sun.com/thread.jspa?threadID=488668&forumID=4)
 */
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class DateUtil {
	/**
	 * No need for an instance
	 */
	private DateUtil() {
	}
	
	/**
	 * Elapsed days based on current time
	 * 
	 * @param date
	 *           Date
	 * @return int number of days
	 */
	public static int getElapsedDays(Date date) {
		return elapsed(date, Calendar.DATE);
	}
	
	/**
	 * Elapsed days based on two Date objects
	 * 
	 * @param d1
	 *           Date
	 * @param d2
	 *           Date
	 * @return int number of days
	 */
	public static int getElapsedDays(Date d1, Date d2) {
		return elapsed(d1, d2, Calendar.DATE);
	}
	
	/**
	 * Elapsed months based on current time
	 * 
	 * @param date
	 *           Date
	 * @return int number of months
	 */
	public static int getElapsedMonths(Date date) {
		return elapsed(date, Calendar.MONTH);
	}
	
	/**
	 * Elapsed months based on two Date objects
	 * 
	 * @param d1
	 *           Date
	 * @param d2
	 *           Date
	 * @return int number of months
	 */
	public static int getElapsedMonths(Date d1, Date d2) {
		return elapsed(d1, d2, Calendar.MONTH);
	}
	
	/**
	 * Elapsed years based on current time
	 * 
	 * @param date
	 *           Date
	 * @return int number of years
	 */
	public static int getElapsedYears(Date date) {
		return elapsed(date, Calendar.YEAR);
	}
	
	/**
	 * Elapsed years based on two Date objects
	 * 
	 * @param d1
	 *           Date
	 * @param d2
	 *           Date
	 * @return int number of years
	 */
	public static int getElapsedYears(Date d1, Date d2) {
		return elapsed(d1, d2, Calendar.YEAR);
	}
	
	/**
	 * All elaspsed types
	 * 
	 * @param g1
	 *           GregorianCalendar
	 * @param g2
	 *           GregorianCalendar
	 * @param type
	 *           int (Calendar.FIELD_NAME)
	 * @return int number of elapsed "type"
	 */
	private static int elapsed(GregorianCalendar g1, GregorianCalendar g2, int type) {
		GregorianCalendar gc1, gc2;
		int elapsed = 0;
		// Create copies since we will be clearing/adding
		if (g2.after(g1)) {
			gc2 = (GregorianCalendar) g2.clone();
			gc1 = (GregorianCalendar) g1.clone();
		} else {
			gc2 = (GregorianCalendar) g1.clone();
			gc1 = (GregorianCalendar) g2.clone();
		}
		if (type == Calendar.MONTH || type == Calendar.YEAR) {
			gc1.clear(Calendar.DATE);
			gc2.clear(Calendar.DATE);
		}
		if (type == Calendar.YEAR) {
			gc1.clear(Calendar.MONTH);
			gc2.clear(Calendar.MONTH);
		}
		while (gc1.before(gc2)) {
			gc1.add(type, 1);
			elapsed++;
		}
		return elapsed;
	}
	
	/**
	 * All elaspsed types based on date and current Date
	 * 
	 * @param date
	 *           Date
	 * @param type
	 *           int (Calendar.FIELD_NAME)
	 * @return int number of elapsed "type"
	 */
	private static int elapsed(Date date, int type) {
		return elapsed(date, new Date(), type);
	}
	
	/**
	 * All elaspsed types
	 * 
	 * @param d1
	 *           Date
	 * @param d2
	 *           Date
	 * @param type
	 *           int (Calendar.FIELD_NAME)
	 * @return int number of elapsed "type"
	 */
	private static int elapsed(Date d1, Date d2, int type) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		GregorianCalendar g1 = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
							.get(Calendar.DATE));
		cal.setTime(d2);
		GregorianCalendar g2 = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
							.get(Calendar.DATE));
		return elapsed(g1, g2, type);
	}
}