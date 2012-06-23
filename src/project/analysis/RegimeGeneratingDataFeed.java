package project.analysis;

import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

import project.datafeed.DataFeed;
import project.datafeed.DataFeedException;

public class RegimeGeneratingDataFeed implements DataFeed {

	private int window;
	private int dataPoints;
	private int index;
	private int maxRegimes;
	private boolean randomizeVariance;
	private Regime[] regimes;

	public RegimeGeneratingDataFeed() {
		this(2);
	}

	public RegimeGeneratingDataFeed(int regimes) {
		this(regimes, (regimes + 1) * 1000);
	}

	public Regime[] getRegimes() {
		return regimes;
	}

	public RegimeGeneratingDataFeed(int regimes, int numPoints) {
		this(regimes, numPoints, true);
	}

	public RegimeGeneratingDataFeed(int regimes, boolean randomizeVariance) {
		this(regimes, (regimes + 1) * 1000, randomizeVariance);
	}

	public RegimeGeneratingDataFeed(int regimes, int numPoints, boolean randomizeVariance) {
		maxRegimes = regimes;
		this.window = numPoints / (regimes + 1);
		dataPoints = numPoints;
		this.randomizeVariance = randomizeVariance;
		index = 0;
		this.regimes = new Regime[maxRegimes + 1];
	}

	@Override
	public double[] getNextValue() throws DataFeedException {
		return null;
	}

	@Override
	public double[][] getAllValues() throws DataFeedException {
		double[][] result = new double[dataPoints][2];
		Random rand = new Random();
		double mean = rand.nextDouble() * 15;
		double stdDevLow = 1;
		double stdDevHigh = 10;
		boolean low = true;
		double stdDev = stdDevLow;
		NormalDistribution dist = new NormalDistribution(mean, stdDev);
		int pointCount = 0;
		int regimes = 0;
		for (; index < dataPoints; index++) {
			result[index][0] = index;
			result[index][1] = dist.sample();
			pointCount++;
			if (pointCount >= window && regimes < maxRegimes) {
				this.regimes[regimes] = new Regime(mean, stdDev, (regimes + 1) * window);
				regimes++;
				pointCount = 0;
				if (low) {
					mean = rand.nextDouble() * 15;
					if (randomizeVariance)
						stdDev = 5 + rand.nextDouble() * 5;
					else
						stdDev = stdDevHigh;
					dist = new NormalDistribution(mean, stdDev);
				}
				else {
					mean = rand.nextDouble() * 15;
					if (randomizeVariance)
						stdDev = rand.nextDouble() * 3;
					else
						stdDev = stdDevLow;
					dist = new NormalDistribution(mean, stdDev);

				}
				low = !low;
			}
		}
		this.regimes[maxRegimes] = new Regime(mean, stdDev, dataPoints);
		return result;
	}

	@Override
	public boolean reset() {
		index = 0;
		return true;
	}

}
