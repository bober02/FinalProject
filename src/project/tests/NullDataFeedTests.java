package project.tests;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import project.datafeed.DataFeed;
import project.datafeed.DataFeedException;
import project.datafeed.NullDataFeed;

public class NullDataFeedTests {

	DataFeed df = new NullDataFeed();
	
	@Test
	public void getNextValueAlwaysReturnsNull(){
		try {
			double[] results = df.getNextValue();
			assertThat(results, nullValue());
		} catch (DataFeedException e) {
			fail("no exception expected from nullDataFeed");
		}
	}
	
	@Test
	public void getAllValuesAlwaysReturnsNull(){
		try {
			double[][] results = df.getAllValues();
			assertThat(results, nullValue());
		} catch (DataFeedException e) {
			fail("no exception expected from nullDataFeed");
		}
	}
	
	@Test
	public void resetAlwaysReturnsFalse(){
			boolean reset = df.reset();
			assertFalse(reset);
	}
}
