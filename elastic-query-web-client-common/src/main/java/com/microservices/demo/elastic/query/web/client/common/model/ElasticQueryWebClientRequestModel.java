package com.microservices.demo.elastic.query.web.client.common.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElasticQueryWebClientRequestModel {
    private String id;
    @NotEmpty
    private String text;
}
