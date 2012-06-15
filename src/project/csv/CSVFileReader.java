package project.csv;

import java.io.IOException;

public interface CSVFileReader {

	boolean hasNext();

	String[] readNext() throws IOException;

	String getFileName();
}
