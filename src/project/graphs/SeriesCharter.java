package project.graphs;

public interface SeriesCharter {
	/**
	 * Adds new point to the last began series
	 * 
	 * @param x
	 *            value
	 * @param y
	 *            value
	 * @throws IllegalArgumentException
	 */
	void addDataPoint(double x, double y) throws IllegalArgumentException;

	/**
	 * Adds new point to the specified series
	 * 
	 * @param seriesKey
	 *            - key of the series to insert point into
	 * @param x
	 *            value
	 * @param y
	 *            value
	 * @throws IllegalArgumentException
	 */
	void addDataPoint(Comparable<?> seriesKey, double x, double y) throws IllegalArgumentException;

	/**
	 * Saves all the points that we have currently added and begins a new series
	 * created by adding each separate point.
	 * 
	 * @param Key
	 *            of the new series
	 * @param allowDuplicates
	 *            specifies whether we can insert duplicated values
	 */

	void beginNewPointSeries(Comparable<?> seriesKey, boolean allowDuplicates);

	boolean isEmpty();

	/**
	 * Adds vertical marker at sepcified date
	 * 
	 * @param xValue
	 *            at which marker is set. It can represent a double value or
	 *            milliseconds since 1970, depending on the context
	 */
	void addDomainMarker(double xValue);

	/**
	 * Adds horizontal marker at sepcified y-value
	 * 
	 * @param yValue
	 *            at which the marker should be set
	 */
	void addRangeMarker(double xValue);

	/**
	 * Upon calling this method, the chart will be rendered to the user. This
	 * method will also save all unsaved points in teh point series and display
	 * them
	 * 
	 * @param title
	 *            of the
	 */
	void showChart(String title, String xAxis, String yAxis);

	/**
	 * Informs whether te current chart is being displyaed to the user. Only one
	 * chart is displayed at a time and no pints can be added unless the chart
	 * is closed.
	 * 
	 * @return true is the chart is set to be visible.
	 */
	boolean isDisplayed();
}
