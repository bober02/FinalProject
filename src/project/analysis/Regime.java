package project.analysis;

public class Regime implements Comparable<Regime>{

	private double mean;
	private double stdDev;
	private double regimeEnd;

	public Regime(double regimeEnd) {
		this(Double.NaN, Double.NaN, regimeEnd);
	}

	public Regime(double stdDev, double maxValue) {
		this(Double.NaN, stdDev, maxValue);
	}

	public Regime(double mean, double stdDev, double maxValue) {
		this.mean = mean;
		this.stdDev = stdDev;
		this.regimeEnd = maxValue;
	}

	public double getMean() {
		return mean;
	}

	public double getStdDev() {
		return stdDev;
	}

	public double getRegimeEnd() {
		return regimeEnd;
	}

	@Override
	public String toString() {
		return "Mean: " + mean + ", standard deviation: " + stdDev + ", max value: " + regimeEnd;
	}

	@Override
	public int compareTo(Regime o) {
		return new Double(regimeEnd).compareTo(new Double(o.getRegimeEnd()));
	}
}
