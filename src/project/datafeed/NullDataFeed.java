package project.datafeed;

/**
 * Null pattern - rather than returning a null upon incorrect parameters passed
 * to the builder
 * 
 */
public class NullDataFeed implements DataFeed {

	@Override
	public double[] getNextValue() throws DataFeedException {
		return null;
	}

	@Override
	public double[][] getAllValues() throws DataFeedException {
		return null;
	}

	@Override
	public boolean reset() {
		return false;
	}

}
