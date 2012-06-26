package project;

import org.jfree.data.function.Function2D;
import org.jfree.data.function.NormalDistributionFunction2D;

public class MyGaussian extends NormalDistributionFunction2D implements Function2D {

	private double scale;
	
	public MyGaussian(double mean, double std, double scale) {
		super(mean, std);
		this.scale = scale;
	}
	
	public double getValue(double arg0){
		return scale * super.getValue(arg0);
		
	}

}
