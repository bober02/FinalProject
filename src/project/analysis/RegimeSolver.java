package project.analysis;

import project.datafeed.DataFeed;

public interface RegimeSolver {

	/**
	 * General method for all solvers to include. This performs the logic of
	 * analysis the data set and returns set of regimes up to max.
	 * 
	 * @param xs
	 *            - set of x values
	 * @param ys
	 *            - set of y values
	 * @param maxRegimes
	 *            - maximum number of regimes we consider. Must be in <0,
	 *            length(xs)>
	 * @return list of x data points which are regime switching points.
	 */
	double[] solve(double[] xs, double[] ys);

	/**
	 * General method for all solvers to include. This performs the logic of
	 * analysis the data set and returns set of regimes up to max.
	 * 
	 * @param df
	 *            - data feed that will provide the data
	 * @param maxRegimes
	 *            - maximum number of regimes we consider. Must be in <0,
	 *            length(xs)>
	 * @return list of x data points which are regime switching points.
	 */
	double[] solve(DataFeed df);

}
