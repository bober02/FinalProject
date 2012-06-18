package project.analysis;

public class Regime {

	private double mean;
	private double stdDev;
	private double maxValue;
	public Regime(double mean, double stdDev, double maxValue) {
		super();
		this.mean = mean;
		this.stdDev = stdDev;
		this.maxValue = maxValue;
	}
	public double getMean() {
		return mean;
	}
	public double getStdDev() {
		return stdDev;
	}
	public double getMaxValue() {
		return maxValue;
	}
	
	@Override
	public String toString(){
		return "Mean: " + mean + ", standard deviation: " + stdDev + ", max value: " + maxValue;
	}
}
