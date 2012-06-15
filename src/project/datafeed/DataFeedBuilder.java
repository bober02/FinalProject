package project.datafeed;

import project.database.connection.DatabaseConnection;
import project.database.connection.SQLiteQueryExecutor;
import project.io.Logger;

public class DataFeedBuilder implements DataFeedProvider {

	private DatabaseConnection db;
	private Logger log;
	private String tableName;
	private boolean addDate;
	private boolean addOpen;
	private boolean addHigh;
	private boolean addLow;
	private boolean addSettle;
	private boolean addDays;

	public DataFeedBuilder(DatabaseConnection dbConnection, Logger log) {
		db = dbConnection;
		this.log = log;
	}

	@Override
	public DataFeedProvider includeDate() {
		addDate = true;
		return this;
	}

	@Override
	public DataFeedProvider includeDays() {
		addDays = true;
		return this;
	}

	@Override
	public DataFeedProvider includeOpen() {
		addOpen = true;
		return this;
	}

	@Override
	public DataFeedProvider includeHigh() {
		addHigh = true;
		return this;
	}

	@Override
	public DataFeedProvider includeLow() {
		addLow = true;
		return this;
	}

	@Override
	public DataFeedProvider includeSettle() {
		addSettle = true;
		return this;
	}

	@Override
	public DataFeedProvider forTable(String table) {
		tableName = table;
		return this;
	}

	@Override
	public DataFeedProvider includeAll() {
		includeDate();
		includeOpen();
		includeSettle();
		includeLow();
		includeHigh();
		return this;
	}

	@Override
	public DataFeedProvider reset() {
		addDate = false;
		addOpen = false;
		addHigh = false;
		addLow = false;
		addSettle = false;
		addDays = false;
		tableName = null;
		return this;
	}

	@Override
	public DataFeed build() {
		if (tableName == null
				|| (!addOpen && !addHigh & !addLow && !addSettle && !addDate))
			return new NullDataFeed();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ");
		if (addDate)
			builder.append("Date,");
		if (addDays)
			builder.append("Days,");
		if (addOpen)
			builder.append("Open,");
		if (addHigh)
			builder.append("High,");
		if (addLow)
			builder.append("Low,");
		if (addSettle)
			builder.append("Settle,");
		builder.delete(builder.length() - 1, builder.length());
		builder.append(" FROM " + tableName);
		try {
			return new PriceDataFeed(builder.toString(),
					new SQLiteQueryExecutor(db));
		} catch (DataFeedException e) {
			log.writeln(e.getMessage());
			return new NullDataFeed();
		} finally {
			reset();
		}
	}

}
