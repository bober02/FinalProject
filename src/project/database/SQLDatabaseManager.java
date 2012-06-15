package project.database;

import project.database.connection.DBQueryExecutor;
import project.database.exceptions.DataTableAccessException;
import project.database.exceptions.DataTableCreationException;
import project.database.exceptions.QueryException;

public class SQLDatabaseManager implements DatabaseManagementSystem {

	private DBQueryExecutor db;

	public SQLDatabaseManager(DBQueryExecutor db) {
		this.db = db;
	}

	@Override
	public void createTable(String tableName, String... columns) throws DataTableCreationException {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE table ");
		builder.append(tableName + "(");
		for (int i = 0; i < columns.length; i++) {
			builder.append(columns[i]);
			builder.append(" DOUBLE");
			if (i < columns.length - 1)
				builder.append(", ");
			else
				builder.append(")");
		}
		try {
			db.prepareQuery(builder.toString());
			db.executeQuery();
		} catch (QueryException e) {
			throw new DataTableCreationException("SQLException occured while creating table " + tableName + ": " + e.getMessage());
		}
	}

	@Override
	public void deleteTable(String tableName) {
		String query = "DROP table " + tableName;
		try {
			db.prepareQuery(query);
			db.executeQuery();
		} catch (QueryException e) {
			throw new DataTableAccessException("SQLException occured while dropping a table " + tableName+ ": " + e.getMessage());
		}
	}

	@Override
	public void putValues(String tableName, double... values) throws DataTableAccessException {
		StringBuilder builder = new StringBuilder();
		builder.append("INSERT INTO " + tableName);
		builder.append(" values(");
		for (int i = 0; i < values.length; i++) {
			builder.append(values[i]);
			if (i < values.length - 1)
				builder.append(", ");
			else
				builder.append(")");
		}
		try {
			db.prepareQuery(builder.toString());
			db.executeQuery();
		} catch (QueryException e) {
			throw new DataTableAccessException("SqlException occured while inserting values to " + tableName+ ": " + e.getMessage());
		}

	}
}
