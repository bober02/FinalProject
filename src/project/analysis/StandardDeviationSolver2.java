package project.analysis;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.util.FastMath;

import project.analysis.statistics.WindowOnlineVariance;
import project.utis.DescendingDoubleComparator;

public class StandardDeviationSolver2 extends AbstractRegimeSolver {

	private int windowSize;
	private WindowOnlineVariance variance;
	private RegimeStrategy strategy;
	private int maxRegimes;

	public StandardDeviationSolver2() {
		strategy = RegimeStrategy.Buckets;
	}

	public StandardDeviationSolver2(int windowSize, int maxRegimes) {
		this();
		this.windowSize = windowSize;
		variance = new WindowOnlineVariance(windowSize);
		this.maxRegimes = maxRegimes;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void setStrategy(RegimeStrategy strategy) {
		this.strategy = strategy;
	}

	public void setMaxRegimes(int maxRegimes) {
		this.maxRegimes = maxRegimes;
	}

	@Override
	public double[] solve(double[] xs, double[] ys) {
		if (xs.length != ys.length)
			throw new IllegalArgumentException("Lengths of data must be the same: XS: " + xs.length + "  Ys: " + ys.length);
		if (variance == null) {
			if (variance == null) {
				windowSize = (int) Math.max(5, xs.length * 0.05);
				variance = new WindowOnlineVariance(windowSize);
			}
		}
		if (maxRegimes == 0)
			maxRegimes = 10;
		double[] weights = new double[ys.length];
		int length = weights.length;
		weights[0] = weights[ys.length - 1] = 0;
		Integer[] indexes = new Integer[ys.length];
		for (int i = ys.length - 1; i > ys.length - 1 - windowSize && i > 0; i--) {
			variance.increment(ys[i]);
			weights[i - 1] = FastMath.sqrt(variance.getResult());
		}
		variance.clear();
		for (int i = 0; i < windowSize && i + 1 < ys.length; i++) {
			indexes[i] = i;
			variance.increment(ys[i]);
			weights[i + 1] = FastMath.sqrt(variance.getResult());
		}
		for (int i = windowSize; i < ys.length - 1; i++) {
			indexes[i] = i;
			variance.increment(ys[i]);
			double currentVariance = FastMath.sqrt(variance.getResult());
			if (i + 1 > ys.length - 1 - (windowSize + 1)) {
				double rightVariance = weights[i + 1];
				if (rightVariance > 0) {
					weights[i + 1] = calculateMeasure(currentVariance, rightVariance, windowSize, length - i - 2);
				}
			}
			else {
				weights[i + 1] = currentVariance;
			}
			int leftIndex = i - windowSize;
			double leftVariance = weights[leftIndex];
			if (leftVariance > 0) {
				weights[leftIndex] = calculateMeasure(leftVariance, currentVariance, FastMath.min(leftIndex, windowSize), windowSize);
			}
		}
		indexes[ys.length - 1] = ys.length - 1;
		variance.clear();
		return getResult(xs, indexes, weights);
	}

	private double calculateMeasure(double leftStdDev, double rightStdDev, int leftPoints, int rightPoints) {
		double diff = FastMath.abs(leftPoints - rightPoints);
		double k = 1.0 - diff / windowSize;
		return k * FastMath.max(leftStdDev / rightStdDev, rightStdDev / leftStdDev);
	}

	private double[] getResult(double[] xs, Integer[] indexes, double[] weights) {
		double[] result = new double[maxRegimes];
		double distanceThreshold = FastMath.max(0.6 * windowSize, 100);
		if (strategy == RegimeStrategy.TopN) {
			Arrays.sort(indexes, new DescendingDoubleComparator(weights));
			for (int i = 0; i < maxRegimes; i++)
				result[i] = xs[indexes[i]];
		}
		else if (strategy == RegimeStrategy.Distance) {
			Arrays.sort(indexes, new DescendingDoubleComparator(weights));
			result[0] = xs[indexes[0]];
			int regime = 1;
			for (int i = 1; i < indexes.length && regime < maxRegimes; i++) {
				if (FastMath.abs(indexes[regime - 1] - indexes[i]) > distanceThreshold) {
					result[regime] = xs[indexes[i]];
					regime++;
				}
			}
		}
		else {
			int maxLeaders = 5 * maxRegimes;
			maxLeaders = FastMath.max(FastMath.min(maxLeaders, 100), 10);
			// leaders correspond to indexes that have highest weight measures
			// in their corresponding buckets
			int bucketSize = indexes.length / maxLeaders;
			Integer[] leaders;
			// We take into account the case when for instance maxLeaders = 20
			// and data = 2010. Our bucketsize would be 100, leaving 10 data
			// points unchecked
			if (indexes.length % maxLeaders != 0) {
				leaders = new Integer[maxLeaders + 1];
			}
			else {
				leaders = new Integer[maxLeaders];
			}
			Comparator<Integer> comparator = new DescendingDoubleComparator(weights);
			int bucketIndex = 0;
			double bucketMaxWeight = 0.0;
			for (int i = 0; i < weights.length; i++) {
				if (bucketMaxWeight < weights[i]) {
					bucketMaxWeight = weights[i];
					leaders[bucketIndex] = i;
				}
				if (i == (bucketIndex + 1) * bucketSize) {
					bucketIndex++;
					bucketMaxWeight = 0.0;
				}
			}

			Arrays.sort(leaders, comparator);
			result[0] = leaders[0];
			double maxWeight = weights[leaders[0]];
			int regime = 1;
			for (int i = 1; i < leaders.length && regime < maxRegimes; i++) {
				int leader = leaders[i];
				double ratio = weights[leaders[i]] / maxWeight;
				// we have sorted the leaders, so there is no point to continue
				if (ratio < 0.6)
					break;
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
			if (regime < maxRegimes)
				result = Arrays.copyOfRange(result, 0, regime);
			for (int i = 0; i < result.length; i++)
				result[i] = xs[(int) result[i]];
		}

		return result;
	}

}
