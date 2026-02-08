package com.microservices.demo.elastic.query.service.api;

import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceRequestModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/documents")
public class ElasticDocumentController {

    @GetMapping("/")
    public ResponseEntity<List<ElasticQueryServiceResponseModel>> getAllDocuments() {
        List<ElasticQueryServiceResponseModel> response = new ArrayList<>();
        log.info("Elasticsearch returned {} of documents", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElasticQueryServiceResponseModel> getDocumentById(@PathVariable String id) {
        ElasticQueryServiceResponseModel response = ElasticQueryServiceResponseModel.builder()
                .id(id)
                .build();
        log.info("Elasticsearch returned document with id {}", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-document-by-text")
    public ResponseEntity<List<ElasticQueryServiceResponseModel>> getDocumentsByText(@RequestBody ElasticQueryServiceRequestModel request) {
        List<ElasticQueryServiceResponseModel> response = new ArrayList<>();
        ElasticQueryServiceResponseModel model = ElasticQueryServiceResponseModel.builder()
                .text(request.getText())
                .build();
        response.add(model);
        log.info("Elasticsearch returned {} of document", response.size());
        return ResponseEntity.ok(response);
    }
}
