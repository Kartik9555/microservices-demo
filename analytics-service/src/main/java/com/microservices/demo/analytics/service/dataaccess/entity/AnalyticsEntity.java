package com.microservices.demo.analytics.service.dataaccess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "twitter_analytics")
public class AnalyticsEntity implements BaseEntity<UUID> {

    @Id
    @NotNull
    @Column(name = "id", columnDefinition = "uuid")
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @Column(name = "word")
    @EqualsAndHashCode.Exclude
    private String word;

    @NotNull
    @Column(name = "word_count")
    @EqualsAndHashCode.Exclude
    private Long wordCount;

    @NotNull
    @Column(name = "record_date")
    @EqualsAndHashCode.Exclude
    private LocalDateTime recordDate;
}
