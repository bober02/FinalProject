package project.analysis;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

import project.analysis.statistics.WindowOnlineVariance;
import project.utis.DescendingDoubleComparator;

public class StandardDeviationSolver extends AbstractRegimeSolver {

	private int windowSize;
	private WindowOnlineVariance variance;
	private double bucketSize;

	public StandardDeviationSolver() {
		bucketSize = 0.05;
	}

	public StandardDeviationSolver(int windowSize, double bucketSize) {
		this();
		this.windowSize = windowSize;
		this.bucketSize = Precision.round(bucketSize, 2);
		variance = new WindowOnlineVariance(windowSize);
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void setBucketSize(double bucketSize) {
		this.bucketSize = bucketSize;
	}

	@Override
	public Regime[] solve(double[] xs, double[] ys) {
		if (xs.length != ys.length)
			throw new IllegalArgumentException("Lengths of data must be the same: XS: " + xs.length + "  Ys: " + ys.length);
		if (variance == null) {
			windowSize = (int) Math.max(5, xs.length * 0.05);
			variance = new WindowOnlineVariance(windowSize);
		}
		double[] weights = new double[ys.length];
		int length = weights.length;

		Integer[] leaders;
		int leaderSize = (int) (100 / (bucketSize * 100));
		if (100 % (bucketSize * 100) != 0) {
			leaders = new Integer[leaderSize + 1];
		}
		else {
			leaders = new Integer[leaderSize];
		}
		weights[0] = weights[ys.length - 1] = 0;
		for (int i = ys.length - 1; i > ys.length - 1 - windowSize && i > 0; i--) {
			variance.increment(ys[i]);
			weights[i - 1] = FastMath.sqrt(variance.getResult());
		}
		variance.clear();
		for (int i = 0; i < windowSize && i + 1 < ys.length; i++) {
			variance.increment(ys[i]);
			double currentVariance = FastMath.sqrt(variance.getResult());
			double rightVariance = weights[i + 1];
			if (rightVariance > 0) {
				weights[i + 1] = calculateMeasure(currentVariance, rightVariance, i + 1, windowSize - i - 1);
				updateLeader(i + 1, length, leaders, weights);
			}
			else {
				weights[i + 1] = currentVariance;
			}
		}
		for (int i = windowSize; i < ys.length - 1; i++) {
			variance.increment(ys[i]);
			double currentVariance = FastMath.sqrt(variance.getResult());
			double index = i + 1;
			if (index > ys.length - 1 - (windowSize + 1)) {
				double rightVariance = weights[i + 1];
				if (rightVariance > 0) {
					// CHANGE
					weights[i + 1] = calculateMeasure(currentVariance, rightVariance, windowSize, length - i - 2);
					updateLeader(i + 1, length, leaders, weights);
				}
			}
			else {
				weights[i + 1] = currentVariance;
			}
			int leftIndex = i - windowSize;
			double leftVariance = weights[leftIndex];
			if (leftVariance > 0) {
				weights[leftIndex] = calculateMeasure(leftVariance, currentVariance, FastMath.min(leftIndex, windowSize), windowSize);
				updateLeader(leftIndex, length, leaders, weights);
			}
		}
		variance.clear();
		Arrays.sort((Integer[]) leaders, new DescendingDoubleComparator(weights));
		double[] result = new double[leaders.length];
		result[0] = leaders[0];
		double maxWeight = weights[leaders[0]];
		int regime = 1;
		double distanceThreshold = FastMath.max(0.5 * windowSize, 100);
		for (int i = 1; i < leaders.length; i++) {
			int leader = leaders[i];
			double ratio = weights[leaders[i]] / maxWeight;
			// we have sorted the leaders, so there is no point to continue
			if (ratio < 0.5)
				continue;
			double diff = Double.MAX_VALUE;
			// compare against all previous regimes
			for (int k = 0; k < regime; k++) {
				diff = FastMath.min(diff, FastMath.abs(result[k] - leader));
			}
			if (diff > distanceThreshold) {
				result[regime] = leader;
				regime++;
			}
		}
		result = Arrays.copyOfRange(result, 0, regime);
		Regime[] regimeResult = new Regime[result.length + 1];
		for (int i = 0; i < result.length; i++)
			regimeResult[i] = new Regime(xs[(int) result[i]]);
		regimeResult[regimeResult.length - 1] = new Regime(length);
		return regimeResult;
	}

	private double calculateMeasure(double leftStdDev, double rightStdDev, int leftPoints, int rightPoints) {
		if (leftStdDev == 0 || rightStdDev == 0)
			return 0;
		double diff = FastMath.abs(leftPoints - rightPoints);
		double k = 1.0 - diff / windowSize;
		return k * FastMath.max(leftStdDev / rightStdDev, rightStdDev / leftStdDev);
	}

	private void updateLeader(int index, int length, Integer[] leaders, double[] weights) {
		double measure = weights[index];
		int bucket = (int) ((((double) index) / length) / bucketSize);
		Integer leaderIndex = leaders[bucket];
		if (leaderIndex == null) {
			leaders[bucket] = index;
		}
		else {
			double maxMeasure = weights[leaderIndex];
			if (measure > maxMeasure) {
				leaders[bucket] = index;
			}
		}
	}
}
