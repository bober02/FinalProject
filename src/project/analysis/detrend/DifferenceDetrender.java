package project.analysis.detrend;


public class DifferenceDetrender implements Detrender {

	@Override
	public double[] detrend(double[] xs, double[] ys) {
		if (xs.length != ys.length)
			throw new IllegalArgumentException("The x and y has to of same length.");
		double[] result = new double[ys.length];
		result[0] = 0.0;
		for(int i = 1; i < result.length; i++){
			result[i] = ys[i] - ys[i-1];
		}
		return result;
	}

	public boolean equals(Object o) {
		return false;
	}
}
