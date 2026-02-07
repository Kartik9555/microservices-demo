package com.microservices.demo.elastic.index.client.service.impl;

import com.microservices.demo.config.ElasticConfigData;
import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.index.client.util.ElasticIndexUtil;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterElasticIndexClient implements ElasticIndexClient<TwitterIndexModel> {

    private final ElasticConfigData elasticConfigData;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticIndexUtil<TwitterIndexModel> elasticIndexUtil;

    @Override
    public List<String> save(List<TwitterIndexModel> documents) {
        final var indexedQueries = elasticIndexUtil.getIndexQueries(documents);
        final var documentIds = elasticsearchOperations.bulkIndex(indexedQueries, IndexCoordinates.of(elasticConfigData.getIndexName()))
                .stream()
                .map(IndexedObjectInformation::id)
                .collect(Collectors.toList());
        log.info("Documents indexed successfully with type: {} and ids: {}", TwitterIndexModel.class.getName(), documentIds);
        return documentIds;
    }
}
