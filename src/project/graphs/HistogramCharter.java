package project.graphs;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.function.Function2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

import project.utis.ColorUtils;

public class HistogramCharter {

	private List<XYDataset> functionSets;
	private HistogramDataset dataset;

	public HistogramCharter() {
		functionSets = new ArrayList<XYDataset>();
		dataset = new HistogramDataset();
        dataset.setType(HistogramType.SCALE_AREA_TO_1);
	}

	public void addHistogram(Comparable<?> name, double[] values, int bins) {
		dataset.addSeries(name, values, bins);
	}

	public void addFunction(Comparable<?> seriesKey, Function2D f, double start, double end, int samples) {
		functionSets.add(DatasetUtilities.sampleFunction2D(f, start, end, samples, seriesKey));
	}

	public void showChart(String title, String xAxis, String yAxis) {
		JFreeChart chart = ChartFactory.createHistogram(title ,xAxis ,yAxis, dataset, PlotOrientation.VERTICAL, true, false, false);
		if (!functionSets.isEmpty()) {
			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setForegroundAlpha(0.5f);
			int index = 1;
			for (XYDataset set : functionSets) {
				plot.setDataset(index, set);
				plot.setRenderer(index, new StandardXYItemRenderer());
				index++;
			}
			setRenderingColours(plot);
			LegendTitle legendTitle = chart.getLegend(); 
			legendTitle.setPosition(RectangleEdge.RIGHT); 
			legendTitle.setItemFont(new Font("Arial", Font.PLAIN, 28));
			
			Font font = new Font("Arial", Font.PLAIN, 20);
			plot.getDomainAxis().setTickLabelFont(font);
			plot.getRangeAxis().setTickLabelFont(font);
			((NumberAxis)plot.getRangeAxis()).setTickUnit(new NumberTickUnit(0.1));
			((NumberAxis)plot.getDomainAxis()).setTickUnit(new NumberTickUnit(2));
			
			font = new Font("Arial", Font.PLAIN, 24);
			plot.getDomainAxis().setLabelFont(font);
			plot.getRangeAxis().setLabelFont(font);
		}
		
		ChartFrame frame = new ChartFrame("Chart window", chart);
		frame.setSize(1400, 1400);
		frame.setVisible(true);
	}
	
	private void setRenderingColours(XYPlot plot) {
		int paintIndex = 0;
		int seriesIndex = 0;
		Paint[] colours = ColorUtils.getColorList();
		XYItemRenderer renderer = null;
		for(int index = 0; index < plot.getDatasetCount(); index++){
			XYItemRenderer next = plot.getRenderer(index);
			if(next != null){
				renderer = next;
				seriesIndex = 0;
			}
			for(int i = 0; i < plot.getDataset(index).getSeriesCount(); i++ ){
				renderer.setSeriesPaint(seriesIndex, colours[paintIndex]);
				renderer.setSeriesStroke(seriesIndex, new BasicStroke(2.0f));
				seriesIndex++;
				paintIndex++;
			}
		}
	}
}
