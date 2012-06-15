package project.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.VoidAction;
import org.junit.Test;

import project.database.connection.DBResultQueryExecutor;
import project.database.exceptions.QueryException;
import project.datafeed.DataFeedException;
import project.datafeed.PriceDataFeed;

public class PriceDataFeedTests {

	private final JUnit4Mockery context = new JUnit4Mockery();

	private DBResultQueryExecutor db;
	private String query = "";
	private PriceDataFeed df;

	@Test(expected = DataFeedException.class)
	public void constructorThrowsException() throws QueryException, DataFeedException {
		db = context.mock(DBResultQueryExecutor.class);
		context.checking(new Expectations() {
			{
				oneOf(db).prepareQuery(query);
				will(throwException(new QueryException("Test")));
			}
		});
		df = new PriceDataFeed(query, db);
	}

	@Test
	public void queryExecutedFully_getNextValueReturnsLastResult() throws QueryException, DataFeedException {
		db = context.mock(DBResultQueryExecutor.class);
		final double[] mockResult = new double[] { 1.6, 2.4, 3.5678 };
		context.checking(new Expectations() {
			{
				oneOf(db).prepareQuery(query);
				oneOf(db).isFinished();
				will(returnValue(true));
				oneOf(db).getLastStepResult();
				will(returnValue(mockResult));
			}
		});
		df = new PriceDataFeed(query, db);

		double[] res = df.getNextValue();

		assertThat(res, equalTo(mockResult));
	}

	@Test
	public void querynotExecutedExecutesNextStep() throws QueryException, DataFeedException {
		db = context.mock(DBResultQueryExecutor.class);
		final double[] mockResult = new double[] { 1.6, 2.4, 3.5678 };
		context.checking(new Expectations() {
			{
				oneOf(db).prepareQuery(query);
				oneOf(db).isFinished();
				will(returnValue(false));
				oneOf(db).executeQueryStep();
				oneOf(db).getLastStepResult();
				will(returnValue(mockResult));
			}
		});
		df = new PriceDataFeed(query, db);

		df.getNextValue();

		context.assertIsSatisfied();
	}
	
	@Test
	public void ExecuteQueryReturnsmatrixOfValues() throws QueryException, DataFeedException {
		db = context.mock(DBResultQueryExecutor.class);
		final double[][] mockResult = new double[][] { new double[]{1.6, 2.4, 3.5678} };
		context.checking(new Expectations() {
			{
				oneOf(db).prepareQuery(query);
				oneOf(db).executeQuery();
				oneOf(db).getLastFullResult();
				will(returnValue(mockResult));
			}
		});
		df = new PriceDataFeed(query, db);

		double[][] res = df.getAllValues();

		assertThat(res, equalTo(mockResult));
	}

	@Test
	public void resetReturnsFalseOnExceptionFromDB() throws QueryException, DataFeedException {
		db = context.mock(DBResultQueryExecutor.class);
		context.checking(new Expectations() {
			{
				allowing(db).prepareQuery(query);
				will(onConsecutiveCalls(new VoidAction(), throwException(new QueryException("Test"))));
			}
		});
		df = new PriceDataFeed(query, db);

		boolean res = df.reset();
		
		assertTrue(!res);
	}

	@Test
	public void resetReturnsTrueOnNormalExecution() throws QueryException, DataFeedException {
		db = context.mock(DBResultQueryExecutor.class);
		context.checking(new Expectations() {
			{
				allowing(db).prepareQuery(query);
			}
		});
		df = new PriceDataFeed(query, db);

		boolean res = df.reset();
		
		assertTrue(res);
	}

}
