package project.analysis;

import project.datafeed.DataFeed;
import project.datafeed.DataFeedException;
import project.utis.DataUtils;

public abstract class AbstractRegimeSolver implements RegimeSolver {

	public abstract Regime[] solve(double[] xs, double[] ys);

	@Override
	public Regime[] solve(DataFeed df) {
		try {
			double[][] data = df.getAllValues();
			double[][] transData = DataUtils.transpose(data);
			return solve(transData[0], transData[1]);
		} catch (DataFeedException e) {
			return new Regime[0];
		}
	}
}
