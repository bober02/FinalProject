package project.graphs;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.function.Function2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import project.utis.DataUtils;

public class DoubleSeriesCharter extends AbstractCharter implements DataSeriesCharter {

	private XYSeriesCollection pointSeriesCollection;
	private XYSeries pointSeries;
	private DefaultXYDataset matrixDataSet;
	private List<XYDataset> functionSets;

	public DoubleSeriesCharter() {
		super();
		pointSeriesCollection = new XYSeriesCollection();
		matrixDataSet = new DefaultXYDataset();
		functionSets = new ArrayList<XYDataset>();
	}

	@Override
	public void addDataPoint(double x, double y) throws IllegalArgumentException {
		if (pointSeries == null)
			throw new IllegalArgumentException("Series has not been instantiated!");
		if (isDisplayed())
			throw new IllegalArgumentException("Cannot modify data while the chart is open!");
		pointSeries.add(x, y);
	}
	
	public void addFunction(Comparable<?> seriesKey, Function2D f, double start, double end, int samples) {
		functionSets.add(DatasetUtilities.sampleFunction2D(f, start, end, samples, seriesKey));
	}

	@Override
	public void addDataPoint(Comparable<?> seriesKey, double x, double y) throws IllegalArgumentException {
		pointSeriesCollection.getSeries(seriesKey).add(x, y);
	}

	@Override
	public void addSeries(Comparable<?> seriesKey, double[] xs, double[] ys) throws IllegalArgumentException {
		if (xs.length != ys.length)
			throw new IllegalArgumentException("Size of domain ser must be equal to the size of values set size");
		if (isDisplayed())
			throw new IllegalArgumentException("Cannot modify data while the chart is open!");
		matrixDataSet.addSeries(seriesKey, new double[][] { xs, ys });
	}

	@Override
	public void addSeries(Comparable<?> seriesKey, double[][] values, boolean rowValues) {
		if (isDisplayed())
			throw new IllegalArgumentException("Cannot modify data while the chart is open!");
		if (values == null)
			throw new IllegalArgumentException("Values cannot be null!");
		double[][] newValues;
		if (!rowValues) {
			newValues = DataUtils.transpose(values);
		} else
			newValues = values;
		matrixDataSet.addSeries(seriesKey, newValues);
	}


	@Override
	protected JFreeChart generateAndSetupChart(String title, String xAxis, String yAxis) {
		boolean hasPointData = pointSeriesCollection.getSeriesCount() > 0;
		JFreeChart chart;
		if (hasPointData) {
			chart = ChartFactory.createXYLineChart(title, xAxis, yAxis, pointSeriesCollection,
					PlotOrientation.VERTICAL, true, true, false);
			if (matrixDataSet.getSeriesCount() > 0) {
				XYPlot plot = (XYPlot) chart.getPlot();
				plot.setDataset(1, matrixDataSet);
				plot.setRenderer(1, new StandardXYItemRenderer());

			}
		} else {
			chart = ChartFactory.createXYLineChart(title, xAxis, yAxis, matrixDataSet,
					PlotOrientation.VERTICAL, true, true, false);
			XYPlot plot = (XYPlot) chart.getPlot();
			if (!functionSets.isEmpty()) {
				plot.setForegroundAlpha(0.5f);
				int index = 1;
				for (XYDataset set : functionSets) {
					plot.setDataset(index, set);
					plot.setRenderer(index, new StandardXYItemRenderer());
					index++;
				}
			}
		}
		return chart;
	}

	@Override
	protected void generateNewSeries(Comparable<?> seriesKey) {
		pointSeries = new XYSeries(seriesKey, false, allowDuplicates);
		pointSeriesCollection.addSeries(pointSeries);
	}

	@Override
	protected void onWindowClose(WindowEvent e) {
		super.onWindowClose(e);
		pointSeriesCollection.removeAllSeries();
		matrixDataSet = new DefaultXYDataset();
	}

	@Override
	public boolean isEmpty() {
		return matrixDataSet.getSeriesCount() == 0 && pointSeriesCollection.getSeriesCount() == 0
				&& (pointSeries == null || pointSeries.isEmpty());
	}

}
