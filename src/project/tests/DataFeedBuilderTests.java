package project.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import project.database.connection.DatabaseConnection;
import project.database.exceptions.DatabaseConnectionException;
import project.datafeed.DataFeed;
import project.datafeed.DataFeedBuilder;
import project.datafeed.DataFeedProvider;
import project.datafeed.NullDataFeed;
import project.datafeed.PriceDataFeed;
import project.io.Logger;

public class DataFeedBuilderTests {

	private Mockery context = new JUnit4Mockery();
	private DatabaseConnection dbConnection;
	private Logger log;
	private DataFeedProvider builder;

	@Before
	public void SetUp() {
		dbConnection = context.mock(DatabaseConnection.class);
		log = context.mock(Logger.class);
	}

	@Test
	public void noTableNameSetReturnsNullDataFeed() {
		builder = new DataFeedBuilder(dbConnection, log);
		DataFeed df = builder.includeDate().build();

		assertThat(df, instanceOf(NullDataFeed.class));
	}

	@Test
	public void noColumnSelectedReturnsNullDataFeed() {
		builder = new DataFeedBuilder(dbConnection, log);
		DataFeed df = builder.forTable("").build();

		assertThat(df, instanceOf(NullDataFeed.class));
	}

	@Test
	public void ResetReturnsNullDataFeed() {
		builder = new DataFeedBuilder(dbConnection, log);
		DataFeed df = builder.includeAll().reset().build();

		assertThat(df, instanceOf(NullDataFeed.class));
	}

	@Test
	public void properSetupCreatesPriceDataFeed()
			throws DatabaseConnectionException {
		context.checking(new Expectations() {
			{
				allowing(dbConnection).prepare(with(any(String.class)));
				will(returnValue(null));
			}
		});

		builder = new DataFeedBuilder(dbConnection, log);
		DataFeed df = builder.forTable("").includeDate().build();

		assertThat(df, instanceOf(PriceDataFeed.class));
	}

}