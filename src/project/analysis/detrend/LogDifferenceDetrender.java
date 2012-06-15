package project.analysis.detrend;

import org.apache.commons.math3.util.FastMath;

public class LogDifferenceDetrender implements Detrender {

	@Override
	public double[] detrend(double[] xs, double[] ys) {
		if (xs.length != ys.length)
			throw new IllegalArgumentException("The x and y has to of same length.");
		double[] result = new double[ys.length];
		result[0] = 0.0;
		for(int i = 1; i < result.length; i++){
			result[i] = FastMath.log(ys[i]/ys[i-1]);
		}
		return result;
	}

	
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		return o.getClass() == this.getClass();
	}
}
