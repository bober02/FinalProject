package project.csv;

import java.io.IOException;

import project.io.Logger;

public class DataInsertModule implements Runnable {

	private CSVFileWriter writer;
	private String[] filenames;
	private Logger log;
	private int files;
	private Thread t;

	public DataInsertModule(CSVFileWriter writer, Logger log,
			String... fileNames) {
		this.writer = writer;
		this.filenames = fileNames;
		this.log = log;
		files = fileNames.length;
	}

	public void start() {
		t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		int errors = 0;
		for (String filename : filenames) {
			try {
				log.writeln("Storing " + filename);
				CSVFileReader reader = new CSVFile(filename);
				writer.storeCSVFile(reader);
			} catch (IOException e) {
				log.writeln("File " + filename + " could not be read.");
				errors++;
			}
		}
		log.writeln("Storing has finished! Files to save: " + files
				+ "  Errors: " + errors);
	}

}
