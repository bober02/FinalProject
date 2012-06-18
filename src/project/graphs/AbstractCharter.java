package project.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;

import project.utis.ColorUtils;

public abstract class AbstractCharter implements SeriesCharter {

	private JFreeChart chart;
	private Set<Marker> domainMarkers;
	private Set<Marker> rangeMarkers;
	protected boolean allowDuplicates;

	public AbstractCharter() {
		domainMarkers = new HashSet<Marker>();
		rangeMarkers = new HashSet<Marker>();
	}

	@Override
	public abstract void addDataPoint(double x, double y) throws IllegalArgumentException;

	@Override
	public void beginNewPointSeries(Comparable<?> seriesKey, boolean allowDuplicates) {
		if (chart != null)
			throw new IllegalArgumentException("Cannot modify data while the chart is open!");
		this.allowDuplicates = allowDuplicates;
		generateNewSeries(seriesKey);
	}

	@Override
	public void addDomainMarker(double xValue) {
		Stroke stroke = new BasicStroke(2.5f);
		Marker newMarker = new ValueMarker(xValue);

		// Possibly set that later

		// newMarker.setStroke(stroke);
		newMarker.setPaint(Color.black);
		if (chart != null) {
			XYPlot plot = (XYPlot) chart.getPlot();
			plot.addDomainMarker(newMarker);
		} else
			domainMarkers.add(newMarker);
	}

	@Override
	public void addRangeMarker(double xValue) {
		Marker newMarker = new ValueMarker(xValue);
		newMarker.setPaint(Color.black);
		if (chart != null) {
			XYPlot plot = (XYPlot) chart.getPlot();
			plot.addRangeMarker(newMarker);
		} else
			rangeMarkers.add(newMarker);
	}

	@Override
	public void showChart(String title, String xAxis, String yAxis) {
		chart = generateAndSetupChart(title, xAxis, yAxis);
		XYPlot plot = (XYPlot) chart.getPlot();
		for (Marker m : domainMarkers) {
			plot.addDomainMarker(m);
		}
		for (Marker m : rangeMarkers) {
			plot.addRangeMarker(m);
		}
		setRenderingColours(plot, true);
		setFonts(plot, RectangleEdge.BOTTOM);
		//setPlotSteps(plot, 5, 10);
		//plot.setForegroundAlpha(0.6f);
		//chart.removeLegend();
		
		ChartFrame frame = new ChartFrame("Chart window", chart);
		frame.setSize(1500, 1400);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				onWindowClose(e);
			}

		});
		frame.setVisible(true);
	}

	private void setFonts(XYPlot plot, RectangleEdge legendPosition) {
		
		LegendTitle legendTitle = chart.getLegend();
		legendTitle.setPosition(legendPosition);
		legendTitle.setItemFont(new Font("Arial", Font.PLAIN, 28));

		Font font = new Font("Arial", Font.PLAIN, 20);
		plot.getDomainAxis().setTickLabelFont(font);
		plot.getRangeAxis().setTickLabelFont(font);


		font = new Font("Arial", Font.PLAIN, 24);
		plot.getDomainAxis().setLabelFont(font);
		plot.getRangeAxis().setLabelFont(font);
	}

	private void setRenderingColours(XYPlot plot, boolean setBigStroke) {
		int paintIndex = 0;
		int seriesIndex = 0;
		Paint[] colours = ColorUtils.getColorList();
		XYItemRenderer renderer = null;
		for (int index = 0; index < plot.getDatasetCount(); index++) {
			XYItemRenderer next = plot.getRenderer(index);
			if (next != null) {
				renderer = next;
				seriesIndex = 0;
			}
			for (int i = 0; i < plot.getDataset(index).getSeriesCount(); i++) {
				renderer.setSeriesPaint(seriesIndex, colours[paintIndex]);
				if(setBigStroke)
					renderer.setSeriesStroke(seriesIndex, new BasicStroke(1.2f));
				seriesIndex++;
				paintIndex++;
			}
		}

	}

	protected void onWindowClose(WindowEvent e) {
		chart = null;
		rangeMarkers.clear();
		domainMarkers.clear();
	}

	@Override
	public boolean isDisplayed() {
		return chart != null;
	}

	protected abstract void generateNewSeries(Comparable<?> seriesKey);

	protected abstract JFreeChart generateAndSetupChart(String title, String xAxis, String yAxis);

}
