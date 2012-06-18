package project.datafeed;

import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

import project.analysis.Regime;

public class RegimeGeneratingDataFeed implements DataFeed {

	private int window;
	private int dataPoints;
	private int index;
	private double[] regimePoints;
	private int maxRegimes;
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

	public double[] getRegimePoints() {
		return regimePoints;
	}

	public RegimeGeneratingDataFeed(int regimes, int numPoints) {
		maxRegimes = regimes;
		this.window = numPoints / (regimes + 1);
		dataPoints = numPoints;
		index = 0;
		regimePoints = new double[regimes];
		this.regimes = new Regime[maxRegimes + 1];
		for (int i = 0; i < regimes; i++) {
			regimePoints[i] = (i + 1) * window;
		}
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
		double stdDev = rand.nextDouble() * 3;
		boolean low = true;
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
					stdDev = 5 + rand.nextDouble() * 5;
					dist = new NormalDistribution(mean, stdDev);
				}
				else {
					mean = rand.nextDouble() * 15;
					stdDev = rand.nextDouble() * 3;
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
