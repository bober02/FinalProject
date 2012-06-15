package project.datafeed;

import org.apache.commons.math3.distribution.NormalDistribution;

public class RegimeGeneratingDataFeed implements DataFeed {

	private int window;
	private int dataPoints;
	private int index;
	private double[] regimePoints;
	private int maxRegimes;

	public RegimeGeneratingDataFeed() {
		this(2);
	}

	public RegimeGeneratingDataFeed(int regimes) {
		this(regimes, (regimes+1)*1000);
	}
	
	public double[] getRegimePoints(){
		return regimePoints;
	}

	public RegimeGeneratingDataFeed(int regimes, int numPoints) {
		maxRegimes = regimes;
		this.window = numPoints / (regimes + 1);
		dataPoints = numPoints;
		index = 0;
		regimePoints = new double[regimes];
		for(int i = 0;  i < regimes; i ++){
			regimePoints[i] = (i+1)*window;
		}
	}

	@Override
	public double[] getNextValue() throws DataFeedException {
		return null;
	}

	@Override
	public double[][] getAllValues() throws DataFeedException {
		double[][] result = new double[dataPoints][2];
		double highVariance = 8;
		double lowVariance = 1;
		NormalDistribution low = new NormalDistribution(0, lowVariance);
		NormalDistribution high = new NormalDistribution(0, highVariance);

		NormalDistribution rand = low;
		int pointCount = 0;
		int regimes = 0;
		for (; index < dataPoints; index++) {
			result[index][0] = index;
			result[index][1] = rand.sample();
			pointCount++;
			if (pointCount >= window && regimes < maxRegimes) {
				regimes++;
				pointCount = 0;
				if (rand == low)
					rand = high;
				else
					rand = low;
			}
		}
		return result;
	}

	@Override
	public boolean reset() {
		index = 0;
		return true;
	}

}
