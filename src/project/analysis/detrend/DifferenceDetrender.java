package project.analysis.detrend;

/**
 * Calculates single differencing (returns) of the asset prices.
 *
 */
public class DifferenceDetrender implements Detrender {

	@Override
	public double[] detrend(double[] xs, double[] ys) {
		if (xs.length != ys.length)
			throw new IllegalArgumentException("The x and y has to of same length.");
		double[] result = new double[ys.length];
		result[0] = 0.0;
		for (int i = 1; i < result.length; i++) {
			result[i] = ys[i] - ys[i - 1];
		}
		return result;
	}

}
