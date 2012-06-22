package project.database.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import project.database.exceptions.DatabaseConnectionException;
import project.database.exceptions.QueryException;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class SQLiteQueryExecutor implements DBQueryExecutor, DBResultQueryExecutor {

	private DatabaseConnection db;
	private double[][] result;
	private double[] stepResult;
	private String query;
	private SQLiteStatement statement;

	public SQLiteQueryExecutor(DatabaseConnection dbConnection) {
		db = dbConnection;
	}

	@Override
	public void prepareQuery(String query) throws QueryException {
		try {
			statement = db.prepare(query);
		} catch (DatabaseConnectionException e) {
			throw new QueryException("Could not prepare the query: " + e.getMessage());
		}
	}

	@Override
	public void executeQuery() throws QueryException {
		if (isFinished())
			throw new QueryException("No query has been set for execution or previous executions has finished");
		List<double[]> res = null;
		try {
			while (statement.step()) {
				if (res == null)
					res = new ArrayList<double[]>(100);
				int columns = statement.columnCount();
				double[] row = new double[columns];
				for (int i = 0; i < columns; i++)
					row[i] = statement.columnDouble(i);
				res.add(row);
			}
			if (res != null && res.size() > 0) {
				int columnSize = res.get(0).length;
				double[][] ret = new double[res.size()][columnSize];
				Iterator<double[]> it = res.iterator();
				int i = 0;
				while (it.hasNext()) {
					ret[i] = it.next();
					i++;
				}
				result = ret;
			}
		} catch (SQLiteException e) {
			throw new QueryException("An error occured while executing query: " + query + ". " + e.getMessage());
		} finally {
			statement = null;
		}
	}

	@Override
	public void executeQueryStep() throws QueryException {
		if (isFinished())
			throw new QueryException("No query has been set for execution or previous executions has finished");
		try {
			boolean hasData = statement.step();
			if (hasData) {
				int columns = statement.columnCount();
				double[] row = new double[columns];
				for (int i = 0; i < columns; i++)
					row[i] = statement.columnDouble(i);
				stepResult = row;
			}
			else {
				stepResult = null;
				statement = null;
			}
		} catch (SQLiteException e) {
			statement = null;
			stepResult = null;
			throw new QueryException("An error occured while executing query: " + query + ". " + e.getMessage());
		}
	}

	@Override
	public double[] getLastStepResult() {
		return stepResult;
	}

	@Override
	public double[][] getLastFullResult() {
		return result;
	}

	@Override
	public void finalize() {
		db.dispose();
	}

	@Override
	public boolean isFinished() {
		return statement == null;
	}

}
