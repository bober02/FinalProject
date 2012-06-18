package project.analysis.detrend;

public class Smoother implements Detrender {

	private static final int DEFAULT_SPAN = 5;
	private int span;

	public Smoother() {
		this(DEFAULT_SPAN);
	}

	public Smoother(int span) {
		setSpan(span);
	}

	public void setSpan(int span) {
		if (span % 2 == 0) {
			span--;
		}
		this.span = span;
	}

	private double[] smooth(double[] ys) {
		double[] result = new double[ys.length];
		for (int i = 0; i < ys.length; i++) {
			int begin = Math.max(0, i - span);
			int end = Math.min(ys.length, i + (i - begin) + 1);
			double mean = 0;
			for (int j = begin; j < end; j++)
				mean += ys[j];
			result[i] = mean / (end - begin);
		}
		return result;
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
