package project.datafeed;

public interface DataFeedProvider {

	DataFeedProvider includeDate();

	DataFeedProvider includeDays();

	DataFeedProvider includeOpen();

	DataFeedProvider includeHigh();

	DataFeedProvider includeLow();

	DataFeedProvider includeSettle();

	DataFeedProvider forTable(String table);

	DataFeedProvider includeAll();

	DataFeedProvider reset();

	DataFeed build();

}