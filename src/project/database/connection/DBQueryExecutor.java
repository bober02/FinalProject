package project.database.connection;

import project.database.exceptions.QueryException;

public interface DBQueryExecutor {

	/**
	 * Parses and prepares the query for the execution
	 * @param query to be processed
	 * @throws QueryException 
	 */
	void prepareQuery(String query) throws QueryException;
	
	/**
	 * Executes previously set query in full
	 * @throws QueryException 
	 */
	void executeQuery() throws QueryException;
	
	/**
	 * Returns true when the query has no further steps to execute
	 * @return true if query has been fully executed
	 */
	boolean isFinished();
	
}
