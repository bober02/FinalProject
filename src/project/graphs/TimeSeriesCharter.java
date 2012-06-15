package project.graphs;

import java.util.Date;

public interface TimeSeriesCharter extends SeriesCharter {

	/**
	 * Adds another time point, where date is the milliseconds UTC time.
	 * @param date - represented using java.util.Date
	 * @param y value
	 * @throws IllegalArgumentException when point is added without series beginning
	 */
	void addTimePoint(Date date, double y) throws IllegalArgumentException;
	
	/**
	 * Adds vertical marker at sepcified date
	 * @param date at which marker is set
	 */
	void addDateMarker(Date date);

}
