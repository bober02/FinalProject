package project.csv;

public interface CSVFileWriter {
	
	/***
	 * Stores the content of the CSV file in the database
	 * @param CSVFile to be read
	 */
	public void storeCSVFile(CSVFileReader CSVFile);
	
	/***
	 * Stores the content of the CSV file in the database up to maximum rows specified
	 * @param CSVFile to be read
	 * @param maxRows - maximum number of rows to be read
	 */
	public void storeCSVFile(CSVFileReader CSVFile, long maxRows);

}
