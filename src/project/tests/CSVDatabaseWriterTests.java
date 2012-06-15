package project.tests;

import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import project.csv.CSVDatabaseWriter;
import project.csv.CSVFileReader;
import project.database.DatabaseManagementSystem;
import project.database.exceptions.DataTableCreationException;
import project.io.Logger;

public class CSVDatabaseWriterTests {

	private final JUnit4Mockery context = new JUnit4Mockery();

	private CSVFileReader csv;
	private Logger log;
	private DatabaseManagementSystem db;
	private CSVDatabaseWriter writer;

	@Before
	public void setUp() {
		db = context.mock(DatabaseManagementSystem.class);
		csv = context.mock(CSVFileReader.class);
		log = context.mock(Logger.class);
	}

	@Test
	public void csvThrowsIOExceptionDBNotCalled() throws IOException {
		final String fileName = "Test";
		context.checking(new Expectations() {
			{
				allowing(csv).getFileName();
				will(returnValue(fileName));
				oneOf(csv).readNext();
				will(throwException(new IOException("Test")));
				allowing(log).writeln(with(any(String.class)));
			}
		});

		writer = new CSVDatabaseWriter(db, log);
		writer.storeCSVFile(csv);
		
		context.assertIsSatisfied();		
	}
	
	@Test
	public void tableCreationThrowsException_csvReaderNotCalled() throws IOException {
		context.checking(new Expectations() {
			{
				allowing(csv).getFileName();
				will(returnValue(null));
				oneOf(csv).readNext();
				will(returnValue(null));
				oneOf(db).createTable(with(any(String.class)), with(any(String[].class)));
				will(throwException(new DataTableCreationException("Test")));
				allowing(log).writeln(with(any(String.class)));
			}
		});

		writer = new CSVDatabaseWriter(db, log);
		writer.storeCSVFile(csv);
		
		context.assertIsSatisfied();
	}
	
	@Test
	public void CorrectValuesReadFromCSVCallsPutValues() throws IOException {
		final String tableName = "Test";
		final String[] cols = new String[]{"Col"};
		final String[] stringValues = new String[] {"1.2", "3.567" };
		final double[] values = new double[] {1.2, 3.567};
		context.checking(new Expectations() {
			{
				allowing(csv).getFileName();
				will(returnValue(tableName));
				exactly(2).of(csv).readNext();
				will(onConsecutiveCalls(returnValue(cols), returnValue(stringValues)));
				oneOf(db).createTable(tableName, cols);
				exactly(2).of(csv).hasNext();
				will(onConsecutiveCalls(returnValue(true), returnValue(false)));
				oneOf(db).putValues(tableName, values);
				allowing(log).writeln(with(any(String.class)));
			}
		});
		writer = new CSVDatabaseWriter(db, log);
		writer.storeCSVFile(csv);
		
		context.assertIsSatisfied();
	}

}
