package project.analysis.detrend;

import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * This class finds a linear trend in the data set and then removes it by
 * subtracting the trend line
 * 
 */
public class RegressionDetrender implements Detrender {

	@Override
	public double[] detrend(double[] xs, double[] ys) {
		if (xs.length != ys.length)
			throw new IllegalArgumentException("The x and y has to of same length.");
		double[] result = new double[ys.length];
		SimpleRegression regressor = new SimpleRegression();
		for (int i = 0; i < xs.length; i++) {
			regressor.addData(xs[i], ys[i]);
		}

		double slope = regressor.getSlope();
		double intercept = regressor.getIntercept();

		for (int i = 0; i < xs.length; i++) {
			result[i] = ys[i];
			result[i] -= intercept;
			result[i] -= (xs[i] * slope);
		}
		return result;
	}

}
