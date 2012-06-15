package project.database.connection;

import project.database.exceptions.QueryException;

public interface DBResultQueryExecutor extends DBQueryExecutor {

	/**
	 * Executes only one step of query
	 * @throws QueryException if no more steps to perform or error occurs in the db
	 */
	void executeQueryStep() throws QueryException;
	
	/**
	 * Returns the result of the last query that was evaluated using executeQueryStep() method
	 * @return
	 */
	double[] getLastStepResult();
	
	/**
	 * Returns the result of the last query that was evaluated using executeQuery() method
	 * @return
	 */
	double[][] getLastFullResult();
	
}
