package de.so_fa.modellflug.jeti.jla.jetilog;

public class TimeDuration {
	long mySeconds;
	
	/** creates a time duration string in the format : 12:34:56 as hours:minutes:seconds
	 * @param aDuration: duration in seconds
	 * @return time duration string
	*/
	static public String getString(long aDurationInSec) {
		TimeDuration td = new TimeDuration(aDurationInSec);
		return td.toString();
	}

	public TimeDuration(long aSeconds) {
		mySeconds = aSeconds;
	}

	public String toString() {
		int time = (int) mySeconds;
		int hours;
		int minutes;
		int seconds;

		hours = (int) time / 3600;
		time -= hours * 3600;
		minutes = time / 60;
		time -= minutes * 60;
		seconds = time;

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}
