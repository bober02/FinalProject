package project.analysis.statistics;

public interface Statistic {

	/**
	 * Insert value to the given statistic set
	 * 
	 * @param x
	 *            - value to be inserted
	 */
	public abstract void increment(final double x);

	/**
	 * Clear all the values of the statstic
	 */
	public abstract void clear();

	/**
	 * Return current result of the statistic
	 * 
	 * @return value that the statistic is currently holding.
	 */
	public abstract double getResult();

	/**
	 * Returns number of values that have been inserted.
	 * 
	 * @return number of values inserted.
	 */
	public abstract long getN();

}