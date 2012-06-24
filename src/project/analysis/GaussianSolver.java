package project.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.FastMath;

public class GaussianSolver extends AbstractRegimeSolver {

	private Measures measures;
	private ChiSquaredDistribution chi;
	private SortedSet<Integer> markers;
	private Variance leftVariance;
	private Variance rightVariance;
	private double confidence;

	public GaussianSolver() {
		measures = new Measures();
		leftVariance = new Variance(false);
		rightVariance = new Variance(false);
		markers = new TreeSet<Integer>();
		chi = new ChiSquaredDistribution(2);
		confidence = 0.001;
	}

	private void reset() {
		markers.clear();
		resetVariances();
	}

	private void resetVariances() {
		leftVariance.clear();
		rightVariance.clear();
	}

	@Override
	public double[] solve(double[] xs, double[] ys) {
		if (xs.length != ys.length)
			throw new IllegalArgumentException("Lengths of data must be the same: XS: " + xs.length + "  Ys: " + ys.length);
		double L0 = Double.NaN;
		int length = ys.length;
		// reduce that to just keeping score table; for left simply add to the
		// score, and right will come later and adds itself
		double[] logLikelihoods = new double[length];
		for (int i = 0; i < length; i++) {
			logLikelihoods[i] = Double.NaN;
			leftVariance.increment(ys[i]);
		}
		// initial L0 measure
		L0 = -length / 2 * FastMath.log(leftVariance.getResult());

		List<Double> previousLikelihoods = new ArrayList<Double>();
		previousLikelihoods.add(L0);
		markers.add(length);
		boolean finish = false;
		while (!finish) {
			int maxMarker = -1;
			measures.reset();
			int begin = 0;
			int markerIndex = 0;
			List<Integer> l = new LinkedList<Integer>();
			for (Integer marker : markers) {
				resetVariances();
				double segmentMeasure = previousLikelihoods.get(markerIndex);
				double otherSegmentsLikelihood = L0 - segmentMeasure;
				// mark unimportant data point
				leftVariance.increment(ys[begin]);
				logLikelihoods[marker - 1] = logLikelihoods[begin] = Double.NEGATIVE_INFINITY;
				for (int i = begin + 1; i < marker; i++) {
					leftVariance.increment(ys[i]);
					int rightIndex = marker - i + begin;
					rightVariance.increment(ys[rightIndex]);

					double variance = leftVariance.getResult();
					boolean success = updateMeasures(i, (i + 1) - begin, logLikelihoods, variance, L0, otherSegmentsLikelihood, true);
					if (success) {
						maxMarker = markerIndex;
						l.add(i);
					}

					variance = rightVariance.getResult();
					success = updateMeasures(rightIndex - 1, i - begin, logLikelihoods, variance, L0, otherSegmentsLikelihood, false);
					if (success) {
						maxMarker = markerIndex;
						l.add(rightIndex - 1);
					}
				}
				begin = marker + 1;
				markerIndex++;
			}
			finish = measures.getMaxIndex() == -1;
			if (!finish) {
				markers.add(measures.getMaxIndex());
				L0 = measures.getMaxLikelihood();
				previousLikelihoods.remove(maxMarker);
				previousLikelihoods.add(maxMarker, measures.getMaxRightLikelihood());
				previousLikelihoods.add(maxMarker, measures.getMaxLeftLikelihood());
			}
		}
		double[] res = new double[markers.size() - 1];
		int i = 0;
		for (Integer marker : markers) {
			double diff = Double.MAX_VALUE;
			// compare against all previous regimes
			for (int k = 0; k < i; k++) {
				diff = FastMath.min(diff, FastMath.abs(res[k] - marker));
			}
			if (marker < length && diff > 0.05 * length) {
				res[i] = marker;
				i++;
			}
		}
		res = Arrays.copyOfRange(res, 0, i);
		double[] result = new double[res.length + 1];
		for (int k = 0; k < res.length; k++)
			result[k] = xs[(int) res[k]];
		result[result.length - 1] = xs[length - 1] + 1;
		Arrays.sort(result);
		reset();
		return result;
	}

	private boolean updateMeasures(int index, int n, double[] logLikelihoods, double variance, double L0, double otherSegmentsLikelihood,
			boolean fromLeft) {
		boolean success = false;
		// if variance is 0.0, our measure would be infinity (best),
		// as log(0) = -inf, which is not what we want
		// Highly unlikely in real data so ignore it
		if (variance == 0) {
			// reset
			logLikelihoods[index] = Double.NEGATIVE_INFINITY;
		}
		else {
			double currLikelihood = logLikelihoods[index];
			if (Double.isNaN(currLikelihood)) {
				logLikelihoods[index] = -n / 2.0 * FastMath.log(variance);
			}
			else {
				double additionalLikelihood = -n / 2.0 * FastMath.log(variance);

				double L1 = additionalLikelihood + currLikelihood + otherSegmentsLikelihood;
				double measure = 2 * (L1 - L0);
				double correctedConfidence = confidence / logLikelihoods.length;
				double pVal = 1 - chi.cumulativeProbability(measure);
				if (pVal < correctedConfidence) {
					if (measure > measures.getMaxChiMeasure()) {
						success = true;
						measures.setMaxIndex(index);
						measures.setMaxChiMeasure(measure);
						measures.setMaxLikelihood(L1);
						if (fromLeft) {
							measures.setMaxLeftLikelihood(additionalLikelihood);
							measures.setMaxRightLikelihood(currLikelihood);
						}
						else {
							measures.setMaxLeftLikelihood(currLikelihood);
							measures.setMaxRightLikelihood(additionalLikelihood);
						}
					}
				}
				// reset for next run
				logLikelihoods[index] = Double.NaN;
			}
		}
		return success;
	}

	private static class Measures {
		private double maxChiMeasure = Double.MIN_VALUE;
		private double maxLeftLikelihood = Double.MIN_VALUE;
		private double maxRightLikelihood = Double.MIN_VALUE;
		private double maxLikelihood = Double.MIN_VALUE;
		private int maxIndex = -1;

		public void reset() {
			maxChiMeasure = Double.MIN_VALUE;
			maxLeftLikelihood = Double.MIN_VALUE;
			maxRightLikelihood = Double.MIN_VALUE;
			maxLikelihood = Double.MIN_VALUE;
			maxIndex = -1;
		}

		public int getMaxIndex() {
			return maxIndex;
		}

		public void setMaxIndex(int maxIndex) {
			this.maxIndex = maxIndex;
		}

		public double getMaxChiMeasure() {
			return maxChiMeasure;
		}

		public void setMaxChiMeasure(double maxChiMeasure) {
			this.maxChiMeasure = maxChiMeasure;
		}

		public double getMaxLeftLikelihood() {
			return maxLeftLikelihood;
		}

		public void setMaxLeftLikelihood(double maxLeftLikelihood) {
			this.maxLeftLikelihood = maxLeftLikelihood;
		}

		public double getMaxRightLikelihood() {
			return maxRightLikelihood;
		}

		public void setMaxRightLikelihood(double maxRightLikelihood) {
			this.maxRightLikelihood = maxRightLikelihood;
		}

		public double getMaxLikelihood() {
			return maxLikelihood;
		}

		public void setMaxLikelihood(double maxLikelihood) {
			this.maxLikelihood = maxLikelihood;
		}

		@Override
		public String toString() {
			return "MaxIndex: " + maxIndex + ", MaxLikelihood: " + maxLikelihood + ", leftLikelihood: " + maxLeftLikelihood + ", rightLikelihood: "
					+ maxRightLikelihood;
		}

	}

}