package project.graphs;

import project.datafeed.DataFeed;
import project.datafeed.DataFeedProvider;
import project.datafeed.DataFeedException;
import project.io.Logger;
import project.utis.DomainAxisType;

public class DBPlotter {

	private DataFeedProvider dataBuilder;
	private TimeSeriesCharter timeCharter;
	private DataSeriesCharter dataCharter;
	private Logger log;
	private DomainAxisType axisType;
	private String currentTable;
	
	public DBPlotter(DataFeedProvider dataBuilder, TimeSeriesCharter timeCharter, DataSeriesCharter dataCharter, Logger log) {
		this.dataBuilder = dataBuilder;
		this.timeCharter = timeCharter;
		this.dataCharter = dataCharter;
		this.log = log;
		axisType = DomainAxisType.Days;
	}
	
	public DBPlotter forTable(String tableName){
		dataBuilder.forTable(tableName);
		currentTable = tableName;
		return this;
	}
	
	public DBPlotter withDomainAxis(DomainAxisType axisType){
		this.axisType = axisType;
		return this;
	}
	
	public DBPlotter plotOpen(){
		dataBuilder.includeOpen();
		generateSeries("Open");
		return this;
	}
	public DBPlotter plotHigh(){
		dataBuilder.includeHigh();
		generateSeries("High");
		return this;
	}
	public DBPlotter plotLow(){
		dataBuilder.includeLow();
		generateSeries("Low");
		return this;
	}
	public DBPlotter plotSettle(){
		dataBuilder.includeSettle();
		generateSeries("Settle");
		return this;
	}
	
	public void showGraph(String title){
		if(!timeCharter.isEmpty())
			timeCharter.showChart(title, "Time (Date)", "Asset price" );
		if(!dataCharter.isEmpty())
			dataCharter.showChart(title, "Time (Days)", "Asset price");
		currentTable = null;
		axisType = DomainAxisType.Days;
	}

	private void generateSeries(String col) throws ChartingException {
		if(currentTable == null)
			throw new ChartingException("Table was not selected for plotting");
		if(axisType == DomainAxisType.Days){
			dataBuilder.includeDays();
			DataFeed df = dataBuilder.build();
			try {
				double[][] data = df.getAllValues();
				dataCharter.addSeries(currentTable + col, data, false);
			} catch (DataFeedException e) {
				log.writeln(e.getMessage());
			}
		}
		else{
			dataBuilder.includeDate();
			timeCharter.beginNewPointSeries(currentTable + "-" +col, false);
			DataFeed df = dataBuilder.build();
			try {
				double[] result = df.getNextValue();
				while(result != null){
					timeCharter.addDataPoint(result[0], result[1]);
					result = df.getNextValue();
				}
			} catch (DataFeedException e) {
				log.writeln(e.getMessage());
			}
		}		
		dataBuilder.reset();
		dataBuilder.forTable(currentTable);
	}
	
}
