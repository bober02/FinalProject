package project.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import project.analysis.statistics.WindowOnlineMean;

public class WindowOnlineMeanTests {

	WindowOnlineMean stat;
	
	@Test
	public void settingNoWindowResultsInSimpleOnlineMean() {

		double[] testValues = new double[] { 1, 4, 5, -6, 12, 15, 16, 17, 21, -2, 0 };
		double sum = 0.0;
		stat = new WindowOnlineMean();

		for (int i = 0; i < testValues.length; i++) {
			stat.increment(testValues[i]);
			sum += testValues[i];
			assertThat(stat.getResult(), equalTo(sum / (i + 1)));
		}
	}
	
	@Test
	public void resettingClearsTheWindow(){
		stat = new WindowOnlineMean(2);
		stat.increment(2);
		stat.increment(5);
		stat.increment(7);
		stat.clear();
		
		stat.increment(2);
		
		assertThat(stat.getResult(), equalTo(2.0));
	}

	@Test
	public void SettingWindowOf2CorrectlyComputesMean() {
		int windowSize = 2;
		stat = new WindowOnlineMean(windowSize);
		
		stat.increment(1);
		stat.increment(4);
		assertThat(Precision.round(stat.getResult(), 2), equalTo((1.0+4)/windowSize));
		
		stat.increment(5);
		assertThat(Precision.round(stat.getResult(), 2), equalTo((4.0+5)/windowSize));
		
		stat.increment(7);
		assertThat(Precision.round(stat.getResult(), 2), equalTo((5.0+7)/windowSize));
		
		stat.increment(-4);
		assertThat(Precision.round(stat.getResult(), 2), equalTo((7.0-4)/windowSize));
	}
	
	@Test
	public void SettingWindowOf5CorrectlyComputesMean() {
		int windowSize = 4;
		stat = new WindowOnlineMean(windowSize);
		
		stat.increment(1);
		stat.increment(4);
		stat.increment(5);
		stat.increment(7);
		assertThat(Precision.round(stat.getResult(), 2), equalTo(4.25));
		
		stat.increment(5);
		assertThat(Precision.round(stat.getResult(), 2), equalTo((4.0+5+7+5)/windowSize));
		
		stat.increment(-12);
		assertThat(Precision.round(stat.getResult(), 2), equalTo((5.0+7 + 5 -12)/windowSize));
		
		stat.increment(3);
		assertThat(Precision.round(stat.getResult(), 2), equalTo((7 + 5 -12 + 3.0)/windowSize));
	}
}
