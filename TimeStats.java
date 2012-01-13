import java.util.Locale;

class TimeStats {
	private double minTime = Double.POSITIVE_INFINITY;
	private double maxTime;
	private double sumTime;
	private double n;

	public void add(double time) {
		minTime = Math.min(minTime, time);
		maxTime = Math.max(maxTime, time);
		sumTime += time;
		n++;
	}

	public double avgTime() {
		return sumTime / n;
	}

	@Override
	public String toString() {
		if (n > 1) {
			double avgTime = avgTime();
			return String.format(Locale.US, "[%+6.2f%% | %.2f - %.2f - %.2f | %+6.2f%%]",
					(minTime - avgTime) * 100 / avgTime, minTime, avgTime, maxTime, (maxTime - avgTime) * 100 / avgTime);
		} else
			return "";
	}
}
