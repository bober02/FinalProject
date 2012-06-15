package project.graphs;

import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class UTCTimeSeriesCharter extends AbstractCharter implements TimeSeriesCharter {

	private TimeSeriesCollection seriesCollection;
	private TimeSeries timeSeries;
	private DateFormat format;

	public UTCTimeSeriesCharter() {
		super();
		seriesCollection = new TimeSeriesCollection();
	}

	public UTCTimeSeriesCharter(DateFormat format) {
		this();
		this.format = format;
	}

	@Override
	public void addTimePoint(Date date, double y) throws IllegalArgumentException {
		if (timeSeries == null)
			throw new IllegalArgumentException("Series has not been instantiated!");
		if (isDisplayed())
			throw new IllegalArgumentException("Cannot modify data while the chart is open!");
		addTimePoint(new FixedMillisecond(date), y);
	}

	@Override
	public void addDataPoint(double date, double y) throws IllegalArgumentException {
		if (timeSeries == null)
			throw new IllegalArgumentException("Series has not been instantiated!");
		if (isDisplayed())
			throw new IllegalArgumentException("Cannot modify data while the chart is open!");
		addTimePoint(new FixedMillisecond((long) date), y);
	}

	private void addTimePoint(RegularTimePeriod date, double y) {
		if (!allowDuplicates)
			timeSeries.addOrUpdate(date, y);
		else
			timeSeries.add(date, y);
	}

	@Override
	public void addDateMarker(Date date) {
		addDomainMarker(date.getTime());
	}

	@Override
	protected void generateNewSeries(Comparable<?> seriesKey) {
		timeSeries = new TimeSeries(seriesKey);
		seriesCollection.addSeries(timeSeries);
	}

	@Override
	protected JFreeChart generateAndSetupChart(String title, String xAxis, String yAxis) {
		JFreeChart chart = ChartFactory.createTimeSeriesChart(title, xAxis, yAxis, seriesCollection, true, true, false);
		if (format != null) {
			DateAxis axis = (DateAxis) chart.getXYPlot().getDomainAxis();
			axis.setDateFormatOverride(format);
		}
		return chart;
	}

	@Override
	protected void onWindowClose(WindowEvent e) {
		super.onWindowClose(e);
		seriesCollection.removeAllSeries();
	}

	@Override
	public boolean isEmpty() {
		return seriesCollection.getSeriesCount() == 0 && (timeSeries == null || timeSeries.isEmpty());
	}

	@Override
	public void addDataPoint(Comparable<?> seriesKey, double x, double y) throws IllegalArgumentException {
		TimeSeries series = seriesCollection.getSeries(seriesKey);
		if (!allowDuplicates)
			series.addOrUpdate(new FixedMillisecond((long) x), y);
		else
			series.add(new FixedMillisecond((long) x), y);

	}
}
