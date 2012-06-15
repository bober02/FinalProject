package project.utis;

import java.util.Comparator;

public class DescendingDoubleComparator implements Comparator<Integer>
{
    private final double[] array; 

    public DescendingDoubleComparator(double[] array)
    {
        this.array = array;
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
    	double val1 = array[index1];
    	double val2 = array[index2];
    	
    	if(val1 < val2)
    		return 1;
    	else if(val1 > val2)
    		return -1;
    	return 0;
    }
}