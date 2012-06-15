package project.datafeed;

import project.database.connection.DBResultQueryExecutor;
import project.database.exceptions.QueryException;

public class PriceDataFeed implements DataFeed {
	
	private String query;
	private DBResultQueryExecutor executor;
	
	public PriceDataFeed(String query, DBResultQueryExecutor executor) throws DataFeedException{
		this.query = query;
		this.executor = executor;
		try {
			executor.prepareQuery(query);
		} catch (QueryException e) {
			throw new DataFeedException("Could initiate data feed based on the query provided: " + e.getMessage());
		}
	}

	@Override
	public double[] getNextValue() throws DataFeedException {
		try {
			if(!executor.isFinished())
				executor.executeQueryStep();
			return executor.getLastStepResult();
		} catch (QueryException e) {
			throw new DataFeedException("Data transfer error: " + e.getMessage());
		}
	}

	@Override
	public double[][] getAllValues() throws DataFeedException {
		try {
			executor.executeQuery();
			return executor.getLastFullResult();
		} catch (QueryException e) {
			throw new DataFeedException("Data transfer error: " + e.getMessage());
		}
	}
	
	@Override
	public boolean reset() {
		try {
			executor.prepareQuery(query);
			return true;
		} catch (QueryException e) {
			return false;
		}
	}

}
