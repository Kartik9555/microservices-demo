package com.microservices.demo.analytics.service.dataaccess.repository.impl;

import com.microservices.demo.analytics.service.dataaccess.entity.BaseEntity;
import com.microservices.demo.analytics.service.dataaccess.repository.AnalyticsCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Slf4j
@Repository
public class AnalyticsRepositoryImpl<T extends BaseEntity<PK>, PK> implements AnalyticsCustomRepository<T, PK> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:50}")
    protected int batchSize;

    @Override
    @Transactional
    public <S extends T> PK persist(S entity) {
        this.entityManager.persist(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public <S extends T> void batchPersist(Collection<S> entities) {
        if(entities.isEmpty()){
            log.info("No entities found to insert");
            return;
        }
        int batchCount = 0;
        for (S entity : entities) {
            log.trace("Persisting entity with id {}", entity.getId());
            this.entityManager.persist(entity);
            batchCount++;
            if(batchCount % this.batchSize == 0){
                this.entityManager.flush();
                this.entityManager.clear();
            }
        }
        if(batchCount % this.batchSize != 0){
            this.entityManager.flush();
            this.entityManager.clear();
        }
    }

    @Override
    @Transactional
    public <S extends T> S merge(S entity) {
        return this.entityManager.merge(entity);
    }

    @Override
    @Transactional
    public <S extends T> void batchMerge(Collection<S> entities) {
        if(entities.isEmpty()){
            log.info("No entities found to merge");
            return;
        }
        int batchCount = 0;
        for (S entity : entities) {
            log.trace("Merging entity with id {}", entity.getId());
            this.entityManager.merge(entity);
            batchCount++;
            if(batchCount % this.batchSize == 0){
                this.entityManager.flush();
                this.entityManager.clear();
            }
        }
        if(batchCount % this.batchSize != 0){
            this.entityManager.flush();
            this.entityManager.clear();
        }
    }

    @Override
    public void clear() {
        this.entityManager.clear();
    }
}
