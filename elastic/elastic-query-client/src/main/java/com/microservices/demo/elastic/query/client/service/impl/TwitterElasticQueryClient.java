package com.microservices.demo.elastic.query.client.service.impl;

import com.microservices.demo.config.ElasticConfigData;
import com.microservices.demo.config.ElasticQueryConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.exception.ElasticQueryClientException;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.client.util.ElasticQueryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterElasticQueryClient implements ElasticQueryClient<TwitterIndexModel> {

    private final ElasticConfigData elasticConfigData;
    private final ElasticQueryConfigData elasticQueryConfigData;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticQueryUtil<TwitterIndexModel> elasticQueryUtil;

    @Override
    public TwitterIndexModel getIndexModelById(String id) {
        final var query = elasticQueryUtil.getSearchQueryById(id);
        final var searchHit = elasticsearchOperations.searchOne(query, TwitterIndexModel.class, IndexCoordinates.of(elasticConfigData.getIndexName()));
        if (searchHit == null) {
            log.error("No document found at elasticsearch with id {}", id);
           throw new ElasticQueryClientException("No document found at elasticsearch with id " + id);
        }
        log.info("Document with id {} retrieved successfully", searchHit.getId());
        return searchHit.getContent();
    }

    @Override
    public List<TwitterIndexModel> getIndexModelByText(String text) {
        final var query = elasticQueryUtil.getSearchQueryByFieldText(elasticQueryConfigData.getTextField(), text);
        return search(query, "{} of documents with text {} retrieved successfully", text);
    }

    @Override
    public List<TwitterIndexModel> getAllIndexModels() {
        final var query = elasticQueryUtil.getSearchQueryForAll();
        return search(query, "{} of documents retrieved successfully");
    }

    private @NonNull List<TwitterIndexModel> search(Query query, String logMessage, Object... logParams) {
        final var searchHits = elasticsearchOperations.search(query, TwitterIndexModel.class, IndexCoordinates.of(elasticConfigData.getIndexName()));
        log.info(logMessage, searchHits.getTotalHits(), logParams);
        return searchHits.get().map(SearchHit::getContent).toList();
    }
}
