package project.analysis.statistics;

/**
 * This class implements my online-variance with window statistic as described in
 * the report. It holds last N inserted values and for each new data point it
 * "removes" the oldest one from the set and then adds the new value. It
 * maintains constant time performance for each new value.
 * 
 */
public class WindowOnlineVariance extends StorelessOnlineMean {

	// bias corrected returns sample variance (N-1)
	private boolean isBiasCorrected;
	private double m2;
	private double[] values;
	private int index;
	private int windowSize;

	public WindowOnlineVariance() {
		this(true);
	}

	public WindowOnlineVariance(boolean isBiasCorrected) {
		this.isBiasCorrected = isBiasCorrected;
		values = null;
		index = -1;
	}

	public WindowOnlineVariance(int windowSize) {
		this(windowSize, true);
	}

	public WindowOnlineVariance(int windowSize, boolean isBiasCorrected) {
		this.isBiasCorrected = isBiasCorrected;
		m2 = Double.NaN;
		index = 0;
		this.windowSize = windowSize;
		values = new double[windowSize];
		resetWindow();
	}

	public void setBias(boolean isBiasCorrected) {
		this.isBiasCorrected = isBiasCorrected;
	}

	@Override
	public void increment(final double x) {
		if (n < 1) {
			m1 = m2 = 0.0;
		}
		if (values != null) {
			if (!Double.isNaN(values[index])) {
				double val = values[index];
				// remove from mean and calculate deviation statistics
				super.remove(val);
				m2 -= ((double) n) * dev * nDev;
				// double precision which can cause minimal negative
				if (m2 < 0)
					m2 = 0;
			}
			values[index] = x;
			if (++index == windowSize)
				index = 0;
		}
		super.increment(x);
		m2 += ((double) n - 1) * dev * nDev;
	}

	@Override
	public void clear() {
		super.clear();
		m2 = Double.NaN;
		resetWindow();
	}

	@Override
	public double getResult() {
		if (n <= 1)
			return m2;
		else {
			if (isBiasCorrected) {
				return m2 / (n - 1d);
			}
			else {
				return m2 / n;
			}
		}
	}

	private void resetWindow() {
		for (int i = 0; i < windowSize; i++) {
			values[i] = Double.NaN;
		}
	}

}
