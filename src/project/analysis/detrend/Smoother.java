package project.analysis.detrend;

import project.analysis.statistics.Statistic;
import project.analysis.statistics.WindowOnlineMean;

public class Smoother implements Detrender {

	private static final int DEFAULT_SPAN = 5;

	private Statistic mean;

	public Smoother() {
		this(DEFAULT_SPAN);
	}

	public Smoother(int span) {
		mean = new WindowOnlineMean(span);
	}

	public void setSpan(int span) {
		if (span % 2 == 0) {
			span--;
		}
		mean = new WindowOnlineMean(span);
	}

	public double[] smooth(double[] ys) {
		double[] res = new double[ys.length];
		for (int i = 0; i < ys.length; i++) {
			mean.increment(ys[i]);
			res[i] = mean.getResult();
		}
		mean.clear();
		return res;
	}

	@Override
	public double[] detrend(double[] xs, double[] ys) {
		return smooth(ys);
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		return o.getClass() == this.getClass();
	}

}
