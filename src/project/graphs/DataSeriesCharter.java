package project.graphs;

public interface DataSeriesCharter extends SeriesCharter{

	/**
	 * Inserts set of (x,y) value pairs as a separate series, specified as two
	 * separate arrays
	 * 
	 * @param xs
	 *            - array of x values
	 * @param ys
	 *            - array of y values
	 * @throws IllegalArgumentException
	 *             - if the lists are not of the same length
	 */
	void addSeries(Comparable<?> seriesKey, double[] xs, double[] ys) throws IllegalArgumentException;

	/**
	 * Inserts set of (x,y) value pairs as a separate series, specified as two
	 * dimensional. This matrix can come in two different dimensions: 2xN or
	 * Nx2, denoting that we either specify vector of x's and y's to be row or
	 * column vectors.
	 * 
	 * @param seriesKey
	 *            - key of the series
	 * @param values
	 *            - matrix of values to be inserted
	 * @param rowValues
	 *            - this flag specifies whether our matrix is in row or column
	 *            form
	 */
	void addSeries(Comparable<?> seriesKey, double[][] values, boolean rowValues);

}
