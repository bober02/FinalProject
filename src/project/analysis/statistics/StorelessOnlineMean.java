package project.analysis.statistics;

public class StorelessOnlineMean implements Statistic {

	/** Count of values that have been added */
	protected long n;

	/** First moment of values that have been added */
	protected double m1;

	/**
	 * Deviation of most recently added value from previous first moment.
	 * Retained to prevent repeated computation in higher order moments.
	 */
	protected double dev;

	/**
	 * Deviation of most recently added value from previous first moment,
	 * normalized by previous sample size. Retained to prevent repeated
	 * computation in higher order moments
	 */
	protected double nDev;

	/**
	 * Create a FirstMoment instance
	 */
	public StorelessOnlineMean() {
		n = 0;
		m1 = Double.NaN;
		dev = Double.NaN;
		nDev = Double.NaN;
	}

	@Override
	public void increment(final double x) {
		if (n == 0) {
			m1 = 0.0;
		}
		n++;
		double n0 = n;
		dev = x - m1;
		nDev = dev / n0;
		m1 += nDev;
	}

	/**
	 * This method removes given value from the overall statistic. I assume that
	 * the value WAS previously added to the set mainly: Given mean value y, if set X containing n
	 * values of x is added using increment and then all x's from that set are
	 * removed using remove(), then we will end up with y. These operations can
	 * be interleaved, but with an assertion that we always add an element
	 * before removing it. This operation will decrease number of elements by
	 * one.
	 * 
	 * @param x
	 *            - value to be removed
	 * @throws ArithmeticException
	 *             - thrown when we try to remove element when n=0
	 */
	//Made public for the sake of testing possibilities. Should really be protected.
	public void remove(final double x) throws ArithmeticException {
		if (n == 0) {
			throw new ArithmeticException("Cannot remove an element when element count of Mean is 0!");
		}
		n--;
		if(n == 0)
			clear();
		else{
			m1 += (m1 - x)/n;
			dev =  x - m1;
			nDev = dev/(n+1);
		}
	}


	@Override
	public void clear() {
		m1 = Double.NaN;
		n = 0;
		dev = Double.NaN;
		nDev = Double.NaN;
	}


	@Override
	public double getResult() {
		return m1;
	}

	@Override
	public long getN() {
		return n;
	}

}
