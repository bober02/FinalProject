package project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.NormalDistributionFunction2D;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import project.analysis.GaussianSolver;
import project.analysis.Regime;
import project.analysis.RegimeGeneratingDataFeed;
import project.analysis.RegimeSolver;
import project.analysis.SolverGraph;
import project.analysis.StandardDeviationSolver;
import project.analysis.detrend.Detrender;
import project.analysis.detrend.DetrenderGraph;
import project.analysis.detrend.DifferenceDetrender;
import project.analysis.detrend.LogDifferenceDetrender;
import project.analysis.detrend.Smoother;
import project.analysis.statistics.WindowOnlineVariance;
import project.database.connection.DatabaseConnection;
import project.database.connection.SQLDatabaseConnection;
import project.datafeed.DataFeed;
import project.datafeed.DataFeedBuilder;
import project.datafeed.DataFeedException;
import project.graphs.DataSeriesCharter;
import project.graphs.DoubleSeriesCharter;
import project.graphs.HistogramCharter;
import project.io.ConsoleLogger;
import project.utis.DataUtils;
import project.utis.Tables;
import project.utis.Timer;

import com.almworks.sqlite4java.SQLiteException;

public class Runner {

	private DatabaseConnection conn;
	private DataSeriesCharter dataCharter;
	private ConsoleLogger log;
	private DataFeedBuilder dfBuilder;
	private DifferenceDetrender differenceDetrender;
	private LogDifferenceDetrender logDifferenceDetrender;

	private static int MAX_REGIMES = 5;

	public Runner() {
		conn = new SQLDatabaseConnection(Tables.DATABASE);
		log = new ConsoleLogger();
		dfBuilder = new DataFeedBuilder(conn, log);
		dataCharter = new DoubleSeriesCharter();
		differenceDetrender = new DifferenceDetrender();
		logDifferenceDetrender = new LogDifferenceDetrender();
	}

	public static void main(String[] args) throws SQLiteException, IOException, DataFeedException {
		new Runner().run();
	}

	// Here goes all the main logic that would be invoked in the main()
	public void run() throws DataFeedException {
		// this.testSyntheticData(100, new StandardDeviationSolver(1000, 0.04),
		// false, differenceDetrender);
		// this.testSyntheticData(100, new GaussianSolver(), true);
		// this.testSyntheticGaussianParameter(100);
		this.showQQPlots(dfBuilder.forTable(Tables.Corn).includeDays().includeSettle().build(), logDifferenceDetrender);
		 //showRealDataResults(new GaussianSolver(), logDifferenceDetrender);
	}

	// ***************************************************** //
	// ********** TESTS ON REAL AND SYNTHETIC DATA ********* //
	// ***************************************************** //

	public void showRealDataResults(RegimeSolver solver, Detrender... detrenders) throws DataFeedException {
		for (String table : Tables.getTables()) {
			DataFeed df = dfBuilder.forTable(table).includeDays().includeSettle().build();
			SolverGraph graph = new SolverGraph(solver, new DoubleSeriesCharter(), log);
			for (Detrender d : detrenders) {
				graph.addDetrender(d);
			}
			String title = "Regimes  for " + Tables.getTableName(table);
			graph.solveAndPlot(df, title, true);
		}
	}

	public void testSyntheticData(int trials, RegimeSolver solver, boolean randomizeVariance, Detrender... detrenders) throws DataFeedException {
		Mean mean = new Mean();
		Variance variance = new Variance(false);
		Random rand = new Random();
		ArrayList<Double> errors = new ArrayList<Double>(100);
		int length = 0;
		int failed = 0;
		double sumSquared = 0;
		double worstCase = 0;
		for (int j = 0; j < trials; j++) {
			RegimeGeneratingDataFeed df = new RegimeGeneratingDataFeed(2 + rand.nextInt(6), randomizeVariance);
			double[] trueRegimes = df.getRegimes();
			double[][] values = DataUtils.transpose(df.getAllValues());
			double[] xs = values[0];
			double[] ys = values[1];
			for (Detrender d : detrenders) {
				ys = d.detrend(xs, ys);
			}
			double[] estimatedRegimes = solver.solve(xs, ys);
			double[] shorter, longer;
			if (estimatedRegimes.length < trueRegimes.length) {
				shorter = estimatedRegimes;
				longer = trueRegimes;
			}
			else {
				longer = estimatedRegimes;
				shorter = trueRegimes;
			}
			if (longer.length != shorter.length) {
				failed++;
				continue;
			}
			double trialSquaredError = 0.0;
			int index = 0;
			int[] foundRegimes = new int[trueRegimes.length];
			for (int i = 0; i < longer.length; i++) {
				double xValue = longer[i];
				double diff = FastMath.abs(xValue - shorter[index]);
				while ((index + 1) < shorter.length) {
					double nextDiff = FastMath.abs(xValue - shorter[index + 1]);
					if (nextDiff < diff) {
						diff = nextDiff;
						index++;
					}
					else {
						break;
					}
				}
				foundRegimes[index] = 1;
				mean.increment(diff);
				variance.increment(diff);
				trialSquaredError += diff * diff;
			}
			int found = 0;
			for (int i = 0; i < foundRegimes.length; i++) {
				found += foundRegimes[i];
			}
			if (found != trueRegimes.length) {
				failed++;
				continue;
			}
			if (trialSquaredError > worstCase)
				worstCase = trialSquaredError;
			errors.add(FastMath.sqrt(trialSquaredError));
			sumSquared += trialSquaredError;
			length += longer.length;
			mean.clear();
			variance.clear();
		}
		log.writeln("trials: " + trials);
		log.writeln("failed: " + failed);
		log.writeln("Worst RMS: " + Precision.round(FastMath.sqrt(worstCase), 3));
		log.writeln("Rootm mean quare error: " + Precision.round(FastMath.sqrt(sumSquared / length), 3));
		HistogramCharter histogramCharter = new HistogramCharter(HistogramType.RELATIVE_FREQUENCY);
		histogramCharter.addHistogram("RMS", ArrayUtils.toPrimitive(errors.toArray(new Double[0])), 15);
		histogramCharter.showChart("Root mean squared errors", "Error", "Relative frequency");
	}

	public void testSyntheticGaussianParameter(int trials) throws DataFeedException {
		Random rand = new Random();
		RegimeSolver solver = new GaussianSolver();
		ArrayList<Double> meanErrors = new ArrayList<Double>(trials);
		ArrayList<Double> stdDevErrors = new ArrayList<Double>(trials);
		double meanError = 0, stdDevError = 0;
		int totalPoints = 0;
		for (int j = 0; j < trials; j++) {
			RegimeGeneratingDataFeed df = new RegimeGeneratingDataFeed(2 + rand.nextInt(6), true);
			double[] trueRegimeEndings = df.getRegimes();
			double[][] values = DataUtils.transpose(df.getAllValues());
			double[] xs = values[0];
			double[] ys = values[1];
			double[] estimatedRegimeEndings = solver.solve(xs, ys);
			totalPoints += xs.length;

			Regime[] trueRegimes = constructRegimePoints(xs, ys, trueRegimeEndings);
			Regime[] estRegimes = constructRegimePoints(xs, ys, estimatedRegimeEndings);
			int trueRegimeIndex = 0, estRegimeIndex = 0;
			Regime trueRegime = trueRegimes[trueRegimeIndex];
			Regime estRegime = estRegimes[estRegimeIndex];
			double trialMeanError = 0, trialStdDevError = 0;
			for (int i = 0; i < xs.length; i++) {
				if (xs[i] > trueRegime.getRegimeEnd()) {
					trueRegimeIndex++;
					trueRegime = trueRegimes[trueRegimeIndex];
				}
				if (xs[i] > estRegime.getRegimeEnd()) {
					estRegimeIndex++;
					estRegime = estRegimes[estRegimeIndex];
				}
				trialMeanError += FastMath.pow((trueRegime.getMean() - estRegime.getMean()) / trueRegime.getMean(), 2);
				trialStdDevError += FastMath.pow((trueRegime.getStdDev() - estRegime.getStdDev()) / trueRegime.getStdDev(), 2);
			}
			meanError += trialMeanError;
			stdDevError += trialStdDevError;
			meanErrors.add(FastMath.sqrt(trialMeanError / xs.length));
			stdDevErrors.add(FastMath.sqrt(trialStdDevError / xs.length));
		}
		log.writeln("Trials: " + trials);
		log.writeln("Total points generated: " + totalPoints);
		log.writeln("Trial RMS of mean estimate: " + Precision.round(FastMath.sqrt(meanError / trials), 3));
		log.writeln("Trial RMS of std dev estimate: " + Precision.round(FastMath.sqrt(stdDevError / trials), 3));
		log.writeln("Point RMS of mean estimate: " + Precision.round(FastMath.sqrt(meanError / totalPoints), 3));
		log.writeln("Point RMS of std dev estimate: " + Precision.round(FastMath.sqrt(stdDevError / totalPoints), 3));
		HistogramCharter histogramCharter = new HistogramCharter(HistogramType.RELATIVE_FREQUENCY);
		histogramCharter.addHistogram("RMS", ArrayUtils.toPrimitive(meanErrors.toArray(new Double[0])), 20);
		histogramCharter.showChart("Frequency of RMS error of mean for one data point across all trials", "Error", "Relative frequency");
		histogramCharter = new HistogramCharter(HistogramType.RELATIVE_FREQUENCY);
		histogramCharter.addHistogram("RMS", ArrayUtils.toPrimitive(stdDevErrors.toArray(new Double[0])), 20);
		histogramCharter
				.showChart("Frequency of RMS error of standard deviation for one data point across all trials", "Error", "Relative frequency");
	}

	private Regime[] constructRegimePoints(double[] xs, double[] ys, double[] regimeEnds) {
		Regime[] regimes = new Regime[regimeEnds.length];
		Mean mean = new Mean();
		Variance variance = new Variance(false);
		int regimeIndex = 0;
		int i = 0;
		for (double regime : regimeEnds) {
			mean.clear();
			variance.clear();
			while (i < xs.length && xs[i] <= regime) {
				mean.increment(ys[i]);
				variance.increment(ys[i]);
				i++;
			}
			regimes[regimeIndex] = new Regime(mean.getResult(), FastMath.sqrt(variance.getResult()), regime);
			regimeIndex++;
		}
		return regimes;
	}

	public void showQQPlots(DataFeed df, Detrender... detrenders) {
		RegimeSolver solver = new GaussianSolver();
		double[][] values;
		try {
			values = DataUtils.transpose(df.getAllValues());
		} catch (DataFeedException e) {
			log.writeln("Exception happened while obtaining data from the feed: " + e.getMessage());
			return;
		}
		double[] xs = values[0];
		double[] ys = values[1];
		for (Detrender d : detrenders) {
			ys = d.detrend(xs, ys);
		}
		double[] regimeEnds = solver.solve(xs, ys);
		Regime[] regimes = constructRegimePoints(xs, ys, regimeEnds);

		Percentile empirical = new Percentile();
		int begin = 0, end = 0, index = 0;
		for (Regime r : regimes) {
			XYSeriesCollection quantiles = new XYSeriesCollection();
			XYSeries series = new XYSeries("QQ plot", false, true);
			NormalDistribution dist = new NormalDistribution(r.getMean(), r.getStdDev());
			while (end < xs.length && xs[end] <= r.getRegimeEnd()) {
				end++;
			}
			empirical.setData(Arrays.copyOfRange(ys, begin, end));
			for (double percentile = 1; percentile < 100; percentile += 1) {
				double fit = dist.inverseCumulativeProbability(percentile / 100);
				double emp = empirical.evaluate(percentile);
				series.add(emp, fit);
			}
			begin = end;
			index++;
			quantiles.addSeries(series);
			JFreeChart chart = ChartFactory.createScatterPlot("QQ plot - segment " + index, "Empirical quantile", "Fit quantile", quantiles, PlotOrientation.VERTICAL, true,
					true, false);
			XYPlot plot = chart.getXYPlot();
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
			renderer.setSeriesShape(0, ShapeUtilities.createUpTriangle(5));
			renderer.setSeriesShapesFilled(0, false);
			DefaultXYDataset line = new DefaultXYDataset();
			line.addSeries(
					"true fit",
					new double[][] { new double[] { empirical.evaluate(1), empirical.evaluate(99) },
							new double[] { empirical.evaluate(1), empirical.evaluate(99) } });
			plot.setDataset(1, line);
			plot.setRenderer(1, new StandardXYItemRenderer());
			ChartFrame frame = new ChartFrame("Chart window", chart);
			frame.setSize(1500, 1400);
			frame.setVisible(true);
		}

		
	}

	public void showSynteticDataResults(RegimeSolver solver, Detrender... detrenders) {
		for (int i = 0; i < 1; i++) {
			RegimeGeneratingDataFeed df = new RegimeGeneratingDataFeed(MAX_REGIMES);
			SolverGraph graph = new SolverGraph(solver, new DoubleSeriesCharter(), log);
			for (Detrender d : detrenders) {
				graph.addDetrender(d);
			}
			graph.solveAndPlot(df, "Syntethic regime test", true);

		}
	}

	// ***************************************************** //
	// ************ SPEED TESTS AND COMPARISONS ************ //
	// ***************************************************** //

	public void measureSolverSpeedMultipleSeries(RegimeSolver solver, int size, int numSeries, Detrender... detrenders) throws DataFeedException {
		dataCharter.beginNewPointSeries("Solver speed", false);
		Timer timer = new Timer();
		double time = 0;
		for (int j = 0; j < 10; j++) {
			timer.start();
			for (int i = 0; i <= numSeries; i++) {
				DataFeed df = new RegimeGeneratingDataFeed(MAX_REGIMES, size);
				double[][] values = DataUtils.transpose(df.getAllValues());
				double[] xs = values[0];
				double[] ys = values[1];
				for (Detrender d : detrenders) {
					ys = d.detrend(xs, ys);
				}
				solver.solve(xs, ys);
			}
			timer.stop();
			time += timer.getTimeElapsed();
		}
		log.writeln(size + "   ,   " + time / 10);
	}

	public void measureSolverSpeed(RegimeSolver solver, int size, int numTrials, Detrender... detrenders) throws DataFeedException {
		dataCharter.beginNewPointSeries("Solver speed", false);
		Timer timer = new Timer();
		double time = 0;
		for (int i = 0; i <= numTrials; i++) {
			DataFeed df = new RegimeGeneratingDataFeed(MAX_REGIMES, size);
			double[][] values = DataUtils.transpose(df.getAllValues());
			timer.start();
			double[] xs = values[0];
			double[] ys = values[1];
			for (Detrender d : detrenders) {
				ys = d.detrend(xs, ys);
			}
			solver.solve(values[0], values[1]);
			timer.stop();
			time += timer.getTimeElapsed();
		}
		log.writeln(size + "   ,   " + time / numTrials);
	}

	public void compareVariancePerformance(int dataPoints, double percentageIncrease) {
		String stat = "Static";
		String online = "Online";
		dataCharter.beginNewPointSeries(stat, false);
		dataCharter.beginNewPointSeries(online, false);
		for (int windowSize = 1; windowSize < dataPoints; windowSize += dataPoints * percentageIncrease) {
			WindowOnlineVariance onlineVariance = new WindowOnlineVariance(windowSize);
			Variance staticVariance = new Variance();
			double[] data = new double[windowSize];
			Random rand = new Random();
			int index = 0;

			long start = System.currentTimeMillis();
			for (int i = 0; i < dataPoints; i++) {
				double nextVal = rand.nextInt(50);
				data[index] = nextVal;
				if (i < windowSize) {
					double[] values = Arrays.copyOfRange(data, 0, i + 1);
					staticVariance.evaluate(values);
				}
				else
					staticVariance.evaluate(data);
				if (++index == windowSize)
					index = 0;
			}
			long end = System.currentTimeMillis();
			double staticTime = ((double) end - start) / 1000;

			start = System.currentTimeMillis();
			for (int i = 0; i < dataPoints; i++) {
				double nextVal = rand.nextInt(50);
				onlineVariance.increment(nextVal);
			}
			onlineVariance.clear();
			end = System.currentTimeMillis();
			double onlineTime = ((double) end - start) / 1000;

			double x = ((double) windowSize) / dataPoints;
			dataCharter.addDataPoint(stat, x, staticTime);
			dataCharter.addDataPoint(online, x, onlineTime);
			log.writeln("Current: " + x);
		}
		dataCharter.showChart("Execution time of variances for " + dataPoints + " data points", "Window length (% of data points)",
				"Execution time in seconds");
	}

	// ***************************************************** //
	// ******* GAUSSIAN AD CHI-SQUARED APPROXIMATIONS ****** //
	// ************** TRENDED DATA GENERATION ************** //
	// ***************************************************** //

	public void showChiFunctions() {
		dataCharter.addSeries("k = 1", getChiSquaredDataSet(1, 100000), true);
		dataCharter.addSeries("k = 2", getChiSquaredDataSet(2, 100000), true);
		dataCharter.addSeries("k = 3", getChiSquaredDataSet(3, 100000), true);
		dataCharter.addSeries("k = 5", getChiSquaredDataSet(4, 100000), true);
		dataCharter.addSeries("k = 8", getChiSquaredDataSet(5, 100000), true);
		dataCharter.showChart("Probability density function", "X", "p(X)");
	}

	private double[][] getChiSquaredDataSet(int degrees, int numPoints) {
		ChiSquaredDistribution dist = new ChiSquaredDistribution(degrees);
		Random r = new Random();
		double[][] res = new double[2][numPoints];
		for (int i = 0; i < numPoints; i++) {
			res[0][i] = r.nextDouble() * 10;
		}
		Arrays.sort(res[0]);
		for (int i = 0; i < numPoints; i++) {
			res[1][i] = dist.density(res[0][i]);
		}
		return res;
	}

	public void showTrendedData() {
		int points = 50;
		int cutoff = 35;
		double[] ys = new double[2 * points];
		double[] xs = new double[2 * points];
		Random r = new Random();
		double value = 0.0;
		double mult = 1.0;
		for (int i = 0; i <= cutoff; i++) {
			value += mult * r.nextDouble();
			ys[i] = value;
			xs[i] = i;
			mult *= 1 + r.nextDouble() / 7;
		}
		xs[cutoff + 1] = cutoff + 1;
		ys[cutoff + 1] = value - 0.5;
		for (int i = cutoff + 2; i < 2 * points; i++) {
			value += -1.5 * r.nextDouble();
			ys[i] = value;
			xs[i] = i;
		}
		dataCharter.addDomainMarker(cutoff);
		dataCharter.addSeries("Trend Data", xs, ys);
		dataCharter.showChart("Trend in data", "Time (days)", "Asset price");
	}

	public void showGaussianFuntions() {
		int begin = -8;
		int end = 9;
		Function2D N03 = new NormalDistributionFunction2D(0, 3);
		Function2D N01 = new NormalDistributionFunction2D(0, 1);
		Function2D N004 = new NormalDistributionFunction2D(0, 0.6);
		Function2D N52 = new NormalDistributionFunction2D(4, 2);
		HistogramCharter charter = new HistogramCharter(HistogramType.SCALE_AREA_TO_1);
		charter.addFunction("mean=0, sd=3 ", N03, begin, end, 100000);
		charter.addFunction("mean=0, sd=1", N01, begin, end, 100000);
		charter.addFunction("mean=0, sd=0.4", N004, begin, end, 100000);
		charter.addFunction("mean=5, sd=2", N52, begin, end, 100000);
		charter.showChart("Probability density function", "X", "p(X)");
	}

	public void showGaussianHistogram() throws DataFeedException {
		DataFeed df = dfBuilder.forTable(Tables.SP).includeDays().includeSettle().build();
		double[][] data = DataUtils.transpose(df.getAllValues());

		double[] detrendedData = logDifferenceDetrender.detrend(Arrays.copyOfRange(data[0], 0, 3460), Arrays.copyOfRange(data[1], 0, 3460));
		Mean mean = new Mean();
		Variance variance = new Variance(false);
		for (int i = 0; i < detrendedData.length; i++) {
			double val = detrendedData[i];
			mean.increment(val);
			variance.increment(val);
		}
		Function2D trueNormal = new NormalDistributionFunction2D(mean.getResult(), FastMath.sqrt(variance.getResult()));
		HistogramCharter charter = new HistogramCharter(HistogramType.SCALE_AREA_TO_1);
		charter.addFunction("Gaussian fit", trueNormal, -0.5, 0.5, 1000000);
		charter.addHistogram("Data count", detrendedData, 200);
		charter.showChart("S&P 500 data fit with logdifference detrending", "X", "P(X)");
	}

	// ***************************************************** //
	// ******* SHOWING DATA AND DE-TRENDING TECHNIQUES ***** //
	// ***************************************************** //

	public void showData() throws DataFeedException {
		for (String table : Tables.getTables()) {
			DataFeed df = dfBuilder.forTable(table).includeDays().includeSettle().build();
			DetrenderGraph smooth = new DetrenderGraph(new DoubleSeriesCharter(), log);
			smooth.detrendAndPlot(df, "Settle price for " + Tables.getTableName(table), true, false);
		}
	}

	public void showSmoothingDetrending() throws DataFeedException {
		String table = Tables.SP2;
		DataFeed df = dfBuilder.forTable(table).includeDays().includeSettle().build();
		double[][] values = DataUtils.transpose(df.getAllValues());
		double[] xs = values[0];
		double[] ys = values[1];
		dataCharter.addSeries("Original Data", xs, ys);
		double[] detrendYs;
		Smoother smooth = new Smoother(11);
		detrendYs = smooth.detrend(xs, ys);
		dataCharter.addSeries("10 points span", xs, detrendYs);

		smooth = new Smoother(41);
		detrendYs = smooth.detrend(xs, ys);
		dataCharter.addSeries("40 points span", xs, detrendYs);

		smooth = new Smoother(151);
		detrendYs = smooth.detrend(xs, ys);
		dataCharter.addSeries("150 points span", xs, detrendYs);
		dataCharter.showChart(Tables.getTableName(table) + " smoothing", "Time (days", "Asset price");
	}

	public void showDifferenceDetrending() {
		String table = Tables.Corn;
		DataFeed df = dfBuilder.forTable(table).includeDays().includeSettle().build();

		DetrenderGraph smooth = new DetrenderGraph(new DoubleSeriesCharter(), log, differenceDetrender);
		smooth.detrendAndPlot(df, Tables.getTableName(table) + " with first differencing", true, false);

		df.reset();
		smooth = new DetrenderGraph(new DoubleSeriesCharter(), log, differenceDetrender);
		smooth.addDetrender(new DifferenceDetrender());
		smooth.detrendAndPlot(df, Tables.getTableName(table) + " with second differencing", true, false);
	}

}
