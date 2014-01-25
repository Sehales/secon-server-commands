package net.sehales.scservercmds;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

	/**
	 * parse a string like 1year2weeks3months4minutes5seconds... and returns the timestamp of the date in milliseconds
	 * 
	 * @param time
	 * @return
	 * @throws Exception
	 */
	public static long getTimestamp(String time) throws Exception {

		Pattern timePattern = Pattern.compile(

		"(?:([0-9]+)y[a-z]*[,]*)?" +

		"(?:([0-9]+)mo[a-z]*[,]*)?" +

		"(?:([0-9]+)w[a-z]*[,]*)?" +

		"(?:([0-9]+)d[a-z]*[,]*)?" +

		"(?:([0-9]+)h[a-z]*[,]*)?" +

		"(?:([0-9]+)m[a-z]*[,]*)?" +

		"(?:([0-9]+)(?:s[a-z]*)?)?",

		Pattern.CASE_INSENSITIVE);

		Matcher m = timePattern.matcher(time);

		int years = 0, months = 0, weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0;

		m.find();

		if (m.group() == null || m.group().isEmpty())
			throw new Exception("Illegal input");

		if (m.group(1) != null && !m.group(1).isEmpty())
			years = Integer.parseInt(m.group(1));
		if (m.group(2) != null && !m.group(2).isEmpty())
			months = Integer.parseInt(m.group(2));
		if (m.group(3) != null && !m.group(3).isEmpty())
			weeks = Integer.parseInt(m.group(3));
		if (m.group(4) != null && !m.group(4).isEmpty())
			days = Integer.parseInt(m.group(4));
		if (m.group(5) != null && !m.group(5).isEmpty())
			hours = Integer.parseInt(m.group(5));
		if (m.group(6) != null && !m.group(6).isEmpty())
			minutes = Integer.parseInt(m.group(6));
		if (m.group(7) != null && !m.group(7).isEmpty())
			seconds = Integer.parseInt(m.group(7));

		Calendar c = new GregorianCalendar();
		if (years > 0)
			c.add(Calendar.YEAR, years);
		if (months > 0)
			c.add(Calendar.MONTH, months);
		if (weeks > 0)
			c.add(Calendar.WEEK_OF_YEAR, weeks);
		if (days > 0)
			c.add(Calendar.DAY_OF_MONTH, days);
		if (hours > 0)
			c.add(Calendar.HOUR_OF_DAY, hours);
		if (minutes > 0)
			c.add(Calendar.MINUTE, minutes);
		if (seconds > 0)
			c.add(Calendar.SECOND, seconds);
		return c.getTimeInMillis();

	}
}
