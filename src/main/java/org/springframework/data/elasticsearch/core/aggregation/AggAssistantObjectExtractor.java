package org.springframework.data.elasticsearch.core.aggregation;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.springframework.data.elasticsearch.core.ResultsExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AggAssistantObjectExtractor<T> implements ResultsExtractor<T> {
    private final Function<Map<String, Object>, T> valueCollector;
    private List<AggAssistant> aggAssistants = new ArrayList<>();

    public AggAssistantObjectExtractor(Function<Map<String, Object>, T> valueCollector,
                                       AggAssistant... aggAssistants) {
        this.valueCollector = valueCollector;
        for (AggAssistant assistant : aggAssistants) {
            this.aggAssistants.add(assistant);
        }
    }

    @Override
    public T extract(SearchResponse response) {
        if (response.getFailedShards() > 0) {
            throw new RuntimeException(response.getShardFailures()[0].reason());
        }

        Map<String, Object> valueMap = new HashMap<>();
        Map<String, Aggregation> aggMap = response.getAggregations().asMap();
        for (AggAssistant assistant : aggAssistants) {
            Aggregation agg = aggMap.get(assistant.getName());
            valueMap.put(assistant.getName(), assistant.collectValue(agg));
        }
        return valueCollector.apply(valueMap);
    }
}
