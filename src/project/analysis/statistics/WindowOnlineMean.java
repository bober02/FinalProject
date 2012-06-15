package project.analysis.statistics;

public class WindowOnlineMean extends StorelessOnlineMean {

	private double[] values;
	private int index;
	private int windowSize;
	
	public WindowOnlineMean(){
		values = null;
		index = -1;
	}
	
	public WindowOnlineMean(int size){
		index = 0;
		windowSize = size;
		values = new double[windowSize];
		resetWindow();

	}
	
	private void resetWindow() {
		for(int i = 0; i< windowSize; i++){
			values[i] = Double.NaN;
		}		
	}

	@Override
	public void increment(double x) {
		//indicates that there is window
		if(values != null){
			if(!Double.isNaN(values[index])){
				double val = values[index];
				this.remove(val);
			}
			values[index] = x;
			if (++index == windowSize) index = 0;
		}	
		super.increment(x);
	}

	@Override
	public void clear() {
		super.clear();
		index = 0;
		resetWindow();		
	}

}
