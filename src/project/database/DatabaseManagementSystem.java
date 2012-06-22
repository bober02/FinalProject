package project.database;

import project.database.exceptions.DataTableCreationException;

/**
 * Encapsulates standard SQL procedures
 *
 */
public interface DatabaseManagementSystem {

	/**
	 * 
	 * Creates a new table if tableName is different to all existing tables in
	 * the database.
	 * 
	 * @param tableName
	 *            of the table to be created (must be unique)
	 * @param columns
	 *            to be entered
	 * @throws DataTableCreationException
	 */
	public void createTable(String tableName, String... columns) throws DataTableCreationException;

	/**
	 * Delete given table.
	 * 
	 * @param tableName
	 */
	public void deleteTable(String tableName);

	/**
	 * Inserts given values into a given table.
	 * 
	 * @param Name
	 *            of the table to be updated
	 * @param Values
	 *            to be inserted
	 */
	public void putValues(String tableName, double... values);

}
