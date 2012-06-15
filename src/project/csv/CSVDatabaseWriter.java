package project.csv;

import java.io.FileNotFoundException;
import java.io.IOException;

import project.database.DatabaseManagementSystem;
import project.database.exceptions.DataTableAccessException;
import project.database.exceptions.DataTableCreationException;
import project.io.Logger;
import project.utis.StringUtils;

public class CSVDatabaseWriter implements CSVFileWriter {

	private DatabaseManagementSystem db;
	private Logger log;

	public CSVDatabaseWriter(DatabaseManagementSystem db, Logger log) {
		this.db = db;
		this.log = log;
	}

	public void storeCSVFile(CSVFileReader csvReader) {
		storeCSVFile(csvReader, Long.MAX_VALUE);
	}

	@Override
	public void storeCSVFile(CSVFileReader csvReader, long maxRows) {
		int errors = 0;
		int lines = 0;
		String tableName = csvReader.getFileName();
		try {
			String[] cols = csvReader.readNext();
			db.createTable(tableName, cols);
			while (csvReader.hasNext() && lines < maxRows) {
				lines++;
				double[] values = StringUtils.convertStrings(csvReader
						.readNext());
				if (values != null) {
					db.putValues(tableName, values);
				} else {
					errors++;
				}
			}
		} catch (FileNotFoundException e) {
			log.writeln("Invalid CSV file!");
		} catch (IOException e) {
			log.writeln("CSV file reading error");
		} catch (DataTableCreationException e) {
			log.writeln(e.getMessage());
		} catch (DataTableAccessException e) {
			log.writeln(e.getMessage());
			// remove partially filled table
			db.deleteTable(tableName);
		}
		log.writeln("Filename: " + tableName);
		log.writeln("Lines read: " + lines);
		log.writeln("Errors: " + errors);
	}

}
