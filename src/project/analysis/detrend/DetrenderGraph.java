package project.analysis.detrend;

import java.util.LinkedHashSet;

import project.datafeed.DataFeed;
import project.datafeed.DataFeedException;
import project.graphs.DataSeriesCharter;
import project.io.Logger;
import project.utis.DataUtils;

public class DetrenderGraph {

	protected DataSeriesCharter charter;
	protected LinkedHashSet<Detrender> detrenders;
	protected Logger log;

	public DetrenderGraph(DataSeriesCharter charter, Logger log) {
		this.charter = charter;
		detrenders = new LinkedHashSet<Detrender>();
		this.log = log;
	}

	public DetrenderGraph(DataSeriesCharter charter, Logger log, Detrender... detrends) {
		this(charter, log);
		if (detrends.length == 0)
			throw new IllegalArgumentException("Must pass at least one detrender!");
		for (Detrender d : detrends)
			detrenders.add(d);
	}

	public void addDetrender(Detrender d) {
		detrenders.add(d);
	}

	public void removeDetrender(Detrender d) {
		detrenders.remove(d);
	}

	public void detrendAndPlot(DataFeed df, String title, boolean plotOriginalData, boolean plotTrendLine) {
		try {
			double[][] values = DataUtils.transpose(df.getAllValues());
			detrendAndPlot(values[0], values[1], title, plotOriginalData, plotTrendLine);
		} catch (DataFeedException e) {
			log.writeln(e.getMessage());
		}
	}

	public void detrendAndPlot(double[] xs, double[] ys, String title, boolean plotOriginalData, boolean plotTrendLine) {
		if (plotOriginalData) {
			charter.addSeries("Original Data", xs, ys);
		}
		if (!detrenders.isEmpty()) {
			double[] detrendYs = ys;
			for (Detrender d : detrenders) {
				detrendYs = d.detrend(xs, detrendYs);
			}
			charter.addSeries("Detrended Data", xs, detrendYs);
			charter.addRangeMarker(0d);
			if (plotTrendLine) {
				double[] trendLine = new double[ys.length];
				for (int i = 0; i < ys.length; i++) {
					trendLine[i] = ys[i] - detrendYs[i];
				}
				charter.addSeries("Trend line", xs, trendLine);
			}
		}
		charter.showChart(title, "Time (Days)", "Asset price");
	}

}
