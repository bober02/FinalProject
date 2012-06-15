package project.analysis.detrend;


public interface Detrender {


	/**
	 * This function will de-trend the data using some mathematical technique.
	 * @param xs values
	 * @param ys values
	 * @return y values after removing the trend line. Original data will be unspoiled.
	 */
	double[] detrend(double[] xs, double[] ys);

}