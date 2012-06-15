package project.analysis;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.util.FastMath;

import project.analysis.statistics.WindowOnlineVariance;
import project.utis.DescendingDoubleComparator;

public class StandardDeviationSolver extends AbstractRegimeSolver {

	private int windowSize;
	private WindowOnlineVariance variance;
	private RegimeStrategy strategy;
	private int maxRegimes;

	public StandardDeviationSolver() {
		strategy = RegimeStrategy.Buckets;
	}

	public StandardDeviationSolver(int windowSize, int maxRegimes) {
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
			throw new IllegalArgumentException("Lengths of data must be the same: XS: " + xs.length + "  Ys: "
					+ ys.length);
		if (variance == null) {
			if (variance == null) {
				windowSize = (int) Math.max(5, xs.length * 0.05);
				variance = new WindowOnlineVariance(windowSize);
			}
		}
		if (maxRegimes == 0)
			maxRegimes = 10;
		double[] stdLeft = new double[ys.length];
		double[] stdRight = new double[ys.length];
		stdLeft[0] = stdRight[ys.length - 1] = 0;

		for (int i = ys.length - 1; i > ys.length - 1 - windowSize && i >= 0; i--) {
			variance.increment(ys[i]);
			stdRight[i - 1] = FastMath.sqrt(variance.getResult());
		}
		variance.clear();
		for (int i = 0; i < windowSize && i < ys.length; i++) {
			variance.increment(ys[i]);
			stdLeft[i + 1] = FastMath.sqrt(variance.getResult());
		}
		for (int i = windowSize; i < ys.length - 1; i++) {
			variance.increment(ys[i]);
			double res = FastMath.sqrt(variance.getResult());
			stdLeft[i + 1] = res;
			int leftindex = i - windowSize;
			stdRight[leftindex] = res;
		}
		double[] weights = new double[ys.length];
		// gather indexes for sorting
		Integer[] indexes = new Integer[ys.length];
		int length = weights.length;
		double maxWeight = 0.0;
		for (int i = 0; i < length; i++) {
			indexes[i] = i;
			if (stdLeft[i] == 0 || stdRight[i] == 0) {
				weights[i] = 0;
			}
			else {
				int leftPoints = FastMath.min(i, windowSize);
				int rightPoints = FastMath.min(length - i - 1, windowSize);
				double diff = FastMath.abs(leftPoints - rightPoints);
				double k = 1.0 - diff / windowSize;
				double weight = k * FastMath.max(stdLeft[i] / stdRight[i], stdRight[i] / stdLeft[i]);
				maxWeight = weight > maxWeight ? weight : maxWeight;
				weights[i] = k * FastMath.max(stdLeft[i] / stdRight[i], stdRight[i] / stdLeft[i]);
			}
		}
		variance.clear();
		return getResult(xs, indexes, weights);
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
