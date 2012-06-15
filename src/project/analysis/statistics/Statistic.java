package project.analysis.statistics;

public interface Statistic {

	public abstract void increment(final double x);

	public abstract void clear();

	public abstract double getResult();

	public abstract long getN();

}