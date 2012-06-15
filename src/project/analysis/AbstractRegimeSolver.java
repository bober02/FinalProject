package project.analysis;

import project.datafeed.DataFeed;
import project.datafeed.DataFeedException;
import project.utis.DataUtils;

public abstract class AbstractRegimeSolver implements RegimeSolver {

	public abstract double[] solve(double[] xs, double[] ys);

	@Override
	public double[] solve(DataFeed df) {
		try {
			double[][] data = df.getAllValues();
			double[][] transData = DataUtils.transpose(data);
			return solve(transData[0], transData[1]);
		} catch (DataFeedException e) {
			return new double[0];
		}
	}
}
