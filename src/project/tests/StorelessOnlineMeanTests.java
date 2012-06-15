package project.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import project.analysis.statistics.StorelessOnlineMean;

public class StorelessOnlineMeanTests {

	StorelessOnlineMean stat = new StorelessOnlineMean();

	@Test
	public void callingIncrementIncreasesN() {
		long n = stat.getN();
		stat.increment(1);
		stat.increment(5);

		Assert.assertEquals(stat.getN(), n + 2);
	}

	@Test(expected = ArithmeticException.class)
	public void callingRemoveOnNonInstantiatedMeanRaisesException() {
		stat.remove(1);
	}

	@Test
	public void callingRemoveDecreasesN() {
		stat.increment(1);
		stat.increment(5);
		long n = stat.getN();

		stat.remove(5);

		Assert.assertEquals(stat.getN(), n - 1);
	}
	
	@Test
	public void callingRemoveOn1ElementSetClears() {
		stat.increment(1);

		stat.remove(1);

		Assert.assertTrue(Double.isNaN(stat.getResult()));
	}

	@Test
	public void callingIncrementCorrectlyUpdatesMean() {

		double[] testValues = new double[] { 1, 4, 5, -6, 12, 15, 16, 17, 21, -2, 0 };
		double sum = 0.0;

		for (int i = 0; i < testValues.length; i++) {
			stat.increment(testValues[i]);
			sum += testValues[i];
			assertThat(stat.getResult(), equalTo(sum / (i + 1)));
		}
	}

	@Test
	public void callingRemoveCorrectlyUpdatesMean() {
		double[] testValues = new double[] { 1, 4, 5, -6, 12, 15, 16, 17, 21, -2, 0 };
		List<Double> storedValues = new ArrayList<Double>();
		double result = 3.38;
		// This is the value that we will check against
		stat.increment(result);
		for (int i = 0; i < testValues.length; i++) {
			stat.increment(testValues[i]);
			storedValues.add(testValues[i]);
		}
		Random rand = new Random();
		while(!storedValues.isEmpty())
			stat.remove(storedValues.remove(rand.nextInt(storedValues.size())));
		
		
		assertThat(Precision.round(stat.getResult(), 2), equalTo(result));
	}
}
