package project;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.FastMath;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.NormalDistributionFunction2D;

import project.analysis.BayesianSolver;
import project.analysis.Regime;
import project.analysis.RegimeSolver;
import project.analysis.RegimeStrategy;
import project.analysis.SolverGraph;
import project.analysis.StandardDeviationSolver;
import project.analysis.StandardDeviationSolver2;
import project.analysis.detrend.Detrender;
import project.analysis.detrend.DetrenderGraph;
import project.analysis.detrend.DifferenceDetrender;
import project.analysis.detrend.LogDifferenceDetrender;
import project.analysis.detrend.RegressionDetrender;
import project.analysis.detrend.Smoother;
import project.analysis.statistics.WindowOnlineVariance;
import project.csv.CSVDatabaseWriter;
import project.csv.CSVFile;
import project.csv.CSVFileWriter;
import project.database.DatabaseManagementSystem;
import project.database.SQLDatabaseManager;
import project.database.connection.DatabaseConnection;
import project.database.connection.SQLDatabaseConnection;
import project.database.connection.SQLiteQueryExecutor;
import project.datafeed.DataFeed;
import project.datafeed.DataFeedBuilder;
import project.datafeed.DataFeedException;
import project.datafeed.RegimeGeneratingDataFeed;
import project.graphs.DBPlotter;
import project.graphs.DataSeriesCharter;
import project.graphs.DoubleSeriesCharter;
import project.graphs.HistogramCharter;
import project.graphs.TimeSeriesCharter;
import project.graphs.UTCTimeSeriesCharter;
import project.io.ConsoleLogger;
import project.utis.DataUtils;
import project.utis.Tables;
import project.utis.Timer;

import com.almworks.sqlite4java.SQLiteException;

public class Runner {

	private DatabaseConnection conn;
	private SQLiteQueryExecutor executor;
	private DatabaseManagementSystem dbManager;
	private CSVFileWriter writer;
	private TimeSeriesCharter timeCharter;
	private DataSeriesCharter dataCharter;
	private ConsoleLogger log;
	private DataFeedBuilder dfBuilder;
	private DBPlotter dbPlotter;
	private RegressionDetrender regressionDetrender;
	private DifferenceDetrender differenceDetrender;
	private LogDifferenceDetrender logDifferenceDetrender;
	private int smoothnessSpan = 30;
	private Smoother smoother;
	private StandardDeviationSolver stdSolver;
	private BayesianSolver bayesSolver;

	public Runner() {
		conn = new SQLDatabaseConnection(Tables.DATABASE);
		log = new ConsoleLogger();
		executor = new SQLiteQueryExecutor(conn.clone());
		dbManager = new SQLDatabaseManager(executor);
		writer = new CSVDatabaseWriter(dbManager, log);
		dfBuilder = new DataFeedBuilder(conn, log);
		dataCharter = new DoubleSeriesCharter();
		timeCharter = new UTCTimeSeriesCharter();
		dbPlotter = new DBPlotter(dfBuilder, timeCharter, dataCharter, log);
		regressionDetrender = new RegressionDetrender();
		differenceDetrender = new DifferenceDetrender();
		logDifferenceDetrender = new LogDifferenceDetrender();
		smoother = new Smoother(smoothnessSpan);
		bayesSolver = new BayesianSolver();
	}

	public void insertcsvFile(String fileName) throws IOException {
		writer.storeCSVFile(new CSVFile(fileName));
	}

	// Here goes all the main logic that would be invoked in the main()
	public void run() throws DataFeedException {
		// this.showGaussianHistogram();
		// this.showChiFunctions();
		// this.showGaussianFuntions();
		// this.showData();
		// this.smoothDetrendAnalysis();
		// this.detrendAnalysis();
		// measureSolverSpeed(1000000, 100);
		// this.testRealData();
		// this.testSyntheticData();
		 this.showSynteticDataResults();
		// this.compareVariancePerformance(100000, 0.01);
		// testLikelihoodsEquality();
		// this.generateTrend();
	}

	public void testRealData() throws DataFeedException {
		int maxRegimes = 5;
		int window = 100;
		Detrender detrender = differenceDetrender;
		RegimeStrategy strategy = RegimeStrategy.TopN;
		stdSolver = new StandardDeviationSolver(window, maxRegimes);
		stdSolver.setStrategy(strategy);
		StandardDeviationSolver2 stdSolver2 = new StandardDeviationSolver2(window, maxRegimes);
		stdSolver2.setStrategy(strategy);
		for (String table : Tables.getTables()) {
			DataFeed df = dfBuilder.forTable(table).includeDays().includeSettle().build();
			SolverGraph graph = new SolverGraph(stdSolver, new DoubleSeriesCharter(), log);
			graph.addDetrender(detrender);
			String title = "Regimes: " + strategy + " for " + table + " using " + window + " window";
			//graph.solveAndPlot(df, title, true);
		}
		strategy = RegimeStrategy.Buckets;
		stdSolver2.setStrategy(strategy);
		stdSolver.setStrategy(strategy);
		for (String table : Tables.getTables()) {
			DataFeed df = dfBuilder.forTable(table).includeDays().includeSettle().build();
			SolverGraph graph = new SolverGraph(stdSolver, new DoubleSeriesCharter(), log);
			graph.addDetrender(detrender);
			String title = "Regimes: " + strategy + " for " + "DowJones" + " using " + window + " window";
			graph.solveAndPlot(df, title, true);
		}
	}
	
	
	public void showSynteticDataResults() {
		RegimeGeneratingDataFeed df = new RegimeGeneratingDataFeed(2);
		// RegimeSolver solver = new StandardDeviationSolver(200, 5);
		RegimeSolver solver = new BayesianSolver();
		SolverGraph graph = new SolverGraph(solver, dataCharter, log);
		// graph.addDetrender(differenceDetrender);
		graph.solveAndPlot(df, "Syntethic regime test", true);
		Regime[] regimes = df.getRegimes();
	}

	public void testSyntheticData() throws DataFeedException {
		int trials = 10;
		Mean mean = new Mean();
		Variance variance = new Variance(false);
		double sumSquared = 0;
		double meanError = 0;
		int length = 0;
		for (int j = 0; j < trials; j++) {
			RegimeGeneratingDataFeed df = new RegimeGeneratingDataFeed(2);
			double[] trueRegimes = df.getRegimePoints();
			RegimeSolver solver = new BayesianSolver();
			double[] estimatedRegimes = solver.solve(df);
			double[] shorter, longer;
			if (estimatedRegimes.length < trueRegimes.length) {
				shorter = estimatedRegimes;
				longer = trueRegimes;
			} else {
				longer = estimatedRegimes;
				shorter = trueRegimes;
			}
			double squaredError = 0.0;
			int index = 0;
			for (int i = 0; i < longer.length; i++) {
				double xValue = longer[i];
				double diff = FastMath.abs(xValue - shorter[index]);
				while ((index + 1) < shorter.length) {
					double nextDiff = FastMath.abs(xValue - shorter[index + 1]);
					if (nextDiff < diff) {
						diff = nextDiff;
						index++;
					} else {
						break;
					}
					mean.increment(diff);
					variance.increment(diff);
					squaredError += diff * diff;
				}
			}
			int coefficient = longer.length - shorter.length;
			sumSquared += squaredError;
			length += longer.length;
			squaredError /= longer.length;
			meanError += coefficient * squaredError;
			mean.clear();
			variance.clear();

		}
		System.out.println("Sum: " + sumSquared / length);
		System.out.println("Mean: " + meanError / trials);
	}



	public void measureSolverSpeed(int size, int numTrials) throws DataFeedException {
		dataCharter.beginNewPointSeries("Solver speed", false);
		int maxRegimes = 3;
		int window = 1000;

		StandardDeviationSolver stdSolver = new StandardDeviationSolver(window, maxRegimes);
		BayesianSolver bayesSolver = new BayesianSolver();
		StandardDeviationSolver2 stdSolver2 = new StandardDeviationSolver2();
		Timer timer = new Timer();
		double time = 0;
		for (int i = 0; i <= numTrials; i++) {
			DataFeed df = new RegimeGeneratingDataFeed(maxRegimes, size);
			double[][] values = DataUtils.transpose(df.getAllValues());
			timer.start();
			stdSolver.solve(values[0], values[1]);
			timer.stop();
			time += timer.getTimeElapsed();
		}
		System.out.println(size + "   ,   " + time / numTrials);
	}

	public void smoothDetrendAnalysis() throws DataFeedException {
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

	public void detrendAnalysis() {
		String table = Tables.Corn;
		DataFeed df = dfBuilder.forTable(table).includeDays().includeSettle().build();

		DetrenderGraph smooth = new DetrenderGraph(new DoubleSeriesCharter(), log, differenceDetrender);
		smooth.detrendAndPlot(df, Tables.getTableName(table) + " with first differencing", true, false);

		df.reset();
		smooth = new DetrenderGraph(new DoubleSeriesCharter(), log, differenceDetrender);
		smooth.addDetrender(new DifferenceDetrender());
		smooth.detrendAndPlot(df, Tables.getTableName(table) + " with second differencing", true, false);
	}


	public void showGaussianHistogram() throws DataFeedException {
		// for (String table : Tables.getTables()) {
		DataFeed df = dfBuilder.forTable(Tables.SP).includeDays().includeSettle().build();
		double[][] data = DataUtils.transpose(df.getAllValues());

		double[] detrendedData = logDifferenceDetrender.detrend(Arrays.copyOfRange(data[0], 0, 3460), Arrays.copyOfRange(data[1], 0, 3460));
		// double[] detrendedData = logDifferenceDetrender.detrend(data[0],
		// data[1]);
		// double[] detrendedData = data[1];
		Mean mean = new Mean();
		Variance variance = new Variance(false);
		for (int i = 0; i < detrendedData.length; i++) {
			double val = detrendedData[i];
			mean.increment(val);
			variance.increment(val);
		}
		Function2D trueNormal = new NormalDistributionFunction2D(mean.getResult(), FastMath.sqrt(variance.getResult()));
		HistogramCharter charter = new HistogramCharter();
		charter.addFunction("Gaussian fit", trueNormal, -0.5, 0.5, 1000000);
		charter.addHistogram("Data count", detrendedData, 200);
		charter.showChart("S&P 500 data fit with logdifference detrending", "X", "P(X)");
		// }

	}

	public void showChiFunctions() {
		dataCharter.addSeries("k = 1", getDataSet(1, 100000), true);
		dataCharter.addSeries("k = 2", getDataSet(2, 100000), true);
		dataCharter.addSeries("k = 3", getDataSet(3, 100000), true);
		dataCharter.addSeries("k = 5", getDataSet(4, 100000), true);
		dataCharter.addSeries("k = 8", getDataSet(5, 100000), true);
		dataCharter.showChart("Probability density function", "X", "p(X)");
	}

	public double[][] getDataSet(int degrees, int numPoints) {
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
	
	public void generateTrend() {
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
		HistogramCharter charter = new HistogramCharter();
		charter.addFunction("μ=0, σ=3 ", N03, begin, end, 100000);
		charter.addFunction("μ=0, σ=1", N01, begin, end, 100000);
		charter.addFunction("μ=0, σ=0.4", N004, begin, end, 100000);
		charter.addFunction("μ=5, σ=2", N52, begin, end, 100000);
		charter.showChart("Probability density function", "X", "p(X)");
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
				} else
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
			System.out.println(x);
		}
		dataCharter.showChart("Execution time of variances for " + dataPoints + " data points", "Window length (% of data points)",
				"Execution time in seconds");
	}

	public void showData() throws DataFeedException {
		for (String table : Tables.getTables()) {
			DataFeed df = dfBuilder.forTable(table).includeDays().includeSettle().build();

			DetrenderGraph smooth = new DetrenderGraph(new DoubleSeriesCharter(), log);
			smooth.detrendAndPlot(df, "Settle price for " + Tables.getTableName(table), true, false);
		}
	}

	public void testLikelihoodsEquality() {
		double[] left = { 3, 4, 4, 4.5, 4, 5 };
		double[] right = { 8, 6, 8, 7, 8, 9, 9, 8.5, 5, 9, 7.6, 8.2, 7.9 };

		double l = getTrueLikelihood(left, right);
		double r = getEstLikelihood(left, right);

		int o = 9;
		o++;
	}

	public double getTrueLikelihood(double[] left, double[] right) {
		Mean mean = new Mean();
		Variance variance = new Variance(false);

		for (int i = 0; i < left.length; i++) {
			double val = left[i];
			mean.increment(val);
			variance.increment(val);
		}
		double m = mean.getResult();
		double std = FastMath.sqrt(variance.getResult());
		double trueLikelihood = 1.0;
		NormalDistribution dist = new NormalDistribution(m, std);
		for (int i = 0; i < left.length; i++) {
			double val = left[i];
			trueLikelihood *= dist.density(val);
		}

		mean.clear();
		variance.clear();
		for (int i = 0; i < right.length; i++) {
			double val = right[i];
			mean.increment(val);
			variance.increment(val);
		}
		m = mean.getResult();
		std = FastMath.sqrt(variance.getResult());
		dist = new NormalDistribution(m, std);
		for (int i = 0; i < right.length; i++) {
			double val = right[i];
			trueLikelihood *= dist.density(val);
		}
		return FastMath.log(trueLikelihood);
	}

	public double getEstLikelihood(double[] left, double[] right) {
		Variance variance = new Variance(false);

		for (int i = 0; i < left.length; i++) {
			double val = left[i];
			variance.increment(val);
		}
		double std = FastMath.sqrt(variance.getResult());
		double n = left.length;
		double estLikelihood = -n / 2 * FastMath.log(2 * Math.PI) - n * FastMath.log(std) - n / 2;

		variance.clear();
		for (int i = 0; i < right.length; i++) {
			double val = right[i];
			variance.increment(val);
		}
		std = FastMath.sqrt(variance.getResult());
		n = right.length;
		estLikelihood += -n / 2 * FastMath.log(2 * Math.PI) - n * FastMath.log(std) - n / 2;
		return estLikelihood;
	}

	public static void main(String[] args) throws SQLiteException, IOException, DataFeedException {
		new Runner().run();
	}
}
