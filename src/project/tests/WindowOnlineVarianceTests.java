package project.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import project.analysis.statistics.WindowOnlineVariance;

public class WindowOnlineVarianceTests {

	private WindowOnlineVariance stat;

	@Test
	public void settingNoWindowResultsInSimpleOnlineVariance() {

		double[] testValues = new double[] { 1, 4, 5, -6, 12, 15, 16, 17, 21, -2, 0 };
		// Using common.math Variance as a true measure
		Variance trueVariance = new Variance();
		stat = new WindowOnlineVariance();

		for (int i = 0; i < testValues.length; i++) {
			stat.increment(testValues[i]);
			trueVariance.increment(testValues[i]);
		}
		assertThat(stat.getResult(), equalTo(trueVariance.getResult()));
	}
	
	@Test
	public void resettingClearsTheWindow(){
		stat = new WindowOnlineVariance(2);
		stat.increment(2);
		stat.increment(5);
		stat.increment(7);
		stat.clear();
		
		stat.increment(2);
		
		assertThat(stat.getResult(), equalTo(0.0));
	}

	@Test
	public void SettingWindowOf2CorrectlyComputesVariance() {
		testWindow(2);
	}

	@Test
	public void SettingWindowOf5CorrectlyComputesVariance() {
		testWindow(5);
	}

	private void testWindow(int size) {
		stat = new WindowOnlineVariance(size);
		int windowSize = size;
		Variance trueVariance = new Variance(true);
		double[] testValues = new double[] { 1, 5, 5, -6, 120, 15, 16, -2, 21, -2, 0, 45, 4 };

		for (int i = 0; i < testValues.length; i++) {
			double currValue = testValues[i];
			stat.increment(currValue);
			double trueRes = trueVariance.evaluate(Arrays.copyOfRange(testValues, Math.max(i + 1 - windowSize, 0),
					i + 1));

			assertThat(Precision.round(stat.getResult(), 6), equalTo(Precision.round(trueRes, 6)));
		}
	}

}
