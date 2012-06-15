package project.csv;

public interface CSVFileWriter {
	
	/***
	 * Stores the content of the CSV file in the database
	 * @param File to be read
	 */
	public void storeCSVFile(CSVFileReader CSVFile);
	
	public void storeCSVFile(CSVFileReader CSVFile, long maxRows);

}
