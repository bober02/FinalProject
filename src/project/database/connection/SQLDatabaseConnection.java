package project.database.connection;

import java.io.File;

import project.database.exceptions.DatabaseConnectionException;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class SQLDatabaseConnection implements DatabaseConnection {

	private SQLiteConnection db;
	private String dbPath;

	public SQLDatabaseConnection(String dbPath) {
		this.dbPath = dbPath;
	}

	@Override
	public void dispose() {
		if(db != null)
			db.dispose();
	}

	@Override
	public SQLiteStatement prepare(String query)
			throws DatabaseConnectionException {
		try {
			if(db == null)
				db = new SQLiteConnection(new File(dbPath));
			if(!db.isOpen())
				db.open();
			return db.prepare(query);
		} catch (SQLiteException e) {
			throw new DatabaseConnectionException(e.getMessage());
		}
	}
		
	@Override
	public DatabaseConnection clone(){
		return new SQLDatabaseConnection(dbPath);
	}

}
