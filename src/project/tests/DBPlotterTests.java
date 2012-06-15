package project.tests;

import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import project.datafeed.DataFeed;
import project.datafeed.DataFeedException;
import project.datafeed.DataFeedProvider;
import project.graphs.ChartingException;
import project.graphs.DBPlotter;
import project.graphs.DataSeriesCharter;
import project.graphs.TimeSeriesCharter;
import project.io.Logger;
import project.utis.DomainAxisType;

public class DBPlotterTests {

	// Tests for plotHigh and other methods could be added but they are
	// extremely similar to general test for data provided below

	private JUnit4Mockery context = new JUnit4Mockery();
	private DBPlotter plotter;
	private DataFeedProvider dataBuilder;
	private TimeSeriesCharter timeCharter;
	private DataSeriesCharter dataCharter;
	private Logger log;

	@Before
	public void setUp() {
		dataBuilder = context.mock(DataFeedProvider.class);
		timeCharter = context.mock(TimeSeriesCharter.class);
		dataCharter = context.mock(DataSeriesCharter.class);
		log = context.mock(Logger.class);
	}

	@Test
	public void forTableCallsDataBuilderForTableMethod() throws IOException {
		final String table = "Test";

		context.checking(new Expectations() {
			{
				oneOf(dataBuilder).forTable(table);
			}
		});
		plotter = new DBPlotter(dataBuilder, timeCharter, dataCharter, log);
		plotter.forTable(table);

		context.assertIsSatisfied();
	}

	@Test
	public void whenBothChartersAreEmptyShowChartIsNotCalled() throws IOException {
		final String table = "Test";

		context.checking(new Expectations() {
			{
				oneOf(timeCharter).isEmpty();
				will(returnValue(true)); 
				oneOf(dataCharter).isEmpty();
				will(returnValue(true));
			}
		});
		plotter = new DBPlotter(dataBuilder, timeCharter, dataCharter, log);
		plotter.showGraph(table);

		context.assertIsSatisfied();
	}

	@Test
	public void whenBothChartersAreNonEmptyShowChartInBothIsCalled() throws IOException {
		final String table = "Test";

		context.checking(new Expectations() {
			{
				oneOf(timeCharter).isEmpty();
				will(returnValue(false));
				oneOf(dataCharter).isEmpty();
				will(returnValue(false));
				oneOf(timeCharter).showChart(with(any(String.class)), with(any(String.class)), with(any(String.class)));
				oneOf(dataCharter).showChart(with(any(String.class)), with(any(String.class)), with(any(String.class)));
			}
		});
		plotter = new DBPlotter(dataBuilder, timeCharter, dataCharter, log);
		plotter.showGraph(table);

		context.assertIsSatisfied();
	}

	@Test(expected = ChartingException.class)
	public void whenNoTableIsSetChartingExceptionIsThrown() {
		context.checking(new Expectations() {
			{
				oneOf(dataBuilder).includeSettle();
			}
		});
		plotter = new DBPlotter(dataBuilder, timeCharter, dataCharter, log);
		plotter.plotSettle();

		context.assertIsSatisfied();
	}

	@Test
	public void whenDataFeedThrowsExceptionItIsLogged() throws DataFeedException {
		final DataFeed df = context.mock(DataFeed.class);
		final String table = "Test";

		context.checking(new Expectations() {
			{
				allowing(dataBuilder).forTable(table);
				oneOf(dataBuilder).includeSettle();
				oneOf(dataBuilder).includeDays();
				oneOf(dataBuilder).reset();
				oneOf(dataBuilder).build();
				will(returnValue(df));
				oneOf(df).getAllValues();
				will(throwException(new DataFeedException("Test")));
				oneOf(log).writeln(table);
			}
		});
		plotter = new DBPlotter(dataBuilder, timeCharter, dataCharter, log);
		plotter.forTable(table).plotSettle();

		context.assertIsSatisfied();
	}

	@Test
	public void whenCorrectDataIsReceivedSeriesIsAdded() throws DataFeedException {
		final DataFeed df = context.mock(DataFeed.class);
		final String table = "Test";

		context.checking(new Expectations() {
			{
				allowing(dataBuilder).forTable(table);
				oneOf(dataBuilder).includeSettle();
				oneOf(dataBuilder).includeDays();
				oneOf(dataBuilder).reset();
				oneOf(dataBuilder).build();
				will(returnValue(df));
				oneOf(df).getAllValues();
				will(returnValue(new double[][] { new double[] { 1, 2 } }));
				oneOf(dataCharter).addSeries(with(any(Comparable.class)), with(any(double[][].class)),
						with(any(Boolean.class)));
			}
		});
		plotter = new DBPlotter(dataBuilder, timeCharter, dataCharter, log);
		plotter.forTable(table).plotSettle();

		context.assertIsSatisfied();
	}

	@Test
	public void whenDateAxisUsedPointsAreAddedToTimeSeries() throws DataFeedException {
		final DataFeed df = context.mock(DataFeed.class);
		final String table = "Test";
		final double x = 1.4567;
		final double y = 34.5612;
		// Add null as next value

		context.checking(new Expectations() {
			{
				allowing(dataBuilder).forTable(table);
				oneOf(dataBuilder).includeSettle();
				oneOf(dataBuilder).includeDate();
				oneOf(dataBuilder).reset();
				oneOf(dataBuilder).build();
				will(returnValue(df));
				exactly(2).of(df).getNextValue();
				will(onConsecutiveCalls(returnValue(new double[] { x, y }), returnValue(null)));
				oneOf(timeCharter).beginNewPointSeries(with(any(Comparable.class)), with(any(Boolean.class)));
				oneOf(timeCharter).addDataPoint(x, y);
			}
		});
		plotter = new DBPlotter(dataBuilder, timeCharter, dataCharter, log);
		plotter.withDomainAxis(DomainAxisType.Dates);
		plotter.forTable(table).plotSettle();

		context.assertIsSatisfied();
	}

	// add Plothigh tests etc... ??
}
