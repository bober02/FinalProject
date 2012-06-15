package project.analysis;

import project.analysis.detrend.Detrender;
import project.analysis.detrend.DetrenderGraph;
import project.datafeed.DataFeed;
import project.datafeed.DataFeedException;
import project.graphs.DataSeriesCharter;
import project.io.Logger;
import project.utis.DataUtils;
import project.utis.Timer;

public class SolverGraph extends DetrenderGraph {

	private RegimeSolver solver;

	public SolverGraph(RegimeSolver solver, DataSeriesCharter charter, Logger log) {
		super(charter, log);
		this.solver = solver;
	}

	public void solveAndPlot(DataFeed df, String title, boolean plotOriginalData) {
		// I could pass data feed on, but that would generate two seaparate,
		// massive matrices in memory. Rather I create it once here
		try {
			double[][] values = DataUtils.transpose(df.getAllValues());
			solveAndPlot(values[0], values[1], title, plotOriginalData);
		} catch (DataFeedException e) {
			log.writeln(e.getMessage());
		}
	}

	public void solveAndPlot(double[] xs, double[] ys, String title, boolean plotOriginalData) {
		if (plotOriginalData) {
			charter.addSeries("Original Data", xs, ys);
		}
		double[] detrendYs = ys;
		if (!detrenders.isEmpty()) {
			for (Detrender d : detrenders) {
				detrendYs = d.detrend(xs, detrendYs);
			}
			charter.addSeries("Detrended Data", xs, detrendYs);
			charter.addRangeMarker(0d);
		}
		Timer t = new Timer();
		t.start();
		double[] res = solver.solve(xs, detrendYs);
		t.stop();
		System.out.println((double) t.getTimeElapsed() / 1000);
		for (double x : res) {
			charter.addDomainMarker(x);
		}
		charter.showChart(title, "Time (Days)", "Asset price");
	}
}
