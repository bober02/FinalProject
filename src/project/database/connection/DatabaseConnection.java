package project.database.connection;

import com.almworks.sqlite4java.SQLiteStatement;

import project.database.exceptions.DatabaseConnectionException;

public interface DatabaseConnection {

	/**
	 * Closes this database connection. Should always be executed when db is no
	 * longer needed.
	 */
	void dispose();

	/**
	 * Prepares given query for internal execution. Failsafe - opens database
	 * connection if necessary.
	 * 
	 * @param query
	 *            to be processed
	 * @return Statement to be evaluated
	 * @throws DatabaseConnectionException
	 *             - this is thrown when database connection fails
	 */
	SQLiteStatement prepare(String query) throws DatabaseConnectionException;


	/**
	 * Clones the database connection for the purpose of multithreading. Note:
	 * classes implementing this also override default Object.clone() method.
	 * 
	 * @return Independent connection to the same database
	 */
	DatabaseConnection clone();

}
