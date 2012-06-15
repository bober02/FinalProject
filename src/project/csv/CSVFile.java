package project.csv;

import java.io.FileReader;
import java.io.IOException;
import au.com.bytecode.opencsv.CSVReader;

public class CSVFile implements CSVFileReader {

	private CSVReader reader;
	private String filename;
	private String[] next;
	
	public CSVFile(String fileName) throws IOException {
		String[] paths = fileName.split("\\.");
		if(paths.length == 1)
			fileName += ".csv";
		reader = new CSVReader(new FileReader(fileName));
		filename = paths[0];
		next = reader.readNext();
	}

	public String getFileName() {
		return filename;
	}

	@Override
	public String[] readNext() throws IOException {
		String [] ret = next;
		next =  reader.readNext();
		return ret;
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}
}
