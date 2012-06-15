package project.database;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import project.database.connection.DatabaseConnection;
import project.database.exceptions.DataTableAccessException;
import project.utis.StringUtils;

import com.almworks.sqlite4java.SQLiteStatement;

public class DBControl {

	private Set<String> tables;
	private DatabaseConnection db;

	public DBControl(DatabaseConnection dbConnection) {
		db = dbConnection;
		tables = new HashSet<String>();
	}

	private void updateTablesList() {
		String query = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
		try {
			SQLiteStatement st = db.prepare(query);
			while (st.step()) {
				tables.add(st.columnString(0));
			}
		} catch (Exception e) {
			System.out
					.println("Exception has occurred while trying to update tables list: "
							+ e.getMessage());
		}

	}

	public void printTables() {
		updateTablesList();
		System.out.println("Tables: ");
		System.out.println(StringUtils.generateString('-', 75));
		for (String table : tables) {
			System.out.print(StringUtils.padRight(table, 10) + "|");
			System.out.println();
		}

	}

	public void clearDatabase() {
		updateTablesList();
		Iterator<String> it = tables.iterator();
		while (it.hasNext()) {
			String table = it.next();
			String query = "DROP table " + table;
			try {
				SQLiteStatement st = db.prepare(query);
				st.step();
				it.remove();
			} catch (Exception e) {
				throw new DataTableAccessException(
						"Exception occured while clearing database: "
								+ e.getMessage());
			}
		}

	}

	public void printValues(String tableName) {
		printValues(tableName, Long.MAX_VALUE);
	}

	public void printValues(String tableName, long limit) {
		System.out.println("TABLE: " + tableName);
		String query = "SELECT * FROM " + tableName + " LIMIT " + limit;
		try {
			SQLiteStatement st = db.prepare(query);
			int columns = st.columnCount();
			for (int i = 0; i < columns; i++) {
				System.out.print(StringUtils.padRight(st.getColumnName(i), 10)
						+ "|");
			}
			System.out.println();
			System.out.println(StringUtils.generateString('-', columns * 11));
			while (st.step()) {
				for (int i = 0; i < columns; i++) {
					Double val = st.columnDouble(i);
					System.out.print(StringUtils.padRight(val.toString(), 10)
							+ "|");
				}
				System.out.println();
			}
		} catch (Exception e) {
			throw new DataTableAccessException(
					"SqlException occured while reading from " + tableName);
		}
	}

	@Override
	public void finalize(){
		db.dispose();
	}
}
