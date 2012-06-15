package project.datafeed;

public interface DataFeed {

	/**
	 * Returns next value coming from the database or null if no more data to
	 * process. This returns the COLUMN format of data
	 * 
	 * @return array of data values or null if no pending data
	 * @throws DataFeedException
	 */
	double[] getNextValue() throws DataFeedException;

	/**
	 * Gets all REMAINGING values for this data feed. This returns the COLUMN
	 * format of data
	 * 
	 * @return table of data values
	 * @throws DataFeedException
	 */
	double[][] getAllValues() throws DataFeedException;

	/**
	 * Resets the data feed so that the feed can be restarted and processed
	 * again.
	 * 
	 * @return true if the reset could be performed
	 */
	boolean reset();
}
